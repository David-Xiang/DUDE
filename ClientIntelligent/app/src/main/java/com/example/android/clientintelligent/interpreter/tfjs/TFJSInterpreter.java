//package com.example.android.clientintelligent.interpreter.tfjs;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.AsyncTask;
//import android.os.SystemClock;
//import android.util.Log;
//import android.webkit.WebView;
//
//import com.example.android.clientintelligent.framework.AccuracyTask;
//import com.example.android.clientintelligent.framework.Interpreter;
//import com.example.android.clientintelligent.framework.Mission;
//import com.example.android.clientintelligent.framework.Recognition;
//import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.PriorityQueue;
//
//public class TFJSInterpreter extends Interpreter {
//    private static final String TAG = "TFJSInterpreter";
//
//    public TFJSInterpreter(Context context) {
//        super(context);
//    }
//
//    @Override
//    public List<Device> getDevices() {
//        return Arrays.asList(Device.CPU, Device.GPU);
//    }
//
//    @Override
//    public String getFramework() {
//        return "TensorFlow.js";
//    }
//
//    @Override
//    public AsyncTask buildTask(Mission mission, IProgressListener progressListener) throws Exception {
//        return new TFJSAccuracyTask(mission, progressListener, mission.getnTime());
//    }
//
//    private final class TFJSAccuracyTask extends AccuracyTask {
//        private static final String TAG = "TFJSAccuracyTask";
//        private List<String> mLabels;
//        private List<Integer> mLabelIndexList;
//        private Boolean useGPU = false;
//        private int nThread = 1;
//        private WebView mWebView;
//
//        protected TFJSAccuracyTask(Mission mission, IProgressListener progressListener, int seconds)
//                throws IOException {
//            super(mission, progressListener, seconds);
//            mWebView = new WebView(getMission().getActivity());
//            loadLabelList(getMission().getLabelFilePath());
//            loadLabelIndexList(getMission().getTrueLabelIndexPath());
//        }
//
//        @Override
//        protected void loadLabelList(String labelFilePath) throws IOException {
//            mLabels = new ArrayList<>();
//            BufferedReader reader =
//                    new BufferedReader(
//                            new InputStreamReader(
//                                    mContext.getAssets().open(labelFilePath)));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                mLabels.add(line);
//            }
//            reader.close();
//        }
//
//        @Override
//        protected void loadLabelIndexList(String trueLabelIndexPath) throws IOException {
//            mLabelIndexList = new ArrayList<>();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(
//                            mContext.getAssets().open(trueLabelIndexPath)));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                mLabelIndexList.add(Integer.parseInt(line));
//            }
//            reader.close();
//        }
//
//        @Override
//        protected void loadModelFile(String path) throws IOException {
//            // todo
//            mWebView.loadUrl(String.format("javascript:loadModelFile(\"%s\")", getMission().getModelFilePath()));
//        }
//
//        @Override
//        protected void configSession(Device device, int nThreads) {
//            if (device == Device.VULKAN) {
//                useGPU = true;
//            }
//            this.nThread = nThreads;
//            // todo
//        }
//
//        protected List<Recognition> recognizeImage(String dataPath) {
//            // todo
//            float[] result = NCNNNative.Detect(bitmap, useGPU, nThread);
//
//            return result2recognitions(result);
//        }
//
//        private List<Recognition> result2recognitions(float [] result) {
//            // 显示结果
//            PriorityQueue<Recognition> pq =
//                    new PriorityQueue<>(
//                            5,
//                            (lhs, rhs) -> {
//                                // Intentionally reversed to put high confidence at the head of the queue.
//                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
//                            });
//            for (int i = 0; i < mLabels.size(); ++i) {
//                pq.add(new Recognition(i+1, mLabels.get(i), result[i], null));
//            }
//            final ArrayList<Recognition> recognitions = new ArrayList<>();
//            int recognitionsSize = Math.min(pq.size(), 5);
//            for (int i = 0; i < recognitionsSize; ++i) {
//                recognitions.add(pq.poll());
//            }
//            return recognitions;
//        }
//
//        @Override
//        protected void processRecognitions(int index, List<Recognition> recognitions, AccuracyResult result) {
//            result.total++;
//
//            if (mLabelIndexList.get(index) == recognitions.get(0).getId()){
//                result.top1count++;
//            }
//            if (recognitions
//                    .stream()
//                    .map(Recognition::getId)
//                    .anyMatch(n-> n.equals(mLabelIndexList.get(index)))) {
//                result.top5count++;
//            }
//
//            Log.i(TAG, String.format("doInBackground: count = %d, top1count = %d, top5count = %d",
//                    index, result.top1count, result.top5count));
//
//
//            if (index > 0 && index % 50 == 0){
//                long now = SystemClock.uptimeMillis();
//                @SuppressLint("DefaultLocale")
//                String msg = String.format("Top 1 accuracy is %f%%, top 5 accuracy is %f%%",
//                        (float)(result.top1count) * 100 / result.total,
//                        (float)(result.top5count) * 100 / result.total);
//                publishProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
//            }
//        }
//
//        @Override
//        protected void publishResults(AccuracyResult result) {
//            long now = SystemClock.uptimeMillis();
//            @SuppressLint("DefaultLocale")
//            String msg = String.format("Top 1 accuracy is %.2f%%, top 5 accuracy is %.2f%%",
//                    (float)(result.top1count) * 100 / result.total,
//                    (float)(result.top5count) * 100 / result.total);
//            publishProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
//        }
//
//        @Override
//        protected void releaseResources() {
//            // todo
//            NCNNNative.Release();
//        }
//
//        @Override
//        protected Object doInBackground(Object... objects) {
//            try {
//                loadLabelList(getMission().getLabelFilePath());
//                loadLabelIndexList(getMission().getTrueLabelIndexPath());
//                loadModelFile(getMission().getModelFilePath());
//            } catch (IOException e) {
//                e.printStackTrace();
//                mProgressListener.onError("Error in loading files!");
//                return 0;
//            }
//
//            configSession(getMission().getDevice(), getMission().getnThreads());
//
//            int count = 0;
//            int dataAmount = getMission().getDataPathList().size();
//            long now = SystemClock.uptimeMillis();
//            AccuracyResult result = new AccuracyResult();
//            Bitmap bitmap = null;
//
//            while(now - nStartTime < nSeconds * 1000 && count < dataAmount){
//
//                String dataPath = getMission().getDataPathList().get(count);
//
//                List<Recognition> recognitions = recognizeImage(dataPath);
//
//                processRecognitions(count, recognitions, result);
//
//                count++;
//
//                now = SystemClock.uptimeMillis();
//            }
//
//            publishResults(result);
//
//            releaseResources();
//            return count;
//        }
//    }
//    }
//}
