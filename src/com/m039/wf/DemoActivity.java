package com.m039.wf;

import java.io.File;

import android.widget.TextView;

import android.app.Activity;
import android.os.Bundle;

public class DemoActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		TextView tv = (TextView) findViewById(R.id.tv);

		String out = "";

		for (File f : FileUtils.findFiles("/sdcard/Images", new String[] {"jpg"})) {
			out += f + "\n";
		}
		
		tv.setText("find: " + out);
    }
}
