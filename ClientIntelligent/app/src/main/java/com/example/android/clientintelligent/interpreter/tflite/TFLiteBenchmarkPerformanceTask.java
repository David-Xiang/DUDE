package com.example.android.clientintelligent.interpreter.tflite;

import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.Task;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.util.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TFLiteBenchmarkPerformanceTask extends Task {
    private static final String TAG = "TFLiteBenchmarkPerformanceTask";
    private ArrayList<ByteBuffer> mDataArray;

    TFLiteBenchmarkPerformanceTask(Mission mission, ArrayList<ByteBuffer> dataArray,
                                   IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
        mDataArray = dataArray;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        TFLiteClassifier classifier;

        String modelFilePath = getMission().getModelFilePath();
        String cacheModelPath = String.format("%s/%s",
                getMission().getContext().getCacheDir(),
                modelFilePath.substring(modelFilePath.lastIndexOf("/")+1));
        Log.i(TAG, "loadModelFile(): cacheModelPath = " + cacheModelPath);

        try {
            FileUtil.copyExternalResource2File(modelFilePath, cacheModelPath);

            if (getMission().getModelMode() == Model.Mode.FLOAT32
                    || getMission().getModelMode() == Model.Mode.FLOAT16){
                // TODO
                classifier = new FloatTFLiteClassifier(getMission(), cacheModelPath, 0);
            } else if (getMission().getModelMode() == Model.Mode.QUANTIZED) {
                classifier = new QuantTFLiteClassifier(getMission(), cacheModelPath, 0);
            } else {
                mProgressListener.onError("doInBackground: Wrong mission model!");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        int count = 0;
        int nImages = mDataArray.size();
        long now = SystemClock.uptimeMillis();
        while(now - nStartTime < nSeconds * 1000){
            classifier.runInference(mDataArray.get(count%nImages));
            if (count % 50 == 0){
                now = SystemClock.uptimeMillis();
                publishProgress((int) ((now - nStartTime) / (nSeconds * 10)));
            }
            count++;
        }
        classifier.close();
        return count;
    }
}