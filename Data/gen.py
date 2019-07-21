import argparse
import cv2
import numpy
import os

def random_gen_pic(height, width, channel=1, amount=1):
    for i in range(amount):
        randomByteArray = bytearray(os.urandom(height*width*channel))
        numpyArray = numpy.array(randomByteArray).reshape(height, width, channel)
        cv2.imwrite(str(i)+".png", numpyArray)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Gernerate images randomly.")
    parser.add_argument("-t", "--height", type=int, required=True, help="Height of images")
    parser.add_argument("-w", "--width", type=int, required=True, help="Width of images")
    parser.add_argument("-c", "--channel", type=int, required=True, help="Number of channel")
    parser.add_argument("-n", "--amount", type=int, required=True, help="Number of images")
    args = parser.parse_args()
    random_gen_pic(args.height, args.width, args.channel, args.amount)

    