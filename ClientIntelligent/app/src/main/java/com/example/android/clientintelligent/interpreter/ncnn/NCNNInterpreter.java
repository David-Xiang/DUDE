package com.example.android.clientintelligent.interpreter.ncnn;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.SyncInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class NCNNInterpreter extends SyncInterpreter {
    private static final String TAG = "NCNNInterpreter";

    public NCNNInterpreter(Context context) {
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Collections.singletonList(Device.CPU);//, Device.VULKAN);
    }

    @Override
    public String getFramework() {
        return "NCNN";
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
        return new NCNNAccuracyTask(mission, progressListener, mission.getnTime());
    }

    private AsyncTask buildPerformanceTask(Mission mission, IProgressListener progressListener)
        throws IOException {
        return new NCNNPerformanceTask(mission, progressListener, mission.getnTime());
    }
}
