package com.example.android.clientintelligent.framework.pojo;

import android.util.Log;

public class Model {
    private static final String TAG = "Model";

    public enum Mode {FLOAT32, FLOAT16, QUANTIZED}

    private Mode mMode;
    private MetaData mMetaData;
    private String mFilePath;
    private String mDataSetName;
    private float mAccuracy;

    public Model(MetaData mMetaData, String mFilePath, Mode mode, String dataSetName, float accuracy) {
        this(mMetaData, mFilePath, mode, dataSetName);
        mAccuracy = accuracy > 0 && accuracy <= 100 ? accuracy : 0;
    }

    public Model(MetaData mMetaData, String mFilePath, Mode mode, String dataSetName) {
        this.mMetaData = mMetaData;
        this.mFilePath = mFilePath;
        this.mMode = mode;
        this.mDataSetName = dataSetName;
        if (mMetaData == null){
            Log.e(TAG, "Model: mMetaData is null!");
        }
    }

    public Mode getMode() {
        return mMode;
    }

    public String getFilePath() {
        return mFilePath;
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

    public Float getAccuracy() {
        return mAccuracy;
    }

    public String getDataSetName() {
        return mDataSetName;
    }
}
