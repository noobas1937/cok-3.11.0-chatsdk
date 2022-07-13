package com.elex.im.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.elex.im.CokChannelDef;
import com.elex.im.core.IMCore;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.view.ChatActivity;
import com.elex.im.ui.view.ChatFragment;
import com.elex.im.ui.view.ChatRoomNameModifyActivity;
import com.elex.im.ui.view.ChatRoomSettingActivity;
import com.elex.im.ui.view.MemberSelectorActivity;
import com.elex.im.ui.view.WriteMailActivity;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;

public class UIManager
{
	private static Activity							currentActivity;
	public static boolean							isNativeShowing		= false;								// 仅在IF.onResume中重置为false，主要被IF使用

	private static Timer							flyHintTimer;

	private static int								flyHintCount;
	/** 正在打开native界面，可能是从游戏打开，或从native打开另一个新的，或退出当前的native显示堆栈中的另一个 */
	public static boolean							isNativeOpenning	= false;								// 主要被原生activity使用

	private static ArrayList<MyActionBarActivity>	activityStack		= new ArrayList<MyActionBarActivity>();

	public static void setCurrentActivity(Activity a)
	{
		UIManager.currentActivity = a;
	}

	public static Activity getCurrentActivity()
	{
		return UIManager.currentActivity;
	}

	public static ChatActivity getChatActivity()
	{
		if (getCurrentActivity() != null && getCurrentActivity() instanceof ChatActivity)
		{
			return (ChatActivity) getCurrentActivity();
		}
		return null;
	}

	public static ChatFragment getChatFragment()
	{
		if (getChatActivity() != null && getChatActivity().fragment != null
				&& getChatActivity().fragment instanceof ChatFragment)
		{
			return (ChatFragment) getChatActivity().fragment;
		}
		return null;
	}

	public static ChatRoomSettingActivity getChatRoomSettingActivity()
	{
		if (getCurrentActivity() != null
				&& getCurrentActivity() instanceof ChatRoomSettingActivity)
		{
			return (ChatRoomSettingActivity) getCurrentActivity();
		}
		return null;
	}

	public static void showChatActivity(Activity a, int channelType, boolean rememberPosition)
	{
		if (a == null)
			return;
	
		ChatFragment.rememberPosition = rememberPosition;
	
		Intent intent = null;
		try
		{
			if (channelType >= 0)
			{
				// 可能出异常
				intent = new Intent(a, ChatActivity.class);
				intent.putExtra("channelType", channelType);
			}
			
			CokChannelDef.trackActivityPageView(channelType, rememberPosition);
	
			UIManager.showActivity(a, ChatActivity.class, true, true, intent, false, false);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return;
		}
	}

	private static void showActivity(Activity a, Class<?> cls, boolean newTask, boolean clearTop, Intent intent, boolean requestResult,
				boolean popStackAnimation)
		{
			ArrayList<Object> args = new ArrayList<Object>();
			args.add("class");
			args.add(cls.getSimpleName());
			if (intent != null)
			{
				for (Iterator<String> iterator = intent.getExtras().keySet().iterator(); iterator.hasNext();)
				{
					String key = (String) iterator.next();
					args.add(key);
					args.add(intent.getExtras().get(key));
				}
			}
			LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, args.toArray(new Object[0]));
	
			UIManager.isNativeOpenning = true;
			isNativeShowing = true;
			GSController.isReturningToGame = false;
	//		ChannelListFragment.preventSecondChannelId = false;
	
			Intent i = intent != null ? intent : new Intent(a, cls);
			if (clearTop)
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
			if (!requestResult)
			{
				a.startActivity(i);
			}
			else
			{
				a.startActivityForResult(i, 0);
			}
	
			if (!popStackAnimation)
			{
				a.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
			else
			{
				a.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
		}

	public static void showMemberSelectorActivity(Activity a, boolean requestResult)
	{
		LogUtil.trackPageView("ShowMemberSelector");
		showActivity(a, MemberSelectorActivity.class, true, false, null, requestResult, false);
	}

	public static void showChatRoomSettingActivity(Activity a)
	{
		showActivity(a, ChatRoomSettingActivity.class, true, false, null, false, false);
	}

	public static void showChatRoomNameModifyActivity(Activity a)
	{
		showActivity(a, ChatRoomNameModifyActivity.class, true, false, null, false, false);
	}

	public static void showWriteMailActivity(Activity a, boolean clearTop, String roomName, String uidStr, String nameStr)
	{
		LogUtil.trackPageView("ShowWriteMail");
		Intent intent = null;
	
		if (StringUtils.isNotEmpty(roomName) || StringUtils.isNotEmpty(uidStr) || StringUtils.isNotEmpty(nameStr))
		{
			intent = new Intent(a, WriteMailActivity.class);
			intent.putExtra("roomName", roomName);
			intent.putExtra("memberUids", uidStr);
			intent.putExtra("memberNames", nameStr);
		}
	
		showActivity(a, WriteMailActivity.class, true, clearTop, intent, false, clearTop);
	}

	public static void showGameActivity(Activity a)
	{
		UIManager.showGameActivity(a, false);
	}

	public static void showGameActivity(Activity a, boolean reverseAnimation)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW);
		GSController.isReturningToGame = true;
		clearActivityStack();
	
		Intent intent = new Intent(a, IMCore.hostClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		a.startActivity(intent);
		if (!reverseAnimation)
		{
			a.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
		else
		{
			a.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left_fast);
		}
	}

	public static void pushActivity(MyActionBarActivity a)
	{
		if (!activityStack.contains(a))
		{
			activityStack.add(a);
		}
		else
		{
			LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_VIEW, "pushActivity already have", activityStack.size());
		}
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "activityStack.size()", activityStack.size());
	}

	public static void popActivity(MyActionBarActivity a)
	{
		if (activityStack.contains(a))
		{
			activityStack.remove(a);
		}
		LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "activityStack.size()", activityStack.size());
	}

	public static int getNativeActivityCount()
	{
		return activityStack.size();
	}

	public static void clearActivityStack()
	{
		activityStack.clear();
	}
	
//	public static MemberSelectorFragment getMemberSelectorFragment()
//	{
//		if (GSController.getCurrentActivity() != null && GSController.getCurrentActivity().fragment != null
//				&& GSController.getCurrentActivity().fragment instanceof MemberSelectorFragment)
//		{
//			return (MemberSelectorFragment) GSController.getCurrentActivity().fragment;
//		}
//		return null;
//	}

	public static void stopFlyHintTimer()
	{
		if (flyHintTimer != null)
		{
			flyHintTimer.cancel();
			flyHintTimer.purge();
		}
	}

	public static void flyHint(String icon, String titleText, String contentText, float time, float dy, boolean useDefaultIcon)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "contentText", contentText, "titleText", titleText);

		if (!isNativeShowing || UIManager.getCurrentActivity() == null)
			return;

		final String text = contentText;
		final int duration = time > 0 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;

		UIManager.getCurrentActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (UIManager.getCurrentActivity().getApplicationContext() != null && text != null)
					{
						Toast toast = Toast.makeText(UIManager.getChatActivity().getApplicationContext(), text, duration);
						toast.setGravity(Gravity.TOP, 0, UIManager.getChatActivity().getToastPosY());
						toast.show();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public static void flySystemUpdateHint(double countDown, boolean isFlyHintLogin, boolean isLogin, String tip, String icon)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "tip", tip, "isFlyHintLogin", isFlyHintLogin, "countDown",
				countDown);

		if (!isNativeShowing || UIManager.getCurrentActivity() == null)
			return;
		stopFlyHintTimer();
		flyHintTimer = new Timer();
		final String text = tip;
		flyHintCount = (int) countDown / 10;
		final boolean flyHintLogin = isFlyHintLogin;

		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				UIManager.getCurrentActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							String showText = "";
							if (flyHintLogin)
								if (flyHintCount / 60 > 0)
									showText = text
											+ "\n"
											+ LanguageManager.getLangByKey(LanguageKeys.FLYHINT_DOWN_MIN, String.valueOf(flyHintCount / 60));
								else
									showText = text + "\n"
											+ LanguageManager.getLangByKey(LanguageKeys.FLYHINT_DOWN_SECOND, String.valueOf(flyHintCount));
							if (UIManager.getCurrentActivity().getApplicationContext() != null)
							{
								Toast toast = Toast.makeText(UIManager.getCurrentActivity().getApplicationContext(), showText,
										1);
								toast.setGravity(Gravity.TOP, 0, UIManager.getChatActivity().getToastPosY());
								toast.show();
							}
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});

				flyHintCount--;
				if (flyHintCount <= 0)
				{
					stopFlyHintTimer();
				}
			}

		};
		flyHintTimer.schedule(timerTask, 0, 10000);
	}

	public static void toggleFullScreen(final boolean fullscreen, final boolean noTitle, final Activity activity)
	{
		activity.runOnUiThread(new Runnable()
		{
			public void run()
			{
				try
				{
					// TODO 删除noTitle参数
					if (noTitle)
					{
						activity.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
					}
					else
					{
						// activity.requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);
						// activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);//
						// 去掉标题栏
					}
					if (fullscreen)
					{
						activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
					else
					{
						activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
				}
				catch (Exception e)
				{
				}
			}
		});
	}

}
