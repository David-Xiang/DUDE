package com.example.android.clientintelligent.interpreter.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.example.android.clientintelligent.InferenceTask;
import com.example.android.clientintelligent.IntelligentInterpreter;
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

        TFLiteClassifier classifier = new TFLiteClassifier(task);

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
                    addPixelValue(imgData, val);
                }
            }
            images.add(imgData);
        }
        return new TFLiteInferenceTask(classifier, images, progressListener, task.getnTime());
    }

    private void addPixelValue(ByteBuffer imgData, int pixelValue) {
        // for mnist currently
        imgData.putFloat((pixelValue & 0xFF) / 255.f);
    }

    private class TFLiteInferenceTask extends InferenceTask {
        TFLiteClassifier mClassifier;
        ArrayList<ByteBuffer> mDataArray;

        TFLiteInferenceTask(TFLiteClassifier classifier, ArrayList<ByteBuffer> dataArray, ProgressListener progressListener, int seconds){
            super(progressListener, seconds);
            mClassifier = classifier;
            mDataArray = dataArray;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            int count = 0;
            int nImages = mDataArray.size();
            long now = SystemClock.uptimeMillis();
            while(now - nStartTime < nSeconds * 1000){
                mClassifier.runInference(mDataArray.get(count%nImages));
                if (count % 5000 == 0){
                    now = SystemClock.uptimeMillis();
                    publishProgress((int) ((now - nStartTime) / (nSeconds * 10)));
                }
                count++;
            }
            return count;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            mClassifier.close();
        }
    }
}
