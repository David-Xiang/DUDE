package com.example.android.clientintelligent;

import com.example.android.clientintelligent.interpreter.tflite.TFLiteInterpreter;

import java.util.ArrayList;
import java.util.List;

public class IntelligentEngineImpl implements IntelligentEngine {
    ArrayList<IntelligentInterpreter> mInterpretors;

    public IntelligentEngineImpl(){
        mInterpretors = new ArrayList<>();
        mInterpretors.add(new TFLiteInterpreter());
    }

    @Override
    public List<String> getInterpreterList() {
        return null;
    }

    @Override
    public boolean addInterpreter(IntelligentInterpreter interpreter) {
        mInterpretors.add(interpreter);
        return true;
    }

    @Override
    public boolean executeTask(String interpreterName, IntelligentTask task) {
        return false;
    }
}
