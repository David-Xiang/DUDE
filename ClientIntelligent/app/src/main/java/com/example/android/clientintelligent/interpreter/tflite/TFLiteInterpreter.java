package com.example.android.clientintelligent.interpreter.tflite;

import com.example.android.clientintelligent.IntelligentInterpreter;
import com.example.android.clientintelligent.IntelligentTask;

import java.util.List;

public class TFLiteInterpreter implements IntelligentInterpreter {
    @Override
    public List<String> getBackends() {
        return null;
    }

    @Override
    public String getFramework() {
        return null;
    }

    @Override
    public void executeTask(IntelligentTask task) {

    }
}
