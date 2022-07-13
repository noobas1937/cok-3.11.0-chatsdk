package com.elex.chatservice.view.allianceshare;

import java.io.File;
import java.io.Serializable;

import com.elex.chatservice.R;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.photoview.PhotoViewAttacher;
import com.elex.chatservice.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * 单张图片显示Fragment
 */
public class ImageDetailFragment extends Fragment
{
	private AllianceShareImageData	mImageData;
	private ImageView				mImageView;
	private ImageView				mThumbImageView;
	private ProgressBar				progressBar;
	private PhotoViewAttacher		mAttacher;
	private String					mLocalPath;
	private static boolean			isFromServer	= true;
	private int thumbImageWidth    	= 177;

	public static ImageDetailFragment newInstance(AllianceShareImageData imageData,int width)
	{
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putSerializable("imagedata", (Serializable) imageData);
		args.putInt("thumbImageWidth", width);
		f.setArguments(args);
		isFromServer = true;

		return f;
	}

	public static ImageDetailFragment newInstance(String localPath)
	{
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putString("localPath", localPath);
		f.setArguments(args);
		isFromServer = false;
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		System.out.println("ImageDetailFraget onCreate");
		super.onCreate(savedInstanceState);
		if (isFromServer)
		{
			mImageData = getArguments() != null ? (AllianceShareImageData) (getArguments().getSerializable("imagedata")) : null;
			thumbImageWidth = getArguments() != null ? getArguments().getInt("thumbImageWidth") : 177;
		}
		else
		{
			mLocalPath = getArguments() != null ? getArguments().getString("localPath") : "";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		System.out.println("ImageDetailFraget onCreateView");
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		mThumbImageView = (ImageView) v.findViewById(R.id.thumb_image);
		mThumbImageView.setVisibility(View.INVISIBLE);
		
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mThumbImageView.getLayoutParams();
		layoutParams.width = thumbImageWidth;
		layoutParams.height = thumbImageWidth;
		mThumbImageView.setLayoutParams(layoutParams);
		
		int width = 0;
		int height = 0;
		if (isFromServer)
		{
			width = mImageData.getWidth();
			height = mImageData.getHeight();
		}
		else
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mLocalPath, options);
			width = options.outWidth;
			height = options.outHeight;
		}
		mAttacher = new PhotoViewAttacher(mImageView,width,height);

		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener()
		{

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2)
			{
				getActivity().finish();
			}
		});

		progressBar = (ProgressBar) v.findViewById(R.id.loading);
		progressBar.setVisibility(View.INVISIBLE);
		
		((ImagePagerActivity) getActivity()).detailFragment = this;
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if(isFromServer)
		{
			if(ImageUtil.isFileExist(mImageData.getLocalPath()))
			{
				mThumbImageView.setVisibility(View.INVISIBLE);
				ImageUtil.loadAllianeShareImage(getActivity(), mImageView, mImageData.getLocalPath(),false);
//				ImageUtil.setAllianeShareLocalImage(getActivity(), mImageView, mThumbImageView,progressBar,mImageData);
			}
			else
			{
				mThumbImageView.setVisibility(View.VISIBLE);
				ImageUtil.loadAllianeShareDetailThumbImage(getActivity(), mThumbImageView,mImageView,progressBar, mImageData);
			}
//			ImageUtil.loadAllianeShareImage(getActivity(), mImageView, mImageData,false);
//			ImageUtil.setAllianeShareImage(getActivity(), mImageView, mImageData,false);
		}
		else
		{
			mThumbImageView.setVisibility(View.INVISIBLE);
			ImageUtil.loadAllianeShareImage(getActivity(), mImageView, mLocalPath,false);
//			ImageUtil.setAllianeShareImage(getActivity(), mImageView, mLocalPath);
		}

//		if(isFromServer)
//		{
////			ImageUtil.loadAllianeShareImage(getActivity(), mImageView, mImageData,1);
//			ImageUtil.setAllianeShareImage(getActivity(), mImageView, mImageData,false);
//		}
//		else
//		{
//			ImageUtil.loadAllianeShareImage(getActivity(), mImageView, mLocalPath,1);
////			ImageUtil.setAllianeShareImage(getActivity(), mImageView, mLocalPath);
//		}
		// ImageLoader.getInstance().displayImage(mImageUrl, mImageView, new
		// SimpleImageLoadingListener() {
		// @Override
		// public void onLoadingStarted(String imageUri, View view) {
		// progressBar.setVisibility(View.VISIBLE);
		// }
		//
		// @Override
		// public void onLoadingFailed(String imageUri, View view, FailReason
		// failReason) {
		// String message = null;
		// switch (failReason.getType()) {
		// case IO_ERROR:
		// message = "下载错误";
		// break;
		// case DECODING_ERROR:
		// message = "图片无法显示";
		// break;
		// case NETWORK_DENIED:
		// message = "网络有问题，无法下载";
		// break;
		// case OUT_OF_MEMORY:
		// message = "图片太大无法显示";
		// break;
		// case UNKNOWN:
		// message = "未知的错误";
		// break;
		// }
		// Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
		// progressBar.setVisibility(View.GONE);
		// }
		//
		// @Override
		// public void onLoadingComplete(String imageUri, View view, Bitmap
		// loadedImage) {
		// progressBar.setVisibility(View.GONE);
		// mAttacher.update();
		// }
		// });
	}
	
	@Override
	public void onDestroy()
	{
		System.out.println("ImageDetailFraget onDestroy");
		mLocalPath = null;
		mImageData = null;
		super.onDestroy();
	}
	
	@Override
	public void onPause()
	{
		System.out.println("ImageDetailFraget onPause");
		super.onPause();
	}
	
	public void hideProgress()
	{
		if(getActivity()!=null)
		{
			getActivity().runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if (progressBar != null)
						progressBar.setVisibility(View.INVISIBLE);
					if (mThumbImageView != null)
						mThumbImageView.setVisibility(View.INVISIBLE);
				}
			});
		}
	}
	
	public void onOpenDetailImageSuccessd()
	{
		if(getActivity()!=null)
		{
			getActivity().runOnUiThread(new Runnable()
			{
				
				@Override
				public void run()
				{
					if (progressBar != null)
						progressBar.setVisibility(View.INVISIBLE);
					if (mThumbImageView != null)
						mThumbImageView.setVisibility(View.INVISIBLE);
					
				}
			});
		}
	}
	
	public void updateAttacher()
	{
		if(mAttacher!=null)
			mAttacher.update();
	}
	
	public void loadDetailServerImage()
	{
		System.out.println("ImageDetailFragment loadDetailServerImage");
		if (progressBar != null)
			progressBar.setVisibility(View.VISIBLE);
//		ImageUtil.setAllianeShareImage(getActivity(), mImageView, mImageData, false);
//		ImageUtil.loadAllianeShareDetailImage(getActivity(), mImageView, mImageData);
	}
}
