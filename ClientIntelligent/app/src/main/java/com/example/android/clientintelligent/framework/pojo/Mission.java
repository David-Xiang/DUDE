package com.example.android.clientintelligent.framework.pojo;

import android.content.Context;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;

import java.util.Collections;
import java.util.List;

public class Mission {
    public enum Purpose {BENCH_PERFORMANCE, BENCH_ACCURACY, APP_PERFORMANCE, APP_ACCURACY, APP_BANLANCE}

    private final Purpose purpose;
    private final Context context;

    private final IInterpreter.Device device;

    private final int nThreads;
    private final int nTime;

    private final List<Model> mModelList;
    private final DataSet mDataSet;

    public Mission(Context context, List<Model> modelList, DataSet dataSet,
                   Purpose purpose, IInterpreter.Device device, int nThreads, int nTime){
        this.context = context;
        this.mModelList = modelList;
        this.mDataSet = dataSet;

        this.purpose = purpose;
        this.device = device;

        this.nThreads = nThreads;
        this.nTime = nTime;
    }

    public Mission(Context context, Model model, DataSet dataSet,
                   Purpose purpose, IInterpreter.Device device, int nThreads, int nTime){
        this(context, Collections.singletonList(model), dataSet, purpose, device, nThreads, nTime);
    }

    public Context getContext() {
        return context;
    }

    public String getModelFilePath() {
        return mModelList.get(0).getFilePath();
    }

    public List<Model> getModels() {
        return mModelList;
    }

    public List<String> getDataPathList() {
        return mDataSet.getPathList();
    }

    public String getLabelFilePath() {
        return mDataSet.getLabelFilePath();
    }

    public String getTrueLabelIndexPath() { return mDataSet.getTrueLabelIndexPath(); }

    public int getnThreads() {
        return nThreads;
    }

    public int getnTime() {
        return nTime;
    }

    public int getnImageSizeX() {
        return mModelList.get(0).getImageSizeX();
    }

    public int getnImageSizeY() {
        return mModelList.get(0).getImageSizeY();
    }

    public int getBytesPerChannel() {
        return mModelList.get(0).getBytesPerChannel();
    }

    public IInterpreter.Device getDevice() {
        return device;
    }

    public int getChannelsPerPixel() {
        return mModelList.get(0).getChannelsPerPixel();
    }

    public Model.Mode getModelMode() { return mModelList.get(0).getMode(); }

    public Purpose getPurpose() {
        return purpose;
    }
}
