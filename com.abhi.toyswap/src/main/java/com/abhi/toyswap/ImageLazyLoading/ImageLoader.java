package com.abhi.toyswap.ImageLazyLoading;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.abhi.toyswap.R;
import com.abhi.toyswap.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    private Context appMainContext;

    Handler handler = new Handler();//handler to display images in UI thread
    private boolean isHighResolution;

    private ProgressBar progressBar;

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        appMainContext = context;
        executorService = Executors.newFixedThreadPool(5);
    }

    //final int stub_id=R.drawable.loading_icon;
    public void DisplayImage(String url, ImageView imageView, boolean isHighResolution) {
            this.isHighResolution = isHighResolution;
            imageViews.put(imageView, url);
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.refreshDrawableState();
            } else {
                queuePhoto(url, imageView,null);
                imageView.setImageResource(R.drawable.loading_icon_big);
            }

    }

    public void DisplayImage(String url, ImageView imageView, boolean isHighResolution, ProgressBar progressBar) {
        this.isHighResolution = isHighResolution;
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        this.progressBar = progressBar;
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.refreshDrawableState();
            progressBar.setVisibility(View.GONE);
        } else {
            queuePhoto(url, imageView,progressBar);
            // imageView.setImageResource(R.drawable.loading_icon_big);
        }
    }

    private void queuePhoto(String url, ImageView imageView,ProgressBar progressBar) {
        PhotoToLoad p = new PhotoToLoad(url, imageView,progressBar);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);
        //from SD cache
        Bitmap b = decodeFile(f);
        if (b != null)
            return b;

        //from web
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            appMainContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));

            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {


            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_WIDTH_SIZE;
            final int REQUIRED_HEIGHT_SIZE;

            if (isHighResolution) {
                REQUIRED_WIDTH_SIZE = 600;
                REQUIRED_HEIGHT_SIZE = 450;
            } else {
                REQUIRED_WIDTH_SIZE = 120;
                REQUIRED_HEIGHT_SIZE = 120;

            }
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_WIDTH_SIZE || height_tmp / 2 < REQUIRED_HEIGHT_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public ProgressBar progressBar;

        public PhotoToLoad(String u, ImageView i,ProgressBar pb) {
            url = u;
            imageView = i;
           progressBar=pb;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad)) {
                return;
            }

            if (bitmap != null) {

                photoToLoad.imageView.setImageBitmap(bitmap);
                photoToLoad.imageView.refreshDrawableState();
            } else {
                photoToLoad.imageView.setImageBitmap(null);
            }
            if ( photoToLoad.progressBar != null) {
                photoToLoad.progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
