package com.elex.chatservice.view.allianceshare.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class ImageDispalyManager
{
	private static ImageDispalyManager				mInstance		= null;
	private Context									context;
	private static final int						THUMB_WIDTH		= 256;
	private static final int						THUMB_HEIGHT	= 256;
	private int										mScreenWidth;
	private int										mScreenHeight;
	public Handler									handler			= new Handler();
	public final String								TAG				= getClass().getSimpleName();
	private HashMap<String, SoftReference<Bitmap>>	imageCache		= new HashMap<String, SoftReference<Bitmap>>();
	private ExecutorService		executorService			= null;

	public static ImageDispalyManager getInstance(Context context)
	{
		if (mInstance == null)
		{
			synchronized (ImageDispalyManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new ImageDispalyManager(context);
				}
			}
		}
		return mInstance;
	}

	private ImageDispalyManager(Context context)
	{
		if (context.getApplicationContext() != null)
			this.context = context.getApplicationContext();
		else
			this.context = context;

		DisplayMetrics dm = new DisplayMetrics();
		dm = this.context.getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;
		executorService = Executors.newFixedThreadPool(2);
	}

	public void put(String key, Bitmap bmp)
	{
		if (StringUtils.isNotEmpty(key) && bmp != null)
		{
			imageCache.put(key, new SoftReference<Bitmap>(bmp));
		}
	}

	public void displayBmp(final ImageView imageView, final String thumbPath, final String sourcePath)
	{
		displayBmp(imageView, thumbPath, sourcePath, true);
	}

	public void displayBmp(final ImageView imageView, final String thumbPath, final String sourcePath, final boolean showThumb)
	{
		if (StringUtils.isEmpty(thumbPath) && StringUtils.isEmpty(sourcePath))
		{
			return;
		}

		if (imageView.getTag() != null && imageView.getTag().equals(sourcePath))
		{
			return;
		}

//		showDefault(imageView);

		final String path;
		if (StringUtils.isNotEmpty(thumbPath) && showThumb)
		{
			path = thumbPath;
		}
		else if (StringUtils.isNotEmpty(sourcePath))
		{
			path = sourcePath;
		}
		else
		{
			return;
		}

		imageView.setTag(path);

		if (imageCache.containsKey(showThumb ? path + THUMB_WIDTH + THUMB_HEIGHT : path))
		{
			SoftReference<Bitmap> reference = imageCache.get(showThumb ? path + THUMB_WIDTH + THUMB_HEIGHT : path);
			// 可以用LruCahche会好些
			if(reference!=null)
			{
				Bitmap imgInCache = reference.get();
				if (imgInCache != null)
				{
					refreshView(imageView, imgInCache, path);
					return;
				}
			}
		}
		imageView.setImageBitmap(null);

		if (executorService != null)
			executorService.execute(new Runnable()
			{
				
				@Override
				public void run()
				{
					Bitmap	bitmap = null;
					try
					{
						if (path != null && path.equals(thumbPath))
						{
							bitmap = BitmapFactory.decodeFile(path);
						}
						if (bitmap == null)
						{
							bitmap = compressImage(sourcePath, showThumb);
						}
					}
					catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					if (bitmap != null)
					{
						put(showThumb ? path + THUMB_WIDTH + THUMB_HEIGHT : path, bitmap);
					}
					
					final Bitmap mBitmap = bitmap;
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							refreshView(imageView, mBitmap, path);
						}
					});
				}
			});
	}

	private void refreshView(ImageView imageView, Bitmap bitmap, String path)
	{
		if (imageView != null && bitmap != null)
		{
			if (path != null)
			{
				((ImageView) imageView).setImageBitmap(bitmap);
				imageView.setTag(path);
			}
		}
	}

	private void showDefault(ImageView imageView)
	{
		imageView.setBackgroundResource(R.drawable.bg_img);
	}

	public Bitmap compressImage(String path, boolean showThumb) throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, opt);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		if (showThumb)
		{
			while (true)
			{
				if ((opt.outWidth >> i <= THUMB_WIDTH) && (opt.outHeight >> i <= THUMB_HEIGHT))
				{
					in = new BufferedInputStream(new FileInputStream(new File(path)));
					opt.inSampleSize = (int) Math.pow(2.0D, i);
					opt.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeStream(in, null, opt);
					break;
				}
				i += 1;
			}
		}
		else
		{
			while (true)
			{
				if ((opt.outWidth >> i <= mScreenWidth) && (opt.outHeight >> i <= mScreenHeight))
				{
					in = new BufferedInputStream(new FileInputStream(new File(path)));
					opt.inSampleSize = (int) Math.pow(2.0D, i);
					opt.inJustDecodeBounds = false;
					bitmap = BitmapFactory.decodeStream(in, null, opt);
					break;
				}
				i += 1;
			}
		}
		return bitmap;
	}

	public interface ImageCallback
	{
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params);
	}
}
