package com.example.android.clientintelligent.interpreter.tflite;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.android.clientintelligent.framework.Model;
import com.example.android.clientintelligent.framework.Recognition;
import com.example.android.clientintelligent.framework.Mission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

/** A classifier specialized to label images using TensorFlow Lite. */
public abstract class BaseClassifier {
    /** The model type used for classification. */

    private static final String TAG = "BaseClassifier";

    /** Number of results to show in the UI. */
    private static final int MAX_RESULTS = 5;

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    protected final Mission mission;

    /** Preallocated buffers for storing image data in. */
    private final int[] intValues;

    /** The loaded TensorFlow Lite model. */
    private MappedByteBuffer tfliteModel;

    /** Labels corresponding to the output of the vision model. */
    private List<String> labels;

    /** Optional GPU delegate for accleration. */
    private GpuDelegate gpuDelegate;

    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    Interpreter tflite;

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
    ByteBuffer imgData;

    protected BaseClassifier(Activity activity, Mission mission) throws IOException {
        this.mission = mission;
        tfliteModel = loadModelFile(activity);
        intValues = new int[getImageSizeX() * getImageSizeY()];
        Interpreter.Options tfliteOptions = new Interpreter.Options();
        switch (mission.getDevice()) {
            case NNAPI:
                tfliteOptions.setUseNNAPI(true);
                break;
            case GPU:
                gpuDelegate = new GpuDelegate();
                tfliteOptions.addDelegate(gpuDelegate);
                break;
            case CPU:
                break;
        }
        tfliteOptions.setNumThreads(mission.getnThreads());
        if (mission.getModelMode() == Model.Mode.FLOAT16){
            tfliteOptions.setAllowFp16PrecisionForFp32(true);
        }
        tflite = new Interpreter(tfliteModel, tfliteOptions);
        labels = loadLabelList(activity);
        imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * getImageSizeX()
                                * getImageSizeY()
                                * getNumChannelsPerPixel()
                                * getNumBytesPerChannel());
        imgData.order(ByteOrder.nativeOrder());

        Log.d(TAG, "Created a Tensorflow Lite Image BaseClassifier.");
    }

    /** Reads label list from Assets. */
    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(activity.getAssets().open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        FileInputStream inputStream = new FileInputStream(new File(getModelPath()));
        FileDescriptor fileDescriptor = inputStream.getFD();
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    /** Writes Image data into a {@code ByteBuffer}. */
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < getImageSizeX(); ++i) {
            for (int j = 0; j < getImageSizeY(); ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
    }

    /** Runs inference and returns the classification results. */
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        convertBitmapToByteBuffer(bitmap);

        // Run the inference call.
        runInference();

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        5,
                        (lhs, rhs) -> {
                            // Intentionally reversed to put high confidence at the head of the queue.
                            return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                        });
        for (int i = 0; i < labels.size(); ++i) {
            pq.add(
                    new Recognition(
                            i+1,
                            labels.get(i),
                            getNormalizedProbability(i),
                            null));
        }
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    /** Closes the interpreter and model to release resources. */
    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        tfliteModel = null;
    }

    /**
     * Get the image size along the x axis.
     */
    public abstract int getImageSizeX();

    /**
     * Get the image size along the y axis.
     */
    public abstract int getImageSizeY();

    /**
     * Get the name of the model file stored in Assets.
     */
    protected abstract String getModelPath();

    /**
     * Get the name of the label file stored in Assets.
     */
    protected abstract String getLabelPath();

    /**
     * Get the number of bytes that is used to store a single color channel value.
     */
    protected abstract int getNumBytesPerChannel();

    /**
     * Get the number of channels of a single pixel.
     */
    protected abstract int getNumChannelsPerPixel();

    /**
     * Add pixelValue to byteBuffer.
     */
    protected abstract void addPixelValue(int pixelValue);

    /**
     * Read the probability value for the specified label This is either the original value as it was
     * read from the net's output or the updated value after the filter was applied.
     */
    protected abstract float getProbability(int labelIndex);

    /**
     * Set the probability value for the specified label.
     */
    protected abstract void setProbability(int labelIndex, Number value);

    /**
     * Get the normalized probability value for the specified label. This is the final value as it
     * will be shown to the user.
     *
     */
    protected abstract float getNormalizedProbability(int labelIndex);

    /**
     * Run inference using the prepared input in {@link #imgData}. Afterwards, the result will be
     * provided by getProbability().
     *
     * <p>This additional method is necessary, because we don't have a common base for different
     * primitive data types.
     */
    protected abstract void runInference();

    protected abstract void runInference(ByteBuffer data);

    /**
     * Get the total number of labels.
     */
    protected int getNumLabels() {
        return labels.size();
    }
}