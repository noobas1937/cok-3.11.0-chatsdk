package com.elex.chatservice.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.RoomGroupCmd;
import com.elex.chatservice.net.IChatListener;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.adapter.UserMsgChannelAdapter;

public class MsgListActivity extends AbstractBaseActivity implements IChatListener {

	public static final int	MENU_TYPE_READ		= 0;
	public static final int	MENU_TYPE_DELETE	= 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
		showEditButton(true);
		WebSocketManager.getInstance().registerChatListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		WebSocketManager.getInstance().registerChatListener(this);
	}

	@Override
	public void openItem(ChannelListItem item)
	{
		if (item != null && item instanceof ChatChannel)
		{
			openChannel((ChatChannel) item);
		}
	}

	@Override
	public SwipeMenuCreator initSwipeMenuCreator()
	{
		return new SwipeMenuCreator()
		{
			@Override
			public void create(SwipeMenu menu)
			{
				int menuWidth = (int) (ScaleUtil.dip2px(80) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
				int menuTextSize = ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * getScreenCorrectionFactor());

				if(menu.getViewType() == UserMsgChannelAdapter.VIEW_TYPE_DELETE)
				{

					SwipeMenuItem deleteItem = new SwipeMenuItem(getBaseContext());
					deleteItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
					deleteItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
					deleteItem.setTitleSize(menuTextSize);
					deleteItem.setTitleColor(Color.WHITE);
					deleteItem.setWidth(menuWidth);
					menu.addMenuItem(deleteItem);
				}
				else if(menu.getViewType() == UserMsgChannelAdapter.VIEW_TYPE_READ_AND_DELETE)
				{

					SwipeMenuItem readItem = new SwipeMenuItem(getBaseContext());
					readItem.setBackground(new ColorDrawable(Color.rgb(0x77, 0x77, 0x77)));
					readItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
					readItem.setTitleSize(menuTextSize);
					readItem.setTitleColor(Color.WHITE);
					readItem.setWidth(menuWidth);
					menu.addMenuItem(readItem);

					SwipeMenuItem deleteItem = new SwipeMenuItem(getBaseContext());
					deleteItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
					deleteItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
					deleteItem.setTitleSize(menuTextSize);
					deleteItem.setTitleColor(Color.WHITE);
					deleteItem.setWidth(menuWidth);
					menu.addMenuItem(deleteItem);
				}
			}
		};
	}

	@Override
	public OnMenuItemClickListener initSwipeMenuItemClickListener()
	{
		return new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index)
			{
				int menuType = menu.getViewType();
				if (menuType == UserMsgChannelAdapter.VIEW_TYPE_READ_AND_DELETE)
				{
					if(index ==0)
						onReadMenuClick(position);
					else if(index == 1)
						onDeleteMenuClick(position);
				}
				else if (menuType == UserMsgChannelAdapter.VIEW_TYPE_DELETE)
				{
					if(index ==0)
						onDeleteMenuClick(position);
				}
				return true;
			}
		};

	}

	@Override
	public void comfirmOperateMutiMail(List<ChannelListItem> checkedItems, int type)
	{
		if (type == ChannelManager.OPERATION_DELETE_MUTI)
			actualDeleteChannels(checkedItems);
		else if (type == ChannelManager.OPERATION_REWARD_MUTI)
			actualRewardChannels(checkedItems);
	}

	@Override
	protected void adjustSizeExtend()
	{
		if (actionbar_editButton != null)
		{
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) actionbar_editButton.getLayoutParams();
			param.width = (int) (124 * ConfigManager.scaleRatioButton);
			param.height = (int) (48 * ConfigManager.scaleRatioButton);
			actionbar_editButton.setLayoutParams(param);
		}

		if (actionbar_returnButton != null)
		{
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) actionbar_returnButton.getLayoutParams();
			param.width = (int) (124 * ConfigManager.scaleRatioButton);
			param.height = (int) (48 * ConfigManager.scaleRatioButton);
			actionbar_returnButton.setLayoutParams(param);
		}

	}

	@Override
	public void onResume()
	{
		super.onResume();
		ChannelManager.getInstance().isInRootChannelList = true;
	}

	@Override
	protected void onDeleteMenuClick(int position)
	{
		deleteChannel(position);
	}

	@Override
	protected void onReadMenuClick(int position)
	{
		System.out.println("onReadMenuClick");
		readChannel(position);
	}

	@Override
	public void setTitleLabel()
	{
		if (StringUtils.isNotEmpty(channelId))
		{
			ChatChannel channel = ChannelManager.getInstance().getMainMsgChannelById(channelId);
			if (channel != null)
				titleLabel.setText(channel.nameText);
		}
	}

	@Override
	public void refreshScrollLoadEnabled()
	{
		LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
		if (channelListPullView.isPullLoadEnabled())
			channelListPullView.setPullLoadEnabled(false);
		if (channelListPullView.isPullRefreshEnabled())
			channelListPullView.setPullRefreshEnabled(false);

		if (adapter != null && adapter.hasMoreData())
		{
			LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG,"hasmore data");
			if (!channelListPullView.isScrollLoadEnabled())
				channelListPullView.setScrollLoadEnabled(true);
		}
		else
		{
			if (channelListPullView.isScrollLoadEnabled())
				channelListPullView.setScrollLoadEnabled(false);
		}
	}

	@Override
	public void createAdapter()
	{
		adapter = new UserMsgChannelAdapter(this, channelType, channelId);
	}

    @Override
    public void onReceiveChatCmd(RoomGroupCmd cmd, ChatChannel channel, MsgItem msgItem) {
        if (channel == null) return;
        if (cmd == null || msgItem == null){
        	reload();
        	return;
		}
        ArrayList<ChannelListItem> listItems = new ArrayList<ChannelListItem>();
        listItems.add(channel);
        switch (cmd) {
            case ROOMGROUP_QUIT:
                actualDeleteChannels(listItems);
                break;
        }
    }
}
