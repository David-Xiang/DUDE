package com.example.android.clientintelligent.framework;

import android.content.Context;

import com.example.android.clientintelligent.framework.interfaces.IEngine;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Engine implements IEngine {
    private ArrayList<IInterpreter> mInterpreters;
    private Context mContext;

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
    public void addInterpreter(IInterpreter interpreter) {
        mInterpreters.add(interpreter);
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
    public void executeTask(IInterpreter interpreter, Mission mission,
                            IProgressListener progressListener) {
        if (interpreter == null){
            progressListener.onError("IInterpreter param is null!");
            return;
        }

        try {
            if (interpreter instanceof SyncInterpreter) {
                ((SyncInterpreter) interpreter).buildTask(mission, progressListener).execute();
            } else if (interpreter instanceof AsyncInterpreter) {
                ((AsyncInterpreter) interpreter).executeMissionAsync(mission, progressListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressListener.onError("Error in interpreter.buildTask().execute() !");
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    public abstract void initInterpreters();
    public abstract void initData();
    public abstract void initModels();
}
