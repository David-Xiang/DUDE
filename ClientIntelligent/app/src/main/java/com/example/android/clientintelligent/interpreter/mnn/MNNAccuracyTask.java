package com.example.android.clientintelligent.interpreter.mnn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.AccuracyTask;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.framework.pojo.Recognition;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class MNNAccuracyTask extends AccuracyTask {
    private static final String TAG = "MNNAccuracyTask";
    private MNNNetInstance mInstance;
    private MNNNetInstance.Session mSession;
    private MNNNetInstance.Session.Tensor mInputTensor;
    private MNNImageProcess.Config mImgConfig;
    private Matrix mMatrix;
    private List<String> mLabels;
    private List<Integer> mLabelIndexList;
    private Context mContext;

    MNNAccuracyTask(Mission mission, IProgressListener progressListener,
                    int seconds) {
        super(mission, progressListener, seconds);
        mContext = mission.getContext();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            loadLabelList(getMission().getLabelFilePath());
            loadLabelIndexList(getMission().getTrueLabelIndexPath());
            loadModelFile(getMission().getModels().get(0));
        } catch (IOException e) {
            e.printStackTrace();
            mProgressListener.onError("Error in loading files!");
            return 0;
        }

        configSession(getMission().getDevice(), getMission().getnThreads());

        int count = 0;
        int dataAmount = getMission().getDataPathList().size();
        long now = SystemClock.uptimeMillis();
        AccuracyResult result = new AccuracyResult();
        Bitmap bitmap;

        while(now - nStartTime < nSeconds * 1000 && count < dataAmount){

            try {
                bitmap = loadValidImage(count);
            } catch (IOException e) {
                e.printStackTrace();
                mProgressListener.onError("Error in read data!");
                return count;
            }

            List<Recognition> recognitions = recognizeImage(bitmap);

            processRecognitions(count, recognitions, result);

            count++;

            now = SystemClock.uptimeMillis();
        }

        publishResults(result);

        releaseResources();
        return count;
    }

    @Override
    protected void loadLabelList(String labelFilePath) throws IOException {
        mLabels = new ArrayList<>();
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                mContext.getAssets().open(labelFilePath)));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabels.add(line);
        }
        reader.close();
    }

    @Override
    protected void loadLabelIndexList(String trueLabelIndexPath) throws IOException {
        mLabelIndexList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        mContext.getAssets().open(trueLabelIndexPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabelIndexList.add(Integer.parseInt(line));
        }
        reader.close();
    }

    @Override
    protected void loadModelFile(Model model) throws IOException {
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

    private Bitmap loadValidImage(int index) throws IOException {
        InputStream in = mContext
                .getAssets()
                .open(getMission().getDataPathList().get(index));
        return BitmapFactory.decodeStream(in);
    }

    private List<Recognition> recognizeImage(Bitmap bitmap) {
        MNNImageProcess.convertBitmap(bitmap, mInputTensor, mImgConfig, mMatrix);

        mSession.run();

        MNNNetInstance.Session.Tensor output = mSession.getOutput(null);
        float[] result = output.getFloatData();// get float results


        // 显示结果
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        5,
                        (lhs, rhs) -> {
                            // Intentionally reversed to put high confidence at the head of the queue.
                            return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                        });
        for (int i = 0; i < mLabels.size(); ++i) {
            pq.add(new Recognition(i+1, mLabels.get(i), result[i], null));
        }
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), 5);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    @Override
    protected void processRecognitions(int index, List<Recognition> recognitions, AccuracyResult result) {
        result.total++;

        if (mLabelIndexList.get(index) == recognitions.get(0).getId()){
            result.top1count++;
        }
        if (recognitions
                .stream()
                .map(Recognition::getId)
                .anyMatch(n-> n.equals(mLabelIndexList.get(index)))) {
            result.top5count++;
        }

        Log.i(TAG, String.format("doInBackground: count = %d, top1count = %d, top5count = %d",
                index, result.top1count, result.top5count));


        if (index > 0 && index % 50 == 0){
            long now = SystemClock.uptimeMillis();
            @SuppressLint("DefaultLocale")
            String msg = String.format("Top 1 accuracy is %f%%, top 5 accuracy is %f%%",
                    (float)(result.top1count) * 100 / result.total,
                    (float)(result.top5count) * 100 / result.total);
            publishProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
        }
    }

    @Override
    protected void publishResults(AccuracyResult result) {
        long now = SystemClock.uptimeMillis();
        @SuppressLint("DefaultLocale")
        String msg = String.format("Top 1 accuracy is %.2f%%, top 5 accuracy is %.2f%%",
                (float)(result.top1count) * 100 / result.total,
                (float)(result.top5count) * 100 / result.total);
        publishProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
    }

    @Override
    protected void releaseResources() {
        mSession = null;
        mInstance.release();
        mContext = null;
    }
}
