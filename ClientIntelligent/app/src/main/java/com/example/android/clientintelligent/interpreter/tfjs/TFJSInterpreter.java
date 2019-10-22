package com.example.android.clientintelligent.interpreter.tfjs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Recognition;
import com.example.android.clientintelligent.framework.AsyncInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public final class TFJSInterpreter extends AsyncInterpreter {
    private static final String TAG = "TFJSInterpreter";
    private List<String> mLabels;
    private List<Integer> mLabelIndexList;
    private WebView mWebView;
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
    public void executeMissionAsync(Mission mission, IProgressListener progressListener) throws Exception {
        mMission = mission;
        mProgressListener = progressListener;
        switch (mission.getPurpose()) {
            case BENCH_ACCURACY:
                executeAccuracyMissionSync(); break;
            case BENCH_PERFORMANCE:
                executePerformanceMissionSync(); break;
            default:
                throw new Exception("Wrong Purpose");
        }
    }

    private void executeAccuracyMissionSync() {
        try {
            loadLabelList(getMission().getLabelFilePath());
            loadLabelIndexList(getMission().getTrueLabelIndexPath());
        } catch (IOException e) {
            e.printStackTrace();
            mProgressListener.onError("Error in loading files!");
        }

        initWebView();
    }

    private void executePerformanceMissionSync() {
        initWebView();
    }

    @Override
    protected void loadModelFileAsync(String path) {
        ((Activity) getContext()).runOnUiThread(()->
                mWebView.loadUrl(String.format("javascript:loadModelFile(\"%s\")", getMission().getModelFilePath())));
    }

    @Override
    @JavascriptInterface
    public void onWindowLoaded(){
        loadModelFileAsync(getMission().getModelFilePath());
    }

    @Override
    @JavascriptInterface
    public void onModelLoaded() {
        configSessionAsync(getMission().getDevice());
    }

    @Override
    protected void configSessionAsync(Device device) {
        String backend = "cpu";
        if (device == IInterpreter.Device.WEBGL) {
            backend = "webgl";
        }
        String finalBackend = backend;
        ((Activity) getContext()).runOnUiThread(()->
                mWebView.loadUrl(String.format("javascript:setBackend(\"%s\")", finalBackend)));
    }

    @Override
    @JavascriptInterface
    public void onBackendRegistered() {
        nStartTime = SystemClock.uptimeMillis();
        nSeconds = getMission().getnTime();
        if (getMission().getPurpose().equals(Mission.Purpose.BENCH_ACCURACY)){
            mAccuracyResult = new AccuracyResult();
            nLoopCount = 0;
            doAccuracyTask();
        } else {
            doPerformanceTask();
        }
    }

    private void doAccuracyTask() {
        int dataAmount = getMission().getDataPathList().size();
        long now = SystemClock.uptimeMillis();
        if (now - nStartTime < nSeconds * 1000 && nLoopCount < dataAmount){
            String dataPath = getMission().getDataPathList().get(nLoopCount);
            recognizeImageAsync(dataPath);
        } else {
            publishResults(mAccuracyResult);
            releaseResources();
        }
    }

    @SuppressLint("DefaultLocale")
    private void doPerformanceTask() {
        String dataPath = getMission().getDataPathList().get(0);
        ((Activity) getContext()).runOnUiThread(
                ()-> mWebView.loadUrl(
                    String.format("javascript:performanceTask(\"%s\",%d,%d,%d,%d)",
                                    dataPath,
                                    getMission().getnImageSizeX(),
                                    getMission().getnImageSizeY(),
                                    getMission().getChannelsPerPixel(),
                                    nSeconds)
                )
        );
    }

    @Override
    @SuppressLint("DefaultLocale")
    protected void recognizeImageAsync(String picPath) {
        ((Activity) getContext()).runOnUiThread(
                ()-> mWebView.loadUrl(
                    String.format("javascript:recognizeImage(\"%s\",%d,%d,%d)",
                                    picPath,
                                    getMission().getnImageSizeX(),
                                    getMission().getnImageSizeY(),
                                    getMission().getChannelsPerPixel())
                )
        );
    }

    @Override
    @JavascriptInterface
    public void onAccuracyTaskFinished(float [] result) {
        List<Recognition> recognitions = result2recognitions(result);
        processRecognitions(nLoopCount, recognitions, mAccuracyResult);
        nLoopCount = nLoopCount + 1;
        doAccuracyTask();
    }

    @Override
    @JavascriptInterface
    public void onPerformanceTaskFinished(int count, int elapsedTime) {
        mProgressListener.onFinish(count, elapsedTime);
        releaseResources();
    }

    @Override
    @JavascriptInterface
    public void onProgress(int progress) {
        mProgressListener.onProgress(progress, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(){
        mWebView = new WebView(getContext());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //mWebView.setWebContentsDebuggingEnabled(true);
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
                    // bin & json from cache dir
                    if (url.contains(".json")) {
                        return new WebResourceResponse("application/json", "utf-8",
                                new FileInputStream(new File(Environment.getExternalStorageDirectory(),
                                        "/CIBench/"+Objects.requireNonNull(uri.getPath()).substring(1))));
                    } else if (url.contains(".bin")) {
                        return new WebResourceResponse("application/octet-stream", "utf-8",
                                new FileInputStream(new File(Environment.getExternalStorageDirectory(),
                                        "/CIBench/"+Objects.requireNonNull(uri.getPath()).substring(1))));
                    } else if (url.contains(".jpeg")) {
                        return new WebResourceResponse("image/jpeg", "utf-8",
                                getContext().getAssets().open(Objects.requireNonNull(uri.getPath()).substring(1)));
                    } else if (url.contains(".png")) {
                        return new WebResourceResponse("image/png", "utf-8",
                                getContext().getAssets().open(Objects.requireNonNull(uri.getPath()).substring(1)));
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
                                getContext().getAssets().open(getMission().getLabelFilePath())));
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
                        getContext().getAssets().open(getMission().getTrueLabelIndexPath())));
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
        mProgressListener.onFinish(nLoopCount, now - nStartTime);
    }

    @Override
    protected void releaseResources() {

    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(Device.CPU, Device.WEBGL);
    }

    @Override
    public String getFramework() {
        return "TensorFlow.js";
    }


    private Mission getMission() {
        return mMission;
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
