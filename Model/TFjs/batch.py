import os

num = [1, 2, 4, 8, 16, 32]
size = [32, 64, 128, 256, 512, 1024]
for n in num:
    for s in size:
        # train and get onnx model file
        os.system("python3 mnist.py -n %d -s %d" % (n, s))
