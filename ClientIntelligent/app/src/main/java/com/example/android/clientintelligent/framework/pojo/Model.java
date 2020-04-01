package com.example.android.clientintelligent.framework.pojo;

import android.util.Log;

public class Model {
    private static final String TAG = "Model";

    public enum Mode {FLOAT32, FLOAT16, QUANTIZED}

    private Mode mode;
    private MetaData metaData;
    private String modelPath, paramFilePath, libCpuPath, libGpuPath;
    private String dataSetName;
    private float accuracy;

    public Model(MetaData metaData, String mModelPath, String paramFilePath, String libCpuPath, String libGpuPath, Mode mode, String dataSetName, float accuracy) {
        this(metaData, mModelPath, paramFilePath, libCpuPath, libGpuPath, mode, dataSetName);
        this.accuracy = accuracy > 0 && accuracy <= 100 ? accuracy : 0;
    }

    public Model(MetaData metaData, String modelPath, String paramFilePath, String libCpuPath, String libGpuPath, Mode mode, String dataSetName) {
        this.metaData = metaData;
        this.modelPath = modelPath;
        this.paramFilePath = paramFilePath;
        this.libCpuPath = libCpuPath;
        this.libGpuPath = libGpuPath;
        this.mode = mode;
        this.dataSetName = dataSetName;
        if (metaData == null){
            Log.e(TAG, "Model: mMetaData is null!");
        }
    }

    public Mode getMode() {
        return mode;
    }

    public String getModelPath() {
        return modelPath;
    }

    public int getImageSizeX() {
        return metaData.getImageSizeX();
    }

    public int getImageSizeY() {
        return metaData.getImageSizeY();
    }

    public int getBytesPerChannel() {
        return metaData.getBytesPerChannel();
    }

    public int getChannelsPerPixel() {
        return metaData.getChannelsPerPixel();
    }

    public int getOutputSize() {
        return metaData.getOutputSize();
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public String getParamFilePath() {
        return paramFilePath;
    }

    public String getLibCpuPath() {
        return libCpuPath;
    }

    public String getLibGpuPath() {
        return libGpuPath;
    }
}
