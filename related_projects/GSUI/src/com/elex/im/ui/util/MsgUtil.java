package com.elex.im.ui.util;

import java.util.List;

import com.elex.im.core.model.Channel;
import com.elex.im.core.model.ChannelManager;
import com.elex.im.core.model.Msg;
import com.elex.im.core.util.TimeManager;
import com.elex.im.ui.GSController;

public class MsgUtil
{
	public static void handleRedPackageFinishState(Msg msg)
	{
		if (!msg.isRedPackageMessage())
			return;
		if (msg.sendState == Msg.UNHANDLE && isRedPackageFinish(msg))
		{
			msg.sendState = Msg.FINISH;
			msg.updateDB();
		}
	}

	public static boolean isRedPackageFinish(Msg msg)
	{
		if (!msg.isRedPackageMessage())
			return false;
		if (msg.createTime + GSController.red_package_during_time * 60 * 60 < TimeManager.getInstance().getCurrentTime())
			return true;
		return false;
	}

	public static boolean isDifferentDate(Msg item, List<Msg> items)
	{
		if (item == null || items == null)
			return true;
		int index = items.indexOf(item);
		Channel channel = ChannelManager.getInstance().getChannel(GSController.getCurrentChannelType());
		if (channel != null && channel.getMsgIndexArrayForTimeShow() != null && channel.getMsgIndexArrayForTimeShow().size() > 0)
		{
			if (channel.getMsgIndexArrayForTimeShow().contains(Integer.valueOf(index)))
				return true;
		}
		else
		{
			if (index == 0)
			{
				return true;
			}
			else if (index > 0 && items.get(index - 1) != null)
			{
				return !item.getSendTimeYMD().equals(items.get(index - 1).getSendTimeYMD());
			}
		}

		return false;
	}

	public static void setVoiceRecordReadState(Msg item)
	{
		if (item.isAudioMessage() && !item.isSelfMsg())
		{
			item.sendState = Msg.VOICE_READ;
			item.updateDB();
		}
	}
}
