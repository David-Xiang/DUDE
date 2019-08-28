package com.example.android.clientintelligent.interfaces;

public interface ProgressListener {
    void onProgress(int progress);
    void onFinish(int count, long enduredTime);
}
