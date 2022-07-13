package com.elex.chatservice.view.allianceshare.util;

import java.io.File;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.module.GlideModule;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.db.DBHelper;

public class ChatServiceGlideModule implements GlideModule
{

	@Override
	public void applyOptions(Context context, GlideBuilder builder)
	{
		builder.setDiskCache(new DiskCache.Factory()
		{

			@Override
			public DiskCache build()
			{
				File cacheLocation = DBHelper.getLocalDirectoryFile(ChatServiceController.hostActivity, "alliance_share_image");
				return DiskLruCacheWrapper.get(cacheLocation, DEFAULT_DISK_CACHE_SIZE);
			}
		});
		
	}

	@Override
	public void registerComponents(Context arg0, Glide arg1)
	{
		// TODO Auto-generated method stub

	}

}
