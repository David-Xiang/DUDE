package com.example.android.clientintelligent.framework;

import android.util.Log;

import java.util.List;

public class IntelligentModel {
    private static final String TAG = "IntelligentModel";

    public enum Mode {FLOAT32, FLOAT16, QUANTIZED};

    private Mode mMode;
    private IntelligentData mData;
    private final String mFilePath;

    public IntelligentModel(IntelligentData mData, String mFilePath, Mode mode) {
        this.mData = mData;
        this.mFilePath = mFilePath;
        this.mMode = mode;
        if (mData == null){
            Log.e(TAG, "IntelligentModel: mData is null!");
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
