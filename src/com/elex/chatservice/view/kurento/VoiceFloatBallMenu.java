package com.elex.chatservice.view.kurento;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.util.ScaleUtil;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceFloatBallMenu implements IMenu {
    private TextView tvLeftCenter, tvRightCenter, tvLeftGift, tvRightGift;
    private View leftLine, rightLine;
    private int menuWidth, menuHeight;
    private VoiceFloatBall mFloatBall;
    private TextView leftMessageTip, rightMessageTip;
    private LayoutInflater layoutInflater; 

    @Override
    public void onAttach(VoiceFloatBall floatBall, Context context) {
    	menuWidth = (int)(ScaleUtil.getScreenWidth()*0.8f);
//        menuWidth = ScaleUtil.dip2px(context,135);
        menuHeight = ScaleUtil.dip2px(context,30);
        this.mFloatBall = floatBall;
    }

    @Override
    public void addMenu(RelativeLayout parent) {
        //设置菜单的背景色
//        parent.setBackgroundColor(Color.parseColor("#fafafa"));
        parent.setBackgroundColor(Color.TRANSPARENT);
//        addLeftMenu(parent);
//        addRightMenu(parent);
        addRightMessageTip(parent);
        addLeftMessageTip(parent);

        showLeftVoiceTip(false);
        showRightVoiceTip(false);
        
//        //默认状态下隐藏菜单
//        showLeft(false);
//        showRight(false);
    }
    
    private void addLeftMessageTip(RelativeLayout parent) {
        final Context context = parent.getContext();
        RelativeLayout.LayoutParams childLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        childLayoutParams.setMargins(0, 0, ScaleUtil.dip2px(parent.getContext(), 20), 0);
        childLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if(layoutInflater == null)
        	layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(layoutInflater!=null)
        {
        	View leftTipView = layoutInflater.inflate(R.layout.realtime_voice_tip_right, null);
        	leftMessageTip = (TextView) leftTipView.findViewById(R.id.voice_tip);
        	if(ConfigManager.scaleRatio>0)
        		ScaleUtil.adjustTextSize(leftMessageTip, ConfigManager.scaleRatio);
        	leftMessageTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toast(context, "左边的");
                    hideMenu();
                }
            });
        	parent.addView(leftTipView, childLayoutParams);
        }
    }
    
    private void addRightMessageTip(RelativeLayout parent) {
        final Context context = parent.getContext();
        RelativeLayout.LayoutParams childLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        childLayoutParams.setMargins(ScaleUtil.dip2px(parent.getContext(), 20), 0, 0, 0);
        childLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if(layoutInflater == null)
        	layoutInflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(layoutInflater!=null)
        {
        	View rightTipView = layoutInflater.inflate(R.layout.realtime_voice_tip_left, null);
        	rightMessageTip = (TextView) rightTipView.findViewById(R.id.voice_tip);
        	if(ConfigManager.scaleRatio>0)
        		ScaleUtil.adjustTextSize(rightMessageTip, ConfigManager.scaleRatio);
        	rightMessageTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toast(context, "右边的");
                    hideMenu();
                }
            });
        	parent.addView(rightTipView, childLayoutParams);
        }
    }

    private void addLeftMenu(RelativeLayout parent) {
        final Context context = parent.getContext();
        RelativeLayout.LayoutParams childLayoutParams = new RelativeLayout.LayoutParams(ScaleUtil.dip2px(parent.getContext(), 52), ScaleUtil.dip2px(parent.getContext(), 30));
        childLayoutParams.setMargins(0, 0, ScaleUtil.dip2px(parent.getContext(), 20), 0);
        childLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tvLeftGift = new TextView(parent.getContext());
        tvLeftGift.setId(getId());
        tvLeftGift.setText("福利");
        tvLeftGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(context, "福利");
                hideMenu();
            }
        });
        tvLeftGift.setTextSize(14);
        tvLeftGift.setGravity(Gravity.CENTER);
        tvLeftGift.setTextColor(Color.parseColor("#333333"));
        parent.addView(tvLeftGift, childLayoutParams);
        childLayoutParams = new RelativeLayout.LayoutParams(ScaleUtil.dip2px(parent.getContext(), 1), ScaleUtil.dip2px(parent.getContext(), 30));
        childLayoutParams.setMargins(0, ScaleUtil.dip2px(parent.getContext(), 8), 0, ScaleUtil.dip2px(parent.getContext(), 8));
        childLayoutParams.addRule(RelativeLayout.LEFT_OF, tvLeftGift.getId());
        leftLine = new View(parent.getContext());
        leftLine.setId(getId());
        leftLine.setBackgroundColor(Color.parseColor("#e5e5e5"));
        parent.addView(leftLine, childLayoutParams);

        childLayoutParams = new RelativeLayout.LayoutParams(ScaleUtil.dip2px(parent.getContext(), 52), ScaleUtil.dip2px(parent.getContext(), 30));
        childLayoutParams.addRule(RelativeLayout.LEFT_OF, leftLine.getId());
        tvLeftCenter = new TextView(parent.getContext());
        tvLeftCenter.setId(getId());
        tvLeftCenter.setText("我的");
        tvLeftCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(context, "我的");
                hideMenu();
            }
        });
        tvLeftCenter.setTextSize(14);
        tvLeftCenter.setGravity(Gravity.CENTER);
        tvLeftCenter.setTextColor(Color.parseColor("#333333"));
        parent.addView(tvLeftCenter, childLayoutParams);
    }

    private void toast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    private void addRightMenu(RelativeLayout parent) {
        final Context context = parent.getContext();
        RelativeLayout.LayoutParams childLayoutParams = new RelativeLayout.LayoutParams(ScaleUtil.dip2px(parent.getContext(), 52), ScaleUtil.dip2px(parent.getContext(), 30));
        childLayoutParams.setMargins(ScaleUtil.dip2px(parent.getContext(), 20), 0, 0, 0);
        tvRightCenter = new TextView(parent.getContext());
        tvRightCenter.setId(getId());
        tvRightCenter.setText("我的");
        tvRightCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(context, "我的");
                hideMenu();
            }
        });
        tvRightCenter.setTextSize(14);
        tvRightCenter.setGravity(Gravity.CENTER);
        tvRightCenter.setTextColor(Color.parseColor("#333333"));
        parent.addView(tvRightCenter, childLayoutParams);

        childLayoutParams = new RelativeLayout.LayoutParams(ScaleUtil.dip2px(parent.getContext(), 1), ScaleUtil.dip2px(parent.getContext(), 30));
        childLayoutParams.setMargins(0, ScaleUtil.dip2px(parent.getContext(), 8), 0, ScaleUtil.dip2px(parent.getContext(), 8));
        childLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvRightCenter.getId());
        rightLine = new View(parent.getContext());
        rightLine.setId(getId());
        rightLine.setBackgroundColor(Color.parseColor("#e5e5e5"));
        parent.addView(rightLine, childLayoutParams);

        childLayoutParams = new RelativeLayout.LayoutParams(ScaleUtil.dip2px(parent.getContext(), 52), ScaleUtil.dip2px(parent.getContext(), 30));
        childLayoutParams.addRule(RelativeLayout.RIGHT_OF, rightLine.getId());
        tvRightGift = new TextView(parent.getContext());
        tvRightGift.setId(getId());
        tvRightGift.setText("福利");
        tvRightGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(context, "福利");
                hideMenu();
            }
        });
        tvRightGift.setTextSize(14);
        tvRightGift.setGravity(Gravity.CENTER);
        tvRightGift.setTextColor(Color.parseColor("#333333"));
        parent.addView(tvRightGift, childLayoutParams);
    }

    private void hideMenu() {
//        mFloatBall.hideMenuImmediately();
        mFloatBall.hideMenu();
    }

    @Override
    public boolean isRightMenuEnable() {
        return true;
    }

    @Override
    public boolean isLeftMenuEnable() {
        return true;
    }

    @Override
    public void showingRightMenu() {
//        showRight(false);
//        showLeft(true);
        showRightVoiceTip(false);
        showLeftVoiceTip(true);
    }

    @Override
    public void showingLeftMenu() {
//        showRight(true);
//        showLeft(false);
        showRightVoiceTip(true);
        showLeftVoiceTip(false);
    }

    @Override
    public int getMenuHeight() {
        return menuHeight;
    }

    @Override
    public int getMenuWidth() {
        return menuWidth;
    }

    private void showRight(boolean show) {
        tvLeftCenter.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        tvLeftGift.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        leftLine.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLeft(boolean show) {
        tvRightCenter.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        tvRightGift.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        rightLine.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }
    
    private void showRightVoiceTip(boolean show) {
        leftMessageTip.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showLeftVoiceTip(boolean show) {
    	rightMessageTip.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private int getId() {
        return IDFactory.getId();
    }

	@Override
	public void setMenuTip(String tip)
	{
		if(leftMessageTip!=null)
			leftMessageTip.setText(tip);
		if(rightMessageTip!=null)
			rightMessageTip.setText(tip);
	}
}
