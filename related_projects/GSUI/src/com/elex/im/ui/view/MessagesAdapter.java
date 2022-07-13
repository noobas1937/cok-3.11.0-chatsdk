package com.elex.im.ui.view;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.londatiga.android.QuickAction;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.elex.im.CokChannelDef;
import com.elex.im.CokConfig;
import com.elex.im.core.IMCore;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.util.AllianceTaskInfo;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.core.util.StickManager;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.core.util.TranslateListener;
import com.elex.im.core.util.TranslateManager;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.controller.MenuController;
import com.elex.im.ui.model.AllianceOfficerAttachment;
import com.elex.im.ui.model.AllianceSkillInfo;
import com.elex.im.ui.net.XiaoMiToolManager;
import com.elex.im.ui.util.CompatibleApiUtil;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.util.MsgUtil;
import com.elex.im.ui.util.gif.GifMovieView;
import com.elex.im.ui.view.actionbar.MyActionBarActivity;
import com.elex.im.ui.viewholder.MessageViewHolder;
import com.elex.im.ui.viewholder.ViewHolderHelper;
import com.mi.mimsgsdk.AudioRecordListener;

public final class MessagesAdapter extends BaseAdapter implements AudioRecordListener
{
	private Context				c;
	private List<Msg>		items;
	private LayoutInflater		inflater;
	private QuickAction			quickAction;
	private SparseArray<View>	viewArray						= new SparseArray<View>();

	// private static final int ITEM_MESSAGE_SEND = 0;
	// private static final int ITEM_MESSAGE_RECEIVE = 1;
	private static final int	ITEM_GIF_SEND					= 0;
	private static final int	ITEM_GIF_RECEIVE				= 1;
	private static final int	ITEM_PIC_SEND					= 2;
	private static final int	ITEM_PIC_RECEIVE				= 3;
	private static final int	ITEM_REDPACKAGE_SEND			= 4;
	private static final int	ITEM_REDPACKAGE_RECEIVE			= 5;
	private static final int	ITEM_CHATROOM_TIP				= 6;
	private static final int	ITEM_NEWMESSAGE_TIP				= 7;
	private static final int	ITEM_MESSAGE_NORMAL_SEND		= 8;
	private static final int	ITEM_MESSAGE_HORN_SEND			= 9;
	private static final int	ITEM_MESSAGE_SYS_SEND			= 10;
	private static final int	ITEM_MESSAGE_KING_SEND			= 11;
	private static final int	ITEM_MESSAGE_NEWYEAR_SEND		= 12;
	private static final int	ITEM_MESSAGE_SVIP_SEND			= 13;
	private static final int	ITEM_MESSAGE_NORMAL_RECEIVE		= 14;
	private static final int	ITEM_MESSAGE_HORN_RECEIVE		= 15;
	private static final int	ITEM_MESSAGE_SYS_RECEIVE		= 16;
	private static final int	ITEM_MESSAGE_KING_RECEIVE		= 17;
	private static final int	ITEM_MESSAGE_NEWYEAR_RECEIVE	= 18;
	private static final int	ITEM_MESSAGE_SVIP_RECEIVE		= 19;
	private static final int	ITEM_AUDIO_NORMAL_SEND			= 20;
	private static final int	ITEM_AUDIO_HORN_SEND			= 21;
	private static final int	ITEM_AUDIO_SYS_SEND				= 22;
	private static final int	ITEM_AUDIO_KING_SEND			= 23;
	private static final int	ITEM_AUDIO_NEWYEAR_SEND			= 24;
	private static final int	ITEM_AUDIO_SVIP_SEND			= 25;
	private static final int	ITEM_AUDIO_NORMAL_RECEIVE		= 26;
	private static final int	ITEM_AUDIO_HORN_RECEIVE			= 27;
	private static final int	ITEM_AUDIO_SYS_RECEIVE			= 28;
	private static final int	ITEM_AUDIO_KING_RECEIVE			= 29;
	private static final int	ITEM_AUDIO_NEWYEAR_RECEIVE		= 30;
	private static final int	ITEM_AUDIO_SVIP_RECEIVE			= 31;
	private static final int	ITEM_MESSAGE_BATTLE_SEND		= 32;
	private static final int	ITEM_MESSAGE_BATTLE_RECEIVE		= 33;
	private static final int	ITEM_TYPE_TOTAL_COUNT			= 34;

	private static final int	HEAD_PIC_SIZE					= 60;
	
	public MessagesAdapter(MyActionBarActivity activity, List<Msg> objects)
	{
		this.c = activity;
		this.items = objects;
		this.inflater = ((LayoutInflater) this.c.getSystemService("layout_inflater"));

		// XiaoMiToolManager.getInstance().addAudioListener(this);
	}

	private QuickAction.OnActionItemClickListener	actionClickListener	= new QuickAction.OnActionItemClickListener()
																		{

																			@Override
																			public void onItemClick(QuickAction source, int pos,
																					int actionId)
																			{
																				handleItemClick(source, pos, actionId);
																			}
																		};

	private void handleItemClick(final QuickAction source, final int pos, final int actionId)
	{
		final MessagesAdapter adapter = this;
		try
		{
			MenuController.handleItemClick(adapter, source, pos, actionId);
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public static Msg getMsgItemFromQuickAction(QuickAction quickAction)
	{
		// return ((MessageHolder)
		// quickAction.currentTextView.getTag()).msgItem;
		if (quickAction.currentTextView != null && quickAction.currentTextView.getTag() != null)
			return (Msg) quickAction.currentTextView.getTag();
		return null;
	}

	public void showTranslatedLanguage(final TextView textView, final Msg item)
	{
		if (TranslateManager.getInstance().isTranslateMsgValid(item))
		{
			item.hasTranslated = true;
			item.isTranslatedByForce = true;
			item.hasTranslatedByForce = true;
			item.isOriginalLangByForce = false;
			setText(textView, item.translateMsg, item, true);
		}
		else
		{
			TranslateManager.getInstance().loadTranslation(item, new TranslateListener()
			{
				@Override
				public void onTranslateFinish(final String translateMsg)
				{
					if (!item.canShowTranslateMsg() || StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{"))
						return;
					item.isOriginalLangByForce = false;
					setTextOnUIThread(textView, translateMsg, item);
				}
			});
		}
	}

	private void setTextOnUIThread(final TextView textView, final String translateMsg, final Msg item)
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
						setText(textView, translateMsg, item, true);
					}
					catch (Exception e)
					{
						LogUtil.printException(e);
					}
				}
			});
		}
	}

	public void showOriginalLanguage(TextView textView, Msg item)
	{
		item.hasTranslated = false;
		item.isTranslatedByForce = false;
		item.isOriginalLangByForce = true;
		setText(textView, item.msg, item, false);
	}

	// TODO 返回值是否正确？
	public boolean isEnabled(int position)
	{
		return false;
	}

	@Override
	public int getViewTypeCount()
	{
		return ITEM_TYPE_TOTAL_COUNT;
	}

	@Override
	public int getItemViewType(int position)
	{
		if (position >= 0 && position < items.size())
		{
			Msg item = items.get(position);
			if (item != null)
			{
				int type = item.getMsgItemType(c);
				boolean isSelfMsg = item.isSelfMsg();
				boolean isRightMessage = ConfigManager.getInstance().needRTL()?!isSelfMsg:isSelfMsg;
				
				if (type == Msg.MSGITEM_TYPE_MESSAGE || type == Msg.MSGITEM_TYPE_AUDIO)
				{
					return getMessageItemType(item, type);
				}
				else if (type == Msg.MSGITEM_TYPE_GIF)
				{
					return isRightMessage ? ITEM_GIF_SEND : ITEM_GIF_RECEIVE;
				}
				else if (type == Msg.MSGITEM_TYPE_PIC)
				{
					return isRightMessage ? ITEM_PIC_SEND : ITEM_PIC_RECEIVE;
				}
				else if (type == Msg.MSGITEM_TYPE_REDPACKAGE)
				{
					return isRightMessage ? ITEM_REDPACKAGE_SEND : ITEM_REDPACKAGE_RECEIVE;
				}
				else if (type == Msg.MSGITEM_TYPE_CHATROM_TIP)
				{
					return ITEM_CHATROOM_TIP;
				}
				else if (type == Msg.MSGITEM_TYPE_NEW_MESSAGE_TIP)
				{
					return ITEM_NEWMESSAGE_TIP;
				}
			}
		}
		return -1;
	}

	private boolean needShowTime(int position)
	{
		int itemType = getItemViewType(position);
		if (itemType == ITEM_NEWMESSAGE_TIP)
			return false;
		return true;
	}

	private int getMessageItemType(Msg msgItem, int msgType)
	{
		boolean isSelfMsg = msgItem.isSelfMsg();
		boolean isLeftMessage = ConfigManager.getInstance().needRTL()?isSelfMsg:!isSelfMsg;
		if (msgItem.isSystemMessage())
		{
			if (isLeftMessage)
			{
				if (msgItem.isHornMessage() || msgItem.isStealFailedMessage())
				{
					if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
						return ITEM_MESSAGE_HORN_RECEIVE;
					else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
						return ITEM_AUDIO_HORN_RECEIVE;
				}
				else
				{
					if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
						return ITEM_MESSAGE_SYS_RECEIVE;
					else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
						return ITEM_AUDIO_SYS_RECEIVE;
				}
			}
			else
			{
				if (msgItem.isHornMessage() || msgItem.isStealFailedMessage())
				{
					if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
						return ITEM_MESSAGE_HORN_SEND;
					else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
						return ITEM_AUDIO_HORN_SEND;
				}
				else
				{
					if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
						return ITEM_MESSAGE_SYS_SEND;
					else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
						return ITEM_AUDIO_SYS_SEND;
				}
			}
		}
		else
		{
			if (isLeftMessage)
			{
				if(msgItem.isInCrossFightServer() && CokConfig.isInBattleField())
				{
					return ITEM_MESSAGE_BATTLE_RECEIVE;
				}
				else
				{
					if (isKingMsg(msgItem))
					{
						if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
							return ITEM_MESSAGE_KING_RECEIVE;
						else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
							return ITEM_AUDIO_KING_RECEIVE;
					}
					else
					{
						if (GSController.isNewYearStyleMsg && msgItem.isSVIPMsg())
						{
							if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
								return ITEM_MESSAGE_NEWYEAR_RECEIVE;
							else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
								return ITEM_AUDIO_NEWYEAR_RECEIVE;
						}
						else
						{
							if (msgItem.isSVIPMsg())
							{
								if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
									return ITEM_MESSAGE_SVIP_RECEIVE;
								else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
									return ITEM_AUDIO_SVIP_RECEIVE;
							}
							else
							{
								if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
									return ITEM_MESSAGE_NORMAL_RECEIVE;
								else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
									return ITEM_AUDIO_NORMAL_RECEIVE;
							}
						}
					}
				}
			}
			else
			{
				if(msgItem.isInCrossFightServer() && CokConfig.isInBattleField())
				{
					return ITEM_MESSAGE_BATTLE_SEND;
				}
				else
				{
					if (isKingMsg(msgItem))
					{
						if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
							return ITEM_MESSAGE_KING_SEND;
						else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
							return ITEM_AUDIO_KING_SEND;
					}
					else
					{
						if (GSController.isNewYearStyleMsg && msgItem.isSVIPMsg())
						{
							if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
								return ITEM_MESSAGE_NEWYEAR_SEND;
							else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
								return ITEM_AUDIO_NEWYEAR_SEND;
						}
						else
						{
							if (msgItem.isSVIPMsg())
							{
								if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
									return ITEM_MESSAGE_SVIP_SEND;
								else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
									return ITEM_AUDIO_SVIP_SEND;
							}
							else
							{
								if (msgType == Msg.MSGITEM_TYPE_MESSAGE)
									return ITEM_MESSAGE_NORMAL_SEND;
								else if (msgType == Msg.MSGITEM_TYPE_AUDIO)
									return ITEM_AUDIO_NORMAL_SEND;
							}
						}
					}
				}
			}
		}
		return -1;
	}

	public boolean isKingMsg(Msg msg)
	{
		if (StringUtils.isNotEmpty(msg.uid) && StringUtils.isNotEmpty(GSController.kingUid)
				&& GSController.kingUid.equals(msg.uid))
			return true;
		return false;
	}

	public void refreshAudioDownloadState(View convertView, Msg item)
	{
		if (!item.isAudioMessage())
			return;
		TextView downloadtip = ViewHolderHelper.get(convertView, R.id.downloadtip);
		if (downloadtip != null)
		{
			downloadtip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_LOADING) + "...");
			if (item.isAudioDownloading)
				downloadtip.setVisibility(View.VISIBLE);
			else
				downloadtip.setVisibility(View.GONE);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (position < 0 || position >= items.size())
			return null;

		final Msg item = (Msg) this.items.get(position);
		if (item == null)
		{
			return null;
		}
//		System.out.println();
//		System.out.println("getView post:" + position + " convertView:" + (convertView == null) + "  msg:" + item.msg);
		int type = item.getMsgItemType(c);

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (convertView == null)
		{
			int itemType = getItemViewType(position);
			convertView = createViewByMessage(item, position, type);
			setMessageTextBackground(convertView, itemType, sdk);
			adjustSize(convertView, type);
		}

		if (type == Msg.MSGITEM_TYPE_NEW_MESSAGE_TIP)
		{
			Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
			if (channel != null && channel.wsNewMsgCount > 0)
			{
				channel.wsNewMsgCount = 0;
				if (getChatFragment() != null)
				{
					getChatFragment().refreshToolTip();
				}
			}
			setNewMsgTipData(convertView, item);
		}
		else
		{
			TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
			if (messageText != null)
				messageText.setTag(item);

			if (needShowTime(position))
				setSendTimeData(convertView, item);

			if (type == Msg.MSGITEM_TYPE_CHATROM_TIP)
			{
				setChatRoomTipData(convertView, item);
			}
			else
			{
				adjustHeadImageContainerSize(convertView, item);
				setPlayerData(convertView, item);
				addHeadImageOnClickAndTouchListener(convertView, item);
				addSendStatusTimerAndRefresh(convertView, item);
				refreshVoiceReadState(convertView, item);
				addOnClickSendStateListener(convertView, item);

				if (type == Msg.MSGITEM_TYPE_MESSAGE)
				{
					setMessageData(convertView, item);
				}
				else if (type == Msg.MSGITEM_TYPE_REDPACKAGE)
				{
					setRedPackageData(convertView, item);
				}
				else if (type == Msg.MSGITEM_TYPE_GIF)
				{
					setGifData(convertView, item);
				}
				else if (type == Msg.MSGITEM_TYPE_PIC)
				{
					setPicData(convertView, item);
				}
				else if (type == Msg.MSGITEM_TYPE_AUDIO)
				{
					refreshAudioDownloadState(convertView, item);
					setAudioText(convertView, item);
				}
			}

			MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
			if (holder != null)
				holder.currentMsgItem = item;

		}
		viewArray.put(position, convertView);

		return convertView;
	}

	// @SuppressWarnings("deprecation")
	// @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	// private void setAudioTextBackground(View convertView, Msg msgItem,
	// int sdk)
	// {
	// TextView messageText = ViewHolderHelper.get(convertView,
	// R.id.messageText);
	// if (messageText == null)
	// return;
	// String background = "chatfrom_bg";
	// messageText.setTextColor(0xff4f4f4f);
	//
	// if (msgItem.isSelfMsg())
	// {
	// if (isKingMsg(msgItem))
	// background = "king_msg_right_bg";
	// else
	// {
	// if (GSController.isNewYearStyleMsg && msgItem.isSVIPMsg())
	// {
	// messageText.setTextColor(0xff443b39);
	// background = "chat_newyear_right_bg_normal";
	// }
	// else
	// {
	// if (msgItem.isSVIPMsg())
	// {
	// if (msgItem.getSVipLevel() >= 1 && msgItem.getSVipLevel() <= 3)
	// background = "chat_svip1_right_bg";
	// else if (msgItem.getSVipLevel() >= 4 && msgItem.getSVipLevel() <= 6)
	// background = "chat_svip2_right_bg";
	// else if (msgItem.getSVipLevel() >= 7 && msgItem.getSVipLevel() <= 9)
	// background = "chat_svip3_right_bg";
	// else if (msgItem.getSVipLevel() >= 10 && msgItem.getSVipLevel() <= 12)
	// background = "chat_svip4_right_bg";
	// else if (msgItem.getSVipLevel() > 12)
	// background = "chat_svip5_right_bg";
	// }
	// else
	// {
	// background = "chatfrom_bg";
	// }
	// }
	// }
	// }
	// else
	// {
	// if (isKingMsg(msgItem))
	// background = "king_msg_left_bg";
	// else
	// {
	// if (GSController.isNewYearStyleMsg && msgItem.isSVIPMsg())
	// {
	// messageText.setTextColor(0xff443b39);
	// background = "chat_newyear_left_bg_normal";
	// }
	// else
	// {
	// if (msgItem.isSVIPMsg())
	// {
	//
	// if (msgItem.getSVipLevel() >= 1 && msgItem.getSVipLevel() <= 3)
	// background = "chat_svip1_left_bg";
	// else if (msgItem.getSVipLevel() >= 4 && msgItem.getSVipLevel() <= 6)
	// background = "chat_svip2_left_bg";
	// else if (msgItem.getSVipLevel() >= 7 && msgItem.getSVipLevel() <= 9)
	// background = "chat_svip3_left_bg";
	// else if (msgItem.getSVipLevel() >= 10 && msgItem.getSVipLevel() <= 12)
	// background = "chat_svip4_left_bg";
	// else if (msgItem.getSVipLevel() > 12)
	// background = "chat_svip5_left_bg";
	// }
	// else
	// {
	// background = "chatto_bg";
	// }
	// }
	// }
	// }
	//
	// Drawable drawable = ImageUtil.getDrawableByResName(c, background);
	// Drawable backDrawable = messageText.getBackground();
	// backDrawable = null;
	// if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
	// {
	// messageText.setBackgroundDrawable(drawable);
	// }
	// else
	// {
	// messageText.setBackground(null);
	// messageText.setBackground(drawable);
	// }
	// }

	private void setAudioText(final View convertView, final Msg item)
	{
		TextView timeText = ViewHolderHelper.get(convertView, R.id.audioTimeText);
		if (timeText != null)
		{
			if (StringUtils.isNumeric(item.msg))
				timeText.setText(TimeManager.getAudioLength(Double.parseDouble(item.msg)));
			else
				timeText.setText("0");
		}

		TextView textView = ViewHolderHelper.get(convertView, R.id.messageText);
		if (textView == null)
			return;

		View emptySpaceView = ViewHolderHelper.get(convertView, R.id.emptySpaceView);
		double audioLength = Double.parseDouble(item.msg);
		double maxLength = 60000;
		audioLength = audioLength <= 1000 ? 0 : audioLength;
		audioLength = audioLength > maxLength ? maxLength : audioLength;
		float textRatio = (float) (audioLength / maxLength);

		LinearLayout.LayoutParams spaceLayoutParams = (LinearLayout.LayoutParams) emptySpaceView.getLayoutParams();
		spaceLayoutParams.weight = 1 - textRatio;
		emptySpaceView.setLayoutParams(spaceLayoutParams);

		LinearLayout.LayoutParams textLayoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
		textLayoutParams.weight = textRatio;
		textView.setLayoutParams(textLayoutParams);
		setAudioTextIcon(item, textView, 0);
		addItemOnClickListener(convertView, item);
		// addOnClickItemListener(convertView, item);
	}

	private void setAudioTextIcon(final Msg item, final TextView textView, int state)
	{
		if (item == null || textView == null)
			return;

		String str = "";
		item.currentText = str;
		// 将html特殊符号进行转义，否则"<"后面的内容会被Html.fromHtml吞掉
		str = TextUtils.htmlEncode(str);
		// 转化坐标为链接
		String htmlLinkText = str;
		htmlLinkText = insertCoordinateLink(convertLineBreak(str));

		Spanned spannedText = Html.fromHtml(htmlLinkText);
		textView.setText(spannedText);

		textView.setMovementMethod(LinkMovementMethod.getInstance());

		CharSequence text = textView.getText();
		if (text instanceof Spannable)
		{
			CharSequence text2 = textView.getText();

			SpannableStringBuilder style = new SpannableStringBuilder(text2);
			style.clearSpans();
			
			boolean isSelfMsg = item.isSelfMsg();
			boolean isRightMessage = ConfigManager.getInstance().needRTL()?!isSelfMsg:isSelfMsg;

			final int icon;
			if (state == 1)
			{
				icon = isRightMessage ? R.drawable.voice_play_right1 : R.drawable.voice_play_left1;
			}
			else if (state == 2)
			{
				icon = isRightMessage ? R.drawable.voice_play_right2 : R.drawable.voice_play_left2;
			}
			else
			{
				icon = isRightMessage ? R.drawable.voice_play_right3 : R.drawable.voice_play_left3;
			}

			ImageGetter imageGetter = new ImageGetter()
			{
				@Override
				public Drawable getDrawable(String source)
				{
					if (c == null)
						return null;

					Drawable d = c.getResources().getDrawable(icon);
					if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
					{
						d.setBounds(0, -10, (int) (d.getIntrinsicWidth() * 0.5f * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
								(int) (d.getIntrinsicHeight() * 0.5f * ConfigManager.scaleRatio * getScreenCorrectionFactor()) - 10);
					}
					else
					{
						d.setBounds(0, -10, (int) (d.getIntrinsicWidth() * 0.5f), (int) (d.getIntrinsicHeight() * 0.5f) - 10);
					}
					// ((BitmapDrawable) d).setGravity(Gravity.TOP);
					return d;
				}
			};

			style.insert(0, Html.fromHtml("<img src='" + icon + "'/>", imageGetter, null));

			textView.setText(style);
		}
	}

	private void addItemOnClickListener(final View convertView, final Msg item)
	{
		if (item == null)
			return;
		OnClickListener onClickListener = new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (c == null || ((ChatActivity) c).fragment == null || ((ChatFragment) ((ChatActivity) c).fragment).isKeyBoradShowing)
				{
					return;
				}
				if (ignoreClick)
				{
					ignoreClick = false;
					return;
				}

				if (item.isRedPackageMessage())
				{
					if (item.sendState == Msg.HANDLED || item.isSelfMsg())
						GSController.doHostAction("viewRedPackage", "", "", item.attachmentId, true);
					else
					{
//						if (GSIM.getChatFragment() != null)
//							GSIM.getChatFragment().showRedPackageConfirm(item);

					}
				}
				else if (item.isAudioMessage())
				{
					if (view instanceof TextView)
						onAudioMsgClicked((TextView) view, item);
				}
				else
				{
					if (quickAction != null)
					{
						quickAction.setOnActionItemClickListener(null);
					}

					quickAction = ChatQuickActionFactory.createQuickAction((Activity) c, item);
					quickAction.setOnActionItemClickListener(actionClickListener);

					quickAction.currentTextView = (TextView) view;
					quickAction.show(view);
				}
			}
		};

		// View.OnLongClickListener onLongClickListener = new
		// View.OnLongClickListener()
		// {
		// @Override
		// public boolean onLongClick(View view)
		// {
		// if (c == null || ((ChatActivity) c).fragment == null ||
		// ((ChatFragment) ((ChatActivity) c).fragment).isKeyBoradShowing)
		// {
		// return false;
		// }
		// if (ignoreClick)
		// {
		// ignoreClick = false;
		// return false;
		// }
		//
		// if (item.isAudioMessage() && view instanceof TextView)
		// {
		// onAudioMsgLongClicked((TextView) view, item);
		// return true;
		// }
		// return false;
		// }
		// };

		if (item.isRedPackageMessage())
		{
			LinearLayout red_package_top_layout = ViewHolderHelper.get(convertView, R.id.red_package_top_layout);
			if (red_package_top_layout != null)
				red_package_top_layout.setOnClickListener(onClickListener);

			LinearLayout red_package_bottom_layout = ViewHolderHelper.get(convertView, R.id.red_package_bottom_layout);
			if (red_package_bottom_layout != null)
				red_package_bottom_layout.setOnClickListener(onClickListener);
		}
		else
		{
			TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
			if (messageText != null)
			{
				messageText.setOnClickListener(onClickListener);
				if (item.isAudioMessage())
				{
					// messageText.setOnLongClickListener(onLongClickListener);
				}
			}
		}
	}

	protected void onAudioMsgClicked(TextView view, Msg item)
	{
		LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "view", view, "item", item, "item.media", item.media);
		LogUtil.trackPageView("Audio-click");
		if (!isPlayingAudio)
		{
			XiaoMiToolManager.getInstance().playVoice(item.media);
			playingTextView = view;
			if (playingMsgItem != null)
				playingMsgItem.isAudioDownloading = false;
			playingMsgItem = item;
			if (StringUtils.isNotEmpty(item.media))
				item.isAudioDownloading = true;
			notifyDataSetChanged();
//			GSController.getInstance().refreshVoiceReadState();
			nextTextView = null;
			nextMsgItem = null;
		}
		else
		{
			XiaoMiToolManager.getInstance().stopPlayVoice();
			if (playingMsgItem.equals(item))
				playingMsgItem.readStateBefore = Msg.VOICE_READ;
			if (view != playingTextView)
			{
				nextTextView = view;
				nextMsgItem = item;
			}
		}
	}

	protected void onAudioMsgLongClicked(TextView view, Msg item)
	{
		if (quickAction != null)
		{
			quickAction.setOnActionItemClickListener(null);
		}

		quickAction = ChatQuickActionFactory.createQuickAction((Activity) c, item);
		quickAction.setOnActionItemClickListener(actionClickListener);

		quickAction.currentTextView = (TextView) view;
		quickAction.show(view);
	}

	public void onAudioPlayChannelChanged()
	{
		// TODO 弹提示，修改标题图标
		XiaoMiToolManager.getInstance().refreshSpeakerphoneState();
	}

	private boolean		isPlayingAudio;
	private Timer		playAudioTimer;
	private TimerTask	playAudioTimerTask;
	private int			playAudioAnimCout;
	private TextView	playingTextView;
	private Msg		playingMsgItem;
	private TextView	nextTextView;
	private Msg		nextMsgItem;

	private void startPlayAudioTimer(TextView textView, Msg item)
	{
		stopPlayAudioTimer();
		playAudioTimer = new Timer();
		playAudioTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (c != null)
				{
					playAudioAnimCout++;
					if (playingTextView == null || playingTextView.getTag() == null)
						return;

					Msg currentMsgItem = (Msg) playingTextView.getTag();

					if (currentMsgItem != null && currentMsgItem.equals(playingMsgItem))
					{
						((Activity) c).runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									setAudioTextIcon(playingMsgItem, playingTextView, playAudioAnimCout % 3);
								}
								catch (Exception e)
								{
									LogUtil.printException(e);
								}
							}
						});

					}
				}
			}
		};
		if (playAudioTimer != null)
		{
			playAudioTimer.schedule(playAudioTimerTask, 0, 500);
			playAudioAnimCout = 0;
		}
	}

	private void stopPlayAudioTimer()
	{
		if (playAudioTimer != null)
		{
			playAudioTimer.cancel();
			playAudioTimer.purge();
			playAudioTimer = null;
		}
	}

	private void adjustSize(View convertView, int type)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			adjustTextSize(convertView);

			if (type != Msg.MSGITEM_TYPE_CHATROM_TIP && type != Msg.MSGITEM_TYPE_NEW_MESSAGE_TIP)
			{
				int length = (int) (ScaleUtil.dip2px(c, 50) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
				if (headImage != null)
				{
					FrameLayout.LayoutParams headImageLayoutParams = (FrameLayout.LayoutParams) headImage.getLayoutParams();
					headImageLayoutParams.width = length;
					headImageLayoutParams.height = length;
					headImage.setLayoutParams(headImageLayoutParams);
				}
			}

			if (type == Msg.MSGITEM_TYPE_REDPACKAGE)
			{
				int headImageContainerWidth = (int) (ScaleUtil.dip2px(c, HEAD_PIC_SIZE) * ConfigManager.scaleRatio * getScreenCorrectionFactor());

				LinearLayout redpackage_layout = ViewHolderHelper.get(convertView, R.id.redpackage_layout);
				if (redpackage_layout != null)
				{
					int redPackageWidth = (int) (ScaleUtil.getScreenWidth() * 0.8f) - headImageContainerWidth;
					RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) redpackage_layout.getLayoutParams();
					int targetWidht = redPackageWidth * 3 / 4;
					if (redPackageWidth > 600)
						targetWidht = 600;
					linearParams.width = targetWidht;
					redpackage_layout.setLayoutParams(linearParams);
				}

				ImageView red_package_image = ViewHolderHelper.get(convertView, R.id.red_package_image);
				if (red_package_image != null)
				{
					int originalHeight = 36;
					int targetRedPackageHeight = (int) (ScaleUtil.dip2px(c, originalHeight) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
					LinearLayout.LayoutParams redPackageLayout = (LinearLayout.LayoutParams) red_package_image.getLayoutParams();
					redPackageLayout.width = targetRedPackageHeight;
					redPackageLayout.height = targetRedPackageHeight;
					red_package_image.setLayoutParams(redPackageLayout);
				}
			}
		}
	}

	private void adjustHeadImageContainerSize(View convertView, Msg item)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
			if (headImageContainer != null)
			{
				int width = (int) (ScaleUtil.dip2px(c, HEAD_PIC_SIZE) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				int height = (int) (ScaleUtil.dip2px(c, HEAD_PIC_SIZE) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				LinearLayout.LayoutParams headImageContainerLayoutParams = (LinearLayout.LayoutParams) headImageContainer.getLayoutParams();
				headImageContainerLayoutParams.width = width;
				headImageContainerLayoutParams.height = height;
				headImageContainer.setLayoutParams(headImageContainerLayoutParams);
			}

			ImageView headImageKingIcon = ViewHolderHelper.get(convertView, R.id.headImageKingIcon);
			if (headImageKingIcon != null)
			{
				int width = (int) (ScaleUtil.dip2px(c, 40) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				int height = (int) (ScaleUtil.dip2px(c, 22) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				LinearLayout.LayoutParams headImageKingIconLayoutParams = (LinearLayout.LayoutParams) headImageKingIcon.getLayoutParams();
				headImageKingIconLayoutParams.width = width;
				headImageKingIconLayoutParams.height = height;
				headImageKingIcon.setLayoutParams(headImageKingIconLayoutParams);
			}
		}
	}

	private void adjustTextSize(View convertView)
	{
		TextView downloadtip = ViewHolderHelper.get(convertView, R.id.downloadtip);
		if (downloadtip != null)
			ScaleUtil.adjustTextSize(downloadtip, ConfigManager.scaleRatio);

		TextView newMsgLabel = ViewHolderHelper.get(convertView, R.id.newMsgLabel);
		if (newMsgLabel != null)
			ScaleUtil.adjustTextSize(newMsgLabel, ConfigManager.scaleRatio);

		TextView sendDateLabel = ViewHolderHelper.get(convertView, R.id.sendDateLabel);
		if (sendDateLabel != null)
			ScaleUtil.adjustTextSize(sendDateLabel, ConfigManager.scaleRatio);

		TextView messageText_center = ViewHolderHelper.get(convertView, R.id.messageText_center);
		if (messageText_center != null)
			ScaleUtil.adjustTextSize(messageText_center, ConfigManager.scaleRatio);

		TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
		if (messageText != null)
			ScaleUtil.adjustTextSize(messageText, ConfigManager.scaleRatio);

		TextView vipLabel = ViewHolderHelper.get(convertView, R.id.vipLabel);
		if (vipLabel != null)
			ScaleUtil.adjustTextSize(vipLabel, ConfigManager.scaleRatio);

		TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
		if (allianceLabel != null)
			ScaleUtil.adjustTextSize(allianceLabel, ConfigManager.scaleRatio);

		TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
			ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);

		TextView red_package_title = ViewHolderHelper.get(convertView, R.id.red_package_title);

		if (red_package_title != null)
			ScaleUtil.adjustTextSize(red_package_title, ConfigManager.scaleRatio);

		TextView red_package_content = ViewHolderHelper.get(convertView, R.id.red_package_content);
		if (red_package_content != null)
			ScaleUtil.adjustTextSize(red_package_content, ConfigManager.scaleRatio);

		TextView redpackage_time = ViewHolderHelper.get(convertView, R.id.redpackage_time);
		if (redpackage_time != null)
			ScaleUtil.adjustTextSize(redpackage_time, ConfigManager.scaleRatio);
	}

	public void refreshVoiceReadState(View convertView, final Msg msgItem)
	{
		if (!msgItem.isAudioMessage() || msgItem.isSelfMsg())
			return;
		TextView audioUnreadText = ViewHolderHelper.get(convertView, R.id.audioUnreadText);
		if (audioUnreadText == null)
			return;
		if (msgItem.sendState < Msg.VOICE_READ)
			audioUnreadText.setVisibility(View.VISIBLE);
		else
			audioUnreadText.setVisibility(View.GONE);
	}

	public void refreshSendState(View convertView, final Msg msgItem)
	{
		final ProgressBar send_progressbar = ViewHolderHelper.get(convertView, R.id.send_progressbar);
		final ImageView sendFail_image = ViewHolderHelper.get(convertView, R.id.sendFail_image);
		if (send_progressbar == null || sendFail_image == null)
			return;

		if (msgItem.sendState == Msg.SENDING)
		{
			if (send_progressbar.getVisibility() != View.VISIBLE)
				send_progressbar.setVisibility(View.VISIBLE);
			if (sendFail_image.getVisibility() != View.GONE)
				sendFail_image.setVisibility(View.GONE);
		}
		else if (msgItem.sendState == Msg.SEND_FAILED)
		{
			if (send_progressbar.getVisibility() != View.GONE)
				send_progressbar.setVisibility(View.GONE);
			if (send_progressbar.getVisibility() != View.VISIBLE)
				sendFail_image.setVisibility(View.VISIBLE);
		}
		else if (msgItem.sendState == Msg.SEND_SUCCESS)
		{
			if (send_progressbar.getVisibility() != View.GONE)
				send_progressbar.setVisibility(View.GONE);
			if (sendFail_image.getVisibility() != View.GONE)
				sendFail_image.setVisibility(View.GONE);
		}
	}

	private void addSendStatusTimerAndRefresh(final View convertView, final Msg item)
	{
		if (!item.isSelfMsg())
			return;
		if (!item.isRedPackageMessage())
			refreshSendState(convertView, item);
		else
		{
			final ProgressBar send_progressbar = ViewHolderHelper.get(convertView, R.id.send_progressbar);
			final ImageView sendFail_image = ViewHolderHelper.get(convertView, R.id.sendFail_image);
			if (send_progressbar != null && send_progressbar.getVisibility() != View.GONE)
				send_progressbar.setVisibility(View.GONE);
			if (sendFail_image != null && sendFail_image.getVisibility() != View.GONE)
				sendFail_image.setVisibility(View.GONE);
		}

		final MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
		if (holder == null)
			return;
		if ((!item.isSystemMessage() || item.isHornMessage()))
		{
			if (item.sendState == Msg.SENDING)
			{
				if (holder.sendTimer == null)
				{
					holder.sendTimer = new Timer();
				}
				if (holder.sendTimerTask != null)
					return;
				holder.sendTimerTask = new TimerTask()
				{
					@Override
					public void run()
					{
						if (item.sendState == Msg.SENDING)
							item.sendState = Msg.SEND_FAILED;

						if (c == null)
							return;

						((Activity) c).runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								refreshSendState(convertView, item);
								holder.removeSendTimer();
							}
						});
					}
				};

				holder.sendTimer.schedule(holder.sendTimerTask, 10000);
			}
			else
			{
				if (holder.sendTimer == null)
					return;
				holder.removeSendTimer();
			}
		}
	}

	private void setGifData(View convertView, Msg item)
	{
		GifMovieView gifMovieView = ViewHolderHelper.get(convertView, R.id.gifMovieView);
		if (gifMovieView == null)
			return;
		String replacedEmoj = StickManager.getPredefinedEmoj(item.msg);
		if (replacedEmoj != null)
		{
			int resId = ResUtil.getId(c, "drawable", replacedEmoj);
			if (resId != 0 && c.getString(resId).endsWith(".gif"))
				gifMovieView.setMovieResource(resId);
		}
	}

	private void setPicData(View convertView, Msg item)
	{
		ImageView picImageView = ViewHolderHelper.get(convertView, R.id.picImageView);
		
		if (picImageView == null)
		{
			return;
		}
		
		double scale = StickManager.getPredefinedEmojScale(item.msg);
		if(scale > 0)
		{
			int width = (int) (ScaleUtil.dip2px(c, HEAD_PIC_SIZE) * ConfigManager.scaleRatio * getScreenCorrectionFactor() * scale);
			int height = (int) (ScaleUtil.dip2px(c, HEAD_PIC_SIZE) * ConfigManager.scaleRatio * getScreenCorrectionFactor() * scale);
			ViewGroup.MarginLayoutParams picImageViewLayoutParams = (ViewGroup.MarginLayoutParams) picImageView.getLayoutParams();
			picImageViewLayoutParams.width = width;
			picImageViewLayoutParams.height = height;
			picImageView.setLayoutParams(picImageViewLayoutParams);
		}
		
		try
		{
			String replacedEmoj = StickManager.getPredefinedEmoj(item.msg);
			if (StringUtils.isNotEmpty(replacedEmoj))
			{
				picImageView.setImageDrawable(ImageUtil.getDrawableByResName(c, replacedEmoj));
			}
		}
		catch (OutOfMemoryError error)
		{
			error.printStackTrace();
		}

	}

	private void setMessageData(final View convertView, final Msg item)
	{
		final TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
		if (messageText == null)
			return;
		if (item.isSelfMsg())
		{
			setText(messageText, item.msg, item, false);
		}
		else
		{
			if (item.canShowTranslateMsg())
			{
				if (!item.isTipMsg())
					setText(messageText, item.translateMsg, item, true);
				else
					messageText.setText(item.translateMsg);
				TranslateManager.getInstance().enterTranlateQueue(item);
			}
			else
			{
				if (!item.isTipMsg())
					setText(messageText, item.msg, item, false);
				else
					messageText.setText(item.msg);
				if (ConfigManager.autoTranlateMode > 0)
				{
					TranslateManager.getInstance().loadTranslation(item, new TranslateListener()
					{
						@Override
						public void onTranslateFinish(final String translateMsg)
						{
							MessageViewHolder holder = ViewHolderHelper.getViewHolder(convertView);
							if (holder != null)
							{
								Msg msgItem = holder.currentMsgItem;
								if ((msgItem != null && !msgItem.equals(item)) || !item.canShowTranslateMsg()
										|| StringUtils.isEmpty(translateMsg) || translateMsg.startsWith("{\"code\":{"))
									return;
								setTextOnUIThread(messageText, translateMsg, item);
							}
						}
					});
				}
			}
		}

		addItemOnClickListener(convertView, item);
	}

	private void setRedPackageData(View convertView, Msg item)
	{
		MsgUtil.handleRedPackageFinishState(item);

		TextView red_package_title = ViewHolderHelper.get(convertView, R.id.red_package_title);
		if (red_package_title != null)
		{
			if (StringUtils.isNotEmpty(item.translateMsg))
				red_package_title.setText(item.translateMsg);
			else
				red_package_title.setText(item.msg);
		}

		TextView redpackage_time = ViewHolderHelper.get(convertView, R.id.redpackage_time);
		if (redpackage_time != null)
		{
			redpackage_time.setText(item.getSendTimeHM());

		}

		TextView red_package_content = ViewHolderHelper.get(convertView, R.id.red_package_content);
		if (red_package_content != null)
		{
			if (item.isSelfMsg())
				red_package_content.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_CONTENT_2));
			else
				red_package_content.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_RED_PACKAGE_CONTENT));
		}
		addItemOnClickListener(convertView, item);
		// addOnClickItemListener(convertView, item);
	}

	private void setPlayerData(View convertView, Msg item)
	{
		TextView vipLabel = ViewHolderHelper.get(convertView, R.id.vipLabel);
		if (vipLabel != null)
			vipLabel.setText(item.getVipLabel());

		TextView allianceLabel = ViewHolderHelper.get(convertView, R.id.allianceLabel);
		if (allianceLabel != null)
		{
			allianceLabel.setText(CokChannelDef.getAllianceLabel(item));
		}

		TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
		if (nameLabel != null)
		{
			nameLabel.setText(item.getName() + (item.getSrcServerId() > 0 ? "#" + item.getSrcServerId() : ""));
		}

		ImageView headImage = ViewHolderHelper.get(convertView, R.id.headImage);
		if (headImage != null)
		{
			ImageUtil.setHeadImage(c, item.getHeadPic(), headImage, item.getUser());
		}

		ImageView headImageKingIcon = ViewHolderHelper.get(convertView, R.id.headImageKingIcon);
		if (headImageKingIcon != null)
		{
			if (isKingMsg(item) && headImageKingIcon.getVisibility() != View.VISIBLE)
				headImageKingIcon.setVisibility(View.VISIBLE);
			else if (!isKingMsg(item) && headImageKingIcon.getVisibility() != View.GONE)
				headImageKingIcon.setVisibility(View.GONE);
		}

		ImageView privilegeImage = ViewHolderHelper.get(convertView, R.id.privilegeImage);
		if (privilegeImage != null)
		{
			String privilegeImageName = getGmodResourceName(item.getGmod());
			if (StringUtils.isNotEmpty(privilegeImageName))
			{
				privilegeImage.setImageDrawable(ImageUtil.getDrawableByResName(c, privilegeImageName));
			}
			else
			{
				privilegeImage.setImageDrawable(null);
			}
		}

	}

	@SuppressLint("ClickableViewAccessibility")
	private void addHeadImageOnClickAndTouchListener(View convertView, final Msg item)
	{
		FrameLayout headImageContainer = ViewHolderHelper.get(convertView, R.id.headImageContainer);
		if (headImageContainer == null)
			return;
		headImageContainer.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				int iAction = event.getAction();
				if (iAction == MotionEvent.ACTION_DOWN || iAction == MotionEvent.ACTION_MOVE)
				{
					CompatibleApiUtil.getInstance().setButtonAlpha(v, false);
				}
				else
				{
					CompatibleApiUtil.getInstance().setButtonAlpha(v, true);
				}
				return false;
			}
		});

		headImageContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				if (!item.isSystemHornMsg())
				{
					if (GSController.isContactMod)
						GSController.doHostAction("showPlayerInfo@mod", item.uid, item.getName(), "", true);
					else
						GSController.doHostAction("showPlayerInfo", item.uid, item.getName(), "", true);
				}
			}
		});
	}

	// @SuppressWarnings("deprecation")
	// @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	// private void setHeadImageBackground(View convertView, Msg msgItem,
	// int sdk)
	// {
	// String headImageBg = "icon_kuang";
	// FrameLayout headImageContainer = ViewHolderHelper.get(convertView,
	// R.id.headImageContainer);
	// if (headImageContainer == null)
	// return;
	// if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
	// {
	// headImageContainer.setBackgroundDrawable(ImageUtil.getDrawableByResName(c,
	// headImageBg));
	// }
	// else
	// {
	// headImageContainer.setBackground(ImageUtil.getDrawableByResName(c,
	// headImageBg));
	// }
	//
	// }

	// @SuppressWarnings("deprecation")
	// @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	// private void setRedPackageBackground(View convertView, Msg msgItem,
	// int sdk)
	// {
	// LinearLayout red_package_top_layout = ViewHolderHelper.get(convertView,
	// R.id.red_package_top_layout);
	// LinearLayout red_package_bottom_layout =
	// ViewHolderHelper.get(convertView, R.id.red_package_bottom_layout);
	// String topbackground = "redpackage_left_bg";
	// String bottombackground = "redpackage_left_time_bg";
	// if (msgItem.isSelfMsg())
	// {
	// topbackground = "redpackage_right_bg";
	// bottombackground = "redpackage_right_time_bg";
	// }
	// else
	// {
	// topbackground = "redpackage_left_bg";
	// bottombackground = "redpackage_left_time_bg";
	// }
	//
	// Drawable topbackgroundDrawable = red_package_top_layout.getBackground();
	// Drawable bottombackgroundDrawable =
	// red_package_bottom_layout.getBackground();
	// topbackgroundDrawable = null;
	// bottombackgroundDrawable = null;
	//
	// if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
	// {
	// red_package_top_layout.setBackgroundDrawable(ImageUtil.getDrawableByResName(c,
	// topbackground));
	// red_package_bottom_layout.setBackgroundDrawable(ImageUtil.getDrawableByResName(c,
	// bottombackground));
	// }
	// else
	// {
	// red_package_top_layout.setBackground(ImageUtil.getDrawableByResName(c,
	// topbackground));
	// red_package_bottom_layout.setBackground(ImageUtil.getDrawableByResName(c,
	// bottombackground));
	// }
	// }

	private String getBackground(int itemType)
	{
		String background = "";
		switch (itemType)
		{
			case ITEM_MESSAGE_NORMAL_SEND:
			case ITEM_AUDIO_NORMAL_SEND:
				background = "chatfrom_bg";
				break;
			case ITEM_MESSAGE_HORN_SEND:
			case ITEM_AUDIO_HORN_SEND:
				background = "chat_horn_right_bg";
				break;
			case ITEM_MESSAGE_SYS_SEND:
			case ITEM_AUDIO_SYS_SEND:
				background = "chatsystem_right_bg";
				break;
			case ITEM_MESSAGE_KING_SEND:
			case ITEM_AUDIO_KING_SEND:
				background = "king_msg_right_bg";
				break;
			case ITEM_MESSAGE_NEWYEAR_SEND:
			case ITEM_AUDIO_NEWYEAR_SEND:
				background = "chat_newyear_right_bg_normal";
				break;
			case ITEM_MESSAGE_SVIP_SEND:
			case ITEM_AUDIO_SVIP_SEND:
				background = "chat_svip_right_bg";
				break;
			case ITEM_MESSAGE_NORMAL_RECEIVE:
			case ITEM_AUDIO_NORMAL_RECEIVE:
				background = "chatto_bg";
				break;
			case ITEM_MESSAGE_HORN_RECEIVE:
			case ITEM_AUDIO_HORN_RECEIVE:
				background = "chat_horn_left_bg";
				break;
			case ITEM_MESSAGE_SYS_RECEIVE:
			case ITEM_AUDIO_SYS_RECEIVE:
				background = "chatsystem_left_bg";
				break;
			case ITEM_MESSAGE_KING_RECEIVE:
			case ITEM_AUDIO_KING_RECEIVE:
				background = "king_msg_left_bg";
				break;
			case ITEM_MESSAGE_NEWYEAR_RECEIVE:
			case ITEM_AUDIO_NEWYEAR_RECEIVE:
				background = "chat_newyear_left_bg_normal";
				break;
			case ITEM_MESSAGE_SVIP_RECEIVE:
			case ITEM_AUDIO_SVIP_RECEIVE:
				background = "chat_svip_left_bg";
				break;
			case ITEM_MESSAGE_BATTLE_SEND:
				background = "chat_battle_right_bg";
				break;
			case ITEM_MESSAGE_BATTLE_RECEIVE:
				background = "chat_battle_left_bg";
				break;
			default:
				break;
		}
		return background;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setMessageTextBackground(View convertView, int itemType, int sdk)
	{
		TextView messageText = ViewHolderHelper.get(convertView, R.id.messageText);
		if (messageText == null)
			return;
		String background = getBackground(itemType);
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			messageText.setBackgroundDrawable(c.getResources().getDrawable(ImageUtil.getHeadResId(c, background)));
		}
		else
		{
			messageText.setBackground(c.getResources().getDrawable(ImageUtil.getHeadResId(c, background)));
		}
	}

	private void setSendTimeData(View convertView, Msg item)
	{
		TextView sendDateLabel = ViewHolderHelper.get(convertView, R.id.sendDateLabel);
		if (sendDateLabel == null)
			return;
		if (MsgUtil.isDifferentDate(item, items))
		{
			if (sendDateLabel.getVisibility() == View.GONE)
				sendDateLabel.setVisibility(View.VISIBLE);
			sendDateLabel.setText(item.getSendTimeToShow());
			if (GSController.sendTimeTextHeight == 0)
				GSController.sendTimeTextHeight = sendDateLabel.getHeight();
		}
		else
		{
			if (sendDateLabel.getVisibility() == View.VISIBLE)
				sendDateLabel.setVisibility(View.GONE);
		}
	}

	private void setNewMsgTipData(View convertView, Msg item)
	{
		TextView newMsgLabel = ViewHolderHelper.get(convertView, R.id.newMsgLabel);
		if (newMsgLabel == null)
			return;
		if (item.firstNewMsgState == 1)
		{
			newMsgLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_NEW_MESSAGE_BELOW));
		}
		else
		{
			newMsgLabel.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_N_NEW_MESSAGE_BELOW, ChannelManager.LOAD_ALL_MORE_MAX_COUNT
					+ ""));
		}
	}

	private void setChatRoomTipData(View convertView, Msg item)
	{
		TextView messageText_center = ViewHolderHelper.get(convertView, R.id.messageText_center);
		if (messageText_center == null)
			return;
		if (item.canShowTranslateMsg())
		{
			messageText_center.setText(item.translateMsg);
			TranslateManager.getInstance().enterTranlateQueue(item);
		}
		else
		{
			messageText_center.setText(item.msg);
			TranslateManager.getInstance().enterTranlateQueue(item);
		}
	}

	private void addOnClickSendStateListener(View convertView, final Msg item)
	{
		if (!item.isSelfMsg())
			return;
		ImageView sendFail_image = ViewHolderHelper.get(convertView, R.id.sendFail_image);
		if (sendFail_image == null)
			return;
		sendFail_image.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (item.sendState != Msg.SEND_SUCCESS)
					MenuController.showReSendConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_RESEND), item, getChatFragment());
			}
		});
	}

	public void setProgressBarState(boolean showProgressBar, boolean showSendFailImage)
	{

	}

	private int getGmodResourceId(int gmod)
	{
		int idPrivilegeImage = 0;
		switch (gmod)
		{
			case 2:
				idPrivilegeImage = R.drawable.mod;
				break;
			case 4:
				idPrivilegeImage = R.drawable.smod;
				break;
			case 5:
				idPrivilegeImage = R.drawable.tmod;
				break;
			case 3:
				idPrivilegeImage = R.drawable.gm;
				break;
			case 11:
				idPrivilegeImage = R.drawable.vip_certification;
				break;
			default:
				break;
		}
		return idPrivilegeImage;
	}

	private String getGmodResourceName(int gmod)
	{
		String idPrivilegeImage = "";
		switch (gmod)
		{
			case 2:
				idPrivilegeImage = "mod";
				break;
			case 4:
				idPrivilegeImage = "smod";
				break;
			case 5:
				idPrivilegeImage = "tmod";
				break;
			case 3:
				idPrivilegeImage = "gm";
				break;
			case 11:
				idPrivilegeImage = "vip_certification";
				break;
			default:
				break;
		}
		return idPrivilegeImage;
	}

	@SuppressLint("InflateParams")
	private View createViewByMessage(Msg msgItem, int position, int type)
	{
		int itemType = getItemViewType(position);
		boolean isSelfMsg = msgItem.isSelfMsg();
		boolean isRightMessage = ConfigManager.getInstance().needRTL()?!isSelfMsg:isSelfMsg;
		if (type == Msg.MSGITEM_TYPE_MESSAGE)
		{
			if (isRightMessage)
				return inflater.inflate(R.layout.msgitem_message_send, null);
			else
				return inflater.inflate(R.layout.msgitem_message_receive, null);
		}
		else if (type == Msg.MSGITEM_TYPE_GIF)
		{
			if (itemType == ITEM_GIF_SEND)
				return inflater.inflate(R.layout.msgitem_gif_send, null);
			else if (itemType == ITEM_GIF_RECEIVE)
				return inflater.inflate(R.layout.msgitem_gif_receive, null);
		}
		else if (type == Msg.MSGITEM_TYPE_PIC)
		{
			if (itemType == ITEM_PIC_SEND)
				return inflater.inflate(R.layout.msgitem_pic_send, null);
			else if (itemType == ITEM_PIC_RECEIVE)
				return inflater.inflate(R.layout.msgitem_pic_receive, null);
		}
		else if (type == Msg.MSGITEM_TYPE_REDPACKAGE)
		{
			if (itemType == ITEM_REDPACKAGE_SEND)
				return inflater.inflate(R.layout.msgitem_redpackage_send, null);
			else if (itemType == ITEM_REDPACKAGE_RECEIVE)
				return inflater.inflate(R.layout.msgitem_redpackage_receive, null);
		}
		else if (type == Msg.MSGITEM_TYPE_CHATROM_TIP)
		{
			if (itemType == ITEM_CHATROOM_TIP)
				return inflater.inflate(R.layout.msgitem_chatroom_tip, null);
		}
		else if (type == Msg.MSGITEM_TYPE_NEW_MESSAGE_TIP)
		{
			return itemType == ITEM_NEWMESSAGE_TIP ? inflater.inflate(R.layout.msgitem_newmsg_tip, null) : null;
		}
		else if (type == Msg.MSGITEM_TYPE_AUDIO)
		{
			if (isRightMessage)
				return inflater.inflate(R.layout.msgitem_audio_send, null);
			else
				return inflater.inflate(R.layout.msgitem_audio_receive, null);
		}

		return null;
	}

	private ChatFragment getChatFragment()
	{
		if (c != null && c instanceof ChatActivity && ((ChatActivity) c).fragment != null
				&& ((ChatActivity) c).fragment instanceof ChatFragment)
		{
			return ((ChatFragment) ((ChatActivity) c).fragment);
		}
		else
		{
			return null;
		}
	}

	private static final String	JOIN_NOW_URL	= "JoinNow";
	private static final String	SHOW_EQUIP_URL	= "ShowEquip";

	private String convertLineBreak(String input)
	{
		return input.replace("\n", "<br/>");
	}

	private int getColorByIndex(int index)
	{
		int color = 0;
		switch (index)
		{
			case 0:
				color = 0xffc7beb3;
				break;
			case 1:
				color = 0xff56e578;
				break;
			case 2:
				color = 0xff4599f8;
				break;
			case 3:
				color = 0xffaf49ea;
				break;
			case 4:
				color = 0xffe8771f;
				break;
			case 5:
				color = 0xffedd538;
				break;
			case 6:
				color = 0xffff0000;
				break;
			default:
				color = 0xffc7beb3;
				break;
		}
		return color;
	}

	private void setText(TextView textView, String str, Msg item, boolean isTranslated)
	{
		String equipName = "";
		String taskName = "";
		String allianceTreasureName = "";
		int colorIndex = -1;
		if (item.isEquipMessage())
		{
			String msgStr = item.msg;
			if (StringUtils.isNotEmpty(msgStr))
			{
				String[] equipInfo = msgStr.split("\\|");
				if (equipInfo.length == 2)
				{
					equipName = LanguageManager.getLangByKey(equipInfo[1]);
					if (StringUtils.isNumeric(equipInfo[0]))
						colorIndex = Integer.parseInt(equipInfo[0]);
				}
			}
			str = LanguageManager.getLangByKey(LanguageKeys.TIP_EQUIP_SHARE, equipName);
		}
		else if (item.isAllianceTaskMessage())
		{
			String msgStr = item.msg;
			if (StringUtils.isNotEmpty(msgStr))
			{
				String[] taskInfo = msgStr.split("\\|");
				if (taskInfo.length >= 4)
				{
					taskName = LanguageManager.getLangByKey(taskInfo[2]);
					if (StringUtils.isNumeric(taskInfo[0]))
						colorIndex = Integer.parseInt(taskInfo[0]);
					String taskPlayerName = taskInfo[3];
					if (taskInfo.length > 4)
					{
						for (int i = 4; i < taskInfo.length; i++)
						{
							taskPlayerName += "|" + taskInfo[i];
						}
					}
					if (StringUtils.isNotEmpty(taskPlayerName))
					{
						try
						{
							List<AllianceTaskInfo> taskInfoArr = JSON.parseArray(taskPlayerName, AllianceTaskInfo.class);
							if (taskInfoArr != null && taskInfoArr.size() >= 1 && taskInfoArr.get(0) != null)
							{
								String publisher = taskInfoArr.get(0).getName();
								if (taskInfoArr.size() == 1 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1))
								{
									str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_1, publisher, taskName);
								}
								else if (taskInfoArr.size() == 2 && taskInfo[1].equals(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2))
								{
									AllianceTaskInfo taskInfo2 = taskInfoArr.get(1);
									if (taskInfo2 != null)
									{
										str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TASK_SHARE_2, publisher, taskName,
												taskInfo2.getName());
									}
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}

		}
		else if (item.isAllianceTreasureMessage())
		{
			String name = item.getAllianceTreasureInfo(1);
			if (StringUtils.isNotEmpty(name) && StringUtils.isNumeric(name))
				allianceTreasureName = LanguageManager.getLangByKey(name);
			if (StringUtils.isNotEmpty(allianceTreasureName))
				str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_TREASURE_SHARE, allianceTreasureName);
			String colorStr = item.getAllianceTreasureInfo(0);
			if (StringUtils.isNotEmpty(colorStr) && StringUtils.isNumeric(colorStr))
				colorIndex = Integer.parseInt(colorStr);
		}
		else if (item.isAllianceHelpMessage())
		{
			str = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE_HELP);
		}
		else if (item.isLotteryMessage())
		{
			str = LanguageManager.getLangByKey(LanguageKeys.TIP_LUCK_WHEEL);
		}
		else if (item.isStealFailedMessage())
		{
			String msgStr = Msg.parseStealFailedMsg(item);
			str = StringUtils.isNotEmpty(msgStr) ? msgStr :LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);
		}
		else if (item.isShotMessage())
		{
			String msgStr = Msg.parseShotMsg(item);
			if(StringUtils.isNotEmpty(msgStr))
				str = msgStr;
		}
		else if( item.isAllianceSkillMessage())
		{
			if(StringUtils.isNotEmpty(item.attachmentId))
			{
				try
				{
					AllianceSkillInfo skillInfo = JSON.parseObject(item.attachmentId, AllianceSkillInfo.class);
					if (skillInfo != null)
					{
						String skillId = skillInfo.getSkillId();
						if (StringUtils.isNotEmpty(skillId))
						{
							String skillName = JniController.getInstance().excuteJNIMethod("getNameById", new Object[] { skillId });
							String skillDes = JniController.getInstance().excuteJNIMethod("getPropById",
									new Object[] { skillId, "description" });
							String skillBase = JniController.getInstance().excuteJNIMethod("getPropById", new Object[] { skillId, "base" });
							String des = "";
							if (StringUtils.isEmpty(skillBase))
							{
								des = skillDes + skillBase;
							}
							else
							{
								String[] baseArr = skillBase.split("\\|");
								if (baseArr.length == 1)
								{
									des = LanguageManager.getLangByKey(skillDes, skillBase);
								}
								else if (baseArr.length == 2)
								{
									des = LanguageManager.getLangByKey(skillDes, baseArr[0], baseArr[1]);
								}
							}
							str = LanguageManager.getLangByKey(skillInfo.getDialog(), skillName, des);
						}

					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (item.isAllianceOfficerMessage())
		{
			if (StringUtils.isNotEmpty(item.attachmentId))
			{
				try
				{
					AllianceOfficerAttachment officer = JSON.parseObject(item.attachmentId, AllianceOfficerAttachment.class);
					if (officer != null)
					{
						str = LanguageManager.getLangByKey(officer.getDialog(), officer.getName(),
								LanguageManager.getLangByKey(officer.getOfficer()));
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		if (item.isVersionInvalid())
			str = LanguageManager.getLangByKey(LanguageKeys.MSG_VERSION_NO_SUPPORT);

		item.currentText = str;
		textView.setText(str);
		textView.setMovementMethod(LinkMovementMethod.getInstance());

		CharSequence text = textView.getText();
		String textStr = text.toString();

		SpannableStringBuilder style = new SpannableStringBuilder(text);
		style.clearSpans();

		Pattern pattern = Pattern
				.compile("(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])(:|：|: |： )(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find())
		{
			String matchStr = matcher.group();
			System.out.println("matchStr:" + matchStr);
			int start = textStr.indexOf(matchStr);
			style.setSpan(new MyURLSpan(matchStr.replaceAll("(:|：|: |： )", ",")), start, start + matchStr.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		if (CokChannelDef.canShowSysIcon() && item.isSystemMessage()
				&& !(item.isHornMessage() || item.isStealFailedMessage()))
		{
			Drawable d;
			if(ConfigManager.getInstance().needRTL())
				d = c.getResources().getDrawable(R.drawable.sys_ar);
			else
				d = c.getResources().getDrawable(R.drawable.sys);
			if(d!=null)
			{
				if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
				{
					d.setBounds(0, -10, (int) (d.getIntrinsicWidth() * 0.8f * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
							(int) (d.getIntrinsicHeight() * 0.9f * ConfigManager.scaleRatio * getScreenCorrectionFactor()) - 10);
				}
				else
				{
					d.setBounds(0, -10, (int) (d.getIntrinsicWidth() * 0.8f), (int) (d.getIntrinsicHeight() * 0.9f) - 10);
				}
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
				style.insert(0, "1");
				style.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		// 添加末尾的战报图标
		boolean canViewBattleReport = ((item.isBattleReport() || item.isDetectReport()) && !UserManager.getInstance().getCurrentUser().allianceId
				.equals(""));
		if (canViewBattleReport)
		{
			Drawable d = c.getResources().getDrawable(R.drawable.mail_battlereport);
			if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
						(int) (d.getIntrinsicHeight() * 0.5 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
			}
			else
			{
				d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.5), (int) (d.getIntrinsicHeight() * 0.5));
			}

			ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
			style.append("1");
			String tmp = style.toString();
			style.setSpan(span, tmp.length() - 1 > 0 ? tmp.length() - 1 : 0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		// 添加末尾的装备分享
		if (item.isEquipMessage())
		{

			int color = getColorByIndex(colorIndex);
			String txt = text.toString();
			int start = txt.indexOf(equipName) ;
			style.setSpan(new ForegroundColorSpan(color), start, start + equipName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			Drawable d = c.getResources().getDrawable(R.drawable.equip_share);
			if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
			{
				d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()),
						(int) (d.getIntrinsicHeight() * 0.8 * ConfigManager.scaleRatio * getScreenCorrectionFactor()));
			}
			else
			{
				d.setBounds(0, 0, (int) (d.getIntrinsicWidth() * 0.8), (int) (d.getIntrinsicHeight() * 0.8));
			}

			ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
			style.append("1");
			String tmp = style.toString();
			style.setSpan(span, tmp.length() - 1 > 0 ? tmp.length() - 1 : 0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else if (item.isAllianceTaskMessage())
		{

			int color = getColorByIndex(colorIndex);
			String txt = text.toString();
			int start = txt.indexOf(taskName) + 1;
			style.setSpan(new ForegroundColorSpan(color), start, start + taskName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		else if (item.isAllianceTreasureMessage())
		{
			if (StringUtils.isNotEmpty(allianceTreasureName))
			{
				int color = getColorByIndex(colorIndex);
				String txt = text.toString();
				int start = txt.indexOf(allianceTreasureName) + 1;
				style.setSpan(new ForegroundColorSpan(color), start, start + allianceTreasureName.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		// 添加时间、翻译信息
		String time = "";
		if (!item.isSelfMsg())
		{
			if (isTranslated)
			{
				String originalLang = item.originalLang;
				String[] originalLangArr = originalLang.split(",");
				String lang = originalLang;
				for (int i = 0; i < originalLangArr.length; i++)
				{
					if (StringUtils.isNotEmpty(originalLangArr[i]))
					{
						lang = LanguageManager.getOriginalLangByKey(originalLangArr[i]);
						if (!lang.startsWith("lang."))
							break;
					}
				}
				time += " " + LanguageManager.getLangByKey(LanguageKeys.TIP_TRANSLATED_BY, lang);
			}
			else if (!isTranslated
					&& TranslateManager.getInstance().isTranslateMsgValid(item)
					&& !item.isTranlateDisable()
					&& !item.isOriginalSameAsTargetLang()
					&& (IMCore.getInstance().getAppConfig().isDefaultTranslateEnable() || (!IMCore.getInstance().getAppConfig().isDefaultTranslateEnable() && item.hasTranslatedByForce)))
			{
				time += " " + LanguageManager.getLangByKey(LanguageKeys.MENU_ORIGINALLAN);
			}
		}

		if (StringUtils.isNotEmpty(time))
		{
			style.append("\n");
			SpannableString styledResultText = new SpannableString(time);
			styledResultText.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE), 0, styledResultText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			styledResultText.setSpan(new ForegroundColorSpan(0xff808080), 0, styledResultText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			style.append(styledResultText);
		}

		textView.setText(style);
	}

	private String insertCoordinateLink(String htmlLinkText)
	{
		// (1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])
		// 逆序可贪婪匹配，遇到不合法数字时可能只匹配合法部分
		// htmlLinkText = htmlLinkText.replaceAll("\\(([0-9]+),([0-9]+)\\)",
		// "<a href='$1,$2'><u>($1,$2)</u></a>");
		htmlLinkText = htmlLinkText.replaceAll(
				"(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])(:|：|: |： )(1200|[1][0-1][0-9]{2}|[1-9][0-9]{2}|[1-9][0-9]|[0-9])",
				"<a href='$1,$3'><u> $1:$3 </u></a>");
		return htmlLinkText;
	}

	private TextView	joinAnnounceTextView;
	private Msg		joinAnnounceItem;

	public void onJoinAnnounceInvitationSuccess()
	{
		if (joinAnnounceTextView != null && joinAnnounceItem != null)
		{
			if (joinAnnounceItem.hasTranslated)
				setText(joinAnnounceTextView, joinAnnounceItem.currentText, joinAnnounceItem, true);
			else
				setText(joinAnnounceTextView, joinAnnounceItem.currentText, joinAnnounceItem, false);
		}
		joinAnnounceItem = null;
		joinAnnounceTextView = null;
	}

	private void onURLClick(View widget, String url)
	{
		ignoreClick = true;
		final String[] coords = url.split(",");

		if (coords[0].equals(SHOW_EQUIP_URL))
		{
			try
			{
				if (coords.length == 2 && StringUtils.isNotEmpty(coords[1]))
				{
					GSController.doHostAction("showEquipment", "", "", coords[1], true);
				}
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
			return;
		}
		else
		{
			if (!isCoordinateValid(coords[0]) || !isCoordinateValid(coords[1]))
			{
				Toast.makeText(c, "coordinate (" + coords[0] + "," + coords[1] + ") is invalid!", Toast.LENGTH_LONG).show();
				return;
			}
			GSController.doHostAction("gotoCoordinate", coords[0], coords[1], "", false);
		}
	}

	private class MyURLSpan extends ClickableSpan
	{
		private String	mUrl;

		MyURLSpan(String url)
		{
			mUrl = url;
		}

		@Override
		public void onClick(View widget)
		{
			onURLClick(widget, mUrl);
		}
	}

	// 合法坐标[0,1200]
	public boolean isCoordinateValid(String coord)
	{
		return Integer.parseInt(coord) >= 0 && Integer.parseInt(coord) <= 1200;
	}

	/**
	 * 高ppi手机的缩放修正因子
	 */
	public double getScreenCorrectionFactor()
	{
		int density = c.getResources().getDisplayMetrics().densityDpi;

		if (density >= DisplayMetrics.DENSITY_XXHIGH)
		{
			// 小米note3是640，大于DENSITY_XXHIGH
			return 0.8;
		}
		else
		{
			return 1.0;
		}
	}

	private boolean	ignoreClick	= false;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void copyToClipboard(QuickAction source)
	{
		if (source.currentTextView == null || !(source.currentTextView instanceof TextView))
			return;

		Msg item = getMsgItemFromQuickAction(source);
		if (item == null)
			return;
		String text = item.canShowTranslateMsg() ? item.translateMsg : item.msg;

		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ((Activity) c)
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		}
		else
		{
			// 一个label对应一个clipboard slot
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ((Activity) c)
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("cok_" + item.getName() + "_" + item.getSendTime(), text);
			clipboard.setPrimaryClip(clip);
		}
	}

	// 去掉复制后文本中的[obj]块（图片导致）
	// http://stackoverflow.com/questions/8560045/android-getting-obj-using-textview-settextcharactersequence
	private CharSequence stripHtml(String s)
	{
		String result = s.substring(0, s.lastIndexOf("\n"));
		result = result.replaceAll("\n", "#linebreak#");
		result = Html.fromHtml(result).toString().replace((char) 65532, (char) 32);
		result = result.replaceAll("#linebreak#", "\n");
		result = result.trim();
		return result;
	}

	public void destroy()
	{
		XiaoMiToolManager.getInstance().removeAudioListener(this);

		c = null;
		inflater = null;
		items = null;

		if (quickAction != null)
		{
			quickAction.currentTextView = null;
			quickAction.setOnActionItemClickListener(null);
		}
		actionClickListener = null;
	}

	@Override
	public int getCount()
	{
		if (items != null)
			return items.size();
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		if (position >= 0 && position < items.size())
			return items.get(position);
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public void onEndingRecord()
	{
		if (getChatFragment() != null)
		{
			getChatFragment().exitRecordingUI();
		}
	}

	@Override
	public void onPlayBegin(String s)
	{
		System.out.println("onPlayBegin(String");
		if (playingMsgItem == null)
			return;
		GSController.getInstance().setGameMusiceEnable(false);
		// JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable",
		// new Object[]{Boolean.valueOf(false)});
		isPlayingAudio = true;
		playingMsgItem.isAudioDownloading = false;
		playingMsgItem.readStateBefore = playingMsgItem.sendState;
		MsgUtil.setVoiceRecordReadState(playingMsgItem);
//		GSController.getInstance().refreshVoiceReadState();
		notifyDataSetChanged();
		startPlayAudioTimer(playingTextView, playingMsgItem);

	}

	@Override
	public void onPlayEnd(String s, boolean flag)
	{
		System.out.println("onPlayEnd(String");
		if (playingMsgItem != null)
		{
			playingMsgItem.isAudioDownloading = false;
		}
		isPlayingAudio = false;
		stopPlayAudioTimer();
		if (XiaoMiToolManager.getInstance().isRecordVoice || XiaoMiToolManager.getInstance().exitChat)
		{
			if (c != null)
			{
				((Activity) c).runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							setAudioTextIcon(playingMsgItem, playingTextView, 0);
							playingMsgItem = null;
							playingTextView = null;
							nextMsgItem = null;
							nextTextView = null;
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});
			}
			return;
		}

		if (c != null)
		{
			((Activity) c).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						setAudioTextIcon(playingMsgItem, playingTextView, 0);
						if (nextTextView != null && nextMsgItem != null)
						{
							onAudioMsgClicked(nextTextView, nextMsgItem);
						}
						else
						{
							if (playingMsgItem.readStateBefore <= Msg.VOICE_UNREAD)
							{
								playingMsgItem.readStateBefore = playingMsgItem.sendState;
								boolean hasNext = false;
								if (items != null && items.size() > 0)
								{
									int position = items.indexOf(playingMsgItem);
									for (int i = position + 1; i < items.size(); i++)
									{
										Msg item = items.get(i);
										if (item != null && item.isAudioMessage() && item.sendState <= Msg.VOICE_UNREAD)
										{
											nextMsgItem = item;
											if (viewArray != null)
											{
												View converView = viewArray.get(i);
												TextView messageText = ViewHolderHelper.get(converView, R.id.messageText);
												nextTextView = messageText;
											}
											hasNext = true;
											onAudioMsgClicked(nextTextView, nextMsgItem);
											break;
										}
									}
								}

								if (!hasNext)
									// JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable",
									// new Object[]{Boolean.valueOf(true)});
									GSController.getInstance().setGameMusiceEnable(true);
							}
							else
							{
								GSController.getInstance().setGameMusiceEnable(true);
								// JniController.getInstance().excuteJNIVoidMethod("setGameMusicEnable",
								// new Object[]{Boolean.valueOf(true)});
							}
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

	@Override
	public void onRecordFailed()
	{
	}

	@Override
	public void onRecordFinished(String s, long l)
	{
	}

	@Override
	public void onRecordInitializationCancelled()
	{
	}

	@Override
	public void onRecordInitializationFailed()
	{
	}

	@Override
	public void onRecordInitializationSucceed()
	{
	}

	@Override
	public void onRecordStart()
	{
	}

	@Override
	public void onRmsChanged(int arg0)
	{
	}
}