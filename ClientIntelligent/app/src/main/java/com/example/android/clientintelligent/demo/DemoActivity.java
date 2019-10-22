package com.example.android.clientintelligent.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.clientintelligent.MainActivity;
import com.example.android.clientintelligent.R;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Recognition;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DemoActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, IProgressListener {
    private static final String TAG = "DemoActivity";
    private static String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final int mRequestCode = 100;
    private List<String> mPermissionList = new ArrayList<>();

    private DemoEngineImpl mEngine;
    private IInterpreter mInterpreter;

    private Spinner mInterpreterSpinner;
    private Spinner mDeviceSpinner;
    private Spinner mModeSpinner;
    private ProgressBar mProgressBar;
    private SeekBar mTimeSeekBar;
    private SeekBar mThreadSeekBar;
    private TextView mTimeTextView;
    private TextView mThreadTextView;
    private FloatingActionButton mStartButton;

    private LinearLayout mResultLayout;
    private ImageView mResultImage;
    private List<TextView> mResultTextViewList;
    private TextView mMsgTextView;

    private static final BiMap<IInterpreter.Device, String> DEVICE_STRING_HASH_MAP;
    private static final BiMap<Mission.Purpose, String> PURPOSE_STRING_HASH_MAP;

    static {
        DEVICE_STRING_HASH_MAP = HashBiMap.create();
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.CPU, "CPU");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.GPU, "GPU");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.NNAPI, "NNAPI");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.VULKAN, "VULKAN");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.OPENCL, "OPENCL");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.OPENGL, "OPENGL");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.WEBGL, "WEBGL");
        PURPOSE_STRING_HASH_MAP = HashBiMap.create();
        PURPOSE_STRING_HASH_MAP.put(Mission.Purpose.APP_PERFORMANCE, "Performance Mode");
        PURPOSE_STRING_HASH_MAP.put(Mission.Purpose.APP_ACCURACY, "Accuracy Mode");
        PURPOSE_STRING_HASH_MAP.put(Mission.Purpose.APP_BANLANCE, "Balance Mode");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRootViews();
        initPermission();
    }

    private void initPermission(){
        mPermissionList.clear();

        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }

        if (mPermissionList.size() > 0){
            requestPermissions(permissions, mRequestCode);
        } else {
            initAfterPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;
        if (mRequestCode == requestCode)
        {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED)
                    hasAllPermission = false;
            }
            if (!hasAllPermission){
                Snackbar.make(mStartButton,
                        "Permission required!!!",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Gotcha", v -> {initPermission();}).show();
            }
        }
        initAfterPermission();
    }

    private void initAfterPermission() {
        mEngine = new DemoEngineImpl(this);
        initMainPageView();
    }

    private void initRootViews(){
        setContentView(R.layout.activity_demo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mStartButton = findViewById(R.id.fab);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initMainPageView(){
        mInterpreterSpinner = findViewById(R.id.sp_interpreter);
        mDeviceSpinner = findViewById(R.id.sp_device);
        mModeSpinner = findViewById(R.id.sp_mode);
        mProgressBar = findViewById(R.id.pb_progress);
        mTimeSeekBar = findViewById(R.id.sb_time);
        mThreadSeekBar = findViewById(R.id.sb_thread);
        mTimeTextView = findViewById(R.id.tv_time);
        mThreadTextView = findViewById(R.id.tv_thread);

        mResultLayout = findViewById(R.id.ll_res);
        mResultLayout.setVisibility(View.GONE);
        mResultImage = findViewById(R.id.iv_img);
        mResultTextViewList = Arrays.asList(
                findViewById(R.id.tv_res1),
                findViewById(R.id.tv_res2),
                findViewById(R.id.tv_res3),
                findViewById(R.id.tv_res4),
                findViewById(R.id.tv_res5)
        );
        mMsgTextView = findViewById(R.id.tv_msg);

        ArrayAdapter<String> interpreterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mEngine.getInterpreterList());
        mInterpreterSpinner.setAdapter(interpreterAdapter);
        mInterpreterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mInterpreter = mEngine.getInterpreter(mEngine.getInterpreterList().get(position));
                ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(DemoActivity.this,
                        android.R.layout.simple_spinner_item,
                        mInterpreter
                                .getDevices()
                                .stream()
                                .map(DEVICE_STRING_HASH_MAP::get)
                                .collect(Collectors.toList()));
                mDeviceSpinner.setAdapter(deviceAdapter);
                mDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        if ("CPU".equals(((ArrayAdapter<String>) mDeviceSpinner.getAdapter())
                                .getItem(mDeviceSpinner.getSelectedItemPosition()))) {
                            mThreadTextView.getPaint().setFlags(0);
                            mThreadSeekBar.setEnabled(true);
                        } else {
                            mThreadSeekBar.setEnabled(false);
                            mThreadTextView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                            mThreadTextView.setText("Thread");
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) { }
                });

                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(DemoActivity.this,
                        android.R.layout.simple_spinner_item,
                        Arrays.asList("Performance Mode", "Accuracy Mode", "Balance Mode"));
                mModeSpinner.setAdapter(modeAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTimeTextView.setText(String.format("Time Limit \n%s ms/pic", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mTimeSeekBar.setMax(2000);
        mTimeSeekBar.setProgress(1000);

        mThreadSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mThreadTextView.setText(String.format("Thread: %d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mThreadSeekBar.setProgress(1);

        mStartButton.setOnClickListener(v -> {
            int time = mTimeSeekBar.getProgress();
            int threads = mThreadSeekBar.getProgress();
            String deviceName = ((ArrayAdapter<String>)mDeviceSpinner.getAdapter())
                    .getItem(mDeviceSpinner.getSelectedItemPosition());
            IInterpreter.Device device = DEVICE_STRING_HASH_MAP.inverse().get(deviceName);
            String modeName = ((ArrayAdapter<String>)mModeSpinner.getAdapter())
                    .getItem(mModeSpinner.getSelectedItemPosition());
            Mission.Purpose purpose = PURPOSE_STRING_HASH_MAP.inverse().get(modeName);

            if (time == 0 || device == null || (device == IInterpreter.Device.CPU && threads == 0) ||
                    mInterpreter == null || purpose == null){
                Snackbar.make(v, "Oops, sth's wrong...", Snackbar.LENGTH_SHORT).show();
                return;
            }

            mResultLayout.setVisibility(View.VISIBLE);
            Mission mission = mEngine.buildSmartSwitchMission(DemoActivity.this, mInterpreter, purpose, device, threads, time);
            mEngine.executeMission(mInterpreter, mission, DemoActivity.this);
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(DemoActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_demo) {
            // do nothing
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onMsg(String msg) {
        runOnUiThread(()-> mMsgTextView.setText(msg));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onProgress(int progress, Object... msgs) {
        runOnUiThread(() -> {
            // msg[0]: String, msg[1] Bitmap, msg[2] List<Recognition>
            //Log.i(TAG, "onProgress: " + progress);
            mProgressBar.setProgress(progress);
            if (msgs == null)
                return;

            String msg = (String)msgs[0];
            if (!msg.trim().equals("")) {
                Log.i(TAG, "onProgress: msg: " + msg);
                mMsgTextView.setText(msg);
            }

            if (msgs.length >= 3) {
                Bitmap img = (Bitmap) msgs[1];
                List<Recognition> resultList = (List<Recognition>) msgs[2];
                mResultImage.setImageBitmap(img);
                for (int i = 0; i < mResultTextViewList.size(); i++) {
                    Recognition result = resultList.get(i);
                    if (result != null) {
                        mResultTextViewList.get(i).setText(
                                String.format("%s    Prob: %.2f%%", result.getName(), result.getConfidence()*100));
                    } else {
                        mResultTextViewList.get(i).setText("");
                    }
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onFinish(int count, long enduredTime) {
        runOnUiThread(() -> Snackbar.make(mStartButton,
                String.format("Finished %d tasks in %d ms!", count ,enduredTime),
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Gotcha", v -> {}).show());
    }

    @Override
    public void onError(String msg) {
        runOnUiThread(() -> Snackbar.make(mStartButton, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Gotcha", v -> {}).show());
    }
}
