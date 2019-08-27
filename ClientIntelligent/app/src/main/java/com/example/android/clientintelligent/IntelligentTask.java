package com.example.android.clientintelligent;

import android.app.Activity;

import java.util.ArrayList;

public class IntelligentTask {
    private final Activity activity;

    private final String modelFilePath;
    private final String labelFilePath;

    private final ArrayList<String> dataPathList;

    private final String deviceName;

    private final int nThreads;
    private final int nTime;
    private final int nImageSizeX;
    private final int nImageSizeY;
    private final int nBytesPerChannel;
    private final int nChannelsPerPixel;

    IntelligentTask(Activity activity, String modelFilePath, String labelFilePath,
                    ArrayList<String> dataPathList, String deviceName, int nThreads, int nTime,
                    int nImageSizeX, int nImageSizeY, int nBytesPerChannel, int nChannelsPerPixel){
        this.activity = activity;

        this.modelFilePath = modelFilePath;
        this.labelFilePath = labelFilePath;

        this.dataPathList = dataPathList;

        this.deviceName = deviceName;

        this.nThreads = nThreads;
        this.nTime = nTime;

        this.nImageSizeX = nImageSizeX;
        this.nImageSizeY = nImageSizeY;
        this.nBytesPerChannel = nBytesPerChannel;
        this.nChannelsPerPixel = nChannelsPerPixel;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public ArrayList<String> getDataPathList() {
        return dataPathList;
    }

    public int getnThreads() {
        return nThreads;
    }

    public int getnTime() {
        return nTime;
    }

    public int getnImageSizeX() {
        return nImageSizeX;
    }

    public int getnImageSizeY() {
        return nImageSizeY;
    }

    public String getLabelFilePath() {
        return labelFilePath;
    }

    public int getnBytesPerChannel() {
        return nBytesPerChannel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getnChannelsPerPixel() {
        return nChannelsPerPixel;
    }
}
