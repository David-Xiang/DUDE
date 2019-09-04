package com.example.android.clientintelligent;

import android.os.AsyncTask;
import android.os.SystemClock;

import com.example.android.clientintelligent.interfaces.ProgressListener;

public abstract class InferenceTask extends AsyncTask<Object, Object, Object> {
    protected ProgressListener mProgressListener;
    protected long nStartTime;
    protected int nSeconds;

    protected InferenceTask(ProgressListener progressListener, int seconds) {
        mProgressListener = progressListener;
        nSeconds = seconds;
    }

    @Override
    protected void onPreExecute() {
        nStartTime = SystemClock.uptimeMillis();
    }

    @Override
    protected void onProgressUpdate(Object... objects) {
        super.onProgressUpdate(objects);
        mProgressListener.onProgress((int)objects[0], (String)objects[1]);
    }


    @Override
    protected void onPostExecute(Object result) {
        long enduredTime = SystemClock.uptimeMillis() - nStartTime;
        mProgressListener.onFinish((int) result, enduredTime);
    }
}
