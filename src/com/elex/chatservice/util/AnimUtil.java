package com.elex.chatservice.util;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.elex.chatservice.R;

public class AnimUtil
{
	public static void animVisibility(View view, int visibility, Context context)
	{
		view.setVisibility(visibility);
		Animation anim = AnimationUtils.loadAnimation(context, getAnimByVisibility(visibility));
//		anim.setFillAfter(true);
		if (visibility == View.VISIBLE)
			view.startAnimation(anim);
	}

	private static int getAnimByVisibility(int visibility)
	{
		if (visibility == View.VISIBLE)
		{
			return R.anim.scale_in;
		}
		else
		{
			return R.anim.scale_out;
		}
	}
}
