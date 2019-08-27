package com.example.android.clientintelligent;

import android.content.Context;

import com.example.android.clientintelligent.interpreter.tflite.TFLiteInterpreter;

public class IntelligentEngineImpl extends IntelligentEngine {
    IntelligentEngineImpl(Context context) {
        super(context);
    }

    @Override
    public void initInterpreters() {
        mInterpreters.add(new TFLiteInterpreter(mContext));
    }

    @Override
    void initData() {

    }

    @Override
    public void initModels() {

    }
}
