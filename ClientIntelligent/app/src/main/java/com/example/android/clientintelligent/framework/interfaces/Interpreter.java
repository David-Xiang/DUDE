package com.example.android.clientintelligent.framework.interfaces;

import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.IntelligentModel;
import com.example.android.clientintelligent.framework.IntelligentMission;

import java.io.IOException;
import java.util.List;

public interface Interpreter {
    enum Device {CPU, GPU, NNAPI, VULKAN, OPENGL, OPENCL}

    List<Device> getDevices();
    String getFramework();

    boolean addModel(IntelligentModel model);
    IntelligentModel getModel(String modelName);
    List<String> getModels();

    AsyncTask buildTask(IntelligentMission task, ProgressListener progressListener) throws IOException;
}
