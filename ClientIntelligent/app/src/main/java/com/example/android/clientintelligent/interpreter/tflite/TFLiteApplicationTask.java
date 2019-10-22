package com.example.android.clientintelligent.interpreter.tflite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.clientintelligent.framework.Task;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;
import com.example.android.clientintelligent.framework.pojo.Recognition;
import com.example.android.clientintelligent.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.android.clientintelligent.framework.pojo.Mission.Purpose.APP_BANLANCE;
import static com.example.android.clientintelligent.framework.pojo.Mission.Purpose.APP_PERFORMANCE;

public class TFLiteApplicationTask extends Task {
    private static final String TAG = "TFLiteApplicationTask";
    private List<Integer> mLabelIndexList;

    TFLiteApplicationTask(Mission mission, IProgressListener progressListener) throws IOException {
        super(mission, progressListener, Integer.MAX_VALUE);
        mLabelIndexList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getMission().getContext().getAssets().open(getMission().getTrueLabelIndexPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            mLabelIndexList.add(Integer.parseInt(line));
        }
        reader.close();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        List<TFLiteClassifier> classifiers;
        List<Model> models = getMission().getModels();

        classifiers = getClassifiers(models);

        ClassifierStrategy classiferStrategy = new ClassifierStrategy(getMission().getPurpose(), classifiers, mProgressListener);

        int count = 0;
        int top1count = 0;
        int top5count = 0;
        int dataAmount = getMission().getDataPathList().size();
        long now = SystemClock.uptimeMillis();
        while(count < dataAmount){
            // load data
            Bitmap bitmap;
            try {
                InputStream in = getMission().getContext().getAssets().open(getMission().getDataPathList().get(count));
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                mProgressListener.onError("Error in read data!");
                return count;
            }

            // get right time interval
            int remainMs = getMission().getnTime() - (int)(SystemClock.uptimeMillis() - now);
            if (remainMs > 0) {
                try {
                    Thread.sleep(remainMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            now = SystemClock.uptimeMillis();

            // start classification
            TFLiteClassifier classifier = classiferStrategy.getClassifier();
            long beforeRecognize = SystemClock.uptimeMillis();
            List<Recognition> recognitionList = classifier.recognizeImage(bitmap);
            classiferStrategy.evaluateCallBack(classifier, (int)(SystemClock.uptimeMillis()-beforeRecognize));
            publishProgress(100*count/dataAmount, "", bitmap, recognitionList);

            // get results
            if (mLabelIndexList.get(count) == recognitionList.get(0).getId()){
                top1count++;
            }
            int finalCount = count;
            if (recognitionList
                    .stream()
                    .map(Recognition::getId)
                    .anyMatch(n-> n.equals(mLabelIndexList.get(finalCount)))) {
                top5count++;
            }
            Log.i(TAG, String.format("doInBackground: count = %d, top1count = %d, top5count = %d",
                    count, top1count, top5count));
            count++;
        }

        @SuppressLint("DefaultLocale")
        String msg = String.format("Top 1 accuracy is %.2f%%, top 5 accuracy is %.2f%%",
                (float)(top1count) * 100 / count,
                (float)(top5count) * 100 / count);
        publishProgress(100, msg);

        // release sources
        for (TFLiteClassifier classifier: classifiers) {
            classifier.close();
        }
        return count;
    }

    private List<TFLiteClassifier> getClassifiers(List<Model> models) {
        return models.stream().map(
                (Function<Model, TFLiteClassifier>) model -> {
                    String modelFilePath = model.getFilePath();
                    String cacheModelPath = String.format("%s/%s",
                            getMission().getContext().getCacheDir(),
                            modelFilePath.substring(modelFilePath.lastIndexOf("/")+1));
                    Log.i(TAG, "loadModelFiles(): cacheModelPath = " + cacheModelPath);

                    try {
                        FileUtil.copyExternalResource2File(modelFilePath, cacheModelPath);
                        if (getMission().getModelMode() == Model.Mode.FLOAT32)
                            return new FloatTFLiteClassifier(getMission(), cacheModelPath, model.getAccuracy());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }

                    return null;
                }).collect(Collectors.toList());
    }

    private class ClassifierStrategy {
        private static final String TAG = "ClassifierStrategy";
        private static final int EVAL_ROUND = 5; // evaluate each classfier for 3 times

        private List<TFLiteClassifier> classifiers;
        private Mission.Purpose purpose;
        private IProgressListener progressListener;

        private boolean classifierDecided;
        private TFLiteClassifier decision;

        private int count;
        private Map<TFLiteClassifier, Integer> classifierTimeMap;

        ClassifierStrategy(Mission.Purpose purpose, List<TFLiteClassifier> classifiers, IProgressListener progressListener) {
            this.purpose = purpose;
            this.classifiers = classifiers;
            this.progressListener = progressListener;
            classifierDecided = false;
            decision = null;
            count = 0;
            classifierTimeMap = new HashMap<>();
        }
        TFLiteClassifier getClassifier() {
            if (classifierDecided) {
                return decision;
            }

            switch (purpose) {
                case APP_ACCURACY:
                    return getClassifierWithHighestAccuracy();
                case APP_PERFORMANCE:
                    return getClassifierWithLowestLatency();
                case APP_BANLANCE:
                    return getClassifierWithBalancePerformance();
                case BENCH_ACCURACY:
                case BENCH_PERFORMANCE:
                default:
                    sendMsg("Using default model");
                    foundDecision(classifiers.get(0));
                    return decision;
            }
        }

        @SuppressLint("DefaultLocale")
        TFLiteClassifier getClassifierWithLowestLatency() {
            if (count >= classifiers.size() * EVAL_ROUND) {
                foundDecision(
                        classifiers.stream().max(
                                (o1, o2) -> classifierTimeMap.get(o2) - classifierTimeMap.get(o1)).get());
                sendMsg(String.format("Using %s with lowest latency %d ms",
                        getModelName(decision.getModelPath()), classifierTimeMap.get(decision)/EVAL_ROUND));
                return decision;
            }

            TFLiteClassifier retval = classifiers.get(count/EVAL_ROUND);
            sendMsg(String.format("Evaluating %s ", getModelName(retval.getModelPath())));
            return retval;
        }

        @SuppressLint("DefaultLocale")
        TFLiteClassifier getClassifierWithBalancePerformance() {
            if (count >= classifiers.size() * EVAL_ROUND) {
                foundDecision(
                        classifiers.stream()
                                .filter(
                                        classifier -> classifierTimeMap.get(classifier)/EVAL_ROUND < getMission().getnTime())
                                .max(
                                        (c1, c2) -> c1.getAccuracy() > c2.getAccuracy() ? 1 : -1).get()
                );
                sendMsg(String.format("Using %s with balanced performance",
                        getModelName(decision.getModelPath())));
                return decision;
            }

            TFLiteClassifier retval = classifiers.get(count/EVAL_ROUND);
            sendMsg(String.format("Evaluating %s ", getModelName(retval.getModelPath())));
            return retval;
        }

        @SuppressLint("DefaultLocale")
        TFLiteClassifier getClassifierWithHighestAccuracy() {
            foundDecision(
                    classifiers.stream().max(
                            (c1, c2) -> c1.getAccuracy() > c2.getAccuracy() ? 1 : -1).get());
            Log.i(TAG, "getClassifierWithHighestAccuracy: "+getModelName(decision.getModelPath()));
            sendMsg(String.format("Using %s with highest acc %.1f%%",
                    getModelName(decision.getModelPath()), decision.getAccuracy()));
            return decision;
        }

        void foundDecision(TFLiteClassifier classifier) {
            classifierDecided = true;
            decision = classifier;

            // release other classifiers early!
            for (TFLiteClassifier c: classifiers) {
                if (c != classifier) {
                    c.close();
                }
            }
        }

        void evaluateCallBack(TFLiteClassifier classifier, int elapsedTime) {
            if (classifierDecided) {
                return;
            }
            if (purpose == APP_PERFORMANCE || purpose == APP_BANLANCE) {
                Log.i(TAG, String.format("evaluateCallBack: %s %d", getModelName(classifier.getModelPath()), elapsedTime));
                Integer sum = classifierTimeMap.get(classifier);
                if (sum == null) {
                    classifierTimeMap.put(classifier, elapsedTime);
                } else {
                    classifierTimeMap.put(classifier, sum+elapsedTime);
                }
                count++;
            }
        }
        void sendMsg(String msg) {
            progressListener.onMsg(String.format("%s", msg));
        }

        String getModelName(String path) {
            return path.substring(path.lastIndexOf("/")+1);
        }
    }
}
