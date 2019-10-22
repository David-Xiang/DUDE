package com.example.android.clientintelligent.framework.interfaces;

public interface IProgressListener {
    void onMsg(String msg);
    void onProgress(int progress, Object ... msgs);
    void onFinish(int count, long enduredTime);
    void onError(String msg);
}
