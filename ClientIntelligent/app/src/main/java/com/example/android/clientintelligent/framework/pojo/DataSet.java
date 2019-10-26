package com.example.android.clientintelligent.framework.pojo;

import java.util.List;

public class DataSet {
    private final String mName;
    private final List<String> mPathList;
    private final String mTrueLabelIndexPath;
    private final String mLabelFilePath;

    private final MetaData mMetaData;

    public DataSet(String mName, List<String> mPathList, String mTrueLabelIndexPath,
                   String mLabelFilePath, int nImageSizeX, int nImageSizeY,
                   int nBytesPerChannel, int nChannelsPerPixel) {
        this.mName = mName;
        this.mPathList = mPathList;
        this.mTrueLabelIndexPath = mTrueLabelIndexPath;
        this.mLabelFilePath = mLabelFilePath;
        mMetaData = new MetaData(nImageSizeX, nImageSizeY, nBytesPerChannel, nChannelsPerPixel);
    }

    public List<String> getPathList() {
        return mPathList;
    }

    public String getLabelFilePath() {
        return mLabelFilePath;
    }

    public int getImageSizeX() {
        return mMetaData.getImageSizeX();
    }

    public int getImageSizeY() {
        return mMetaData.getImageSizeY();
    }

    public int getBytesPerChannel() {
        return mMetaData.getBytesPerChannel();
    }

    public int getChannelsPerPixel() {
        return mMetaData.getChannelsPerPixel();
    }

    public String getTrueLabelIndexPath() {
        return mTrueLabelIndexPath;
    }

    public MetaData getMetaData() {
        return mMetaData;
    }

    public String getName() {
        return mName;
    }
}
