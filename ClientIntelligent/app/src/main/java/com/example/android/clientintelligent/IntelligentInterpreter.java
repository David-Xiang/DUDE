package com.example.android.clientintelligent;

public interface IntelligentInterpreter {
    String getBackend();
    String getFramework();
    void executeTask(IntelligentTask task);
}
