package com.example.android.clientintelligent.interpreter.ncnn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.PerformanceTask;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public final class NCNNPerformanceTask extends PerformanceTask {
    private static final String TAG = "NCNNPerformanceTask";
    private List<Bitmap> mBitmapList;
    private Boolean useGPU = false;
    private String mInNodeName;
    private String mOutNodeName;
    private int nThread = 1;

    public NCNNPerformanceTask(Mission mission, IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
    }

    @Override
    protected void loadModelFile(String path) throws IOException {
        byte[] param;
        byte[] bin;
        String paramPath = path.split("\\$")[0];
        String binPath = path.split("\\$")[1];

        InputStream inputStream = FileUtil.getExternalResourceInputStream(paramPath);
        int available = inputStream.available();
        param = new byte[available];
        int byteCode = inputStream.read(param);
        inputStream.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtil.getExternalResourceInputStream(paramPath)));
        reader.readLine();
        reader.readLine();
        String inputLine = reader.readLine();
        mInNodeName = inputLine.split("\\s+")[1];

        String lastLine = null;
        while((inputLine = reader.readLine()) != null) {
            lastLine = inputLine;
        }
        mOutNodeName = lastLine.split("\\s+")[1];
        Log.i(TAG, "loadModelFile: in_node = " + mInNodeName + " out_node = " + mOutNodeName);


        inputStream = FileUtil.getExternalResourceInputStream(binPath);
        available = inputStream.available();
        bin = new byte[available];
        byteCode = inputStream.read(bin);
        inputStream.close();

        NCNNNative.InitModel(param, bin);
    }

    @Override
    protected void configSession(IInterpreter.Device device, int nThreads) {
        switch(device){
            case CPU: this.useGPU = false; break;
            case VULKAN: this.useGPU = true; break;
            default: mProgressListener.onError("buildTask: Wrong device type!");return;
        }
        this.nThread = nThreads;
    }

    @Override
    protected Bitmap loadValidImage(int index) {
        return mBitmapList.get(index % mBitmapList.size());
    }

    @Override
    protected void releaseResources() {

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

            float[] result = NCNNNative.Detect(bitmap, useGPU, nThread, mInNodeName.toCharArray(), mOutNodeName.toCharArray());

            if (count % 50 == 0){
                now = SystemClock.uptimeMillis();
                publishProgress((int) ((now - nStartTime) / (nSeconds * 10)));
            }
            count++;
        }

        releaseResources();
        return count;
    }

    private Bitmap readImage(String path) {
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(getMission().getContext().getAssets().open(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
}

