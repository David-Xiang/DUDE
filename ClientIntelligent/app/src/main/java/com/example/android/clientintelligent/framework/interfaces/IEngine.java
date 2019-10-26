package com.example.android.clientintelligent.framework.interfaces;

import android.content.Context;

import com.example.android.clientintelligent.framework.pojo.DataSet;
import com.example.android.clientintelligent.framework.pojo.Mission;
import com.example.android.clientintelligent.framework.pojo.Model;

import java.util.List;

public interface IEngine {
    List<String> getInterpreterList();
    Context getContext();
    void addInterpreter(IInterpreter interpreter);
    IInterpreter getInterpreter(String interpreterName);
    void executeMission(IInterpreter interpreter, Mission mission,
                        IProgressListener progressListener);
    Mission buildMission(Context context, Model model, DataSet dataSet, Mission.Purpose purpose,
                         IInterpreter.Device device, int threads, int timeLimit);
}
