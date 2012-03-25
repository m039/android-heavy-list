package com.m039.wf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheUtils {
    static File ROOT = new File("/sdcard/ImageCache");

    static {
        ROOT.mkdir();
    }

    static void     clear() {
        if (ROOT.exists()) {
            FileUtils.delete(ROOT);
        }

        ROOT.mkdir();
    }

    static File     find(String name) {
        return new File(ROOT, name);
    }

    static Bitmap   get(File cache) {
        return BitmapFactory.decodeFile(cache.getAbsolutePath());
    }

    static void     put(Bitmap b, File cache) {
        BitmapUtils.saveBitmap(b, cache);
    }
}