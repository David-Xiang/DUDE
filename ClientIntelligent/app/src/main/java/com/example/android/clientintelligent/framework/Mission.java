package com.example.android.clientintelligent.framework;

import android.app.Activity;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;

import java.util.List;

public class Mission {
    public enum Purpose {PERFORMANCE, ACCURACY};

    private final Purpose purpose;
    private final Activity activity;

    private final IInterpreter.Device device;

    private final int nThreads;
    private final int nTime;

    private final Model mModel;

    public Mission(Activity activity, Model model,
                   Purpose purpose, IInterpreter.Device device, int nThreads, int nTime){
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

    public IInterpreter.Device getDevice() {
        return device;
    }

    public int getChannelsPerPixel() {
        return mModel.getChannelsPerPixel();
    }

    public Model.Mode getModelMode() { return mModel.getMode(); }

    public String getTrueLabelIndexPath() { return mModel.getTrueLabelIndexPath(); }

    public Purpose getPurpose() {
        return purpose;
    }
}
