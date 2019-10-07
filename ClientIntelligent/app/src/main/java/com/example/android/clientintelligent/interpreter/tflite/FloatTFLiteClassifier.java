package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.framework.Mission;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FloatTFLiteClassifier extends TFLiteClassifier{
    /** MobileNet requires additional normalization of the used input. */
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private float[][] labelProbArray;

    FloatTFLiteClassifier(Mission mission, String modelFilePath) throws IOException {
        super(mission, modelFilePath);
        labelProbArray = new float[1][getNumLabels()];
    }

    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.floatValue();
    }

    protected float getNormalizedProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }

    protected void runInference(ByteBuffer data){
        tflite.run(data, labelProbArray);
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }
}
