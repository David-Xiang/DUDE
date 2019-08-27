package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.IntelligentTask;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TFLiteClassifier extends BaseClassifier {
    private float[][] labelProbArray = null;
    private IntelligentTask task;

    TFLiteClassifier(IntelligentTask task) throws IOException {
        super(task.getActivity(), task.getDeviceName(), task.getnThreads());
        this.task = task;
    }

    @Override
    public int getImageSizeX() {
        return task.getnImageSizeX();
    }

    @Override
    public int getImageSizeY() {
        return task.getnImageSizeY();
    }

    @Override
    protected String getModelPath() {
        return task.getModelFilePath();
    }

    @Override
    protected String getLabelPath() {
        return task.getLabelFilePath();
    }

    @Override
    protected int getNumBytesPerChannel() {
        return task.getBytesPerChannel();
    }

    @Override
    protected int getNumChannelsPerPixel() {
        return task.getChannelsPerPixel();
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        // for mnist currently
        imgData.putFloat((pixelValue & 0xFF) / 255.f);
    }

    @Override
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.floatValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }

    void runInference(ByteBuffer data){
        tflite.run(data, labelProbArray);
    }
}
