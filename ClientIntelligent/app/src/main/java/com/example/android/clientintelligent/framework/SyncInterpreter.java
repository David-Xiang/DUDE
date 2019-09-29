package com.example.android.clientintelligent.framework;

import android.content.Context;
import android.webkit.ValueCallback;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SyncInterpreter implements IInterpreter {
    protected Context mContext;
    protected List<Model> mModels;

    protected SyncInterpreter(Context context) {
        mContext = context;
        mModels = new ArrayList<>();
    }

    public boolean addModel(Model model) {
        mModels.add(model);
        return true;
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

    public abstract void executeMission(Mission mission, IProgressListener progressListener) throws Exception;
    protected abstract void loadLabelList(String path) throws IOException;
    protected abstract void loadLabelIndexList(String path) throws IOException;

    protected abstract void loadModelFile(String path) throws IOException;
    protected abstract void configSession(IInterpreter.Device device);
    //    protected abstract Bitmap loadValidImage(int index) throws IOException;
//    protected abstract List<Recognition> recognizeImage(Bitmap bitmap);
    protected abstract void processRecognitions(int index, List<Recognition> recognitions, AccuracyResult result);
    protected abstract void publishResults(AccuracyResult result);
    protected abstract void releaseResources();

    public class AccuracyResult {
        public int total = 0;
        public int top1count = 0;
        public int top5count = 0;
    }
}
