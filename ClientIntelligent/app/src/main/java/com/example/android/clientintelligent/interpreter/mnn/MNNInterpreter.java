package com.example.android.clientintelligent.interpreter.mnn;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.Task;
import com.example.android.clientintelligent.framework.Interpreter;
import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MNNInterpreter extends Interpreter {
    private static final String TAG = "MNNInterpreter";

    public MNNInterpreter(Context context) {
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.OPENGL, Device.OPENCL, Device.VULKAN);
    }

    @Override
    public String getFramework() {
        return "MNN";
    }

    @Override
    public AsyncTask buildTask(Mission task, IProgressListener progressListener)
            throws IOException {
        MNNNetInstance instance = MNNNetInstance.createFromFile(task.getModelFilePath());
        MNNNetInstance.Config config= new MNNNetInstance.Config();
        switch(task.getDevice()){
            case CPU: config.forwardType = MNNForwardType.FORWARD_CPU.type; break;
            case OPENCL: config.forwardType = MNNForwardType.FORWARD_OPENCL.type; break;
            case OPENGL: config.forwardType = MNNForwardType.FORWARD_OPENGL.type; break;
            case VULKAN: config.forwardType = MNNForwardType.FORWARD_VULKAN.type; break;
            default: Log.e(TAG, "buildTask: Wrong device type!");return null;
        }
        config.numThread = task.getnThreads();
        // TODO
        return null;
    }

    private class MNNAccuracyTask extends Task {
        // TODO
        MNNNetInstance instance;
        ArrayList<ByteBuffer> mDataArray;

        MNNAccuracyTask(ArrayList<ByteBuffer> dataArray, IProgressListener progressListener,
                        int seconds){
            super(progressListener, seconds);
            mDataArray = dataArray;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            int count = 0;
            int nImages = mDataArray.size();
            long now = SystemClock.uptimeMillis();
            while(now - nStartTime < nSeconds * 1000){

                //mClassifier.runInference(mDataArray.get(count%nImages));
                if (count % 5000 == 0){
                    now = SystemClock.uptimeMillis();
                    publishProgress((int) ((now - nStartTime) / (nSeconds * 10)));
                }
                count++;
            }
            return count;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            instance.release();
        }
    }
}
