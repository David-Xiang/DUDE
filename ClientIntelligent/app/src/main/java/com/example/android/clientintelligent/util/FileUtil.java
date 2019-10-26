package com.example.android.clientintelligent.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    private static String commonDir = "/CIBench";
    public static void copyAssetResource2File(Context context, String assetsFile, String outFile) throws IOException {
        InputStream is = context.getAssets().open(assetsFile);
        File outF = new File(outFile);
        FileOutputStream fos = new FileOutputStream(outF);

        int byteCount;
        byte[] buffer = new byte[1024];
        while ((byteCount = is.read(buffer)) != -1) {
            fos.write(buffer, 0, byteCount);
        }
        fos.flush();
        is.close();
        fos.close();
    }

    public static void copyExternalResource2File(String externalFile, String outFile) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory(), commonDir+externalFile);
        InputStream is = new FileInputStream(file);

        File outF = new File(outFile);
        FileOutputStream fos = new FileOutputStream(outF);

        int byteCount;
        byte[] buffer = new byte[1024];
        while ((byteCount = is.read(buffer)) != -1) {
            fos.write(buffer, 0, byteCount);
        }
        fos.flush();
        is.close();
        fos.close();
    }

    public static InputStream getExternalResourceInputStream(String externalFile) throws IOException {
        return new FileInputStream(new File(Environment.getExternalStorageDirectory(), commonDir+externalFile));
    }
}
