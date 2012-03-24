package com.m039.wf;

import android.graphics.Color;

import java.io.File;

import java.io.ByteArrayOutputStream;

import java.io.FileInputStream;

import java.io.BufferedInputStream;

import java.io.InputStream;

import java.io.BufferedInputStream;

import java.io.InputStream;

import android.widget.ArrayAdapter;

import android.content.Context;

import android.view.View;

import android.widget.BaseAdapter;

import android.widget.ListView;

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
	ListView		mImages;
	Bitmap			mDefaultBitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mImages = (ListView) findViewById(R.id.images);
		mFiles = FileUtils.findFiles("/sdcard/Images", new String[] {"jpg"});

		// new Thread() {
		// 	public void run() {
		// 		int count = mFiles.size();

		// 		for (int i = 0; i < count; i++) {
		// 			final ImageView iv = new ImageView(DemoActivity.this);

		// 			iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		// 														  ViewGroup.LayoutParams.WRAP_CONTENT));

		// 			Bitmap b = decodeImage(mFiles.get(i));
		// 			iv.setImageBitmap(b);

		// 			runOnUiThread(new Runnable() {
		// 					public void run() {
		// 						mImages.addView(iv);
		// 					}
		// 				});
		// 		}

		// 	}
		// }.start();

		mImages.setAdapter(new MAdapter(this, R.layout.element, mFiles));

		log();
	}

	static File ROOT = new File("/sdcard/ImageCache");

	static {
		if (ROOT.exists()) {
			FileUtils.delete(ROOT);
		}

		ROOT.mkdir();
	}

	class MAdapter extends ArrayAdapter<File> {
		int		COUNT		= 4;
		int		mIndex		= 0;
		Thread	mThreads[]  = new Thread[COUNT];

		MAdapter(Context c, int r, List<File> obj) {
			super(c, r, obj);
		}

		public View 	getView(final int position,
								View convertView,
								final ViewGroup parent) {

			if (mDefaultBitmap == null) {
				mDefaultBitmap = BitmapUtils.createDebugImage(parent.getWidth(), parent.getHeight());
			}

			ImageView iv;

			if (convertView == null) {
				iv = new ImageView(DemoActivity.this);
				iv.setImageBitmap(mDefaultBitmap); // necessary to set!
			} else {
				iv = (ImageView) convertView;
			}

			final ImageView image = iv;

			Thread t = (Thread) image.getTag();
			if (t != null)
				t.interrupt();


			final File bfile = mFiles.get(position);			
			final File cache = new File(ROOT, bfile.getName());

			if (cache.exists()) {
				Bitmap b;

				b = BitmapFactory.decodeFile(cache.getAbsolutePath());

				image.setImageBitmap(b);
			} else {
				t = new Thread() {
						public void run() {
							final Bitmap b;

							if (cache.exists()) {
								b = BitmapFactory.decodeFile(cache.getAbsolutePath());
							} else {
								Bitmap bmp = BitmapUtils.decodeBitmap(bfile,
																	  parent.getWidth(), parent.getHeight());

								Bitmap scaled = BitmapUtils.createProportionalScaleBitmap(bmp,
																						  parent.getWidth(),
																						  parent.getHeight());

								bmp.recycle();
								b = scaled;
							}

							if (interrupted()) {
								runOnUiThread(new Runnable() {
										public void run() {
											image.setImageBitmap(mDefaultBitmap);
										}
									});

								Log.d(TAG, " interrupt [2]");
							} else {
								runOnUiThread(new Runnable() {
										public void run() {
											image.setImageBitmap(b);
										}
									});
							}

							if (!cache.exists()) {
								BitmapUtils.saveBitmap(b, cache);
							}
						}
					};

				iv.setTag(t);

				// add thread to pool
				Thread prev = mThreads[mIndex];
				if (prev != null)
					prev.interrupt();

				mThreads[mIndex] = t;

				mIndex++;

				if (mIndex >= COUNT)
					mIndex = 0;

				t.start();
			}

			return iv;
		}
	}

	void log() {
		Log.d(TAG, "mFiles:\n");

		for (File f : mFiles) {
			Log.d(TAG, "  " + f + "\n");
		}
	}
}
