package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.interpreter.tflite.TFLiteInterpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntelligentEngineImpl extends IntelligentEngine {
    private Map<String, IntelligentData> mDataMap;

    IntelligentEngineImpl(Context context) {
        super(context);
    }

    @Override
    public void initInterpreters() {
        mInterpreters.clear();
        mInterpreters.add(new TFLiteInterpreter(mContext));
    }

    @Override
    public void initData() {
        mDataMap = new HashMap<>();

        // MNIST
        List<String> mnistDataPathList = Arrays.asList(
                "MNIST/images/0.png", "MNIST/images/1.png", "MNIST/images/2.png",
                "MNIST/images/3.png", "MNIST/images/4.png", "MNIST/images/5.png",
                "MNIST/images/6.png", "MNIST/images/7.png", "MNIST/images/8.png",
                "MNIST/images/9.png");

        mDataMap.put("MNIST", new IntelligentData(mnistDataPathList, "MNIST/labels.txt",
                28, 28, 4, 1));
    }

    @Override
    public void initModels() {
        this.getInterpreter("TensorFlow Lite").addModel(new IntelligentModel(mDataMap.get("MNIST"), "MNIST/models/tflite/mnist-1-32.tflite"));
        this.getInterpreter("TensorFlow Lite").addModel(new IntelligentModel(mDataMap.get("MNIST"), "MNIST/models/tflite/mnist-4-256.tflite"));
    }
}
