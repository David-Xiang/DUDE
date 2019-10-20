#from __future__ import print_function
import argparse
import torch
import torch.nn as nn
import torch.nn.functional as F
from thop import profile

from torch.autograd import Variable

IMAGE_LENGTH = 28
INPUT_NODE = 784
OUTPUT_NODE = 10

class Net(nn.Module):
    def __init__(self, num, size):
        super(Net, self).__init__()
        self.num = num
        self.size = size
        self.fc = nn.ModuleList()
        self.fc.append(nn.Linear(INPUT_NODE, self.size))
        for i in range(self.num - 1):
            self.fc.append(nn.Linear(self.size, self.size))
        self.fc.append(nn.Linear(self.size, OUTPUT_NODE))
        print(self.fc)

    def forward(self, x):
        # nn.Linear()的输入输出都是维度为一的值，所以要把多维度的tensor展平成一维
        x = x.view(x.size()[0], -1)
        for i in range(self.num):
            x = self.fc[i](x)
            x = F.relu(x)
        x = self.fc[self.num](x)
        return F.softmax(x, dim=1)

def main():
    # Training settings
    parser = argparse.ArgumentParser(description="Gernerate pretrained DNN Mnist model given hidden layers' number and size.")
    parser.add_argument("-n", "--num", type=int, required=True, help="Number of hidden layers")
    parser.add_argument("-s", "--size", type=int, required=True, help="Size of hidden layers")
    
    parser.add_argument('--no-cuda', action='store_true', default=False,
                        help='disables CUDA training')
    parser.add_argument('--seed', type=int, default=1, metavar='S',
                        help='random seed (default: 1)')
    
    args = parser.parse_args()
    use_cuda = not args.no_cuda and torch.cuda.is_available()

    torch.manual_seed(args.seed)

    device = torch.device("cuda" if use_cuda else "cpu")
    
    # kwargs = {'num_workers': 1, 'pin_memory': True} if use_cuda else {}
    # kwargs = {}
    # train_loader = torch.utils.data.DataLoader(
    #     datasets.MNIST('../../Data', train=True, download=True,
    #                    transform=transforms.Compose([
    #                        transforms.ToTensor(),
    #                        transforms.Normalize((0.1307,), (0.3081,))
    #                    ])),
    #     batch_size=args.batch_size, shuffle=True, **kwargs)
    # test_loader = torch.utils.data.DataLoader(
    #     datasets.MNIST('../../Data', train=False, transform=transforms.Compose([
    #                        transforms.ToTensor(),
    #                        transforms.Normalize((0.1307,), (0.3081,))
    #                    ])),
    #     batch_size=args.test_batch_size, shuffle=True, **kwargs)


    model = Net(args.num, args.size).to(device)

    # for epoch in range(1, args.epochs + 1):
    #     train(args, model, device, train_loader, optimizer, epoch)
    #     test(args, model, device, test_loader)
    
    batch_size = 1    # just a random number

    # Input to the model
    x = Variable(torch.randn(batch_size, 1, 28, 28), requires_grad=True)


    name = "mnist-%d-%d" % (args.num, args.size)
    inputs = torch.randn(1, 1, 28, 28)
    flops, params = profile(model, inputs=(inputs,))
    with open("flops.txt", "a") as f:
        f.write("%s: %s flops\n" % (name, str(flops)))
    print("Flops : " + str(flops))
    # Export the model
    torch_out = torch.onnx._export(model,             # model being run
                                x,                       # model input (or a tuple for multiple inputs)
                                "%s.onnx" % (name),       # where to save the model (can be a file or file-like object)
                                export_params=True)      # store the trained parameter weights inside the model file

    # import onnx
    # from onnx import helper
    # from caffe2.python.onnx.backend import Caffe2Backend as backend
    # import numpy as np

    # # Load the ONNX GraphProto object. Graph is a standard Python protobuf object
    # model = onnx.load("mnist-%d-%d.onnx" % (args.num, args.size))

    # # prepare the caffe2 backend for executing the model this converts the ONNX graph into a
    # # Caffe2 NetDef that can execute it. Other ONNX backends, like one for CNTK will be
    # # availiable soon.
    # prepared_backend = backend.prepare(model)

    # # run the model in Caffe2

    # # Construct a map from input names to Tensor data.
    # # The graph itself contains inputs for all weight parameters, followed by the input image.
    # # Since the weights are already embedded, we just need to pass the input image.
    # # last input the grap
    # W = {model.graph.input[0].name: x.data.numpy()}

    # # Run the Caffe2 net:
    # c2_out = prepared_backend.run(W)[0]

    # # Verify the numerical correctness upto 3 decimal places
    # np.testing.assert_almost_equal(torch_out.data.cpu().numpy(), c2_out, decimal=3)

    # # Export to mobile

    # init_net, predict_net = backend.onnx_graph_to_caffe2_net(model, device="CPU")
    # with open("mnist-%d-%d-init.pb"%(args.num, args.size), "wb") as f:
    #     f.write(init_net.SerializeToString())
    # with open("mnist-%d-%d-predict.pb"%(args.num, args.size), "wb") as f:
    #     f.write(predict_net.SerializeToString())

    # # Verify it runs with predictor
    # with open("mnist-%d-%d-init.pb"%(args.num, args.size), "rb") as f:
    #     init_net = f.read()
    # with open("mnist-%d-%d-predict.pb"%(args.num, args.size), "rb") as f:
    #     predict_net = f.read()
    # from caffe2.python import workspace
    # p = workspace.Predictor(init_net, predict_net)
    # # The following code should run:
    # img = np.random.rand(1, 1, 28, 28).astype(np.float32)
    # p.run([img])
        
if __name__ == '__main__':
    main()
