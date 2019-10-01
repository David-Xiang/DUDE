package com.example.android.clientintelligent.framework;

import android.content.Context;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AsyncInterpreter implements IInterpreter {
    private Context mContext;
    private List<Model> mModels;

    protected AsyncInterpreter(Context context) {
        mContext = context;
        mModels = new ArrayList<>();
    }

    public void addModel(Model model) {
        mModels.add(model);
    }

    public Model getModel(String modelName) {
        return mModels
                .stream()
                .filter(m->m.getFilePath()
                            .equals(modelName))
                .findFirst()
                .orElse(null);
    }

    public List<String> getModels() {
        return mModels
                .stream()
                .map(Model::getFilePath)
                .collect(Collectors.toList());
    }

    public abstract void executeMissionAsync(Mission mission, IProgressListener progressListener) throws Exception;
    protected abstract void loadLabelList(String path) throws IOException;
    protected abstract void loadLabelIndexList(String path) throws IOException;

    protected abstract void loadModelFileAsync(String path);
    protected abstract void configSessionAsync(IInterpreter.Device device);
    protected abstract void recognizeImageAsync(String picPath);

    public abstract void onWindowLoaded();
    public abstract void onModelLoaded() throws IOException;
    public abstract void onBackendRegistered();
    public abstract void onAccuracyTaskFinished(float [] result);
    public abstract void onPerformanceTaskFinished(int count, int elapsedTime);
    public abstract void onProgress(int progress);

    protected abstract void processRecognitions(int index, List<Recognition> recognitions, AccuracyResult result);
    protected abstract void publishResults(AccuracyResult result);
    protected abstract void releaseResources();

    public Context getContext() {
        return mContext;
    }

    public class AccuracyResult {
        public int total = 0;
        public int top1count = 0;
        public int top5count = 0;
    }
}
