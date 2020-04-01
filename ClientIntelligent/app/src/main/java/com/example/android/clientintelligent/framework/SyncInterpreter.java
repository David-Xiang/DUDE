package com.example.android.clientintelligent.framework;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;

import java.util.ArrayList;
import java.util.List;

public abstract class SyncInterpreter implements IInterpreter {
    private Context mContext;
    private List<Model> mModels;

    protected SyncInterpreter(Context context) {
        mContext = context;
        mModels = new ArrayList<>();
    }

    public void addModel(Model model) {
        mModels.add(model);
    }

    public Model getModel(String modelName) {
        return mModels
                .stream()
                .filter(m->m.getModelPath()
                            .equals(modelName))
                .findFirst()
                .orElse(null);
    }

    public List<Model> getModels() {
        return mModels;
    }

    // 虽然是采用AsyncTask在新线程中执行任务，但仍旧是同步的顺序调用代码
    // 对比于TensorFlow.js可认为是Sync
    public abstract AsyncTask buildTask(Mission mission, IProgressListener progressListener) throws Exception;

    public Context getContext() {
        return mContext;
    }
}
