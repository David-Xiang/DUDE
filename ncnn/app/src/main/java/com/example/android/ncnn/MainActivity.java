package com.example.android.ncnn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.ContentValues;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int nImages = 10;
    private TextView mTextView;
    private ArrayList<Bitmap> mArrayBitmap;
    private Handler handler;
    private HandlerThread handlerThread;
    private Button mButton;
    private EditText mEditText;
    private ArrayList<String> models;
    private Spinner mSpinner;
    private int selectIndex;
    private static String path = "models";
    private String platform;
    private String status;
    private AssetManager mAssetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = "Ready";
        mTextView = findViewById(R.id.text);
        mTextView.setText(status);
        mButton = findViewById(R.id.button);
        mEditText = findViewById(R.id.editText);
        mEditText.setText("30");

        platform = "cpu";
//        platform = "gpu"; // vulkan

        mArrayBitmap = new ArrayList<>();
        for (int i = 0; i < nImages; i++){
            try {
                InputStream in = getAssets().open("images/"+i+".png");
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                mArrayBitmap.add(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // list all models
        mAssetManager = this.getAssets();
        models = new ArrayList<String>();
        try {
            String [] files = mAssetManager.list(path);
            for (String str: files){
                if (str.contains("bin"))
                    models.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//         init spinner
        mSpinner = findViewById(R.id.spinner);
        selectIndex = 0;
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, models);
        //绑定 Adapter到控件
        mSpinner.setAdapter(spinnerAdapter);
        //选择监听
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //parent就是父控件spinner
            //view就是spinner内填充的textview,id=@android:id/text1
            //position是值所在数组的位置
            //id是值所在行的位置，一般来说与positin一致
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                selectIndex = pos;
                Log.i(TAG, "Spinner: selected model " + models.get(selectIndex));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                processImage(
                        models.get(selectIndex).substring(0, models.get(selectIndex).lastIndexOf(".")),
                        Integer.valueOf(mEditText.getText().toString()));
            }
        });
    }

    protected void processImage(final String modelName, final int seconds) {
        mTextView.setText("Started");
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, String.format("modelName is %s", modelName));
                        String binPath = String.format("%s/%s.bin", path, modelName);
                        String paramPath = String.format("%s/%s.param", path, modelName);

                        Pattern pattern = Pattern.compile("^mnist-([0-9]+)-([0-9]+)$");
                        Matcher matcher = pattern.matcher(modelName);
                        int width = 0;
                        int depth = 0;
                        if(matcher.find()){
                            depth = Integer.parseInt(matcher.group(1));
                            width = Integer.parseInt(matcher.group(2));
                            Log.i(TAG, String.valueOf(width));
                            Log.i(TAG, String.valueOf(depth));
                        }

                        NCNN ncnn = new NCNN();

                        byte[] param = null;
                        byte[] bin = null;

                        try{
                            InputStream assetsInputStream = getAssets().open(paramPath);
                            int available = assetsInputStream.available();
                            param = new byte[available];
                            assetsInputStream.read(param);
                            assetsInputStream.close();
                            assetsInputStream = getAssets().open(binPath);
                            available = assetsInputStream.available();
                            bin = new byte[available];
                            assetsInputStream.read(bin);
                            assetsInputStream.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }


                        ncnn.Init(param, bin);

                        Log.i(TAG, "After init");
                        final long startTime = SystemClock.uptimeMillis();

                        //LocalDateTime startLocalDateTime = LocalDateTime.now();
                        //LocalDateTime endLocalDateTime = null;

                        int count = 0;
                        while(SystemClock.uptimeMillis() - startTime < seconds * 1000){
                            ncnn.Detect(mArrayBitmap.get(count%nImages), platform.equals("gpu"), depth);
                            count++;
                            if (count % 10000 == 0){
                                Log.i(TAG, Integer.toString(count));
                                freshUi(Integer.toString(count));
                            }
                        }

                        int enduredTime = (int) (SystemClock.uptimeMillis() - startTime);

                        //endLocalDateTime = LocalDateTime.now();

                        Log.i(TAG, String.format("Detect %d images in %d s.", count, seconds));
                        freshUi(String.format("Detect %d images in %d s.", count, seconds));
//                    updateDatabase("mnist", "caffe2",
//                            android.os.Build.BRAND + " " + android.os.Build.MODEL,
//                            android.os.Build.VERSION.RELEASE, platform, "dnn",
//                            width, depth, startLocalDateTime, endLocalDateTime, enduredTime, count);

                    }
                    public void freshUi(final String msg){
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mTextView.setText(msg);
                                    }
                                });
                    }
                });
    }
    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    void updateDatabase(String project, String framework, String device, String system,
                        String platform, String nntype, int width, int depth,
                        LocalDateTime startTime, LocalDateTime endTime, int enduredTime,
                        int itemProcessed){
        ContentValues contentValues = new ContentValues();
        contentValues.put("project", project); // String
        contentValues.put("framework", framework); // String
        contentValues.put("device", device); // String
        contentValues.put("system", system);
        contentValues.put("platform", platform); // String
        contentValues.put("nntype", nntype); // String
        contentValues.put("width", width); // int
        contentValues.put("depth", depth); // int
        contentValues.put("starttime", startTime.toString()); // LocalDateTime
        contentValues.put("endtime", endTime.toString()); // LocalDateTime
        contentValues.put("enduredtime", enduredTime); // int
        contentValues.put("itemprocessed", itemProcessed); // int

        Uri uri = Uri.parse("content://com.example.android.experimentdb.provider/insert");
        getContentResolver().insert(uri, contentValues);
        Toast.makeText(getApplicationContext(), "Record inserted", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Record inserted.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume().");
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        Log.v(TAG, "onPause()");

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }
}
