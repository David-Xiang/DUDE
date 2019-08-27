package com.example.android.clientintelligent;

import android.app.Activity;

import java.util.List;

public class IntelligentTask {
    private final Activity activity;

    private final String deviceName;

    private final int nThreads;
    private final int nTime;

    private final IntelligentModel mModel;

    IntelligentTask(Activity activity, IntelligentModel model,
                    String deviceName, int nThreads, int nTime){
        this.activity = activity;

        this.mModel = model;

        this.deviceName = deviceName;

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

    public String getDeviceName() {
        return deviceName;
    }

    public int getChannelsPerPixel() {
        return mModel.getChannelsPerPixel();
    }
}
