package com.example.android.clientintelligent.framework.interfaces;

import android.content.Context;

import com.example.android.clientintelligent.framework.Mission;

import java.util.List;

public interface IEngine {
    List<String> getInterpreterList();
    Context getContext();
    boolean addInterpreter(IInterpreter interpreter);
    IInterpreter getInterpreter(String interpreterName);
    boolean executeTask(IInterpreter interpreter, Mission task,
                        IProgressListener progressListener); // 检查task和interpreter compatible
}
