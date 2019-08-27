package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.interfaces.IntelligentEngine;
import com.example.android.clientintelligent.interfaces.IntelligentInterpreter;
import com.example.android.clientintelligent.interfaces.ProgressListener;
import com.example.android.clientintelligent.interpreter.tflite.TFLiteInterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntelligentEngineImpl implements IntelligentEngine {
    private ArrayList<IntelligentInterpreter> mInterpretors;

    IntelligentEngineImpl(Context context){
        mInterpretors = new ArrayList<>();
        mInterpretors.add(new TFLiteInterpreter(context));
    }

    @Override
    public List<String> getInterpreterList() {
        return mInterpretors
                .stream()
                .map(IntelligentInterpreter::getFramework)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addInterpreter(IntelligentInterpreter interpreter) {
        mInterpretors.add(interpreter);
        return true;
    }

    @Override
    public IntelligentInterpreter getInterpreter(String interpreterName) {
        return mInterpretors.stream().filter(i->i.getFramework().equals(interpreterName)).findFirst().orElse(null);
    }

    @Override
    public boolean executeTask(String interpreterName, IntelligentTask task, ProgressListener progressListener) {
        IntelligentInterpreter interpreter = getInterpreter(interpreterName);
        if (interpreter == null){
            // TODO Toast
            return false;
        }

        try {
            interpreter.buildTask(task, progressListener).execute();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
