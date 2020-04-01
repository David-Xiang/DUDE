package com.example.android.clientintelligent.framework;

import android.graphics.Bitmap;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;

import java.io.IOException;

public abstract class PerformanceTask extends Task {

    public PerformanceTask(Mission mission, IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
    }

    protected abstract void loadModelFile(Model model) throws IOException;
    protected abstract void configSession(IInterpreter.Device device, int nThreads);
    protected abstract Bitmap loadValidImage(int index) throws IOException;
//    protected abstract List<Recognition> recognizeImage(Bitmap bitmap);
    protected abstract void releaseResources();
}
