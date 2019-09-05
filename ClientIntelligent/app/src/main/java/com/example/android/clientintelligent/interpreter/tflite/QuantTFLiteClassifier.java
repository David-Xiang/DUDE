package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.framework.Mission;

import java.io.IOException;
import java.nio.ByteBuffer;

public class QuantTFLiteClassifier extends TFLiteClassifier{
    private byte[][] labelProbArray;

    QuantTFLiteClassifier(Mission task) throws IOException {
        super(task);
        labelProbArray = new byte[1][getNumLabels()];
    }

    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.byteValue();
    }

    protected float getNormalizedProbability(int labelIndex) {
        return (labelProbArray[0][labelIndex] & 0xff) / 255.0f;
    }

    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }

    protected void runInference(ByteBuffer data){
        tflite.run(data, labelProbArray);
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        imgData.put((byte) ((pixelValue >> 16) & 0xFF));
        imgData.put((byte) ((pixelValue >> 8) & 0xFF));
        imgData.put((byte) (pixelValue & 0xFF));
    }
}
