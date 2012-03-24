package com.m039.wf;

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
		mDefaultBitmap = decodeImage(mFiles.get(0));

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
			ImageView iv;

			if (convertView == null) {
				iv = new ImageView(DemoActivity.this);
				iv.setImageBitmap(mDefaultBitmap);
			} else {
				iv = (ImageView) convertView;
			}

			final ImageView image = iv;

			Thread t = (Thread) image.getTag();
			if (t != null)
				t.interrupt();

			t = new Thread() {
					public void run() {
						// File file = mFiles.get(position);

						// byte[] data = null;

						// try {
						// 	byte[] buffer = new byte[4096];

						// 	InputStream in = new BufferedInputStream(new FileInputStream(file));
						// 	ByteArrayOutputStream out = new ByteArrayOutputStream();

						// 	int number = 0;

						// 	while ( (number = in.read(buffer)) != -1) {
						// 		out.write(buffer, 0, buffer.length);

						// 		if (interrupted()) {
						// 			Log.d(TAG, " interrupt [1]");
						// 			return;
						// 		}
						// 	}

						// 	data = out.toByteArray();

						// } catch (Exception e) {
						// }

						// if (data == null)
						// 	return;

						BitmapFactory.Options options = new BitmapFactory.Options();

						options.inSampleSize = 8;

						// final Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length, options);

						Bitmap bmp = BitmapUtils.decodeBitmap(mFiles.get(position),
															  parent.getWidth(), parent.getHeight());

						Bitmap scaled = BitmapUtils.createProportionalScaleBitmap(bmp,
																				  parent.getWidth(),
																				  parent.getHeight());

						final Bitmap b = scaled;

						if (interrupted()) {
							Log.d(TAG, " interrupt [2]");
							return;
						}

						runOnUiThread(new Runnable() {
								public void run() {
									image.setImageBitmap(b);
								}
							});

						// the end
						bmp.recycle();						
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

			return iv;
		}
	}

	/**
	 * @param file is path to the image
	 */
	Bitmap decodeImage(File file) {
		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inSampleSize = 1;

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
