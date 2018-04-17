package com.abhi.toyswap.ImageLazyLoading;

import java.io.File;
import java.net.URLEncoder;

import android.content.Context;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context) {
		// Find the dir to save cached images
		/*
		 * if
		 * (android.os.Environment.getExternalStorageState().equals(android.os.
		 * Environment.MEDIA_MOUNTED)){ cacheDir=new
		 * File(Environment.getExternalStorageDirectory(),"NWE"); } else{
		 * cacheDir=context.getCacheDir(); }
		 */
		//cacheDir = new File(Environment.getExternalStorageDirectory(), "ToyApp");
		cacheDir=context.getCacheDir();
		 context.getCacheDir().deleteOnExit();
		// cacheDir=new File(Environment.getRootDirectory(),"NegiSportsApp");

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}

	}

	public File getFile(String url) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
		// String filename=String.valueOf(url.hashCode());
		// Another possible solution (thanks to grantland)
		String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename);
		return f;

	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}

}