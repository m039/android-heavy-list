package com.m039.wf;

import android.os.Message;

import android.os.Handler;


import android.os.Looper;

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

	Handler			mImageHandler = null;
	Handler			mBackgroundImageHandler = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mImages = (ListView) findViewById(R.id.images);
		mFiles = FileUtils.findFiles("/sdcard/Images", new String[] {"jpg"});

		mImages.setAdapter(new MAdapter(this, R.layout.element, mFiles));

		startImageThread();
		startBackgroundImageThread();

		log();
	}

	void startImageThread() {
		new Thread() {
			public void run() {
				Looper.prepare();

				mImageHandler = new Handler() {
						public void handleMessage(Message msg) {
							Runnable r = msg.getCallback();

							if (r != null)
								r.run();
						}
					};

				Looper.loop();
			}
		}.start();
	}

	void startBackgroundImageThread() {
		new Thread() {
			public void run() {
				Looper.prepare();

				mBackgroundImageHandler = new Handler() {
						public void handleMessage(Message msg) {
							Runnable r = msg.getCallback();

							if (r != null)
								r.run();
						}
					};

				Looper.loop();
			}
		}.start();
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

			image.setTag(position);

			final File bfile = mFiles.get(position);
			final File cache = new File(ROOT, bfile.getName());

			if (cache.exists()) {
				mImageHandler.post(new Runnable() {
						public void run() {
							final Bitmap b;

							b = BitmapFactory.decodeFile(cache.getAbsolutePath());

							runOnUiThread(new Runnable() {
									public void run() {
										Integer pos = (Integer) image.getTag();

										if (pos != null && pos != position) {
											image.setImageBitmap(mDefaultBitmap);
										} else {
											image.setImageBitmap(b);
										}
									}
								});
						}
					});
			} else {
				image.setImageBitmap(mDefaultBitmap);

				mBackgroundImageHandler.post(new Runnable() {
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

							mImageHandler.post(new Runnable() {
									public void run() {
										Integer pos = (Integer) image.getTag();

										if (pos != null && pos != position)
											return;

										runOnUiThread(new Runnable() {
												public void run() {
													image.setImageBitmap(b);
												}
											});
									}
								});

							if (!cache.exists()) {
								BitmapUtils.saveBitmap(b, cache);
							}
						}
					});

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
