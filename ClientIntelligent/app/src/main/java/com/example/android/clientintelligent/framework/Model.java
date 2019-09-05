package com.example.android.clientintelligent.framework;

import android.util.Log;

import java.util.List;

public class Model {
    private static final String TAG = "Model";

    public enum Mode {FLOAT32, FLOAT16, QUANTIZED};

    private Mode mMode;
    private Data mData;
    private final String mFilePath;

    public Model(Data mData, String mFilePath, Mode mode) {
        this.mData = mData;
        this.mFilePath = mFilePath;
        this.mMode = mode;
        if (mData == null){
            Log.e(TAG, "Model: mData is null!");
        }
    }

    public Mode getMode() {
        return mMode;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public int getImageSizeX() {
        return mData.getImageSizeX();
    }

    public int getImageSizeY() {
        return mData.getImageSizeY();
    }

    public int getBytesPerChannel() {
        return mData.getBytesPerChannel();
    }

    public int getChannelsPerPixel() {
        return mData.getChannelsPerPixel();
    }

    public List<String> getDataPathList() {
        return mData.getPathList();
    }

    public String getLabelFilePath() {
        return mData.getLabelFilePath();
    }

    public String getTrueLabelIndexPath() {return mData.getTrueLabelIndexPath();}
}
