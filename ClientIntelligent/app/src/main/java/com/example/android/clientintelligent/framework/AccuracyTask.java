package com.example.android.clientintelligent.framework;

import android.graphics.Bitmap;

import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.IOException;
import java.util.List;

public abstract class AccuracyTask extends Task {

    public AccuracyTask(Mission mission, IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
    }

    protected abstract void loadLabelList(String path) throws IOException;
    protected abstract void loadLabelIndexList(String path) throws IOException;
    protected abstract void loadModelFile(String path) throws IOException;
    protected abstract void configSession(IInterpreter.Device device, int nThreads);
    protected abstract Bitmap loadValidImage(int index) throws IOException;
    protected abstract List<Recognition> recognizeImage(Bitmap bitmap);
    protected abstract void processReconitions(int index, List<Recognition> recognitions, AccuracyResult result);
    protected abstract void publishResults(AccuracyResult result);
    protected abstract void releaseResources();

    public class AccuracyResult {
        public int total = 0;
        public int top1count = 0;
        public int top5count = 0;
    }
}
