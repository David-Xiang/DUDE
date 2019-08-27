package com.example.android.clientintelligent.interfaces;

import com.example.android.clientintelligent.IntelligentTask;

import java.util.List;

public interface IntelligentEngine {
    List<String> getInterpreterList();
    boolean addInterpreter(IntelligentInterpreter interpreter);
    IntelligentInterpreter getInterpreter(String interpreterName);
    boolean executeTask(String interpreterName, IntelligentTask task, ProgressListener progressListener); // 检查task和interpreter compatible
}
