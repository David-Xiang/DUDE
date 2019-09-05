package com.example.android.clientintelligent.framework.interfaces;

public interface IProgressListener {
    void onProgress(int progress, String msg);
    void onFinish(int count, long enduredTime);
    void onError(String msg);
}
