package com.example.android.clientintelligent;

import java.util.List;

public interface IntelligentInterpreter {
    List<String> getBackends();
    String getFramework();
    void executeTask(IntelligentTask task);
}
