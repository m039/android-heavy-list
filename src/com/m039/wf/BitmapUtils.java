package com.m039.wf;

import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.File;

public class BitmapUtils {
    private static final String TAG = "m039";

    static Bitmap   createDebugImage(int width, int height) {
        Bitmap b =  Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();

        p.setColor(Color.WHITE);

        Canvas c = new Canvas(b);
        c.drawColor(Color.BLACK);

        String text = "Loading";

        c.drawText(text, width / 2 - p.measureText(text) / 2 , height / 2, p);

        return b;
    }

    static BitmapFactory.Options decodeBitmapBounds(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return options;
    }

    static Bitmap decodeBitmap(File file, int maxWidthHint, int maxHeightHint) {
        BitmapFactory.Options options;

        options = decodeBitmapBounds(file);

        int bw = options.outWidth;
        int bh = options.outHeight;

        int dw = bw / maxWidthHint;
        int dh = bh / maxHeightHint;

        int dmax = Math.max(dw, dh);

        options = null;

        if (dmax > 1) {
            options = new BitmapFactory.Options();
            options.inSampleSize = dmax;
        }

        Bitmap bout;

        if (options != null)
            bout = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        else
            bout = BitmapFactory.decodeFile(file.getAbsolutePath());

        Log.d(TAG, "decodeBitmap {\n" +
              "  file:" + file + "\n" +
              "  maxWidthHint : " + maxWidthHint + "\n" +
              "  maxHeightHint : " + maxHeightHint + "\n" +
              "  bw : " + bw + "\n" +
              "  bh : " + bh + "\n" +
              "  dw : " + dw + "\n" +
              "  dh : " + dh + "\n" +
              "  dmax : " + dmax + "\n" +
              "}");

        return bout;
    }

    static Bitmap createProportionalScaleBitmap(Bitmap src, int toWidth, int toHeight) {
        float sw = src.getWidth();
        float sh = src.getHeight();

        float dw = toWidth / sw;
        float dh = toHeight / sh;

        float d = Math.min(dw, dh);

        Log.d(TAG, "createProportionalScaleBitmap {\n" +
              "  sw :" + sw + "\n" +
              "  sh : " + sh + "\n" +
              "  dw : " + dw + "\n" +
              "  dh : " + dh + "\n" +
              "  d : " + d + "\n" +
              "}");

        return Bitmap.createScaledBitmap(src, (int) (sw * d), (int) (sh * d), false);
    }

    /**
     * Taken from SO
     */
    static void saveBitmap(Bitmap src, File location) {
        try {
            FileOutputStream out = new FileOutputStream(location);
            src.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (Exception e) {
            Log.e(TAG, "saveBitmap", e);
        }

        Log.d(TAG, "Bitmap saved to " + location);
    }
}