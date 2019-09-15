package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.framework.Data;
import com.example.android.clientintelligent.framework.Engine;
import com.example.android.clientintelligent.framework.Model;
import com.example.android.clientintelligent.interpreter.mnn.MNNInterpreter;
import com.example.android.clientintelligent.interpreter.ncnn.NCNNInterpreter;
import com.example.android.clientintelligent.interpreter.tflite.TFLiteInterpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineImpl extends Engine {
    private Map<String, Data> mDataMap;

    EngineImpl(Context context) {
        super(context);
    }

    @Override
    public void initInterpreters() {
        addInterpreter(new TFLiteInterpreter(getContext()));
        addInterpreter(new MNNInterpreter(getContext()));
//        addInterpreter(new NCNNInterpreter(getContext()));
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

        mDataMap.put("mnist", new Data(mnistDataPathList, null,"mnist/labels.txt",
                28, 28, 4, 1));

        // ImageNet 224*224
        List<String> imagenet224DataPathList = Arrays.asList(
                "imagenet224/images/pic0.png", "imagenet224/images/pic1.png", "imagenet224/images/pic2.png",
                "imagenet224/images/pic3.png", "imagenet224/images/pic4.png", "imagenet224/images/pic5.png",
                "imagenet224/images/pic6.png", "imagenet224/images/pic7.png", "imagenet224/images/pic8.png",
                "imagenet224/images/pic9.png"
        );
        mDataMap.put("imagenet224_quant", new Data(imagenet224DataPathList, "imagenet224/answer.txt","imagenet224/labels.txt",
                224, 224, 1, 3));
        mDataMap.put("imagenet224", new Data(imagenet224DataPathList, "imagenet224/answer.txt","imagenet224/labels.txt",
                224, 224, 4, 3));

        // ILSVRC2012
        List<String> ilsvrcDataPathList = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            ilsvrcDataPathList.add(String.format("ilsvrc2012/images/ILSVRC2012_val_%08d.JPEG", i+1));
        }
        mDataMap.put("ilsvrc_quant", new Data(ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt","ilsvrc2012/labels.txt",
                224, 224, 1, 3));
        mDataMap.put("ilsvrc", new Data(ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt","ilsvrc2012/labels.txt",
                224, 224, 4, 3));
    }

    @Override
    public void initModels() {
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-256.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_v1_1.0_224_quant.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_v1_1.0_224.tflite");

//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_bfloat16.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_float32.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_float16.tflite", Model.Mode.FLOAT16);
//        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_int32.tflite", Model.Mode.QUANTIZED);
//        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_int64.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenet_uint8.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_double.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_optimize_latency.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenet_optimize_size.tflite");
//
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_bfloat16.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_float32.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_float16.tflite", Model.Mode.FLOAT16);
//        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenetV2_int32.tflite", Model.Mode.QUANTIZED);
//        //addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenetV2_int64.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("imagenet224_quant"), "imagenet224/models/tflite/mobilenetV2_uint8.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_double.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_optimize_latency.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/mobilenetV2_optimize_size.tflite");

//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet_bfloat16.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet_float32.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet_float16.tflite", Model.Mode.FLOAT16);
////        addTFLiteModel(mDataMap.get("ilsvrc_quant"), "ilsvrc2012/models/tflite/mobilenet_int32.tflite", Model.Mode.QUANTIZED);
////        addTFLiteModel(mDataMap.get("ilsvrc_quant"), "ilsvrc2012/models/tflite/mobilenet_int64.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("ilsvrc_quant"), "ilsvrc2012/models/tflite/mobilenet_uint8.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet_double.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet_optimize_latency.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenet_optimize_size.tflite");
//
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_bfloat16.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_float32.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_float16.tflite", Model.Mode.FLOAT16);
////        addTFLiteModel(mDataMap.get("ilsvrc_quant"), "ilsvrc2012/models/tflite/mobilenetV2_int32.tflite", Model.Mode.QUANTIZED);
////        addTFLiteModel(mDataMap.get("ilsvrc_quant"), "ilsvrc2012/models/tflite/mobilenetV2_int64.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("ilsvrc_quant"), "ilsvrc2012/models/tflite/mobilenetV2_uint8.tflite", Model.Mode.QUANTIZED);
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_double.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_optimize_latency.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_optimize_size.tflite");

        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-1-1.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-1-2.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-1-4.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-1-8.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-2-1.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-2-2.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-2-4.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-2-8.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-4-1.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-4-2.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-4-4.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-4-8.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-8-1.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-8-2.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-8-4.mnn");
        addTFLiteModel(mDataMap.get("imagenet224"), "ilsvrc2012/models/tflite/cnn-8-8.mnn");
    }

    private void addTFLiteModel(Data data, String modelFilePath){
        addTFLiteModel(data, modelFilePath, Model.Mode.FLOAT32);
    }

    private void addTFLiteModel(Data data, String modelFilePath, Model.Mode mode){
        this.getInterpreter("TensorFlow Lite")
                .addModel(new Model(data, modelFilePath, mode));
    }

    private void addMNNModel(Data data, String modelFilePath){
        addMNNModel(data, modelFilePath, Model.Mode.FLOAT32);
    }

    private void addMNNModel(Data data, String modelFilePath, Model.Mode mode){
        this.getInterpreter("MNN")
                .addModel(new Model(data, modelFilePath, mode));
    }

    private void addNCNNModel(Data data, String modelFilePath){
        addNCNNModel(data, modelFilePath, Model.Mode.FLOAT32);
    }

    private void addNCNNModel(Data data, String modelFilePath, Model.Mode mode){
        this.getInterpreter("NCNN")
                .addModel(new Model(data, modelFilePath, mode));
    }
}
