import os
import onnx
from caffe2.python.onnx.backend import Caffe2Backend as backend


num = [1, 2, 4, 8, 16, 32]
size = [32, 64, 128, 256, 512, 1024]
for n in num:
    for s in size:
        # train and get onnx model file
        os.system("python3 mnist.py -n %d -s %d" % (n, s))
        
        modelName = "mnist-%d-%d" % (n, s)
        
        # Load the ONNX GraphProto object. Graph is a standard Python protobuf object
        # model = onnx.load("%s.onnx" % modelName)

        # init_net, predict_net = backend.onnx_graph_to_caffe2_net(model, device="CPU")
        # with open("%s-init.pb" % modelName, "wb") as f:
        #     f.write(init_net.SerializeToString())
        # with open("%s-predict.pb" % modelName, "wb") as f:
        #     f.write(predict_net.SerializeToString())


