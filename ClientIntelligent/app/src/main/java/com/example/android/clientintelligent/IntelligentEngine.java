package com.example.android.clientintelligent;

import java.util.List;

public interface IntelligentEngine {
    List<String> getInterpreterList();
    boolean addInterpreter(IntelligentInterpreter interpreter);

    boolean executeTask(String interpreterName, IntelligentTask task); // 检查task和interpreter compatible
}
