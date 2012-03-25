/*
* Copyright (C) 2012 Mozgin Dmitry
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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

/**
* Created: 25 March 2012
*
* @author <a href="mailto:flam44@gmail.com">Mozgin Dmitry</a>
* @version 1.0
*/

public class DemoActivity extends Activity
{
    private static final String TAG = "m039";

    List<File>      mFiles;
    ListView        mImages;

    Bitmap          mDefaultBitmap;
    Bitmap          mDefaultSkippingBitmap;

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

            initDefaultImages(parent.getWidth(), parent.getHeight());

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
                                                image.setImageBitmap(mDefaultSkippingBitmap);
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

        void initDefaultImages(int width, int height) {
            if (mDefaultBitmap == null) {
                mDefaultBitmap = BitmapUtils.createDebugImage("Loading", width, height);
            }

            if (mDefaultSkippingBitmap == null) {
                mDefaultSkippingBitmap = BitmapUtils.createDebugImage("Skipping", width, height);
            }
        }
    }

    void log() {
        Log.d(TAG, "mFiles:\n");

        for (File f : mFiles) {
            Log.d(TAG, "  " + f + "\n");
        }
    }
}
