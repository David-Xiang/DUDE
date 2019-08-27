package com.example.android.clientintelligent.interpreter.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.example.android.clientintelligent.interfaces.IntelligentInterpreter;
import com.example.android.clientintelligent.IntelligentTask;
import com.example.android.clientintelligent.interfaces.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFLiteInterpreter implements IntelligentInterpreter {
    private Context mContext;
    public TFLiteInterpreter(Context context){
        mContext = context;
    }

    @Override
    public List<String> getDevices() {
        return Arrays.asList("CPU", "GPU", "NNAPI");
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
            ByteBuffer imgData = ByteBuffer.allocateDirect(
                    task.getnImageSizeX() * task.getnImageSizeY() * task.getnChannelsPerPixel() * task.getnBytesPerChannel());
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
        return new InferenceTask(classifier, images, progressListener, task.getnTime());
    }

    private void addPixelValue(ByteBuffer imgData, int pixelValue) {
        // for mnist currently
        imgData.putFloat((pixelValue & 0xFF) / 255.f);
    }

    private class InferenceTask extends AsyncTask<Object, Integer, Object> {
        ProgressListener mProgressListener;
        TFLiteClassifier mClassifier;
        ArrayList<ByteBuffer> mDataArray;
        long nStartTime;
        int nSeconds;


        InferenceTask(TFLiteClassifier classifier, ArrayList<ByteBuffer> dataArray, ProgressListener progressListener, int seconds){
            mProgressListener = progressListener;
            mClassifier = classifier;
            mDataArray = dataArray;
            nSeconds = seconds;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            int count = 0;
            int nImages = mDataArray.size();
            long now = SystemClock.uptimeMillis();
            while(now - nStartTime < nSeconds * 1000){
                mClassifier.runInference(mDataArray.get(count%nImages));
                if (count % 5000 == 0){ publishProgress((int) ((now - nStartTime) / (nSeconds * 10))); }
                count++;
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            nStartTime = SystemClock.uptimeMillis();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressListener.onProgress(progress[0]);
        }


        @Override
        protected void onPostExecute(Object result) {
            mClassifier.close();
            long enduredTime = SystemClock.uptimeMillis() - nStartTime;
            mProgressListener.onFinish(enduredTime);
        }
    }
}
