import tvm
from tvm import relay
from tvm import rpc
from tvm.contrib import util, ndk
from tvm.contrib import graph_runtime
import numpy as np
from PIL import Image
import time
import keras
from keras.applications.resnet50 import preprocess_input

#model_path = "keras/mobilenetV2.h5"
#model_path = "keras/densenet121.h5"
model_path = "keras/resnet50.h5"

image_path = "ilsvrc2012/images/ILSVRC2012_val_00000001.JPEG"
target = "llvm -target=aarch64-linux-android" # target = "opencl"
target_host = "llvm -target=aarch64-linux-android"

# read keras model
if "mobilenetV2" in model_path:
    keras_model = keras.applications.mobilenet_v2.MobileNetV2(include_top=True, weights=None, input_shape=(224, 224, 3), classes=1000)
    keras_model.load_weights(model_path)
    print(type(keras_model))
if "densenet121" in model_path:
    keras_model = keras.applications.densenet.DenseNet121(include_top=True, weights=None, input_shape=(224, 224, 3), classes=1000)
    keras_model.load_weights(model_path)
    print(type(keras_model))
if "resnet50" in model_path:
    keras_model = keras.applications.resnet50.ResNet50(include_top=True, weights=None, input_shape=(224, 224, 3), classes=1000)
    keras_model.load_weights(model_path)
    print(type(model_path))

# preprocess image
image = Image.open(image_path).resize((224, 224))
data = np.array(image)[np.newaxis, :].astype('float32')
data = preprocess_input(data).transpose([0, 3, 1, 2])
print('input_1', data.shape)

# parse model from keras
shape_dict = {'input_1': data.shape}
mod, params = relay.frontend.from_keras(keras_model, shape_dict) # keras -> tvm.module
print(type(mod))

# build graph and params
with relay.build_config(opt_level=3):
    graph, lib, params = relay.build(mod, target=target,
                                     target_host=target_host, params=params)
lib.export_library("/Users/liuyuanqiang/Desktop/net.so", ndk.create_shared) # ndk 

# rpc tracker
tracker = rpc.connect_tracker("0.0.0.0", 9190)
remote = tracker.request("RedmiK30", priority=0, session_timeout=60)
if target == "opencl":
    ctx = remote.cl(0)
else:
    ctx = remote.cpu(0)
remote.upload("/Users/liuyuanqiang/Desktop/net.so")
rlib = remote.load_module('net.so')

# run
start = time.time()
module = graph_runtime.create(graph, rlib, ctx)
module.set_input(**params)
module.set_input('input_1', tvm.nd.array(data.astype('float32')))
module.run()
tvm_out = module.get_output(0)
end = time.time()

# get tvm output id
top1_tvm = np.argmax(tvm_out.asnumpy())
print("relay execution time: ", end - start)
print("relay top-1 id: {}".format(top1_tvm))

# get keras output id
keras_out = keras_model.predict(data.transpose([0, 2, 3, 1]))
top1_keras = np.argmax(keras_out)
print("keras top-1 id: {}".format(top1_keras))