package com.samsung.test.audioproejct.file;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {

    private File getRoot() {
        File root = Environment.getExternalStorageDirectory();
        /**
         * directory make
         */
        File dir = new File(root, "AudioSamsungTest");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }
    public String getFilePath() {
        String extension = "mp4";

        String fileName = System.currentTimeMillis() +"_audio_test" + "." + extension;
        File dir = getRoot();

        File path = new File(dir, fileName);
        return path.getAbsolutePath();
    }

    public List<String> getFileList() {
        File root = getRoot();

        if (root.list() != null) {
            return Arrays.asList(root.list());
        } else {
            return new ArrayList<>();
        }
    }

    public List<File> getFilePathList() {
        File root = getRoot();

        if (root.listFiles() != null) {
            return Arrays.asList(root.listFiles());
        } else {
            return new ArrayList<>();
        }
    }
}
