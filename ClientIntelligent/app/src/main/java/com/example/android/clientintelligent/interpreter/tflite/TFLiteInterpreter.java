package com.example.android.clientintelligent.interpreter.tflite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.InferenceTask;
import com.example.android.clientintelligent.IntelligentInterpreter;
import com.example.android.clientintelligent.IntelligentModel;
import com.example.android.clientintelligent.IntelligentRecognition;
import com.example.android.clientintelligent.IntelligentTask;
import com.example.android.clientintelligent.interfaces.ProgressListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFLiteInterpreter extends IntelligentInterpreter {
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
    public AsyncTask buildTask(IntelligentTask task, ProgressListener progressListener) throws IOException {
        switch (task.getPurpose()) {
            case PERFORMANCE:
                return buildPerformanceTask(task, progressListener);
            case ACCURACY:
                return buildAccuracyTask(task, progressListener);
            default:
                progressListener.onError("Wrong Purpose!"); return null;
        }
    }

    private AsyncTask buildAccuracyTask(IntelligentTask task, ProgressListener progressListener) throws IOException {
        return new TFLiteAccuracyTask(task, progressListener, task.getnTime());
    }

    private AsyncTask buildPerformanceTask(IntelligentTask task, ProgressListener progressListener) throws IOException {
        int[] intValues = new int[task.getnImageSizeX() * task.getnImageSizeY()];
        ArrayList<ByteBuffer> images = new ArrayList<>();

        // convert data
        for (int s = 0; s < task.getDataPathList().size(); s++){
            InputStream in = mContext.getAssets().open(task.getDataPathList().get(s));
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            ByteBuffer imgData = ByteBuffer.allocateDirect(
                    task.getnImageSizeX() * task.getnImageSizeY() * task.getChannelsPerPixel() * task.getBytesPerChannel());
            imgData.order(ByteOrder.nativeOrder());
            imgData.rewind();
            bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
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

    private void addPixelValue(IntelligentModel.Mode mode, int channels, ByteBuffer imgData, int pixelValue) {
        if ((mode == IntelligentModel.Mode.FLOAT32 || mode == IntelligentModel.Mode.FLOAT16) && channels == 3) {
            imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        } else if (mode == IntelligentModel.Mode.FLOAT32 && channels == 1){
            imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        } else if (mode == IntelligentModel.Mode.QUANTIZED) {
            imgData.put((byte) ((pixelValue >> 16) & 0xFF));
            imgData.put((byte) ((pixelValue >> 8) & 0xFF));
            imgData.put((byte) (pixelValue & 0xFF));
        } else {
            Log.w(TAG, "addPixelValue: Wrong model mode!");
        }
    }

    private class TFLiteAccuracyTask extends InferenceTask {
        private static final String TAG = "TFLitePerformanceTask";
        IntelligentTask mTask;
        List<Integer> mLabelIndexList;

        TFLiteAccuracyTask(IntelligentTask task, ProgressListener progressListener, int seconds) throws IOException {
            super(progressListener, seconds);
            mTask = task;
            mLabelIndexList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open(mTask.getTrueLabelIndexPath())));
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
                if (mTask.getModelMode() == IntelligentModel.Mode.FLOAT32 || mTask.getModelMode() == IntelligentModel.Mode.FLOAT16){
                    classifier = new FloatTFLiteClassifier(mTask);
                } else if (mTask.getModelMode() == IntelligentModel.Mode.QUANTIZED) {
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
                List<IntelligentRecognition> recognitionList = classifier.recognizeImage(bitmap);
                if (mLabelIndexList.get(count) == recognitionList.get(0).getId()){
                    top1count++;
                }
                int finalCount = count;
                if (recognitionList
                        .stream()
                        .map(IntelligentRecognition::getId)
                        .anyMatch(n-> n.equals(mLabelIndexList.get(finalCount)))) {
                    top5count++;
                }
                Log.i(TAG, String.format("doInBackground: count = %d, top1count = %d, top5count = %d", count, top1count, top5count));
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
            classifier.close();
            return count;
        }
    }
    private class TFLitePerformanceTask extends InferenceTask {
        private static final String TAG = "TFLitePerformanceTask";
        IntelligentTask mTask;
        ArrayList<ByteBuffer> mDataArray;

        TFLitePerformanceTask(IntelligentTask task, ArrayList<ByteBuffer> dataArray, ProgressListener progressListener, int seconds) throws IOException {
            super(progressListener, seconds);
            mTask = task;
            mDataArray = dataArray;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            TFLiteClassifier classifier;

            try {
                if (mTask.getModelMode() == IntelligentModel.Mode.FLOAT32 || mTask.getModelMode() == IntelligentModel.Mode.FLOAT16){
                    classifier = new FloatTFLiteClassifier(mTask);
                } else if (mTask.getModelMode() == IntelligentModel.Mode.QUANTIZED) {
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
            int nImages = mDataArray.size();
            long now = SystemClock.uptimeMillis();
            while(now - nStartTime < nSeconds * 1000){
                classifier.runInference(mDataArray.get(count%nImages));
                Log.i(TAG, "doInBackground: count = " + count);
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
