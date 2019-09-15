package com.example.android.clientintelligent.interpreter.ncnn;

import android.graphics.Bitmap;

public class NCNNNative {
    private static final String TAG = "NCNNNative";

    public static native boolean InitModel(byte[] param, byte[] bin);

    public static native float [] Detect(Bitmap bitmap, boolean use_gpu, int threads);

    public static native void Release();

    static {
        System.loadLibrary("ncnncore");
    }
}
