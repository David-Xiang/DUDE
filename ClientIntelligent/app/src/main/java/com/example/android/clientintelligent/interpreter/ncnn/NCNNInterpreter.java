package com.example.android.clientintelligent.interpreter.ncnn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.AccuracyTask;
import com.example.android.clientintelligent.framework.AsyncInterpreter;
import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.Recognition;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class NCNNInterpreter extends AsyncInterpreter {
    private static final String TAG = "NCNNInterpreter";

    public NCNNInterpreter(Context context) {
        super(context);
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU);//, Device.VULKAN);
    }

    @Override
    public String getFramework() {
        return "NCNN";
    }

    @Override
    public AsyncTask buildTask(Mission mission, IProgressListener progressListener) throws Exception {
        return new NCNNAccuracyTask(mission, progressListener, mission.getnTime());
    }

    private final class NCNNAccuracyTask extends AccuracyTask {
        private static final String TAG = "NCNNAccuracyTask";
        private List<String> mLabels;
        private List<Integer> mLabelIndexList;
        private Boolean useGPU = false;
        private int nThread = 1;

        protected NCNNAccuracyTask(Mission mission, IProgressListener progressListener, int seconds)
                throws IOException {
            super(mission, progressListener, seconds);
            loadLabelList(getMission().getLabelFilePath());
            loadLabelIndexList(getMission().getTrueLabelIndexPath());
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
        protected void loadModelFile(String path) throws IOException {
            // todo parse path
            byte[] param = null;
            byte[] bin = null;
            String paramPath = path.split("\\$")[0];
            String binPath = path.split("\\$")[1];

            InputStream assetsInputStream = mContext.getAssets().open(paramPath);
            int available = assetsInputStream.available();
            param = new byte[available];
            int byteCode = assetsInputStream.read(param);
            assetsInputStream.close();

            assetsInputStream = mContext.getAssets().open(binPath);
            available = assetsInputStream.available();
            bin = new byte[available];
            byteCode = assetsInputStream.read(bin);
            assetsInputStream.close();

            NCNNNative.InitModel(param, bin);
        }

        @Override
        protected void configSession(Device device, int nThreads) {
            if (device == Device.VULKAN) {
                useGPU = true;
            }
            this.nThread = nThreads;
        }

        protected Bitmap loadValidImage(int index) throws IOException {
            InputStream in = mContext
                    .getAssets()
                    .open(getMission().getDataPathList().get(index));
            return BitmapFactory.decodeStream(in);
        }

        protected List<Recognition> recognizeImage(Bitmap bitmap) {
            float[] result = NCNNNative.Detect(bitmap, useGPU, nThread);

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
            NCNNNative.Release();
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                loadLabelList(getMission().getLabelFilePath());
                loadLabelIndexList(getMission().getTrueLabelIndexPath());
                loadModelFile(getMission().getModelFilePath());
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
            Bitmap bitmap = null;

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
    }
}
