//package com.example.android.clientintelligent.interpreter.tflite;
//
//import android.content.ContentValues;
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.SystemClock;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Spinner;
//import android.widget.TextView;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//    private static final int nImages = 10;
//    private TextView mTextView;
//    private ArrayList<Bitmap> mArrayBitmap;
//    private Classifier classifier;
//    private Button mButton;
//    private EditText mEditText;
//    private String [] models;
//    private Spinner mSpinner;
//    private int selectIndex;
//    private static String path = "models";
//    private String platform;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mTextView = findViewById(R.id.text);
//        mButton = findViewById(R.id.button);
//        mEditText = findViewById(R.id.editText);
//        mEditText.setText("30");
//
//        platform = "cpu";
//
//        mArrayBitmap = new ArrayList<>();
//        for (int i = 0; i < nImages; i++){
//            try {
//                InputStream in = getAssets().open("images/"+i+".png");
//                Bitmap bitmap = BitmapFactory.decodeStream(in);
//                mArrayBitmap.add(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // list all models
//        AssetManager assetManager = this.getAssets();
//        models = null;
//        try {
//            models = assetManager.list(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (String model: models){
//            Log.i(TAG, model);
//        }
//
//        // init spinner
//        mSpinner = findViewById(R.id.spinner);
//        selectIndex = 0;
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, models);
//        //绑定 Adapter到控件
//        mSpinner.setAdapter(spinnerAdapter);
//        //选择监听
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            //parent就是父控件spinner
//            //view就是spinner内填充的textview,id=@android:id/text1
//            //position是值所在数组的位置
//            //id是值所在行的位置，一般来说与positin一致
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view,
//                                       int pos, long id) {
//                selectIndex = pos;
//                Log.i(TAG, "Spinner: selected model " + models[selectIndex]);
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Another interface callback
//            }
//        });
//
//
//        mButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    classifier = new MnistClassfier(MainActivity.this, path+"/"+models[selectIndex]);
//                    classifier.useCPU();
//                    platform = "cpu";
////                    classifier.useGpu();
////                    platform = "gpu";
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                processImage(Integer.valueOf(mEditText.getText().toString()));
//            }
//        });
//    }
//
//    protected void processImage(final int seconds) {
//        if (classifier == null) {
//            return;
//        }
//
//        runInBackground(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        final long startTime = SystemClock.uptimeMillis();
//
//                        LocalDateTime startLocalDateTime = LocalDateTime.now();
//                        LocalDateTime endLocalDateTime = null;
//
//                        int count = 0;
//                        while(SystemClock.uptimeMillis() - startTime < seconds * 1000){
//                            final List<Classifier.Recognition> results =
//                                    classifier.recognizeImage(mArrayBitmap.get(count%nImages));
//                            count++;
//                            if (count % 10000 == 0){
//                                Log.i(TAG, Integer.toString(count));
//                                freshUi(Integer.toString(count));
//                            }
//                        }
//
//                        int enduredTime = (int) (SystemClock.uptimeMillis() - startTime);
//
//                        endLocalDateTime = LocalDateTime.now();
//
//                        Log.i(TAG, String.format("Detect %d images in %d s.", count, seconds));
//                        freshUi(String.format("Detect %d images in %d s.", count, seconds));
//                        Pattern pattern = Pattern.compile("^mnist-([0-9]+)-([0-9]+).tflite$");
//                        Matcher matcher = pattern.matcher(models[selectIndex]);
//                        int width = 0;
//                        int depth = 0;
//                        if(matcher.find()){
//                            depth = Integer.parseInt(matcher.group(1));
//                            width = Integer.parseInt(matcher.group(2));
//                            Log.i(TAG, String.valueOf(width));
//                            Log.i(TAG, String.valueOf(depth));
//                        }
////                        updateDatabase("mnist", "tflite",
////                                android.os.Build.BRAND + " " + android.os.Build.MODEL,
////                                android.os.Build.VERSION.RELEASE, platform, "dnn",
////                                width, depth, startLocalDateTime, endLocalDateTime, enduredTime, count);
//
//                    }
//                    public void freshUi(final String msg){
//                        runOnUiThread(
//                                new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        mTextView.setText(msg);
//                                    }
//                                });
//                    }
//                });
//    }
//
//
//    void updateDatabase(String project, String framework, String device, String system,
//                        String platform, String nntype, int width, int depth,
//                        LocalDateTime startTime, LocalDateTime endTime, int enduredTime,
//                        int itemProcessed){
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("project", project); // String
//        contentValues.put("framework", framework); // String
//        contentValues.put("device", device); // String
//        contentValues.put("system", system);
//        contentValues.put("platform", platform); // String
//        contentValues.put("nntype", nntype); // String
//        contentValues.put("width", width); // int
//        contentValues.put("depth", depth); // int
//        contentValues.put("starttime", startTime.toString()); // LocalDateTime
//        contentValues.put("endtime", endTime.toString()); // LocalDateTime
//        contentValues.put("enduredtime", enduredTime); // int
//        contentValues.put("itemprocessed", itemProcessed); // int
//
//        Uri uri = Uri.parse("content://com.example.android.experimentdb.provider/insert");
//        getContentResolver().insert(uri, contentValues);
//        //Toast.makeText(getApplicationContext(), "Record inserted", Toast.LENGTH_SHORT).show();
//        Log.i(TAG, "Record inserted.");
//    }
//
//
//}
