package com.elex.chatservice.net;

import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.RoomGroupCmd;

/**
 * Created by wangyan on 2017/10/26.
 */

public interface IChatListener {
    void onReceiveChatCmd(RoomGroupCmd cmd, ChatChannel channel, MsgItem msgItem);
}
