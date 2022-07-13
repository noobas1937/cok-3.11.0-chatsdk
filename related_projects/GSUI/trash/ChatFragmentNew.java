package com.elex.im.ui.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elex.im.CokChannelDef;
import com.elex.im.CokConfig;
import com.elex.im.core.IMCore;
import com.elex.im.core.event.ChannelChangeEvent;
import com.elex.im.core.event.Event;
import com.elex.im.core.event.EventCallBack;
import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.ConfigManager;
import com.elex.im.core.model.LanguageKeys;
import com.elex.im.core.model.LanguageManager;
import com.elex.im.core.model.LocalConfig;
import com.elex.im.core.model.MailManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.model.User;
import com.elex.im.core.model.UserManager;
import com.elex.im.core.model.db.DBDefinition;
import com.elex.im.core.util.CallBack;
import com.elex.im.core.util.LogUtil;
import com.elex.im.core.util.PermissionManager;
import com.elex.im.core.util.ResUtil;
import com.elex.im.core.util.ScaleUtil;
import com.elex.im.core.util.StringUtils;
import com.elex.im.core.util.TimeManager;
import com.elex.im.ui.GSController;
import com.elex.im.ui.R;
import com.elex.im.ui.UIManager;
import com.elex.im.ui.adaptor.CustomExpandableListAdapter;
import com.elex.im.ui.controller.JniController;
import com.elex.im.ui.controller.MenuController;
import com.elex.im.ui.model.ChannelView;
import com.elex.im.ui.net.XiaoMiToolManager;
import com.elex.im.ui.util.CompatibleApiUtil;
import com.elex.im.ui.util.FixedAspectRatioFrameLayout;
import com.elex.im.ui.util.ImageUtil;
import com.elex.im.ui.util.MsgUtil;
import com.elex.im.ui.util.RoundImageView;
import com.elex.im.ui.view.actionbar.ActionBarFragment;
import com.elex.im.ui.view.autoscroll.ScrollText;
import com.elex.im.ui.view.autoscroll.ScrollTextManager;
import com.elex.im.ui.view.listview.ListViewLoadListener;
import com.elex.im.ui.view.listview.PullDownToLoadMoreView;
import com.elex.im.ui.view.misc.messenger.AndroidUtilities;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.AnimatorSetProxy;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.ObjectAnimatorProxy;
import com.elex.im.ui.view.misc.messenger.AnimationCompat.ViewProxy;
import com.elex.im.ui.view.misc.ui.Components.FrameLayoutFixed;
import com.elex.im.ui.view.misc.ui.Components.LayoutHelper;
import com.elex.im.ui.view.misc.ui.Components.SizeNotifierFrameLayout;

public class ChatFragmentNew extends ActionBarFragment implements SensorEventListener
{
	protected RelativeLayout	messagesListFrameLayout;
	private RelativeLayout	messagesListLayout;
	private FrameLayout			noAllianceFrameLayout;
	private LinearLayout		relativeLayout1;
	protected LinearLayout		buttonsLinearLayout;
	protected EditText			replyField;
//	protected LinearLayout		header;
	private MenuItem			attachScreenshotMenu;
	private TextView			wordCount;
	protected Button			addReply;
	private Button				buttonCountry;
	private Button				buttonAlliance;
	private Button				buttonCustom;
	private ArrayList<Button>	channelButton;
	private ImageView			imageView1;
	protected ImageView			imageView2;
	private Button				buttonJoinAlliance;
	private TextView			noAllianceTipText;
	private Timer				mTimer;
	private TimerTask			mTimerTask;
	private CheckBox			horn_checkbox;
//	private LinearLayout		horn_tip_layout;
	private RelativeLayout		horn_scroll_layout;
//	private TextView			horn_text_tip;
	private TextView			horn_name;
	private ScrollText			horn_scroll_text;
	private LinearLayout		tooltipLayout;
	private TextView			tooltipLabel;
	private ImageView			tooltipArrow;
	private ImageView			horn_close_btn;
	private LinearLayout		hs__dragon_chat_tip_layout;
	private TextView			dragon_chat_tip_text;
	private RelativeLayout		custom_chat_tip_layout;
	private TextView			addCustomChatBtn;
	private TextView			custom_chat_tip_text;
	private RelativeLayout		battle_field_tip_layout;
	private TextView			battle_field_btn;
	private TextView			battle_field_tip_text;
	private LinearLayout		custom_setting_layout;
	private TextView			add_title;
	private TextView			add_tip;
	private RoundImageView		customChannelHeadImage;
	private TextView			customChannelName;
	private ImageView			custom_mod_image;
	private FrameLayout			custom_head_layout;
	private FixedAspectRatioFrameLayout headImageContainer;
	private ExpandableListView	custom_expand_listview;
	private CustomExpandableListAdapter customChannelListAdapter;
	private RelativeLayout		custom_channel_setting_layout;
	private TextView			custom_channel_name;
	private ImageView			custom_channel_settting_btn;
	private LinearLayout		country_exchange_layout;
	private TextView			country_channel_name;
	private ImageView			country_exchange_btn;
	private int					loadMoreCount				= 0;
	protected int				loadingHeaderHeight;
	protected boolean			isKeyBoardFirstShowed		= false;
	private int					curMaxInputLength			= 500;
	private LinearLayout		custom_settting_btn_layout;
	private TextView			custom_setting_confim;
	public static Channel  	showingCustomChannel		= null;
	

	public static boolean		rememberPosition			= false;

	private static String		savedText					= "";
	private boolean				isJoinAlliancePopupShowing	= false;
//	public static String		gmailAccount				= "";

	public boolean				isKeyBoradShowing			= false;
	public boolean				isKeyBoradChange			= false;

	private boolean				isSelectMemberBtnEnable		= false;
	private List<ChannelView>	channelViews				= null;
	private int 				customChannelType			= -1;
	private String				customChannelId;
	public boolean				isSettingCustomChannel		= false;
	private boolean 			customChannelChange			= false;
	private List<Channel> friendList = null;
	private List<Channel> chatroomChannelList  = null;
	

	public boolean isSelectMemberBtnEnable()
	{
		return isSelectMemberBtnEnable;
	}

	public int getCustomChannelType()
	{
		return customChannelType;
	}

	public String getCustomChannelId()
	{
		return customChannelId;
	}

	private ChannelView getCurrentChannelView()
	{
		try
		{
			if (channelViews == null || currentChannelViewIndex < 0 || currentChannelViewIndex >= channelViews.size())
				return null;
			return channelViews.get(currentChannelViewIndex);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public Channel getCurrentChannel()
	{
		if(getCurrentChannelView() == null) return null;
		return getCurrentChannelView().channel;
	}

	public ChatFragmentNew()
	{
		isKeyBoardFirstShowed = false;
	}
	
	public void afterSendMsgShowed()
	{
		if(getCurrentChannel() != null)
		{
			notifyDataSetChanged(GSController.getCurrentChannelType(), getCurrentChannel().getChannelID(),true);
			scrollToLastLine();
		}
	}

	public void refreshMemberSelectBtn()
	{
		boolean isAllAllianceMember = StringUtils.isNotEmpty(UserManager.getInstance().getCurrentMail().opponentUid) &&
				StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().uid) &&
				UserManager.getInstance().getCurrentUser().uid.equals(UserManager.getInstance().getCurrentMail().opponentUid);
		if (!((CokChannelDef.isInMailDialog() && !isAllAllianceMember) || GSController.isCreateChatRoom || customChannelType!=-1))
		{
			isSelectMemberBtnEnable = false;
			return;
		}

		try
		{
			if ((CokChannelDef.isInChatRoom() || (CokChannelDef.isInChat() && CokChannelDef.isInChatRoom(customChannelType)))
					&& !CokChannelDef.getIsMemberFlag(UserManager.getInstance().getCurrentMail().opponentUid))
			{
				isSelectMemberBtnEnable = false;
				return;
			}
			ArrayList<String> memberUidArray = GSController.getInstance().getSelectMemberUidArr();
			if (memberUidArray == null)
			{
				isSelectMemberBtnEnable = false;
				return;
			}

			HashMap<String, User> memberInfoMap = UserManager.getInstance().getChatRoomMemberInfoMap();
			isSelectMemberBtnEnable = true;
			if (memberUidArray == null
					|| memberUidArray.size() <= 0
					|| (memberUidArray != null && memberUidArray.size() > 0 && !memberUidArray.contains(UserManager.getInstance()
							.getCurrentUser().uid)))
				isSelectMemberBtnEnable = false;
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}
	}

	public void setSelectMemberBtnState()
	{
		if (getMemberSelectButton() != null)
		{
			getMemberSelectButton().setVisibility(isSelectMemberBtnEnable && !CokChannelDef.isInBasicChat() ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	private void showMessageBox()
	{
		relativeLayout1.setVisibility(View.VISIBLE);
		this.buttonsLinearLayout.setVisibility(View.VISIBLE);
		refreshWordCount();

		if (this.attachScreenshotMenu != null)
		{
			this.attachScreenshotMenu.setVisible(true);
		}
	}

	public void saveState()
	{
		if(channelViews == null)
			return;
		for (int i = 0; i < channelViews.size(); i++)
		{
			ChannelView channelView = channelViews.get(i);
			if (channelView != null)
			{
				Channel channel = channelView.channel;
				if (channel != null && channelView.messagesListView != null)
				{
					channel.lastPosition.x = channelView.messagesListView.getFirstVisiblePosition();
					View v = channelView.messagesListView.getChildAt(0);
					channel.lastPosition.y = (v == null) ? 0 : (v.getTop() - channelView.messagesListView.getPaddingTop());
				}
			}
		}
	}

	protected boolean	isJustCreated	= true;

	public void checkFirstGlobalLayout()
	{
		if (isJustCreated)
		{
			isJustCreated = false;
			refreshTab();
		}

		if (oldChatFragmentHeight == -1 && computeUsableHeight() > 0)
		{
			oldChatFragmentHeight = computeUsableHeight();

		}
		else if (oldChatFragmentHeight > computeUsableHeight())
		{
			oldChatFragmentHeight = computeUsableHeight();
			if (isKeyBoardFirstShowed)
				isKeyBoradShowing = true;
			if (!rememberPosition)
			{
				gotoLastLine();
			}
			else
			{
				rememberPosition = false;
			}
		}
		else if (oldChatFragmentHeight == computeUsableHeight())
		{
			if (isKeyBoradChange)
			{
				keyBoardChangeCount++;
			}
			if (keyBoardChangeCount == 2)
			{
				isKeyBoradChange = false;
			}
		}
		else if (oldChatFragmentHeight < computeUsableHeight())
		{
			keyBoardChangeCount = 0;
			isKeyBoradChange = true;
			oldChatFragmentHeight = computeUsableHeight();
			isKeyBoradShowing = false;
			isKeyBoardFirstShowed = true;
		}

		int usableHeightNow = computeUsableHeight();

		if (usableHeight == -1 && usableHeightNow > 0)
		{
			usableHeight = usableHeightNow;
		}

		if (usableHeight != -1 && usableHeight > usableHeightNow)
		{
			if (!isSystemBarResized)
			{
				isSystemBarResized = true;
				return;
			}
			for (int i = 0; i < channelViews.size(); i++)
			{
				ChannelView channelView = channelViews.get(i);
				if (channelView!=null && channelView.channel != null && channelView.channel.lastPosition.x == -1
						&& channelView.messagesListView != null && channelView.getMessagesAdapter() != null)
				{
					channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
				}
			}
			usableHeight = usableHeightNow;
		}
	}

	protected int		keyBoardChangeCount		= 0;
	protected int		oldChatFragmentHeight	= -1;
	protected boolean	isSystemBarResized		= false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.activity = ((ChatActivity) getActivity());
		initChannelViews();
		return inflater.inflate(ResUtil.getId(this, "layout", "cs__messages_fragment_new"), container, false);
	}

	private FrameLayout.LayoutParams getLayoutParams()
	{
		FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		param.gravity = Gravity.CENTER;
		return param;
	}

	public void refreshIsInLastScreen(final int channelType,final String channelId)
	{
		if (isSameChannel(channelType,channelId))
		{
			try
			{
				inLastScreen = isInLastScreen();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}
	}

	private boolean isInLastScreen()
	{
		// messagesListView存在时messagesListView.getChildAt(0)也可能为0
		if (getCurrentChannelView() == null || getCurrentChannelView().getMessagesAdapter() == null
				|| getCurrentChannelView().getMessagesAdapter().getCount() == 0 || getCurrentChannelView().messagesListView == null)
		{
			return true;
		}
		// 遍历从view.getFirstVisiblePosition()可见高度及到最下方的各个item的高度，计算这高度和是否小于一定的值（1.6屏）
		View v = getCurrentChannelView().messagesListView.getChildAt(0);
		if (v == null)
		{
			return true;
		}

		// 第一个item被上方盖住的部分
		int firstOffset = v.getTop() - getCurrentChannelView().messagesListView.getPaddingTop();

		int totalHeight = v.getHeight() + firstOffset;
		if ((getCurrentChannelView().getMessagesAdapter().getCount() - getCurrentChannelView().messagesListView.getFirstVisiblePosition()) > 20)
		{
			return false;
		}

		for (int i = (getCurrentChannelView().messagesListView.getFirstVisiblePosition() + 1); i < getCurrentChannelView().getMessagesAdapter()
				.getCount(); i++)
		{
			View listItem = getCurrentChannelView().getMessagesAdapter().getView(i, null, getCurrentChannelView().messagesListView);
			listItem.measure(MeasureSpec.makeMeasureSpec(getCurrentChannelView().messagesListView.getWidth(), MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = listItem.getMeasuredHeight();
			totalHeight += h + getCurrentChannelView().messagesListView.getDividerHeight();
		}

		if (totalHeight <= (getCurrentChannelView().messagesListView.getHeight() * 1.75))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	boolean	inLastScreen	= false;
	private boolean isAudioBtnDown = false;
	
	public void updateListPositionForNewMsg(int channelType,String channelId, boolean isSelfMsg)
	{
		if (!isSameChannel(channelType,channelId))
			return;

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
			gotoLastLine();
		inLastScreen = false;
	}
	
	public void smoothUpdateListPositionForNewMsg(int channelType,String channelId, boolean isSelfMsg)
	{
		if (!isSameChannel(channelType,channelId) || getCurrentChannelView().messagesListView == null || getCurrentChannelView().getMessagesAdapter() == null)
		{
			return;
		}

		if (!isSelfMsg && (isKeyBoradShowing || inLastScreen))
		{
			scrollToLastLine();
		}
		inLastScreen = false;
	}

	public void updateListPositionForOldMsg(int channelType,String channelId, int loadCount, final boolean needMergeSendTime)
	{
		if (!isSameChannel(channelType,channelId)|| getCurrentChannelView().getMessagesAdapter() == null)
			return;
		loadMoreCount = loadCount;
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					ListView listView = getCurrentChannelView().messagesListView;
					if(listView!=null)
					{
						if (!getCurrentChannel().isLoadingAllNew)
						{
							int heightOffest = getCurrentChannelView().pullDownToLoadListView.getPullDownHeight();
							if (needMergeSendTime)
							{
								if (GSController.sendTimeTextHeight != 0)
									heightOffest += GSController.sendTimeTextHeight + ScaleUtil.dip2px(activity, 15);
								else
									heightOffest += ScaleUtil.dip2px(activity, 44);
							}
							listView.setSelectionFromTop(loadMoreCount, heightOffest);
						}
						else
						{
							listView.setSelectionFromTop(0, 0);
						}
					}
					
					refreshToolTip();
					getCurrentChannelView().pullDownToLoadListView.hideProgressBar();
					getCurrentChannelView().stopTimerTask();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void changeChatRoomName(String name)
	{
		if (!name.equals(""))
		{
			getTitleLabel().setText(name);
		}
	}

	public void setEditText(String text)
	{
		if (replyField != null)
			replyField.setText(text);
	}

	public static void onMsgAdd(int channelType,String channelId, boolean needCalculateShowTimeIndex)
	{
		dataChanged = true;
		if (UIManager.getChatFragment() != null)
		{
			UIManager.getChatFragment().notifyDataSetChanged(channelType,channelId, needCalculateShowTimeIndex);
			dataChanged = false;
		}
	}

	public void notifyDataSetChanged(final int channelType,final String channelId, final boolean needCalculateShowTimeIndex)
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(!isSameChannel(channelType,channelId))
						return;
					ChannelView curChannelView = getCurrentChannelView();
					if(curChannelView!=null)
					{
						MessagesAdapter adapter = curChannelView.getMessagesAdapter();
						if (adapter != null)
						{
							if (needCalculateShowTimeIndex)
							{
								Channel channel = ChannelManager.getInstance().getChannel(curChannelView.channelType);
								if (channel != null)
									channel.getTimeNeedShowMsgIndex();
							}

							adapter.notifyDataSetChanged();
						}
						refreshHasMoreData();
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}


	public void resetMoreDataStart(int channelType,String channelId)
	{
		if(StringUtils.isEmpty(channelId))
		{
			if(getCurrentChannel() != null)
				channelId = getCurrentChannel().getChannelID();
		}
		if (isSameChannel(channelType,channelId))
		{
			getCurrentChannelView().setLoadingStart(false);
		}
	}

	protected void gotoLastLine()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (getCurrentChannelView() != null && getCurrentChannelView().messagesListView != null
							&& getCurrentChannelView().getMessagesAdapter() != null)
					{
						LogUtil.printVariables(Log.INFO, LogUtil.TAG_DEBUG, "gotoLastLine", getCurrentChannelView().getMessagesAdapter().getCount() - 1);
						getCurrentChannelView().messagesListView.setSelection(getCurrentChannelView().getMessagesAdapter().getCount() - 1);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	protected void scrollToLastLine()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (getCurrentChannelView() != null && getCurrentChannelView().messagesListView != null
							&& getCurrentChannelView().getMessagesAdapter() != null)
					{
						LogUtil.printVariables(Log.INFO, LogUtil.TAG_DEBUG, "scrollToLastLine", getCurrentChannelView().getMessagesAdapter().getCount() - 1);
						getCurrentChannelView().messagesListView
								.smoothScrollToPosition(getCurrentChannelView().getMessagesAdapter().getCount() - 1);
					}
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}
	protected boolean isSameChannel(int channelType,String channelId)
	{
		if (getCurrentChannelView() == null)
			return false;
		System.out.println("isSameChannel  channelType:"+getCurrentChannelView().channelType);
		if (CokChannelDef.isInBasicChat(getCurrentChannelView().channelType))
			return getCurrentChannelView().channelType == channelType;
		else
		{
			if (getCurrentChannel() != null)
			{
				return (getCurrentChannelView().channelType == channelType || getCurrentChannel().getChannelType() == channelType)
						&& StringUtils.isNotEmpty(channelId)
						&& StringUtils.isNotEmpty(getCurrentChannel().getChannelID())
						&& getCurrentChannel().getChannelID().equals(channelId);
			}
						
			else
				return false;
		}
	}

	private final int COLOR_RECORD_BACK = 0xff1f2020; //0xffffffff;
	private final int COLOR_RECORD_DOT_BACK = 0xff1f2020; //0xffffffff;
	private final int COLOR_RECORD_CIRCLE_BACK = 0xff407448; //0xff5795cc;
	private final int COLOR_RECORD_CIRCLE_PAINT = 0x0d000000;
	private final int COLOR_RECORD_SLIDE_TEXT = 0xffa69279; //0xff999999;
	private final int COLOR_RECORD_TIME_TEXT = 0xffa69279; //0xff4d4c4b;
	
    private class RecordCircle extends View {

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint paintRecord = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Drawable micDrawable;
        private float scale;
        private float amplitude;
        private float animateToAmplitude;
        private float animateAmplitudeDiff;
        private long lastUpdateTime;

        public RecordCircle(Context context) {
            super(context);
            paint.setColor(COLOR_RECORD_CIRCLE_BACK);
            paintRecord.setColor(COLOR_RECORD_CIRCLE_PAINT);
            micDrawable = getResources().getDrawable(R.drawable.voice_mic_pressed);
        }

        public void setAmplitude(double value) {
            animateToAmplitude = (float) Math.min(100, value) / 100.0f;
            animateAmplitudeDiff = (animateToAmplitude - amplitude) / 150.0f;
            lastUpdateTime = System.currentTimeMillis();
            invalidate();
        }

        public float getScale() {
            return scale;
        }

        public void setScale(float value) {
            scale = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int cx = getMeasuredWidth() / 2;
            int cy = getMeasuredHeight() / 2;
            float sc;
            float alpha;
            if (scale <= 0.5f) {
                alpha = sc = scale / 0.5f;
            } else if (scale <= 0.75f) {
                sc = 1.0f - (scale - 0.5f) / 0.25f * 0.1f;
                alpha = 1;
            } else {
                sc = 0.9f + (scale - 0.75f) / 0.25f * 0.1f;
                alpha = 1;
            }
            long dt = System.currentTimeMillis() - lastUpdateTime;
            if (animateToAmplitude != amplitude) {
                amplitude += animateAmplitudeDiff * dt;
                if (animateAmplitudeDiff > 0) {
                    if (amplitude > animateToAmplitude) {
                        amplitude = animateToAmplitude;
                    }
                } else {
                    if (amplitude < animateToAmplitude) {
                        amplitude = animateToAmplitude;
                    }
                }
                invalidate();
            }
            lastUpdateTime = System.currentTimeMillis();
            if (amplitude != 0) {
                canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, (AndroidUtilities.dp(42) * (float) getAudioUIScale() + AndroidUtilities.dp(20) * (float) getAudioUIScale() * amplitude) * scale, paintRecord);
            }
            canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, AndroidUtilities.dp(42) * (float) getAudioUIScale() * sc, paint);
            int micWidth = (int) (micDrawable.getIntrinsicWidth() * getAudioUIScale());
            int micHeight = (int) (micDrawable.getIntrinsicHeight() * getAudioUIScale());
            micDrawable.setBounds(cx - micWidth / 2, cy - micHeight / 2, cx + micWidth / 2, cy + micHeight / 2);
            micDrawable.setAlpha((int) (255 * alpha));
            micDrawable.draw(canvas);
        }
    }
    private class RecordDot extends View {

        private Drawable dotDrawable;
        private float alpha;
        private long lastUpdateTime;
        private boolean isIncr;

        public RecordDot(Context context) {
            super(context);

            dotDrawable = getResources().getDrawable(R.drawable.voice_rec);
        }

        public void resetAlpha() {
            alpha = 1.0f;
            lastUpdateTime = System.currentTimeMillis();
            isIncr = false;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            dotDrawable.setBounds(0, 0, AndroidUtilities.dp(11), AndroidUtilities.dp(11));
            dotDrawable.setAlpha(185 + (int) (70 * alpha));
            long dt = (System.currentTimeMillis() - lastUpdateTime);
            if (!isIncr) {
                alpha -= dt / 200.0f;
                if (alpha <= 0) {
                    alpha = 0;
                    isIncr = true;
                }
            } else {
                alpha += dt / 200.0f;
                if (alpha >= 1) {
                    alpha = 1;
                    isIncr = false;
                }
            }
            lastUpdateTime = System.currentTimeMillis();
            dotDrawable.draw(canvas);
            invalidate();
        }
    }

    private String lastTimeString;
    private float startedDraggingX = -1;
    private boolean recordingAudio = false;
    private boolean recordBtnUp = true;
    private float distCanMove = AndroidUtilities.dp(80);
    private int audioInterfaceState;
    private PowerManager.WakeLock mWakeLock;
    
    private void updateAudioRecordIntefrace() {
        if (recordingAudio) {
            if (audioInterfaceState == 1) {
                return;
            }
            audioInterfaceState = 1;
            try {
                if (mWakeLock == null) {
                    PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "audio record lock");
                    mWakeLock.acquire();
                }
            } catch (Exception e) {
//                FileLog.e("tmessages", e);
            }
            AndroidUtilities.lockOrientation(activity);

    		onRecordPanelShown(true);
            recordPanel.setVisibility(View.VISIBLE);
            recordCircle.setVisibility(View.VISIBLE);
            recordCircle.setAmplitude(0);
            recordTimeText.setText("00:00");
            recordDot.resetAlpha();
            lastTimeString = null;

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
            params.leftMargin = AndroidUtilities.dp(30);
            slideLayout.setLayoutParams(params);
            ViewProxy.setAlpha(slideLayout, 1);
            ViewProxy.setX(recordPanel, AndroidUtilities.displaySize.x);
            ViewProxy.setTranslationX(recordCircle, 0);
            if (runningAnimationAudio != null) {
                runningAnimationAudio.cancel();
            }
            runningAnimationAudio = new AnimatorSetProxy();
            runningAnimationAudio.playTogether(ObjectAnimatorProxy.ofFloat(recordPanel, "translationX", 0),
                    ObjectAnimatorProxy.ofFloat(recordCircle, "scale", (float) getAudioUIScale()),
                    ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 0));
            runningAnimationAudio.setDuration(300);
            runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy() {
                @Override
                public void onAnimationEnd(Object animator) {
                    if (runningAnimationAudio != null && runningAnimationAudio.equals(animator)) {
                        ViewProxy.setX(recordPanel, 0);
                        runningAnimationAudio = null;
                    }
                }
            });
            runningAnimationAudio.setInterpolator(new DecelerateInterpolator());
            runningAnimationAudio.start();
        } else {
            if (mWakeLock != null) {
                try {
                    mWakeLock.release();
                    mWakeLock = null;
                } catch (Exception e) {
//                    FileLog.e("tmessages", e);
                }
            }
            AndroidUtilities.unlockOrientation(activity);
            if (audioInterfaceState == 0) {
                return;
            }
            audioInterfaceState = 0;

            if (runningAnimationAudio != null) {
                runningAnimationAudio.cancel();
            }
            runningAnimationAudio = new AnimatorSetProxy();
            runningAnimationAudio.playTogether(ObjectAnimatorProxy.ofFloat(recordPanel, "translationX", AndroidUtilities.displaySize.x),
                    ObjectAnimatorProxy.ofFloat(recordCircle, "scale", 0.0f),
                    ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 1.0f));
            runningAnimationAudio.setDuration(300);
            runningAnimationAudio.addListener(new AnimatorListenerAdapterProxy() {
                @Override
                public void onAnimationEnd(Object animator) {
                    if (runningAnimationAudio != null && runningAnimationAudio.equals(animator)) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
                        params.leftMargin = AndroidUtilities.dp(30);
                        slideLayout.setLayoutParams(params);
                        ViewProxy.setAlpha(slideLayout, 1);
                		onRecordPanelShown(false);
                        recordPanel.setVisibility(View.GONE);
                        recordCircle.setVisibility(View.GONE);
                        runningAnimationAudio = null;
                    }
                }
            });
            runningAnimationAudio.setInterpolator(new AccelerateInterpolator());
            runningAnimationAudio.start();
        }
    }

	private Timer recordTimer;
	private TimerTask recordTimerTask;
	private long recordStartTime;
    private void startRecordTimer()
	{
    	stopRecordTimer();
		recordTimer = new Timer();
		recordTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				long timePassed = System.currentTimeMillis() - recordStartTime;
	            Long time = (Long) timePassed / 1000;
	            final String str = String.format("%02d:%02d", time / 60, time % 60);
	            if (lastTimeString == null || !lastTimeString.equals(str)) {
	            	lastTimeString = str;
	                if (time % 5 == 0) {
//	                    MessagesController.getInstance().sendTyping(dialog_id, 1, 0);
	                }
	                if (recordTimeText != null && activity != null) {
	    				activity.runOnUiThread(new Runnable()
	    				{
	    					@Override
	    					public void run()
	    					{
	    						try
	    						{
	    		                    recordTimeText.setText(str);
	    		                    
//	    		    	            if (recordCircle != null) {
//	    		    	                recordCircle.setAmplitude((Double) 100.0);
//	    		    	            }
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
		if (recordTimer != null){
			recordTimer.schedule(recordTimerTask, 500, 100);
			recordStartTime = System.currentTimeMillis();
		}
	}

	private void stopRecordTimer()
	{
		if (recordTimer != null)
		{
			recordTimer.cancel();
			recordTimer.purge();
			recordTimer = null;
		}
	}
    
    public void didReceivedNotification(String id, Object... args) {
        if (id.equals("NotificationCenter.recordProgressChanged")) {
            Long time = (Long) args[0] / 1000;
            String str = String.format("%02d:%02d", time / 60, time % 60);
            if (lastTimeString == null || !lastTimeString.equals(str)) {
                if (time % 5 == 0) {
//                    MessagesController.getInstance().sendTyping(dialog_id, 1, 0);
                }
                if (recordTimeText != null) {
                    recordTimeText.setText(str);
                }
            }
            if (recordCircle != null) {
                recordCircle.setAmplitude((Double) args[1]);
            }
        } else if (id.equals("NotificationCenter.recordStartError") || id.equals("NotificationCenter.recordStopped")) {
            if (recordingAudio) {
                exitRecordingUI();
            }
        } else if (id.equals("NotificationCenter.recordStarted")) {
            if (!recordingAudio) {
                recordingAudio = true;
                updateAudioRecordIntefrace();
            }
        } else if (id.equals("NotificationCenter.audioDidSent")) {
//            if (delegate != null) {
//                delegate.onMessageSend(null);
//            }
        }
    }
    
	public void exitRecordingUI()
	{
		recordingAudio = false;
		updateAudioRecordIntefrace();
	}
	
	public void refreshCustomChannelImage(Channel channel)
	{
		showingCustomChannel = channel;
		if(activity!=null && CokChannelDef.isInCustomChat() && isSettingCustomChannel)
		{
			ImageUtil.setChannelImage(activity, channel, customChannelHeadImage);
			refreshSettingCustomChannelName(channel);
		}
		if(showingCustomChannel!=null)
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP2));
		else
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP));
	}
	
	private void prepareCustomChannelData()
	{
		if(activity!=null && CokChannelDef.isInChat())
		{
			SparseArray<List<Channel>> channelMap = new SparseArray<List<Channel>>();
			friendList = new ArrayList<Channel>();
			chatroomChannelList = new ArrayList<Channel>();
			List<String> friendChannelIdList = new ArrayList<String>();
			List<String> chatRoomChannelIdList = new ArrayList<String>();
			
			List<Channel> msgChannelList = ChannelManager.getInstance().getAllMessageChannel();
			List<Channel> modChannelList = ChannelManager.getInstance().getAllModChannel();
			
			if(modChannelList!=null)
			{
				for(Channel channel :modChannelList)
				{
					if(channel!=null)
					{
						if(CokChannelDef.isInChatRoom(channel.getChannelType()) && !chatRoomChannelIdList.contains(channel.getChannelID()))
						{
							chatRoomChannelIdList.add(channel.getChannelID());
							chatroomChannelList.add(channel);
						}
						else if(!CokConfig.getInstance().isAllAllianceMailChannel(channel) && CokChannelDef.isInUserMail(channel.getChannelType())
								&& !channel.getChannelID().equals(MailManager.CHANNELID_MOD) && !channel.getChannelID().equals(MailManager.CHANNELID_MESSAGE) && !friendChannelIdList.contains(channel.getChannelID()))
						{
							friendChannelIdList.add(channel.getChannelID());
							friendList.add(channel);
						}
					}
				}
			}
			
			if(msgChannelList!=null)
			{
				for(Channel channel :msgChannelList)
				{
					if(channel!=null)
					{
						if(CokChannelDef.isInChatRoom(channel.getChannelType()) && !chatRoomChannelIdList.contains(channel.getChannelID()))
						{
							chatRoomChannelIdList.add(channel.getChannelID());
							chatroomChannelList.add(channel);
						}
						else if(!CokConfig.getInstance().isAllAllianceMailChannel(channel) && CokChannelDef.isInUserMail(channel.getChannelType()) 
								&& !channel.getChannelID().equals(MailManager.CHANNELID_MOD) && !channel.getChannelID().equals(MailManager.CHANNELID_MESSAGE) && !friendChannelIdList.contains(channel.getChannelID()))
						{
							friendChannelIdList.add(channel.getChannelID());
							friendList.add(channel);
						}
					}
				}
			}
			
			Collections.sort(friendList);
			Collections.sort(chatroomChannelList);
			channelMap.put(0, friendList);
			channelMap.put(1, chatroomChannelList);
			customChannelListAdapter = new CustomExpandableListAdapter(activity, channelMap);
			custom_expand_listview.setAdapter(customChannelListAdapter);
			custom_expand_listview.expandGroup(0);
			custom_expand_listview.expandGroup(1);
		}
	}

	private void onRecordPanelShown(boolean b)
	{
        if(isHornUI)
        {
//        	horn_tip_layout.setVisibility(b ? View.GONE : View.VISIBLE);
        }else{
//        	imageView1.setVisibility(b ? View.GONE : View.VISIBLE);
        }
	}

	private LinearLayout sendMessageLayout;
	private LinearLayout voice_rec_button_layout;
	private Button voice_rec_button;
	
    private RecordCircle recordCircle;
    private RecordDot recordDot;
    private LinearLayout slideLayout;
    private TextView recordTimeText;
    private FrameLayout recordPanel;
	private FrameLayout	inputFrameLayout;
	private FrameLayout	popFrameLayout;
	private SizeNotifierFrameLayout sizeNotifierFrameLayout;
	private boolean hornTextHidden = false; 

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		System.out.println("chatfragmentnew onViewCreated");
		super.onViewCreated(view, savedInstanceState);
		if(!CokChannelDef.isInChat())
			refreshMemberSelectBtn();
		this.noAllianceFrameLayout = (FrameLayout) view.findViewById(R.id.hs__noAllianceLayout);
		this.relativeLayout1 = (LinearLayout) view.findViewById(R.id.relativeLayout1);
		this.buttonsLinearLayout = (LinearLayout) view.findViewById(R.id.buttonsLinearLayout);
		this.messagesListFrameLayout = (RelativeLayout) view.findViewById(R.id.hs__messagesListLayout);
		messagesListLayout = (RelativeLayout) view.findViewById(R.id.messagesListLayout);
		ImageUtil.setYRepeatingBG(activity, messagesListLayout, R.drawable.mail_list_bg);
		
		
		imageView1 = (ImageView) view.findViewById(R.id.imageView1);
		imageView2 = (ImageView) view.findViewById(R.id.imageView2);
		horn_checkbox = (CheckBox) view.findViewById(R.id.horn_checkbox);
//		horn_tip_layout = (LinearLayout) view.findViewById(R.id.horn_tip_layout);
//		horn_text_tip = (TextView) view.findViewById(R.id.horn_text_tip);
//		horn_text_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_HORN_TEXT));
		horn_scroll_text = (ScrollText) view.findViewById(R.id.horn_scroll_text);
		horn_scroll_text.setChannelType(CokConfig.getHornChannelType());
		horn_name = (TextView) view.findViewById(R.id.horn_name);
		horn_scroll_layout = (RelativeLayout) view.findViewById(R.id.horn_scroll_layout);
		horn_scroll_layout.setVisibility(View.GONE);
		horn_close_btn = (ImageView) view.findViewById(R.id.horn_close_btn);
		
		hs__dragon_chat_tip_layout = (LinearLayout) view.findViewById(R.id.hs__dragon_chat_tip_layout);
		hs__dragon_chat_tip_layout.setVisibility(View.GONE);
		dragon_chat_tip_text = (TextView) view.findViewById(R.id.dragon_chat_tip_text);
		dragon_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_DRAGON_CHAT));
		
		custom_chat_tip_layout = (RelativeLayout) view.findViewById(R.id.custom_chat_tip_layout); 
		addCustomChatBtn = (TextView) view.findViewById(R.id.addCustomChatBtn);
		custom_chat_tip_text = (TextView) view.findViewById(R.id.custom_chat_tip_text);
		custom_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD_TIP));
		
		battle_field_tip_layout = (RelativeLayout) view.findViewById(R.id.battle_field_tip_layout); 
		battle_field_btn = (TextView) view.findViewById(R.id.battle_field_btn);
		battle_field_tip_text = (TextView) view.findViewById(R.id.battle_field_tip_text);
		battle_field_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_BATTLE_FIELD_ADD_TIP));
		battle_field_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_GO_TO_SEE));
		battle_field_btn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LogUtil.trackAction("click_to_battle_from_chat");
				GSController.doHostAction("showBattleActivity", "", "", "", false);
			}
		});
		
		addCustomChatBtn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD));
		
		addCustomChatBtn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				showCustomChannelSetting();
				LogUtil.trackAction("click_add_custom_btn");
			}
		});
		
		custom_setting_layout = (LinearLayout) view.findViewById(R.id.custom_setting_layout);
		add_title = (TextView) view.findViewById(R.id.add_title);
		add_title.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD));
		add_tip = (TextView) view.findViewById(R.id.add_tip);
		if(StringUtils.isNotEmpty(customChannelId))
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP2));
		else
			add_tip.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_ADD_USER_TIP));
		customChannelHeadImage = (RoundImageView) view.findViewById(R.id.headImage);
		customChannelHeadImage.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				refreshCustomChannelImage(null);
				if(customChannelListAdapter!=null)
					customChannelListAdapter.notifyDataSetChanged();
			}
		});
		
		custom_setting_layout.setVisibility(View.GONE);
		customChannelName = (TextView) view.findViewById(R.id.name);
		custom_mod_image = (ImageView) view.findViewById(R.id.custom_mod_image);
		
		custom_head_layout = (FrameLayout) view.findViewById(R.id.member_head_layout);
		custom_expand_listview = (ExpandableListView) view.findViewById(R.id.custom_expand_listview);
		custom_expand_listview.setGroupIndicator(null);
		
		headImageContainer = (FixedAspectRatioFrameLayout) view.findViewById(R.id.headImageContainer);

		inputFrameLayout = (FrameLayout) view.findViewById(R.id.inputFrameLayout);
        recordPanel = new FrameLayoutFixed(activity);
        recordPanel.setVisibility(View.GONE);
        recordPanel.setBackgroundColor(COLOR_RECORD_BACK);
        inputFrameLayout.addView(recordPanel, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.BOTTOM));

        slideLayout = new LinearLayout(activity);
        slideLayout.setOrientation(LinearLayout.HORIZONTAL);
        recordPanel.addView(slideLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 30, 0, 0, 0));

        ImageView slideArrowImageView = new ImageView(activity);
        slideArrowImageView.setImageResource(R.drawable.voice_slidearrow);
        slideLayout.addView(slideArrowImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 1, 0, 0));

        TextView slideTextView = new TextView(activity);
        slideTextView.setText(LanguageManager.getLangByKey(LanguageKeys.AUDIO_SLIDE_TO_CANCEL));
        slideTextView.setTextColor(COLOR_RECORD_SLIDE_TEXT);
        slideTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		ScaleUtil.adjustTextSize(slideTextView, getAudioUIScale());
        slideLayout.addView(slideTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));

        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(AndroidUtilities.dp(13), 0, 0, 0);
        linearLayout.setBackgroundColor(COLOR_RECORD_DOT_BACK);
        recordPanel.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        recordDot = new RecordDot(activity);
        linearLayout.addView(recordDot, LayoutHelper.createLinear(11, 11, Gravity.CENTER_VERTICAL, 0, 1, 0, 0));

        recordTimeText = new TextView(activity);
        recordTimeText.setText("00:00");
        recordTimeText.setTextColor(COLOR_RECORD_TIME_TEXT);
        recordTimeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		ScaleUtil.adjustTextSize(recordTimeText, getAudioUIScale());
		
        linearLayout.addView(recordTimeText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 6, 0, 0, 0));
        
		sendMessageLayout = (LinearLayout) view.findViewById(R.id.hs__sendMessageLayout);
		voice_rec_button_layout = (LinearLayout) view.findViewById(R.id.voice_rec_button_layout);
		voice_rec_button = (Button) view.findViewById(R.id.voice_rec_button);
		
		custom_channel_setting_layout = (RelativeLayout) view.findViewById(R.id.custom_channel_setting_layout);
		custom_channel_name = (TextView) view.findViewById(R.id.custom_channel_name);
		custom_channel_settting_btn = (ImageView) view.findViewById(R.id.custom_channel_settting_btn);
		
		custom_channel_settting_btn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				showCustomChannelSetting();
			}
		});
		
		country_exchange_layout = (LinearLayout) view.findViewById(R.id.country_exchange_layout);
		country_channel_name = (TextView) view.findViewById(R.id.country_channel_name);
		country_exchange_btn = (ImageView) view.findViewById(R.id.country_exchange_btn);
		
		country_exchange_btn.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if(currentTab == TAB_COUNTRY)
					showTab(TAB_BATTLE_FIELD);
				else if(currentTab == TAB_BATTLE_FIELD)
					showTab(TAB_COUNTRY);
			}
		});
		
		custom_settting_btn_layout = (LinearLayout) view.findViewById(R.id.custom_settting_btn_layout);
		custom_setting_confim = (TextView) view.findViewById(R.id.custom_setting_confim);
		custom_setting_confim.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CONFIRM));
		custom_setting_confim.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				
				LocalConfig config = ConfigManager.getInstance().getLocalConfig();
				if(config == null)
				{
					config = new LocalConfig();
					ConfigManager.getInstance().setLocalConfig(config);
				}
				if(showingCustomChannel == null && (config.getCustomChannelType()!=-1 || StringUtils.isNotEmpty(config.getCustomChannelId())))
				{
					config.setCustomChannelType(-1);
					config.setCustomChannelId("");
					if(customChannelType != -1)
					{
						LogUtil.trackAction("custom_channel_removed");
						CokChannelDef.postLatestCustomChatMessage(null);
						customChannelChange = true;
					}
					refreshCustomChatChannel();
				}
				else if(showingCustomChannel != null && (config.getCustomChannelType()!=showingCustomChannel.getChannelType() || !config.getCustomChannelId().equals(showingCustomChannel.getChannelID())))
				{
					config.setCustomChannelType(showingCustomChannel.getChannelType());
					config.setCustomChannelId(showingCustomChannel.getChannelID());
					Channel channel = ChannelManager.getInstance().getChannel(showingCustomChannel.getChannelType(),showingCustomChannel.getChannelID());
					if(customChannelType == -1 && showingCustomChannel.getChannelType()!=-1)
					{
						LogUtil.trackAction("custom_channel_added");
						customChannelChange = true;
						if(channel!=null)
							CokChannelDef.sendChatLatestMessage(channel);
					}
					else if(customChannelType != -1 && showingCustomChannel.getChannelType()!=-1 && (customChannelType!=showingCustomChannel.getChannelType() || (!customChannelId.equals(showingCustomChannel.getChannelID()))))
					{
						LogUtil.trackAction("custom_channel_changed");
						customChannelChange = true;
						if(channel!=null)
							CokChannelDef.sendChatLatestMessage(channel);
					}
					refreshCustomChatChannel();
				}
				
				hideCustomChannelSetting();
				
				custom_chat_tip_layout.setVisibility(StringUtils.isEmpty(customChannelId) ? View.VISIBLE : View.GONE);
			}
		});

		recordCircle = new RecordCircle(activity);
		recordCircle.setVisibility(View.GONE);
		popFrameLayout = (FrameLayout) view.findViewById(R.id.popFrameLayout);
//		sizeNotifierFrameLayout = new SizeNotifierFrameLayout(activity);
//		ColorDrawable back = new ColorDrawable(0x000000);
//		back.setAlpha(0);
//		sizeNotifierFrameLayout.setBackgroundImage(back);
//		popFrameLayout.addView(sizeNotifierFrameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//		sizeNotifierFrameLayout.addView(recordCircle, LayoutHelper.createFrame(124, 124, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, -36, -38));
		popFrameLayout.addView(recordCircle, LayoutHelper.createFrame((int) (124 * getAudioUIScale()), (int) (124 * getAudioUIScale()), Gravity.BOTTOM | Gravity.RIGHT, 0, 0, (int) (-36 * getAudioUIScale()), (int) (-38 * getAudioUIScale())));
		
//		fragmentLayout.addView(recordCircle, LayoutHelper.createFrame(124, 124, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, -36, -38));
		
		prepareCustomChannelData();
		
		if((friendList!=null && friendList.size()>0) || (chatroomChannelList!=null && chatroomChannelList.size()>0))
		{
			addCustomChatBtn.setVisibility(View.VISIBLE);
			custom_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT_ADD_TIP));
		}
		else
		{
			addCustomChatBtn.setVisibility(View.GONE);
			custom_chat_tip_text.setText(LanguageManager.getLangByKey(LanguageKeys.CUSTOM_CHAT_ADD_TIP));
		}
		
		if(channelViews != null && channelViews.size() >= TAB_CUSTOM+1)
		{
			ChannelView channelView  = channelViews.get(TAB_CUSTOM);
			if(channelView != null)
			{
				refreshCustomChannelName(channelView.channel);
			}
		}
		
		
		
		voice_rec_button_layout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                	
                	if (!PermissionManager.checkXMRecordPermission(UIManager.getCurrentActivity()))
					{
						return false;
					}
                	
                	recordBtnUp = false;
                	
                	Timer timer = new Timer();
                	TimerTask timerTask = new TimerTask()
					{
						
						@Override
						public void run()
						{
		                    System.out.println("voice_rec_button_layout onTouch ACTION_DOWN");
		                    if(activity!=null)
		                    {
		                    	activity.runOnUiThread(new Runnable()
								{
									
									@Override
									public void run()
									{
										if(!recordBtnUp)
										{
											startedDraggingX = -1;
						                    gotoLastLine();
						                    XiaoMiToolManager.getInstance().startRecord();
						                    startRecordTimer();

						                    //  录音开始的回调中的处理
						                    if (!recordingAudio) {
						                        recordingAudio = true;
						                        updateAudioRecordIntefrace();
						                    }
						                    
						                    updateAudioRecordIntefrace();
						                    voice_rec_button_layout.getParent().requestDisallowInterceptTouchEvent(true);
										}
									}
								});
		                    }
		                    
						}
					};
					timer.schedule(timerTask, 300);
					
                } else if ((motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL)) {
                	recordBtnUp = true;
                	startedDraggingX = -1;
                	if(recordingAudio)
                	{
                		XiaoMiToolManager.getInstance().stopRecord(recordingAudio);
                    	stopRecordTimer();
                	}
                    exitRecordingUI();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && recordingAudio) {
                    float x = motionEvent.getX();
                    if (x < -distCanMove) {
                        if(recordingAudio){
                			LogUtil.trackPageView("Audio-cancelRecord");
                        	XiaoMiToolManager.getInstance().stopRecord(false);
                        	stopRecordTimer();
                        }
                        
                        exitRecordingUI();
                    }

                    x = x + ViewProxy.getX(voice_rec_button_layout);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
                    if (startedDraggingX != -1) {
                        float dist = (x - startedDraggingX);
                        ViewProxy.setTranslationX(recordCircle, dist);
                        params.leftMargin = AndroidUtilities.dp(30) + (int) dist;
                        slideLayout.setLayoutParams(params);
                        float alpha = 1.0f + dist / distCanMove;
                        if (alpha > 1) {
                            alpha = 1;
                        } else if (alpha < 0) {
                            alpha = 0;
                        }
                        ViewProxy.setAlpha(slideLayout, alpha);
                    }
                    if (x <= ViewProxy.getX(slideLayout) + slideLayout.getWidth() + AndroidUtilities.dp(30)) {
                        if (startedDraggingX == -1) {
                            startedDraggingX = x;
                            distCanMove = (recordPanel.getMeasuredWidth() - slideLayout.getMeasuredWidth() - AndroidUtilities.dp(48)) / 2.0f;
                            if (distCanMove <= 0) {
                                distCanMove = AndroidUtilities.dp(80);
                            } else if (distCanMove > AndroidUtilities.dp(80)) {
                                distCanMove = AndroidUtilities.dp(80);
                            }
                        }
                    }
                    if (params.leftMargin > AndroidUtilities.dp(30)) {
                        params.leftMargin = AndroidUtilities.dp(30);
                        ViewProxy.setTranslationX(recordCircle, 0);
                        slideLayout.setLayoutParams(params);
                        ViewProxy.setAlpha(slideLayout, 1);
                        startedDraggingX = -1;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
        });
//		initInputLayout();

		if (!lazyLoading)
			renderList();

		this.replyField = ((EditText) view.findViewById(ResUtil.getId(this.activity, "id", "hs__messageText")));
		this.wordCount = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "wordCountTextView")));
		if (CokChannelDef.isInMailDialog())
		{
			String title = UserManager.getInstance().getCurrentMail().opponentName;
			if (CokChannelDef.isInUserMail())
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(UserManager.getInstance().getCurrentMail().opponentUid);
				if (StringUtils.isNotEmpty(fromUid))
				{
					if (fromUid.equals(UserManager.getInstance().getCurrentUser().uid))
					{
						title = LanguageManager.getLangByKey(LanguageKeys.TIP_ALLIANCE);
					}
					else
					{
						User fromUser = UserManager.getInstance().getUser(fromUid);
						if (fromUser != null && StringUtils.isNotEmpty(fromUser.userName))
						{
							title = fromUser.userName;
						}
					}
				}

			}

			if (CokChannelDef.isInChatRoom() && title.length() > 16)
			{
				title = LanguageManager.getLangByKey(LanguageKeys.TITLE_CHATROOM);
				if (title.equals(""))
					title = "Group";
			}
			if (UserManager.getInstance().getCurrentMail().opponentUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
				title += "(MOD)";
			getTitleLabel().setText(title);
		}

		if (CokChannelDef.isChatRestrictForLevel(GSController.getCurrentChannelType()))
		{
			replyField.setEnabled(false);
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
					"" + CokChannelDef.getChatRestrictLevel()));
		}
		else
		{
			replyField.setEnabled(true);
			replyField.setHint("");
			if (GSController.needShowAllianceDialog)
				replyField.setText(LanguageManager.getLangByKey(LanguageKeys.INPUT_ALLIANCE_DIALOG));
			else
			{
				Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
				if(channel!=null && StringUtils.isNotEmpty(channel.getDraft()))
				{
					replyField.setText(channel.getDraft());
					replyField.setSelection(channel.getDraft().length());
				}
				else
					replyField.setText("");
			}
		}

		addReply = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "hs__sendMessageBtn"));

		getMemberSelectButton().setVisibility(isSelectMemberBtnEnable && !CokChannelDef.isInBasicChat() ? View.VISIBLE : View.GONE);

		buttonCountry = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonCountry"));
		buttonAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonAllie"));
		buttonCustom = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "buttonFriend"));
		
//		if (GSController.isInCrossFightServer())
//		{
//			buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD));
//		}
//		else
//		{
			buttonCountry.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_COUNTRY));
//		}
		buttonAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_ALLIANCE));
		buttonCustom.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CUSTOM_CHAT));
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, true);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, false);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCustom, false);

		tooltipLayout = ((LinearLayout) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipLayout")));
		tooltipLabel = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipLabel")));
		tooltipArrow = ((ImageView) view.findViewById(ResUtil.getId(this.activity, "id", "tooltipArrow")));
		showToolTip(false);
		tooltipLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickToolTip();
			}
		});

		channelButton = new ArrayList<Button>();
		channelButton.add(buttonCountry);
		channelButton.add(buttonAlliance);
		channelButton.add(buttonCustom);

		buttonJoinAlliance = (Button) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceBtn"));
		buttonJoinAlliance.setText(LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));

		buttonJoinAlliance.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				GSController.doHostAction("joinAllianceBtnClick", "", "", "", true);
			}
		});

		horn_close_btn.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				hideHornScrollText();
				hornTextHidden = true;
			}
		});

		noAllianceTipText = ((TextView) view.findViewById(ResUtil.getId(this.activity, "id", "joinAllianceTipText")));
		noAllianceTipText.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE));

		refreshSendButton();

		for (int i = 0; i < channelButton.size(); i++)
		{
			channelButton.get(i).setTag(""+i);
			channelButton.get(i).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					try
					{
						if (view.getTag() == null || channelViews == null || channelViews.size() <= 0)
							return;
						int index = Integer.parseInt(view.getTag().toString());
						if(index == 0 && channelViews.size() == 4 && CokConfig.needCrossServerBattleChat())
							index = 3;
						if (index >= channelViews.size())
							return;
						ChannelView channelView = channelViews.get(index);
						if (channelView != null)
						{
							channelView.setLoadingStart(false);

							showTab(channelView.tab);
							
							if(channelView.tab == TAB_CUSTOM)
							{
								if(StringUtils.isNotEmpty(customChannelId))
								{
									LogUtil.trackAction("click_custom_channel_exist_true");
								}
								else
								{
									LogUtil.trackAction("click_custom_channel_exist_false");
								}
							}
							else
							{
								LogUtil.trackAction("click_chat_tab"+channelView.tab);
							}

							if (channelView.tab == TAB_COUNTRY)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_COUNTRY) });
							}
							else if (channelView.tab == TAB_ALLIANCE)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_ALLIANCE) });
							}
							else if (channelView.tab == TAB_CUSTOM)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT) });
							}
							else if (channelView.tab == TAB_BATTLE_FIELD)
							{
								JniController.getInstance().excuteJNIVoidMethod("postCurChannel",
										new Object[] { Integer.valueOf(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD) });
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}

		getMemberSelectButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				hideSoftKeyBoard();
				UIManager.showChatRoomSettingActivity(activity);
//				GSIM.showMemberSelectorActivity(activity, true);
			}
		});

		addReply.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onSendButtonClick();
			}
		});

		replyField.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == 4)
				{
					addReply.performClick();
				}
				return false;
			}
		});
		
		replyField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				EditText editText = (EditText) v;
				if(editText!=null)
				{
					String hint;
		             if (hasFocus) {
		                 hint = editText.getHint().toString();
		                 editText.setTag(hint);
		                 editText.setHint("");
		             } else {
		                 hint = editText.getTag().toString();
		                 editText.setHint(hint);
		             }
				}
			}
		});
		
		replyField.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (CokChannelDef.isChatRestrict())
				{
					MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
				}
			}
		});

		textChangedListener = new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				replyField.post(new Runnable()
				{
					@Override
					public void run()
					{
						refreshWordCount();
					}
				});
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				refreshSendButton();
			}
		};
		this.replyField.addTextChangedListener(textChangedListener);

		getShowFriendButton().setVisibility(CokChannelDef.isInMailDialog() ? View.GONE : View.VISIBLE);
		getShowFriendButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				GSController.doHostAction("showFriend", "", "", "", false);
			}
		});

		showMessageBox();

		horn_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				refreshBottomUI(isChecked);
				if (isChecked)
					ConfigManager.isHornBtnEnable = true;
				else
					ConfigManager.isHornBtnEnable = false;
			}
		});

		onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				checkFirstGlobalLayout();
				adjustHeight();
			}
		};
		messagesListFrameLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
		((ChatActivity) getActivity()).fragment = this;

		if (!lazyLoading)
		{
			refreshToolTip();
			refreshHasMoreData();
		}

		Msg msgItem = ScrollTextManager.getInstance().getNextText(GSController.getCurrentChannelType());
		if (msgItem != null)
			showHornScrollText(msgItem);
		else
		{
			horn_scroll_layout.setVisibility(View.GONE);
		}
		
		initSensorListener();
		
		onNetworkConnectionChanged();
	}
	
	private void onSendButtonClick()
	{
		final String message = replyField.getText().toString().trim();
		if (TextUtils.isEmpty(message))
			return;

		// if(replyText.endsWith("png"))
		// {
		// System.out.println("setCommonImage");
		// ImageUtil.setCommonImage(activity, replyText, imageView2);
		// }

		if (horn_checkbox.isChecked() && CokChannelDef.isInCountryTab())
		{
			int hornBanedTime = JniController.getInstance().excuteJNIMethod("getHornBanedTime", null);
			if (hornBanedTime == 0)
			{
				int price = JniController.getInstance().excuteJNIMethod("isHornEnough", null);
				String horn = LanguageManager.getLangByKey(LanguageKeys.TIP_HORN);
				if (price == 0)
				{
					if (ConfigManager.isFirstUserHorn)
					{
						MenuController.showSendHornMessageConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_USEITEM, horn),
								new CallBack()
								{
									public void onCallback()
									{
										sendMsg(message, true, false, null);
									};
								});
					}
					else
					{
						sendMsg(message, true, false, null);
					}
				}
				else if (price > 0)
				{
					if (ConfigManager.isFirstUserCornForHorn)
					{
						MenuController.showSendHornWithCornConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_ITEM_NOT_ENOUGH, horn),
								price, new CallBack()
								{
									public void onCallback()
									{
										sendMsg(message, true, true, null);
									};
								});
					}
					else
					{
						boolean isCornEnough = JniController.getInstance().excuteJNIMethod("isCornEnough",
								new Object[] { Integer.valueOf(price) });
						if (isCornEnough)
						{
							sendMsg(message, true, true, null);
						}
						else
						{
							MenuController.showCornNotEnoughConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CORN_NOT_ENOUGH));
						}
					}
				}
			}
		}
		else
		{
			sendMsg(message, false, false, null);
		}
	}
	
	private void sendMsg(String messageText, boolean isHornMsg, boolean usePoint, String audioUrl)
	{
		if (!checkSendRestrict())
			return;
		
		clearInput();

		getCurrentChannel().sendMsg(messageText, isHornMsg, usePoint, audioUrl);
	}
	
	private void afterAddDummyMsg()
	{
		// 发送后的行为（跳到最后一行），包含了notifyDataSetChanged
		afterSendMsgShowed();
	}

	private void onDataSetChanged(Event event)
	{
		if(event instanceof ChannelChangeEvent)
		{
			Channel channel = ((ChannelChangeEvent) event).channel;
			notifyDataSetChanged(channel.getChannelType(), channel.getChannelID(), false);
		}
	}

	// 重发消息
	public void resendMsg(Msg msgItem, boolean isHornMsg, boolean usePoint)
	{
		if (!checkSendRestrict())
			return;

		getCurrentChannel().resendMsg(msgItem, isHornMsg, usePoint);
	}

	// 重发消息
	public void resendAudioMsg(Msg msgItem)
	{
		if (!checkSendRestrict())
			return;

		getCurrentChannel().resendAudioMsg(msgItem);
	}
	
	private boolean checkSendRestrict()
	{
		if (GSController.getCurrentChannelType() < 0 || !GSController.isSendIntervalValid())
			return false;
		if (CokChannelDef.isChatRestrict())
		{
			MenuController.showChatRestrictConfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_CHAT_RESTRICT));
			return false;
		}
		if(getCurrentChannel() == null)
			return false;
		
		return true;
	}
	
	public void notifyCustomChannelDataSetChanged()
	{
		if(customChannelListAdapter!=null)
			customChannelListAdapter.notifyDataSetChanged();
	}

	public void showHornScrollText(Msg msgItem)
	{
		if (!(msgItem.isHornMessage() || msgItem.isStealFailedMessage()))
			return;
		
		if(!CokChannelDef.isInCountryTab())
		{
			showHornScrollLayout(msgItem, false);
		}
		else
		{
			if(CokChannelDef.isInCountryTab(msgItem.channelType))
			{
				hornTextHidden = false;
				showHornScrollLayout(msgItem, true);
			}
			else
			{
				if(msgItem.canEnterScrollTextQueue())
				{
					ScrollTextManager.getInstance().clear(msgItem.channelType);
					ScrollTextManager.getInstance().push(msgItem,msgItem.channelType);
				}
			}
		}
	}
	
	private void showHornScrollLayout(Msg msgItem, boolean visible)
	{
		if (horn_scroll_layout != null)
		{
			horn_scroll_layout.setVisibility(visible ? View.VISIBLE : View.GONE);
			if (visible)
			{
				ScrollTextManager.getInstance().showScrollText(msgItem, horn_scroll_text, horn_name, horn_scroll_layout,
						msgItem.channelType);
			}
		}
	}

	private void refreshCustomChannelName(Channel channel)
	{
		if(channel == null)
		{
			if(custom_channel_setting_layout.getVisibility()!=View.GONE)
				custom_channel_setting_layout.setVisibility(View.GONE);
		}
		else
		{
			if(custom_channel_setting_layout.getVisibility()!=View.VISIBLE)
				custom_channel_setting_layout.setVisibility(View.VISIBLE);
			
			if(CokChannelDef.isInUserMail(channel.getChannelType()))
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.getChannelID());
				if(StringUtils.isNotEmpty(fromUid) && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					User userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;
					if (userInfo != null)
						nameText = userInfo.userName;
					else
						nameText = channel.getCustomName();
					if(StringUtils.isNotEmpty(nameText) && channel.getChannelID().endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
						nameText+="(MOD)";
					custom_channel_name.setText(nameText);
				}
				else
				{
					if(StringUtils.isNotEmpty(channel.getCustomName()))
						custom_channel_name.setText(channel.getCustomName());
					else
						custom_channel_name.setText(channel.getChannelID());
				}
				
			}
			else if(CokChannelDef.isInChatRoom(channel.getChannelType()))
			{
				if(StringUtils.isNotEmpty(channel.getCustomName()))
					custom_channel_name.setText(channel.getCustomName());
				else
					custom_channel_name.setText(channel.getChannelID());
			}
		}
	}
	
	private void refreshSettingCustomChannelName(Channel channel)
	{
		custom_mod_image.setVisibility(View.GONE);
		if(channel == null)
		{
			if(customChannelName.getVisibility()!=View.INVISIBLE)
				customChannelName.setVisibility(View.INVISIBLE);
		}
		else
		{
			if(customChannelName.getVisibility()!=View.VISIBLE)
				customChannelName.setVisibility(View.VISIBLE);
			
			if(CokChannelDef.isInUserMail(channel.getChannelType()))
			{
				String fromUid = ChannelManager.getInstance().getModChannelFromUid(channel.getChannelID());
				if(StringUtils.isNotEmpty(fromUid) && StringUtils.isNumeric(fromUid))
				{
					UserManager.checkUser(fromUid, "", 0);
					User userInfo = UserManager.getInstance().getUser(fromUid);
					String nameText = fromUid;
					if (userInfo != null)
					{
						nameText = userInfo.userName;
						if(StringUtils.isNotEmpty(userInfo.asn))
							nameText = "("+userInfo.asn+")"+nameText;
					}
					else
						nameText = channel.getCustomName();
					if(StringUtils.isNotEmpty(nameText) && channel.getChannelID().endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
					{
						custom_mod_image.setVisibility(View.VISIBLE);
					}
					customChannelName.setText(nameText);
				}
				else
				{
					if(StringUtils.isNotEmpty(channel.getCustomName()))
						customChannelName.setText(channel.getCustomName());
					else
						customChannelName.setText(channel.getChannelID());
				}
			}
			else if(CokChannelDef.isInChatRoom(channel.getChannelType()))
			{
				if(StringUtils.isNotEmpty(channel.getCustomName()))
					customChannelName.setText(channel.getCustomName());
				else
					customChannelName.setText(channel.getChannelID());
			}
		}
	}
	
	public void hideHornScrollText()
	{
		if(CokChannelDef.isInCountryTab())
			ScrollTextManager.getInstance().shutDownScrollText(horn_scroll_text,GSController.getCurrentChannelType());
	}

	private boolean	lazyLoading	= true;

	protected void onBecomeVisible()
	{
		if (inited)
			return;
		timerDelay = 500;
		startTimer();
	}

	private int						currentChannelViewIndex;
	

	private void initChannelViews()
	{
		channelViews = new ArrayList<ChannelView>();
		if(CokChannelDef.isInChat())
		{
			LocalConfig config = ConfigManager.getInstance().getLocalConfig();
			if(config!=null)
			{
				customChannelType = config.getCustomChannelType();
				customChannelId = config.getCustomChannelId();
			}
			
			int channelViewCount = 3;
			if(CokConfig.needCrossServerBattleChat())
				channelViewCount = 4;
			
			for (int i = 0; i < channelViewCount; i++)
			{
				ChannelView channelView = new ChannelView();
				channelView.tab = i;
				Channel chatChannel = null;
				if(i == TAB_CUSTOM)
				{
					System.out.println("customChannelType:"+customChannelType + "  customChannelId:"+customChannelId);
					if((CokChannelDef.isInUserMail(customChannelType) || CokChannelDef.isInChatRoom(customChannelType)) && StringUtils.isNotEmpty(customChannelId))
						chatChannel = ChannelManager.getInstance().getChannel(customChannelType, customChannelId);
					channelView.channelType = DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
				}
				else if(i == 3)
				{
					chatChannel = CokConfig.getInstance().getBattleFieldChannel();
					channelView.channelType = DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
					channelView.tab = TAB_BATTLE_FIELD;
				}
				else
				{
					chatChannel = ChannelManager.getInstance().getChannel(i);
					channelView.channelType = i;
				}
				if(chatChannel!=null)
				{
					if(!chatChannel.hasInitLoaded())
						chatChannel.loadMoreMsg();
					System.out.println("i:"+i + "  chatChannel.channelId:"+chatChannel.getChannelID()+"  chatChannel.msgList:"+chatChannel.msgList.size());
					chatChannel.clearFirstNewMsg();
					chatChannel.setChannelView(channelView);
					if(i == TAB_CUSTOM)
					{
						int mailType = CokConfig.getInstance().isModChannel(chatChannel) ? MailManager.MAIL_MOD_PERSONAL : MailManager.MAIL_USER;
						GSController.setMailInfo(chatChannel.getChannelID(), chatChannel.latestId, chatChannel.getCustomName(), mailType);
					}
					
					IMCore.getInstance().addEventListener(ChannelChangeEvent.AFTER_ADD_DUMMY_MSG, this, new EventCallBack(){
						public void onCallback(Event event){
							afterAddDummyMsg();
						};
					});
					IMCore.getInstance().addEventListener(ChannelChangeEvent.DATASET_CHANGED, this, new EventCallBack(){
						public void onCallback(Event event){
							onDataSetChanged(event);
						};
					});
					IMCore.getInstance().addEventListener(ChannelChangeEvent.ON_RECIEVE_NEW_MSG, this, new EventCallBack(){
						public void onCallback(Event event){
							if(event instanceof ChannelChangeEvent)
							{
								onRecieveNewMsg((ChannelChangeEvent) event);
							}
						};
					});
					IMCore.getInstance().addEventListener(ChannelChangeEvent.ON_RECIEVE_OLD_MSG, this, new EventCallBack(){
						public void onCallback(Event event){
							if(event instanceof ChannelChangeEvent)
							{
								onRecieveOldMsg((ChannelChangeEvent) event);
							}
						};
					});
				}
				channelView.channel = chatChannel;
				channelViews.add(channelView);
			}
		}
		else if(CokChannelDef.isInMailDialog())
		{
			ChannelView channelView = new ChannelView();
			channelView.tab = TAB_MAIL;
			
			if(CokChannelDef.isInUserMail() || CokChannelDef.isInChatRoom())
			{
				int currentChannelType = GSController.getCurrentChannelType();
				Channel chatChannel = ChannelManager.getInstance().getChannel(currentChannelType);
				if(chatChannel!=null)
				{
					chatChannel.clearFirstNewMsg();
					chatChannel.setChannelView(channelView);
				}
				channelView.channelType = currentChannelType;
				channelView.channel = chatChannel;
			}
			channelViews.add(channelView);
		}
	}

	protected void onRecieveNewMsg(ChannelChangeEvent event)
	{
		onDataSetChanged(event);
		
		Channel c = event.channel;
		refreshIsInLastScreen(c.getChannelType(), c.getChannelID());

		for (int i = 0; i < event.chatInfoArr.length; i++)
		{
			Msg recievedMsg = event.chatInfoArr[i];
			if(recievedMsg.isHornMessage())
				showHornScrollText(recievedMsg);
			if(recievedMsg.isAudioMessage())
				updateAudioHint();
		}
		
		if (event.chatInfoArr.length == 1)
			smoothUpdateListPositionForNewMsg(c.getChannelType(), c.getChannelID(), event.hasSelfMsg);
		else
			updateListPositionForNewMsg(c.getChannelType(), c.getChannelID(), event.hasSelfMsg);
	}

	private static boolean	oldHornMsgPushed	= false;
	private static boolean	oldBattleHornMsgPushed	= false;
	protected void onRecieveOldMsg(ChannelChangeEvent event)
	{
		int loadCount = event.loadCount;
		Msg oldFirstItem = event.oldFirstItem;
		Msg[] chatInfoArr = event.chatInfoArr;
		Channel c = event.channel;
		
		if (loadCount > 0)
			c.getLoadedTimeNeedShowMsgIndex(loadCount);

		if (CokChannelDef.isInCountry(c.getChannelType()) && !oldHornMsgPushed)
		{
			if (c.msgList != null && c.msgList.size() > 0)
			{
				for (int i = 0; i < c.msgList.size(); i++)
				{
					Msg msgItem = c.msgList.get(i);
					if (msgItem != null && msgItem.canEnterScrollTextQueue())
					{
						ScrollTextManager.getInstance().clear(c.getChannelType());
						ScrollTextManager.getInstance().push(msgItem, c.getChannelType());
						oldHornMsgPushed = true;
					}
				}
			}
		}
		else if (CokChannelDef.isInBattleField(c.getChannelType()) && !oldBattleHornMsgPushed)
		{
			if (c.msgList != null && c.msgList.size() > 0)
			{
				for (int i = 0; i < c.msgList.size(); i++)
				{
					Msg msgItem = c.msgList.get(i);
					if (msgItem != null && msgItem.canEnterScrollTextQueue()){
						ScrollTextManager.getInstance().clear(c.getChannelType());
						ScrollTextManager.getInstance().push(msgItem, c.getChannelType());
						oldBattleHornMsgPushed = true;
					}
				}
			}
		}

		onDataSetChanged(event);
		refreshIsInLastScreen(c.getChannelType(), c.getChannelID());
		updateListPositionForOldMsg(c.getChannelType(), c.getChannelID(), loadCount, !MsgUtil.isDifferentDate(oldFirstItem, c.msgList));
		resetMoreDataStart(c.getChannelType(), c.getChannelID());
	}

	public ChannelView getChannelView(int index)
	{
		return channelViews.get(index);
	}

	private void setChannelViewIndex(int i)
	{
		if (i >= 0 && i < channelViews.size())
		{
			currentChannelViewIndex = i;
		}
	}

	private ChannelView getCountryChannelView()
	{
		try
		{
			if(CokChannelDef.isInCountry())
				return channelViews.get(0);
			else
				return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private void showCustomChannelSetting()
	{
		custom_setting_layout.setVisibility(View.VISIBLE);
		activity.hideSoftKeyBoard();
		replyField.clearFocus();
		isSettingCustomChannel = true;
		relativeLayout1.setVisibility(View.GONE);
		custom_settting_btn_layout.setVisibility(View.VISIBLE);
		if(channelViews!=null && channelViews.size() >= TAB_CUSTOM+1)
		{
			ChannelView channelView = channelViews.get(TAB_CUSTOM);
			if(channelView!=null)
			{
				refreshCustomChannelImage(channelView.channel);
				if(customChannelListAdapter!=null)
					customChannelListAdapter.notifyDataSetWithSort();
			}
		}
	}
	
	public void hideCustomChannelSetting()
	{
		custom_setting_layout.setVisibility(View.GONE);
		isSettingCustomChannel = false;
		if(StringUtils.isEmpty(customChannelId))
			relativeLayout1.setVisibility(View.GONE);
		else
			relativeLayout1.setVisibility(View.VISIBLE);
		custom_settting_btn_layout.setVisibility(View.GONE);
		custom_channel_setting_layout.setVisibility(StringUtils.isNotEmpty(customChannelId)? View.VISIBLE : View.GONE);
		
		horn_scroll_layout.setVisibility(View.GONE);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void renderList()
	{
		if(channelViews == null)
			return;
		for(int i=0;i<channelViews.size();i++)
		{
			final ChannelView channelView = channelViews.get(i);
			if(channelView == null)
				continue;
			
			PullDownToLoadMoreView pullDownToLoadListView = new PullDownToLoadMoreView(activity);
			pullDownToLoadListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			pullDownToLoadListView.setTopViewInitialize(true);
			pullDownToLoadListView.setAllowPullDownRefersh(false);
			pullDownToLoadListView.setBottomViewWithoutScroll(false);
			pullDownToLoadListView.setListViewLoadListener(mListViewLoadListener);
			pullDownToLoadListView.setListViewTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					onContentAreaTouched();
					return false;
				}
			});

			ListView messagesListView = new ListView(activity);
			messagesListView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			messagesListView.setVerticalFadingEdgeEnabled(false);
			messagesListView.setCacheColorHint(Color.TRANSPARENT);
			messagesListView.setDivider(null);

			List<Msg> msgList = null;
			if(channelView.channel != null)
				msgList = channelView.channel.msgList;
			if(msgList == null)
				msgList = new ArrayList<Msg>();
			
			if (msgList != null)
			{
				MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
				channelView.setMessagesAdapter(adapter);
			}
			if((CokChannelDef.isInChat() && i!=0) || !CokChannelDef.isInChat())
				XiaoMiToolManager.getInstance().addAudioListener(channelView.getMessagesAdapter());
			messagesListView.setAdapter(channelView.getMessagesAdapter());

			messagesListView.setOnScrollListener(mOnScrollListener);
			messagesListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
			messagesListView.setKeepScreenOn(true);

			pullDownToLoadListView.addView(messagesListView);

			channelView.pullDownToLoadListView = pullDownToLoadListView;
			channelView.messagesListView = messagesListView;

			if (channelView.channel!=null && channelView.channel.lastPosition.x != -1 && rememberPosition)
			{
				channelView.messagesListView.setSelectionFromTop(channelView.channel.lastPosition.x, channelView.channel.lastPosition.y);
			}
			else
			{
				if(channelView.getMessagesAdapter()!=null)
					channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
			}
			messagesListFrameLayout.addView(pullDownToLoadListView);
		}
		if (lazyLoading)
		{
			System.out.println("lazyLoading refreshTab");
			refreshTab();
		}
		activity.hideProgressBar();
	}

	protected void refreshTab()
	{
		if (CokChannelDef.isInAlliance())
		{
			showTab(TAB_ALLIANCE);
		}
		else if (CokChannelDef.isInCountryTab())
		{
			if(channelViews.size() == 4 && CokConfig.needCrossServerBattleChat())
				showTab(TAB_BATTLE_FIELD);
			else
				showTab(TAB_COUNTRY);
		}
		else if (CokChannelDef.isInCustomChat())
		{
			showTab(TAB_CUSTOM);
		}
		else if (CokChannelDef.isInMailDialog())
		{
			showTab(TAB_MAIL);
		}
		refreshWordCount();
	}
	
	protected void onNetworkConnectionChanged()
	{
		if(voice_rec_button_layout.getVisibility() == View.VISIBLE)
		{
			// 在切换tab后，可能会被重刷成alpha为1；在连接恢复后，可能因为不可见而没有设为enable；还需要增加调用
			refreshAudioButton();
		}
		else
		{
			refreshSendButton();
		}
	}
	
	private boolean isWSConnectionAvailable()
	{
		boolean result = true;
		Channel channel = null;
		if(CokChannelDef.isInCustomChat())
			channel = ChannelManager.getInstance().getChannel(customChannelType,customChannelId);
		else
			channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if (channel == null)
		{
			result = false;
		}
		else //if (WebSocketManager.isRecieveFromWebSocket(channel.getChannelType()) || WebSocketManager.isSendFromWebSocket(channel.getChannelType()))
		{
			result = ConfigManager.isWebSocketNetWorkNormal();
		}
		return result;
	}

	protected void refreshAudioButton()
	{
		if (!isWSConnectionAvailable())
		{
			voice_rec_button_layout.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(voice_rec_button_layout, false);
		}
		else
		{
			voice_rec_button_layout.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(voice_rec_button_layout, true);
		}
	}

	protected void refreshSendButton()
	{
		if (this.replyField.getText().length() == 0 || !isWSConnectionAvailable())
		{
			addReply.setEnabled(false);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, false);
		}
		else
		{
			addReply.setEnabled(true);
			CompatibleApiUtil.getInstance().setButtonAlpha(addReply, true);
		}
	}

	public void showToolTip(boolean b)
	{
		tooltipLayout.setVisibility(b ? View.VISIBLE : View.GONE);
	}

	private void onClickToolTip()
	{
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if (channel == null) return;
		
		if (false)//!WebSocketManager.isRecieveFromWebSocket(channel.getChannelType()))
		{
//			if (channel.canLoadAllNew())
//			{
//				getCurrentChannel().setLoadingStart(true);
//				loadMoreCount = 0;
//				channel.isLoadingAllNew = true;
//				channel.hasLoadingAllNew = true;
//				ChannelManager.getInstance().loadAllNew(channel);
//
//				refreshToolTip();
//			}
		}
		else
		{
			if (channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
			{
				channel.wsNewMsgCount = 0;
				updateListPositionForOldMsg(channel.getChannelType(),channel.getChannelID(), 0, false);
			}
		}
	}

	public void refreshToolTip()
	{
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		// 未加入联盟时，channel不存在
		if (channel == null || isInMail())
		{
			showToolTip(false);
			return;
		}

		if (false)//!WebSocketManager.isRecieveFromWebSocket(channel.getChannelType()))
		{
//			refreshToolTipInGameServer(channel);
		}
		else
		{
			refreshToolTipInWSServer(channel);
		}
	}

//	private void refreshToolTipInGameServer(Channel channel)
//	{
//		if (channel != null && channel.canLoadAllNew())
//		{
//			String newMsgCount = channel.getNewMsgCount() < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.getNewMsgCount() + ""
//					: ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
//			tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
//			showToolTip(true);
//		}
//		else
//		{
//			showToolTip(false);
//		}
//	}

	private void refreshToolTipInWSServer(Channel channel)
	{
		// 第一次加载历史消息后，重置channel.wsNewMsgCount
		// TODO 应该改为显示到第一条消息后重置
		// if(channel.wsNewMsgCount > 0 && channel.msgList.size() !=
		// channel.wsNewMsgCount)
		// {
		// channel.wsNewMsgCount = 0;
		// }

		if (channel != null && channel.wsNewMsgCount > ChannelManager.LOAD_ALL_MORE_MIN_COUNT)
		{
			String newMsgCount = channel.wsNewMsgCount < ChannelManager.LOAD_ALL_MORE_MAX_COUNT ? channel.wsNewMsgCount + ""
					: ChannelManager.LOAD_ALL_MORE_MAX_COUNT + "+";
			tooltipLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NEW_MESSAGE_ALERT, newMsgCount));
			showToolTip(true);
		}
		else
		{
			showToolTip(false);
		}
	}

	public void clearInput()
	{
		replyField.setText("");
	}

	private boolean isHornUI;
	private void refreshBottomUI(boolean isChecked)
	{
		if (!isChecked)
			GSController.isHornItemUsed = false;
		isHornUI = isChecked && CokChannelDef.isInCountryTab() && ConfigManager.enableChatHorn;
		String background = "btn_chat_send";
		String bottomBg = "chuzheng_frame02";
		String lineBg = "line_grey02";
		if(isHornUI)
		{
			background = "btn_chat_send_horn";
			bottomBg = "bottom_bg";
			lineBg = "line_brown";
		}
		else
		{
			if(CokChannelDef.isInBattleField())
			{
				background = "btn_battle_chat_send";
				bottomBg = "battle_bottom_bg";
				lineBg = "line_battle_bottom";
			}
		}
		
		String inputBg = isHornUI ? "text_field_horn" : "text_field_bg2";
		addReply.setBackgroundResource(ResUtil.getId(activity, "drawable", background));
		if(isHornUI){
			relativeLayout1.setBackgroundColor(0xffffcb64);
		}else{
			relativeLayout1.setBackgroundResource(ResUtil.getId(activity, "drawable", bottomBg));
		}
		replyField.setBackgroundResource(ResUtil.getId(activity, "drawable", inputBg));
//		horn_tip_layout.setVisibility(isHornUI ? View.VISIBLE : View.GONE);
//		imageView1.setVisibility(isHornUI ? View.GONE : View.VISIBLE);
		imageView1.setImageResource(ResUtil.getId(activity, "drawable", lineBg));
		setMaxInputLength(isHornUI);
		resetInputButton(false);
	}

	private void setMaxInputLength(boolean isHornUI)
	{
		curMaxInputLength = isHornUI && ConfigManager.maxHornInputLength > 0 ? ConfigManager.maxHornInputLength : 500;
		replyField.setFilters(new InputFilter[] { new InputFilter.LengthFilter(curMaxInputLength) });
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void showJoinAlliancePopup()
	{
		final AlertDialog dlg = new AlertDialog.Builder(activity).create();
		dlg.setCancelable(true);
		dlg.setCanceledOnTouchOutside(true);
		dlg.show();
		isJoinAlliancePopupShowing = true;
		Window window = dlg.getWindow();
		window.setBackgroundDrawable(new ColorDrawable());
		window.setContentView(R.layout.cs__first_alliance_popup);
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		TextView first_join_title = (TextView) window.findViewById(R.id.first_join_title);
		first_join_title.setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_JOIN_ALLIANCE));

		TextView first_join_title_tip = (TextView) window.findViewById(R.id.first_join_title_tip);
		first_join_title_tip.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_JOIN_ALLIANCE_TITLE, "200"));

		TextView first_join_alliance_text1 = (TextView) window.findViewById(R.id.first_join_alliance_text1);
		first_join_alliance_text1.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_1));

		TextView first_join_alliance_text2 = (TextView) window.findViewById(R.id.first_join_alliance_text2);
		first_join_alliance_text2.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_2));

		TextView first_join_alliance_text3 = (TextView) window.findViewById(R.id.first_join_alliance_text3);
		first_join_alliance_text3.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_3));

		TextView first_join_alliance_text4 = (TextView) window.findViewById(R.id.first_join_alliance_text4);
		first_join_alliance_text4.setText(LanguageManager.getLangByKey(LanguageKeys.TIP_FIRST_JOIN_ALLIANCE_4));

		RelativeLayout firstJoinAllianceLayout = (RelativeLayout) window.findViewById(R.id.firstAllianceLayout);

		firstJoinAllianceLayout.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				dlg.cancel();
				isJoinAlliancePopupShowing = false;
				return false;
			}
		});

		Button joinAllianceBtn = (Button) window.findViewById(R.id.joinAllianceBtn);
		joinAllianceBtn.setText(LanguageManager.getLangByKey(LanguageKeys.MENU_JOIN));
		joinAllianceBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlg.cancel();
				isJoinAlliancePopupShowing = false;

				GSController.doHostAction("joinAllianceBtnClick", "", "", "", true);
			}
		});

		if (ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0)
		{
			ScaleUtil.adjustTextSize(joinAllianceBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_title_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text1, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text2, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text3, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(first_join_alliance_text4, ConfigManager.scaleRatio);
		}
	}

	public class LoadMoreMsgParam
	{
		public long		requestMinTime;
		public long		requestMaxTime;

		public int		requestMinSeqId;
		public int		requestMaxSeqId;

		public boolean	useTime;

		public boolean	fetchFromServer;

		public LoadMoreMsgParam(int minSeqId, int maxSeqId, boolean fetchFromServer)
		{
			useTime = false;
			this.requestMinSeqId = minSeqId;
			this.requestMaxSeqId = maxSeqId;
			this.fetchFromServer = fetchFromServer;
		}

		public LoadMoreMsgParam(long requestMinTime, long requestMaxTime, boolean fetchFromServer)
		{
			useTime = true;
			this.requestMinTime = requestMinTime;
			this.requestMaxTime = requestMaxTime;
			this.fetchFromServer = fetchFromServer;
		}

		public int getRequestCount()
		{
			return requestMaxSeqId - requestMinSeqId + 1;
		}
	}

	/**
	 * 获取加载区间的逻辑，也是检查能否加载的逻辑
	 */
	private LoadMoreMsgParam getLoadMoreMsgParam(int channelType)
	{
		if (!CokChannelDef.isInChat())
		{
			return null;
		}
		Channel channel = ChannelManager.getInstance().getChannel(channelType);
		if (channel == null || channel.msgList == null || channel.getChannelView() == null)
		{
			return null;
		}

//		if (false)//!WebSocketManager.isRecieveFromWebSocket(channelType))
//		{
//			return getLoadMoreMsgParamByTime(channel);
//		}
//		else
//		{
			return getLoadMoreMsgParamByTime(channel);
//		}
	}

	private LoadMoreMsgParam getLoadMoreMsgParamByTime(Channel channel)
	{
		Pair<Long, Long> range = channel.getLoadMoreTimeRange();
		if (range != null)
		{
			return new LoadMoreMsgParam(range.first, range.second, false);
		}

		return null;
	}

//	private LoadMoreMsgParam getLoadMoreMsgParamBySeqId(Channel channel)
//	{
//		int viewMinSeqId = channel.getMinSeqId();
//
//		// 不能加载: 没有消息时viewMinSeqId为0，有消息时seqId最小为1
//		if (viewMinSeqId <= 1)
//		{
//			return null;
//		}
//
//		// desireMaxSeqId可能等于desireMinSeqId，仅当二者都为1时
//		int desireMaxSeqId = DBManager.getInstance().getLoadMoreMaxSeqId(channel.getChatTable(), viewMinSeqId);
//		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "desireMaxSeqId", desireMaxSeqId, "viewMinSeqId", viewMinSeqId);
//		int desireMinSeqId = (desireMaxSeqId - 19) > 1 ? (desireMaxSeqId - 19) : 1;
//
//		// 如果desireMaxSeqId在本地db中有，就从db加载（不一定能满20条）
//		if (DBManager.getInstance().isMsgExists(channel.getChatTable(), desireMaxSeqId, -1))
//		{
//			return new LoadMoreMsgParam(desireMinSeqId, desireMaxSeqId, false);
//		}
//
//		// 否则，如果在server范围内，从server加载
//		// server中seqId连续，可以用交集判断
//		Point inter = getIntersection(new Point(channel.serverMinSeqId, channel.serverMaxSeqId), new Point(desireMinSeqId, desireMaxSeqId));
//		if (inter != null)
//		{
//			return new LoadMoreMsgParam(inter.x, inter.y, true);
//		}
//
//		// 既不在db，又不在server（再往前的也肯定不在server），则找到db中最早的，加载之
//		Point range = DBManager.getInstance().getHistorySeqIdRange(channel.getChatTable(), desireMaxSeqId, ChannelManager.LOAD_MORE_COUNT);
//		if (range != null)
//		{
//			return new LoadMoreMsgParam(range.x, range.y, false);
//		}
//
//		return null;
//	}

	/**
	 * 计算两段连续区间的交集
	 * 
	 * @param sec1
	 *            [sec1.x, sec1.y]组成的区间
	 * @param sec2
	 *            [sec2.x, sec2.y]组成的区间
	 * @return null，如果无交集
	 */
	public static Point getIntersection(Point sec1, Point sec2)
	{
		int[] fourValue = { sec1.x, sec1.y, sec2.x, sec2.y };
		Arrays.sort(fourValue); // 升序排序
		int lower = -1;
		int upper = -1;
		for (int i = 0; i < fourValue.length; i++)
		{
			if (fourValue[i] >= sec1.x && fourValue[i] <= sec1.y && fourValue[i] >= sec2.x && fourValue[i] <= sec2.y)
			{
				lower = fourValue[i];
				break;
			}
		}
		for (int i = fourValue.length - 1; i >= 0; i--)
		{
			if (fourValue[i] >= sec1.x && fourValue[i] <= sec1.y && fourValue[i] >= sec2.x && fourValue[i] <= sec2.y)
			{
				upper = fourValue[i];
				break;
			}
		}
		if (lower != -1 && upper != -1)
		{
			return new Point(lower, upper);
		}
		else
		{
			return null;
		}
	}

	private boolean checkMessagesAdapter()
	{
		if (getCurrentChannelView() == null || getCurrentChannelView().getMessagesAdapter() == null)
		{
			// 初始化时showTab肯定会发生，此时getCurrentChannel().getMessagesAdapter()为null
//			LogUtil.trackMessage("checkMessagesAdapter() fail: currentChannel = " + getCurrentChannel() + " messagesAdapter = "
//					+ (getCurrentChannel() == null ? "null" : getCurrentChannel().getMessagesAdapter()) + " currentChatType = "
//					+ GSController.getCurrentChannelType() + " chatActivity = " + GSIM.getChatActivity()
//					+ " chatFragment = " + GSIM.getChatFragment());
			return false;
		}
		return true;
	}

	private void loadMoreMsg()
	{
		createTimerTask();

		if (!checkMessagesAdapter())
			return;

		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		// 极少情况下会发生
		if (channel == null)
			return;
		LoadMoreMsgParam loadMoreMsgParam = getLoadMoreMsgParam(channel.getChannelType());

		if (!getCurrentChannelView().getLoadingStart() && loadMoreMsgParam != null)
		{
			LogUtil.trackPageView("LoadMoreMsg");
			getCurrentChannelView().setLoadingStart(true);
			// 可能有异常 getCount() on a null object reference
			loadMoreCount = 0;
			channel.isLoadingAllNew = false;
			if (loadMoreMsgParam.fetchFromServer)
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从server加载消息");
				JniController.getInstance().excuteJNIVoidMethod(
						"getMsgBySeqId",
						new Object[] {
								Integer.valueOf(loadMoreMsgParam.requestMinSeqId),
								Integer.valueOf(loadMoreMsgParam.requestMaxSeqId),
								Integer.valueOf(channel.getChannelType()),
								channel.getChannelID() });
			}
			else
			{
				LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_VIEW, "从db加载消息");
//				if (!loadMoreMsgParam.useTime)
//				{
//					ChannelManager.getInstance().loadMoreMsgFromDB(channel, loadMoreMsgParam.requestMinSeqId,
//							loadMoreMsgParam.requestMaxSeqId, -1, false);
//				}
//				else
//				{
					ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
//				}
			}
		}

	}

	private void loadMoreMail()
	{
		createTimerTask();

		if (!checkMessagesAdapter())
			return;

		if (!getCurrentChannelView().getLoadingStart() && hasMoreData())
		{
			LogUtil.trackPageView("LoadMoreMail");
			if (GSController.isNewMailListEnable)
			{
				Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
				ChannelManager.getInstance().loadMoreMsgFromDB(channel, -1, -1, channel.getMinCreateTime(), true);
			}
			else
			{
				getCurrentChannelView().setLoadingStart(true);
				loadMoreCount = 0;

				JniController.getInstance().excuteJNIVoidMethod(
						"requestMoreMail",
						new Object[] {
								UserManager.getInstance().getCurrentMail().opponentUid,
								UserManager.getInstance().getCurrentMail().mailUid,
								Integer.valueOf(getCurrentChannelView().getMessageCount()) });
			}
		}
	}

	/**
	 * 时机：各个参数变化时、初始化时 server数据变化时：GetNewMsg返回时 view数据变化时：获取到新消息时
	 */
	public void refreshHasMoreData()
	{
		if (!isInMail())
		{
			if (ChannelManager.getInstance().isGetingNewMsg)
			{
				hasMoreData = false;
			}
			else
			{
				int channelType = CokChannelDef.isInCustomChat() ? customChannelType : GSController.getCurrentChannelType();
				if(channelType == -1)
					hasMoreData = false;
				else
					hasMoreData = getLoadMoreMsgParam(channelType) != null;
			}
		}
		else if (GSController.isNewMailListEnable)
		{
			Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
			if (channel == null)
			{
				hasMoreData = false;
				return;
			}
//			List<Msg> dbUserMails = DBManager.getInstance().getMsgsByTime(channel.getChatTable(), channel.getMinCreateTime(), 1);
//			hasMoreData = dbUserMails.size() > 0;
		}
	}

	private boolean isInMail()
	{
		return (getCurrentChannelView().tab == TAB_MAIL && !CokChannelDef.isInChatRoom()) || getCurrentChannelView().tab == TAB_CUSTOM;
	}

	public void refreshStatusLabel(String status)
	{
		if (StringUtils.isNotEmpty(status))
		{
			getTitleLabel().setText(status);
		}
		else
		{
			getTitleLabel().setText(LanguageManager.getLangByKey(LanguageKeys.TITLE_CHAT));
		}
		onNetworkConnectionChanged();
	}

	public boolean hasMoreData()
	{
		return hasMoreData;
	}

	private ListViewLoadListener	mListViewLoadListener	= new ListViewLoadListener()
															{
																@Override
																public void refreshData()
																{
																	if (isInMail())
																	{
																		loadMoreMail();
																	}
																	else
																	{
																		loadMoreMsg();
																	}
																}

																@Override
																public boolean getIsListViewToTop()
																{
																	if (getCurrentChannelView() == null
																			|| getCurrentChannelView().messagesListView == null)
																		return false;
																	ListView listView = getCurrentChannelView().messagesListView;

																	View topListView = listView.getChildAt(listView
																			.getFirstVisiblePosition());
																	if ((topListView == null) || (topListView.getTop() != 0))
																	{
																		return false;
																	}
																	else
																	{
																		return true;
																	}
																}

																@Override
																public boolean getIsListViewToBottom()
																{
																	if (getCurrentChannelView() == null
																			|| getCurrentChannelView().messagesListView == null)
																		return false;
																	ListView listView = getCurrentChannelView().messagesListView;
																	View bottomView = listView.getChildAt(-1 + listView.getChildCount());
																	if (bottomView == null)
																		return false;
																	if (bottomView.getBottom() > listView.getHeight()
																			|| (listView.getLastVisiblePosition() != -1
																					+ listView.getAdapter().getCount()))
																	{
																		return false;
																	}
																	else
																	{
																		return true;
																	}
																}
															};

	private void createTimerTask()
	{
		final ChannelView channelView = getCurrentChannelView();
		if(channelView == null)
			return;
		channelView.mTimer = new Timer();
		channelView.mTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (activity == null)
					return;
				activity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (channelView.pullDownToLoadListView != null && channelView.channel!=null)
							{
								channelView.pullDownToLoadListView.hideProgressBar();
								resetMoreDataStart(channelView.channelType,channelView.channel.getChannelID());
							}
						}
						catch (Exception e)
						{
							LogUtil.printException(e);
						}
					}
				});
			}
		};
		if (channelView.mTimer != null)
			channelView.mTimer.schedule(channelView.mTimerTask, 5000);
	}

	private boolean				hasMoreData			= true;

	private OnScrollListener	mOnScrollListener	= new AbsListView.OnScrollListener()
													{

														@Override
														public void onScrollStateChanged(AbsListView view, int scrollState)
														{
															if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
															{
																if (getCurrentChannelView() != null
																		&& getCurrentChannelView().messagesListView != null)
																{
																	View topView = getCurrentChannelView().messagesListView
																			.getChildAt(getCurrentChannelView().messagesListView
																					.getFirstVisiblePosition());
																	if ((topView != null) && (topView.getTop() == 0)
																			&& !getCurrentChannelView().getLoadingStart())
																	{
																		getCurrentChannelView().pullDownToLoadListView.startTopScroll();
																	}
																}

															}

															if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
																GSController.isListViewFling = true;
															else
																GSController.isListViewFling = false;
														}

														@Override
														public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
																int totalItemCount)
														{
															if (getCurrentChannelView() != null
																	&& getCurrentChannelView().pullDownToLoadListView != null
																	&& getCurrentChannelView().pullDownToLoadListView.getVisibility() == View.VISIBLE)
															{
																if (hasMoreData())
																{
																	if (!getCurrentChannelView().getLoadingStart())
																	{
																		getCurrentChannelView().pullDownToLoadListView
																				.setAllowPullDownRefersh(false);
																	}
																	else
																	{
																		getCurrentChannelView().pullDownToLoadListView
																				.setAllowPullDownRefersh(true);
																	}
																}
																else
																{
																	getCurrentChannelView().pullDownToLoadListView
																			.setAllowPullDownRefersh(true);
																}
															}
														}
													};

	public void onJoinAnnounceInvitationSuccess()
	{
		if (getCountryChannelView() != null)
		{
			// 隐藏noAllianceFrameLayout，点联盟自然会调用
			getCountryChannelView().getMessagesAdapter().onJoinAnnounceInvitationSuccess();
		}
	}

	public int getToastPosY()
	{
		int[] location = { 0, 0 };
		messagesListFrameLayout.getLocationOnScreen(location);
		return location[1] + ScaleUtil.dip2px(activity, 5);
	}

	private final int	TAB_COUNTRY		= 0;
	private final int	TAB_ALLIANCE	= 1;
	private final int	TAB_CUSTOM		= 2;
	private final int	TAB_MAIL		= 3;
	private final int	TAB_BATTLE_FIELD= 4;
	
	private int repeatCount = 0;
	
	private int currentTab = -1;
	
	private void showTab(int tab)
	{
		System.out.println("showTab tab:"+tab);
		if(currentTab < 3 || currentTab == TAB_BATTLE_FIELD)
		{
			saveDraft();
		}
		
		if(currentTab == TAB_COUNTRY && tab == TAB_BATTLE_FIELD)
			LogUtil.trackAction("click_from_country_to_battle");
		else if(currentTab == TAB_BATTLE_FIELD && tab == TAB_COUNTRY)
			LogUtil.trackAction("click_from_battle_to_country");
		
		if(currentTab!=-1 && currentTab != tab)
		{
			activity.hideSoftKeyBoard();
			replyField.clearFocus();
		}
		
		boolean hasCustomData = (friendList!=null && friendList.size()>0) || (chatroomChannelList!=null && chatroomChannelList.size()>0);
		
		currentTab = tab;
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCountry, tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonAlliance, tab == TAB_ALLIANCE);
		CompatibleApiUtil.getInstance().setButtonAlpha(buttonCustom, tab == TAB_CUSTOM);

		if (tab == TAB_MAIL)
		{
			buttonsLinearLayout.setVisibility(View.GONE);
		}
		else
		{
			buttonsLinearLayout.setVisibility(View.VISIBLE);
			imageView2.setVisibility(View.VISIBLE);
		}

		boolean isInAlliance = UserManager.getInstance().isCurrentUserInAlliance();

		if(channelViews!=null)
		{
			for (int i = 0; i < channelViews.size(); i++)
			{
				ChannelView channelView = channelViews.get(i);
				if (channelView != null)
				{
					if((tab == TAB_ALLIANCE && !isInAlliance)
							|| (tab == TAB_CUSTOM && (StringUtils.isEmpty(customChannelId) || !hasCustomData))
							|| (tab == TAB_COUNTRY && !CokConfig.isBattleChatEnable && CokConfig.isInDragonSencen()))
						channelView.setVisibility(View.GONE);
					else
						channelView.setVisibility(tab == channelView.tab ? View.VISIBLE : View.GONE);
				}
			}
		}

		if(tab == TAB_COUNTRY)
		{
			country_channel_name.setText(GSController.originalServerName);
		}
		else if(tab == TAB_BATTLE_FIELD)
		{
			if(CokConfig.isInAncientSencen())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD_ANCIENT));
			else if(CokConfig.isInDragonSencen())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD_DRAGON));
			else if(CokConfig.isInKingdomBattleField())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD_KINGDOM));
			else if(CokConfig.needShowBattleTipLayout())
				country_channel_name.setText(LanguageManager.getLangByKey(LanguageKeys.BATTLE_FIELD));
		}
		horn_checkbox.setVisibility(((tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD) && ConfigManager.enableChatHorn) ? View.VISIBLE : View.GONE);
		custom_channel_setting_layout.setVisibility((tab == TAB_CUSTOM && !isSettingCustomChannel && StringUtils.isNotEmpty(customChannelId) && hasCustomData)? View.VISIBLE : View.GONE);
		country_exchange_layout.setVisibility(CokConfig.needShowBattleFieldChannel() && (tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD) ? View.VISIBLE : View.GONE);
		custom_setting_layout.setVisibility(tab == TAB_CUSTOM && isSettingCustomChannel? View.VISIBLE : View.GONE);
		noAllianceFrameLayout.setVisibility((tab == TAB_ALLIANCE && !isInAlliance) ? View.VISIBLE : View.GONE);
		custom_chat_tip_layout.setVisibility(tab == TAB_CUSTOM && (StringUtils.isEmpty(customChannelId) || !hasCustomData) ? View.VISIBLE : View.GONE);
		battle_field_tip_layout.setVisibility(tab == TAB_BATTLE_FIELD && CokConfig.needShowBattleTipLayout() ? View.VISIBLE : View.GONE);
		custom_settting_btn_layout.setVisibility(tab == TAB_CUSTOM && isSettingCustomChannel ? View.VISIBLE : View.GONE);
		hs__dragon_chat_tip_layout.setVisibility((tab == TAB_COUNTRY && !CokConfig.isBattleChatEnable && CokConfig.isInDragonSencen()) ? View.VISIBLE: View.GONE);
		relativeLayout1.setVisibility(hs__dragon_chat_tip_layout.getVisibility() == View.VISIBLE || (tab == TAB_ALLIANCE && !isInAlliance) || (tab == TAB_BATTLE_FIELD && CokConfig.needShowBattleTipLayout()) || (tab == TAB_CUSTOM && (StringUtils.isEmpty(customChannelId) || isSettingCustomChannel || !hasCustomData)) ? View.GONE : View.VISIBLE);
		
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		
		if(country_exchange_layout.getVisibility() == View.VISIBLE && (config == null || (config!=null && !config.isBattleChannelShowed())))
		{
			if(config!=null && !config.isBattleChannelShowed())
			{
				config.setBattleChannelShowed(true);
				ConfigManager.getInstance().saveLocalConfig();
			}
			else if(config == null)
			{
				config = new LocalConfig();
				config.setBattleChannelShowed(true);
				ConfigManager.getInstance().setLocalConfig(config);
				ConfigManager.getInstance().saveLocalConfig();
			}
			
			ScaleAnimation animation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(500);
			animation.setRepeatCount(5);
			animation.setRepeatMode(Animation.REVERSE);
			country_exchange_btn.startAnimation(animation);
		}
		
		
		if (tab == TAB_ALLIANCE && !isInAlliance && ConfigManager.getInstance().isFirstJoinAlliance && !isJoinAlliancePopupShowing)
		{
			try
			{
				showJoinAlliancePopup();
			}
			catch (Exception e)
			{
				LogUtil.printException(e);
			}
		}

		if (tab == TAB_COUNTRY)
		{
			GSController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_COUNTRY);
			if (GSController.isHornItemUsed && ConfigManager.enableChatHorn)
			{
				horn_checkbox.setChecked(true);
				refreshBottomUI(true);
				ConfigManager.isHornBtnEnable = true;
			}
			else
			{
				horn_checkbox.setChecked(ConfigManager.isHornBtnEnable);
				refreshBottomUI(ConfigManager.isHornBtnEnable);
			}

			if (CokChannelDef.isChatRestrictForLevel(GSController.getCurrentChannelType()))
			{
				replyField.setEnabled(false);
				replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.CHAT_RESTRICT_TIP,
						"" + CokChannelDef.getChatRestrictLevel()));
			}
			else
			{
				replyField.setHint("");
				replyField.setEnabled(true);
				Channel channel = CokConfig.getInstance().getCountryChannel();
				if(channel!=null && StringUtils.isNotEmpty(channel.getDraft()))
				{
					replyField.setText(channel.getDraft());
					replyField.setSelection(channel.getDraft().length());
				}
				else
					replyField.setText("");
			}
		}
		else
		{
			if(tab!=TAB_BATTLE_FIELD && (config == null || (config!=null && !config.isAudioUsed())))
				replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO_USE));
			else
				replyField.setHint("");
			replyField.setEnabled(true);
			
			if (tab == TAB_ALLIANCE)
				GSController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_ALLIANCE);
			else if (tab == TAB_CUSTOM)
				GSController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT);
			else if (tab == TAB_BATTLE_FIELD)
			{
				GSController.setCurrentChannelType(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
				if (GSController.isHornItemUsed && ConfigManager.enableChatHorn)
				{
					horn_checkbox.setChecked(true);
					refreshBottomUI(true);
					ConfigManager.isHornBtnEnable = true;
				}
				else
				{
					horn_checkbox.setChecked(ConfigManager.isHornBtnEnable);
					refreshBottomUI(ConfigManager.isHornBtnEnable);
				}
			}
				
			refreshBottomUI(false);
			
			Channel channel = null;
			if(tab == TAB_CUSTOM)
			{
				if((CokChannelDef.isInUserMail(customChannelType) || CokChannelDef.isInChatRoom(customChannelType)) && StringUtils.isNotEmpty(customChannelId))
					channel = ChannelManager.getInstance().getChannel(customChannelType,customChannelId);
				if(isSettingCustomChannel)
					refreshCustomChannelImage(channel);
			}
			else
				channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
			
			if(channel!=null && StringUtils.isNotEmpty(channel.getDraft()))
			{
				replyField.setText(channel.getDraft());
				replyField.setSelection(channel.getDraft().length());
			}
			else
				replyField.setText("");
		}
		
		refreshInputButton();
//		getShowFriendButton().setVisibility(CokChannelDef.isInMailDialog() || (CokChannelDef.isInCustomChat()) ? View.GONE : View.VISIBLE);
//		setSelectMemberBtnState();
		if(channelViews == null || channelViews.size()<=0)
			return;
		ChannelView channelView = null;
		if(tab == TAB_MAIL)
		{
			channelView = channelViews.get(0);
			setChannelViewIndex(0);
		}
		else if(tab == TAB_BATTLE_FIELD)
		{
			if(channelViews.size()>3)
			{
				channelView = channelViews.get(3);
				setChannelViewIndex(3);
			}
		}
		else if(tab<channelViews.size())
		{
			channelView = channelViews.get(tab);
			setChannelViewIndex(tab);
		}
		
		if (channelView != null && channelView.getVisibility() == View.VISIBLE)
		{
			refreshToolTip();
			if ((tab == TAB_COUNTRY || tab == TAB_BATTLE_FIELD) && !hornTextHidden)
			{
				Msg msgItem = ScrollTextManager.getInstance().getNextText(GSController.getCurrentChannelType());
				if (msgItem != null)
					showHornScrollText(msgItem);
				else
				{
					horn_scroll_layout.setVisibility(View.GONE);
				}
			}
			else
			{
				horn_scroll_layout.setVisibility(View.GONE);
			}
			refreshHasMoreData();

			if (channelView.channel != null)
			{
				channelView.channel.getTimeNeedShowMsgIndex();
				channelView.channel.markAsRead();
			}
		}
		else
		{
			showToolTip(false);
			horn_scroll_layout.setVisibility(View.GONE);
		}
	}
	
	public void updateAudioHint()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if(!CokChannelDef.isInCountryTab() && (config == null || (config!=null && !config.isAudioUsed())))
			replyField.setHint(LanguageManager.getLangByKey(LanguageKeys.TIP_AUDIO_USE));
		else
			replyField.setHint("");
	}
	
	private void updateHornLayoutHeight(RelativeLayout horn_scroll_layout)
	{
		if(horn_scroll_layout!=null)
		{
			int length = CokConfig.needShowBattleFieldChannel() ? (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio
					* activity.getScreenCorrectionFactor() + ScaleUtil.dip2px(6)) : ScaleUtil.dip2px(10);
			RelativeLayout.LayoutParams horn_scroll_layoutLayoutParams = (RelativeLayout.LayoutParams) horn_scroll_layout.getLayoutParams();

			if (horn_scroll_layoutLayoutParams.topMargin != length)
			{
				horn_scroll_layoutLayoutParams.topMargin = length;
				horn_scroll_layout.setLayoutParams(horn_scroll_layoutLayoutParams);
			}
		}
	}

	private int previousTextCount = 0;
	/**
	 * 没有文字时，设置初始状态
	 */
	private void resetInputButton(boolean resetVisibility)
	{
		if(resetVisibility)
		{
			sendMessageLayout.setVisibility(canShowRecordButton() ? View.GONE : View.VISIBLE);
			voice_rec_button_layout.setVisibility(canShowRecordButton() ? View.VISIBLE : View.GONE);
		}
		
		// 在联盟输入文字再删除，返回国家，按钮会不显示，因为被动画代码改变了alpha和缩放
        ViewProxy.setAlpha(voice_rec_button_layout, 1.0f);
        ViewProxy.setScaleX(voice_rec_button_layout, 1.0f);
        ViewProxy.setScaleY(voice_rec_button_layout, 1.0f);
        ViewProxy.setAlpha(sendMessageLayout, 1.0f);
        ViewProxy.setScaleX(sendMessageLayout, 1.0f);
        ViewProxy.setScaleY(sendMessageLayout, 1.0f);
	}
	
	public void saveDraft()
	{
		if(replyField == null)
			return;
		String replyFieldText = replyField.getText().toString();
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if(channel!=null && replyFieldText!=null)
		{
			channel.setDraft(replyFieldText);
			channel.setDraftTime(TimeManager.getInstance().getCurrentTimeMS());
			channel.updateDB();
		}
	}
	
	@Override
	public void onPause()
	{
		System.out.println("onPause saveDraft");
		saveDraft();
		super.onPause();
	}
	
	private boolean canShowRecordButton()
	{
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN && ConfigManager.isXMEnabled && ConfigManager.isXMAudioEnabled && currentTab != TAB_COUNTRY && currentTab != TAB_BATTLE_FIELD && !isHornUI;
	}
	
	private void refreshInputButton()
	{
		if(canShowRecordButton())
		{
			checkSendButton(true);
		}else{
			resetInputButton(true);
		}
	}
	
	private void refreshWordCount()
	{
		if (replyField == null || wordCount == null)
			return;

		// 有文字与没文字之间发生切换时
		if((previousTextCount == 0 && replyField.getText().length() > 0) || (previousTextCount > 0 && replyField.getText().length() == 0))
		{
			previousTextCount = replyField.getText().length();
			
			refreshInputButton();
		}
		
		if (replyField.getLineCount() >= 2)
		{
			wordCount.setVisibility(View.VISIBLE);
		}
		else
		{
			wordCount.setVisibility(View.GONE);
		}
		wordCount.setText(replyField.getText().length() + "/" + curMaxInputLength);
	}

    private AnimatorSetProxy runningAnimation;
    private AnimatorSetProxy runningAnimation2;
    private AnimatorSetProxy runningAnimationAudio;
    private int runningAnimationType;
    /*
     * 语音和发送按钮的动画切换
     */
	private void checkSendButton(final boolean animated) {
        if (replyField.getText().length() > 0) {
            if (voice_rec_button_layout.getVisibility() == View.VISIBLE) {
                if (animated) {
                    if (runningAnimationType == 1) {
                        return;
                    }
                    if (runningAnimation != null) {
                        runningAnimation.cancel();
                        runningAnimation = null;
                    }
                    if (runningAnimation2 != null) {
                        runningAnimation2.cancel();
                        runningAnimation2 = null;
                    }

                    sendMessageLayout.setVisibility(View.VISIBLE);
                    runningAnimation = new AnimatorSetProxy();
                    runningAnimationType = 1;

                    runningAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleX", 0.1f),
                            ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleY", 0.1f),
                            ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 0.0f),
                            ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(sendMessageLayout, "alpha", 1.0f)
                    );

                    runningAnimation.setDuration(150);
                    runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            if (runningAnimation != null && runningAnimation.equals(animation)) {
                                sendMessageLayout.setVisibility(View.VISIBLE);
                                voice_rec_button_layout.setVisibility(View.GONE);
                                voice_rec_button_layout.clearAnimation();
                                runningAnimation = null;
                                runningAnimationType = 0;
                            }
                        }
                    });
                    runningAnimation.start();
                } else {
                    ViewProxy.setScaleX(voice_rec_button_layout, 0.1f);
                    ViewProxy.setScaleY(voice_rec_button_layout, 0.1f);
                    ViewProxy.setAlpha(voice_rec_button_layout, 0.0f);
                    ViewProxy.setScaleX(sendMessageLayout, 1.0f);
                    ViewProxy.setScaleY(sendMessageLayout, 1.0f);
                    ViewProxy.setAlpha(sendMessageLayout, 1.0f);
                    sendMessageLayout.setVisibility(View.VISIBLE);
                    voice_rec_button_layout.setVisibility(View.GONE);
                    voice_rec_button_layout.clearAnimation();
                }
            }
        } else {
            if (animated) {
                if (runningAnimationType == 2) {
                    return;
                }

                if (runningAnimation != null) {
                    runningAnimation.cancel();
                    runningAnimation = null;
                }
                if (runningAnimation2 != null) {
                    runningAnimation2.cancel();
                    runningAnimation2 = null;
                }

                voice_rec_button_layout.setVisibility(View.VISIBLE);
                runningAnimation = new AnimatorSetProxy();
                runningAnimationType = 2;

                runningAnimation.playTogether(
                        ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleX", 0.1f),
                        ObjectAnimatorProxy.ofFloat(sendMessageLayout, "scaleY", 0.1f),
                        ObjectAnimatorProxy.ofFloat(sendMessageLayout, "alpha", 0.0f),
                        ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleX", 1.0f),
                        ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "scaleY", 1.0f),
                        ObjectAnimatorProxy.ofFloat(voice_rec_button_layout, "alpha", 1.0f)
                );

                runningAnimation.setDuration(150);
                runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (runningAnimation != null && runningAnimation.equals(animation)) {
                            sendMessageLayout.setVisibility(View.GONE);
                            sendMessageLayout.clearAnimation();
                            voice_rec_button_layout.setVisibility(View.VISIBLE);
                            runningAnimation = null;
                            runningAnimationType = 0;
                        }
                    }
                });
                runningAnimation.start();
            } else {
                ViewProxy.setScaleX(sendMessageLayout, 0.1f);
                ViewProxy.setScaleY(sendMessageLayout, 0.1f);
                ViewProxy.setAlpha(sendMessageLayout, 0.0f);
                ViewProxy.setScaleX(voice_rec_button_layout, 1.0f);
                ViewProxy.setScaleY(voice_rec_button_layout, 1.0f);
                ViewProxy.setAlpha(voice_rec_button_layout, 1.0f);
                sendMessageLayout.setVisibility(View.GONE);
                sendMessageLayout.clearAnimation();
                voice_rec_button_layout.setVisibility(View.VISIBLE);
            }
        }
    }

	@Override
	public void onStop()
	{
		super.onStop();
	}

	public void onBackClicked()
	{
	}

	private final static int	sendButtonBaseWidth		= 60;
	private final static int	sendButtonBaseHeight	= 61;
	private final static int	hornCheckBoxWidth		= 70;
	int targetButtonWidth;
	int targetButtonHeight;
	
	private double getAudioUIScale()
	{
		return ConfigManager.getInstance().needScaleInputPanel() ? ConfigManager.scaleRatio : 1;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void adjustHeight()
	{
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (addReply.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (addReply.getWidth() != 0 && !adjustSizeCompleted)
		{
	        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	        if (resourceId > 0) {
	            AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
	        }
	        
	        int originalWidth = addReply.getWidth();
        	int scaleWidth = (int) Math.floor(ScaleUtil.getScreenWidth() / 14);
	        if(ConfigManager.getInstance().needScaleInputPanel() && scaleWidth > originalWidth){
	        	targetButtonWidth = scaleWidth;
	        }else{
	        	targetButtonWidth = originalWidth;
	        }
	        
	        double editTextRatio = (double) targetButtonWidth / (double) originalWidth;
	        
			// S3手机上的尺寸(目标效果是在S3手机上调的好，界面、文字都相对于它进行缩放)
			// addReply宽度是宽度的1/4，让其高度保持长宽比，然后再计算出缩放的倍率（textRatio）
			double sendButtonRatio = (double) sendButtonBaseHeight / (double) sendButtonBaseWidth;
	        targetButtonHeight = (int) (targetButtonWidth * sendButtonRatio);
	        
	        updateHornLayoutHeight(horn_scroll_layout);

	        LinearLayout.LayoutParams relativeLayoutLayoutParams = (LinearLayout.LayoutParams) relativeLayout1.getLayoutParams();
			relativeLayoutLayoutParams.height = targetButtonHeight + ScaleUtil.dip2px(5);
			relativeLayout1.setLayoutParams(relativeLayoutLayoutParams);
	        
			LinearLayout.LayoutParams checkboxParams = (LinearLayout.LayoutParams) horn_checkbox.getLayoutParams();
			checkboxParams.width = targetButtonHeight;
			checkboxParams.height = targetButtonHeight;
			horn_checkbox.setLayoutParams(checkboxParams);

			LinearLayout.LayoutParams addReplyParams = (LinearLayout.LayoutParams)addReply.getLayoutParams();
			addReplyParams.width = targetButtonWidth;
			addReplyParams.height = targetButtonHeight;
			addReply.setLayoutParams(addReplyParams);
			
			LinearLayout.LayoutParams replyFieldParams = (LinearLayout.LayoutParams)replyField.getLayoutParams();
			replyFieldParams.height = targetButtonHeight;
			replyField.setLayoutParams(replyFieldParams);
			
			LinearLayout.LayoutParams buttonCountryParams = (LinearLayout.LayoutParams)buttonCountry.getLayoutParams();
			buttonCountryParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonCountry.setLayoutParams(buttonCountryParams);

			LinearLayout.LayoutParams recordButtonParams = (LinearLayout.LayoutParams) voice_rec_button.getLayoutParams();
			recordButtonParams.width = targetButtonWidth;
			recordButtonParams.height = targetButtonHeight;
			voice_rec_button.setLayoutParams(recordButtonParams);
			
			LinearLayout.LayoutParams buttonAllianceParams = (LinearLayout.LayoutParams)buttonAlliance.getLayoutParams();
			buttonAllianceParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonAlliance.setLayoutParams(buttonAllianceParams);
			

			LinearLayout.LayoutParams buttonFriendParams = (LinearLayout.LayoutParams)buttonCustom.getLayoutParams();
			buttonFriendParams.height = (int) (79 * ConfigManager.scaleRatioButton);
			buttonCustom.setLayoutParams(buttonFriendParams);

			LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams((int) (13 * ConfigManager.scaleRatio),
					(int) (17 * ConfigManager.scaleRatio), 1);
			param3.gravity = Gravity.CENTER_VERTICAL;
			tooltipArrow.setLayoutParams(param3);

			ScaleUtil.adjustTextSize(addReply, ConfigManager.scaleRatio);
			if(ConfigManager.getInstance().needScaleInputPanel()){
				ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio * editTextRatio * 0.9);
			}else{
				ScaleUtil.adjustTextSize(replyField, ConfigManager.scaleRatio);
			}
			
			
			int length = (int) (ScaleUtil.dip2px(activity, 65) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (custom_head_layout != null)
			{
				FrameLayout.LayoutParams custom_head_layoutLayoutParams = (FrameLayout.LayoutParams) custom_head_layout.getLayoutParams();
				custom_head_layoutLayoutParams.width = length;
				custom_head_layoutLayoutParams.height = length;
				custom_head_layout.setLayoutParams(custom_head_layoutLayoutParams);
			}
			
			int length2 = (int) (ScaleUtil.dip2px(activity, 50) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (headImageContainer != null)
			{
				FrameLayout.LayoutParams headImageContainerLayoutParams = (FrameLayout.LayoutParams) headImageContainer.getLayoutParams();
				headImageContainerLayoutParams.width = length2;
				headImageContainerLayoutParams.height = length2;
				headImageContainer.setLayoutParams(headImageContainerLayoutParams);
			}
			
			int length3 = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (custom_channel_setting_layout != null)
			{
				FrameLayout.LayoutParams custom_channel_setting_layoutLayoutParams = (FrameLayout.LayoutParams) custom_channel_setting_layout.getLayoutParams();
				custom_channel_setting_layoutLayoutParams.height = length3;
				custom_channel_setting_layout.setLayoutParams(custom_channel_setting_layoutLayoutParams);
			}
			
			if (country_exchange_layout != null)
			{
				FrameLayout.LayoutParams country_exchange_layoutLayoutParams = (FrameLayout.LayoutParams) country_exchange_layout.getLayoutParams();
				country_exchange_layoutLayoutParams.height = length3;
				country_exchange_layout.setLayoutParams(country_exchange_layoutLayoutParams);
			}
			
			int length4 = (int) (ScaleUtil.dip2px(activity, 40) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if (add_title != null)
			{
				LinearLayout.LayoutParams add_titleLayoutParams = (LinearLayout.LayoutParams) add_title.getLayoutParams();
				add_titleLayoutParams.height = length4;
				add_title.setLayoutParams(add_titleLayoutParams);
			}
			
			int length4_1 = (int) (ScaleUtil.dip2px(activity, 35) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if(custom_channel_settting_btn!=null)
			{
				RelativeLayout.LayoutParams custom_channel_settting_btn_Layout = (RelativeLayout.LayoutParams)custom_channel_settting_btn.getLayoutParams();
				custom_channel_settting_btn_Layout.width = length4_1;
				custom_channel_settting_btn_Layout.height = length4_1;
				if(ConfigManager.getInstance().needRTL())
				{
					custom_channel_settting_btn_Layout.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					custom_channel_settting_btn_Layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				custom_channel_settting_btn.setLayoutParams(custom_channel_settting_btn_Layout);
			}
			
			int length5 = (int) (ScaleUtil.dip2px(activity, 25) * ConfigManager.scaleRatio * activity.getScreenCorrectionFactor());
			if(country_exchange_btn!=null)
			{
				LinearLayout.LayoutParams country_exchange_btn_Layout = (LinearLayout.LayoutParams)country_exchange_btn.getLayoutParams();
				country_exchange_btn_Layout.width = length5;
				country_exchange_btn_Layout.height = length5;
				country_exchange_btn.setLayoutParams(country_exchange_btn_Layout);
			}
				
			ScaleUtil.adjustTextSize(wordCount, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonCountry, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonAlliance, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(buttonCustom, ConfigManager.scaleRatio);

			ScaleUtil.adjustTextSize(buttonJoinAlliance, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(noAllianceTipText, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(dragon_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_chat_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(addCustomChatBtn, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(tooltipLabel, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_scroll_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(horn_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(add_title, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(add_tip, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(customChannelName, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_channel_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(custom_setting_confim, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(country_channel_name, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(battle_field_tip_text, ConfigManager.scaleRatio);
			ScaleUtil.adjustTextSize(battle_field_btn, ConfigManager.scaleRatio);
			

			adjustSizeCompleted = true;
			
			this.resetInputButton(true);

			if (lazyLoading)
			{
				activity.showProgressBar();
				onBecomeVisible();
			}
		}
	}
	
	private void refreshCustomChatChannel(Channel chatChannel)
	{
		if(activity == null || !CokChannelDef.isInChat() || channelViews == null || channelViews.size() < (TAB_CUSTOM+1))
			return;
		
		final ChannelView channelView = channelViews.get(TAB_CUSTOM);
		if(channelView == null)
			return;
		
		channelView.tab = TAB_CUSTOM;
		if(chatChannel!=null)
		{
			if(!chatChannel.hasInitLoaded())
				chatChannel.loadMoreMsg();
			chatChannel.clearFirstNewMsg();
			chatChannel.setChannelView(channelView);
			int mailType = CokConfig.getInstance().isModChannel(chatChannel) ? MailManager.MAIL_MOD_PERSONAL : MailManager.MAIL_USER;
			GSController.setMailInfo(chatChannel.getChannelID(), chatChannel.latestId, chatChannel.getCustomName(), mailType);
		}
		else
		{
			GSController.setMailInfo("", "", "", -1);
		}
//		refreshMemberSelectBtn();
		channelView.channelType = DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT;
		channelView.channel = chatChannel;
		
		List<Msg> msgList = null;
		
		if(channelView.channel!=null)
		{
			msgList = channelView.channel.msgList;
			channelView.setVisibility(View.VISIBLE);
		}
		else
		{
			channelView.setVisibility(View.GONE);
		}
		
		if(msgList == null)
			msgList = new ArrayList<Msg>();
		
		if (msgList != null)
		{
			MessagesAdapter adapter = new MessagesAdapter(activity, msgList);
			channelView.setMessagesAdapter(adapter);
		}
		activity.runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				if(channelView.messagesListView!=null)
					channelView.messagesListView.setAdapter(channelView.getMessagesAdapter());
				if (channelView.channel!=null && channelView.channel.lastPosition.x != -1 && rememberPosition)
				{
					channelView.messagesListView.setSelectionFromTop(channelView.channel.lastPosition.x, channelView.channel.lastPosition.y);
				}
				else
				{
					if(channelView.getMessagesAdapter()!=null)
						channelView.messagesListView.setSelection(channelView.getMessagesAdapter().getCount() - 1);
				}
			}
		});
		refreshCustomChannelName(chatChannel);
	}
	
	private void refreshCustomChatChannel(int channelType,String channelId)
	{
		Channel chatChannel = ChannelManager.getInstance().getChannel(channelType, channelId);
		refreshCustomChatChannel(chatChannel);
		if(chatChannel!=null && StringUtils.isNotEmpty(chatChannel.getDraft()))
		{
			replyField.setText(chatChannel.getDraft());
			replyField.setSelection(chatChannel.getDraft().length());
		}
		else
			replyField.setText("");
	}
	
	private void refreshCustomChatChannel()
	{
		LocalConfig config = ConfigManager.getInstance().getLocalConfig();
		if(config!=null)
		{
			customChannelType = config.getCustomChannelType();
			customChannelId = config.getCustomChannelId();
		}
		refreshCustomChatChannel(customChannelType, customChannelId);
	}

	public void showRedPackageConfirm(final Msg msgItem)
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					activity.showRedPackagePopup(msgItem);
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});
	}

	public void hideRedPackageConfirm()
	{
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					activity.hideRedPackagePopup();
				}
				catch (Exception e)
				{
					LogUtil.printException(e);
				}
			}
		});

	}

	public Msg getCurrentRedPackageItem()
	{
		if (activity != null)
		{
			return activity.getRedPackagePopItem();
		}
		return null;
	}

	protected void onContentAreaTouched()
	{
		hideSoftKeyBoard();
		replyField.clearFocus();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(customChannelChange)
		{
			ConfigManager.getInstance().saveLocalConfig();
			customChannelChange = false;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void onDestroy()
	{
		System.out.println("chatfragment new onDestroy");
		if(customChannelChange)
		{
			ConfigManager.getInstance().saveLocalConfig();
			customChannelChange = false;
		}
		if(CokChannelDef.isInAlliance())
			GSController.getInstance().setGameMusiceEnable(true);
		GSController.isContactMod = false;
		GSController.needShowAllianceDialog = false;
		if (tooltipLayout != null)
			tooltipLayout.setOnClickListener(null);
		if (buttonJoinAlliance != null)
			buttonJoinAlliance.setOnClickListener(null);
		if (channelButton != null)
		{
			for (int i = 0; i < channelButton.size(); i++)
			{
				channelButton.get(i).setTag(null);
				channelButton.get(i).setOnClickListener(null);
			}
			channelButton.clear();
			channelButton = null;
		}

		try
		{
			getMemberSelectButton().setOnClickListener(null);
			if (getShowFriendButton() != null)
			{
				getShowFriendButton().setOnClickListener(null);
			}
		}
		catch (Exception e)
		{
			LogUtil.printException(e);
		}

		if (addReply != null)
		{
			addReply.setOnClickListener(null);
			addReply = null;
		}

		if (replyField != null)
		{
			replyField.setOnEditorActionListener(null);
			replyField.removeTextChangedListener(textChangedListener);
			replyField = null;
		}
		textChangedListener = null;

		if (horn_checkbox != null)
		{
			horn_checkbox.setOnCheckedChangeListener(null);
			horn_checkbox = null;
		}

		if (messagesListFrameLayout != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			if (messagesListFrameLayout.getViewTreeObserver() != null)
			{
				messagesListFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
			}
			messagesListFrameLayout.removeAllViews();
			messagesListFrameLayout = null;
		}
		onGlobalLayoutListener = null;

		mOnScrollListener = null;
		mListViewLoadListener = null;

		noAllianceFrameLayout = null;
		relativeLayout1 = null;
		buttonsLinearLayout = null;
		imageView1 = null;
		imageView2 = null;
		wordCount = null;
		buttonCountry = null;
		buttonAlliance = null;
		buttonCustom = null;
		tooltipLayout = null;
		tooltipLabel = null;
		tooltipArrow = null;
		buttonJoinAlliance = null;
		noAllianceTipText = null;
		hs__dragon_chat_tip_layout = null;
		dragon_chat_tip_text = null;

        if(mManager != null){
            mManager.unregisterListener(this);//注销传感器监听
            mManager = null;
        }
        
		((ChatActivity) getActivity()).fragment = null;

		super.onDestroy();
	}

	protected ViewTreeObserver.OnGlobalLayoutListener	onGlobalLayoutListener;
	private TextWatcher									textChangedListener;

	private SensorManager								mManager;				// 传感器管理对象

	private void initSensorListener()
	{
		mManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),// 距离感应器
				SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型

	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		float[] values = event.values;
		if (values != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY)
		{
			boolean oldValue = ConfigManager.playAudioBySpeaker;
			// 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
			if (values[0] == 0.0)
			{
				// 贴近手机
				System.out.println("hands up");
				ConfigManager.playAudioBySpeaker = false;
			}
			else
			{
				// 远离手机
				System.out.println("hands moved");
				ConfigManager.playAudioBySpeaker = true;
			}
			if (oldValue != ConfigManager.playAudioBySpeaker)
			{
				XiaoMiToolManager.getInstance().refreshSpeakerphoneState();
			}
		}

	}
	
	protected static boolean	dataChanged	= false;

	@Override
	public void onResume()
	{
		System.out.println("onResume");
		super.onResume();
		if (CokChannelDef.isInChatRoom())
			getTitleLabel().setText(UserManager.getInstance().getCurrentMail().opponentName);
		else if(CokChannelDef.isInCustomChat())
			custom_channel_name.setText(UserManager.getInstance().getCurrentMail().opponentName);
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if(channel!=null && StringUtils.isNotEmpty(channel.getDraft()))
		{
			activity.showSoftKeyBoard(replyField);
			replyField.requestFocus();
		}
		if (dataChanged && getCurrentChannel() != null)
		{
			System.out.println("onResume chatfragment onMsgAdd");
			notifyDataSetChanged(GSController.getCurrentChannelType(), getCurrentChannel().getChannelID(),true);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}
}