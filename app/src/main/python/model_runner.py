import numpy as np
import pandas as pd
import os
import torch

# reading csv file
def input_preprocessing(model_path, csv_filepath):
	"""
	Following program takes csv_directory as input, does necessary operations and then outputs another csv in same directory.
	:param csv_filepath // path where all csv files will be stored
	:return:
	"""
	df = pd.read_csv(csv_filepath + "myfile.csv" , engine='python')
	df.iloc[:,1]  =  df.iloc[:,1] + 1
	df.to_csv(csv_filepath + '/modified.csv', index=False)
	loaded_model = torch.load(model_path + "bayesbeat_cpu.pt")

	return os.listdir(csv_filepath)
