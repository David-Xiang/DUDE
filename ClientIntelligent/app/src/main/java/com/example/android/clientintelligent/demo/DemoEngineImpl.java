package com.example.android.clientintelligent.demo;

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
import com.example.android.clientintelligent.interpreter.tflite.TFLiteInterpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DemoEngineImpl extends Engine {
    private static final String TAG = "DemoEngineImpl";
    private DataSet dataSet;

    DemoEngineImpl(Context context) {
        super(context);
    }

    @Override
    public void initInterpreters() {
        addInterpreter(new TFLiteInterpreter(getContext()));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void initData() {
        // ILSVRC2012
        List<String> ilsvrcDataPathList = new ArrayList<>();
        for (int i = 0; i < 1000; i++){
            ilsvrcDataPathList.add(String.format("ilsvrc2012/images/ILSVRC2012_val_%08d.JPEG", i+1));
        }
        dataSet = new DataSet("ilsvrc", ilsvrcDataPathList, "ilsvrc2012/ILSVRC2012_validation_ground_truth_mapped.txt",
                "ilsvrc2012/labels.txt", 224, 224, 4, 3);

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

            String interpreter = jsonObject.getString("interpreter");
            if (getInterpreter(interpreter) == null){
                continue;
            }

            String modelName = jsonObject.getString("model_name");
            String modelFilePath = jsonObject.getString("model_file_path");
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
            if (dataset.equals(dataSet.getName())) {
                Model model = new Model(dataSet.getMetaData(), modelFilePath, mode, dataset, accuracy);
                getInterpreter(interpreter).addModel(model);
            }
        }
    }

    @Override
    public Mission buildMission(Context context, Model model, DataSet dataSet, Mission.Purpose purpose,
                                IInterpreter.Device device, int threads, int timeLimit) {
        return null;
    }

    public Mission buildMission(Context context, List<Model> models, DataSet dataSet, Mission.Purpose purpose,
                                IInterpreter.Device device, int threads, int timeLimit) {
        return new Mission(context, models, dataSet, purpose, device, threads, timeLimit);
    }

    public Mission buildSmartSwitchMission(Context context, IInterpreter interpreter,
                                           Mission.Purpose purpose, IInterpreter.Device device,
                                           int threads, int timeLimit) {
        return this.buildMission(context, interpreter.getModels(), dataSet, purpose, device, threads, timeLimit);
    }
}
