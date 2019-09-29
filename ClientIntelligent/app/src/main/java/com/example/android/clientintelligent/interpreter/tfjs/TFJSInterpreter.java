package com.example.android.clientintelligent.interpreter.tfjs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.Recognition;
import com.example.android.clientintelligent.framework.SyncInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public final class TFJSInterpreter extends SyncInterpreter {
    private static final String TAG = "TFJSInterpreter";
    private List<String> mLabels;
    private List<Integer> mLabelIndexList;
    private Boolean useGPU = false;
    private WebView mWebView;
    private Context mContext;
    private Mission mMission;
    private IProgressListener mProgressListener;
    private long nStartTime;
    private int nSeconds;
    private int nLoopCount;
    private AccuracyResult mAccuracyResult;

    public TFJSInterpreter(Context context) {
        super(context);
    }

    @Override
    public void executeMission(Mission mission, IProgressListener progressListener) throws Exception {
        switch (mission.getPurpose()) {
            case ACCURACY:
                executeAccuracyMission(mission, progressListener); break;
//            case PERFORMANCE:
//                return executePerformanceMission(mission, progressListener); break;
            default:
                throw new Exception("Wrong Purpose");
        }
    }

    private void executeAccuracyMission(Mission mission, IProgressListener progressListener) throws IOException {
        mContext = mission.getActivity();
        mMission = mission;
        mProgressListener = progressListener;

        initWebView();

        try {
            loadLabelList(getMission().getLabelFilePath());
            loadLabelIndexList(getMission().getTrueLabelIndexPath());
        } catch (IOException e) {
            e.printStackTrace();
            mProgressListener.onError("Error in loading files!");
        }
    }

    @Override
    protected void loadModelFile(String path) throws IOException {
        ((Activity) mContext).runOnUiThread(()->{
            mWebView.loadUrl(String.format("javascript:loadModelFile(\"%s\")", getMission().getModelFilePath()));
        });
    }

    @JavascriptInterface
    public void onWindowLoaded() throws IOException {

        loadModelFile(getMission().getModelFilePath());
    }

    @JavascriptInterface
    public void onModelLoaded() {
        configSession(getMission().getDevice());
    }

    @Override
    protected void configSession(Device device) {
        String backend = "cpu";
        if (device == IInterpreter.Device.WEBGL) {
            useGPU = true;
            backend = "webgl";
        }
        String finalBackend = backend;
        ((Activity) mContext).runOnUiThread(()->{
            mWebView.loadUrl(String.format("javascript:setBackend(\"%s\")", finalBackend));
        });
    }

    @JavascriptInterface
    public void onBackendRegistered() {
        nStartTime = SystemClock.uptimeMillis();
        nSeconds = getMission().getnTime();
        mAccuracyResult = new AccuracyResult();
        nLoopCount = 0;
        doInferenceLoop();
    }

    private void doInferenceLoop() {
        int dataAmount = getMission().getDataPathList().size();
        long now = SystemClock.uptimeMillis();
        if (now - nStartTime < nSeconds * 1000 && nLoopCount < dataAmount){
            String dataPath = getMission().getDataPathList().get(nLoopCount);
            recognizeImage(dataPath);
        } else {
            publishResults(mAccuracyResult);
            releaseResources();
        }
    }

    @SuppressLint("DefaultLocale")
    private void recognizeImage(String picPath) {
        ((Activity) mContext).runOnUiThread(()->{
            mWebView.loadUrl(
                    String.format("javascript:recognizeImage(\"%s\",%d,%d)",
                    picPath, getMission().getnImageSizeX(), getMission().getnImageSizeX()));
        });
    }

    @JavascriptInterface
    public void onInferenceFinished(float [] result) {
        List<Recognition> recognitions = result2recognitions(result);
        processRecognitions(nLoopCount, recognitions, mAccuracyResult);
        nLoopCount = nLoopCount + 1;
        doInferenceLoop();
    }

    @SuppressLint("JavascriptInterface")
    private void initWebView(){
        mWebView = new WebView(mContext);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        // todo delete this
        mWebView.setWebContentsDebuggingEnabled(true);
        mWebView.addJavascriptInterface(this, "Android");

        // intercept data & model requests
        mWebView.setWebViewClient(new WebViewClient(){
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString().toLowerCase();
                Log.i(TAG, "shouldInterceptRequest: Url = " + url);
                try {
                    if (url.contains("model.json")) {
                        return new WebResourceResponse("application/json", "utf-8",
                                mContext.getAssets().open(uri.getPath().substring(1)));
                    } else if (url.contains(".bin")) {
                        return new WebResourceResponse("application/octet-stream", "utf-8",
                                mContext.getAssets().open(uri.getPath().substring(1)));
                    } else if (url.contains(".jpeg")) {
                        return new WebResourceResponse("image/jpeg", "utf-8",
                                mContext.getAssets().open(uri.getPath().substring(1)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        mWebView.loadUrl("file:///android_asset/tfjs/index.html");
    }

    @Override
    protected void loadLabelList(String path) throws IOException {
        mLabels = new ArrayList<>();
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                mContext.getAssets().open(getMission().getLabelFilePath())));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabels.add(line);
        }
        reader.close();
    }

    @Override
    protected void loadLabelIndexList(String path) throws IOException {
        mLabelIndexList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        mContext.getAssets().open(getMission().getTrueLabelIndexPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabelIndexList.add(Integer.parseInt(line));
        }
        reader.close();
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
            mProgressListener.onProgress((int) ((now - nStartTime) / (nSeconds * 10)), msg);
        }
    }

    @Override
    protected void publishResults(AccuracyResult result) {
        long now = SystemClock.uptimeMillis();
        @SuppressLint("DefaultLocale")
        String msg = String.format("Top 1 accuracy is %.2f%%, top 5 accuracy is %.2f%%",
                (float)(result.top1count) * 100 / result.total,
                (float)(result.top5count) * 100 / result.total);
        mProgressListener.onFinish(nLoopCount, now - nStartTime);
    }

    @Override
    protected void releaseResources() {

    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.GPU);
    }

    @Override
    public String getFramework() {
        return "TensorFlow.js";
    }


    private Mission getMission() {
        return mMission;
    }

    private IProgressListener getProgressListener() {
        return mProgressListener;
    }

    private List<Recognition> result2recognitions(float [] result) {
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
}
