package com.example.android.clientintelligent.framework;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Interpreter implements IInterpreter {
    protected Context mContext;
    protected List<Model> mModels;

    protected Interpreter(Context context) {
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

    // 虽然是采用AsyncTask在新线程中执行任务，但仍旧是同步的顺序调用代码
    // 对比于TensorFlow.js可认为是Sync
    public abstract AsyncTask buildTask(Mission mission, IProgressListener progressListener) throws Exception;
}
