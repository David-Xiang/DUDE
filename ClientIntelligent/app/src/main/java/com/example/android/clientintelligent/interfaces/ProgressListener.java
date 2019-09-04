package com.example.android.clientintelligent.interfaces;

public interface ProgressListener {
    void onProgress(int progress, String msg);
    void onFinish(int count, long enduredTime);
    void onError(String msg);
}
