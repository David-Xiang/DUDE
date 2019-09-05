import tensorflow as tf
import tensorflow.keras as keras

model = keras.applications.mobilenet.MobileNet(input_shape=None, alpha=1.0, depth_multiplier=1, dropout=1e-3, include_top=True, weights='imagenet', input_tensor=None, pooling=None, classes=1000)
name = "mobilenet"
model.save(name + ".h5")
converter = tf.lite.TFLiteConverter.from_keras_model_file(name + ".h5")
tflite_model = converter.convert()
open(name + ".tflite", "wb").write(tflite_model)

converter = tf.lite.TFLiteConverter.from_keras_model_file(name + ".h5")
converter.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_SIZE]
tflite_model = converter.convert()
open(name + "_optimize_size.tflite", "wb").write(tflite_model)

converter = tf.lite.TFLiteConverter.from_keras_model_file(name + ".h5")
converter.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_LATENCY]
tflite_model = converter.convert()
open(name + "_optimize_latency.tflite", "wb").write(tflite_model)

arr = []
arr.append({"type": tf.bfloat16, "suffix": "_bfloat16"})
arr.append({"type": tf.double, "suffix": "_double"})
arr.append({"type": tf.lite.constants.FLOAT, "suffix": "_float32"})
arr.append({"type": tf.lite.constants.FLOAT16, "suffix": "_float16"})
for i in arr:
    converter = tf.lite.TFLiteConverter.from_keras_model_file(name + ".h5")
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [i["type"]]
    tflite_model = converter.convert()
    open(name + i["suffix"] + ".tflite", "wb").write(tflite_model)

arr = []
arr.append({"type": tf.lite.constants.QUANTIZED_UINT8, "suffix": "_uint8"})

for i in arr:
    converter = tf.lite.TFLiteConverter.from_keras_model_file(name + ".h5")
    converter.target_spec.supported_types = [i["type"]]
    converter.inference_input_type = tf.lite.constants.QUANTIZED_UINT8
    input_arrays = converter.get_input_arrays()
    converter.quantized_input_stats = {input_arrays[0]: (0., 1.)}  # mean, std_dev
    tflite_model = converter.convert()
    open(name + i["suffix"] + ".tflite", "wb").write(tflite_model)


arr = []
arr.append({"type": tf.lite.constants.INT32, "suffix": "_int32"})
arr.append({"type": tf.lite.constants.INT64, "suffix": "_int64"})

for i in arr:
    converter = tf.lite.TFLiteConverter.from_keras_model_file(name + ".h5")
    # converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [i["type"]]
    # input_arrays = converter.get_input_arrays()
    # converter.quantized_input_stats = {input_arrays[0]: (0., 1.)}  # mean, std_dev
    tflite_model = converter.convert()
    open(name + i["suffix"] + ".tflite", "wb").write(tflite_model)