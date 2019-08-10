import argparse
import tensorflow as tf
import tensorflow.keras as keras
import tensorflowjs as tfjs
import numpy as np

from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras.datasets import mnist

IMAGE_LENGTH = 28
INPUT_NODE = 784
OUTPUT_NODE = 10
NUM_CHANNELS = 1
LEARNING_RATE = 0.15

# data, split between train and validate sets
(x_train, y_train), (x_eval, y_eval) = mnist.load_data("mnist")
x_train = x_train.reshape(x_train.shape[0], INPUT_NODE)
x_eval = x_eval.reshape(x_eval.shape[0], INPUT_NODE)
x_train = x_train.astype(np.float32) / 255 - 0.5
x_eval = x_eval.astype(np.float32) / 255 - 0.5

# convert class vector to binary class matrices (one-hot representation)
y_train = keras.utils.to_categorical(y_train, OUTPUT_NODE)
y_eval = keras.utils.to_categorical(y_eval, OUTPUT_NODE)

model = Sequential();
def init(num, size):
    model.add(Dense(
        input_shape=(INPUT_NODE,),
        units=size,
        activation="relu"
    ))
   
    for i in range(num - 1):
        model.add(Dense(
            units=size,
            activation="relu"
        ))

    model.add(Dense(
        units=OUTPUT_NODE,
        activation="softmax"
    ))
    
    model.compile(
        loss=keras.losses.categorical_crossentropy,
        optimizer="sgd",
        metrics=["accuracy"]
    )

def train():
    print("start training...")
    model.fit(x_train, y_train, epochs=1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Gernerate pretrained DNN Mnist model given hidden layers' number and size.")
    parser.add_argument("-n", "--num", type=int, required=True, help="Number of hidden layers")
    parser.add_argument("-s", "--size", type=int, required=True, help="Size of hidden layers")
    args = parser.parse_args()
    init(args.num, args.size)
    train()
    name = "mnist-%d-%d" % (args.num, args.size)

    # for tflite
    model.save(name + ".h5")
    converter = tf.lite.TFLiteConverter.from_keras_model_file(name+".h5")
    tflite_model = converter.convert()
    open(name+".tflite", "wb").write(tflite_model)

    converter = tf.lite.TFLiteConverter.from_keras_model_file(name+".h5")
    converter.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_SIZE]
    tflite_model = converter.convert()
    open(name+"_optimize_for_size.tflite", "wb").write(tflite_model)

    converter = tf.lite.TFLiteConverter.from_keras_model_file(name+".h5")
    converter.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_LATENCY]
    tflite_model = converter.convert()
    open(name+"_optimize_for_latency.tflite", "wb").write(tflite_model)

    arr = []
    arr.append({"type": tf.float16, "suffix": "_float16"})
    arr.append({"type": tf.float32, "suffix": "_float32"})
    arr.append({"type": tf.bfloat16, "suffix": "_bfloat16"})
    arr.append({"type": tf.double, "suffix": "_double"})
    arr.append({"type": tf.int8, "suffix": "_int8"})
    arr.append({"type": tf.int16, "suffix": "_int16"})
    arr.append({"type": tf.int32, "suffix": "_int32"})
    arr.append({"type": tf.int64, "suffix": "_int64"})
    arr.append({"type": tf.lite.constants.FLOAT, "suffix": "_constant_float32"})
    arr.append({"type": tf.lite.constants.FLOAT16, "suffix": "_constant_float16"})
    arr.append({"type": tf.lite.constants.INT8, "suffix": "_constant_int8"})
    arr.append({"type": tf.lite.constants.INT32, "suffix": "_constant_int32"})
    arr.append({"type": tf.lite.constants.INT64, "suffix": "_constant_int64"})
    arr.append({"type": tf.lite.constants.QUANTIZED_UINT8, "suffix": "_constant_uint8"})

    for i in arr:
        converter = tf.lite.TFLiteConverter.from_keras_model_file(name+".h5")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [i["type"]]
        tflite_model = converter.convert()
        open(name+ i["suffix"] + ".tflite", "wb").write(tflite_model)
    
    # for tfjs
    # tfjs.converters.save_keras_model(model, name)