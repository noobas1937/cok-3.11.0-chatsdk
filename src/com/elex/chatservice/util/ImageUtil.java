package com.elex.chatservice.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.elex.chatservice.R;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.image.AsyncImageLoader;
import com.elex.chatservice.image.ImageLoaderListener;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.HeadPicUtil.MD5;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;

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
	
	public static Drawable getDrawableByBackgroundName(String backgroundName)
	{
		if (resMap == null)
			resMap = new HashMap<String, Drawable>();

		try
		{
			if (resMap.containsKey(backgroundName))
				return resMap.get(backgroundName);
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
	public static void setPredefinedHeadImage(final Context c, String headPic, final ImageView imageView, UserInfo userInfo)
	{
		try
		{
			if(userInfo!=null)
				System.out.println("userInfo:"+userInfo.userName + "  headPic:"+userInfo.headPic);
			int resId = getHeadResId(c, headPic);
			Drawable drawable = null;
			if(resId>0)
				drawable = c.getResources().getDrawable(resId);
			if (drawable != null)
			{
				imageView.setImageDrawable(drawable);
			}
			else
			{
				Drawable defaultDrawable = c.getResources().getDrawable(R.drawable.g044);
				if (defaultDrawable != null)
					imageView.setImageDrawable(defaultDrawable);
				if (userInfo != null)
				{
					if (userInfo.isCustomHeadImage())
						return;
					String fileName = userInfo.headPic.endsWith(".png") ? userInfo.headPic : (userInfo.headPic + ".png");
					setDynamicHeadImage(c, fileName, imageView,userInfo.uid);
				}
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}
	
	public static void setCustomTextBackground(final Context c, String headPic, final TextView textView, final String customChatBg)
	{
		try
		{
			if(StringUtils.isNotEmpty(customChatBg))
			{
				int customBgId = ImageUtil.getHeadResId(c, customChatBg);
				if(customBgId>0)
				{
					Drawable customBg = c.getResources().getDrawable(customBgId);
					setBackground(textView, customBg);
				}
				else
				{
					setBackground(textView, c.getResources().getDrawable(ImageUtil.getHeadResId(c, headPic)));
					String backgroundFullName = customChatBg+".9.png";
					String localPath = getCommonPicLocalPath(backgroundFullName);
					String url = ConfigManager.getCDNUrl(backgroundFullName);
					getDynamicPic(url, localPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(final Bitmap bitmap)
						{
							if(bitmap != null && textView.getTag()!=null && textView.getTag() instanceof MsgItem)
							{
								MsgItem item = (MsgItem)textView.getTag();
								int type = item.getMsgItemType(c);
								String customChatBgName = item.getCustomChatBgPressedName();
								String customChatBgName2 = item.getCustomChatBgNormalName();
								if((type == MsgItem.MSGITEM_TYPE_MESSAGE || type == MsgItem.MSGITEM_TYPE_AUDIO ) 
										&& ((StringUtils.isNotEmpty(customChatBgName) && customChatBgName.equals(customChatBg)) || 
												(StringUtils.isNotEmpty(customChatBgName2) && customChatBgName2.equals(customChatBg))))
								{
									Drawable drawable = bitmapToDrawble(bitmap, c);
									if(drawable!=null)
										setBackgroundOnUiThread(c, textView, drawable);
//									if(drawable!=null)
//									{
//										if(!resMap.containsKey(customChatBg))
//											resMap.put(customChatBg, drawable);
//										
//									}
								}
							}
						}
					});
				}
			}
			else
			{
				setBackground(textView, c.getResources().getDrawable(ImageUtil.getHeadResId(c, headPic)));
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}
	
	public static void setCustomTextNormalBackground(final Context c, String headPic, final TextView textView, final String customChatBg,final String customChatPressedBg,final MsgItem msgItem)
	{
		try
		{
			if(StringUtils.isNotEmpty(customChatBg))
			{
				int customBgId = ImageUtil.getHeadResId(c, customChatBg);
				if(customBgId>0)
				{
					Drawable customBg = c.getResources().getDrawable(customBgId);
					setBackground(textView, customBg);
				}
				else
				{
					setBackground(textView, c.getResources().getDrawable(ImageUtil.getHeadResId(c, headPic)));
					String backgroundFullName = customChatBg+".9.png";
					String localPath = getCommonPicLocalPath(backgroundFullName);
					String url = ConfigManager.getCDNUrl(backgroundFullName);
					
					String backgroundFullNamePressed = customChatPressedBg+".9.png";
					String localPathPressed = getCommonPicLocalPath(backgroundFullNamePressed);
					String urlPress = ConfigManager.getCDNUrl(backgroundFullNamePressed);
					getCustomTextBg(url, localPath,urlPress,localPathPressed, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(final Bitmap bitmap)
						{
							if(bitmap != null && textView.getTag()!=null && textView.getTag() instanceof MsgItem)
							{
								MsgItem item = (MsgItem)textView.getTag();
								if(item!=null && msgItem!=null && item.createTime == msgItem.createTime 
										&& StringUtils.isNotEmpty(item.uid) && StringUtils.isNotEmpty(msgItem.uid) &&
										item.uid.equals(msgItem.uid)
										&& StringUtils.isNotEmpty(item.msg) && StringUtils.isNotEmpty(msgItem.msg) &&
										item.msg.equals(msgItem.msg))
								{
									int type = item.getMsgItemType(c);
									String customChatBgName = item.getCustomChatBgPressedName();
									String customChatBgName2 = item.getCustomChatBgNormalName();
									if((type == MsgItem.MSGITEM_TYPE_MESSAGE || type == MsgItem.MSGITEM_TYPE_AUDIO ) 
											&& ((StringUtils.isNotEmpty(customChatBgName) && customChatBgName.equals(customChatBg)) || 
													(StringUtils.isNotEmpty(customChatBgName2) && customChatBgName2.equals(customChatBg))))
									{
										Drawable drawable = bitmapToDrawble(bitmap, c);
										if(drawable!=null)
											setBackgroundOnUiThread(c, textView, drawable);
									}
								}
							}
						}
					});
				}
			}
			else
			{
				setBackground(textView, c.getResources().getDrawable(ImageUtil.getHeadResId(c, headPic)));
			}
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}
	
	public static Drawable bitmapToDrawble(Bitmap bitmap,Context context){  
		Rect paddingRect = new Rect();
		if(bitmap.getNinePatchChunk()!=null)
		{	
			NinePatchUtils.readPaddingFromChunk(bitmap.getNinePatchChunk(), paddingRect);
			return new NinePatchDrawable(context.getResources(),
					bitmap, bitmap.getNinePatchChunk(), paddingRect, null);
		}
		else
			return new BitmapDrawable(context.getResources(), bitmap);  
//        return drawable;  
    }  
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setBackground(View textView,Drawable drawable)
	{
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
			textView.setBackgroundDrawable(drawable);
		else
			textView.setBackground(drawable);
	}
	
	public static void setBackgroundOnUiThread(final Context c, final TextView textView, final Drawable drawable)
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
						setBackground(textView, drawable);
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
						if (ChatServiceController.getImageDetailFragment() != null)
							ChatServiceController.getImageDetailFragment().updateAttacher();
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

	public static void setCustomHeadImage(final Context c, final ImageView imageView, final UserInfo user)
	{
		if (user != null && StringUtils.isNotEmpty(user.uid))
		{
			imageView.setTag(user.uid);
			getDynamicPic(user.getCustomHeadPicUrl(), user.getCustomHeadPic(), new ImageLoaderListener()
			{
				@Override
				public void onImageLoaded(final Bitmap bitmap)
				{
					String uid = (String) imageView.getTag();
					if ((StringUtils.isNotEmpty(uid) && !uid.equals(user.uid)) || bitmap == null)
						return;
					setImageOnUiThread(c, imageView, bitmap);
				}
			});
		}
	}

	public static void loadAllianeShareThumbImage(final Context c, final ImageView imageView, final AllianceShareImageData imageData, boolean usrPlaceHolder)
	{
		imageView.setTag(null);
		if (usrPlaceHolder){
			Glide.with(c).load(imageData.getServerThumbPath()).placeholder(R.drawable.bg_img).error(R.drawable.bg_img).into(imageView);
		}else
			{
			Glide.with(c).load(imageData.getServerThumbPath()).error(R.drawable.bg_img).into(imageView);
	}
}

	public static void loadAllianeShareThumbImage2(final Context c, final ImageView imageView, final AllianceShareImageData imageData,
			boolean usrPlaceHolder)
	{
		File localFile = new File(imageData.getLocalPath());
		imageView.setTag(null);
		if (localFile.exists())
		{
			if (usrPlaceHolder)
				Glide.with(c).load(localFile).placeholder(R.drawable.bg_img).error(R.drawable.bg_img).into(imageView);
			else
				Glide.with(c).load(localFile).error(R.drawable.bg_img).into(imageView);
		}
		else
		{
			if (usrPlaceHolder)
				Glide.with(c).load(imageData.getServerThumbPath()).placeholder(R.drawable.bg_img).error(R.drawable.bg_img)
						.into(imageView);
			else
				Glide.with(c).load(imageData.getServerThumbPath()).error(R.drawable.bg_img).into(imageView);
		}
	}

	public static void loadAllianeShareDetailThumbImage(final Activity c, final ImageView thumbImageView, final ImageView imageView,
			final ProgressBar progressBar, final AllianceShareImageData imageData)
	{
		System.out.println("loadAllianeShareDetailThumbImage:" + imageData.getServerThumbPath());
		thumbImageView.setTag(null);
		Glide.with(c).load(imageData.getServerThumbPath()).diskCacheStrategy(DiskCacheStrategy.NONE)
				.listener(new RequestListener<String, GlideDrawable>()
				{
					@Override
					public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource)
					{
						return false;
					}

					@Override
					public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
							boolean isFromMemoryCache, boolean isFirstResource)
					{
						setAllianeShareImage(c, imageView, imageData, false);
						c.runOnUiThread(new Runnable()
						{

							@Override
							public void run()
							{
								if (progressBar != null)
									progressBar.setVisibility(View.VISIBLE);
							}
						});
						return false;
					}
				}).error(R.drawable.bg_img).into(thumbImageView);
	}

	public static void loadAllianeShareImage(final Context c, final ImageView imageView, final AllianceShareImageData imageData,
			boolean usrPlaceHolder)
	{
		File localFile = new File(imageData.getLocalPath());
		imageView.setTag(null);
		if (localFile.exists())
		{
			if (usrPlaceHolder)
				Glide.with(c).load(localFile).placeholder(R.drawable.bg_img).error(R.drawable.bg_img)
						.diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
			else
				Glide.with(c).load(localFile).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
		}
		else
		{
			File localThumbFile = new File(imageData.getLocalThumbPath());
			DrawableRequestBuilder thumbnailRequest;
			if (localThumbFile.exists())
				thumbnailRequest = Glide.with(c).load(localThumbFile);
			else
				thumbnailRequest = Glide.with(c).load(imageData.getServerThumbPath());
			if (usrPlaceHolder)
				Glide.with(c).load(imageData.getServerPath()).placeholder(R.drawable.bg_img).error(R.drawable.bg_img)
						.diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(thumbnailRequest).into(imageView);
			else
				Glide.with(c).load(imageData.getServerPath()).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.ALL)
						.thumbnail(thumbnailRequest).into(imageView);
		}
	}

	public static void loadAllianeShareImage(final Context c, final ImageView imageView, final String localPath, boolean usrPlaceHolder)
	{
		File localFile = new File(localPath);
		imageView.setTag(null);
		if (usrPlaceHolder)
			Glide.with(c).load(localFile).placeholder(R.drawable.bg_img).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView);
		else
			Glide.with(c).load(localFile).error(R.drawable.bg_img).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);

	}

	public static void setAllianeShareImage(final Context c, final ImageView imageView, final AllianceShareImageData imageData,
			boolean isThumb)
	{
		imageView.setTag(imageData.getUrl());
		getAllianceShareImage(isThumb, imageData, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String url = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(url) && !url.equals(imageData.getUrl())) || bitmap == null)
					return;
				if (ChatServiceController.getImageDetailFragment() != null)
					ChatServiceController.getImageDetailFragment().onOpenDetailImageSuccessd();
				setImageOnUiThread(c, imageView, bitmap);
			}
		});
	}

	public static void setAllianeShareLocalImage(final Activity activity, final ImageView imageView, ImageView thumbImageView,
			ProgressBar progressBar, AllianceShareImageData imageData,int gridSize)
	{
		final String localPath = imageData.getLocalPath();
		imageView.setTag(localPath);
		getAllianceShareImage(activity, imageData, imageView,gridSize, thumbImageView, progressBar, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String url = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(url) && !url.equals(localPath)) || bitmap == null)
					return;
				if (ChatServiceController.getImageDetailFragment() != null)
					ChatServiceController.getImageDetailFragment().onOpenDetailImageSuccessd();
				setImageOnUiThread(activity, imageView, bitmap);
			}
		});
	}

	public static void getAllianceShareImage(final Activity activity, final AllianceShareImageData imageData, ImageView imageView,final int gridSize,
			final ImageView thumbImageView, final ProgressBar progressBar, final ImageLoaderListener listener)
	{
		final String localPath = imageData.getLocalPath();
		if (StringUtils.isNotEmpty(localPath) && listener != null)
		{
			if (AsyncImageLoader.getInstance().isCacheExistForKey(localPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localPath);
				listener.onImageLoaded(bitmap);
			}
			else
			{
				if (activity != null)
				{
					activity.runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							thumbImageView.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.VISIBLE);
							loadAllianeShareThumbImage(activity, thumbImageView, imageData, true);
						}
					});
				}
				if (ImageUtil.isFileExist(localPath))
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
			}
		}
	}

	public static void getAllianceShareImage(boolean isThumb, final AllianceShareImageData imageData, final ImageLoaderListener listener)
	{
		if (imageData != null && listener != null)
		{
			String localPath = imageData.getLocalPath();
			String localThumbPath = imageData.getLocalThumbPath();
			if (AsyncImageLoader.getInstance().isCacheExistForKey(localPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localPath);
				listener.onImageLoaded(bitmap);
			}
			else if (AsyncImageLoader.getInstance().isCacheExistForKey(localThumbPath))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(localThumbPath);
				listener.onImageLoaded(bitmap);
			}
			else
			{
				if (ImageUtil.isFileExist(localPath))
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
				else if (ImageUtil.isFileExist(localThumbPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(localThumbPath, new ImageLoaderListener()
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
					String path = isThumb ? localThumbPath : localPath;
					String url = isThumb ? imageData.getServerThumbPath() : imageData.getServerPath();
					AsyncImageLoader.getInstance().loadBitmapFromUrl(url, path, new ImageLoaderListener()
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

	public static String getCommonPicLocalPath(String fileName)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "common_pic");
		path += "cache_" + MD5.getMD5Str(fileName);
		if(fileName.endsWith(".9.png"))
			path += ".9.png";
		return path;
	}

	public static boolean isUpdateImageExist(String imageName)
	{
		return isFileExist(getCommonPicLocalPath(imageName));
	}
	
	public static void getCustomTextBg(final String url, final String localPath,final String pressedUrl, final String pressedLocalPath, final ImageLoaderListener listener)
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
				if (isFileExist(localPath))
				{
					AsyncImageLoader.getInstance().loadBitmapFromStore(localPath, new ImageLoaderListener()
					{
						@Override
						public void onImageLoaded(Bitmap bitmap)
						{
							listener.onImageLoaded(bitmap);
						}
					});
					if(isFileExist(pressedLocalPath))
						AsyncImageLoader.getInstance().loadBitmapFromStore(pressedLocalPath, null);
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
					AsyncImageLoader.getInstance().loadBitmapFromUrl(pressedUrl, pressedLocalPath, null);
				}
			}
		}
	}

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
				if (isFileExist(localPath))
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
			String locaPath = getCommonPicLocalPath(fileName);
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
				if (isPicExist(locaPath))
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
				if (isPicExist(locaPath))
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

	public static void setHeadImage(Context c, String headPic, final ImageView imageView, UserInfo user)
	{
		try
		{
			setPredefinedHeadImage(c, headPic, imageView, user);
			if (ConfigManager.enableCustomHeadImg && user != null && user.isCustomHeadImage())
				setCustomHeadImage(c, imageView, user);
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
		if(StringUtils.isNotEmpty(fileName))
		{
			int customBgId = ImageUtil.getHeadResId(c, fileName);
			if(customBgId>0)
			{
				Drawable customBg = c.getResources().getDrawable(customBgId);
				if(customBg!=null)
					imageView.setImageDrawable(customBg);
			}
			else
			{
				imageView.setTag(fileName);
				String localPath = getCommonPicLocalPath(fileName);
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
		}
	}
	
	public static void setEmojImage(final Context c, final int icon,final String groupId,final String id,final ImageView imageView,boolean isGif)
	{
		if(StringUtils.isNotEmpty(groupId))
		{
			if(icon>0)
				Glide.with(c).load(icon).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
			else
			{
				String url = EmojSubscribeManager.getEmojCDNPath(groupId, id, isGif);
				Glide.with(c).load(url).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
			}
		}
	}
	
	public static void setDynamicHeadImage(final Context c, final String fileName, final ImageView imageView,final String uid)
	{
		imageView.setTag(uid);
		String localPath = getCommonPicLocalPath(fileName);
		String url = ConfigManager.getCDNUrl(fileName);
		getDynamicPic(url, localPath, new ImageLoaderListener()
		{
			@Override
			public void onImageLoaded(final Bitmap bitmap)
			{
				String userId = (String) imageView.getTag();
				if ((StringUtils.isNotEmpty(userId) && !userId.equals(uid)) || bitmap == null)
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
			if (center_bmp == null)
				return null;
			BitmapDrawable center_drawable = new BitmapDrawable(activity.getResources(), center_bmp);
			// change here setTileModeY to setTileModeX if you want to repear in X
			center_drawable.setTileModeY(Shader.TileMode.REPEAT);
			return center_drawable;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isPicExist(String path)
	{
		String fileName = path;
		if (StringUtils.isEmpty(fileName))
			return false;

		try
		{
			File file = new File(fileName);
			if (file.exists())
			{
				return true;
			}
			if (!path.endsWith(".png") && !path.endsWith(".jpg"))
			{
				fileName = path + ".png";
				file = new File(fileName);
				if (file.exists())
				{
					return true;
				}
				else
				{
					fileName = path + ".jpg";
					file = new File(fileName);
					if (file.exists())
					{
						return true;
					}
				}
			}
			else
			{
				file = new File(path);
				if (file.exists())
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isFileExist(String path)
	{
		if (StringUtils.isEmpty(path))
			return false;
		try
		{
			File file = new File(path);
			if (file.exists())
			{
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
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
		try
		{
			Bitmap bitmap = BitmapFactory.decodeFile(fileName, options);
			if (bitmap == null)
				return null;
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
		catch(OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static File compressImage(String fileName, String compressFileName)
	{
		File outputFile = null;
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileName, options);
			int height = options.outHeight;
			int width = options.outWidth;
			long fileSize = height * width;
			final long fileMaxSize = (long) (1024 * 1024 * 1.5f);
			if (fileSize >= fileMaxSize)
			{
				double scale = Math.sqrt((float) fileSize / fileMaxSize);
				options.outHeight = (int) (height / scale);
				options.outWidth = (int) (width / scale);
				options.inSampleSize = (int) (scale + 0.5);
				options.inJustDecodeBounds = false;
				outputFile = saveImage(fileName, compressFileName, options);
			}
			else
			{
				options.inJustDecodeBounds = false;
				outputFile = saveImage(fileName, compressFileName, null);
			} 
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		return outputFile;
	}
	
	public static void setChannelImage(Context context , ChatChannel channel, ImageView imageView)
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
		if(channel.channelType == DBDefinition.CHANNEL_TYPE_USER)
		{
			String fromUid = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
			if (StringUtils.isEmpty(fromUid))
				return;
			UserManager.checkUser(fromUid, "", 0);
			UserInfo userInfo = UserManager.getInstance().getUser(fromUid);
			if (userInfo != null)
				ImageUtil.setHeadImage(context, userInfo.headPic, imageView, userInfo);
		}
		else if(channel.channelType == DBDefinition.CHANNEL_TYPE_CHATROOM)
		{
			setChatRoomIcon(context, channel, imageView);
		}
		else if(channel.channelType == DBDefinition.CHANNEL_TYPE_RANDOM_CHAT)
		{
			setRandomChatRoomIcon(context, channel, imageView);
		}
		else
		{
			imageView.setImageDrawable(null);
		}
	}
	
	public static void setRandomChatRoomIcon(final Context context ,final ChatChannel channel, final ImageView imageView)
	{
		if (channel!= null)
		{
			if(channel.randomChatMode == 1)
			{
				String lang = ConfigManager.getInstance().gameLang;
				if(StringUtils.isEmpty(lang))
					lang = "en";
				String langImage = LanguageManager.getLangImage(lang);
				int resId = ImageUtil.getHeadResId(context, langImage);
				Drawable drawable = null;
				if(resId>0)
					drawable = context.getResources().getDrawable(resId);
				if(drawable!=null)
					imageView.setImageDrawable(drawable);
				else
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mail_pic_flag_31));
			}
			else
				imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.random_global));
		}
	}
	
	public static void setChatRoomIcon(final Context context ,final ChatChannel channel, final ImageView imageView)
	{
		
		if (channel== null || channel.memberUidArray == null || channel.memberUidArray.size() == 0)
		{
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.mail_pic_flag_31));
			return;
		}

		String fileName = getChatroomHeadPicPath(context) + getChatroomHeadPicFile(channel.channelID);
		if (!channel.isMemberUidChanged)
		{
			if (AsyncImageLoader.getInstance().isCacheExistForKey(fileName))
			{
				Bitmap bitmap = AsyncImageLoader.getInstance().loadBitmapFromCache(fileName);
				imageView.setImageBitmap(bitmap);
				
			}
			else if (isChatroomHeadPicExist(context,channel.channelID))
			{
				imageView.setTag(channel.channelID);
				AsyncImageLoader.getInstance().loadBitmapFromStore(fileName, new ImageLoaderListener()
				{
					@Override
					public void onImageLoaded(Bitmap bitmap)
					{
						String groupId = (String) imageView.getTag();
						if ((StringUtils.isNotEmpty(groupId) && !groupId.equals(channel.channelID)) || bitmap == null)
							return;
						ImageUtil.setImageOnUiThread(context, imageView, bitmap);
					}
				});
			}
			return;
		}
		else
			channel.refreshChatRoomChannelImage();
	}
	
	public static String getChatroomHeadPicPath(Context context)
	{
		if (context == null)
			return null;
		return DBHelper.getHeadDirectoryPath(context) + "chatroom/";
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
			String fileName = getChatroomHeadPicPath(context) + getOldChatroomHeadPicFile(channelId);
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

		String fileName = getChatroomHeadPicPath(context) + getChatroomHeadPicFile(channelId);
		File file = new File(fileName);
		if (file.exists())
		{
			return true;
		}
		return false;
	}

}
