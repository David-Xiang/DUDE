package com.example.android.clientintelligent.framework.interfaces;

import android.content.Context;

import com.example.android.clientintelligent.framework.Data;
import com.example.android.clientintelligent.framework.Mission;
import com.example.android.clientintelligent.framework.Model;

import java.util.List;

public interface IEngine {
    List<String> getInterpreterList();
    Context getContext();
    void addInterpreter(IInterpreter interpreter);
    IInterpreter getInterpreter(String interpreterName);
    void executeMission(IInterpreter interpreter, Mission mission,
                        IProgressListener progressListener);
    Mission buildMission(Context context, Model model, Data data, Mission.Purpose purpose,
                         IInterpreter.Device device, int threads, int timeLimit);
}
