package com.example.android.clientintelligent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.android.clientintelligent.framework.Engine;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.pojo.DataSet;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.interpreter.ncnn.NCNNInterpreter;
import com.example.android.clientintelligent.interpreter.tvm.TVMInterpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EngineImpl extends Engine {
    private static final String TAG = "EngineImpl";
    private Map<String, DataSet> mDataMap;
    private Map<Model, DataSet> mModelDataMap;

    EngineImpl(Context context) {
        super(context);
    }

    @Override
    public void initInterpreters() {
//        addInterpreter(new TFLiteInterpreter(getContext()));
//        addInterpreter(new MNNInterpreter(getContext()));
//        addInterpreter(new TFJSInterpreter(getContext()));
//        addInterpreter(new NCNNInterpreter(getContext()));
        addInterpreter(new TVMInterpreter(getContext()));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void initData() {
        mDataMap = new HashMap<>();
        mModelDataMap = new HashMap<>();

        // mnist
        List<String> mnistDataPathList = Arrays.asList(
                "mnist/images/0.png", "mnist/images/1.png", "mnist/images/2.png",
                "mnist/images/3.png", "mnist/images/4.png", "mnist/images/5.png",
                "mnist/images/6.png", "mnist/images/7.png", "mnist/images/8.png",
                "mnist/images/9.png");

        mDataMap.put("mnist", new DataSet("mnist", mnistDataPathList, null,"mnist/labels.txt",
                28, 28, 4, 1, 10));

        // ImageNet 224*224
        List<String> imagenet224DataPathList = Arrays.asList(
                "imagenet224/images/pic0.png", "imagenet224/images/pic1.png", "imagenet224/images/pic2.png",
                "imagenet224/images/pic3.png", "imagenet224/images/pic4.png", "imagenet224/images/pic5.png",
                "imagenet224/images/pic6.png", "imagenet224/images/pic7.png", "imagenet224/images/pic8.png",
                "imagenet224/images/pic9.png"
        );
        mDataMap.put("imagenet224_quant", new DataSet("imagenet224_quant", imagenet224DataPathList, "imagenet224/answer.txt","imagenet224/labels.txt",
                224, 224, 1, 3, 1000));
        mDataMap.put("imagenet224", new DataSet("imagenet224", imagenet224DataPathList, "imagenet224/answer.txt","imagenet224/labels.txt",
                224, 224, 4, 3, 1000));

        // ILSVRC2012
        List<String> ilsvrcDataPathList = new ArrayList<>();
        for (int i = 0; i < 1000; i++){
            ilsvrcDataPathList.add(String.format("ilsvrc2012/images/ILSVRC2012_val_%08d.JPEG", i+1));
        }
        mDataMap.put("ilsvrc_quant", new DataSet("ilsvrc_quant", ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt","ilsvrc2012/labels.txt",
                224, 224, 1, 3, 1000));
        mDataMap.put("ilsvrc", new DataSet("ilsvrc", ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt","ilsvrc2012/labels.txt",
                224, 224, 4, 3, 1000));
    }

    @Override
    public void initModels() throws Exception {
        Log.i(TAG, "External path = " + Environment.getExternalStorageDirectory());
        File file = new File(Environment.getExternalStorageDirectory(), "/CIBench/modelConfig.json");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = Objects.requireNonNull(reader).readLine()) != null) {
            stringBuilder.append(line);
        }
        Log.i(TAG, "JSON: " + stringBuilder.toString());
        JSONArray jsonArray = JSON.parseArray(stringBuilder.toString());
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            String modelName = jsonObject.getString("model_name");
            String modelFilePath = jsonObject.getString("model_file_path"); // graph
            String paramFilePath = jsonObject.getString("param_file_path"); // parameters
            String libCpuPath = jsonObject.getString("lib_cpu_path"); // lib.so for tvm
            String libGpuPath = jsonObject.getString("lib_gpu_path"); // lib.so for tvm
            String interpreter = jsonObject.getString("interpreter");
            String dataset = jsonObject.getString("dataset");

            String dataType = jsonObject.getString("data_type");
            dataType = dataType == null ? "" : dataType;
            Model.Mode mode;
            switch (dataType) {
                case "float16": mode = Model.Mode.FLOAT16; break;
                case "quantized": mode = Model.Mode.QUANTIZED; break;
                default: mode = Model.Mode.FLOAT32;
            }
            Float accuracy = jsonObject.getFloat("accuracy");
            accuracy = accuracy == null ? 0 : accuracy;
            if (mDataMap.get(dataset) != null && getInterpreter(interpreter) != null) {
                Model model = new Model(Objects.requireNonNull(mDataMap.get(dataset)).getMetaData(), modelFilePath, paramFilePath, libCpuPath, libGpuPath, mode, dataset, accuracy);
                getInterpreter(interpreter).addModel(model);
                mModelDataMap.put(model, Objects.requireNonNull(mDataMap.get(dataset)));
            }
        }
        reader.close();
    }

    @Override
    public Mission buildMission(Context context, Model model, DataSet dataSet, Mission.Purpose purpose, IInterpreter.Device device, int threads, int timeLimit) {
        return new Mission(context, model, dataSet, purpose, device, threads, timeLimit);
    }

    public Mission buildDefaultMission(Context context, Model model, Mission.Purpose purpose, IInterpreter.Device device, int threads, int timeLimit) {
        return buildMission(context, model, mModelDataMap.get(model), purpose, device, threads, timeLimit);
    }

    public DataSet getDefaultData(Model model) {
        return mModelDataMap.get(model);
    }
}
