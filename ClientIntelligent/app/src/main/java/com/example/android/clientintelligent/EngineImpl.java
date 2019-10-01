package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.framework.Data;
import com.example.android.clientintelligent.framework.Engine;
import com.example.android.clientintelligent.framework.Model;
import com.example.android.clientintelligent.interpreter.mnn.MNNInterpreter;
import com.example.android.clientintelligent.interpreter.ncnn.NCNNInterpreter;
import com.example.android.clientintelligent.interpreter.tfjs.TFJSInterpreter;
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
        addInterpreter(new TFJSInterpreter(getContext()));
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
        for (int i = 0; i < 1000; i++){
            ilsvrcDataPathList.add(String.format("ilsvrc2012/images/ILSVRC2012_val_%08d.JPEG", i+1));
        }
        mDataMap.put("ilsvrc_quant", new Data(ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt","ilsvrc2012/labels.txt",
                224, 224, 1, 3));
        mDataMap.put("ilsvrc", new Data(ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt","ilsvrc2012/labels.txt",
                224, 224, 4, 3));
    }

    @Override
    public void initModels() {
        // [quant]

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

        // [classic model]
        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2.tflite");
        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/mobilenetV2_quant.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/resnet50.tflite");
//        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/resnet50_quant.tflite");
        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/resnet50v2.tflite");
        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/resnet50v2_quant.tflite");
        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/densenet121.tflite");
        addTFLiteModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tflite/densenet121_quant.tflite");
        addMNNModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/mnn/mobilenetV2.mnn");
//        addMNNModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/mnn/shufflenet_v2_x1_0.mnn");
        addMNNModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/mnn/resnet50.mnn");
//        addMNNModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/mnn/resnext50.mnn");
        addMNNModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/mnn/densenet121.mnn");
        addTFJSModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tfjs/mobilenetV2/mobilenetV2.json");
        addTFJSModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tfjs/resnet50/resnet50.json");
        addTFJSModel(mDataMap.get("ilsvrc"), "ilsvrc2012/models/tfjs/densenet121/densenet121.json");

        // [mnist dnn model]
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-1-32/mnist-1-32.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-1-64/mnist-1-64.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-1-128/mnist-1-128.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-1-256/mnist-1-256.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-1-512/mnist-1-512.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-1-1024/mnist-1-1024.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-2-32/mnist-2-32.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-2-64/mnist-2-64.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-2-128/mnist-2-128.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-2-256/mnist-2-256.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-2-512/mnist-2-512.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-2-1024/mnist-2-1024.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-4-32/mnist-4-32.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-4-64/mnist-4-64.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-4-128/mnist-4-128.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-4-256/mnist-4-256.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-4-512/mnist-4-512.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-4-1024/mnist-4-1024.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-8-32/mnist-8-32.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-8-64/mnist-8-64.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-8-128/mnist-8-128.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-8-256/mnist-8-256.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-8-512/mnist-8-512.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-8-1024/mnist-8-1024.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-16-32/mnist-16-32.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-16-64/mnist-16-64.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-16-128/mnist-16-128.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-16-256/mnist-16-256.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-16-512/mnist-16-512.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-16-1024/mnist-16-1024.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-32-32/mnist-32-32.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-32-64/mnist-32-64.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-32-128/mnist-32-128.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-32-256/mnist-32-256.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-32-512/mnist-32-512.json");
        addTFJSModel(mDataMap.get("mnist"), "mnist/models/tfjs/mnist-32-1024/mnist-32-1024.json");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-64.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-128.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-256.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-512.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-1-1024.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-2-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-2-64.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-2-128.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-2-256.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-2-512.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-2-1024.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-64.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-128.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-256.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-512.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-4-1024.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-8-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-8-64.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-8-128.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-8-256.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-8-512.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-8-1024.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-16-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-16-64.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-16-128.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-16-256.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-16-512.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-16-1024.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-32-32.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-32-64.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-32-128.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-32-256.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-32-512.tflite");
//        addTFLiteModel(mDataMap.get("mnist"), "mnist/models/tflite/mnist-32-1024.tflite");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-1-32.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-1-64.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-1-128.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-1-256.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-1-512.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-1-1024.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-2-32.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-2-64.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-2-128.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-2-256.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-2-512.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-2-1024.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-4-32.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-4-64.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-4-128.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-4-256.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-4-512.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-4-1024.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-8-32.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-8-64.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-8-128.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-8-256.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-8-512.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-8-1024.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-16-32.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-16-64.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-16-128.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-16-256.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-16-512.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-16-1024.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-32-32.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-32-64.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-32-128.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-32-256.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-32-512.mnn");
//        addMNNModel(mDataMap.get("mnist"), "mnist/models/mnn/mnist-32-1024.mnn");

        // [imagenet cnn model]
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-1-1/cnn-1-1.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-1-2/cnn-1-2.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-1-4/cnn-1-4.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-1-8/cnn-1-8.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-2-1/cnn-2-1.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-2-2/cnn-2-2.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-2-4/cnn-2-4.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-2-8/cnn-2-8.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-4-1/cnn-4-1.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-4-2/cnn-4-2.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-4-4/cnn-4-4.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-4-8/cnn-4-8.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-8-1/cnn-8-1.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-8-2/cnn-8-2.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-8-4/cnn-8-4.json");
        addTFJSModel(mDataMap.get("imagenet224"), "imagenet224/models/tfjs/cnn-8-8/cnn-8-8.json");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-1-1.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-1-2.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-1-4.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-1-8.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-2-1.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-2-2.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-2-4.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-2-8.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-4-1.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-4-2.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-4-4.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-4-8.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-8-1.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-8-2.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-8-4.tflite");
//        addTFLiteModel(mDataMap.get("imagenet224"), "imagenet224/models/tflite/cnn-8-8.tflite");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-1-1.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-1-2.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-1-4.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-1-8.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-2-1.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-2-2.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-2-4.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-2-8.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-4-1.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-4-2.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-4-4.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-4-8.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-8-1.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-8-2.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-8-4.mnn");
//        addMNNModel(mDataMap.get("imagenet224"), "imagenet224/models/mnn/cnn-8-8.mnn");

    }

    private void addTFLiteModel(Data data, String modelFilePath){
        addTFLiteModel(data, modelFilePath, Model.Mode.FLOAT32);
    }

    private void addTFLiteModel(Data data, String modelFilePath, Model.Mode mode){
        this.getInterpreter("TensorFlow Lite")
                .addModel(new Model(data, modelFilePath, mode));
    }

    private void addTFJSModel(Data data, String modelFilePath){
        addTFJSModel(data, modelFilePath, Model.Mode.FLOAT32);
    }

    private void addTFJSModel(Data data, String modelFilePath, Model.Mode mode){
        this.getInterpreter("TensorFlow.js")
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
