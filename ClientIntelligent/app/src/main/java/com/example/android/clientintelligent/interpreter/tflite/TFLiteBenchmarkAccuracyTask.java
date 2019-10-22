package com.example.android.clientintelligent.interpreter.tflite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.Task;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.framework.pojo.Recognition;
import com.example.android.clientintelligent.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TFLiteBenchmarkAccuracyTask extends Task {
    private static final String TAG = "TFLiteBenchmarkPerformanceTask";
    private List<Integer> mLabelIndexList;

    TFLiteBenchmarkAccuracyTask(Mission mission, IProgressListener progressListener, int seconds)
            throws IOException {
        super(mission, progressListener, seconds);
        mLabelIndexList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getMission().getContext().getAssets().open(getMission().getTrueLabelIndexPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabelIndexList.add(Integer.parseInt(line));
        }
        reader.close();
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
                Log.w(TAG, "doInBackground: Wrong mission model!");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        int count = 0;
        int top1count = 0;
        int top5count = 0;
        int dataAmount = getMission().getDataPathList().size();
        long now = SystemClock.uptimeMillis();
        while(now - nStartTime < nSeconds * 1000 && count < dataAmount){
            Bitmap bitmap;
            try {
                InputStream in = getMission().getContext().getAssets().open(getMission().getDataPathList().get(count));
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                mProgressListener.onError("Error in read data!");
                return count;
            }
            List<Recognition> recognitionList = classifier.recognizeImage(bitmap);
            if (mLabelIndexList.get(count) == recognitionList.get(0).getId()){
                top1count++;
            }
            int finalCount = count;
            if (recognitionList
                    .stream()
                    .map(Recognition::getId)
                    .anyMatch(n-> n.equals(mLabelIndexList.get(finalCount)))) {
                top5count++;
            }
            Log.i(TAG, String.format("doInBackground: count = %d, top1count = %d, top5count = %d",
                    count, top1count, top5count));
            if (count > 0 && count % 50 == 0){
                now = SystemClock.uptimeMillis();
                @SuppressLint("DefaultLocale")
                String msg = String.format("Top 1 accuracy is %f%%, top 5 accuracy is %f%%",
                        (float)(top1count) * 100 / count,
                        (float)(top5count) * 100 / count);
                publishProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
            }
            count++;
        }

        now = SystemClock.uptimeMillis();
        @SuppressLint("DefaultLocale")
        String msg = String.format("Top 1 accuracy is %.2f%%, top 5 accuracy is %.2f%%",
                (float)(top1count) * 100 / count,
                (float)(top5count) * 100 / count);
        publishProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
        classifier.close();
        return count;
    }
}