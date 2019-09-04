package com.example.android.clientintelligent;

import android.app.Activity;

import com.example.android.clientintelligent.interfaces.Interpreter;

import java.util.List;

public class IntelligentTask {
    public enum Purpose {PERFORMANCE, ACCURACY};

    private final Purpose purpose;
    private final Activity activity;

    private final Interpreter.Device device;

    private final int nThreads;
    private final int nTime;

    private final IntelligentModel mModel;

    IntelligentTask(Activity activity, IntelligentModel model,
                    Purpose purpose, Interpreter.Device device, int nThreads, int nTime){
        this.activity = activity;
        this.mModel = model;

        this.purpose = purpose;
        this.device = device;

        this.nThreads = nThreads;
        this.nTime = nTime;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getModelFilePath() {
        return mModel.getFilePath();
    }

    public List<String> getDataPathList() {
        return mModel.getDataPathList();
    }

    public int getnThreads() {
        return nThreads;
    }

    public int getnTime() {
        return nTime;
    }

    public int getnImageSizeX() {
        return mModel.getImageSizeX();
    }

    public int getnImageSizeY() {
        return mModel.getImageSizeY();
    }

    public String getLabelFilePath() {
        return mModel.getLabelFilePath();
    }

    public int getBytesPerChannel() {
        return mModel.getBytesPerChannel();
    }

    public Interpreter.Device getDevice() {
        return device;
    }

    public int getChannelsPerPixel() {
        return mModel.getChannelsPerPixel();
    }

    public IntelligentModel.Mode getModelMode() { return mModel.getMode(); }

    public String getTrueLabelIndexPath() { return mModel.getTrueLabelIndexPath(); }

    public Purpose getPurpose() {
        return purpose;
    }
}
