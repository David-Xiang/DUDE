package com.example.android.clientintelligent;

import java.util.List;

public class IntelligentModel {
    private IntelligentData mData;
    private final String mFilePath;

    public IntelligentModel(IntelligentData mData, String mFilePath) {
        this.mData = mData;
        this.mFilePath = mFilePath;
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
}
