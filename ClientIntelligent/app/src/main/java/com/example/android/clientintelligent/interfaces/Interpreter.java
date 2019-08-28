package com.example.android.clientintelligent.interfaces;

import android.os.AsyncTask;

import com.example.android.clientintelligent.IntelligentModel;
import com.example.android.clientintelligent.IntelligentTask;

import java.io.IOException;
import java.util.List;

public interface Interpreter {
    enum Device {CPU, GPU, NNAPI, VULKAN}

    List<Device> getDevices();
    String getFramework();

    boolean addModel(IntelligentModel model);
    IntelligentModel getModel(String modelName);
    List<String> getModels();

    AsyncTask buildTask(IntelligentTask task, ProgressListener progressListener) throws IOException;
}
