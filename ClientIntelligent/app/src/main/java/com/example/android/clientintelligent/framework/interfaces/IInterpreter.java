package com.example.android.clientintelligent.framework.interfaces;

import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.Model;
import com.example.android.clientintelligent.framework.Mission;

import java.util.List;

public interface IInterpreter {
    enum Device {CPU, GPU, NNAPI, VULKAN, OPENGL, OPENCL, WEBGL}

    List<Device> getDevices();
    String getFramework();

    boolean addModel(Model model);
    Model getModel(String modelName);
    List<String> getModels();
}
