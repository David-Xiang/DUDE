package com.example.android.clientintelligent.interpreter.mnn;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.clientintelligent.framework.Interpreter;
import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.example.android.clientintelligent.framework.interfaces.IInterpreter.Device.VULKAN;

public class MNNInterpreter extends Interpreter {
    private static final String TAG = "MNNInterpreter";

    public MNNInterpreter(Context context) {
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.OPENGL, Device.OPENCL, VULKAN);
    }

    @Override
    public String getFramework() {
        return "MNN";
    }

    @Override
    public AsyncTask buildTask(Mission mission, IProgressListener progressListener)
            throws Exception {
        switch (mission.getPurpose()) {
            case ACCURACY:
                return buildAccuracyTask(mission, progressListener);
            case PERFORMANCE:
                return buildPerformanceTask(mission, progressListener);
            default:
                throw new Exception("Wrong Purpose");
        }
    }

    private AsyncTask buildAccuracyTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        return new MNNAccuracyTask(mission, progressListener, mission.getnTime());
    }

    private AsyncTask buildPerformanceTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        return new MNNPerformanceTask(mission, progressListener, mission.getnTime());
    }
}
