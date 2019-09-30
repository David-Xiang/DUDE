package com.example.android.clientintelligent;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.Model;
import com.example.android.clientintelligent.framework.interfaces.IEngine;
import com.example.android.clientintelligent.framework.interfaces.IInterpreter;
import com.example.android.clientintelligent.framework.interfaces.IProgressListener;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IProgressListener {
    private static final String TAG = "MainActivity";
    private static final BiMap<IInterpreter.Device, String> DEVICE_STRING_HASH_MAP;

    private IEngine mEngine;
    private IInterpreter mInterpreter;
    private List<String> mOriginModelPathList;

    private Spinner mInterpreterSpinner;
    private Spinner mDeviceSpinner;
    private Spinner mModelSpinner;
    private ProgressBar mProgressBar;
    private SeekBar mTimeSeekBar;
    private SeekBar mThreadSeekBar;
    private TextView mTimeTextView;
    private TextView mThreadTextView;
    private TextView mPurposeTextView;
    private Switch mSwitch;
    private FloatingActionButton mStartButton;

    static {
        DEVICE_STRING_HASH_MAP = HashBiMap.create();
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.CPU, "CPU");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.GPU, "GPU");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.NNAPI, "NNAPI");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.VULKAN, "VULKAN");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.OPENCL, "OPENCL");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.OPENGL, "OPENGL");
        DEVICE_STRING_HASH_MAP.put(IInterpreter.Device.WEBGL, "WEBGL");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRootViews();

        mEngine = new EngineImpl(this);
        initMainPageView();
    }

    private void initRootViews(){
        setContentView(R.layout.activity_main);
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

    @SuppressLint("SetTextI18n")
    private void initMainPageView(){
        mInterpreterSpinner = findViewById(R.id.sp_interpreter);
        mDeviceSpinner = findViewById(R.id.sp_device);
        mModelSpinner = findViewById(R.id.sp_model);
        mProgressBar = findViewById(R.id.pb_progress);
        mTimeSeekBar = findViewById(R.id.sb_time);
        mThreadSeekBar = findViewById(R.id.sb_thread);
        mTimeTextView = findViewById(R.id.tv_time);
        mThreadTextView = findViewById(R.id.tv_thread);
        mPurposeTextView = findViewById(R.id.tv_purpose_text);
        mSwitch = findViewById(R.id.switch_purpose);


        ArrayAdapter<String> interpreterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mEngine.getInterpreterList());
        mInterpreterSpinner.setAdapter(interpreterAdapter);
        mInterpreterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mInterpreter = mEngine.getInterpreter(mEngine.getInterpreterList().get(position));
                ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(MainActivity.this,
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


                mOriginModelPathList = mInterpreter.getModels();
                ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        mOriginModelPathList
                                .stream()
                                .map(s->s.substring(
                                            s.lastIndexOf("/")+1,
                                            s.lastIndexOf("."))
                                    )
                                .collect(Collectors.toList()));
                mModelSpinner.setAdapter(modelAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Model model = mInterpreter.getModel(
                        mOriginModelPathList.get(
                                mModelSpinner.getSelectedItemPosition()));
                if (TextUtils.isEmpty(model.getTrueLabelIndexPath())) {
                    mSwitch.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTimeTextView.setText(String.format("Time: %ds", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mTimeSeekBar.setMax(30000);
        mTimeSeekBar.setProgress(30000);

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

        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPurposeTextView.setText("Accuracy");
            } else {
                mPurposeTextView.setText("Performance");
            }
        });
        mSwitch.setChecked(true);

        mStartButton.setOnClickListener(v -> {
            int time = mTimeSeekBar.getProgress();
            int threads = mThreadSeekBar.getProgress();
            String deviceName = ((ArrayAdapter<String>)mDeviceSpinner.getAdapter())
                    .getItem(mDeviceSpinner.getSelectedItemPosition());
            IInterpreter.Device device = DEVICE_STRING_HASH_MAP.inverse().get(deviceName);
            Model model = mInterpreter.getModel(
                                        mOriginModelPathList.get(
                                            mModelSpinner.getSelectedItemPosition()));

            if (time == 0 || device == null || (device == IInterpreter.Device.CPU && threads == 0) ||
                    mInterpreter == null || model == null){
                Snackbar.make(v, "Oops, sth's wrong...", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Mission.Purpose purpose = Mission.Purpose.PERFORMANCE;
            if (mPurposeTextView.getText().equals("Accuracy")){
                purpose = Mission.Purpose.ACCURACY;
            }
            Mission mission = new Mission(
                    MainActivity.this, model, purpose, device, threads, time);
            mEngine.executeTask(mInterpreter, mission, MainActivity.this);
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Enter");
    }

    @Override
    public synchronized void onPause() {
        Log.d(TAG, "onPause: Enter");

        super.onPause();
    }

    @Override
    public void onProgress(int progress, String msg) {
        mProgressBar.setProgress(progress);
        if (msg != null){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onFinish(int count, long enduredTime) {
        Snackbar.make(mStartButton,
                String.format("Finished %d tasks in %d ms!", count ,enduredTime),
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Gotcha", v -> {}).show();
    }

    @Override
    public void onError(String msg) {
        Snackbar.make(mStartButton, msg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Gotcha", v -> {}).show();
    }

}
