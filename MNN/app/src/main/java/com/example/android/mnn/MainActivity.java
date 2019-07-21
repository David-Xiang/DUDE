package com.example.android.mnn;

import android.content.ContentValues;
        import android.content.res.AssetManager;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.net.Uri;
        import android.os.Handler;
        import android.os.HandlerThread;
        import android.os.SystemClock;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
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
        import java.nio.ByteBuffer;
        import java.time.LocalDateTime;
        import java.util.ArrayList;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int nImages = 10;
    private TextView mTextView;
    private ArrayList<Bitmap> mArrayBitmap;
    private ArrayList<byte []> mArrayByte;
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

    private void prepareModels(String mMobileModelFileName, String mMobileModelPath) {
        try {
            Common.copyAssetResource2File(getBaseContext(), mMobileModelFileName, mMobileModelPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

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

//        platform = "cpu";
//        platform = "gpu"; // opencl
        platform = "vulkan";

        mArrayBitmap = new ArrayList<>();
        mArrayByte = new ArrayList<>();
        for (int i = 0; i < nImages; i++){
            try {
                InputStream in = getAssets().open("images/"+i+".png");
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                int bytes = bitmap.getByteCount();

                mArrayBitmap.add(bitmap);

                ByteBuffer buf = ByteBuffer.allocate(bytes);
                bitmap.copyPixelsToBuffer(buf);

                byte[] byteArray = buf.array();
                mArrayByte.add(byteArray);
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
                models.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String model: models){
            Log.i(TAG, model);
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
                        models.get(selectIndex),
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
                        String modelFilePath = String.format("%s/%s", path, modelName);
                        String mobileModelPath = getCacheDir() + "/" + modelName;
                        prepareModels(modelFilePath, mobileModelPath);

                        MNNNetInstance instance = MNNNetInstance.createFromFile(MainActivity.this, mobileModelPath);
                        // create session
                        MNNNetInstance.Config config= new MNNNetInstance.Config();
                        config.numThread = 4;
                        config.forwardType = MNNForwardType.FORWARD_CPU.type;
                        if(platform.equals("gpu")){
                            config.forwardType = MNNForwardType.FORWARD_OPENCL.type;
                        } else if (platform.equals("vulkan")){
                            config.forwardType = MNNForwardType.FORWARD_VULKAN.type;
                        }
                        //config.saveTensors = new String[]{"layer name"};
                        MNNNetInstance.Session session = instance.createSession(config);
                        // get input tensor
                        MNNNetInstance.Session.Tensor inputTensor = session.getInput(null);
                        Matrix matrix = new Matrix();

                        MNNImageProcess.Config config1 = new MNNImageProcess.Config();

                        //Log.i(TAG, "After init");
                        final long startTime = SystemClock.uptimeMillis();

                        //LocalDateTime startLocalDateTime = LocalDateTime.now();
                        //LocalDateTime endLocalDateTime = null;

                        int count = 0;
                        while(SystemClock.uptimeMillis() - startTime < seconds * 1000){
                            MNNImageProcess.convertBuffer(mArrayByte.get(count%nImages), 28, 28, inputTensor,
                                    config1, matrix);
                            session.run();
                            MNNNetInstance.Session.Tensor output = session.getOutput(null);
                            float[] result = output.getFloatData();
                            count++;
                            if (count % 10000 == 0){
                                Log.i(TAG, Integer.toString(count));
                                freshUi(Integer.toString(count));
                            }
                        }

                        int enduredTime = (int) (SystemClock.uptimeMillis() - startTime);
                        instance.release();

                        //endLocalDateTime = LocalDateTime.now();

                        Log.i(TAG, String.format("Detect %d images in %d s.", count, seconds));
                        freshUi(String.format("Detect %d images in %d s.", count, seconds));
                        Pattern pattern = Pattern.compile("^mnist-([0-9]+)-([0-9]+)-init.pb$");
                        Matcher matcher = pattern.matcher(models.get(selectIndex));
                        int width = 0;
                        int depth = 0;
                        if(matcher.find()){
                            depth = Integer.parseInt(matcher.group(1));
                            width = Integer.parseInt(matcher.group(2));
                            Log.i(TAG, String.valueOf(width));
                            Log.i(TAG, String.valueOf(depth));
                        }
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
