package com.m039.wf;

import java.io.File;

import java.io.FilenameFilter;

import java.util.ArrayList;

import java.util.regex.Matcher;

import java.util.List;

import java.util.regex.Pattern;

public class FileUtils
{
	static List<File>		findFiles(String root, final String[] extensions) {
		final File file = new File(root);

		if (!file.isDirectory())
			return null;
		
		final FilenameFilter extf, dirf;

		extf = new FilenameFilter() {
				Pattern p;

				{
					p = Pattern.compile("\\.(" + join(extensions, "|") + ")$");
				}

				public boolean accept (File dir, String filename) {
					return p.matcher(filename).find();
				}
			};

		dirf = new FilenameFilter() {
				public boolean accept (File dir, String filename) {
					return new File(dir, filename).isDirectory();
				}
			};


		final List<File> res = new ArrayList<File>();

		new Runnable() {
			public void run() {
				add(file);
			}

			void add(File root) {
				for (File f : root.listFiles(extf)) {
					res.add(f);
				}

				for (File d : root.listFiles(dirf)) {
					add(d);
				}
			}			
		}.run();

		return res;
	}

	static boolean			debug() {
		// String test = "Hello world.jpg";
		// Matcher m = p.matcher(test);

		// if (m.find())
		// 	return true;

		return false;
	}

	static String			join(String [] strs, String separator) {
		if (strs == null || strs.length <= 0)
			return null;

		if (strs.length == 1)
			return strs[0];

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < strs.length - 1; i++) {
			sb.append(strs[i]);
			sb.append(separator);
		}

		sb.append(strs[strs.length - 1]);

		return sb.toString();
	}

	/**
	 * Taken from SO
	 */   
	static void delete(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				delete(child);

		fileOrDirectory.delete();
	}
	
}