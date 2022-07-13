package com.elex.im.ui.controller;

import java.util.ArrayList;

import net.londatiga.android.QuickAction;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elex.im.CokConfig;
import com.elex.im.core.IMCore;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.CallBack;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.PermissionManager;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.view.ChatFragment;
import com.elex.im.ui.view.ChatQuickActionFactory;
import com.elex.im.ui.view.MemberSelectorFragment;
import com.elex.im.ui.view.MessagesAdapter;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;

public class MenuController
{
	public static void handleItemClick(final MessagesAdapter adapter, QuickAction source, int pos, int actionId)
	{
		final Msg item = MessagesAdapter.getMsgItemFromQuickAction(source);
		if (item == null)
			return;
		switch (actionId)
		{
			case ChatQuickActionFactory.ID_INVITE:
				GSController.doHostAction("inviteJoinAlliance", item.uid, item.getName(), "", true);
				break;
			case ChatQuickActionFactory.ID_JOIN_ALLIANCE:
				GSController.doHostAction("joinAlliance", item.uid, item.getName(), "", true);
				break;
			case ChatQuickActionFactory.ID_COPY:
				adapter.copyToClipboard(source);
				break;
			case ChatQuickActionFactory.ID_SEND_MAIL:
				if (GSController.isNewMailListEnable)
				{
					GSController.isCreateChatRoom = false;
					GSController.setMailInfo(item.uid, "", item.getName(), MailManager.MAIL_USER);
					UIManager.showChatActivity(UIManager.getCurrentActivity(), CokConfig.getUserChannelType(), false);
				}
				else
				{
					GSController.doHostAction("sendMail", item.uid, item.getName(), "", true);
				}
				break;
			case ChatQuickActionFactory.ID_VIEW_PROFILE:
				if (GSController.isContactMod)
					GSController.doHostAction("showPlayerInfo@mod", item.uid, item.getName(), "", true);
				else
					GSController.doHostAction("showPlayerInfo", item.uid, item.getName(), "", true);
				break;
			case ChatQuickActionFactory.ID_BLOCK:
				showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_SHIELD_PLAYER, item.getName()), item, UserManager.BLOCK_LIST);
				break;
			case ChatQuickActionFactory.ID_UNBLOCK:
				JniController.getInstance().excuteJNIVoidMethod("unShieldPlayer", new Object[] { item.uid, item.getName() });
				UserManager.getInstance().removeRestrictUser(item.uid, UserManager.BLOCK_LIST);
				break;
			case ChatQuickActionFactory.ID_BAN:
				showBanConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_BAN, item.getName()), item);
				break;
			case ChatQuickActionFactory.ID_UNBAN:
				if (item.isHornMessage())
				{
					JniController.getInstance().excuteJNIVoidMethod("unBanPlayerNotice", new Object[] { item.uid });
					UserManager.getInstance().removeRestrictUser(item.uid, UserManager.BAN_NOTICE_LIST);
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod("unBanPlayer", new Object[] { item.uid });
					UserManager.getInstance().removeRestrictUser(item.uid, UserManager.BAN_LIST);
				}
				break;
			case ChatQuickActionFactory.ID_TRANSLATE:
				// final MessageHolder holder = (MessageHolder)
				// source.currentTextView.getTag();
				adapter.showTranslatedLanguage((TextView) source.currentTextView, item);
				break;
			case ChatQuickActionFactory.ID_ORIGINAL_LANGUAGE:
				// final MessageHolder holder2 = (MessageHolder)
				// source.currentTextView.getTag();
				adapter.showOriginalLanguage((TextView) source.currentTextView, item);
				break;
			case ChatQuickActionFactory.ID_VIEW_BATTLE_REPORT:
				GSController.doHostAction("viewBattleReport", item.uid, "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_DETECT_REPORT:
				GSController.doHostAction("viewDetectReport", item.uid, "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_EQUIPMENT:
				GSController.doHostAction("showEquipment", "", "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_REPORT_PLAYER_CHAT:
				if (GSController.oldReportContentTime == 0
						|| System.currentTimeMillis() - GSController.oldReportContentTime >= GSController.REPORT_CONTENT_TIME_INTERVAL)
				{
					if (UserManager.getInstance().isInReportContentList(item, UserManager.REPORT_CONTETN_LIST))
					{
						showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CONTETN_REPORTED));
					}
					else
					{
						showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT), item, UserManager.REPORT_CONTETN_LIST);
					}
				}
				else
				{
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT_INTERVAL));
				}
				break;
			case ChatQuickActionFactory.ID_REPORT_HEAD_IMG:
				if (UserManager.getInstance().isInRestrictList(item.uid, UserManager.REPORT_LIST))
				{
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_HEADIMG_REPORTED));
				}
				else
				{
					showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_HEADIMG, item.getName()), item,
							UserManager.REPORT_LIST);
				}
				break;
			case ChatQuickActionFactory.ID_TRANSLATE_NOT_UNDERSTAND:
				if (UserManager.getInstance().isInReportContentList(item, UserManager.REPORT_TRANSLATION_LIST))
				{
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATION_REPORTED));
				}
				else
				{
					showConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRASNALTION), item,
							UserManager.REPORT_TRANSLATION_LIST);
				}
				break;
			case ChatQuickActionFactory.ID_SAY_HELLO:
				sayHello(item);
				break;
			case ChatQuickActionFactory.ID_VIEW_RALLY_INFO:
				GSController.doHostAction("viewRallyInfo", "", "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_LOTTERY_SHARE:
				GSController.doHostAction("viewLotteryShare", "", "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_ALLIANCETASK_SHARE:
				GSController.doHostAction("viewAllianceTaskShare", "", "", "", true);
				break;
			case ChatQuickActionFactory.ID_VIEW_RED_PACKAGE:
				GSController.doHostAction("viewRedPackage", "", "", item.attachmentId, true);
				break;
			case ChatQuickActionFactory.ID_VIEW_ALLIANCE_TREASURE:
				if(CokConfig.isInDragonSencen() || CokConfig.isInAncientSencen())
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_DISABLE));
				else if(GSController.currentLevel < 10)
					showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_WARN));
				else
					GSController.doHostAction("viewAllianceTreasure", "", "", item.getAllianceTreasureInfo(2), true);
				break;
			case ChatQuickActionFactory.ID_SWITCH_TO_RECEIVER:
				ConfigManager.playAudioBySpeaker = false;
				adapter.onAudioPlayChannelChanged();
				break;
			case ChatQuickActionFactory.ID_SWITCH_TO_SPEAKER:
				ConfigManager.playAudioBySpeaker = true;
				adapter.onAudioPlayChannelChanged();
				break;
			case ChatQuickActionFactory.ID_VIEW_ALLIANCEHELP:
				GSController.doHostAction("showAllianceHelp", "", "", item.attachmentId, false);
				break;
			default:
				break;
		}
	}

	private static void sayHello(Msg msgItem)
	{
		String[] dialogs = { LanguageKeys.SAY_HELLO1, LanguageKeys.SAY_HELLO2, LanguageKeys.SAY_HELLO3, LanguageKeys.SAY_HELLO4 };
		String helloText = "Welcome!";
		if (dialogs.length > 0)
		{
			long randomNum = Math.round(Math.random() * dialogs.length);
			int index = (int) randomNum;
			if (index >= 0 && index < dialogs.length)
			{
				if (StringUtils.isNotEmpty(dialogs[index]) && StringUtils.isNumeric(dialogs[index]))
				{
					String key = LanguageManager.getLangByKey(dialogs[index]);
					if (key.contains("{0}"))
					{
						if (key.contains("{1}"))
						{
							helloText = LanguageManager.getLangByKey(dialogs[index], msgItem.getASN(), msgItem.getName());
						}
						else
						{
							helloText = LanguageManager.getLangByKey(dialogs[index], msgItem.getASN());
						}
					}
					else
					{
						helloText = key;
					}
				}
			}
			else
			{
				helloText = LanguageManager.getLangByKey(LanguageKeys.SAY_HELLO1);
			}

		}
		else
		{
			helloText = LanguageManager.getLangByKey(LanguageKeys.SAY_HELLO1);
		}
		if (StringUtils.isEmpty(helloText))
			helloText = "Welcome!";
		final String text = helloText;
		IMCore.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (UIManager.getChatFragment() != null)
					{
						UIManager.getChatFragment().setEditText(text);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	private static Window initAlertDialog(AlertDialog dlg, int id)
	{
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(id);
		window.setBackgroundDrawable(new ColorDrawable());
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		return window;
	}

	private static void setDismissListener(View frame, final Dialog dlg)
	{
		frame.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				dlg.dismiss();
				return true;
			}
		});
	}

	private static void setDismissListener(View frame, final Dialog dlg, final OnClickListener onOKClickListener)
	{
		frame.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				onOKClickListener.onClick(v);
				dlg.dismiss();
				return true;
			}
		});
	}

	private static void showBanConfirm(String content, final Msg item)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;

		Window window = initAlertDialog(dlg, R.layout.cs__ban_confirm_dialog);
		setDismissListener((FrameLayout) window.findViewById(R.id.banConfirmFrameLayout), dlg);

		TextView alertTextView = (TextView) window.findViewById(R.id.textView1);
		alertTextView.setText(content);

		final ArrayList<CheckBox> checkBoxs = new ArrayList<CheckBox>();
		int[] checkBoxIds = { R.id.checkBox1, R.id.checkBox2, R.id.checkBox3, R.id.checkBox4 };
		String[] timeValues = { "1", "2", "3", "4" };
		String[] banTimeArr = GSController.banTime.split("\\|");
		if (banTimeArr.length == 4)
		{
			timeValues[0] = banTimeArr[0];
			timeValues[1] = banTimeArr[1];
			timeValues[2] = banTimeArr[2];
			timeValues[3] = banTimeArr[3];
		}
		String timeStr = LanguageManager.getLangByKey(LanguageKeys.TIP_TIME);

		OnClickListener checkOnClickListener = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				for (int i = 0; i < checkBoxs.size(); i++)
				{
					checkBoxs.get(i).setChecked((i + 1) == ((Integer) (v.getTag())).intValue());
				}
			}
		};

		for (int i = 0; i < checkBoxIds.length; i++)
		{
			CheckBox checkBox = (CheckBox) window.findViewById(checkBoxIds[i]);
			checkBox.setText(" " + timeValues[i] + timeStr);
			checkBox.setTag(Integer.valueOf(i + 1));
			checkBox.setOnClickListener(checkOnClickListener);
			checkBoxs.add(checkBox);
		}

		// 为确认按钮添加事件,执行退出应用操作
		Button ok = (Button) window.findViewById(R.id.okBanBtn);
		ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
		ok.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				int selectIndex = 0;
				for (int i = 0; i < checkBoxs.size(); i++)
				{
					if (checkBoxs.get(i).isChecked())
					{
						selectIndex = i;
						break;
					}
				}

				if (item.isHornMessage())
				{
					JniController.getInstance().excuteJNIVoidMethod("banPlayerNoticeByIndex",
							new Object[] { item.uid, Integer.valueOf(selectIndex) });
					UserManager.getInstance().addRestrictUser(item.uid, UserManager.BAN_NOTICE_LIST);
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod("banPlayerByIndex",
							new Object[] { item.uid, Integer.valueOf(selectIndex) });
					UserManager.getInstance().addRestrictUser(item.uid, UserManager.BAN_LIST);
				}

			}
		});
		// 关闭alert对话框架
		Button cancel = (Button) window.findViewById(R.id.cancelBanBtn);
		cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		});

		adjustBanDialog(alertTextView, ok, cancel, checkBoxs);
	}

	private static void adjustBanDialog(TextView alertTextView, Button ok, Button cancel, final ArrayList<CheckBox> checkBoxs)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(ok, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel, ConfigManager.scaleRatio);
			for (int i = 0; i < checkBoxs.size(); i++)
			{
				ScaleUtil.adjustTextSize(checkBoxs.get(i), ConfigManager.scaleRatio);
			}
		}
	}

	private static void showConfirm(String content, final Msg item, final int type)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				if (type == UserManager.BLOCK_LIST)
				{
					JniController.getInstance().excuteJNIVoidMethod("shieldPlayer", new Object[] { item.uid });
					UserManager.getInstance().addRestrictUser(item.uid, type);
				}
				else if (type == UserManager.REPORT_LIST)
				{
					JniController.getInstance().excuteJNIVoidMethod("reportCustomHeadImg", new Object[] { item.uid });
					Toast toast = Toast.makeText(UIManager.getCurrentActivity(),
							LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_HEADIMG_SUCCESS), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, UIManager.getChatActivity().getToastPosY());
					toast.show();
					UserManager.getInstance().addRestrictUser(item.uid, type);
				}
				else if (type == UserManager.REPORT_CONTETN_LIST)
				{
					GSController.oldReportContentTime = System.currentTimeMillis();
					JniController.getInstance().excuteJNIVoidMethod("reportPlayerChatContent", new Object[] { item.uid, item.msg });
					Toast toast = Toast.makeText(UIManager.getCurrentActivity(),
							LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT_SUCCESS), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, UIManager.getChatActivity().getToastPosY());
					toast.show();
					UserManager.getInstance().addReportContent(item, type);
				}
				else if (type == UserManager.REPORT_TRANSLATION_LIST)
				{

					JniController.getInstance().excuteJNIVoidMethod("translateOptimize",
							new Object[] { "notunderstand", item.originalLang, item.translatedLang, item.msg, item.translateMsg });
					Toast toast = Toast.makeText(UIManager.getCurrentActivity(),
							LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRANSLATION_SUCCESS), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, UIManager.getChatActivity().getToastPosY());
					toast.show();
					UserManager.getInstance().addReportContent(item, type);
				}

			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

//	public static void showAllianceShareDeleteComfirm(String content, final AllianceShareInfo info)
//	{
//		final AlertDialog dlg = createAlertDialog();
//		if (dlg == null)
//			return;
//		setDialogView(dlg, content, new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dlg.cancel();
//				JniController.getInstance().excuteJNIVoidMethod(
//						"deleteAllianceShare",
//						new Object[] {
//								info.getId(),
//								UserManager.getInstance().getCurrentUser().allianceId,
//								"",
//								"",
//								Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_MSG) });
//			}
//		}, 0, true);
//	}
//	
//	public static void showAllianceShareCommentDeleteComfirm(String content, final AllianceShareComment comment,final String allianceShareSender)
//	{
//		final AlertDialog dlg = createAlertDialog();
//		if (dlg == null)
//			return;
//		setDialogView(dlg, content, new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				dlg.cancel();
//				JniController.getInstance().excuteJNIVoidMethod(
//						"deleteAllianceShare",
//						new Object[] {
//								comment.getId(),
//								UserManager.getInstance().getCurrentUser().allianceId,
//								comment.getFid(),
//								allianceShareSender,
//								Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_COMMENT) });
//			}
//		}, 0, true);
//	}

	private static void adjustConfirmDialog(TextView alertTextView, Button ok, Button cancel)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(ok, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel, ConfigManager.scaleRatio);
		}
	}

	private static void adjustConfirmCoinDialog(TextView alertTextView, TextView okTextView, TextView coinTextView, Button cancel)
	{
		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(okTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(coinTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel, ConfigManager.scaleRatio);
		}
	}

	public static void showReSendConfirm(String content, final Msg msgItem, final ChatFragment chatFragmentNew)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				if (msgItem.isHornMessage())
				{
					int hornBanedTime = JniController.getInstance().excuteJNIMethod("getHornBanedTime", null);
					if (hornBanedTime == 0)
					{
						int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
						String horn = LanguageManager.getLangByKey(LanguageKeys.TIP_HORN);
						if (price == 0)
						{
							showResendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn), msgItem, chatFragmentNew);
						}
						else if (price > 0)
						{
							showResendHornWithCornConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn), msgItem,
									price, chatFragmentNew);
						}
					}
					else
					{
						Toast.makeText(UIManager.getCurrentActivity(), "you have been baned!", Toast.LENGTH_SHORT).show();
					}
				}
				else if (msgItem.isAudioMessage())
				{
					chatFragmentNew.resendAudioMsg(msgItem);
				}
				else
				{
					chatFragmentNew.resendMsg(msgItem, false, false);
				}

			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showResendHornMessageConfirm(String content, final Msg msgItem, final ChatFragment chatFragmentNew)
	{
		final int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				chatFragmentNew.resendMsg(msgItem, true, false);
			}
		};
		setDialogView(dlg, content, okOnlickListener, price, true);
	}

	public static void showResendHornWithCornConfirm(String content, final Msg msgItem, final int price, final ChatFragment chatFragmentNew)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough", new Object[] { Integer.valueOf(price) });
				if (isCornEnough)
				{
					chatFragmentNew.resendMsg(msgItem, true, true);
				}
				else
				{
					showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
				}
			}
		};

		setDialogViewWithCoin(dlg, content, okOnlickListener, price, true);
	}

	public static void showSendHornMessageConfirm(String content, final CallBack callback)
	{
		final int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				callback.onCallback();
			}
		};
		setDialogView(dlg, content, okOnlickListener, price, true);
	}

	public static void showSendHornWithCornConfirm(String content, final int price, final CallBack callback)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough", new Object[] { Integer.valueOf(price) });
				if (isCornEnough)
				{
					callback.onCallback();
				}
				else
				{
					showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
				}
			}
		};
		setDialogViewWithCoin(dlg, content, okOnlickListener, price, true);
	}

	public static void showCornNotEnoughConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, false);
	}
	
//	public static void showExitAllianceShareConfirm(String content,final MyActionBarActivity activity)
//	{
//		final AlertDialog dlg = createAlertDialog();
//		if (dlg == null)
//			return;
//		OnClickListener okOnlickListener = new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				dlg.cancel();
//				activity.exitActivity();
//			}
//		};
//		
//		try
//		{
//			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
//			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);
//
//			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
//			Button ok = (Button) window.findViewById(R.id.exitBtn0);
//			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
//
//			adjustConfirmDialog(alertTextView, ok, cancel);
//
//			alertTextView.setText(content);
//
//			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TEXT_ABORT));
//
//			ok.setOnClickListener(okOnlickListener);
//
//			cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TEXT_KEEPON));
//			cancel.setOnClickListener(new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					dlg.cancel();
//				}
//			});
//		}
//		catch (Exception e)
//		{
//			LogUtil.printException(e);
//		}
//	}

	public static void showChatRestrictConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				GSController.doHostAction("changeNickName", "", "", "", false);
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, false).setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CHANGE_NAME));
	}

	public static void showCreateChatRoomConfirm(final MyActionBarActivity activity, String content, final ArrayList<String> memberUidAdd)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				
				String roomName = MemberSelectorFragment.roomName;
				if(StringUtils.isEmpty(roomName))
				{
					String memberNameStr = UserManager.getInstance().createNameStr(memberUidAdd);
					if(!memberNameStr.contains(UserManager.getInstance().getCurrentUser().userName))
						roomName = UserManager.getInstance().getCurrentUser().userName + "," + memberNameStr;
					else
						roomName = memberNameStr;
				}
				
				JniController.getInstance().excuteJNIVoidMethod(
						"createChatRoom",
						new Object[] {
								UserManager.getInstance().createNameStr(memberUidAdd),
								UserManager.getInstance().createUidStr(memberUidAdd),
								roomName,
								"" });
				activity.exitActivity();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showChatRoomManagerConfirm(final MyActionBarActivity activity, String content, final ArrayList<String> memberUidAdd,
			final ArrayList<String> memberUidRemoved, final String roomName)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				if (GSController.isCreateChatRoom)
				{
					if (GSController.isNewMailListEnable)
					{
						GSController.isCreateChatRoom = true;
						Intent intent = new Intent();
						intent.putExtra("roomName", MemberSelectorFragment.roomName);
						intent.putExtra("uidStr", UserManager.getInstance().createUidStr(memberUidAdd));
						intent.putExtra("nameStr", UserManager.getInstance().createNameStr(memberUidAdd));
						activity.setResult(Activity.RESULT_OK, intent);
						activity.exitActivity();
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod(
								"selectChatRoomMember",
								new Object[] {
										MemberSelectorFragment.roomName,
										UserManager.getInstance().createNameStr(memberUidAdd),
										UserManager.getInstance().createUidStr(memberUidAdd) });
						activity.exitActivity();
					}
				}
				else
				{
					if (memberUidAdd != null && memberUidAdd.size() > 0)
					{
						JniController.getInstance().excuteJNIVoidMethod(
								"inviteChatRoomMember",
								new Object[] {
										UserManager.getInstance().getCurrentMail().opponentUid,
										UserManager.getInstance().createNameStr(memberUidAdd),
										UserManager.getInstance().createUidStr(memberUidAdd) });
					}
					if (memberUidRemoved != null && memberUidRemoved.size() > 0)
					{
						JniController.getInstance().excuteJNIVoidMethod(
								"kickChatRoomMember",
								new Object[] {
										UserManager.getInstance().getCurrentMail().opponentUid,
										UserManager.getInstance().createNameStr(memberUidRemoved),
										UserManager.getInstance().createUidStr(memberUidRemoved) });
					}
					if (roomName != null && !roomName.equals(""))
					{
						JniController.getInstance().excuteJNIVoidMethod("modifyChatRoomName",
								new Object[] { UserManager.getInstance().getCurrentMail().opponentUid, roomName });
					}

					activity.exitActivity();
				}
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showInviteChatRoomMemberConfirm(final MyActionBarActivity activity, String content,
			final ArrayList<String> memberUidAdd)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();

				if (GSController.isCreateChatRoom)
				{
					if (GSController.isNewMailListEnable)
					{
						GSController.isCreateChatRoom = true;
						Intent intent = new Intent();
						intent.putExtra("roomName", MemberSelectorFragment.roomName);
						intent.putExtra("uidStr", UserManager.getInstance().createUidStr(memberUidAdd));
						intent.putExtra("nameStr", UserManager.getInstance().createNameStr(memberUidAdd));
						activity.setResult(Activity.RESULT_OK, intent);
						activity.exitActivity();
					}
					else
					{
						JniController.getInstance().excuteJNIVoidMethod(
								"selectChatRoomMember",
								new Object[] {
										MemberSelectorFragment.roomName,
										UserManager.getInstance().createNameStr(memberUidAdd),
										UserManager.getInstance().createUidStr(memberUidAdd) });
						activity.exitActivity();
					}
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod(
							"inviteChatRoomMember",
							new Object[] {
									UserManager.getInstance().getCurrentMail().opponentUid,
									UserManager.getInstance().createNameStr(memberUidAdd),
									UserManager.getInstance().createUidStr(memberUidAdd) });
					activity.exitActivity();
				}
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void quitChatRoomConfirm(final MyActionBarActivity activity, String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				JniController.getInstance().excuteJNIVoidMethod("quitChatRoom",
						new Object[] { UserManager.getInstance().getCurrentMail().opponentUid });
				activity.exitActivity();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	private static AlertDialog createAlertDialog()
	{
		if (UIManager.getCurrentActivity() == null)
			return null;
		return createAlertDialog(UIManager.getCurrentActivity());
	}

	private static AlertDialog createAlertDialog(Context context)
	{
		try
		{
			return new AlertDialog.Builder(context).create();
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
			return null;
		}
	}

	private static Button setDialogView(final AlertDialog dlg, String content, OnClickListener onOKClickListener, int corn,
			boolean showCancelBtn)
	{
		return setDialogView(dlg, content, onOKClickListener, corn, showCancelBtn, false);
	}

	private static Button setDialogView(final AlertDialog dlg, String content, OnClickListener onOKClickListener, int corn,
			boolean showCancelBtn, boolean takeDismissAsOK)
	{
		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
			cancel.setVisibility(showCancelBtn ? View.VISIBLE : View.GONE);

			adjustConfirmDialog(alertTextView, ok, cancel);

			alertTextView.setText(content);

			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));

			ok.setOnClickListener(onOKClickListener);

			cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
			cancel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dlg.cancel();
				}
			});
			return ok;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
		return null;
	}

	private static void setDialogViewWithCoin(final AlertDialog dlg, String content, OnClickListener onOKClickListener, int coin,
			boolean cancelBtnShow)
	{
		Window window = initAlertDialog(dlg, R.layout.cs__gold_confirm_dialog);
		setDismissListener((FrameLayout) window.findViewById(R.id.goldConfirmFrameLayout), dlg);

		TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
		LinearLayout confirm_layout = (LinearLayout) window.findViewById(R.id.confirm_layout);
		TextView coin_count_text = (TextView) window.findViewById(R.id.confirm_coin_count);
		TextView ok_btn_text = (TextView) window.findViewById(R.id.ok_btn_text);
		Button cancel = (Button) window.findViewById(R.id.exitBtn1);
		cancel.setVisibility(cancelBtnShow ? View.VISIBLE : View.GONE);

		adjustConfirmCoinDialog(alertTextView, ok_btn_text, coin_count_text, cancel);

		alertTextView.setText(content);
		coin_count_text.setText("" + coin);

		ok_btn_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));

		confirm_layout.setOnClickListener(onOKClickListener);

		cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
			}
		});
	}

	public static void showContentConfirm(String content)
	{
//		final AlertDialog dlg = createAlertDialog();
//		if (dlg == null)
//			return;
//		OnClickListener okOnlickListener = new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				dlg.cancel();
//			}
//		};
//
//		try
//		{
//			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
//
//			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);
//
//			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
//			Button ok = (Button) window.findViewById(R.id.exitBtn0);
//			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
//			cancel.setVisibility(View.GONE);
//			adjustConfirmDialog(alertTextView, ok, cancel);
//			alertTextView.setText(content);
//			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
//			ok.setOnClickListener(okOnlickListener);
//		}
//		catch (Exception e)
//		{
//			LogUtil.printException(e);
//		}
		
		if(UIManager.getCurrentActivity()!=null)
		{
			LayoutInflater inflater = UIManager.getCurrentActivity().getLayoutInflater();
			if(inflater!=null)
			{
				View view = inflater.inflate(R.layout.content_toast, null);
				TextView textView = (TextView)view.findViewById(R.id.alertTextView);
				if(textView!=null)
				{
					textView.setText(content);
					Toast toast=new Toast(UIManager.getCurrentActivity());
					toast.setGravity(Gravity.CENTER, 0, 200);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setView(view);
					toast.show();
				}
			}
		}
	}

//	public static void showDeleteChannelConfirm(String content, final Channel channel)
//	{
//		final AlertDialog dlg = createAlertDialog();
//		if (dlg == null)
//			return;
//		OnClickListener okOnlickListener = new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				GSController.getChannelListFragment().actualDeleteSingleChannel(channel);
//				dlg.cancel();
//			}
//		};
//
//		setDialogView(dlg, content, okOnlickListener, 0, true);
//	}
//
//	public static void showOperateMutiMail(String content, final List<ChannelListItem> list, final int type)
//	{
//		final AlertDialog dlg = createAlertDialog();
//		if (dlg == null)
//			return;
//		OnClickListener okOnlickListener = new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				GSController.getChannelListFragment().comfirmOperateMutiMail(list, type);
//				dlg.cancel();
//			}
//		};
//
//		setDialogView(dlg, content, okOnlickListener, 0, true);
//	}

	public static void showAllowPermissionConfirm(final Activity activity, final String content, final String permissionKey)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final AlertDialog dlg = createAlertDialog(activity);
					if (dlg == null)
						return;
					OnClickListener okOnlickListener = new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							PermissionManager.onNotifyPermissionConfirm(permissionKey, UIManager.getCurrentActivity());
							dlg.cancel();
						}
					};
					setDialogView(dlg, content, okOnlickListener, 0, false, true);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}
}
