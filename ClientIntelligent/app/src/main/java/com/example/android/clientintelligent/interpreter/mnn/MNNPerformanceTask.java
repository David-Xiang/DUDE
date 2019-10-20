package com.example.android.clientintelligent.interpreter.mnn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.PerformanceTask;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.util.FileUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class MNNPerformanceTask extends PerformanceTask {
    private static final String TAG = "MNNPerformanceTask";
    private MNNNetInstance mInstance;
    private MNNNetInstance.Session mSession;
    private MNNNetInstance.Session.Tensor mInputTensor;
    private MNNImageProcess.Config mImgConfig;
    private Matrix mMatrix;
    private Context mContext;
    private List<Bitmap> mBitmapList;

    MNNPerformanceTask(Mission mission, IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
        mContext = mission.getContext();
    }

    @Override
    protected void loadModelFile(String path) throws IOException {
        // create net mInstance
        String modelFilePath = getMission().getModelFilePath();
        String cacheModelPath = String.format("%s/%s",
                mContext.getCacheDir(),
                modelFilePath.substring(modelFilePath.lastIndexOf("/")+1));
        Log.i(TAG, "loadModelFile(): cacheModelPath = " + cacheModelPath);
        FileUtil.copyExternalResource2File(modelFilePath, cacheModelPath);
        mInstance = MNNNetInstance.createFromFile(cacheModelPath);
    }

    @Override
    protected void configSession(IInterpreter.Device device, int nThreads) {
        // create session with config
        MNNNetInstance.Config config = new MNNNetInstance.Config();
        switch(device){
            case CPU: config.forwardType = MNNForwardType.FORWARD_CPU.type; break;
            case OPENCL: config.forwardType = MNNForwardType.FORWARD_OPENCL.type; break;
            case OPENGL: config.forwardType = MNNForwardType.FORWARD_OPENGL.type; break;
            case VULKAN: config.forwardType = MNNForwardType.FORWARD_VULKAN.type; break;
            default: mProgressListener.onError("buildTask: Wrong device type!");return;
        }
        config.numThread = nThreads;
        mSession = mInstance.createSession(config);
        mInputTensor = mSession.getInput(null);


        mImgConfig = new MNNImageProcess.Config();
        // normalization params
        mImgConfig.mean = new float[]{103.94f, 116.78f, 123.68f};
        mImgConfig.normal = new float[]{0.017f, 0.017f, 0.017f};
        // input data format
        mImgConfig.dest = MNNImageProcess.Format.BGR;
        mMatrix = new Matrix();
        mMatrix.invert(mMatrix);
    }

    @Override
    protected Bitmap loadValidImage(int index) {
        return mBitmapList.get(index % mBitmapList.size());
    }

    @Override
    protected void releaseResources() {
        mSession = null;
        mInstance.release();
        mContext = null;
    }

    private Bitmap readImage(String path) {
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(mContext.getAssets().open(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            loadModelFile(getMission().getModelFilePath());
            List<String> dataPathList = getMission().getDataPathList();
            mBitmapList = dataPathList.stream()
                    .map(this::readImage)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            mProgressListener.onError("Error in loading files!");
            return 0;
        }

        configSession(getMission().getDevice(), getMission().getnThreads());

        int count = 0;
        long now = SystemClock.uptimeMillis();
        Bitmap bitmap;

        while(now - nStartTime < nSeconds * 1000) {
            bitmap = loadValidImage(count);

            MNNImageProcess.convertBitmap(bitmap, mInputTensor, mImgConfig, mMatrix);
            mSession.run();
            mSession.getOutput(null);

            if (count % 50 == 0){
                now = SystemClock.uptimeMillis();
                publishProgress((int) ((now - nStartTime) / (nSeconds * 10)));
            }
            count++;
        }

        releaseResources();
        return count;
    }
}