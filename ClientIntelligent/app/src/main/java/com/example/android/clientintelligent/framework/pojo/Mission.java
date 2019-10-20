package com.example.android.clientintelligent.framework.pojo;

import android.content.Context;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;

import java.util.List;

public class Mission {
    public enum Purpose {PERFORMANCE, ACCURACY}

    private final Purpose purpose;
    private final Context context;

    private final IInterpreter.Device device;

    private final int nThreads;
    private final int nTime;

    private final Model mModel;
    private final Data mData;

    public Mission(Context context, Model model, Data data,
                   Purpose purpose, IInterpreter.Device device, int nThreads, int nTime){
        this.context = context;
        this.mModel = model;
        this.mData = data;

        this.purpose = purpose;
        this.device = device;

        this.nThreads = nThreads;
        this.nTime = nTime;
    }

    public Context getContext() {
        return context;
    }

    public String getModelFilePath() {
        return mModel.getFilePath();
    }

    public List<String> getDataPathList() {
        return mData.getPathList();
    }

    public String getLabelFilePath() {
        return mData.getLabelFilePath();
    }

    public String getTrueLabelIndexPath() { return mData.getTrueLabelIndexPath(); }

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

    public Purpose getPurpose() {
        return purpose;
    }
}
