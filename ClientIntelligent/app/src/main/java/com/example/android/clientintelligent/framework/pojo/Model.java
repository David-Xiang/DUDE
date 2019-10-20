package com.example.android.clientintelligent.framework.pojo;

import android.util.Log;

public class Model {
    private static final String TAG = "Model";

    public enum Mode {FLOAT32, FLOAT16, QUANTIZED};

    private Mode mMode;
    private MetaData mMetaData;
    private String mFilePath;

    public Model(MetaData mMetaData, String mFilePath, Mode mode) {
        this.mMetaData = mMetaData;
        this.mFilePath = mFilePath;
        this.mMode = mode;
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
}
