package com.example.android.clientintelligent.interpreter.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.InferenceTask;
import com.example.android.clientintelligent.IntelligentInterpreter;
import com.example.android.clientintelligent.IntelligentModel;
import com.example.android.clientintelligent.IntelligentTask;
import com.example.android.clientintelligent.interfaces.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
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
        return new TFLiteInferenceTask(task, images, progressListener, task.getnTime());
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

    private class TFLiteInferenceTask extends InferenceTask {
        private static final String TAG = "TFLiteInferenceTask";
        IntelligentTask mTask;
        ArrayList<ByteBuffer> mDataArray;

        TFLiteInferenceTask(IntelligentTask task, ArrayList<ByteBuffer> dataArray, ProgressListener progressListener, int seconds) throws IOException {
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
