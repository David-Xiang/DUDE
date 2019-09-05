package com.example.android.clientintelligent.interpreter.mnn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.Recognition;
import com.example.android.clientintelligent.framework.Task;
import com.example.android.clientintelligent.framework.Interpreter;
import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.utils.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import static com.example.android.clientintelligent.framework.interfaces.IInterpreter.Device.VULKAN;

public class MNNInterpreter extends Interpreter {
    private static final String TAG = "MNNInterpreter";

    public MNNInterpreter(Context context) {
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.OPENGL, Device.OPENCL, VULKAN);
    }

    @Override
    public String getFramework() {
        return "MNN";
    }

    @Override
    public AsyncTask buildTask(Mission mission, IProgressListener progressListener)
            throws Exception {
        switch (mission.getPurpose()) {
            case ACCURACY:
                return buildAccuracyTask(mission, progressListener);
            default:
                throw new Exception("Wrong Purpose");
        }
    }

    private AsyncTask buildAccuracyTask(Mission mission, IProgressListener progressListener)
            throws IOException {
        return new MNNAccuracyTask(mission, progressListener, mission.getnTime());
    }

    private class MNNAccuracyTask extends Task {
        private static final String TAG = "MNNAccuracyTask";
        MNNNetInstance mInstance;
        Mission mMission;
        private List<String> mLabels;
        private List<Integer> mLabelIndexList;

        MNNAccuracyTask(Mission mission, IProgressListener progressListener,
                        int seconds) throws IOException {
            super(progressListener, seconds);
            mMission = mission;
            loadLabelList();
            loadLabelIndexList();
        }

        /** Reads label list from Assets. */
        private void loadLabelList() throws IOException {
            mLabels = new ArrayList<>();
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    mContext.getAssets().open(mMission.getLabelFilePath())));
            String line;
            while ((line = reader.readLine()) != null) {
                mLabels.add(line);
            }
            reader.close();
        }

        private void loadLabelIndexList() throws IOException {
            mLabelIndexList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            mContext.getAssets().open(mMission.getTrueLabelIndexPath())));
            String line;
            while ((line = reader.readLine()) != null) {
                mLabelIndexList.add(Integer.parseInt(line));
            }
            reader.close();
        }

        @Override
        protected Object doInBackground(Object... objects) {
            // create net mInstance
            String modelFilePath = mMission.getModelFilePath();
            String cacheModelPath = mContext.getCacheDir() +
                    modelFilePath.substring(modelFilePath.lastIndexOf("/")+1);
            try {
                FileUtil.copyAssetResource2File(mContext, modelFilePath, cacheModelPath);
                mInstance = MNNNetInstance.createFromFile(cacheModelPath);
            }  catch (IOException e) {
                e.printStackTrace();
                mProgressListener.onError("Error in loading model file");
                return null;
            }

            // create session with config
            MNNNetInstance.Config config = new MNNNetInstance.Config();
            switch(mMission.getDevice()){
                case CPU: config.forwardType = MNNForwardType.FORWARD_CPU.type; break;
                case OPENCL: config.forwardType = MNNForwardType.FORWARD_OPENCL.type; break;
                case OPENGL: config.forwardType = MNNForwardType.FORWARD_OPENGL.type; break;
                case VULKAN: config.forwardType = MNNForwardType.FORWARD_VULKAN.type; break;
                default: mProgressListener.onError("buildTask: Wrong device type!");return null;
            }
            config.numThread = mMission.getnThreads();
            MNNNetInstance.Session mSession = mInstance.createSession(config);
            MNNNetInstance.Session.Tensor inputTensor = mSession.getInput(null);


            final MNNImageProcess.Config imgConfig = new MNNImageProcess.Config();
            // normalization params
            imgConfig.mean = new float[]{103.94f, 116.78f, 123.68f};
            imgConfig.normal = new float[]{0.017f, 0.017f, 0.017f};
            // input data format
            imgConfig.dest = MNNImageProcess.Format.BGR;
            Matrix matrix = new Matrix();
            matrix.invert(matrix); // TODO

            int count = 0;
            int top1count = 0;
            int top5count = 0;
            int dataAmount = mMission.getDataPathList().size();
            long now = SystemClock.uptimeMillis();
            while(now - nStartTime < nSeconds * 1000 && count < dataAmount){
                Bitmap bitmap = null;
                try {
                    InputStream in = mContext
                                        .getAssets()
                                        .open(mMission.getDataPathList().get(count));
                    bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    mProgressListener.onError("Error in read data!");
                    return count;
                }
                MNNImageProcess.convertBitmap(bitmap, inputTensor, imgConfig, matrix);

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

                if (mLabelIndexList.get(count) == recognitions.get(0).getId()){
                    top1count++;
                }
                int finalCount = count;
                if (recognitions
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
            mInstance.release();
            return count;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
        }
    }
}
