package com.example.android.clientintelligent.interpreter.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.clientintelligent.framework.SyncInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFLiteInterpreter extends SyncInterpreter {
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
    public AsyncTask buildTask(Mission mission, IProgressListener progressListener)
            throws Exception {
        switch (mission.getPurpose()) {
            case BENCH_PERFORMANCE:
                return buildBenchmarkPerformanceTask(mission, progressListener);
            case BENCH_ACCURACY:
                return buildBenchmarkAccuracyTask(mission, progressListener);
            case APP_ACCURACY:
            case APP_PERFORMANCE:
            case APP_BANLANCE:
                return buildApplicationTask(mission, progressListener);
            default:
                throw new Exception("Wrong Purpose");
        }
    }

    private AsyncTask buildBenchmarkAccuracyTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        return new TFLiteBenchmarkAccuracyTask(mission, progressListener, mission.getnTime());
    }

    private AsyncTask buildBenchmarkPerformanceTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        int[] intValues = new int[mission.getnImageSizeX() * mission.getnImageSizeY()];
        ArrayList<ByteBuffer> images = new ArrayList<>();

        // convert data
        for (int s = 0; s < Math.min(mission.getDataPathList().size(), 10); s++){
            InputStream in = getContext().getAssets().open(mission.getDataPathList().get(s));
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            ByteBuffer imgData = ByteBuffer.allocateDirect(
                    mission.getnImageSizeX() *
                    mission.getnImageSizeY() *
                    mission.getChannelsPerPixel() *
                    mission.getBytesPerChannel());
            imgData.order(ByteOrder.nativeOrder());
            imgData.rewind();
            bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                    bitmap.getWidth(), bitmap.getHeight());
            // Convert the image to floating point.
            int pixel = 0;
            for (int i = 0; i < mission.getnImageSizeX(); ++i) {
                for (int j = 0; j < mission.getnImageSizeY(); ++j) {
                    final int val = intValues[pixel++];
                    addPixelValue(mission.getModelMode(), mission.getChannelsPerPixel(), imgData, val);
                }
            }
            images.add(imgData);
        }
        return new TFLiteBenchmarkPerformanceTask(mission, images, progressListener, mission.getnTime());
    }

    private AsyncTask buildApplicationTask(Mission mission, IProgressListener progressListener) throws IOException {
        return new TFLiteApplicationTask(mission, progressListener);
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
}
