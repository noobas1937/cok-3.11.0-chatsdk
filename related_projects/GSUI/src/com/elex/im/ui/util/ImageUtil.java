package com.elex.im.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elex.im.CokChannelDef;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.FileUtil;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.image.AsyncImageLoader;
import com.elex.im.core.util.image.ImageLoaderListener;
import com.elex.im.ui.R;

public class ImageUtil
{
	private static Map<String, Drawable>	resMap	= new HashMap<String, Drawable>();

	public static int getHeadResId(Context c, String headPic)
	{
		int idFlag = ResUtil.getId(c, "drawable", headPic);
		return idFlag;
	}

	public static Drawable getDrawableByResName(Context context, String resName)
	{
		if (resMap == null)
			resMap = new HashMap<String, Drawable>();

		try
		{
			if (!resMap.containsKey(resName))
			{
				int resId = getHeadResId(context, resName);
				Drawable drawable = context.getResources().getDrawable(resId);
				if (drawable != null)
					resMap.put(resName, drawable);
				return drawable;
			}
			else
				return resMap.get(resName);
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 如果默认的g044图片不存在，则不会设置imageView
	 * 
	 * @param headPic可为空
	 *            ，使用默认头像g044
	 */
	public static void setPredefinedHeadImage(final Context c, String headPic, final ImageView imageView, User userInfo)
	{
		try
		{
			Drawable drawable = getDrawableByResName(c, headPic);
			if (drawable != null)
			{
				imageView.setImageDrawable(drawable);
			}
			else
			{
				Drawable defaultDrawable = getDrawableByResName(c, "g044");
				if (defaultDrawable != null)
					imageView.setImageDrawable(defaultDrawable);
				if (userInfo != null)
				{
					if (userInfo.isCustomHeadImage())
						return;
					String fileName = userInfo.headPic.endsWith(".png") ? userInfo.headPic : (userInfo.headPic + ".png");
					setDynamicImage(c, fileName, imageView);
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void setImageOnUiThread(final Context c, final ImageView imageView, final Bitmap bitmap)
	{
		if (c != null && c instanceof Activity)
		{
			((Activity) c).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						imageView.setImageBitmap(bitmap);
//						if (ChatServiceController.getImageDetailFragment() != null)
//							ChatServiceController.getImageDetailFragment().updateAttacher();
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

	public static void setCustomHeadImage(final Context c, final ImageView imageView, final User user)
	{
		if (user != null && StringUtils.isNotEmpty(user.uid))
		{
			imageView.setTag(null);
			Glide.with(c).load(user.getCustomHeadPicUrl()).into(imageView);
//			imageView.setTag(user.uid);
//			getDynamicPic(user.getCustomHeadPicUrl(), user.getCustomHeadPic(), new ImageLoaderListener()
//			{
//				@Override
//				public void onImageLoaded(final Bitmap bitmap)
//				{
//					String uid = (String) imageView.getTag();
//					if ((StringUtils.isNotEmpty(uid) && !uid.equals(user.uid)) || bitmap == null)
//						return;
//					setImageOnUiThread(c, imageView, bitmap);
//				}
//			});
		}
	}

//	public static void loadAllianeShareThumbImage(final Context c, final ImageView imageView, final AllianceShareImageData imageData, boolean usrPlaceHolder)
//	{
//		imageView.setTag(null);
//		if (usrPlaceHolder)
//			Glide.with(c).load(imageData.getServerThumbPath()).placeholder(R.drawable.bg_img).error(R.drawable.bg_img).into(imageView);
//		else
//			Glide.with(c).load(imageData.getServerThumbPath()).error(R.drawable.bg_img).into(imageView);
//	}
//
//	public static void loadAllianeShareThumbImage2(final Context c, final ImageView imageView, final AllianceShareImageData imageData,
//			boolean usrPlaceHolder)
//	{
//		File localFile = new File(imageData.getLocalPath());
//		imageView.setTag(null);
//		if (localFile.exists())
//		{
//			if (usrPlaceHolder)
//				Glide.with(c).load(localFile).placeholder(R.drawable.bg_img).error(R.drawable.bg_img).into(imageView);
//			else
//				Glide.with(c).load(localFile).error(R.drawable.bg_img).into(imageView);
//		}
//		else
//		{
//			if (usrPlaceHolder)
//				Glide.with(c).load(imageData.getServerThumbPath()).placeholder(R.drawable.bg_img).error(R.drawable.bg_img)
//						.into(imageView);
//			else
//				Glide.with(c).load(imageData.getServerThumbPath()).error(R.drawable.bg_img).into(imageView);
//		}
//	}
//
//	public static void loadAllianeShareDetailThumbImage(final Activity c, final ImageView thumbImageView, final ImageView imageView,
//			final ProgressBar progressBar, final AllianceShareImageData imageData)
//	{
//		System.out.println("loadAllianeShareDetailThumbImage:" + imageData.getServerThumbPath());
//		thumbImageView.setTag(null);
//		Glide.with(c).load(imageData.getServerThumbPath()).diskCacheStrategy(DiskCacheStrategy.NONE)
//				.listener(new RequestListener<String, GlideDrawable>()
//				{
//					@Override
//					public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource)
//					{
//						return false;
//					}
//
//					@Override
//					public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
//							boolean isFromMemoryCache, boolean isFirstResource)
//					{
//						setAllianeShareImage(c, imageView, imageData, false);
//						c.runOnUiThread(new Runnable()
//						{
//
//							@Override
//							public void run()
//							{
//								if (progressBar != null)
//									progressBar.setVisibility(View.VISIBLE);
//							}
//						});
//						return false;
//					}
//				}).error(R.drawable.bg_img).into(thumbImageView);
//	}
//
//	public static void loadAllianeShareImage(final Context c, final ImageView imageView, final AllianceShareImageData imageData,
//			boolean usrPlaceHolder)
//	{
//		File localFile = new File(imageData.getLocalPath());
//		imageView.setTag(null);
//		if (localFile.exists())
//		{
//			if (usrPlaceHolder)
//				Glide.with(c).load(localFile).placeholder(R.drawable.bg_img).error(R.drawable.bg_img)
//						.diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
//			else
//				Glide.with(c).load(localFile).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
//		}
//		else
//		{
//			File localThumbFile = new File(imageData.getLocalThumbPath());
//			DrawableRequestBuilder thumbnailRequest;
//			if (localThumbFile.exists())
//				thumbnailRequest = Glide.with(c).load(localThumbFile);
//			else
//				thumbnailRequest = Glide.with(c).load(imageData.getServerThumbPath());
//			if (usrPlaceHolder)
//				Glide.with(c).load(imageData.getServerPath()).placeholder(R.drawable.bg_img).error(R.drawable.bg_img)
//						.diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(thumbnailRequest).into(imageView);
//			else
//				Glide.with(c).load(imageData.getServerPath()).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.ALL)
//						.thumbnail(thumbnailRequest).into(imageView);
//		}
//	}
//
//	public static void loadAllianeShareImage(final Context c, final ImageView imageView, final String localPath, boolean usrPlaceHolder)
//	{
//		File localFile = new File(localPath);
//		imageView.setTag(null);
//		if (usrPlaceHolder)
//			Glide.with(c).load(localFile).placeholder(R.drawable.bg_img).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.NONE)
//					.into(imageView);
//		else
//			Glide.with(c).load(localFile).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
//
//	}
//
//	public static void setAllianeShareImage(final Context c, final ImageView imageView, final AllianceShareImageData imageData,
//			boolean isThumb)
//	{
//		imageView.setTag(imageData.getUrl());
//		getAllianceShareImage(isThumb, imageData, new ImageLoaderListener()
//		{
//			@Override
//			public void onImageLoaded(final Bitmap bitmap)
//			{
//				String url = (String) imageView.getTag();
//				if ((StringUtils.isNotEmpty(url) && !url.equals(imageData.getUrl())) || bitmap == null)
//					return;
//				if (ChatServiceController.getImageDetailFragment() != null)
//					ChatServiceController.getImageDetailFragment().onOpenDetailImageSuccessd();
//				setImageOnUiThread(c, imageView, bitmap);
//			}
//		});
//	}
//
//	public static void setAllianeShareLocalImage(final Activity activity, final ImageView imageView, ImageView thumbImageView,
//			ProgressBar progressBar, AllianceShareImageData imageData,int gridSize)
//	{
//		final String localPath = imageData.getLocalPath();
//		imageView.setTag(localPath);
//		getAllianceShareImage(activity, imageData, imageView,gridSize, thumbImageView, progressBar, new ImageLoaderListener()
//		{
//			@Override
//			public void onImageLoaded(final Bitmap bitmap)
//			{
//				String url = (String) imageView.getTag();
//				if ((StringUtils.isNotEmpty(url) && !url.equals(localPath)) || bitmap == null)
//					return;
//				if (ChatServiceController.getImageDetailFragment() != null)
//					ChatServiceController.getImageDetailFragment().onOpenDetailImageSuccessd();
//				setImageOnUiThread(activity, imageView, bitmap);
//			}
//		});
//	}
//
//	public static void getAllianceShareImage(final Activity activity, final AllianceShareImageData imageData, ImageView imageView,final int gridSize,
//			final ImageView thumbImageView, final ProgressBar progressBar, final ImageLoaderListener listener)
//	{
//		final String localPath = imageData.getLocalPath();
//		if (StringUtils.isNotEmpty(localPath) && listener != null)
//		{
//			if (AsyncImageLoader.getInstance().isCacheExistForKey(localPath))
//			{
//				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localPath);
//				listener.onImageLoaded(bitmap);
//			}
//			else
//			{
//				if (activity != null)
//				{
//					activity.runOnUiThread(new Runnable()
//					{
//
//						@Override
//						public void run()
//						{
//							thumbImageView.setVisibility(View.VISIBLE);
//							progressBar.setVisibility(View.VISIBLE);
//							loadAllianeShareThumbImage(activity, thumbImageView, imageData, true);
//						}
//					});
//				}
//				if (ImageUtil.isFileExist(localPath))
//				{
//					AsyncImageLoader.getInstance().loadBitmapFromStore(localPath, new ImageLoaderListener()
//					{
//						@Override
//						public void onImageLoaded(Bitmap bitmap)
//						{
//							listener.onImageLoaded(bitmap);
//						}
//					});
//				}
//			}
//		}
//	}
//
//	public static void getAllianceShareImage(boolean isThumb, final AllianceShareImageData imageData, final ImageLoaderListener listener)
//	{
//		if (imageData != null && listener != null)
//		{
//			String localPath = imageData.getLocalPath();
//			String localThumbPath = imageData.getLocalThumbPath();
//			if (AsyncImageLoader.getInstance().isCacheExistForKey(localPath))
//			{
//				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localPath);
//				listener.onImageLoaded(bitmap);
//			}
//			else if (AsyncImageLoader.getInstance().isCacheExistForKey(localThumbPath))
//			{
//				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localThumbPath);
//				listener.onImageLoaded(bitmap);
//			}
//			else
//			{
//				if (ImageUtil.isFileExist(localPath))
//				{
//					AsyncImageLoader.getInstance().loadBitmapFromStore(localPath, new ImageLoaderListener()
//					{
//						@Override
//						public void onImageLoaded(Bitmap bitmap)
//						{
//							listener.onImageLoaded(bitmap);
//						}
//					});
//				}
//				else if (ImageUtil.isFileExist(localThumbPath))
//				{
//					AsyncImageLoader.getInstance().loadBitmapFromStore(localThumbPath, new ImageLoaderListener()
//					{
//						@Override
//						public void onImageLoaded(Bitmap bitmap)
//						{
//							listener.onImageLoaded(bitmap);
//						}
//					});
//				}
//				else
//				{
//					String path = isThumb ? localThumbPath : localPath;
//					String url = isThumb ? imageData.getServerThumbPath() : imageData.getServerPath();
//					AsyncImageLoader.getInstance().loadBitmapFromUrl(url, path, new ImageLoaderListener()
//					{
//						@Override
//						public void onImageLoaded(Bitmap bitmap)
//						{
//							listener.onImageLoaded(bitmap);
//						}
//					});
//				}
//			}
//		}
//	}

	public static void getDynamicPic(final String url, final String localPath, final ImageLoaderListener listener)
	{
		if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(localPath) && listener != null)
		{
			if (AsyncImageLoader.getInstance().isCacheExistForKey(localPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localPath);
				listener.onImageLoaded(bitmap);
			}
			else
			{
				if (FileUtil.isFileExist(localPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(localPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromUrl(url, localPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
						}
					});
				}
			}
		}
	}

	public static void getCommonPic(final String fileName, final ImageLoaderListener listener)
	{
		if (StringUtils.isNotEmpty(fileName) && listener != null)
		{
			String locaPath = FileUtil.getCommonPicLocalPath(fileName);
			if (AsyncImageLoader.getInstance().isCacheExistForKey(locaPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(locaPath);
				listener.onImageLoaded(bitmap);
				// LogUtil.printVariablesWithFuctionName(Log.INFO,
				// LogUtil.TAG_DEBUG, "bitmap from cache is not null", bitmap !=
				// null,
				// "isListFilling", ChatServiceController.isListViewFling,
				// "user", user.userName);
			}
			else
			{
				if (FileUtil.isPicExist(locaPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
							// LogUtil.printVariablesWithFuctionName(Log.INFO,
							// LogUtil.TAG_DEBUG,
							// "bitmap from sdcard is not null",
							// bitmap != null, "isListFilling",
							// ChatServiceController.isListViewFling, "user",
							// user.userName);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromCocos2dx(fileName, locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
							// LogUtil.printVariablesWithFuctionName(Log.INFO,
							// LogUtil.TAG_DEBUG,
							// "bitmap from http is not null",
							// bitmap != null, "isListFilling",
							// ChatServiceController.isListViewFling, "user",
							// user.userName);
						}
					});
				}
			}
		}
	}

	public static void getXiaoMiPic(final String fileName, final ImageLoaderListener listener)
	{
		if (StringUtils.isNotEmpty(fileName) && listener != null)
		{

			String locaPath = fileName;
			if (AsyncImageLoader.getInstance().isCacheExistForKey(locaPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(locaPath);
				listener.onImageLoaded(bitmap);
				// LogUtil.printVariablesWithFuctionName(Log.INFO,
				// LogUtil.TAG_DEBUG, "bitmap from cache is not null", bitmap !=
				// null,
				// "isListFilling", ChatServiceController.isListViewFling,
				// "user", user.userName);
			}
			else
			{
				if (FileUtil.isPicExist(locaPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
							// LogUtil.printVariablesWithFuctionName(Log.INFO,
							// LogUtil.TAG_DEBUG,
							// "bitmap from sdcard is not null",
							// bitmap != null, "isListFilling",
							// ChatServiceController.isListViewFling, "user",
							// user.userName);
						}
					});
				}
				else
				{
					AsyncImageLoader.getInstance().loadBitmapFromCocos2dx(fileName, locaPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
							// LogUtil.printVariablesWithFuctionName(Log.INFO,
							// LogUtil.TAG_DEBUG,
							// "bitmap from http is not null",
							// bitmap != null, "isListFilling",
							// ChatServiceController.isListViewFling, "user",
							// user.userName);
						}
					});
				}
			}
		}
	}

	public static void setHeadImage(Context c, String headPic, final ImageView imageView, User user)
	{
		try
		{
			imageView.setTag(null);
			int resId = getHeadResId(c, headPic);
			if (resId == 0)
				resId = getHeadResId(c, "g044");
			if (ConfigManager.enableCustomHeadImg && user != null && user.isCustomHeadImage())
			{
				if (resId > 0)
					Glide.with(c).load(user.getCustomHeadPicUrl()).placeholder(resId).dontAnimate().into(imageView);
				else
					Glide.with(c).load(user.getCustomHeadPicUrl()).dontAnimate().into(imageView);
			}
			else
			{
				if (resId > 0)
					Glide.with(c).load(resId).dontAnimate().into(imageView);
				//			setPredefinedHeadImage(c, headPic, imageView, user);
			}
		}
		catch(OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void setCommonImage(final Context c, final String fileName, final ImageView imageView)
	{
		getCommonPic(fileName, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String picName = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(picName) && !picName.equals(fileName)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}

	public static void setDynamicImage(final Context c, final String fileName, final ImageView imageView)
	{
		imageView.setTag(fileName);
		String localPath = FileUtil.getCommonPicLocalPath(fileName);
		String url = ConfigManager.getCDNUrl(fileName);
		getDynamicPic(url, localPath, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String picName = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(picName) && !picName.equals(fileName)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}

	public static void setXiaomiImage(final Context c, final String fileName, final ImageView imageView)
	{
		getXiaoMiPic(fileName, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String picName = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(picName) && !picName.equals(fileName)) || bitmap == null)
					return;
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}

	/**
	 * x匹配宽度，y按tile重复
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setYRepeatingBG(Activity activity, View view, int id)
	{
		Drawable d = ImageUtil.getRepeatingBG(activity, R.drawable.mail_list_bg);
		if (d == null)
			return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
		{
			view.setBackgroundDrawable(d);
		}
		else
		{
			view.setBackground(d);
		}
	}

	public static Drawable getRepeatingBG(Activity activity, int center)
	{
		try
		{
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = true;
			Bitmap center_bmp = BitmapFactory.decodeResource(activity.getResources(), center, options);
			if (center_bmp == null)
				return null;
			center_bmp.setDensity(Bitmap.DENSITY_NONE);
			center_bmp = Bitmap.createScaledBitmap(center_bmp, dm.widthPixels, center_bmp.getHeight(), true);
			BitmapDrawable center_drawable = new BitmapDrawable(activity.getResources(), center_bmp);
			// change here setTileModeY to setTileModeX if you want to repear in X
			center_drawable.setTileModeY(Shader.TileMode.REPEAT);
			return center_drawable;
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth)
		{
			if (width > height)
			{
				inSampleSize = Math.round((float) height / (float) reqHeight);
			}
			else
			{
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromFile(byte[] data, int reqWidth, int reqHeight)
	{

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	private static File saveImage(String fileName, String compressFileName, BitmapFactory.Options options)
	{
		Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);

		System.out.println("compressImage compressFileName:" + compressFileName);
		FileOutputStream fos = null;
		File outputFile = new File(compressFileName);
		try
		{
			if (!outputFile.exists())
				outputFile.createNewFile();
			fos = new FileOutputStream(outputFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
			fos.close();
		}
		catch (IOException e)
		{
			if (outputFile.exists())
			{
				outputFile.delete();
			}
			e.printStackTrace();
		}
		if (!bitmap.isRecycled())
		{
			bitmap.recycle();
		}
		return outputFile;
	}

	public static File compressImage(String fileName, String compressFileName)
	{
		File outputFile = saveImage(fileName, compressFileName, null);
//		long fileSize = outputFile.length();
//		final long fileMaxSize = 200 * 1024;
//		if (fileSize >= fileMaxSize)
//		{
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inJustDecodeBounds = true;
//			BitmapFactory.decodeFile(fileName, options);
//			int height = options.outHeight;
//			int width = options.outWidth;
//
//			double scale = Math.sqrt((float) fileSize / fileMaxSize);
//			options.outHeight = (int) (height / scale);
//			options.outWidth = (int) (width / scale);
//			options.inSampleSize = (int) (scale + 0.5);
//			options.inJustDecodeBounds = false;
//
//			outputFile = saveImage(fileName, compressFileName, options);
//
//		}
//		else
//		{
//			outputFile = saveImage(fileName, compressFileName, null);
//		}
		return outputFile;

	}
	
	public static void setChannelImage(Context context , Channel channel, ImageView imageView)
	{
		if(context == null || imageView == null)
			return;
		GradientDrawable bgShape = (GradientDrawable) imageView.getBackground();
		if(bgShape!=null)
			bgShape.setColor(0xDD000000);
		if(channel == null)
		{
			imageView.setImageDrawable(null);
			return;
		}
		if(CokChannelDef.getInstance().getChannelDef(channel.getChannelType()).hasUserHeadPic)
		{
			if (CokChannelDef.getInstance().getChannelDef(channel.getChannelType()).useMultiUserHeadPic)
			{
				setChatRoomIcon(context, channel, imageView);
			}
			else
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.getChannelID());
				if (StringUtils.isEmpty(fromUid))
					return;
				UserManager.checkUser(fromUid, "", 0);
				User userInfo = UserManager.getInstance().getUser(fromUid);
				if (userInfo != null)
					ImageUtil.setHeadImage(context, userInfo.headPic, imageView, userInfo);
			}
		}
		else
		{
			imageView.setImageDrawable(null);
		}
	}
	
	public static void setChatRoomIcon(final Context context ,final Channel channel, final ImageView imageView)
	{
		
		if (channel== null || channel.memberUidArray == null || channel.memberUidArray.size() == 0)
		{
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mail_pic_flag_31));
			return;
		}

		String fileName = FileUtil.getChatroomHeadPicPath(context) + getChatroomHeadPicFile(channel.getChannelID());
		if (!channel.isMemberUidChanged)
		{
			if (AsyncImageLoader.getInstance().isCacheExistForKey(fileName))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(fileName);
				imageView.setImageBitmap(bitmap);
				return;
			}
			else if (isChatroomHeadPicExist(context,channel.getChannelID()))
			{
				imageView.setTag(channel.getChannelID());
				AsyncImageLoader.getInstance().loadBitmapFromStore(fileName, new ImageLoaderListener()
				{
					@Override
					public void onImageLoaded(Bitmap bitmap)
					{
						String groupId = (String) imageView.getTag();
						if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.getChannelID())) || bitmap == null)
							return;
						ImageUtil.setImageOnUiThread(context, imageView, bitmap);
					}
				});
				return;
			}
		}
		
		channel.chatroomHeadImages = new HashMap<String, Bitmap>();
		channel.customPicLoadingCnt = 0;

		ArrayList<User> users = new ArrayList<User>();
		for (int i = 0; i < channel.memberUidArray.size(); i++)
		{
			User user = UserManager.getInstance().getUser(channel.memberUidArray.get(i));
			if (user != null)
			{
				users.add(user);
			}
			if (users.size() >= 9)
				break;
		}

		for (int i = 0; i < users.size(); i++)
		{
			final User user = users.get(i);
			Bitmap predefinedHeadImage = BitmapFactory.decodeResource(context.getResources(),
					ImageUtil.getHeadResId(context, user.headPic));
			if (predefinedHeadImage != null)
				channel.chatroomHeadImages.put(user.uid, predefinedHeadImage);

			if (user.isCustomHeadImage())
			{
				channel.customPicLoadingCnt ++;
				imageView.setTag(user.uid);
				ImageUtil.getDynamicPic(user.getCustomHeadPicUrl(),user.getCustomHeadPic(), new ImageLoaderListener()
				{
					@Override
					public void onImageLoaded(final Bitmap bitmap)
					{
						if(bitmap!=null && imageView.getTag()!=null && user.uid.equals(imageView.getTag().toString()))
							onCustomImageLoaded(context,channel, user.uid, bitmap, imageView);
					}
				});
			}
		}
		if (channel.customPicLoadingCnt == 0)
		{
			generateCombinePic(context ,channel, imageView);
		}
	}
	
	private static void generateCombinePic(Context context,Channel channel, ImageView imageView)
	{
		if(channel == null)
			return;
		if(channel.chatroomHeadImages == null)
			channel.chatroomHeadImages = new HashMap<String, Bitmap>();
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		Set<String> keySet = channel.chatroomHeadImages.keySet();
		for (String key : keySet)
		{
			if (StringUtils.isNotEmpty(key) && channel.chatroomHeadImages.get(key) != null)
			{
				bitmaps.add(channel.chatroomHeadImages.get(key));
			}
		}

		Bitmap bitmap = CombineBitmapManager.getInstance().getCombinedBitmap(bitmaps);
		try
		{
			String chatroomHeadPicPath = FileUtil.getChatroomHeadPicPath(context);
			
			if (bitmap != null && StringUtils.isNotEmpty(channel.getChannelID()) &&  StringUtils.isNotEmpty(chatroomHeadPicPath))
			{
				String chatroomHeadPicName = getChatroomHeadPicFile(channel.getChannelID());
				if(StringUtils.isNotEmpty(chatroomHeadPicName))
				{
					BitmapUtil.saveMyBitmap(bitmap, chatroomHeadPicPath, chatroomHeadPicName);

					if (channel.isMemberUidChanged)
					{
						channel.isMemberUidChanged = false;
						String fileName = chatroomHeadPicPath + chatroomHeadPicName;
						AsyncImageLoader.removeMemoryCache(fileName);
					}
				}
			}
		}
		catch (IOException e)
		{
			LogUtil.printException(e);
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}

		String groupId = (String) imageView.getTag();
		if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.getChannelID())) || bitmap == null)
			return;
		ImageUtil.setImageOnUiThread(context, imageView, bitmap);
	}
	
	private static void onCustomImageLoaded(Context context,Channel channel, String uid, final Bitmap bitmap, ImageView imageView)
	{
		if(channel == null || bitmap == null || StringUtils.isEmpty(uid))
			return;
		channel.chatroomHeadImages.put(uid, bitmap);
		channel.customPicLoadingCnt--;
		if (channel.customPicLoadingCnt == 0)
		{
			generateCombinePic(context,channel, imageView);
		}
	}
	
	public static String getChatroomHeadPicFile(String channelId)
	{
		return channelId;
	}

	public static String getOldChatroomHeadPicFile(String channelId)
	{
		return channelId + ".png";
	}

	public static boolean isChatroomHeadPicExist(Context context,String channelId)
	{
		try
		{
			String fileName = FileUtil.getChatroomHeadPicPath(context) + getOldChatroomHeadPicFile(channelId);
			File oldfile = new File(fileName);
			if (oldfile.exists())
			{
				oldfile.delete();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String fileName = FileUtil.getChatroomHeadPicPath(context) + getChatroomHeadPicFile(channelId);
		File file = new File(fileName);
		if (file.exists())
		{
			return true;
		}
		return false;
	}

}
