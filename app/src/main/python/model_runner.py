import numpy as np
import pandas as pd
import os
import torch
from torch.nn import functional as F
from models.deepbeat_bayesian import Bayesian_Deepbeat
import math
import json
import random

import heartpy as hp
import matplotlib.pyplot as plt
from scipy.signal import resample
import sys
from sklearn.preprocessing import scale
from sklearn.preprocessing import minmax_scale

'''def sliding_window(x, w=800, o=400):
	"""
    x: 1D numpy array
    w: size of each window
    o: min len of overlapping
    """
	st = 0
	en = 0
	ranges = []
	while en < len(x):
		en = st+w
		if en > len(x):
			en = len(x)
			st = len(x)-w
		ranges.append((st, en))
		# print(st, en)
		st = en-o
	X = np.zeros(shape=(len(ranges),w))
	for r in range(len(ranges)):
		X[r]=x[ranges[r][0]:ranges[r][1]]
	return X
'''
#Divide signal in segments using window_size & overlap. Eliminate segments that has -
#i) a range that is at least 50% of range of the raw signal
#ii) a maximum that is 90% the raw signal’s maximum
#iii) a minimum that is the (minimum + 10%) of the raw signal

def Layer_1(raw, windowsize, overlap=0, enable_print=False):
    mx = np.max(raw)
    mn = np.min(raw)
    global_range = mx - mn

    #windowsize = 300
    #filtered = []
    final_filtered_data = []
    end = 0
    i = 0

    while (end+windowsize)<=len(raw):
        start = int((i * windowsize) -  (overlap * windowsize * i))
        end = int(start + windowsize)
        i = i + 1
        if enable_print:
            print('Range ', i, ': ')
            print('Start :', start)
            print('End :', end)
        sliced = raw[start:end]
        rng = np.max(sliced) - np.min(sliced)

        if ((rng >= (0.5 * global_range))
            or
            (np.max(sliced) >= 0.9 * mx)
            or
            (np.min(sliced) <= mn )):

            if enable_print:
                print('Rejected!')
            #for x in sliced:
            #    filtered.append(0)
        else:
            if enable_print:
                print('Accepted!')
            filtered = []
            for x in sliced:
                filtered.append(x)
            final_filtered_data.append(filtered)
        #filtered.clear()
        if enable_print:
            print('\n')

    return final_filtered_data

#Passing through a Band-pass filter (0.5Hz - 4.00Hz) / (30bpm -240 bpm)

def Layer_2(layer1_output, minFreq=0.5, maxFreq=4.00, sample_rate=10.00, enable_print=False):
    layer2_output = np.empty([len(layer1_output), len(layer1_output[0])])
    for i in range (len(layer1_output)):
        band_filtered = hp.filter_signal(np.array(layer1_output[i]), [minFreq, maxFreq], sample_rate=sample_rate,
                                         order=3, filtertype='bandpass')
        layer2_output[i] = band_filtered
    return layer2_output

#Resample the signal (increase frequency)
#using freq_enhancer_factor = 10
def Layer_3(layer2_output, freq_enhancer_factor = 3.2, sample_rate=10.00, enable_print=False):
    layer3_output = []
    for i in range (layer2_output.shape[0]):
        resampled_sig = resample(layer2_output[i], int(len(layer2_output[i]) * freq_enhancer_factor))
        layer3_output.append(resampled_sig)
    new_sample_rate = sample_rate * freq_enhancer_factor
    return (layer3_output, new_sample_rate)

#Segments are eliminated those hertpy can’t process due to ‘bad signal warning’
#Handled with try-except
#Returns the working_data too for later peak analysis
def Layer_4(layer3_output, new_sample_rate, enable_print=False):
    layer4_output = []
    reject = 0
    wd_list = []
    for i in range (len(layer3_output)):
        #print(i)
        try:
            wd, m = hp.process(np.array(layer3_output[i]), sample_rate = new_sample_rate, bpmmin=30, bpmmax=240)
            layer4_output.append(layer3_output[i])
            wd_list.append(wd['binary_peaklist'])
            #hp.plotter(wd, m)
        except:
            #print('HeartPy couldn\'t process')
            reject = reject + 1
    if enable_print:
        print('Rejection Percentage: ', (reject*100.00)/len(layer3_output))
    return (layer4_output, wd_list)

#Check peak detection ratio.
#Eliminate signals using a peak_acceptance_cutoff ratio.
#Currently using 70%

def Layer_5(layer4_output, wd_list, peak_acceptance_cutoff=0.7, enable_print=False):
    layer5_output = []
    for i in range (len(layer4_output)):
        ratio = np.count_nonzero(wd_list[i]==1)/(len(wd_list[i]))
        if enable_print:
            print(i, ': ', ratio)
        if ratio>=peak_acceptance_cutoff:
            layer5_output.append(layer4_output[i])
    return layer5_output

#Downsample to desired new_freq (32Hz) + divide in segments of desired duration (25sec)
#Standardize [0,1] the signal

def Layer_6(layer5_output, freq=32, sec=25, new_sample_rate=100.00, enable_print=False):
    layer6_output = []
    length = freq * sec
    new_freq = (length*new_sample_rate)/(len(layer5_output[0]))
    for i in range(len(layer5_output)):
        resampled = resample(np.array(layer5_output[i]), int(len(layer5_output[i])*new_freq/new_sample_rate))
        for j in range(len(resampled)//length):
            start = j * length
            end = (j+1) * length
            #norm = np.linalg.norm(resampled)
            #normal_arr = resampled / norm
            #layer6_output.append(normal_arr)
            #print(resampled[start:end])
            resampled_scaled = minmax_scale(resampled[start:end], feature_range=(0, 1), axis=0, copy=False)
            #print(resampled_scaled)
            layer6_output.append(resampled_scaled)

    fin_layer6_output = np.array(layer6_output)
    return (fin_layer6_output, new_freq)

def preprocess_bayesbeat(raw, sample_rate=10, plot=False):
    layer1_output = Layer_1(raw, 250, 0.6, enable_print=False)
    layer2_output = Layer_2(layer1_output, enable_print=False)
    layer3_output, new_sample_rate = Layer_3(layer2_output, freq_enhancer_factor=3.2,
                                            sample_rate=sample_rate, enable_print=False)
    layer4_output, wd_list = Layer_4(layer3_output, new_sample_rate, enable_print=False)
    layer5_output = Layer_5(layer4_output, wd_list, 0.3, enable_print=False)
    layer6_output, new_freq = Layer_6(layer5_output,  new_sample_rate=new_sample_rate, enable_print=False)
    final_output = np.reshape(np.copy(layer6_output), (layer6_output.shape[0], layer6_output.shape[1], 1))
    return final_output, layer6_output, new_freq


'''def run_model(model_path, ppg_signals):
	ppg = ppg_signals
	ppg = (ppg-ppg.min())/(ppg.max()-ppg.min())

	batches = sliding_window(ppg, w=800, o=400) #np.zeros((len(ppg)//800, 1, 800))
	batches = batches.reshape(batches.shape[0], 1, batches.shape[1])

	model = Bayesian_Deepbeat()
	model.load_state_dict(torch.load(model_path)['state_dict'])
	model.eval()

	outputs = None
	batches = torch.Tensor(batches)
	with torch.no_grad():
		outputs, _ = model(batches)
		outputs = F.log_softmax(outputs, dim=1)
		outputs = torch.exp(outputs)
	return outputs.numpy()'''

def run_model_new(model_path, final_tens):
    model = Bayesian_Deepbeat()
    model.load_state_dict(torch.load(model_path)['state_dict'])
    model.eval()
    with torch.no_grad():
    	outputs, _ = model(final_tens)
    	#outputs = F.log_softmax(outputs, dim=1)
    	#outputs = torch.exp(outputs)
    return outputs.numpy()


def avg_hr_activity(df):
    """
    average of valid hrs and most frequent activity
    """
    df = df.loc[(10 <= df[0] ) & (df[0] <= 250)]
    return df[0].mean(), df[13].value_counts()[:1].index[0]


# reading csv file
# 0-NonAF, 1-AF
def input_preprocessing(model_path, csv_filepath):

#    Following program takes csv_directory as input, does necessary operations and then outputs another csv in same directory.
#    :param csv_filepath // path where all csv files will be stored
#    :return:

    df = pd.read_csv(csv_filepath, engine='python', header=None)
    raw = df.values[:, 1]
    final_output, layer6_output, new_freq = preprocess_bayesbeat(raw, sample_rate=10, plot=False)
    final_tens = torch.Tensor(final_output).view(-1, 1, 800)

    (hr, activity) = avg_hr_activity(df)
    return json.dumps({'predict_ara': np.argmax(run_model_new(model_path, final_tens), axis=1).tolist(), 'hear_rate_data': { 'activity': activity, 'hr': hr }})


def input_preprocessingTmp():
	"""
    Following program takes csv_directory as input, does necessary operations and then outputs another csv in same directory.
    :param csv_filepath // path where all csv files will be stored
    :return:
    """
	randNum = random.randint(60, 100)

	return json.dumps({'predict_ara': '[1 0 0 1]', 'hear_rate_data': { 'activity': 'W', 'hr': str(randNum) }})
