package com.example.android.clientintelligent.framework;

import java.util.List;

public class Data {
    private final List<String> mPathList;
    private final String mTrueLabelIndexPath;
    private final String mLabelFilePath;

    private final int nImageSizeX;
    private final int nImageSizeY;
    private final int nBytesPerChannel;
    private final int nChannelsPerPixel;

    public Data(List<String> mPathList, String mTrueLabelIndexPath,
                String mLabelFilePath, int nImageSizeX, int nImageSizeY,
                int nBytesPerChannel, int nChannelsPerPixel) {
        this.mPathList = mPathList;
        this.mTrueLabelIndexPath = mTrueLabelIndexPath;
        this.mLabelFilePath = mLabelFilePath;
        this.nImageSizeX = nImageSizeX;
        this.nImageSizeY = nImageSizeY;
        this.nBytesPerChannel = nBytesPerChannel;
        this.nChannelsPerPixel = nChannelsPerPixel;
    }

    public List<String> getPathList() {
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

    public String getTrueLabelIndexPath() {
        return mTrueLabelIndexPath;
    }
}
