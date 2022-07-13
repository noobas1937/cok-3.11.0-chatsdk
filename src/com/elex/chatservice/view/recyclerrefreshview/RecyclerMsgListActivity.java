package com.elex.chatservice.view.recyclerrefreshview;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenu;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenuCreator;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.RecyclerSwipeMenuItem;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuHandle;
import com.elex.chatservice.view.recyclerrefreshview.pulltoswipeview.SwipeMenuRecyclerListView.OnMenuItemClickListener;

public class RecyclerMsgListActivity extends AbstractRecyclerActivity
{

	public static final int	MENU_TYPE_READ		= 0;
	public static final int	MENU_TYPE_DELETE	= 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		ChatServiceController.toggleFullScreen(true, true, this);
		super.onCreate(savedInstanceState);
		showEditButton(true);
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
	public RecyclerSwipeMenuCreator initSwipeMenuCreator()
	{
		return new RecyclerSwipeMenuCreator()
		{
			@Override
			public void create(RecyclerSwipeMenu menu)
			{
				if(menu.getViewType() == AbstractRecyclerAdapter.ITEM_VIEW_TYPE_SWIPE_MENU)
				{
					int menuWidth = (int) (ScaleUtil.dip2px(80) * ConfigManager.scaleRatio * getScreenCorrectionFactor());
					int menuTextSize = ScaleUtil.getAdjustTextSize(14, ConfigManager.scaleRatio * getScreenCorrectionFactor());

					RecyclerSwipeMenuItem readItem = new RecyclerSwipeMenuItem(getBaseContext());
					readItem.setBackground(new ColorDrawable(Color.rgb(0x77, 0x77, 0x77)));
					readItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.MENU_MARKASREAD));
					readItem.setTitleSize(menuTextSize);
					readItem.setTitleColor(Color.WHITE);
					readItem.setWidth(menuWidth);
					readItem.setMenuType(MENU_TYPE_READ);
					menu.addMenuItem(readItem);

					RecyclerSwipeMenuItem deleteItem = new RecyclerSwipeMenuItem(getBaseContext());
					deleteItem.setBackground(new ColorDrawable(Color.rgb(0xb4, 0x00, 0x00)));
					deleteItem.setTitle(LanguageManager.getLangByKey(LanguageKeys.DELETE));
					deleteItem.setTitleSize(menuTextSize);
					deleteItem.setTitleColor(Color.WHITE);
					deleteItem.setWidth(menuWidth);
					deleteItem.setMenuType(MENU_TYPE_DELETE);
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
			public boolean onMenuItemClick(int position, RecyclerSwipeMenu menu, int index)
			{
				RecyclerSwipeMenuItem item = menu.getMenuItem(index);
				if (item != null)
				{
					int menuType = item.getMenuType();
					if (menuType == MENU_TYPE_READ)
					{
						onReadMenuClick(position);
					}
					else if (menuType == MENU_TYPE_DELETE)
					{
						onDeleteMenuClick(position);
					}
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
		if (channelListPullView.isPullLoadEnabled())
			channelListPullView.setPullLoadEnabled(false);
		if (channelListPullView.isPullRefreshEnabled())
			channelListPullView.setPullRefreshEnabled(false);

		if (adapter != null && adapter.hasMoreData())
		{
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
		adapter = new RecyclerMsgChannelAdapter(this, channelType, channelId);
	}

	@Override
	public SwipeMenuHandle createSwipeMenuHandle()
	{
		if (adapter != null)
		{
			return new SwipeMenuHandle()
			{

				@Override
				public int[] getCurrentItemMenuTypeList(int position)
				{
					if (adapter != null)
					{
						ChannelListItem item = adapter.getItem(position);
						if (item != null)
						{
							if (item.isUnread())
								return new int[] { MENU_TYPE_READ, MENU_TYPE_DELETE };
							else
								return new int[] { MENU_TYPE_DELETE };
						}
					}
					return null;
				}
			};
		}
		return null;
	}
}
