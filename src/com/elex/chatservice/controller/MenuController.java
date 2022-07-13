package com.elex.chatservice.controller;

import java.util.ArrayList;
import java.util.List;

import net.londatiga.android.QuickAction;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.R;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.TranslateManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.ChatQuickActionFactory;
import com.elex.chatservice.view.MemberSelectorFragment;
import com.elex.chatservice.view.MessagesAdapter;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;

public class MenuController
{
	public static void handleItemClick(final MessagesAdapter adapter, QuickAction source, int pos, int actionId)
	{
		final MsgItem item = MessagesAdapter.getMsgItemFromQuickAction(source);
		if (item == null)
			return;

		if (actionId < ChatQuickActionFactory.ID_VIEW_KING_INFO)
		{
			switch (actionId)
			{
				case ChatQuickActionFactory.ID_INVITE:
					ChatServiceController.doHostAction("inviteJoinAlliance", item.uid, item.getName(), "", true);
					break;
				case ChatQuickActionFactory.ID_JOIN_ALLIANCE:
					ChatServiceController.doHostAction("joinAlliance", item.uid, item.getName(), "", true);
					break;
				case ChatQuickActionFactory.ID_COPY:
					adapter.copyToClipboard(source);
					break;
				case ChatQuickActionFactory.ID_SEND_MAIL:
					if (ChatServiceController.isNewMailListEnable)
					{
						ChatServiceController.isCreateChatRoom = false;
						ServiceInterface.setMailInfo(item.uid, "", item.getName(), MailManager.MAIL_USER);
						ServiceInterface.showChatActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_USER, false);
					}
					else
					{
						ChatServiceController.doHostAction("sendMail", item.uid, item.getName(), "", true);
					}
					break;
				case ChatQuickActionFactory.ID_VIEW_PROFILE:
					if (ChatServiceController.isModContactMode())
						ChatServiceController.doHostAction("showPlayerInfo@mod", item.uid, item.getName(), "", true);
					else if (ChatServiceController.isDriftingBottleContactMode())
						ChatServiceController.doHostAction("showPlayerInfo@driftingbottle", item.uid, item.getName(), "", true);
					else if (ChatServiceController.isNearbyContactMode())
						ChatServiceController.doHostAction("showPlayerInfo@nearby", item.uid, item.getName(), "", true);
					else
						ChatServiceController.doHostAction("showPlayerInfo", item.uid, item.getName(), "", true);
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
					ChatServiceController.doHostAction("viewBattleReport", item.uid, "", item.attachmentId, true);
					break;
				case ChatQuickActionFactory.ID_VIEW_DETECT_REPORT:
					ChatServiceController.doHostAction("viewDetectReport", item.uid, "", item.attachmentId, true);
					break;
				case ChatQuickActionFactory.ID_VIEW_EQUIPMENT:
					ChatServiceController.doHostAction("showEquipment", "", "", item.attachmentId, true);
					break;
				case ChatQuickActionFactory.ID_REPORT_PLAYER_CHAT:
					if (ChatServiceController.oldReportContentTime == 0
							|| System.currentTimeMillis() - ChatServiceController.oldReportContentTime >= ChatServiceController.REPORT_CONTENT_TIME_INTERVAL)
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
						LocalConfig config = ConfigManager.getInstance().getLocalConfig();
						if (config == null || !config.isTranslateDevelopChecked())
						{
							showSendTranslateDevelopMsg(LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRASNALTION),
									LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATION_REPORTED_CHECKBOX_TEXT), item);
						}
						else
						{
							TranslateManager.getInstance().submitTranslateDevelop(item);
							// JniController.getInstance().excuteJNIVoidMethod("translateOptimize",
							// new Object[] { "notunderstand", item.originalLang, item.translatedLang, item.msg, item.translateMsg });
							Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity(),
									LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRANSLATION_SUCCESS), Toast.LENGTH_SHORT);
							toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
							toast.show();
							UserManager.getInstance().addReportContent(item, UserManager.REPORT_TRANSLATION_LIST);
						}

					}
					break;
				case ChatQuickActionFactory.ID_SAY_HELLO:
					sayHello(item);
					break;
				case ChatQuickActionFactory.ID_VIEW_RALLY_INFO:
					ChatServiceController.doHostAction("viewRallyInfo", "", "", item.attachmentId, true);
					break;
				case ChatQuickActionFactory.ID_VIEW_LOTTERY_SHARE:
					ChatServiceController.doHostAction("viewLotteryShare", "", "", item.attachmentId, true);
					break;
				case ChatQuickActionFactory.ID_VIEW_ALLIANCETASK_SHARE:
					ChatServiceController.doHostAction("viewAllianceTaskShare", "", "", "", true);
					break;
				case ChatQuickActionFactory.ID_VIEW_RED_PACKAGE:
					ChatServiceController.doHostAction("viewRedPackage", "", "", item.attachmentId, true);
					break;
				case ChatQuickActionFactory.ID_VIEW_ALLIANCE_TREASURE:
					if (ChatServiceController.isInDragonSencen() || ChatServiceController.isInAncientSencen())
						showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_DISABLE));
					else if (ChatServiceController.currentLevel < 10)
						showContentConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_WARN));
					else
						ChatServiceController.doHostAction("viewAllianceTreasure", "", "", item.getAllianceTreasureInfo(2), true);
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
					ChatServiceController.doHostAction("showAllianceHelp", "", "", item.attachmentId, false);
					break;
				case ChatQuickActionFactory.ID_VIEW_BUY_MSG_BG:
					ChatServiceController.doHostAction("buyChatBg", "", "", "", false);
					LogUtil.trackAction("buy_chat_bg");
					break;
				case ChatQuickActionFactory.ID_VIEW_FIRST_KILL:
					if (StringUtils.isNotEmpty(item.attachmentId))
					{
						try
						{
							JSONObject extraJson = JSON.parseObject(item.attachmentId);
							if (extraJson != null && extraJson.containsKey("dialog"))
							{
								String dialog = extraJson.getString("dialog");
								if (StringUtils.isNotEmpty(dialog))
								{
									if (dialog.equals("170169") || dialog.equals("170170") || dialog.equals("170171"))
									{
										ChatServiceController.doHostAction("showKnightFarm", "", "", "", false);
									}
									else
									{
										if (extraJson.containsKey("msgarr"))
										{
											JSONArray dialogMsgArr = extraJson.getJSONArray("msgarr");
											if (dialogMsgArr != null && (dialogMsgArr.size() == 2 || dialogMsgArr.size() == 3))
												ChatServiceController.doHostAction("showMonsterFirstKill", "", "", dialogMsgArr.getString(1), false);
										}
									}
								}
							}
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					break;
				case ChatQuickActionFactory.ID_VIEW_VIP_LOTTERY:
					String actionArg = "";
					if (StringUtils.isNotEmpty(item.actionArgs))
					{
						try
						{
							JSONArray jsonArr = JSON.parseArray(item.actionArgs);
							if (jsonArr != null && jsonArr.size() > 0)
							{
								actionArg = jsonArr.getString(0);
							}
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
					}
					ChatServiceController.doHostAction("viewVipGiftBox", "", "", actionArg, false);
					break;

				default:
					break;
			}
		}
		else
		{
			if (item.isNeedParseDialog())
			{
				ChatServiceController.doHostAction("showGamePopupView", "", "" + item.post, item.actionArgs, true);
			}
		}
	}

	private static void sayHello(MsgItem msgItem)
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
		ChatServiceController.hostActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (ChatServiceController.getChatFragment() != null)
					{
						ChatServiceController.getChatFragment().setEditText(text);
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

	private static void showBanConfirm(String content, final MsgItem item)
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
		String[] banTimeArr = ChatServiceController.banTime.split("\\|");
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
							new Object[] { item.uid, Integer.valueOf(selectIndex), item.msg });
					UserManager.getInstance().addRestrictUser(item.uid, UserManager.BAN_NOTICE_LIST);
				}
				else
				{
					JniController.getInstance().excuteJNIVoidMethod("banPlayerByIndex",
							new Object[] { item.uid, Integer.valueOf(selectIndex), item.msg });
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

	private static void showConfirm(String content, final MsgItem item, final int type)
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
					Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity(),
							LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_HEADIMG_SUCCESS), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
					toast.show();
					UserManager.getInstance().addRestrictUser(item.uid, type);
				}
				else if (type == UserManager.REPORT_CONTETN_LIST)
				{
					ChatServiceController.oldReportContentTime = System.currentTimeMillis();
					JniController.getInstance().excuteJNIVoidMethod("reportPlayerChatContent", new Object[] { item.uid, item.msg });
					Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity(),
							LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_CONTENT_SUCCESS), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
					toast.show();
					UserManager.getInstance().addReportContent(item, type);
				}
				else if (type == UserManager.REPORT_TRANSLATION_LIST)
				{

					JniController.getInstance().excuteJNIVoidMethod("translateOptimize",
							new Object[] { "notunderstand", item.originalLang, item.translatedLang, item.msg, item.translateMsg });
					Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity(),
							LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRANSLATION_SUCCESS), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
					toast.show();
					UserManager.getInstance().addReportContent(item, type);
				}

			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showAllianceShareDeleteComfirm(String content, final AllianceShareInfo info)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		setDialogView(dlg, content, new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				JniController.getInstance().excuteJNIVoidMethod(
						"deleteAllianceShare",
						new Object[] {
								info.getId(),
								UserManager.getInstance().getCurrentUser().allianceId,
								"",
								"",
								Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_MSG),
								info.getSender()});
			}
		}, 0, true);
	}

	public static void showAllianceShareCommentDeleteComfirm(String content, final AllianceShareComment comment, final String allianceShareSender)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		setDialogView(dlg, content, new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				JniController.getInstance().excuteJNIVoidMethod(
						"deleteAllianceShare",
						new Object[] {
								comment.getId(),
								UserManager.getInstance().getCurrentUser().allianceId,
								comment.getFid(),
								allianceShareSender,
								Integer.valueOf(AllianceShareManager.ALLIANCE_SHARE_COMMENT),
								allianceShareSender});
			}
		}, 0, true);
	}

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

	public static void showReSendConfirm(String content, final MsgItem msgItem)
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
							showResendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn), msgItem);
						}
						else if (price > 0)
						{
							showResendHornWithCornConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn), msgItem,
									price);
						}
					}
					else
					{
						Toast.makeText(ChatServiceController.getCurrentActivity(), "you have been baned!", Toast.LENGTH_SHORT).show();
					}
				}
				else if (msgItem.isAudioMessage())
				{
					ChatServiceController.getInstance().resendAudioMsg(msgItem);
				}
				else
				{
					ChatServiceController.getInstance().resendMsg(msgItem, false, false);
				}

			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showResendHornMessageConfirm(String content, final MsgItem msgItem)
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
				ChatServiceController.resendMsg(msgItem, true, false);
			}
		};
		setDialogView(dlg, content, okOnlickListener, price, true);
	}

	public static void showResendHornWithCornConfirm(String content, final MsgItem msgItem, final int price)
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
					ChatServiceController.resendMsg(msgItem, true, true);
				}
				else
				{
					showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
				}
			}
		};

		setDialogViewWithCoin(dlg, content, okOnlickListener, price, true);
	}

	public static void showQuitRealtimeVoiceRoomConfirm(final MyActionBarActivity activity, String content)
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
				ServiceInterface.unbindRealtimeVoice();
				// ServiceInterface.exitRealtimeVoice();
				activity.exitActivity();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showSendHornMessageConfirm(String content, final String message)
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
				ChatServiceController.sendMsg(message, true, false, null);
			}
		};
		setDialogView(dlg, content, okOnlickListener, price, true);
	}

	public static void showSendHornWithCornConfirm(String content, final String message, final int price)
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
					ChatServiceController.sendMsg(message, true, true, null);
				}
				else
				{
					showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
				}
			}
		};
		setDialogViewWithCoin(dlg, content, okOnlickListener, price, true);
	}

	public static void showSendTranslateDevelopMsg(String content, String checkboxText, final MsgItem item)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		setDialogViewWithCheckbox(dlg, content, checkboxText, item);
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

	public static void showAutoTranslateSettingConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ChatServiceController.doHostAction("showLanguageSetting", "", "", "", false);
				dlg.cancel();
			}
		};

		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);

			adjustConfirmDialog(alertTextView, ok, cancel);

			alertTextView.setText(content);

			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TEXT_GOTO_SETTING));

			ok.setOnClickListener(okOnlickListener);

			cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TEXT_KNOW));
			cancel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dlg.cancel();
				}
			});
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void showExitAllianceShareConfirm(String content, final MyActionBarActivity activity)
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
				activity.exitActivity();
			}
		};

		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);

			adjustConfirmDialog(alertTextView, ok, cancel);

			alertTextView.setText(content);

			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TEXT_ABORT));

			ok.setOnClickListener(okOnlickListener);

			cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TEXT_KEEPON));
			cancel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dlg.cancel();
				}
			});
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

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
				ChatServiceController.doHostAction("changeNickName", "", "", "", false);
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
				int isNotDefaultName = 1;
				if (StringUtils.isEmpty(roomName))
				{
					String memberNameStr = UserManager.getInstance().createNameStr(memberUidAdd);
					if (!memberNameStr.contains(UserManager.getInstance().getCurrentUser().userName))
						roomName = UserManager.getInstance().getCurrentUser().userName + "," + memberNameStr;
					else
						roomName = memberNameStr;
					isNotDefaultName = 0;
				}
				if(SwitchUtils.customWebsocketEnable){
					WebSocketManager.getInstance().roomGroupCreate(roomName,UserManager.getInstance().getUidStr(memberUidAdd));
				}else {
					JniController.getInstance().excuteJNIVoidMethod(
							"createChatRoom",
							new Object[] {
									UserManager.getInstance().createNameStr(memberUidAdd),
									UserManager.getInstance().createUidStr(memberUidAdd),
									roomName,
									"",
									Integer.valueOf(isNotDefaultName) });
				}
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

				if (ChatServiceController.isCreateChatRoom)
				{
					if (ChatServiceController.isNewMailListEnable)
					{
						ChatServiceController.isCreateChatRoom = true;
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
						if(SwitchUtils.customWebsocketEnable){
							WebSocketManager.getInstance().roomGroupInvite(UserManager.getInstance().getCurrentMail().opponentUid,UserManager.getInstance().getUidStr(memberUidAdd));
						}else {
							JniController.getInstance().excuteJNIVoidMethod(
									"inviteChatRoomMember",
									new Object[] {
											UserManager.getInstance().getCurrentMail().opponentUid,
											UserManager.getInstance().createNameStr(memberUidAdd),
											UserManager.getInstance().createUidStr(memberUidAdd) });
						}
					}
					if (memberUidRemoved != null && memberUidRemoved.size() > 0)
					{
						if(SwitchUtils.customWebsocketEnable){
							WebSocketManager.getInstance().roomGroupKick(UserManager.getInstance().getCurrentMail().opponentUid,UserManager.getInstance().getUidStr(memberUidRemoved));
						}else {
							JniController.getInstance().excuteJNIVoidMethod(
									"kickChatRoomMember",
									new Object[] {
											UserManager.getInstance().getCurrentMail().opponentUid,
											UserManager.getInstance().createNameStr(memberUidRemoved),
											UserManager.getInstance().createUidStr(memberUidRemoved) });
						}
					}
					if (roomName != null && !roomName.equals(""))
					{
						if(SwitchUtils.customWebsocketEnable){
							WebSocketManager.getInstance().roomGroupModifyName(UserManager.getInstance().getCurrentMail().opponentUid,roomName);
						}else {
							JniController.getInstance().excuteJNIVoidMethod("modifyChatRoomName",
									new Object[] { UserManager.getInstance().getCurrentMail().opponentUid, roomName });
						}
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

				if (ChatServiceController.isCreateChatRoom)
				{
					if (ChatServiceController.isNewMailListEnable)
					{
						ChatServiceController.isCreateChatRoom = true;
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
					if(SwitchUtils.customWebsocketEnable){
						WebSocketManager.getInstance().roomGroupInvite(UserManager.getInstance().getCurrentMail().opponentUid,UserManager.getInstance().getUidStr(memberUidAdd));
					}else {
						JniController.getInstance().excuteJNIVoidMethod(
								"inviteChatRoomMember",
								new Object[] {
										UserManager.getInstance().getCurrentMail().opponentUid,
										UserManager.getInstance().createNameStr(memberUidAdd),
										UserManager.getInstance().createUidStr(memberUidAdd) });
					}
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
				if (SwitchUtils.customWebsocketEnable){
					WebSocketManager.getInstance().roomGroupQuit(UserManager.getInstance().getCurrentMail().opponentUid);
				}else {
					JniController.getInstance().excuteJNIVoidMethod("quitChatRoom",
							new Object[] { UserManager.getInstance().getCurrentMail().opponentUid });
				}
				activity.exitActivity();
			}
		};
		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	private static AlertDialog createAlertDialog()
	{
		if (ChatServiceController.getCurrentActivity() == null)
			return null;
		return createAlertDialog(ChatServiceController.getCurrentActivity());
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
		return setDialogView(dlg, content, corn, onOKClickListener, "", showCancelBtn, null, "", false, false, false);
	}

	private static Button setDialogView(final AlertDialog dlg, String content, int corn, OnClickListener onOKClickListener, String okBtnText,
			boolean showCancelBtn, final OnClickListener onCancelClickListener, String cancelBtnText, boolean takeDismissAsOK,
			boolean disableBackButton, boolean disableDismiss)
	{
		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);

			if (!disableDismiss)
			{
				if (!takeDismissAsOK)
				{
					setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);
				}
				else
				{
					setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg, onOKClickListener);
				}
			}

			if (disableBackButton)
				dlg.setCancelable(false);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
			cancel.setVisibility(showCancelBtn ? View.VISIBLE : View.GONE);

			adjustConfirmDialog(alertTextView, ok, cancel);

			alertTextView.setText(content);

			ok.setText(StringUtils.isEmpty(okBtnText) ? LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM) : okBtnText);

			ok.setOnClickListener(onOKClickListener);

			cancel.setText(StringUtils.isEmpty(cancelBtnText) ? LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL) : cancelBtnText);
			cancel.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (onCancelClickListener != null)
					{
						onCancelClickListener.onClick(v);
					}
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

	private static void setDialogViewWithCheckbox(final AlertDialog dlg, String content, String checkboxText,
			final MsgItem item)
	{
		Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog_with_checkbox);
		setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);

		TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
		LinearLayout confirm_layout = (LinearLayout) window.findViewById(R.id.confirm_layout);
		final CheckBox can_not_see_check = (CheckBox) window.findViewById(R.id.can_not_see_check);
		Button confirm_btn = (Button) window.findViewById(R.id.confirm_btn);
		Button cancel_btn = (Button) window.findViewById(R.id.cancel_btn);

		can_not_see_check.setText(checkboxText);

		can_not_see_check.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
					LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "isChecked", isChecked);
			}
		});

		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(alertTextView, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(confirm_btn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(cancel_btn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(can_not_see_check, ConfigManager.scaleRatio);
		}

		alertTextView.setText(content);

		confirm_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));

		confirm_btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				TranslateManager.getInstance().submitTranslateDevelop(item);
				// JniController.getInstance().excuteJNIVoidMethod("translateOptimize",
				// new Object[] { "notunderstand", item.originalLang, item.translatedLang, item.msg, item.translateMsg });
				Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity(),
						LanguageManager.getLangByKey(LanguageKeys.TIP_REPORT_TRANSLATION_SUCCESS), Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
				toast.show();
				UserManager.getInstance().addReportContent(item, UserManager.REPORT_TRANSLATION_LIST);

				if (can_not_see_check.isChecked())
				{
					LocalConfig config = ConfigManager.getInstance().getLocalConfig();
					if (config == null)
						config = new LocalConfig();
					config.setTranslateDevelopChecked(true);
					ConfigManager.getInstance().setLocalConfig(config);
					ConfigManager.getInstance().saveLocalConfig();
				}
			}
		});

		cancel_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		cancel_btn.setOnClickListener(new View.OnClickListener()
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
		// final AlertDialog dlg = createAlertDialog();
		// if (dlg == null)
		// return;
		// OnClickListener okOnlickListener = new View.OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// dlg.cancel();
		// }
		// };
		//
		// try
		// {
		// Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);
		//
		// setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);
		//
		// TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
		// Button ok = (Button) window.findViewById(R.id.exitBtn0);
		// Button cancel = (Button) window.findViewById(R.id.exitBtn1);
		// cancel.setVisibility(View.GONE);
		// adjustConfirmDialog(alertTextView, ok, cancel);
		// alertTextView.setText(content);
		// ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
		// ok.setOnClickListener(okOnlickListener);
		// }
		// catch (Exception e)
		// {
		// LogUtil.printException(e);
		// }

		if (ChatServiceController.getCurrentActivity() != null)
		{
			LayoutInflater inflater = ChatServiceController.getCurrentActivity().getLayoutInflater();
			if (inflater != null)
			{
				View view = inflater.inflate(R.layout.content_toast, null);
				TextView textView = (TextView) view.findViewById(R.id.alertTextView);
				if (textView != null)
				{
					textView.setText(content);
					Toast toast = new Toast(ChatServiceController.getCurrentActivity());
					toast.setGravity(Gravity.CENTER, 0, 200);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setView(view);
					toast.show();
				}
			}
		}
	}

	public static void showOldContentConfirm(String content)
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

		try
		{
			Window window = initAlertDialog(dlg, R.layout.cs__confirm_dialog);

			setDismissListener((FrameLayout) window.findViewById(R.id.confirmFrameLayout), dlg);

			TextView alertTextView = (TextView) window.findViewById(R.id.alertTextView);
			Button ok = (Button) window.findViewById(R.id.exitBtn0);
			Button cancel = (Button) window.findViewById(R.id.exitBtn1);
			cancel.setVisibility(View.GONE);
			adjustConfirmDialog(alertTextView, ok, cancel);
			alertTextView.setText(content);
			ok.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
			ok.setOnClickListener(okOnlickListener);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static void showDeleteChannelConfirm(String content, final ChatChannel channel)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(ChatServiceController.getChannelListFragment()!=null)
				{
					ChatServiceController.getChannelListFragment().actualDeleteSingleChannel(channel);
				}
				dlg.cancel();
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}
	
	public static void showDeleteRecyclerChannelConfirm(String content, final ChatChannel channel,final int position)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(ChatServiceController.getBaseListActivity()!=null)
				{
					ChatServiceController.getBaseListActivity().actualDeleteSingleChannel(channel,position);
				}
				dlg.cancel();
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showClearLocationConfirm(String content)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LogUtil.trackNearby("nearby_cleanLocation");
				if (!SwitchUtils.mqttEnable)
					WebSocketManager.getInstance().clearLocation();
				else
					MqttManager.getInstance().clearLocation();
				dlg.cancel();
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	public static void showOperateMutiMail(String content, final List<ChannelListItem> list, final int type)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(ChatServiceController.getChannelListFragment()!=null)
					ChatServiceController.getChannelListFragment().comfirmOperateMutiMail(list, type);
				dlg.cancel();
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}
	
	public static void showRecyclerOperateMutiMail(String content, final List<ChannelListItem> list, final int type)
	{
		final AlertDialog dlg = createAlertDialog();
		if (dlg == null)
			return;
		OnClickListener okOnlickListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(ChatServiceController.getBaseListActivity()!=null)
					ChatServiceController.getBaseListActivity().comfirmOperateMutiMail(list, type);
				dlg.cancel();
			}
		};

		setDialogView(dlg, content, okOnlickListener, 0, true);
	}

	/**
	 * 有三种对话框
	 * 权限说明：含一个确定按钮，点击此按钮则打开系统的授权界面
	 * 权限解释：含两个按钮现在授权(点击则打开系统的授权界面)，下次再说(点击则取消，可能需要通过设置onCancelClickListener，来继续进行中断的逻辑)
	 * 如何手动开启权限提示：含一个确定按钮，点击后关闭面板
	 */
	public static void showPermissionDialog(final Activity activity, final String content, final String permissionKey,
			final boolean promtPermission, final OnClickListener onCancelClickListener)
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
							if (promtPermission)
							{
								PermissionManager.getInstance().onInfoDialogConfirm(permissionKey);
							}
							dlg.cancel();
						}
					};

					if (onCancelClickListener == null)
					{
						setDialogView(dlg, content, 0, okOnlickListener, "", false, null, "", true, true, true);
					}
					else
					{
						final String okBtnText = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_BTN_REQUEST_NOW);
						final String cancelBtnText = LanguageManager.getLangByKey(LanguageKeys.PERMISSION_BTN_REQUEST_NEXT_TIME);
						setDialogView(dlg, content, 0, okOnlickListener, okBtnText, true, onCancelClickListener, cancelBtnText, true,
								true, true);
					}

				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}
}
