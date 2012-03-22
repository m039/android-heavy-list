package com.m039.wf;

import android.view.ViewGroup;

import android.widget.ImageView;

import android.graphics.BitmapFactory;

import android.graphics.Bitmap;

import android.widget.LinearLayout;

import java.util.List;

import android.util.Log;

import java.io.File;

import android.widget.TextView;

import android.app.Activity;
import android.os.Bundle;

public class DemoActivity extends Activity
{
	private static final String TAG = "m039";
	
	List<File>		mFiles;
	LinearLayout	mImages;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		mImages = (LinearLayout) findViewById(R.id.images);
		
		mFiles = FileUtils.findFiles("/sdcard/Images", new String[] {"jpg"});

		new Thread() {
			public void run() {
				int count = mFiles.size();
				
				for (int i = 0; i < count; i++) {
					final ImageView iv = new ImageView(DemoActivity.this);

					iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
																  ViewGroup.LayoutParams.WRAP_CONTENT));
					
					Bitmap b = decodeImage(mFiles.get(i));
					iv.setImageBitmap(b);

					runOnUiThread(new Runnable() {
							public void run() {
								mImages.addView(iv);
							}
						});
				}
				
			}
		}.start();
		
		log();
    }

	/**
	 * @param file is path to the image
	 */
	Bitmap decodeImage(File file) {
		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inSampleSize = 8;
		
		Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		return b;
	}

	void log() {
		Log.d(TAG, "mFiles:\n");
		
		for (File f : mFiles) {
			Log.d(TAG, "  " + f + "\n");
		}	
	}
}
