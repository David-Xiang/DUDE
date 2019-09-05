package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.framework.IntelligentMission;

import java.io.IOException;

public abstract class TFLiteClassifier extends BaseClassifier {
    TFLiteClassifier(IntelligentMission task) throws IOException {
        super(task.getActivity(), task);
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
}
