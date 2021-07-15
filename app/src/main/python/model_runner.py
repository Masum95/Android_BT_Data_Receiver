import numpy as np
import pandas as pd
import os
import torch
from torch.nn import functional as F
from models.deepbeat_bayesian import Bayesian_Deepbeat
import math
import json
import random

def sliding_window(x, w=800, o=400):
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


def run_model(model_path, ppg_signals):
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
	return outputs.numpy()


# reading csv file
# 0-NonAF, 1-AF
def input_preprocessing(model_path, csv_filepath):
	"""
    Following program takes csv_directory as input, does necessary operations and then outputs another csv in same directory.
    :param csv_filepath // path where all csv files will be stored
    :return:
    """

	df = pd.read_csv(csv_filepath, engine='python', header=None)
	df = df[df[0]>40]
	ppg = df[1].to_numpy()
	# return ppg
	# return df[1].to_numpy().shape
	randNum = random.randint(60, 100)
	return json.dumps({'predict_ara': np.argmax(run_model(model_path, ppg), axis=1), 'hear_rate_data': { 'activity': 'W', 'hr': str(randNum) }})


def input_preprocessingTmp():
	"""
    Following program takes csv_directory as input, does necessary operations and then outputs another csv in same directory.
    :param csv_filepath // path where all csv files will be stored
    :return:
    """
	randNum = random.randint(60, 100)

	return json.dumps({'predict_ara': '[1 0 0 1]', 'hear_rate_data': { 'activity': 'W', 'hr': str(randNum) }})
