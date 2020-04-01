package com.example.android.clientintelligent.interpreter.tvm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.PerformanceTask;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.util.FileUtil;

import org.apache.tvm.Function;
import org.apache.tvm.Module;
import org.apache.tvm.NDArray;
import org.apache.tvm.TVMContext;
import org.apache.tvm.TVMType;
import org.apache.tvm.TVMValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public final class TVMPerformanceTask extends PerformanceTask {
    private static final String TAG = "TVMPerformanceTask";
    private static final String INPUT_NAME = "data";

    private List<Bitmap> mBitmapList;
    private Context mContext;
    private int nThread = 1;
    private boolean useGPU = false;
    private TVMContext mTVMCtx;
    private Module mGraphRuntimeModule;
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    TVMPerformanceTask(Mission mission, IProgressListener progressListener, int seconds) {
        super(mission, progressListener, seconds);
        mContext = mission.getContext();
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

    @Override
    protected Bitmap loadValidImage(int index) {
        return mBitmapList.get(index % mBitmapList.size());
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
            List<String> dataPathList = getMission().getDataPathList();
            mBitmapList = dataPathList.stream()
                    .map(this::readImage)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            mProgressListener.onError("Error in loading files!");
            return 0;
        }

        int count = 0;
        long now = SystemClock.uptimeMillis();
        Bitmap bitmap;
        int imageSizeX = getMission().getnImageSizeX();
        int imageSizeY = getMission().getnImageSizeY();
        int imageChannel = getMission().getChannelsPerPixel();
        int[] pixelValues = new int[imageSizeX * imageSizeY];
        float[] imgRgbValues = new float[imageSizeX * imageSizeY * imageChannel];
        NDArray inputNdArray = NDArray.empty(new long[]{1, imageChannel, imageSizeX, imageSizeY}, new TVMType("float32"));
        NDArray outputNdArray = NDArray.empty(new long[]{1, getMission().getOutputSize()}, new TVMType("float32"));

        int interval = imageSizeX * imageSizeY;
        while(now - nStartTime < nSeconds * 1000) {
            bitmap = loadValidImage(count);
            bitmap.getPixels(pixelValues, 0, imageSizeX, 0, 0, imageSizeX, imageSizeY);
            for (int j = 0; j < pixelValues.length; ++j) {
                int val = pixelValues[j];
                imgRgbValues[j] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                imgRgbValues[j+interval] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                imgRgbValues[j+2*interval] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
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

            if (count % 50 == 0){
                now = SystemClock.uptimeMillis();
                publishProgress((int) ((now - nStartTime) / (nSeconds * 10)));
            }
            count++;
        }

        inputNdArray.release();
        outputNdArray.release();

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

