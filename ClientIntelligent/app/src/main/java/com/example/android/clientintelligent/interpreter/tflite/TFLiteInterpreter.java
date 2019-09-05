package com.example.android.clientintelligent.interpreter.tflite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.Model;
import com.example.android.clientintelligent.framework.Task;
import com.example.android.clientintelligent.framework.Interpreter;
import com.example.android.clientintelligent.framework.Recognition;
import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFLiteInterpreter extends Interpreter {
    /** MobileNet requires additional normalization of the used input. */
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private static final String TAG = "TFLiteInterpreter";

    public TFLiteInterpreter(Context context){
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.GPU, Device.NNAPI);
    }

    @Override
    public String getFramework() {
        return "TensorFlow Lite";
    }

    @Override
    public AsyncTask buildTask(Mission task, IProgressListener progressListener)
            throws IOException {
        switch (task.getPurpose()) {
            case PERFORMANCE:
                return buildPerformanceTask(task, progressListener);
            case ACCURACY:
                return buildAccuracyTask(task, progressListener);
            default:
                progressListener.onError("Wrong Purpose!"); return null;
        }
    }

    private AsyncTask buildAccuracyTask(Mission task, IProgressListener progressListener)
            throws IOException {
        return new TFLiteAccuracyTask(task, progressListener, task.getnTime());
    }

    private AsyncTask buildPerformanceTask(Mission task, IProgressListener progressListener)
            throws IOException {
        int[] intValues = new int[task.getnImageSizeX() * task.getnImageSizeY()];
        ArrayList<ByteBuffer> images = new ArrayList<>();

        // convert data
        for (int s = 0; s < task.getDataPathList().size(); s++){
            InputStream in = mContext.getAssets().open(task.getDataPathList().get(s));
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            ByteBuffer imgData = ByteBuffer.allocateDirect(
                    task.getnImageSizeX() *
                    task.getnImageSizeY() *
                    task.getChannelsPerPixel() *
                    task.getBytesPerChannel());
            imgData.order(ByteOrder.nativeOrder());
            imgData.rewind();
            bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                    bitmap.getWidth(), bitmap.getHeight());
            // Convert the image to floating point.
            int pixel = 0;
            for (int i = 0; i < task.getnImageSizeX(); ++i) {
                for (int j = 0; j < task.getnImageSizeY(); ++j) {
                    final int val = intValues[pixel++];
                    addPixelValue(task.getModelMode(), task.getChannelsPerPixel(), imgData, val);
                }
            }
            images.add(imgData);
        }
        return new TFLitePerformanceTask(task, images, progressListener, task.getnTime());
    }

    private void addPixelValue(Model.Mode mode, int channels, ByteBuffer imgData,
                               int pixelValue) {
        if ((mode == Model.Mode.FLOAT32 || mode == Model.Mode.FLOAT16)
                && channels == 3) {
            imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        } else if (mode == Model.Mode.FLOAT32 && channels == 1){
            imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        } else if (mode == Model.Mode.QUANTIZED) {
            imgData.put((byte) ((pixelValue >> 16) & 0xFF));
            imgData.put((byte) ((pixelValue >> 8) & 0xFF));
            imgData.put((byte) (pixelValue & 0xFF));
        } else {
            Log.w(TAG, "addPixelValue: Wrong model mode!");
        }
    }

    private class TFLiteAccuracyTask extends Task {
        private static final String TAG = "TFLitePerformanceTask";
        Mission mTask;
        List<Integer> mLabelIndexList;

        TFLiteAccuracyTask(Mission task, IProgressListener progressListener, int seconds)
                throws IOException {
            super(progressListener, seconds);
            mTask = task;
            mLabelIndexList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            mContext.getAssets().open(mTask.getTrueLabelIndexPath())));
            String line;
            while ((line = reader.readLine()) != null) {
                mLabelIndexList.add(Integer.parseInt(line));
            }
            reader.close();
        }

        @Override
        protected Object doInBackground(Object... objects) {
            TFLiteClassifier classifier;

            try {
                if (mTask.getModelMode() == Model.Mode.FLOAT32
                        || mTask.getModelMode() == Model.Mode.FLOAT16){
                    classifier = new FloatTFLiteClassifier(mTask);
                } else if (mTask.getModelMode() == Model.Mode.QUANTIZED) {
                    classifier = new QuantTFLiteClassifier(mTask);
                } else {
                    Log.w(TAG, "doInBackground: Wrong task model!");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            int count = 0;
            int top1count = 0;
            int top5count = 0;
            int dataAmount = mTask.getDataPathList().size();
            long now = SystemClock.uptimeMillis();
            while(now - nStartTime < nSeconds * 1000 && count < dataAmount){
                Bitmap bitmap = null;
                try {
                    InputStream in = mContext.getAssets().open(mTask.getDataPathList().get(count));
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

    private class TFLitePerformanceTask extends Task {
        private static final String TAG = "TFLitePerformanceTask";
        Mission mTask;
        ArrayList<ByteBuffer> mDataArray;

        TFLitePerformanceTask(Mission task, ArrayList<ByteBuffer> dataArray,
                              IProgressListener progressListener, int seconds) throws IOException {
            super(progressListener, seconds);
            mTask = task;
            mDataArray = dataArray;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            TFLiteClassifier classifier;

            try {
                if (mTask.getModelMode() == Model.Mode.FLOAT32
                        || mTask.getModelMode() == Model.Mode.FLOAT16){
                    classifier = new FloatTFLiteClassifier(mTask);
                } else if (mTask.getModelMode() == Model.Mode.QUANTIZED) {
                    classifier = new QuantTFLiteClassifier(mTask);
                } else {
                    mProgressListener.onError("doInBackground: Wrong task model!");
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
}
