package com.example.android.clientintelligent;

import java.util.ArrayList;

public class IntelligentData {
    private final ArrayList<String> mPathList;
    private final String mLabelFilePath;

    private final int nImageSizeX;
    private final int nImageSizeY;
    private final int nBytesPerChannel;
    private final int nChannelsPerPixel;

    public IntelligentData(ArrayList<String> mPathList, String mLabelFilePath, int nImageSizeX, int nImageSizeY, int nBytesPerChannel, int nChannelsPerPixel) {
        this.mPathList = mPathList;
        this.mLabelFilePath = mLabelFilePath;
        this.nImageSizeX = nImageSizeX;
        this.nImageSizeY = nImageSizeY;
        this.nBytesPerChannel = nBytesPerChannel;
        this.nChannelsPerPixel = nChannelsPerPixel;
    }

    public ArrayList<String> getPathList() {
        return mPathList;
    }

    public String getLabelFilePath() {
        return mLabelFilePath;
    }

    public int getImageSizeX() {
        return nImageSizeX;
    }

    public int getImageSizeY() {
        return nImageSizeY;
    }

    public int getBytesPerChannel() {
        return nBytesPerChannel;
    }

    public int getChannelsPerPixel() {
        return nChannelsPerPixel;
    }
}
