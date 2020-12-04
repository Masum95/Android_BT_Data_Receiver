import torch
from torch import nn
from models.layers_bayesian import *
from bayesian.misc import ModuleWrapper, FlattenLayer
from bayesian.BBB_LRT.BBBConv import BBBConv1d
from bayesian.BBB_LRT.BBBLinear import BBBLinear


# from bayesian.BBB.BBBConv import BBBConv1d
# from bayesian.BBB.BBBLinear import BBBLinear


class Bayesian_Deepbeat(ModuleWrapper):
    def __init__(self, dropout=1.00):
        # dropout 0 refers to no dropout in the model

        super(Bayesian_Deepbeat, self).__init__()
        self.convpool1 = conv_pool_block(in_channels=1, out_channels=64, kernel_size=10)
        self.convpool2 = conv_pool_block(in_channels=64, out_channels=45, kernel_size=8)
        self.convpool3 = conv_pool_block(in_channels=45, out_channels=50, kernel_size=5, pool_size=2, pool_stride=2)

        self.bn1 = nn.BatchNorm1d(num_features=50)
        self.clbd1 = conv_activation_batch_dropout(in_channels=50, out_channels=64, kernel_size=4, strides=3,
                                                   activation='lrelu', dropout_rate=0.118249401 * dropout)
        self.clbd2 = conv_activation_batch_dropout(in_channels=64, out_channels=35, kernel_size=4, strides=3,
                                                   activation='lrelu', dropout_rate=0.5449968 * dropout)
        self.clbd3 = conv_activation_batch_dropout(in_channels=35, out_channels=64, kernel_size=4, strides=1,
                                                   activation='lrelu', dropout_rate=0.5678640 * dropout)

        ##### part left #######
        # crbd = conv_relu_batchnorm_dropout
        self.crbd1 = conv_activation_batch_dropout(in_channels=64, out_channels=35, kernel_size=5, strides=3,
                                                   activation='relu', dropout_rate=0.3737667 * dropout)

        self.crbd2 = conv_activation_batch_dropout(in_channels=35, out_channels=25, kernel_size=4, strides=3,
                                                   activation='relu', dropout_rate=0.413829 * dropout)

        self.crbd3 = conv_activation_batch_dropout(in_channels=25, out_channels=35, kernel_size=3, strides=1,
                                                   activation='relu', dropout_rate=0.413829 * dropout)

        self.flatten = FlattenLayer(1)

        self.linear1 = nn.Sequential(
            BBBLinear(in_features=35, out_features=175),
            nn.Softplus()
        )

        self.rhythm_output = nn.Sequential(
            BBBLinear(in_features=175, out_features=2),
        )

