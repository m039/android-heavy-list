package com.m039.wf;

import java.util.List;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;

import android.content.Context;

import android.view.Window;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

import android.app.Activity;

import android.util.Log;

import android.os.Message;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;


public class DemoActivity extends Activity
{
	private static final String TAG = "m039";

	List<File>      mFiles;
	ListView        mImages;
	Bitmap          mDefaultBitmap;

	Handler         mImageHandler = null;
	Handler         mBackgroundImageHandler = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initProgress();

		setContentView(R.layout.main);

		mImages = (ListView) findViewById(R.id.images);
		mFiles = FileUtils.findFiles("/sdcard/Images", new String[] {"jpg"});

		mImages.setAdapter(new MAdapter(this, R.layout.element, mFiles));

		initButtons();

		log();
	}

	@Override
	protected void onResume() {
		super.onResume();

		startThreads();

		((ArrayAdapter) mImages.getAdapter()).notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();

		stopThreads();
	}

	void initProgress() {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	}

	void initButtons() {
		Button b = (Button) findViewById(R.id.clear_cache);
		if (b != null)
			b.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CacheUtils.clear();

						((ArrayAdapter) mImages.getAdapter()).notifyDataSetChanged();
					}
				});
	}

	void startThreads() {
		startImageThread();
		startBackgroundImageThread();
	}

	void startImageThread() {
		new Thread() {
			public void run() {
				Looper.prepare();

				mImageHandler = new Handler() {
						public void handleMessage(Message msg) {
							Runnable r = msg.getCallback();

							if (r == null)
								return;

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

							if (r == null)
								return;

							r.run();
						}
					};

				Looper.loop();
			}
		}.start();
	}

	void stopThreads() {
		if (mImageHandler != null) {
			mImageHandler.getLooper().quit();
		}

		if (mBackgroundImageHandler != null) {
			mBackgroundImageHandler.getLooper().quit();
		}
	}

	class MAdapter extends ArrayAdapter<File> {
		MAdapter(Context c, int r, List<File> obj) {
			super(c, r, obj);
		}

		public View     getView(final int position,
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

			if (mImageHandler != null && mBackgroundImageHandler != null) {
				final ImageView image = iv;

				image.setTag(position);

				final File bfile = mFiles.get(position);
				final File cache = CacheUtils.find(bfile.getName());

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
												b.recycle();
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

								startProgress();

								if (cache.exists()) {
									b = CacheUtils.get(cache);
								} else {
									b = createBitmap();
								}

								Integer pos = (Integer) image.getTag();

								if (pos != null && pos != position) {
									if (!cache.exists()) {
										CacheUtils.put(b, cache);
									}

									b.recycle();

								} else {
									mImageHandler.post(new Runnable() {
											public void run() {
												runOnUiThread(new Runnable() {
														public void run() {
															image.setImageBitmap(b);
														}
													});
											}
										});

									if (!cache.exists()) {
										CacheUtils.put(b, cache);
									}
								}

								stopProgress();
							}

							Bitmap  createBitmap() {
								Bitmap bmp = BitmapUtils.decodeBitmap(bfile,
																	  parent.getWidth(),
																	  parent.getHeight());

								Bitmap scaled = BitmapUtils.createProportionalScaleBitmap(bmp,
																						  parent.getWidth(),
																						  parent.getHeight());

								bmp.recycle();

								return scaled;
							}

							void    startProgress() {
								runOnUiThread(new Runnable() {
										public void run() {
											setProgressBarIndeterminateVisibility(true);
										}
									});
							}

							void    stopProgress() {
								runOnUiThread(new Runnable() {
										public void run() {
											setProgressBarIndeterminateVisibility(false);
										}
									});
							}
						});
				}
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
