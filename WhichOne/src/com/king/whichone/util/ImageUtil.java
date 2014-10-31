package com.king.whichone.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;

public class ImageUtil {
	/**
	 * 从指定目录下获取RGB_565的bitmap对象,以此减少内存占用
	 * 
	 * @param path
	 *            文件名
	 * @return
	 */
	public static Bitmap createBitmap(String path) {

		Options opts = new Options();

		opts.inJustDecodeBounds = false;
		opts.inDither = false;
		opts.inPreferredConfig = Bitmap.Config.RGB_565;

		return BitmapFactory.decodeFile(path, opts);
	}

	// 获取指定宽度的图片，高度进行相应的缩放
	public static Bitmap createBitmap(String filePath, final int widthTo) {
		if (filePath == null || widthTo <= 0) {
			return null;
		}
		int thumbnailHeight;
		Bitmap bitmap = null;
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		thumbnailHeight = (int) (((float) widthTo) / options.outWidth * options.outHeight);
		int m = options.outWidth > options.outHeight ? options.outWidth
				: options.outHeight;
		options.inSampleSize = (m + widthTo - 1) / widthTo;
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		bitmap = BitmapFactory.decodeFile(filePath, options);
		if (null == bitmap)
			return null;
		int rotation = getExifOrientation(filePath);
		if (rotation != 0) {
			bitmap = rotate(bitmap, rotation);
		}
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();

		int focusX = width / 2;
		int focusY = height / 2;
		int cropX;
		int cropY;
		int cropWidth;
		int cropHeight;
		if (widthTo * height < thumbnailHeight * width) {
			// Vertically constrained.
			cropWidth = widthTo * height / thumbnailHeight;
			cropX = Math.max(0,
					Math.min(focusX - cropWidth / 2, width - cropWidth));
			cropY = 0;
			cropHeight = height;
		} else {
			// Horizontally constrained.
			cropHeight = thumbnailHeight * width / widthTo;
			cropY = Math.max(0,
					Math.min(focusY - cropHeight / 2, height - cropHeight));
			cropX = 0;
			cropWidth = width;
		}
		final Bitmap finalBitmap = Bitmap.createBitmap(widthTo,
				thumbnailHeight, Bitmap.Config.RGB_565);// RGB_565
		final Canvas canvas = new Canvas(finalBitmap);
		final Paint paint = new Paint();
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawColor(0);
		canvas.drawBitmap(bitmap, new Rect(cropX, cropY, cropX + cropWidth,
				cropY + cropHeight), new Rect(0, 0, widthTo, thumbnailHeight),
				paint);
		bitmap.recycle();
		// tempBitmap.recycle();
		return finalBitmap;
	}

	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}

			}
		}
		return degree;
	}

	// Rotates the bitmap by the specified degree.
	// If a new bitmap is created, the original bitmap is recycled.
	public static Bitmap rotate(Bitmap b, int degrees) {
		return rotateAndMirror(b, degrees, false, false);
	}

	// Rotates the bitmap by the specified degree.
	// If a new bitmap is created, the original bitmap is recycled.
	public static Bitmap rotate(Bitmap b, int degrees, boolean needRGBA) {
		return rotateAndMirror(b, degrees, false, needRGBA);
	}

	// Rotates and/or mirrors the bitmap. If a new bitmap is created, the
	// original bitmap is recycled.
	public static Bitmap rotateAndMirror(Bitmap b, int degrees, boolean mirror,
			boolean needRgba) {
		if ((degrees != 0 || mirror) && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			if (mirror) {
				m.postScale(-1, 1);
				degrees = (degrees + 360) % 360;
				if (degrees == 0 || degrees == 180) {
					m.postTranslate((float) b.getWidth(), 0);
				} else if (degrees == 90 || degrees == 270) {
					m.postTranslate((float) b.getHeight(), 0);
				} else {
					throw new IllegalArgumentException("Invalid degrees="
							+ degrees);
				}
			}

			m.rectStaysRect();
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
				if (android.os.Build.VERSION.SDK_INT <= 10 && needRgba) {
					if (b.getConfig() == Bitmap.Config.RGB_565) {
						Bitmap b3 = b.copy(Bitmap.Config.ARGB_8888, true);
						if (b != b3) {
							b.recycle();
							b = b3;
						}
					}
				}

			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
			}
		}
		return b;
	}

	public static boolean saveBmp(Bitmap bitmap, String name) {
		if (bitmap == null || bitmap.isRecycled()) {
			return false;
		}
		try {
			File pf = new File(name);
			if (!pf.exists())
				pf.createNewFile();
			else
				pf.delete();
			FileOutputStream stream;
			stream = new FileOutputStream(pf);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			stream.flush();
			stream.close();
			return true;
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// return false;

	}

	/**
	 * 将dip转换为px
	 * 
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 将px转换为dip
	 * 
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static byte[] bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
		return baos.toByteArray();
	}

	public static Bitmap createThumbnail(Bitmap orginalBitmap,
			final int thumbnailWidth, final int thumbnailHeight) {
		if (orginalBitmap == null || orginalBitmap.isRecycled()) {
			return null;
		}
		// Bitmap bitmap = orginalBitmap;

		final int width = orginalBitmap.getWidth();
		final int height = orginalBitmap.getHeight();

		int focusX = width / 2;
		int focusY = height / 2;
		int cropX;
		int cropY;
		int cropWidth;
		int cropHeight;
		if (thumbnailWidth * height < thumbnailHeight * width) {
			// Vertically constrained.
			cropWidth = thumbnailWidth * height / thumbnailHeight;
			cropX = Math.max(0,
					Math.min(focusX - cropWidth / 2, width - cropWidth));
			cropY = 0;
			cropHeight = height;
		} else {
			// Horizontally constrained.
			cropHeight = thumbnailHeight * width / thumbnailWidth;
			cropY = Math.max(0,
					Math.min(focusY - cropHeight / 2, height - cropHeight));
			cropX = 0;
			cropWidth = width;
		}
		final Bitmap finalBitmap = Bitmap.createBitmap(thumbnailWidth,
				thumbnailHeight, Bitmap.Config.RGB_565);// RGB_565
		final Canvas canvas = new Canvas(finalBitmap);
		final Paint paint = new Paint();
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawColor(0);
		canvas.drawBitmap(orginalBitmap, new Rect(cropX, cropY, cropX
				+ cropWidth, cropY + cropHeight), new Rect(0, 0,
				thumbnailWidth, thumbnailHeight), paint);
		orginalBitmap.recycle();
		// tempBitmap.recycle();
		return finalBitmap;
	}

	public static Bitmap createThumbnail(Bitmap orginalBitmap,
			final int thumbnailWidth, final int thumbnailHeight,
			boolean isRecyled) {
		if (orginalBitmap == null || orginalBitmap.isRecycled()) {
			return null;
		}
		// Bitmap bitmap = orginalBitmap;

		final int width = orginalBitmap.getWidth();
		final int height = orginalBitmap.getHeight();

		int focusX = width / 2;
		int focusY = height / 2;
		int cropX;
		int cropY;
		int cropWidth;
		int cropHeight;
		if (thumbnailWidth * height < thumbnailHeight * width) {
			// Vertically constrained.
			cropWidth = thumbnailWidth * height / thumbnailHeight;
			cropX = Math.max(0,
					Math.min(focusX - cropWidth / 2, width - cropWidth));
			cropY = 0;
			cropHeight = height;
		} else {
			// Horizontally constrained.
			cropHeight = thumbnailHeight * width / thumbnailWidth;
			cropY = Math.max(0,
					Math.min(focusY - cropHeight / 2, height - cropHeight));
			cropX = 0;
			cropWidth = width;
		}
		final Bitmap finalBitmap = Bitmap.createBitmap(thumbnailWidth,
				thumbnailHeight, Bitmap.Config.RGB_565);// RGB_565
		final Canvas canvas = new Canvas(finalBitmap);
		final Paint paint = new Paint();
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawColor(0);
		canvas.drawBitmap(orginalBitmap, new Rect(cropX, cropY, cropX
				+ cropWidth, cropY + cropHeight), new Rect(0, 0,
				thumbnailWidth, thumbnailHeight), paint);
		if (isRecyled) {
			orginalBitmap.recycle();
		}

		// tempBitmap.recycle();
		return finalBitmap;
	}

	public static Bitmap createThumbnail(String filePath,
			final int thumbnailWidth, final int thumbnailHeight) {
		if (filePath == null || thumbnailWidth <= 0 || thumbnailHeight <= 0) {
			return null;
		}

		Bitmap bitmap = null;

		// decode the image file
		if (bitmap == null) {
			// String filePath = file.mName;
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			int m = options.outWidth > options.outHeight ? options.outWidth
					: options.outHeight;
			options.inSampleSize = (m + thumbnailWidth - 1) / thumbnailWidth;
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			bitmap = BitmapFactory.decodeFile(filePath, options);
			if (null == bitmap)
				return null;
		}
		int rotation = getExifOrientation(filePath);
		if (rotation != 0) {
			bitmap = rotate(bitmap, rotation);
		}

		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();

		int focusX = width / 2;
		int focusY = height / 2;
		int cropX;
		int cropY;
		int cropWidth;
		int cropHeight;
		if (thumbnailWidth * height < thumbnailHeight * width) {
			// Vertically constrained.
			cropWidth = thumbnailWidth * height / thumbnailHeight;
			cropX = Math.max(0,
					Math.min(focusX - cropWidth / 2, width - cropWidth));
			cropY = 0;
			cropHeight = height;
		} else {
			// Horizontally constrained.
			cropHeight = thumbnailHeight * width / thumbnailWidth;
			cropY = Math.max(0,
					Math.min(focusY - cropHeight / 2, height - cropHeight));
			cropX = 0;
			cropWidth = width;
		}
		final Bitmap finalBitmap = Bitmap.createBitmap(thumbnailWidth,
				thumbnailHeight, Bitmap.Config.RGB_565);// RGB_565
		final Canvas canvas = new Canvas(finalBitmap);
		final Paint paint = new Paint();
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawColor(0);
		canvas.drawBitmap(bitmap, new Rect(cropX, cropY, cropX + cropWidth,
				cropY + cropHeight), new Rect(0, 0, thumbnailWidth,
				thumbnailHeight), paint);
		bitmap.recycle();
		// tempBitmap.recycle();
		return finalBitmap;
	}
}
