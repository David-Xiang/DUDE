package com.example.android.clientintelligent.framework;

import android.os.AsyncTask;
import android.os.SystemClock;

import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;

public abstract class Task extends AsyncTask<Object, Object, Object> {
    protected IProgressListener mProgressListener;
    protected long nStartTime;
    protected int nSeconds;
    private Mission mMission;

    protected Task(Mission mission, IProgressListener progressListener, int seconds) {
        mMission = mission;
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
        if (objects.length > 1) {
            mProgressListener.onProgress((int)objects[0], (String)objects[1]);
        } else {
            mProgressListener.onProgress((int)objects[0], null);
        }
    }


    @Override
    protected void onPostExecute(Object result) {
        long enduredTime = SystemClock.uptimeMillis() - nStartTime;
        mProgressListener.onFinish((int) result, enduredTime);
    }

    public Mission getMission() {
        return mMission;
    }
}
