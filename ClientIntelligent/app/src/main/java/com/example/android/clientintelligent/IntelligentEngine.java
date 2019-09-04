package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.interfaces.Engine;
import com.example.android.clientintelligent.interfaces.Interpreter;
import com.example.android.clientintelligent.interfaces.ProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class IntelligentEngine implements Engine {
    ArrayList<Interpreter> mInterpreters;
    Context mContext;

    IntelligentEngine(Context context){
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
                .map(Interpreter::getFramework)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addInterpreter(Interpreter interpreter) {
        mInterpreters.add(interpreter);
        return true;
    }

    @Override
    public Interpreter getInterpreter(String interpreterName) {
        return mInterpreters.stream().filter(i->i.getFramework().equals(interpreterName)).findFirst().orElse(null);
    }

    @Override
    public boolean executeTask(Interpreter interpreter, IntelligentTask task, ProgressListener progressListener) {
        if (interpreter == null){
            progressListener.onError("Interpreter param is null!");
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

    public abstract void initInterpreters();
    public abstract void initData();
    public abstract void initModels();
}
