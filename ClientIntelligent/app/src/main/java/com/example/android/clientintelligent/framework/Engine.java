package com.example.android.clientintelligent.framework;

import android.content.Context;

import com.example.android.clientintelligent.framework.interfaces.IEngine;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Engine implements IEngine {
    ArrayList<IInterpreter> mInterpreters;
    Context mContext;

    public Engine(Context context){
        mInterpreters = new ArrayList<>();
        mContext = context;
        initInterpreters();
        initData();
        initModels();
    }

    @Override
    public List<String> getInterpreterList() {
        return mInterpreters
                .stream()
                .map(IInterpreter::getFramework)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addInterpreter(IInterpreter interpreter) {
        mInterpreters.add(interpreter);
        return true;
    }

    @Override
    public IInterpreter getInterpreter(String interpreterName) {
        return mInterpreters
                .stream()
                .filter(i->i.getFramework()
                            .equals(interpreterName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean executeTask(IInterpreter interpreter, Mission task,
                               IProgressListener progressListener) {
        if (interpreter == null){
            progressListener.onError("IInterpreter param is null!");
            return false;
        }

        try {
            interpreter.buildTask(task, progressListener).execute();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            progressListener.onError("Error in interpreter.buildTask().execute() !");
        }
        return false;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    public abstract void initInterpreters();
    public abstract void initData();
    public abstract void initModels();
}
