package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.framework.pojo.Mission;

import java.io.IOException;

public abstract class TFLiteClassifier extends BaseClassifier {
    TFLiteClassifier(Mission mission, String modelPath, Float accuracy) throws IOException {
        super(mission.getContext(), mission, modelPath, accuracy);
    }

    @Override
    public int getImageSizeX() {
        return mission.getnImageSizeX();
    }

    @Override
    public int getImageSizeY() {
        return mission.getnImageSizeY();
    }

    @Override
    protected String getLabelPath() {
        return mission.getLabelFilePath();
    }

    @Override
    protected int getNumBytesPerChannel() {
        return mission.getBytesPerChannel();
    }

    @Override
    protected int getNumChannelsPerPixel() {
        return mission.getChannelsPerPixel();
    }
}
