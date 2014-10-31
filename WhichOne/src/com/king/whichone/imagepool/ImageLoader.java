package com.king.whichone.imagepool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	// 线程池
	ExecutorService executorService;
	Context mContext;

	public ImageLoader(Context context) {
		mContext = context;
		executorService = Executors.newFixedThreadPool(10);
	}

	// 当进入listview时默认的图片，可换成你自己的默认图片
	// final int stub_id = R.drawable.template_default;

	// 最主要的方法
	@SuppressWarnings("deprecation")
	public void DisplayImage(String url, ImageView imageView, int stub_id,
			int width) {
		imageViews.put(imageView, url);
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
			// imageView.setBackgroundResource(bitmap);
			imageView.setBackgroundDrawable(new BitmapDrawable(mContext
					.getResources(), bitmap));
		else {
			// 若没有的话则开启新线程加载图片
			queuePhoto(url, imageView,width);
			imageView.setBackgroundResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView,int width) {
		PhotoToLoad p = new PhotoToLoad(url, imageView,width);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url,int width) {
		File f = new File(url);

		// 先从文件缓存中查找是否有
		Bitmap b = decodeFile(f,width);
		if (b != null)
			return b;
		else
			return null;

		// 最后从指定的url中下载图片
		// try {
		// Bitmap bitmap = null;
		// URL imageUrl = new URL(url);
		// HttpURLConnection conn = (HttpURLConnection) imageUrl
		// .openConnection();
		// conn.setConnectTimeout(30000);
		// conn.setReadTimeout(30000);
		// conn.setInstanceFollowRedirects(true);
		// InputStream is = conn.getInputStream();
		// OutputStream os = new FileOutputStream(f);
		// CopyStream(is, os);
		// os.close();
		// bitmap = decodeFile(f);
		// return bitmap;
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// return null;
		// }
	}

	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
	private Bitmap decodeFile(File f,int width) {
		Bitmap newBitmap = null;
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream = new FileInputStream(f);
			if (stream != null) {
				BitmapFactory.decodeStream(stream, null, o);
				try {
					if (stream != null) {
						stream.close();
						stream = null;
					}
				} catch (IOException e) {
				}
			}

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < width
						|| height_tmp / 2 < width)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			stream = new FileInputStream(f);
			newBitmap = BitmapFactory.decodeStream(stream, null, o2);
			try {
				if (stream != null) {
					stream.close();
					stream = null;
				}
			} catch (IOException e) {
			}

		} catch (FileNotFoundException e) {
		}
		return newBitmap;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public int width;

		public PhotoToLoad(String u, ImageView i,int width) {
			url = u;
			imageView = i;
			this.width = width;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url,photoToLoad.width);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// 更新的操作放在UI线程中
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	/**
	 * 防止图片错位
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// 用于在UI线程中更新界面
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			// if (bitmap != null)
			// photoToLoad.imageView.setImageBitmap(bitmap);
			// else
			// photoToLoad.imageView.setImageResource(stub_id);
			if (bitmap != null)
				photoToLoad.imageView.setBackgroundDrawable(new BitmapDrawable(
						mContext.getResources(), bitmap));
			// else
			// photoToLoad.imageView.setBackgroundResource(stub_id);

		}
	}

	public void clearCache() {
		memoryCache.clear();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}