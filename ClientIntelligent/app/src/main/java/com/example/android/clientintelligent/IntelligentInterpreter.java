package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.interfaces.Interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class IntelligentInterpreter implements Interpreter {
    protected Context mContext;
    protected List<IntelligentModel> mModels;

    protected IntelligentInterpreter(Context context) {
        mContext = context;
        mModels = new ArrayList<>();
    }

    public boolean addModel(IntelligentModel model) {
        mModels.add(model);
        return true;
    }

    public IntelligentModel getModel(String modelName) {
        return mModels.stream().filter(m->m.getFilePath().equals(modelName)).findFirst().orElse(null);
    }

    public List<String> getModels() {
        return mModels.stream().map(IntelligentModel::getFilePath).collect(Collectors.toList());
    }
}
