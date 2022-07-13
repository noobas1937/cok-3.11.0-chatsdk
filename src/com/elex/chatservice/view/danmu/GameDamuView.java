package com.elex.chatservice.view.danmu;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.DanmuManager;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.danmu.controller.IDanmakuView;
import com.elex.chatservice.danmu.loader.ILoader;
import com.elex.chatservice.danmu.loader.IllegalDataException;
import com.elex.chatservice.danmu.loader.android.DanmakuLoaderFactory;
import com.elex.chatservice.danmu.model.BaseDanmaku;
import com.elex.chatservice.danmu.model.DanmakuTimer;
import com.elex.chatservice.danmu.model.IDanmakus;
import com.elex.chatservice.danmu.model.IDisplayer;
import com.elex.chatservice.danmu.model.android.BaseCacheStuffer;
import com.elex.chatservice.danmu.model.android.DanmakuContext;
import com.elex.chatservice.danmu.model.android.Danmakus;
import com.elex.chatservice.danmu.model.android.SpannedCacheStuffer;
import com.elex.chatservice.danmu.parser.BaseDanmakuParser;
import com.elex.chatservice.danmu.parser.IDataSource;
import com.elex.chatservice.danmu.parser.android.BiliDanmukuParser;
import com.elex.chatservice.danmu.util.IOUtils;
import com.elex.chatservice.danmu.util.SystemClock;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.DanmuInfo;
import com.elex.chatservice.model.DragonDanmuParser;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class GameDamuView extends RelativeLayout
{

	private BaseDanmakuParser		mParser;
	private EditText				danmu_edittext;
	private Button					sendDanmuBtn;
	private IDanmakuView			mDanmakuView;
	private DanmakuContext			mContext;
	private LinearLayout			danmu_bottom_layout;
	private DanmuMenuPanel			danmu_menu_panel;

	private boolean					adjustSizeCompleted		= false;

	private BaseCacheStuffer.Proxy	mCacheStufferAdapter	= new BaseCacheStuffer.Proxy()
															{

																private Drawable mDrawable;

																@Override
																public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread)
																{
																	if (danmaku.text instanceof Spanned)
																	{																						// 根据你的条件检查是否需要需要更新弹幕
																		// FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
																		new Thread()
																		{

																			@Override
																			public void run()
																			{
																				String url = "http://www.bilibili.com/favicon.ico";
																				InputStream inputStream = null;
																				Drawable drawable = mDrawable;
																				if (drawable == null)
																				{
																					try
																					{
																						URLConnection urlConnection = new URL(url).openConnection();
																						inputStream = urlConnection.getInputStream();
																						drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
																						mDrawable = drawable;
																					}
																					catch (MalformedURLException e)
																					{
																						e.printStackTrace();
																					}
																					catch (IOException e)
																					{
																						e.printStackTrace();
																					}
																					finally
																					{
																						IOUtils.closeQuietly(inputStream);
																					}
																				}
																				if (drawable != null)
																				{
																					drawable.setBounds(0, 0, 100, 100);
																					SpannableStringBuilder spannable = createSpannable(drawable);
																					danmaku.text = spannable;
																					if (mDanmakuView != null)
																					{
																						mDanmakuView.invalidateDanmaku(danmaku, false);
																					}
																					return;
																				}
																			}
																		}.start();
																	}
																}

																@Override
																public void releaseResource(BaseDanmaku danmaku)
																{
																	// TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
																}
															};

	/**
	 * 绘制背景(自定义弹幕样式)
	 */
	private static class BackgroundCacheStuffer extends SpannedCacheStuffer
	{
		// 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
		final Paint paint = new Paint();

		@Override
		public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread)
		{
			danmaku.padding = 10; // 在背景绘制模式下增加padding
			super.measure(danmaku, paint, fromWorkerThread);
		}

		@Override
		public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top)
		{
			paint.setColor(0x8125309b);
			canvas.drawRect(left + 2, top + 2, left + danmaku.paintWidth - 2, top + danmaku.paintHeight - 2, paint);
		}

		@Override
		public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint)
		{
			// 禁用描边绘制
		}
	}

	private BaseDanmakuParser createParser(InputStream stream)
	{

		if (stream == null)
		{
			return new BaseDanmakuParser()
			{

				@Override
				protected Danmakus parse()
				{
					return new Danmakus();
				}
			};
		}

		ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

		try
		{
			loader.load(stream);
		}
		catch (IllegalDataException e)
		{
			e.printStackTrace();
		}
		BaseDanmakuParser parser = new BiliDanmukuParser();
		IDataSource<?> dataSource = loader.getDataSource();
		parser.load(dataSource);
		return parser;

	}

	private BaseDanmakuParser createParser(IDanmakuView mDanmakuView, Context context)
	{
		BaseDanmakuParser parser = new DragonDanmuParser(mDanmakuView, context);
		return parser;

	}

	Timer timer = new Timer();

	class AsyncAddTask extends TimerTask
	{

		@Override
		public void run()
		{
			for (int i = 0; i < 20; i++)
			{
				addDanmaku(true);
				SystemClock.sleep(20);
			}
		}
	};

	public void addDanmu(DanmuInfo danmuInfo)
	{
		if (mDanmakuView == null)
			return;
		BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
		if (danmaku == null || danmuInfo == null || mDanmakuView == null)
			return;
		danmaku.setTime(mDanmakuView.getCurrentTime() + 1000);
		danmaku.textSize = ScaleUtil.sp2px(activity, 18);
		danmaku.textColor = danmuInfo.getColor();
		danmaku.text = danmuInfo.getText();
		danmaku.priority = 1;
		danmaku.padding = ScaleUtil.dip2px(activity, 5);
		danmaku.isLive = true;
		if (danmuInfo.isSelf())
			danmaku.borderColor = Color.GREEN;
		mDanmakuView.addDanmaku(danmaku);
	}

	private void addDanmaku(boolean islive)
	{
		BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
		if (danmaku == null || mDanmakuView == null)
		{
			return;
		}
		// for(int i=0;i<100;i++){
		// }
		danmaku.text = "这是一条弹幕" + System.nanoTime();
		danmaku.padding = 5;
		danmaku.priority = 0; // 可能会被各种过滤器过滤并隐藏显示
		danmaku.isLive = islive;
		danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
		danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
		danmaku.textColor = Color.RED;
		danmaku.textShadowColor = Color.WHITE;
		// danmaku.underlineColor = Color.GREEN;
		danmaku.borderColor = Color.GREEN;
		mDanmakuView.addDanmaku(danmaku);

	}

	private void addDanmaKuShowTextAndImage(boolean islive)
	{
		BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
		Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
		drawable.setBounds(0, 0, 100, 100);
		SpannableStringBuilder spannable = createSpannable(drawable);
		danmaku.text = spannable;
		danmaku.padding = 5;
		danmaku.priority = 1; // 一定会显示, 一般用于本机发送的弹幕
		danmaku.isLive = islive;
		danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
		danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
		danmaku.textColor = Color.RED;
		danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
		danmaku.underlineColor = Color.GREEN;
		mDanmakuView.addDanmaku(danmaku);
	}

	private SpannableStringBuilder createSpannable(Drawable drawable)
	{
		String text = "bitmap";
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
		ImageSpan span = new ImageSpan(drawable);// ImageSpan.ALIGN_BOTTOM);
		spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		spannableStringBuilder.append("图文混排");
		spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		return spannableStringBuilder;
	}

	private void findViews()
	{

		danmu_menu_panel = (DanmuMenuPanel) findViewById(R.id.danmu_menu_panel);
		danmu_menu_panel.initDanmuMenu(activity);
		danmu_bottom_layout = (LinearLayout) findViewById(R.id.danmu_bottom_layout);
		danmu_bottom_layout.setVisibility(View.GONE);
		sendDanmuBtn = (Button) findViewById(R.id.sendDanmuBtn);
		danmu_edittext = (EditText) findViewById(R.id.danmu_edittext);
		sendDanmuBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String text = danmu_edittext.getText().toString();
				if (!SwitchUtils.mqttEnable)
					WebSocketManager.getInstance().sendDanmuMsg(text, DanmuManager.danmuFgColorIndex, TimeManager.getInstance().getCurrentLocalTime());
				else
					MqttManager.getInstance().sendDanmuMsg(text, DanmuManager.danmuFgColorIndex, TimeManager.getInstance().getCurrentLocalTime());
				danmu_edittext.setText("");
			}
		});

		setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				DanmuManager.getInstance().hideDanmuInput();
			}
		});

		// 设置最大显示行数
		HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
		maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 10); // 滚动弹幕最大显示5行
		// 设置是否禁止重叠
		HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
		overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

		mDanmakuView = (IDanmakuView) findViewById(R.id.sv_danmaku);
		if (mContext == null)
			mContext = DanmakuContext.create();
		mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
				.setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
				// .setCacheStuffer(new BackgroundCacheStuffer()) // 绘制背景使用BackgroundCacheStuffer
				.setMaximumLines(maxLinesPair)
				.preventOverlapping(overlappingEnablePair);
		if (mDanmakuView != null)
		{
			// mParser = createParser(this.getResources().openRawResource(R.raw.comments));
			mParser = createParser(mDanmakuView, activity);
			mDanmakuView.setCallback(new com.elex.chatservice.danmu.controller.DrawHandler.Callback()
			{
				@Override
				public void updateTimer(DanmakuTimer timer)
				{
				}

				@Override
				public void drawingFinished()
				{

				}

				@Override
				public void danmakuShown(BaseDanmaku danmaku)
				{
					// Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
				}

				@Override
				public void prepared()
				{
					mDanmakuView.start();
				}
			});
			mDanmakuView.setDrawingThreadType(IDanmakuView.THREAD_TYPE_MAIN_THREAD);
			mDanmakuView.prepare(mParser, mContext);
			mDanmakuView.showFPS(false);
			mDanmakuView.enableDanmakuDrawingCache(false);
		}

	}

	public void showSoftKeyBoard()
	{
		danmu_bottom_layout.setVisibility(View.VISIBLE);
		if (danmuWindowParams != null)
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			danmuWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		}
	}

	public void hideSoftKeyboard()
	{
		danmu_bottom_layout.setVisibility(View.GONE);
		if (danmuWindowParams != null)
		{

			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
			danmuWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		}
	}

	public WindowManager.LayoutParams	danmuWindowParams;
	private Activity					activity;

	public GameDamuView(Activity activity)
	{
		super(activity);
		this.activity = activity;
		initView();
	}

	public GameDamuView(Activity activity, DanmakuContext danmakuContext)
	{
		super(activity);
		this.activity = activity;
		this.mContext = danmakuContext;
		initView();
	}

	private void initView()
	{
		LayoutInflater.from(activity).inflate(R.layout.game_danmu_view, this);
		danmuWindowParams = new WindowManager.LayoutParams();
		danmuWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		danmuWindowParams.format = PixelFormat.RGBA_8888;
		danmuWindowParams.y = ScaleUtil.dip2px(activity, 20);

		danmuWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
		danmuWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		danmuWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		danmuWindowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
		findViews();

		OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				adjustHeight();
			}
		};
		getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
	}

	public double getScreenCorrectionFactor()
	{
		int density = getResources().getDisplayMetrics().densityDpi;

		if (density >= DisplayMetrics.DENSITY_XXHIGH)
		{
			return 0.8;
		}
		else
		{
			return 1.0;
		}
	}

	private final static int	sendButtonBaseWidth		= 51;
	private final static int	sendButtonBaseHeight	= 50;

	private void adjustHeight()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (!ConfigManager.getInstance().scaleFontandUI)
		{
			if (sendDanmuBtn.getWidth() != 0 && !adjustSizeCompleted)
			{
				adjustSizeCompleted = true;
			}
			return;
		}

		if (!adjustSizeCompleted && sendDanmuBtn.getWidth() != 0 && ConfigManager.scaleRatioButton > 0)
		{
			RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) sendDanmuBtn.getLayoutParams();
			layoutParams1.width = (int) (sendButtonBaseWidth * ConfigManager.scaleRatioButton);
			layoutParams1.height = (int) (sendButtonBaseHeight * ConfigManager.scaleRatioButton);
			sendDanmuBtn.setLayoutParams(layoutParams1);

			RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) danmu_edittext.getLayoutParams();
			layoutParams2.height = (int) (sendButtonBaseHeight * ConfigManager.scaleRatioButton);
			danmu_edittext.setLayoutParams(layoutParams2);

			ScaleUtil.adjustTextSize(danmu_edittext, ConfigManager.scaleRatio);
			adjustSizeCompleted = true;
		}
	}

	public void hideDanmu()
	{
		if (mDanmakuView != null)
			mDanmakuView.hide();
	}

	public void showDanmu()
	{
		if (mDanmakuView != null)
			mDanmakuView.show();
	}

}
