package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.IntelligentTask;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FloatTFLiteClassifier extends TFLiteClassifier{
    private float[][] labelProbArray;

    FloatTFLiteClassifier(IntelligentTask task) throws IOException {
        super(task);
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

}
