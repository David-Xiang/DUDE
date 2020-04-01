package com.example.android.clientintelligent.interpreter.tvm;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.SyncInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TVMInterpreter extends SyncInterpreter {
    private static final String TAG = "TVMInterpreter";

    public TVMInterpreter(Context context) {
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.OPENCL);
    }

    @Override
    public String getFramework() {
        return "TVM";
    }

    @Override
    public AsyncTask buildTask(Mission mission, IProgressListener progressListener) throws Exception {
        switch (mission.getPurpose()) {
            case BENCH_ACCURACY:
                return buildAccuracyTask(mission, progressListener);
            case BENCH_PERFORMANCE:
                return buildPerformanceTask(mission, progressListener);
            default:
                throw new Exception("Wrong Purpose");
        }
    }

    private AsyncTask buildAccuracyTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        return new TVMAccuracyTask(mission, progressListener, mission.getnTime());
    }

    private AsyncTask buildPerformanceTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        return new TVMPerformanceTask(mission, progressListener, mission.getnTime());
    }

}
