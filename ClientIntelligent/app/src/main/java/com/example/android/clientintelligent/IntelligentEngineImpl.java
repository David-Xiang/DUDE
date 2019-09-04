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

        // mnist
        List<String> mnistDataPathList = Arrays.asList(
                "mnist/images/0.png", "mnist/images/1.png", "mnist/images/2.png",
                "mnist/images/3.png", "mnist/images/4.png", "mnist/images/5.png",
                "mnist/images/6.png", "mnist/images/7.png", "mnist/images/8.png",
                "mnist/images/9.png");

        mDataMap.put("mnist", new IntelligentData(mnistDataPathList, null,"mnist/labels.txt",
                28, 28, 4, 1));

        // ImageNet 224*224
        List<String> imagenet224DataPathList = Arrays.asList(
                "imagenet224/images/pic0.png", "imagenet224/images/pic1.png", "imagenet224/images/pic2.png",
                "imagenet224/images/pic3.png", "imagenet224/images/pic4.png", "imagenet224/images/pic5.png",
                "imagenet224/images/pic6.png", "imagenet224/images/pic7.png", "imagenet224/images/pic8.png",
                "imagenet224/images/pic9.png"
        );
        mDataMap.put("imagenet224_quant", new IntelligentData(imagenet224DataPathList, "imagenet224/answer.txt","imagenet224/labels.txt",
                224, 224, 1, 3));
        mDataMap.put("imagenet224", new IntelligentData(imagenet224DataPathList, "imagenet224/answer.txt","imagenet224/labels.txt",
                224, 224, 4, 3));
    }

    @Override
    public void initModels() {
        //addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-32.tflite");
        //addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-256.tflite");
        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_v1_1.0_224_quant.tflite", IntelligentModel.Mode.QUANTIZED);
        //addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_v1_1.0_224.tflite");

        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_bfloat16.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_float32.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_float16.tflite", IntelligentModel.Mode.FLOAT16);
        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_int32.tflite", IntelligentModel.Mode.QUANTIZED);
        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_int64.tflite", IntelligentModel.Mode.QUANTIZED);
        addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_uint8.tflite", IntelligentModel.Mode.QUANTIZED);
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_double.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_optimize_latency.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_optimize_size.tflite");

        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_bfloat16.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_float32.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_float16.tflite", IntelligentModel.Mode.FLOAT16);
        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenetV2_int32.tflite", IntelligentModel.Mode.QUANTIZED);
        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenetV2_int64.tflite", IntelligentModel.Mode.QUANTIZED);
        addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenetV2_uint8.tflite", IntelligentModel.Mode.QUANTIZED);
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_double.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_optimize_latency.tflite");
        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_optimize_size.tflite");

    }

    private void addTFLiteModel(IntelligentData data, String modelFilePath){
        addTFLiteModel(data, modelFilePath, IntelligentModel.Mode.FLOAT32);
    }

    private void addTFLiteModel(IntelligentData data, String modelFilePath, IntelligentModel.Mode mode){
        this.getInterpreter("TensorFlow Lite")
                .addModel(new IntelligentModel(data, modelFilePath, mode));
    }
}
