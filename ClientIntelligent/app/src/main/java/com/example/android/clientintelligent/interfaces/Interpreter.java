package com.example.android.clientintelligent.interfaces;

import android.os.AsyncTask;

import com.example.android.clientintelligent.IntelligentTask;

import java.io.IOException;
import java.util.List;

public interface Interpreter {
    List<String> getDevices();
    String getFramework();
    AsyncTask buildTask(IntelligentTask task, ProgressListener progressListener) throws IOException;
}
