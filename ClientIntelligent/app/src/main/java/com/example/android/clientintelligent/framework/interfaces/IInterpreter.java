package com.example.android.clientintelligent.framework.interfaces;

import com.example.android.clientintelligent.framework.pojo.Model;

import java.util.List;

public interface IInterpreter {
    enum Device {CPU, GPU, NNAPI, VULKAN, OPENGL, OPENCL, WEBGL}

    List<Device> getDevices();
    String getFramework();

    void addModel(Model model);
    Model getModel(String modelName);
    List<String> getModels();
}
