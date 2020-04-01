package com.example.android.clientintelligent.interpreter.tvm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.AccuracyTask;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.framework.pojo.Recognition;
import com.example.android.clientintelligent.util.FileUtil;

import org.apache.tvm.Function;
import org.apache.tvm.Module;
import org.apache.tvm.NDArray;
import org.apache.tvm.TVMContext;
import org.apache.tvm.TVMType;
import org.apache.tvm.TVMValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class TVMAccuracyTask extends AccuracyTask {
    private static final String TAG = "TVMAccuracyTask";
    private static final String INPUT_NAME = "input_1";
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    private List<String> mLabels;
    private List<Integer> mLabelIndexList;
    private Context mContext;
    private int nThread = 1;
    private boolean useGPU = false;
    private TVMContext mTVMCtx;
    private Module mGraphRuntimeModule;

    public TVMAccuracyTask(Mission mission, IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
        mContext = mission.getContext();
    }

    @Override
    protected void loadLabelList(String labelFilePath) throws IOException {
        mLabels = new ArrayList<>();
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                getMission().getContext().getAssets().open(labelFilePath)));
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
                        getMission().getContext().getAssets().open(trueLabelIndexPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabelIndexList.add(Integer.parseInt(line));
        }
        reader.close();
    }

    @Override
    protected void loadModelFile(Model model) throws IOException {
        byte[] param;
        byte[] byteArray;
        String modelGraph;


        String graphPath = model.getModelPath();
        String paramPath = model.getParamFilePath();
        String libPath = useGPU ? model.getLibGpuPath() : model.getLibCpuPath();

        InputStream inputStream;
        int available, byteCode;

        inputStream = FileUtil.getExternalResourceInputStream(graphPath);
        available = inputStream.available();
        byteArray = new byte[available];
        byteCode = inputStream.read(byteArray);
        modelGraph = new String(byteArray);
        inputStream.close();

        inputStream = FileUtil.getExternalResourceInputStream(paramPath);
        available = inputStream.available();
        param = new byte[available];
        byteCode = inputStream.read(param);
        inputStream.close();

        // tvm module for compiled functions
        String cacheLibPath = String.format("%s/%s",
                mContext.getCacheDir(),
                libPath.substring(libPath.lastIndexOf("/")+1));
        Log.i(TAG, "loadModelFile(): cacheLibPath = " + cacheLibPath);
        FileUtil.copyExternalResource2File(libPath, cacheLibPath);

        Module modelLib = Module.load(cacheLibPath);

        // get global function module for graph runtime
        Function runtimeCreFun = Function.getFunction("tvm.graph_runtime.create");
        TVMValue runtimeCreFunRes = runtimeCreFun.pushArg(modelGraph)
                .pushArg(modelLib)
                .pushArg(mTVMCtx.deviceType)
                .pushArg(mTVMCtx.deviceId)
                .invoke();
        mGraphRuntimeModule = runtimeCreFunRes.asModule();

        // get the function from the module(load parameters)
        Function loadParamFunc = mGraphRuntimeModule.getFunction("load_params");
        loadParamFunc.pushArg(param).invoke();

        // release tvm local variables
        modelLib.release();
        loadParamFunc.release();
        runtimeCreFun.release();
    }

    @Override
    protected void configSession(IInterpreter.Device device, int nThreads) {
        switch(device){
            case CPU: useGPU = false; mTVMCtx = TVMContext.cpu(); break;
            case OPENCL: useGPU = true; mTVMCtx = TVMContext.opencl(); break;
            default: mProgressListener.onError("buildTask: Wrong device type!");return;
        }
        this.nThread = nThreads;
    }

    protected List<Recognition> recognizeImage(Bitmap bitmap) {
        int imageSizeX = getMission().getnImageSizeX();
        int imageSizeY = getMission().getnImageSizeY();
        int imageChannel = 3;
        int[] pixelValues = new int[imageSizeX * imageSizeY];
        float[] imgRgbValues = new float[imageSizeX * imageSizeY * imageChannel];
        NDArray inputNdArray = NDArray.empty(new long[]{1, imageChannel, imageSizeX, imageSizeY}, new TVMType("float32"));
        NDArray outputNdArray = NDArray.empty(new long[]{1, getMission().getOutputSize()}, new TVMType("float32"));

        bitmap.getPixels(pixelValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;

        int interval = imageSizeX * imageSizeY;
        for (int i = 0; i < imageSizeX; ++i) {
            for (int j = 0; j < imageSizeY; ++j) {
                int val = pixelValues[pixel];
                imgRgbValues[pixel] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                imgRgbValues[pixel+interval] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                imgRgbValues[pixel+2*interval] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                pixel++;
            }
        }

        inputNdArray.copyFrom(imgRgbValues);
        Function setInputFunc = mGraphRuntimeModule.getFunction("set_input");
        setInputFunc.pushArg(INPUT_NAME).pushArg(inputNdArray).invoke();
        setInputFunc.release();

        // get the function from the module(run it)
        Function runFunc = mGraphRuntimeModule.getFunction("run");
        runFunc.invoke();
        // release tvm local variables
        runFunc.release();

        // get the function from the module(get output data)
        Function getOutputFunc = mGraphRuntimeModule.getFunction("get_output");
        getOutputFunc.pushArg(0).pushArg(outputNdArray).invoke();
        float[] output = outputNdArray.asFloatArray();
        // release tvm local variables
        getOutputFunc.release();
        inputNdArray.release();
        outputNdArray.release();

        // 显示结果
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        5,
                        (lhs, rhs) -> {
                            // Intentionally reversed to put high confidence at the head of the queue.
                            return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                        });
        for (int i = 0; i < mLabels.size(); ++i) {
            pq.add(new Recognition(i+1, mLabels.get(i), output[i], null));
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

    private Bitmap loadValidImage(int index) throws IOException {
        InputStream in = getMission().getContext()
                .getAssets()
                .open(getMission().getDataPathList().get(index));
        return BitmapFactory.decodeStream(in);
    }

    @Override
    protected void releaseResources() {
        if (null != mGraphRuntimeModule)
            mGraphRuntimeModule.release();
        mContext = null;
        mTVMCtx = null;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        configSession(getMission().getDevice(), getMission().getnThreads());

        try {
            loadModelFile(getMission().getModels().get(0));
            loadLabelList(getMission().getLabelFilePath());
            loadLabelIndexList(getMission().getTrueLabelIndexPath());
        } catch (IOException e) {
            e.printStackTrace();
            mProgressListener.onError("Error in loading files!");
            return 0;
        }

        int count = 0;
        int totalNum = Math.min(mLabelIndexList.size(), getMission().getDataPathList().size());
        long now = SystemClock.uptimeMillis();
        Bitmap bitmap;
        AccuracyResult result = new AccuracyResult();

        while(now - nStartTime < nSeconds * 1000 && count < totalNum) {
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

