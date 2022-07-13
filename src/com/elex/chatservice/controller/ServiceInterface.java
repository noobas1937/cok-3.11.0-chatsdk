package com.elex.chatservice.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.elex.chatservice.R;
import com.elex.chatservice.model.ChannelListItem;
import com.elex.chatservice.model.ChannelMainListInfo;
import com.elex.chatservice.model.ChannelManager;
import com.elex.chatservice.model.ChatChannel;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.DriftingBottleContent;
import com.elex.chatservice.model.EmojSubscribeManager;
import com.elex.chatservice.model.FlyMutiRewardInfo;
import com.elex.chatservice.model.FriendLatestMail;
import com.elex.chatservice.model.LanguageItem;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.LatestChannelChatInfo;
import com.elex.chatservice.model.LatestChatInfo;
import com.elex.chatservice.model.LocalConfig;
import com.elex.chatservice.model.MailAudioContent;
import com.elex.chatservice.model.MailManager;
import com.elex.chatservice.model.MsgItem;
import com.elex.chatservice.model.NearByManager;
import com.elex.chatservice.model.StickManager;
import com.elex.chatservice.model.TimeManager;
import com.elex.chatservice.model.TranslateManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.ChatTable;
import com.elex.chatservice.model.db.DBDefinition;
import com.elex.chatservice.model.db.DBManager;
import com.elex.chatservice.model.kurento.WebRtcPeerManager;
import com.elex.chatservice.model.mail.MailData;
import com.elex.chatservice.model.mail.battle.BattleMailData;
import com.elex.chatservice.model.mail.updatedata.MailUpdateData;
import com.elex.chatservice.model.shareExtra.MediaContent;
import com.elex.chatservice.model.shareExtra.ShareMsgExtra;
import com.elex.chatservice.mqtt.MqttManager;
import com.elex.chatservice.net.WebSocketManager;
import com.elex.chatservice.net.WebSocketStatusHandler;
import com.elex.chatservice.net.XiaoMiToolManager;
import com.elex.chatservice.util.FileVideoUtils;
import com.elex.chatservice.util.LogUtil;
import com.elex.chatservice.util.LoginShareDataUtil;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.util.SharePreferenceUtil;
import com.elex.chatservice.util.TranslatedByLuaResult;
import com.elex.chatservice.view.AllianceShareActivity;
import com.elex.chatservice.view.ChannelListActivity;
import com.elex.chatservice.view.ChannelListFragment;
import com.elex.chatservice.view.ChatActivity;
//import com.elex.chatservice.view.ChatFragmentActivity;
import com.elex.chatservice.view.ChatFragmentNew;
import com.elex.chatservice.view.ChatRoomNameModifyActivity;
import com.elex.chatservice.view.ChatRoomSettingActivity;
//import com.elex.chatservice.view.ChatViewFragment;
import com.elex.chatservice.view.ForumActivity;
import com.elex.chatservice.view.ForumFragment;
import com.elex.chatservice.view.ICocos2dxScreenLockListener;
import com.elex.chatservice.view.MemberSelectorActivity;
import com.elex.chatservice.view.MsgActivity;
import com.elex.chatservice.view.MsgListActivity;
import com.elex.chatservice.view.PlayVideoActivity;
import com.elex.chatservice.view.RecordVideoActivity;
import com.elex.chatservice.view.WriteMailActivity;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareCommentListActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareDetailActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareListActivity;
import com.elex.chatservice.view.allianceshare.ImageBucketChooseActivity;
import com.elex.chatservice.view.allianceshare.ImageChooseActivity;
import com.elex.chatservice.view.allianceshare.ImagePagerActivity;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.autoscroll.ScrollTextManager;
import com.elex.chatservice.view.banner.BannerInfo;
import com.elex.chatservice.view.danmu.DanmuService;
import com.elex.chatservice.view.danmu.DanmuService.DanmuBinder;
import com.elex.chatservice.view.emoj.EmojSubscribActivity;
import com.elex.chatservice.view.kurento.RealtimeVoiceRoomSettingActivity;
import com.elex.chatservice.view.kurento.RealtimeVoiceService;
import com.elex.chatservice.view.kurento.RealtimeVoiceService.RealtimeVoiceBinder;
import com.elex.chatservice.view.lbs.NearByActivity;
import com.elex.chatservice.view.recyclerrefreshview.AbstractRecyclerActivity;
import com.elex.chatservice.view.recyclerrefreshview.RecyclerMainListActivity;
import com.elex.chatservice.view.recyclerrefreshview.RecyclerMsgListActivity;
import com.elex.chatservice.view.recyclerrefreshview.RecyclerSysMailActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ServiceInterface {
    public static String allianceIdJoining;
    private static boolean oldHornMsgPushed = false;
    private static boolean oldBattleHornMsgPushed = false;

    public static void onJoinAnnounceInvitationSuccess() {
        UserManager.getInstance().getCurrentUser().allianceId = allianceIdJoining;
        ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getChatFragment() != null) {
                        ChatServiceController.getChatFragment().onJoinAnnounceInvitationSuccess();
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public static void postCrossFrightActivityIsStart(boolean isKingdomBattleStart, boolean isAnicientBattleStart, int serverId) {
        System.out.println("postCrossFrightActivityIsStart:" + isKingdomBattleStart + "   isAnicientBattleStart:" + isAnicientBattleStart + "   kingdomBattleEnemyServerId:" + serverId);
        ChatServiceController.isKingdomBattleStart = isKingdomBattleStart;
        ChatServiceController.isAnicientBattleStart = isAnicientBattleStart;
        ChatServiceController.kingdomBattleEnemyServerId = serverId;

    }

    public static void postDragonActivityStart(boolean isDragonBattleStart, String dragonRoomId) {
        System.out.println("postDragonActivityStart:" + isDragonBattleStart + "   dragonRoomId:" + dragonRoomId);
        ChatServiceController.isDragonBattleStart = isDragonBattleStart;
        if (StringUtils.isNotEmpty(dragonRoomId) && !dragonRoomId.equals("dragon_"))
            ChatServiceController.dragonRoomId = dragonRoomId;
    }

    public static void postDragonPlayOffStart(boolean isDragonPlayOffStart, String dragonRoomId) {
        System.out.println("postDragonPlayOffStart:" + isDragonPlayOffStart + "   dragonRoomId:" + dragonRoomId);
        ChatServiceController.isDragonPlayOffStart = isDragonPlayOffStart;
        if (StringUtils.isNotEmpty(dragonRoomId) && !dragonRoomId.equals("dragon_"))
            ChatServiceController.dragonRoomId = dragonRoomId;
    }

    public static void joinDragonObserverRoom(String dragonRoomId) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "dragonRoomId", dragonRoomId);
        if (StringUtils.isNotEmpty(dragonRoomId)) {
            ChatServiceController.dragonObserverRoomId = dragonRoomId;
            if (!SwitchUtils.mqttEnable)
                WebSocketManager.getInstance().joinDragonObserverRoomWrap();
            else
                MqttManager.getInstance().joinDragonObserverRoomWrap();
        }
    }

    public static void setDragonObserverStatus(boolean isInDragonObserver, String dragonObserverRoomId) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "isInDragonObserver", isInDragonObserver, "dragonObserverRoomId", dragonObserverRoomId);

        if (StringUtils.isNotEmpty(dragonObserverRoomId)) {
            ChatServiceController.dragonObserverRoomId = WebSocketManager.GROUP_DRAGON_OBSERVER + "_" + dragonObserverRoomId;
            ChatServiceController.dragonObserverDanmuRoomId = WebSocketManager.GROUP_DRAGON_DANMU + "_" + dragonObserverRoomId;
            ChatServiceController.isInDragonObserverRoom = isInDragonObserver;
        } else {
            ChatServiceController.dragonObserverRoomId = "";
            ChatServiceController.dragonObserverDanmuRoomId = "";
            ChatServiceController.isInDragonObserverRoom = false;
            DanmuManager.getInstance().clear();
        }
    }

    public static void leaveDragonObserverRoom() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
        if (!SwitchUtils.mqttEnable)
            WebSocketManager.getInstance().leaveDragonObserverRoom();
        else
            MqttManager.getInstance().leaveDragonObserverRoom();
    }

    public static void setGameLanguage(String gameLanguage, String xmlVersion, boolean isRTLEnable) {
        System.out.println("setGameLanguage:" + gameLanguage + "   isRTLEnable:" + isRTLEnable);
        ConfigManager.getInstance().gameLang = gameLanguage;
        ConfigManager.getInstance().xmlVersion = xmlVersion;
        ConfigManager.getInstance().isRTLEnable = isRTLEnable;
    }

    public static void toggleFullScreen(final boolean enabled) {
        ChatServiceController.toggleFullScreen(enabled, true, ChatServiceController.hostActivity);
    }

    public static void setMailInfo(String mailFromUid, String mailUid, String mailName, int mailType) {

        LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "mailType", mailType);
        UserManager.getInstance().getCurrentMail().opponentUid = mailFromUid;
        UserManager.getInstance().getCurrentMail().mailUid = mailUid;
        UserManager.getInstance().getCurrentMail().opponentName = mailName;
        UserManager.getInstance().getCurrentMail().type = mailType;
        LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "opponentUid", mailFromUid);
        if (mailType == MailManager.MAIL_MOD_PERSONAL || mailType == MailManager.MAIL_MOD_SEND) {
            ChatServiceController.contactMode = 1;
            if (!mailFromUid.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD)) {
                UserManager.getInstance().getCurrentMail().opponentUid = mailFromUid + DBDefinition.CHANNEL_ID_POSTFIX_MOD;
                LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "opponentUid", UserManager.getInstance().getCurrentMail().opponentUid);
            }
        } else if (mailType == MailManager.MAIL_DRIFTING_BOTTLE_SELF_SEND || mailType == MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND)
            ChatServiceController.contactMode = 2;
        else {
            ChatServiceController.contactMode = 0;
            LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "ChatServiceController.contactMode", ChatServiceController.contactMode);
        }

    }

    public static void setMailInfo(int contatctMode, String mailFromUid, String mailName) {
        LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_DEBUG, "contaceMode", contatctMode);
        UserManager.getInstance().getCurrentMail().opponentUid = mailFromUid;
        UserManager.getInstance().getCurrentMail().opponentName = mailName;
        ChatServiceController.contactMode = contatctMode;
    }

    public static void setContactModState() {
        // ChatServiceController.isContactMod = true;
    }

    public static void setContactMode(int mode) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mode", mode);
        ChatServiceController.contactMode = mode;
    }

    public static void resetPlayerFirstJoinAlliance() {
        ConfigManager.getInstance().isFirstJoinAlliance = false;
        // ServiceInterface.resetPlayerIsInAlliance();
    }

    public static void postNoMoreMessage(int channelType) {
        if (ChatServiceController.getChatFragment() != null) {
            ChatServiceController.getChatFragment().resetMoreDataStart(channelType, "");
        }
    }

    public static void deleteChatRoom(String groupId) {
        ChannelManager.getInstance().deleteChatroomChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, groupId));
    }

    public static void deleteMail(String id, int channelType, int type) {
        List<ChatChannel> channelList = new ArrayList<ChatChannel>();
        if (channelType == DBDefinition.CHANNEL_TYPE_USER || channelType == DBDefinition.CHANNEL_TYPE_CHATROOM) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, id);
            if (channel == null)
                return;
            ChannelManager.getInstance().deleteChannel(channel);
            ChannelManager.getInstance().postMainChannelChangedToGame(channel);
        } else if (channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && ChatServiceController.isNewMailListEnable) {
            if (type == MailManager.MAIL_RESOURCE || (type == MailManager.MAIL_RESOURCE_HELP && ChatServiceController.isNewMailUIEnable)
                    || type == MailManager.MAIL_ATTACKMONSTER || id.equals("knight") || type == MailManager.MAIL_NEW_WORLD_BOSS) {
                String channelId = "";
                if (type == MailManager.MAIL_RESOURCE)
                    channelId = MailManager.CHANNELID_RESOURCE;
                else if (type == MailManager.MAIL_RESOURCE_HELP)
                    channelId = MailManager.CHANNELID_RESOURCE_HELP;
                else if (type == MailManager.MAIL_ATTACKMONSTER)
                    channelId = MailManager.CHANNELID_MONSTER;
                else if (id.equals("knight"))
                    channelId = MailManager.CHANNELID_KNIGHT;
                else if (type == MailManager.MAIL_NEW_WORLD_BOSS)
                    channelId = MailManager.CHANNELID_NEW_WORLD_BOSS;
                ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
                if (channel != null) {
                    channel.clearAllSysMail();
                    if (!channelList.contains(channel))
                        channelList.add(channel);
                    ChannelManager.getInstance().parseChannelList(channelList);
                }
            } else {
                MailData mail = DBManager.getInstance().getSysMailByID(id);
                if (mail != null) {
                    ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
                    if (channel == null)
                        return;
                    channel.updateSysMailCountFromDB(-1);
                    ChannelManager.getInstance().deleteSysMailFromChannel(channel, mail, false);
                    if (!channelList.contains(channel))
                        channelList.add(channel);
                    ChannelManager.getInstance().parseChannelList(channelList);
                }
            }

            if (ChatServiceController.getChannelListFragment() != null)
                ChatServiceController.getChannelListFragment().notifyDataSetChanged();
        }
    }

    public static void setChannelMemberArray(int channelType, String fromUid, String uidStr, String roomName) {
        ChannelManager.getInstance().setChannelMemberArray(fromUid, uidStr, roomName);

        if (ChatServiceController.getChatRoomSettingActivity() != null)
            ChatServiceController.getChatRoomSettingActivity().refreshData();

        if (ChatServiceController.hostActivity == null || channelType != DBDefinition.CHANNEL_TYPE_CHATROOM)
            return;
        ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getChatFragment() != null) {
                        if (!ChatServiceController.getChatFragment().isSelectMemberBtnEnable()) {
                            ChatServiceController.getChatFragment().refreshMemberSelectBtn();
                            ChatServiceController.getChatFragment().setSelectMemberBtnState();
                        }
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public static void updateChannelMemberArray(int channelType, String fromUid, String uidStr, boolean isAdd, boolean needModifyName) {
        ChannelManager.getInstance().updateChannelMemberArray(fromUid, uidStr, isAdd);

        if (ChatServiceController.getChatRoomSettingActivity() != null)
            ChatServiceController.getChatRoomSettingActivity().refreshData();

        if (needModifyName) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_CHATROOM, fromUid));
            if (channel != null) {
                String newName = UserManager.getInstance().createNameStr(channel.memberUidArray);
                notifyChatRoomNameChanged(newName);
                channel.customName = newName;
                DBManager.getInstance().updateChannel(channel);
            }
        }

        if (ChatServiceController.hostActivity == null || channelType != DBDefinition.CHANNEL_TYPE_CHATROOM || !isAdd)
            return;
        ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getChatFragment() != null) {
                        if (!ChatServiceController.getChatFragment().isSelectMemberBtnEnable()) {
                            ChatServiceController.getChatFragment().refreshMemberSelectBtn();
                            ChatServiceController.getChatFragment().setSelectMemberBtnState();
                        }
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public static void setChatRoomFounder(String groupId, String founderUid) {
        ChannelManager.getInstance().setChatRoomFounder(groupId, founderUid);
    }

    public static void setChatHorn(boolean enableChatHorn) {
        ConfigManager.enableChatHorn = enableChatHorn;
    }

    private static void save2DB(MsgItem[] infoArr, final int channelType, final String channelId, String customName) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelType", channelType, "channelId", channelId, "size",
                infoArr.length);

        ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
        if (infoArr.length == 0 || channel == null)
            return;

        if (StringUtils.isNotEmpty(customName) && ChannelManager.isUserChannelType(channelType))
            channel.customName = customName;

        DBManager.getInstance().insertMessages(infoArr, channel.getChatTable());
    }

    /**
     * 获取多条历史消息时，此函数会被多次调用（具体次数由MailCell、MailController决定），多次刷新界面（游戏中看不出来）
     *
     * @param channelId  如果是邮件则总是指对方的uid，如果是聊天室为uid，如果是聊天fromUid为"0"
     * @param customName 如果是邮件则总是指对方的name，如果是聊天室为自定义名称，如果是聊天为""
     */
    public static void notifyMessageIndex(int chatInfoIndex, String channelId, String customName, int contactMode) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelId", channelId, "customName", customName, "contactMode", contactMode);
        if (chatInfoIndex < 0 || StringUtils.isEmpty(channelId))
            return;
        final Object[] chatInfoArr = ChatServiceController.getInstance().host.getChatInfoArray(chatInfoIndex, channelId);

        if (contactMode == 1 && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
            channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
        else if (contactMode == 2 && !channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
            channelId += DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE;

        if (chatInfoArr == null || chatInfoArr.length <= 0)
            return;

        MsgItem[] _itemArray = new MsgItem[chatInfoArr.length];
        for (int i = 0; i < chatInfoArr.length; i++) {
            Object obj = chatInfoArr[i];
            if (obj != null) {
                _itemArray[i] = (MsgItem) obj;
            }
        }

        if ((_itemArray[0].channelType != DBDefinition.CHANNEL_TYPE_USER && WebSocketManager.isRecieveFromWebSocket(_itemArray[0].channelType)) || contactMode == 3)
            return;

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "_itemArray[0].channelType", _itemArray[0].channelType, "_itemArray[0].msg", _itemArray[0].msg);

        handleMessage(_itemArray, channelId, customName, true, true);
    }

    // public static void openDriftingBottle(String senderUid)
    // {
    // LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG,"senderUid",senderUid);
    // if (StringUtils.isEmpty(senderUid))
    // return;
    //
    // String channelId = senderUid;
    // if (!channelId.endsWith(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
    // channelId += DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE;
    // if(ConfigManager.useWebSocketServer && ConfigManager.pm_standalone_read && !channelId.startsWith(DBDefinition.CHANNEL_ID_PREFIX_STANDALONG) &&
    // (ChannelManager.getInstance().userChatChannelInDB==null || (ChannelManager.getInstance().userChatChannelInDB!=null &&
    // !ChannelManager.getInstance().userChatChannelInDB.contains(channelId))))
    // channelId = DBDefinition.CHANNEL_ID_PREFIX_STANDALONG+channelId;
    // ChatTable chatTable = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_USER, channelId);
    // ChatChannel channel = ChannelManager.getInstance().getChannelFromMemory(chatTable);
    // LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG,"channel!=null",channel!=null);
    // if(channel!=null)
    // LogUtil.printVariablesWithFuctionName(Log.INFO,
    // LogUtil.TAG_DEBUG,"channel.isTempDriftBottleChannel()",channel.isTempDriftBottleChannel(),"channel.msgList == null",channel.msgList == null);
    // if(channel.msgList != null)
    // LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG,"channel.msgList.size()",channel.msgList.size());
    // if(channel!=null && !channel.isTempDriftBottleChannel() && (channel.msgList == null || channel.msgList.size()==0))
    // {
    // channel.settings = "0";
    // if (StringUtils.isNotEmpty(senderName))
    // channel.customName = senderName;
    // firstMsgItem.initUserForReceivedMsg(channelId, senderName);
    // if (firstMsgItem.hasTranslated())
    // firstMsgItem.hasTranslated = true;
    // else
    // firstMsgItem.hasTranslated = false;
    // boolean isAddSuccess = channel.addHistoryMsg(firstMsgItem);
    // LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG,"isAddSuccess",isAddSuccess);
    // if (isAddSuccess)
    // channel.getLoadedTimeNeedShowMsgIndex(1);
    // }
    // setMailInfo(channelId, "", senderName, MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND);
    // showChatActivityFrom2dx(0, DBDefinition.CHANNEL_TYPE_USER, 1, false, true, false);
    // if (ChatServiceController.getChatFragment() != null)
    // ChatServiceController.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_USER, channel.channelID, false);
    // }

    public static synchronized MailData parseMailData(MailData mailData, boolean isFromDB) {
        MailData mail = null;
        try {
            mail = MailManager.getInstance().parseMailDataContent(mailData);
            boolean needUpdateDB = false;
            if (isFromDB) {
                boolean needParsedBefore = mailData.needParseContent();
                boolean needParsedNow = mail.needParseContent();
                needUpdateDB = needParsedBefore && !needParsedNow;
            }
            return handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail, isFromDB, needUpdateDB);
        } catch (Exception e) {
            LogUtil.printException(e);
        }
        return null;
    }

    private static ArrayList<Integer> mailDataIndexArray = new ArrayList<Integer>();

    public static void notifyMailDataIndex(final int mailDataIndex, boolean isGetNew) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "mailDataIndex", mailDataIndex, "isGetNew", isGetNew);
        if (isGetNew) {
            // mailDataIndexArray.add(Integer.valueOf(mailDataIndex));

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!MailManager.cocosMailListEnable)
                            handleMailDataIndexForGetNew(mailDataIndex);
                        else
                            handleMailDataIndexForGetNewData(mailDataIndex);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            };
            MailManager.getInstance().runOnExecutorService(run);
        } else {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!MailManager.cocosMailListEnable)
                            handleMailDataIndex(mailDataIndex, false);
                        else
                            handleMailDataFromGame(mailDataIndex);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            };
            MailManager.getInstance().runOnExecutorService(run);
        }
    }

    public static boolean isHandlingGetNewMailMsg = false;

    public static void handleGetNewMailMsg(final String channelInfo) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                synchronized (ServiceInterface.class) {
                    isHandlingGetNewMailMsg = true;

                    for (int i = 0; i < mailDataIndexArray.size(); i++) {
                        handleMailDataIndex(mailDataIndexArray.get(i).intValue(), true);
                    }
                    mailDataIndexArray.clear();

                    postChannelInfo(channelInfo);

                    isHandlingGetNewMailMsg = false;
                    if (ChatServiceController.getChannelListFragment() != null)
                        ChatServiceController.getChannelListFragment().refreshTitle();
                }
            }
        };

        Thread thread = new Thread(run);
        thread.start();
    }

    private static synchronized void handleMailDataIndex(final int mailDataIndex, boolean isGetNew) {
        if (mailDataIndex < 0)
            return;

        final Object[] mailDataArr = ChatServiceController.getInstance().host.getMailDataArray(mailDataIndex);
        if (mailDataArr == null)
            return;

        boolean hasDetectMail = false;
        String channelId = "";
        boolean hasOldNewBattleMail = false;
        boolean hasOldNewDetectMail = false;

        for (int i = 0; i < mailDataArr.length; i++) {
            MailData mailData = (MailData) mailDataArr[i];
            if (mailData == null)
                continue;
            if (!hasDetectMail && mailData.getType() == MailManager.MAIL_DETECT_REPORT)
                hasDetectMail = true;
            if (mailData.isUnread()) {
                if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mailData.getUid()) == 1)
                    hasOldNewBattleMail = true;
                else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mailData.getUid()) == 2)
                    hasOldNewDetectMail = true;
            }
            if (!isGetNew) {
                final MailData mail = parseMailData(mailData, false);
                if (mail != null) {
                    if (StringUtils.isEmpty(channelId))
                        channelId = mail.getChannelId();
                    final ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
                    if (channel != null
                            && (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_MONSTER))) {
                        int createTime = DBManager.getInstance().getEarlistDeleteableMailCreateTime(channel.getSysMailCountInDB(),
                                channel.unreadCount, channelId);
                        if (createTime != -1)
                            ChannelManager.getInstance().deleteSysMailFromCreateTime(channel, createTime);
                    }
                    ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (channel != null) {
                                    // 仅在非getNew才add的目的是避免首次登陆就把所有新邮件全部加载到channel中（可能很耗时间）
                                    channel.addNewMailData(mail);
                                }
                                ChannelManager.getInstance().calulateAllChannelUnreadNum();
                            } catch (Exception e) {
                                LogUtil.printException(e);
                            }
                        }
                    });
                }

            } else {
                mailData.parseMailTypeTab();
                ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
                if (channel != null) {
                    DBManager.getInstance().insertMailData(mailData, channel);
                    channel.refreshRenderData();
                    channel.updateSysMailCountFromDB(1);
                    if (mailData.isUnread())
                        channel.updateUnreadSysMailCountFromDB(1);
                    if (ChannelManager.getInstance().needParseFirstChannel(channel.channelID))
                        ChannelManager.getInstance().parseFirstChannelID();
                }
            }
        }

        // 仅在C++的三种list型邮件面板打开的情况下，才需要刷新它（而getNew时肯定是关闭的）
        if (StringUtils.isNotEmpty(ChannelManager.currentOpenedChannel) && ChannelManager.currentOpenedChannel.equals(channelId))
            ChannelManager.getInstance().postNotifyPopup(channelId);

        if (hasOldNewBattleMail)
            DBManager.getInstance().getLatestUnReadReportByType(1);
        if (hasOldNewDetectMail)
            DBManager.getInstance().getLatestUnReadReportByType(2);

        if (hasDetectMail)
            DBManager.getInstance().getDetectMailInfo();
        ChannelListFragment.onMailAdded();
    }

    private static void handleMailDataFromGame(final int mailDataIndex) {
        if (mailDataIndex < 0)
            return;

        final Object[] mailDataArr = ChatServiceController.getInstance().host.getMailDataArray(mailDataIndex);
        if (mailDataArr == null)
            return;

        boolean hasDetectMail = false;
        String channelId = "";
        boolean hasOldNewBattleMail = false;
        boolean hasOldNewDetectMail = false;

        if (mailDataArr[0] != null) {
            MailData firstMail = MailManager.getInstance().parseMailDataContent((MailData) mailDataArr[0]);
            channelId = firstMail.getChannelId();
        }

        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
        if (channel == null)
            return;

        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channel.channelID", channel.channelID);

        List<MailData> list = new ArrayList<MailData>();

        for (int i = 0; i < mailDataArr.length; i++) {
            MailData mailData = (MailData) mailDataArr[i];
            if (mailData == null)
                continue;
            if (!hasDetectMail && mailData.getType() == MailManager.MAIL_DETECT_REPORT)
                hasDetectMail = true;
            if (mailData.isUnread()) {
                if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mailData.getUid()) == 1)
                    hasOldNewBattleMail = true;
                else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mailData.getUid()) == 2)
                    hasOldNewDetectMail = true;
            }

            if (!channel.isDialogChannel()) {
                mailData = MailManager.getInstance().parseMailDataContent(mailData);
                mailData = handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData, false, false);
            }
            channel.addInLoadMailUidList(mailData.getUid());
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailData.getUid", mailData.getUid(), "channel.channelID", channel.channelID, "createTime", mailData.getCreateTime());
            list.add(mailData);
        }

        if (channel.isDialogChannel()) {
            MailData mail = MailManager.getInstance().createDialogSystemMail(channel, list, false);
            MailManager.getInstance().transportMailDataForGame(mail);
        } else {
            try {
                String jsonStr = JSON.toJSONString(list);
                MailManager.getInstance().transportMailArray(jsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_MONSTER)) {
            int createTime = DBManager.getInstance().getEarlistDeleteableMailCreateTime(channel.getSysMailCountInDB(),
                    channel.unreadCount, channelId);
            if (createTime != -1)
                ChannelManager.getInstance().deleteSysMailFromCreateTime(channel, createTime);
        }

        List<ChatChannel> channelList = new ArrayList<ChatChannel>();
        if (!channelList.contains(channel))
            channelList.add(channel);

        ChannelManager.getInstance().calulateAllChannelUnreadNum();
        ChannelManager.getInstance().parseChannelList(channelList);

        if (hasOldNewBattleMail)
            DBManager.getInstance().getLatestUnReadReportByType(1);
        if (hasOldNewDetectMail)
            DBManager.getInstance().getLatestUnReadReportByType(2);

        if (hasDetectMail)
            DBManager.getInstance().getDetectMailInfo();
    }

    private static synchronized void handleMailDataIndexForGetNew(final int mailDataIndex) {
        if (mailDataIndex < 0)
            return;

        final Object[] mailDataArr = ChatServiceController.getInstance().host.getMailDataArray(mailDataIndex);

        boolean hasDetectMail = false;
        String channelId = "";

        if (mailDataArr != null) {
            for (int i = 0; i < mailDataArr.length; i++) {
                MailData mailData = (MailData) mailDataArr[i];
                if (mailData == null)
                    continue;
                if (!hasDetectMail && mailData.getType() == MailManager.MAIL_DETECT_REPORT)
                    hasDetectMail = true;
                ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
                if (channel != null) {
                    DBManager.getInstance().insertMailData(mailData, channel);
                    channel.refreshRenderData();
                    channel.updateSysMailCountFromDB(1);
                    if (mailData.isUnread())
                        channel.updateUnreadSysMailCountFromDB(1);
                    if (ChannelManager.getInstance().needParseFirstChannel(channel.channelID))
                        ChannelManager.getInstance().parseFirstChannelID();

                    // 若重登陆（getNew且没有reset）时，channel中已经有系统邮件，loadMore不会加载新收到的邮件，需要直接加入channel

                    handleActivityMail(mailData);

                    if (channel.hasSysMailInList()) {
                        final MailData mail = parseMailData(mailData, false);
                        if (mail != null) {
                            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
                                                mail.getChannelId());
                                        if (channel != null) {
                                            channel.addNewMailData(mail);
                                        }
                                        // ChannelManager.getInstance().calulateAllChannelUnreadNum();
                                    } catch (Exception e) {
                                        LogUtil.printException(e);
                                    }
                                }
                            });
                        }
                    }
                }

            }
        }

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "hasMoreNewMailToGet", MailManager.hasMoreNewMailToGet,
                "latestMailUidFromGetNew", MailManager.latestMailUidFromGetNew, "hasDetectMail", hasDetectMail);

        if (MailManager.hasMoreNewMailToGet) {
            if (StringUtils.isNotEmpty(MailManager.latestMailUidFromGetNew) && !MailManager.latestMailUidFromGetNew.equals("0")) {
                MailData mail = DBManager.getInstance().getSysMailByID(MailManager.latestMailUidFromGetNew);
                if (mail != null) {
                    long time = mail.getCreateTime();
                    int createTime = TimeManager.getTimeInS(time);
                    JniController.getInstance().excuteJNIVoidMethod("getNewMailFromServer",
                            new Object[]{MailManager.latestMailUidFromGetNew, String.valueOf(createTime), 20});
                }
            }
        } else {
            ChannelManager.getInstance().prepareSystemMailChannel();
            getNewMailExistStatus();
            if (ChatServiceController.getChannelListFragment() != null)
                ChatServiceController.getChannelListFragment().refreshTitle();
        }

        if (StringUtils.isNotEmpty(ChannelManager.currentOpenedChannel) && ChannelManager.currentOpenedChannel.equals(channelId))
            ChannelManager.getInstance().postNotifyPopup(channelId);

        if (hasDetectMail)
            DBManager.getInstance().getDetectMailInfo();
        ChannelListFragment.onMailAdded();
    }

    private static synchronized void handleMailDataIndexForGetNewData(final int mailDataIndex) {
        if (mailDataIndex < 0)
            return;

        final Object[] mailDataArr = ChatServiceController.getInstance().host.getMailDataArray(mailDataIndex);

        boolean hasDetectMail = false;

        if (mailDataArr != null) {
            for (int i = 0; i < mailDataArr.length; i++) {
                MailData mailData = (MailData) mailDataArr[i];
                if (mailData == null)
                    continue;
                if (!hasDetectMail && mailData.getType() == MailManager.MAIL_DETECT_REPORT)
                    hasDetectMail = true;
                ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData.getChannelId());
                if (channel != null) {
                    DBManager.getInstance().insertMailData(mailData, channel);
                    channel.updateSysMailCountFromDB(1);
                    if (mailData.isUnread())
                        channel.updateUnreadSysMailCountFromDB(1);
                    handleActivityMail(mailData);

                }

            }
        }

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "hasMoreNewMailToGet", MailManager.hasMoreNewMailToGet,
                "latestMailUidFromGetNew", MailManager.latestMailUidFromGetNew, "hasDetectMail", hasDetectMail);

        if (MailManager.hasMoreNewMailToGet) {
            if (StringUtils.isNotEmpty(MailManager.latestMailUidFromGetNew) && !MailManager.latestMailUidFromGetNew.equals("0")) {
                MailData mail = DBManager.getInstance().getSysMailByID(MailManager.latestMailUidFromGetNew);
                if (mail != null) {
                    long time = mail.getCreateTime();
                    int createTime = TimeManager.getTimeInS(time);
                    JniController.getInstance().excuteJNIVoidMethod("getNewMailFromServer",
                            new Object[]{MailManager.latestMailUidFromGetNew, String.valueOf(createTime), 20});
                }
            }
        } else {
            ChannelManager.getInstance().preLoadSystemMailForGame();
            getNewMailExistStatus();
        }
        if (hasDetectMail)
            DBManager.getInstance().getDetectMailInfo();
    }

    public static void postGetNewMailCompleted() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        MailManager.hasMoreNewMailToGet = false;
        ChannelManager.getInstance().preLoadSystemMailForGame();
        getNewMailExistStatus();
    }

    public static MailData handleMailData(int channelType, final MailData mailData, boolean isFromDB, boolean needUpdateDB) {
        if (mailData == null)
            return null;

        boolean isWorldBossKillRewardMail = false;
        if (mailData.isWorldBossKillRewardMail()) {
            isWorldBossKillRewardMail = true;
            mailData.setType(MailManager.MAIL_WORLD_BOSS);
        }
        if (mailData.isAllianceBossKillRewardMail()) {
            isWorldBossKillRewardMail = true;
            mailData.setType(MailManager.MAIL_ALLIANCE_BOSS);
        }

        final ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, mailData.getChannelId());
        if (channel == null)
            return mailData;

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelType", channelType, "mailUid", mailData.getUid());

        if (mailData.getChannelId().equals(MailManager.CHANNELID_RECYCLE_BIN)) {
            long monthTime = 30L;
            monthTime = monthTime * 24 * 60 * 60 * 1000;
            if (mailData.getRecycleTime() == -1) {
                mailData.setRecycleTime(TimeManager.getInstance().getCurrentTimeMS());
                needUpdateDB = true;
            } else if (TimeManager.getInstance().getCurrentTimeMS() - mailData.getRecycleTime() >= monthTime) {
                JniController.getInstance().excuteJNIVoidMethod("deleteSingleMail",
                        new Object[]{Integer.valueOf(mailData.tabType), Integer.valueOf(mailData.getType()), mailData.getUid(), ""});
                DBManager.getInstance().deleteSysMail(channel, mailData.getUid());
            }
        }

        if (!isFromDB) {
            DBManager.getInstance().insertMailData(mailData, channel);
            channel.updateSysMailCountFromDB(1);
            if (mailData.isUnread())
                channel.updateUnreadSysMailCountFromDB(1);
            handleActivityMail(mailData);
        } else {
            if (needUpdateDB || isWorldBossKillRewardMail) {
                mailData.channelId = mailData.getChannelId();
                DBManager.getInstance().updateMail(mailData);
            }
        }

        if (ChannelManager.getInstance().needParseFirstChannel(channel.channelID))
            ChannelManager.getInstance().parseFirstChannelID();

        return mailData;
    }

    public static void handleActivityMail(MailData mailData) {
        LocalConfig config = ConfigManager.getInstance().getLocalConfig();
        if ((mailData.hasReward() && (mailData.isSpecialActivityMail()
                || (StringUtils.isNotEmpty(MailManager.getInstance().getDelayShowActivityMailUid()) && MailManager.getInstance().getDelayShowActivityMailUid().equals(mailData.getUid()))))
                && (config == null || (config != null && !config.isActivityMailShowed()))) {
            JniController.getInstance().excuteJNIVoidMethod("showRewardActivityTip", new Object[]{mailData.getUid()});
            MailManager.getInstance().setDelayShowActivityMailUid("");
            if (config == null)
                config = new LocalConfig();
            config.setActivityMailShowed(true);
            ConfigManager.getInstance().setLocalConfig(config);
            ConfigManager.getInstance().saveLocalConfig();
        }
    }

    public static void handleMessage(final MsgItem[] chatInfoArr, final String channelId, final String customName,
                                     final boolean calulateUnread, final boolean isFromServer) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "msg入库，刷新界面");
        for (int i = 0; i < chatInfoArr.length; i++) {
            if (chatInfoArr[i] == null)
                continue;
            if (isFromServer && chatInfoArr[i].hasTranslation())
                chatInfoArr[i].translatedLang = ConfigManager.getInstance().gameLang;
            if (!chatInfoArr[i].isRedPackageMessage() && !(!chatInfoArr[i].isSelfMsg() && chatInfoArr[i].isAudioMessage()))
                chatInfoArr[i].sendState = MsgItem.SEND_SUCCESS;
            if (isFromServer && chatInfoArr[i].readStatus != 1)
                chatInfoArr[i].readStatus = 0;
            // 存储用户信息
            chatInfoArr[i].initUserForReceivedMsg(channelId, customName);
            if (chatInfoArr[i].hasTranslated())
                chatInfoArr[i].hasTranslated = true;
            else
                chatInfoArr[i].hasTranslated = false;

            if (chatInfoArr[i].isAudioMessage() && chatInfoArr[i].isUserChatChannelMsg()
                    && StringUtils.isNotEmpty(chatInfoArr[i].msg) && chatInfoArr[i].msg.contains("audio_time") && chatInfoArr[i].msg.contains("media")) {
                try {
                    MailAudioContent content = JSON.parseObject(chatInfoArr[i].msg, MailAudioContent.class);
                    if (content != null) {
                        chatInfoArr[i].msg = content.getAudio_time();
                        chatInfoArr[i].media = content.getMedia();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (chatInfoArr[i].isDriftingBottleMsg() && chatInfoArr[i].channelType == DBDefinition.CHANNEL_TYPE_USER && StringUtils.isNotEmpty(chatInfoArr[i].msg)
                    && chatInfoArr[i].msg.contains("type") && chatInfoArr[i].msg.contains("media_time") && chatInfoArr[i].msg.contains("media") && chatInfoArr[i].msg.contains("text")) {
                try {
                    DriftingBottleContent content = JSON.parseObject(chatInfoArr[i].msg, DriftingBottleContent.class);
                    if (content != null) {
                        if (content.getType() == 0) {
                            chatInfoArr[i].msg = content.getText();
                        } else if (content.getType() == 1) {
                            chatInfoArr[i].msg = content.getMedia_time();
                            chatInfoArr[i].media = content.getMedia();
                            chatInfoArr[i].post = MsgItem.MSG_TYPE_DRIFTING_BOTTLE_AUDIO;
                        }
                        if (!(!chatInfoArr[i].isSelfMsg() && chatInfoArr[i].isAudioMessage()))
                            chatInfoArr[i].sendState = MsgItem.SEND_SUCCESS;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "chatInfoArr[i].sendState", chatInfoArr[i].sendState);
        }

        final int channelType = chatInfoArr[0].channelType;

        final boolean isNewMessage = chatInfoArr[0].isNewMsg;

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "channelType", channelType, "channelId", channelId,
                "calulateUnread", calulateUnread, "isFromServer", isFromServer);
        if (isFromServer) {
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "msg入库");
            save2DB(chatInfoArr, channelType, channelId, customName);
        }

        if (ChatServiceController.getChatFragment() != null) {
            if (isNewMessage)
                ChatServiceController.getChatFragment().refreshIsInLastScreen(channelType, channelId);
            if (ChatServiceController.getCurrentActivity() != null)
                ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        actualHandleMessage(channelType, isNewMessage, chatInfoArr, channelId, customName, isFromServer, calulateUnread);
                    }
                });
        } else
            actualHandleMessage(channelType, isNewMessage, chatInfoArr, channelId, customName, isFromServer, calulateUnread);

        if (ConfigManager.isAutoTranslateEnable()) {
            for (int i = 0; i < chatInfoArr.length; i++) {
                TranslateManager.getInstance().loadTranslation(chatInfoArr[i], null);
            }
        }

    }

    private static void actualHandleMessage(int channelType, final boolean isNewMessage, final MsgItem[] chatInfoArr,
                                            final String channelId, String customName, boolean isFromServer, boolean calulateUnread) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "msg刷新ui操作");
        handleMessage2(channelType, isNewMessage, chatInfoArr, channelId, customName, isFromServer);
        if (calulateUnread && !ChannelManager.isNotMailChannel(channelType))
            ChannelManager.getInstance().calulateAllChannelUnreadNum();
        ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
        ChannelManager.getInstance().postMainChannelChangedToGame(channel);
    }

    private static void handleMessage2(final int channelType, final boolean isNewMessage, final MsgItem[] chatInfoArr,
                                       final String channelId, String customName, boolean isFromServer) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "size", chatInfoArr.length, "isNewMessage", isNewMessage,
                "isFromServer", isFromServer, "channelId", channelId);
        ChatChannel channel = ChannelManager.getInstance().getChannel(channelType, channelId);
        if (channel == null)
            return;

        if (StringUtils.isNotEmpty(customName) && ChannelManager.isUserChannelType(channelType))
            channel.customName = customName;

        if (chatInfoArr.length <= 0)
            return;

        List<MsgItem> msgList = null;
        List<MsgItem> sendingMsgList = null;

        boolean hasUserChat = false;

        if (isNewMessage) {
            boolean hasSysAlliance = false;
            boolean hasSysCountry = false;
            boolean hasNearbyMsg = false;
            for (int i = 0; i < chatInfoArr.length; i++) {
                boolean hasNewAllianceSysMsg = false;
                boolean hasNewCountrySysMsg = false;
                MsgItem sendingMsg = null;
                MsgItem recievedMsg = chatInfoArr[i];
                if (ChatServiceController.allianceSysChannelEnable && recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                    if (recievedMsg.isNewAllianceSystem()) {
                        channel = ChannelManager.getInstance().getAllianceSysChannel();
                        msgList = channel.msgList;
                        sendingMsgList = channel.sendingMsgList;
                    } else {
                        channel = ChannelManager.getInstance().getAllianceChannel();
                        msgList = channel.msgList;
                        sendingMsgList = channel.sendingMsgList;
                    }
                } else if (ChatServiceController.countrySysChannelEnable && recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                    if (recievedMsg.isNewCountrySystem()) {
                        channel = ChannelManager.getInstance().getCountrySysChannel();
                        msgList = channel.msgList;
                        sendingMsgList = channel.sendingMsgList;
                    } else {
                        channel = ChannelManager.getInstance().getCountryChannel();
                        msgList = channel.msgList;
                        sendingMsgList = channel.sendingMsgList;
                    }
                } else {
                    LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "msg 放在channel ");
                    channel = ChannelManager.getInstance().getChannel(channelType, channelId);
                    msgList = channel.msgList;
                    sendingMsgList = channel.sendingMsgList;
                }

                LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_MSG, "recievedMsg.msg", recievedMsg.msg,
                        "recievedMsg sendLocalTime", recievedMsg.sendLocalTime, "recievedMsg.isSelfMsg()", recievedMsg.isSelfMsg(),
                        "recievedMsg.isSystemMessage()", recievedMsg.isSystemMessage(), "recievedMsg.isHornMessage()",
                        recievedMsg.isHornMessage(), "recievedMsg.uid", recievedMsg.mailId);

                // TO-DO 针对于语音消息却不是联盟频道的问题做的容错
                if (recievedMsg.isAudioMessage() && (recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD))
                    continue;

                if (msgList != null && msgList.size() > 0) {
                    for (int j = 0; j < sendingMsgList.size(); j++) {
                        MsgItem sendMsg = sendingMsgList.get(j);
                        if (sendMsg == null)
                            continue;
                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "sendMsg.msg", sendMsg.msg,
                                "sendMsg.sendLocalTime", sendMsg.sendLocalTime);
                        if (sendMsg.sendLocalTime != 0 && sendMsg.sendLocalTime == recievedMsg.sendLocalTime) {
                            sendingMsg = sendMsg;
                        }
                    }
                }

                // 我发的消息
                if (sendingMsg != null && recievedMsg.isSelfMsg() && (!recievedMsg.isSystemMessage() || recievedMsg.isHornMessage())) {
                    sendingMsg.sendState = MsgItem.SEND_SUCCESS;
                    sendingMsgList.remove(sendingMsg);
                    channel.replaceDummyMsg(recievedMsg, msgList.indexOf(sendingMsg));
                    // 更新是否发送过语音消息，如果没有发过，会在输入框显示发送方法的hint
                    if (recievedMsg.isAudioMessage()) {
                        try {
                            LocalConfig config = ConfigManager.getInstance().getLocalConfig();
                            if (config != null && !config.isAudioUsed()) {
                                config.setAudioUsed(true);
                                ConfigManager.getInstance().saveLocalConfig();
                            } else if (config == null) {
                                config = new LocalConfig();
                                config.setAudioUsed(true);
                                ConfigManager.getInstance().setLocalConfig(config);
                                ConfigManager.getInstance().saveLocalConfig();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (recievedMsg.isDriftingBottleMsg()) {
                        String bottleId = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
                        if (StringUtils.isNotEmpty(bottleId) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())) {
                            bottleId += ("_" + UserManager.getInstance().getCurrentUserId());
                            LogUtil.trackDriftingBottle(bottleId + "_self_send_" + TimeManager.getSendTimeYMD(recievedMsg.createTime));
                        }

                    }
                } else {
                    boolean addSuccess = channel.addNewMsg(recievedMsg);
                    if (recievedMsg.isDriftingBottleMsg()) {
                        String bottleId = ChannelManager.getInstance().getActualUidFromChannelId(channel.channelID);
                        if (StringUtils.isNotEmpty(bottleId) && StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUserId())) {
                            bottleId += ("_" + UserManager.getInstance().getCurrentUserId());
                            LogUtil.trackDriftingBottle(bottleId + "_other_send_" + TimeManager.getSendTimeYMD(recievedMsg.createTime));
                        }

                    }

                    if (MailManager.nearbyEnable && channel != null && channel.isNearbyChannel() && channel.unreadCount > 0 && !recievedMsg.isSelfMsg() && addSuccess && recievedMsg.isNewMsg) {
                        hasNearbyMsg = true;
                        JniController.getInstance().excuteJNIVoidMethod("postNewNearbyMsg", null);
                    }

                    if (ChatServiceController.allianceSysChannelEnable && addSuccess && !hasNewAllianceSysMsg && recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE
                            && recievedMsg.isNewAllianceSystem())
                        hasNewAllianceSysMsg = true;
                    if (ChatServiceController.countrySysChannelEnable && addSuccess && !hasNewCountrySysMsg && recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY
                            && recievedMsg.isNewCountrySystem())
                        hasNewCountrySysMsg = true;
                }

                if (channel.isModChannel() || channel.isMessageChannel())
                    hasUserChat = true;

                if (ChatServiceController.getChatFragment() != null) {
                    ChatServiceController.getChatFragment().notifyDataSetChanged(channel.channelType, channel.channelID, true);
                    if (chatInfoArr.length == 1)
                        ChatServiceController.getChatFragment().smoothUpdateListPositionForNewMsg(channel.channelType, channel.channelID,
                                recievedMsg.isSelfMsg);
                    else
                        ChatServiceController.getChatFragment().updateListPositionForNewMsg(channel.channelType, channel.channelID,
                                recievedMsg.isSelfMsg);

                    final MsgItem msgItem = recievedMsg;
                    final boolean mHasNewAllianceSysMsg = hasNewAllianceSysMsg;
                    final boolean mHasNewCountrySysMsg = hasNewCountrySysMsg;
                    ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (ChatServiceController.getChatFragment() != null) {

                                    if (msgItem.isHornMessage())
                                        ChatServiceController.getChatFragment().showHornScrollText(msgItem);
                                    if (msgItem.isAudioMessage())
                                        ChatServiceController.getChatFragment().updateAudioHint();
                                    if (mHasNewAllianceSysMsg)
                                        ChatServiceController.getChatFragment().postNewAllianceSystemMsg();
                                    if (mHasNewCountrySysMsg)
                                        ChatServiceController.getChatFragment().postNewCountrySystemMsg();
                                }
                            } catch (Exception e) {
                                LogUtil.printException(e);
                            }
                        }
                    });

                } else {
                    if (hasNewAllianceSysMsg)
                        ChatFragmentNew.setNewAllianceSystemMsg();
                    if (hasNewCountrySysMsg)
                        ChatFragmentNew.setNewCountrySystemMsg();
                    ChatFragmentNew.onMsgAdd(channel.channelType, channel.channelID, true);
                    if (recievedMsg != null && recievedMsg.canEnterScrollTextQueue()) {
                        ScrollTextManager.getInstance().clear(channel.channelType);
                        ScrollTextManager.getInstance().push(recievedMsg, channel.channelType);
                    }
                }

                if (ChatServiceController.allianceSysChannelEnable && !hasSysAlliance && recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE && recievedMsg.isNewAllianceSystem())
                    hasSysAlliance = true;
                if (ChatServiceController.countrySysChannelEnable && !hasSysCountry && recievedMsg.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY && recievedMsg.isNewCountrySystem())
                    hasSysCountry = true;

                // if(recievedMsg!=null && isFromServer && recievedMsg.isAllianceMemberChangedMsg())
                // JniController.getInstance().excuteJNIVoidMethod("getAllianceMember", null);
            }

            if (hasUserChat)
                ChannelManager.getInstance().getNewUserChatChannelId();
            if (hasSysAlliance && ChatServiceController.getChatFragment() != null)
                ChatServiceController.getChatFragment().refreshIsInLastScreen(DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS, channelId);
            if (hasSysCountry && ChatServiceController.getChatFragment() != null)
                ChatServiceController.getChatFragment().refreshIsInLastScreen(DBDefinition.CHANNEL_TYPE_COUNTRY_SYS, channelId);
            if (hasNearbyMsg && ChatServiceController.getNearByListActivity() != null)
                ChatServiceController.getNearByListActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (ChatServiceController.getNearByListActivity() != null)
                            ChatServiceController.getNearByListActivity().refreshNearbyUnreadCount();
                    }
                });
        } else {
            int loadCount = 0;
            int loadSysAllianceCount = 0;
            int loadSysCountryCount = 0;
            MsgItem oldFirstItem = null;
            MsgItem oldSysAllianceFirstItem = null;
            MsgItem oldSysCountryFirstItem = null;

            if (channel.msgList != null && channel.msgList.size() > 0)
                oldFirstItem = channel.msgList.get(0);

            if (ChatServiceController.allianceSysChannelEnable) {
                ChatChannel allianceSysChannel = ChannelManager.getInstance().getAllianceSysChannel();
                if (allianceSysChannel != null && allianceSysChannel.msgList != null && allianceSysChannel.msgList.size() > 0) {
                    oldSysAllianceFirstItem = allianceSysChannel.msgList.get(0);
                }
            }

            if (ChatServiceController.countrySysChannelEnable) {
                ChatChannel countrySysChannel = ChannelManager.getInstance().getCountrySysChannel();
                if (countrySysChannel != null && countrySysChannel.msgList != null && countrySysChannel.msgList.size() > 0) {
                    oldSysCountryFirstItem = countrySysChannel.msgList.get(0);
                }
            }

            for (int i = 0; i < chatInfoArr.length; i++) {
                ChatChannel chatChannel = ChannelManager.getInstance().getChannel(channelType, channelId);
                if (ChatServiceController.allianceSysChannelEnable && chatInfoArr[i].channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                    if (chatInfoArr[i].isNewAllianceSystem())
                        chatChannel = ChannelManager.getInstance().getAllianceSysChannel();
                    else
                        chatChannel = ChannelManager.getInstance().getAllianceChannel();
                }

                if (ChatServiceController.countrySysChannelEnable && chatInfoArr[i].channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                    if (chatInfoArr[i].isNewCountrySystem())
                        chatChannel = ChannelManager.getInstance().getCountrySysChannel();
                    else
                        chatChannel = ChannelManager.getInstance().getCountryChannel();
                }

                if (chatChannel != null) {
                    if (chatInfoArr[i].isAudioMessage() && (chatInfoArr[i].channelType == DBDefinition.CHANNEL_TYPE_COUNTRY ||
                            chatInfoArr[i].channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS ||
                            chatInfoArr[i].channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD))
                        continue;
                    boolean isAddSuccess = chatChannel.addHistoryMsg(chatInfoArr[i]);
                    if (isAddSuccess) {
                        if (chatChannel.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS)
                            loadSysAllianceCount++;
                        else if (chatChannel.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS)
                            loadSysCountryCount++;
                        else
                            loadCount++;
                    }
                }
            }

            if (loadCount > 0) {
                channel.getLoadedTimeNeedShowMsgIndex(loadCount);
            }

            if (ChatServiceController.allianceSysChannelEnable && loadSysAllianceCount > 0) {
                ChatChannel allianceSysChannel = ChannelManager.getInstance().getAllianceSysChannel();
                if (allianceSysChannel != null) {
                    allianceSysChannel.getLoadedTimeNeedShowMsgIndex(loadSysAllianceCount);
                }
            }

            if (ChatServiceController.countrySysChannelEnable && loadSysCountryCount > 0) {
                ChatChannel countrySysChannel = ChannelManager.getInstance().getCountrySysChannel();
                if (countrySysChannel != null) {
                    countrySysChannel.getLoadedTimeNeedShowMsgIndex(loadSysCountryCount);
                }
            }

            if ((channelType == DBDefinition.CHANNEL_TYPE_COUNTRY || channelType == DBDefinition.CHANNEL_TYPE_COUNTRY_SYS) && !oldHornMsgPushed) {
                if (channel.msgList != null && channel.msgList.size() > 0) {
                    for (int i = 0; i < channel.msgList.size(); i++) {
                        MsgItem msgItem = channel.msgList.get(i);
                        if (msgItem != null && msgItem.canEnterScrollTextQueue()) {
                            ScrollTextManager.getInstance().clear(channelType);
                            ScrollTextManager.getInstance().push(msgItem, channelType);
                            oldHornMsgPushed = true;
                        }
                    }
                }
            } else if (channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && !oldBattleHornMsgPushed) {
                if (channel.msgList != null && channel.msgList.size() > 0) {
                    for (int i = 0; i < channel.msgList.size(); i++) {
                        MsgItem msgItem = channel.msgList.get(i);
                        if (msgItem != null && msgItem.canEnterScrollTextQueue()) {
                            ScrollTextManager.getInstance().clear(channelType);
                            ScrollTextManager.getInstance().push(msgItem, channelType);
                            oldBattleHornMsgPushed = true;
                        }
                    }
                }
            }

            if (ChatServiceController.getChatFragment() != null) {
                if (loadCount > 0) {
                    System.out.println("updateListPositionForOldMsg 0");
                    ChatServiceController.getChatFragment().notifyDataSetChanged(channel.channelType, channel.channelID, false);
                    ChatServiceController.getChatFragment().updateListPositionForOldMsg(channel.channelType, channel.channelID, loadCount,
                            !ChatServiceController.getInstance().isDifferentDate(oldFirstItem, channel.msgList));
                    ChatServiceController.getChatFragment().resetMoreDataStart(channel.channelType, channel.channelID);
                }

                ChatChannel systemChannel = null;
                MsgItem oldSysFirstItem = null;
                int loadNum = loadCount;
                if (ChatServiceController.allianceSysChannelEnable && loadSysAllianceCount > 0) {
                    systemChannel = ChannelManager.getInstance().getAllianceSysChannel();
                    oldSysFirstItem = oldSysAllianceFirstItem;
                    loadNum = loadSysAllianceCount;
                } else if (ChatServiceController.countrySysChannelEnable && loadSysCountryCount > 0) {
                    systemChannel = ChannelManager.getInstance().getCountrySysChannel();
                    oldSysFirstItem = oldSysCountryFirstItem;
                    loadNum = loadSysCountryCount;
                }

                if (systemChannel != null) {
                    System.out.println("updateListPositionForOldMsg 00");
                    ChatServiceController.getChatFragment().notifyDataSetChanged(systemChannel.channelType, systemChannel.channelID, false);
                    ChatServiceController.getChatFragment().updateListPositionForOldMsg(systemChannel.channelType, systemChannel.channelID, loadNum,
                            !ChatServiceController.getInstance().isDifferentDate(oldSysFirstItem, systemChannel.msgList));
                    ChatServiceController.getChatFragment().resetMoreDataStart(systemChannel.channelType, systemChannel.channelID);
                }
            }
        }

        if (isFromServer) {
            // 会触发reload，仅在服务器端来了新消息才调用
            AbstractRecyclerActivity.onMsgAdded(channel);
            WebSocketManager.getInstance().notifyChatListener(null,channel,null);
        }

        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channelType", channelType, "channel.channelType",
                channel.channelType, "isWebSocketEnabled()", WebSocketManager.isWebSocketEnabled(), "isRecieveFromWebSocket",
                ConfigManager.isRecieveFromWebSocket);

        if (channel != null && isNewMessage && (WebSocketManager.isRecieveFromWebSocket(channelType) &&
                (channel.isCountryOrAllianceChannel() || channel.isBattleChannel() || channel.isCustomChannelNeedSendLatestMsg()))) {
            sendChatLatestMessage(channel);
        }

    }

    public static void notifyChatRoomNameChanged(final String modifyName) {
        UserManager.getInstance().getCurrentMail().opponentName = modifyName;

        if (ChatServiceController.getChatRoomSettingActivity() != null) {
            ChatServiceController.getChatRoomSettingActivity().refreshChatRoomName();
            ChatServiceController.getChatRoomSettingActivity().refreshTitle();
        }

        ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getChatFragment() != null) {
                        ChatServiceController.getChatFragment().changeChatRoomName(modifyName);
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public static void postChannelNoMoreData(int channelType, boolean hasNoMoreData) {
        try {
            if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
                return;

            ChannelManager.getInstance().setNoMoreDataFlag(ChannelManager.channelType2tab(channelType), hasNoMoreData);
        } catch (Exception e) {
            LogUtil.printException(e);
        }
    }

    public static void removeAllMailByUid(String fromUid) {
        if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
            return;

        ChannelManager.getInstance().removeAllMailMsgByUid(fromUid);
    }

    public static void setIsNewMailListEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isNewMailListEnable", enable);
        ChatServiceController.isNewMailListEnable = enable;
    }

    public static void setCurrentUserId(String uidStr) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "uid", uidStr);
        UserManager.getInstance().setCurrentUserId(uidStr);
    }

    private static UserInfo currentUserClone;

    public static void setPlayerInfo(Map<String, Object> playerDic) {
        boolean countryChange = false;
        UserInfo currentUser = UserManager.getInstance().getCurrentUser();
        currentUserClone = (UserInfo) currentUser.clone();

        if (playerDic != null) {
            int serverId = 0;
            if (playerDic.containsKey("serverId"))
                serverId = Integer.parseInt(playerDic.get("serverId").toString());
            int worldTime = 0;
            if (playerDic.containsKey("worldTime"))
                worldTime = Integer.parseInt(playerDic.get("worldTime").toString());
            int timeZone = 0;
            if (playerDic.containsKey("timeZone"))
                timeZone = Integer.parseInt(playerDic.get("timeZone").toString());
            int gmFlag = 0;
            if (playerDic.containsKey("gmFlag"))
                gmFlag = Integer.parseInt(playerDic.get("gmFlag").toString());
            int picVer = 0;
            if (playerDic.containsKey("picVer"))
                picVer = Integer.parseInt(playerDic.get("picVer").toString());
            String name = "";
            if (playerDic.containsKey("name"))
                name = playerDic.get("name").toString();
            String uid = "";
            if (playerDic.containsKey("uid"))
                uid = playerDic.get("uid").toString();
            String pic = "";
            if (playerDic.containsKey("pic"))
                pic = playerDic.get("pic").toString();
            int vipLevel = 0;
            if (playerDic.containsKey("vipLevel"))
                vipLevel = Integer.parseInt(playerDic.get("vipLevel").toString());
            int svipLevel = 0;
            if (playerDic.containsKey("svipLevel"))
                svipLevel = Integer.parseInt(playerDic.get("svipLevel").toString());
            int vipEndTime = 0;
            if (playerDic.containsKey("vipEndTime"))
                vipEndTime = Integer.parseInt(playerDic.get("vipEndTime").toString());
            int lastUpdateTime = 0;
            if (playerDic.containsKey("lastUpdateTime"))
                lastUpdateTime = Integer.parseInt(playerDic.get("lastUpdateTime").toString());
            int crossFightSrcServerId = 0;
            if (playerDic.containsKey("crossFightSrcServerId"))
                crossFightSrcServerId = Integer.parseInt(playerDic.get("crossFightSrcServerId").toString());
            int chatBgId = 0;
            if (playerDic.containsKey("chatBgId"))
                chatBgId = Integer.parseInt(playerDic.get("chatBgId").toString());
            int chatBgEndTime = 0;
            if (playerDic.containsKey("chatBgEndTime"))
                chatBgEndTime = Integer.parseInt(playerDic.get("chatBgEndTime").toString());
            String chatBgTextColor = "";
            if (playerDic.containsKey("chatBgTextColor"))
                chatBgTextColor = playerDic.get("chatBgTextColor").toString();
            String officer = "";
            if (playerDic.containsKey("officer"))
                officer = playerDic.get("officer").toString();

            String flagCountry = "";
            if (playerDic.containsKey("flagCountry"))
                flagCountry = playerDic.get("flagCountry").toString();

            int showBanner = 0;
            if (playerDic.containsKey("showBanner"))
                showBanner = Integer.parseInt(playerDic.get("showBanner").toString());

            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "uid", uid, "name", name, "serverId", serverId, "timeZone", timeZone,
                    "crossFightSrcServerId", crossFightSrcServerId, "chatBgTextColor", chatBgTextColor, "officer", officer, "flagCountry", flagCountry, "showBanner", showBanner);

            TimeManager.getInstance().setServerBaseTime(worldTime, timeZone);

            // 如果crossFightSrcServerId变了，serverId肯定会变，所以只判断serverId即可
            if (ConfigManager.useWebSocketServer && currentUser.serverId > 0 && serverId > 0 && serverId != currentUser.serverId) {
                countryChange = true;
            }

            currentUser.serverId = serverId;
            ChatServiceController.serverId = serverId;

            currentUser.headPicVer = picVer;
            currentUser.mGmod = gmFlag;
            currentUser.userName = name;
            currentUser.headPic = pic;
            currentUser.vipLevel = vipLevel;
            currentUser.svipLevel = svipLevel;
            currentUser.vipEndTime = vipEndTime;
            currentUser.lastUpdateTime = lastUpdateTime;
            currentUser.chatBgId = chatBgId;
            currentUser.chatBgEndTime = chatBgEndTime;
            currentUser.chatBgTextColor = chatBgTextColor;
            currentUser.officer = officer;
            currentUser.crossFightSrcServerId = crossFightSrcServerId;
            currentUser.flagCountry = flagCountry;
            currentUser.showBanner = showBanner;
            ChatServiceController.crossFightSrcServerId = crossFightSrcServerId;
        }

        UserManager.getInstance().updateUser(currentUser);
        ChannelManager.getInstance().getCountryChannel();

        ScrollTextManager.getInstance().clear(DBDefinition.CHANNEL_TYPE_COUNTRY);
        ScrollTextManager.getInstance().clear(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);

        if (!SwitchUtils.mqttEnable)
            WebSocketManager.getInstance().sendDevice();
        else
            MqttManager.getInstance().sendDevice();

        if (countryChange) {
            if (!SwitchUtils.chatSessionEnable || SharePreferenceUtil.checkSession()) {
                if (!SwitchUtils.mqttEnable)
                    WebSocketManager.getInstance().joinRoom();
                else
                    MqttManager.getInstance().pullNewData();
            }
        }

        EmojSubscribeManager.getInstance().getEmojSubData();
        LogController.getInstance().init(ChatServiceController.hostActivity,"100001",
                UserManager.getInstance().getCurrentUserId(),UserManager.getInstance().getCurrentUser().serverId+"");
    }

    /**
     * 初始登录时会调用 打开聊天时，会紧接着setPlayerInfo后面调
     * 重新登录、切服等时候，会调C++的parseData()刷新联盟信息，也调用此函数
     */
    public static void setPlayerAllianceInfo(String asnStr, String allianceIdStr, int alliancerank, boolean isFirstJoinAlliance) {
        try {
            if (allianceIdStr != null && UserManager.getInstance().getCurrentUser() != null
                    && UserManager.getInstance().getCurrentUser().allianceId != null)
                LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "allianceIdStr", allianceIdStr, "current allianceId", UserManager
                        .getInstance().getCurrentUser().allianceId);
            String previousAllianceId = UserManager.getInstance().getCurrentUser().allianceId;
            // 这里总是会leave，然后再判断是否join，是否应该改成判断前后allianceId是否相等
            resetPlayerIsInAlliance(false);
            if (currentUserClone == null)
                currentUserClone = (UserInfo) UserManager.getInstance().getCurrentUser().clone();
            UserManager.getInstance().getCurrentUser().asn = asnStr;
            UserManager.getInstance().getCurrentUser().allianceId = allianceIdStr;
            UserManager.getInstance().getCurrentUser().allianceRank = alliancerank;
            ConfigManager.getInstance().isFirstJoinAlliance = isFirstJoinAlliance;
            // 使用旧后台、且db中没有联盟时，需要将allianceChannel加入channelMap，以免getChannelInfo中没有联盟
            ChannelManager.getInstance().getAllianceChannel();
            if (ConfigManager.useWebSocketServer && !previousAllianceId.equals(allianceIdStr)) {
                if (!SwitchUtils.chatSessionEnable || SharePreferenceUtil.checkSession()) {
                    // 可能在登陆时调用，此时ws未初始化，调用无效
                    if (!SwitchUtils.mqttEnable)
                        WebSocketManager.getInstance().joinRoom();
                    else
                        MqttManager.getInstance().pullNewData();
                }
            }
            if (!currentUserClone.equalsLogically(UserManager.getInstance().getCurrentUser())) {
//				LogUtil.printVariables(
//						Log.INFO,
//						LogUtil.TAG_CORE,
//						"current user updated:\n"
//								+ LogUtil.compareObjects(new Object[] { UserManager.getInstance().getCurrentUser(), currentUserClone }));
                UserManager.getInstance().updateCurrentUser();
            }
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
            if (!(SwitchUtils.chatSessionEnable && SharePreferenceUtil.isCreatingSession)) {
                if (!SwitchUtils.mqttEnable)
                    connect2WS();
                else {
                    if (WebSocketManager.isWebSocketEnabled())
                        MqttManager.getInstance().init();
                }
            }
            if (ChatServiceController.realtime_voice_enable && resetAllianceFromGame && StringUtils.isNotEmpty(allianceIdStr) && StringUtils.isEmpty(WebRtcPeerManager.webRtcUrl)) {
                resetAllianceFromGame = false;
                WebSocketManager.getInstance().getRealtimeVoiceRoomInfo();
            }
            if (SharePreferenceUtil.checkSession())
                LoginShareDataUtil.saveLoginData();
            AllianceShareManager.getInstance().initFireBase();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 目的是清空联盟消息，以免切换账号或切换联盟之后还有旧消息在
     * 退出联盟会调用，此时fromGame为true
     * 一般的setPlayerAllianceInfo会调用
     * 切换账号时的setPlayerAllianceInfo也会调用
     *
     * @param fromGame
     * 是否是游戏在退出联盟时直接调的
     */

    private static boolean resetAllianceFromGame = false;

    public static void resetPlayerIsInAlliance(boolean fromGame) {
        if (StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
            return;

        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "fromGame", fromGame);

        UserManager.getInstance().clearAllianceMember();

        if (UserManager.getInstance().getCurrentUser().allianceId.equals(""))
            return;

        if (ChannelManager.isInited()) {
            if (UserManager.getInstance().isCurrentUserInAlliance()) {
                if (ChannelManager.getInstance().getAllianceChannel() != null && ChannelManager.getInstance().getAllianceChannel().hasInitLoaded()) {
                    // 有时候会发生nullPointer异常
                    ChannelManager.getInstance().getAllianceChannel().resetMsgChannel();
                    if (ChatServiceController.getChatFragment() != null)
                        ChatServiceController.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE, "", true);
                }

                if (ChannelManager.getInstance().getAllianceSysChannel() != null && ChannelManager.getInstance().getAllianceChannel().hasInitLoaded()) {
                    // 有时候会发生nullPointer异常
                    ChannelManager.getInstance().getAllianceSysChannel().resetMsgChannel();
                    if (ChatServiceController.getChatFragment() != null)
                        ChatServiceController.getChatFragment().notifyDataSetChanged(DBDefinition.CHANNEL_TYPE_ALLIANCE_SYS, "", true);
                }

            }
            ChannelManager.getInstance().setNoMoreDataFlag(1, false);
        }

        String previousAllianceId = UserManager.getInstance().getCurrentUser().allianceId;

        if (fromGame) {
            UserManager.getInstance().getCurrentUser().asn = "";
            UserManager.getInstance().getCurrentUser().allianceId = "";
            UserManager.getInstance().getCurrentUser().allianceRank = -1;
            UserManager.getInstance().updateCurrentUser();

            if (ChatServiceController.realtime_voice_enable && StringUtils.isNotEmpty(previousAllianceId)) {
                resetAllianceFromGame = true;
                unbindRealtimeVoice();
                WebRtcPeerManager.getInstance().reset();
                WebRtcPeerManager.webRtcUrl = "";
            }
        }

        if (fromGame && ConfigManager.useWebSocketServer && StringUtils.isNotEmpty(previousAllianceId)) {
            if (!SwitchUtils.chatSessionEnable || SharePreferenceUtil.checkSession()) {
                // 可能在登陆时调用，此时ws未初始化，调用无效
                if (!SwitchUtils.mqttEnable)
                    WebSocketManager.getInstance().leaveAllianceRoom();
                else
                    MqttManager.getInstance().leaveAllianceRoom();
            }
        }

        AllianceShareManager.getInstance().clearFireBaseData();
    }

    /**
     * 锁屏时调用 以前是切换tab时才会获取数据，用chat.get接口，如果发现已经有数据，就不会再获取，所以得先clear一次
     */
    public static void clearCountryMsg() {
    }

    // 以前就未调用
    public static void clearMailMsg() {
    }

    /**
     * 论坛重新登录
     */
    public static void onPlayerChanged() {
        ForumFragment.isFirstLogin = true;
    }

    public static void notifyChangeLanguage() {
        Object[] langItemArray = ChatServiceController.getInstance().host.getChatLangArray();
        if (langItemArray == null)
            return;

        LanguageItem[] langArray = new LanguageItem[langItemArray.length];
        for (int i = 0; i < langItemArray.length; i++) {
            Object obj = langItemArray[i];
            if (obj != null) {
                langArray[i] = (LanguageItem) obj;
            }
        }
        LanguageManager.initChatLanguage(langArray);
    }

    public static void onCreateChatroomSuccess() {
        ChatServiceController.isCreateChatRoom = false;
        ServiceInterface.showChatActivity(ChatServiceController.getCurrentActivity(), DBDefinition.CHANNEL_TYPE_CHATROOM, false);
    }

    public static boolean isDontKeepActivitiesEnabled() {
        int finishActivitiesEnabled = Settings.System.getInt(ChatServiceController.hostActivity.getContentResolver(),
                Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);

        return finishActivitiesEnabled == 1;
    }

    public static void gotoDevelopmentSetting() {
        ChatServiceController.hostActivity.startActivityForResult(new Intent(
                android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS), 0);
    }

    public static void showAllianceShareFrom2dx() {
        LogUtil.trackPageView("click_alliance_share");
        AllianceShareManager.getInstance().goOnline();
        ServiceInterface.showAllianceShareListActivity(ChatServiceController.hostActivity);
    }

    public static void showWriteMailActivityFrom2dx() {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        showActivity(ChatServiceController.hostActivity, WriteMailActivity.class, true, true, null, false, false);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showChatActivityFrom2dx(int maxHornInputCount, final int chatType, int sendInterval, final boolean rememberPosition,
                                               boolean enableCustomHeadImg, boolean isNoticeItemUsed) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "chatType", chatType, "sendInterval", sendInterval,
                "rememberPosition", rememberPosition, "enableCustomHeadImg", enableCustomHeadImg, "isNoticeItemUsed", isNoticeItemUsed);

        ConfigManager.maxHornInputLength = maxHornInputCount;
        ConfigManager.enableCustomHeadImg = enableCustomHeadImg;
        ChatServiceController.isHornItemUsed = isNoticeItemUsed;
        ConfigManager.sendInterval = sendInterval * 1000;
        ChatServiceController.isCreateChatRoom = false;
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showChatActivity(ChatServiceController.hostActivity, chatType, rememberPosition);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showForumFrom2dx(String url) {
        final String forumUrl = url;
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showForumActivity(ForumActivity.WEBVIEW_TYPE_FORFUM, ChatServiceController.hostActivity, forumUrl);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showTranslationOptimizationFrom2dx(String url) {
        final String forumUrl = url;
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showForumActivity(ForumActivity.WEBVIEW_TYPE_TRANSLATION_OPTIMIZATION,
                                ChatServiceController.hostActivity, forumUrl);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showMemberSelectorFrom2dx() {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ChatServiceController.isCreateChatRoom = true;
                        ServiceInterface.showMemberSelectorActivity(ChatServiceController.hostActivity, false);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showNearByListActivity(Activity a) {
        LogUtil.trackPageView("showNearByListActivity");
        showActivity(a, NearByActivity.class, true, false, null, false, false);
        LogUtil.trackNearby("nearby_click");
    }

    public static void showEmojSubscribActivity(Activity a) {
        LogUtil.trackPageView("showEmojSubscribActivity");
        showActivity(a, EmojSubscribActivity.class, true, false, null, false, false);
    }

    public static void showNearByActivityFrom2dx() {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showNearByListActivity(ChatServiceController.hostActivity);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showChannelListFrom2dx(final boolean isGoBack) {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showChannelListActivity(ChatServiceController.hostActivity, false,
                                DBDefinition.CHANNEL_TYPE_OFFICIAL, null, isGoBack);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void notifyUserInfo(int index) {
        if (index != -1) {
            UserManager.getInstance().onReceiveUserInfo(ChatServiceController.getInstance().host.getUserInfoArray(index));
        } else {
            UserManager.getInstance().onReceiveUserInfo(new Object[]{});
        }
    }

    public static void notifySearchedUserInfo(int index) {
        UserManager.getInstance().onReceiveSearchUserInfo(ChatServiceController.getInstance().host.getUserInfoArray(index));
    }

    public static void notifyUserUids(String uidStr, String lastUpdateTimeStr, int type) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "uidStr", uidStr, "type", type);
        if (uidStr.equals("") || lastUpdateTimeStr.equals(""))
            return;
        String[] uidArr = uidStr.split("_");
        String[] lastUpdateTimeArr = lastUpdateTimeStr.split("_");

        if (type == UserManager.NOTIFY_USERINFO_TYPE_ALLIANCE)
            UserManager.getInstance().clearAllianceMember();
        else if (type == UserManager.NOTIFY_USERINFO_TYPE_FRIEND)
            UserManager.getInstance().clearFriendMember();
        for (int i = 0; i < uidArr.length; i++) {
            if (!uidArr[i].equals("")) {
                UserManager.checkUser(uidArr[i], "", 0);
                UserInfo user = UserManager.getInstance().getUser(uidArr[i]);

                if (user != null) {
                    if (type == UserManager.NOTIFY_USERINFO_TYPE_ALLIANCE)
                        UserManager.getInstance().putChatRoomMemberInMap(user);
                    else if (type == UserManager.NOTIFY_USERINFO_TYPE_FRIEND)
                        UserManager.getInstance().putFriendMemberInMap(user);
                }

                // lastUpdateTimeArr[i]至少为0（C++中将空字符串设为"0"），redis中有的老用户还没有被更新过时，可能会有这种情况
                // 保险起见，这里再检查一下空字符串
                int lastUpdateTime = lastUpdateTimeArr[i].equals("") ? 0 : Integer.parseInt(lastUpdateTimeArr[i]);
                UserManager.checkUser(uidArr[i], "", lastUpdateTime);
            }
        }

        if (ChatServiceController.getChatFragment() != null)
            ChatServiceController.getChatFragment().notifyAllianceMemberChanged();
    }

    /**
     * C++主动关闭原生，发生在网络断开连接时，或创建聊天室之后
     */
    public static void exitChatActivityFrom2dx(boolean needRemeberActivityStack) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "needRemeberActivityStack", needRemeberActivityStack);

        if (!needRemeberActivityStack)
            ChannelListFragment.preventSecondChannelId = true;
        if (ChatServiceController.getCurrentActivity() != null) {
            ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ChatServiceController.showGameActivity(ChatServiceController.getCurrentActivity());
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    /**
     * 这个时机比较奇怪，可能只调了一个activity的onDestroy，就会到这里，之后才会调其它activity的onDestroy
     */
    public static void onReturn2dxGame() {
    }

    public static void flyHint(String icon, String titleText, String contentText, float time, float dy, boolean useDefaultIcon) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "contentText", contentText, "titleText", titleText);

        if (!ChatServiceController.isNativeShowing || ChatServiceController.getCurrentActivity() == null)
            return;

        final String text = contentText;
        final int duration = time > 0 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;

        ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getCurrentActivity().getApplicationContext() != null && text != null) {
                        Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity().getApplicationContext(), text, duration);
                        toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
                        toast.show();
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    private static Timer flyHintTimer;

    public static void stopFlyHintTimer() {
        if (flyHintTimer != null) {
            flyHintTimer.cancel();
            flyHintTimer.purge();
        }
    }

    private static int flyHintCount;

    public static void flySystemUpdateHint(double countDown, boolean isFlyHintLogin, boolean isLogin, String tip, String icon) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, "tip", tip, "isFlyHintLogin", isFlyHintLogin, "countDown",
                countDown);

        if (!ChatServiceController.isNativeShowing || ChatServiceController.getCurrentActivity() == null)
            return;
        stopFlyHintTimer();
        flyHintTimer = new Timer();
        final String text = tip;
        flyHintCount = (int) countDown / 10;
        final boolean flyHintLogin = isFlyHintLogin;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String showText = "";
                            if (flyHintLogin)
                                if (flyHintCount / 60 > 0)
                                    showText = text
                                            + "\n"
                                            + LanguageManager.getLangByKey(LanguageKeys.FLYHINT_DOWN_MIN, String.valueOf(flyHintCount / 60));
                                else
                                    showText = text + "\n"
                                            + LanguageManager.getLangByKey(LanguageKeys.FLYHINT_DOWN_SECOND, String.valueOf(flyHintCount));
                            if (ChatServiceController.getCurrentActivity().getApplicationContext() != null) {
                                Toast toast = Toast.makeText(ChatServiceController.getCurrentActivity().getApplicationContext(), showText,
                                        Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP, 0, ChatServiceController.getInstance().getToastPosY());
                                toast.show();
                            }
                        } catch (Exception e) {
                            LogUtil.printException(e);
                        }
                    }
                });

                flyHintCount--;
                if (flyHintCount <= 0) {
                    stopFlyHintTimer();
                }
            }

        };
        flyHintTimer.schedule(timerTask, 0, 10000);
    }

    public static final int TYPE_CHAT = 0;
    public static final int TYPE_FORUM = 1;
    public static final int TYPE_MEMBER_SELECTOR = 2;

    public static void showChatActivity(Activity a, int channelType, boolean rememberPosition) {
        if (a == null)
            return;

        ChatFragmentNew.rememberPosition = rememberPosition;

        Intent intent = null;
        try {
            if (channelType >= 0) {
                // 可能出异常
                intent = new Intent(a, ChatActivity.class);
                intent.putExtra("channelType", channelType);
            }

            if (channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                LogUtil.trackPageView(!rememberPosition ? "ShowCountry" : "ShowCountryReturn");
            } else if (channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                LogUtil.trackPageView(!rememberPosition ? "ShowAlliance" : "ShowAllianceReturn");
            } else if (channelType == DBDefinition.CHANNEL_TYPE_USER) {
                LogUtil.trackPageView(!rememberPosition ? "ShowMail" : "ShowMailReturn");
            } else if (channelType == DBDefinition.CHANNEL_TYPE_CHATROOM) {
                LogUtil.trackPageView(!rememberPosition ? "ShowChatroom" : "ShowChatroomReturn");
            }
            LogController.getInstance().startChatTime = System.currentTimeMillis();
            showActivity(a, ChatActivity.class, true, true, intent, false, false);

        } catch (Exception e) {
            LogUtil.printException(e);
            return;
        }
    }

    public static void showForumActivity(int webViewType, Activity a, String forumUrl) {
        Intent intent = new Intent(a, ForumActivity.class);
        if (StringUtils.isNotEmpty(forumUrl)) {
            intent.putExtra("forumUrl", forumUrl);
        }
        switch (webViewType) {
            case ForumActivity.WEBVIEW_TYPE_FORFUM:
                LogUtil.trackPageView("ShowForum");
                break;
            case ForumActivity.WEBVIEW_TYPE_TRANSLATION_OPTIMIZATION:
                LogUtil.trackPageView("ShowTranslationOptimization");
                break;
            case ForumActivity.WEBVIEW_TYPE_SHARE:
                LogUtil.trackPageView("ShowShareLink");
                break;
        }
        intent.putExtra("webViewType", webViewType);
        showActivity(a, ForumActivity.class, true, true, intent, false, false);
    }

    public static void showMemberSelectorActivity(Activity a, boolean requestResult) {
        LogUtil.trackPageView("ShowMemberSelector");
        showActivity(a, MemberSelectorActivity.class, true, false, null, requestResult, false);
    }

    public static void showImageBucketActivity(Activity a) {
        showActivity(a, ImageBucketChooseActivity.class, true, false, null, false, false);
    }

    public static void showImageChooseActivity(Activity a, Intent intent) {
        showActivity(a, ImageChooseActivity.class, true, false, intent, false, false);
    }

    public static void showChannelListActivity(Activity a, boolean isSecondLvList, int channelType, String channelId, boolean isGoBack) {
        Intent intent = new Intent(a, ChannelListActivity.class);
        intent.putExtra("isSecondLvList", isSecondLvList);
        intent.putExtra("isGoBack", isGoBack);
        if (channelType >= 0)
            intent.putExtra("channelType", channelType);
        if (StringUtils.isNotEmpty(channelId))
            intent.putExtra("channelId", channelId);

        showActivity(a, ChannelListActivity.class, true, false, intent, false, isGoBack);
    }

    public static void showRecyclerMaillListActivity(Activity a, boolean isSecondLvList, int channelType, String channelId, boolean isGoBack) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "isSecondLvList", isSecondLvList, "channelType", channelType, "channelId", channelId, "isGoBack", isGoBack);
        Class<?> cls = null;
        if (!isSecondLvList) {
            if (ChannelManager.isMainMsgChannel(channelId))
                cls = RecyclerMsgListActivity.class;
            else
                cls = RecyclerMainListActivity.class;
        } else {
            cls = RecyclerSysMailActivity.class;
        }
        Intent intent = new Intent(a, cls);
        if (intent != null) {
            intent.putExtra("isSecondLvList", isSecondLvList);
            intent.putExtra("isGoBack", isGoBack);
            if (channelType >= 0)
                intent.putExtra("channelType", channelType);
            if (StringUtils.isNotEmpty(channelId))
                intent.putExtra("channelId", channelId);
        }

        showActivity(a, cls, true, false, intent, false, isGoBack);
    }

    
    public static void showMaillListActivity(Activity a, int channelType, String channelId) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "channelType", channelType, "channelId", channelId);
        Class<?> cls = MsgListActivity.class;;
        Intent intent = new Intent(a, cls);
        if (intent != null) {
            intent.putExtra("isSecondLvList", false);
            intent.putExtra("isGoBack", false);
            if (channelType >= 0)
                intent.putExtra("channelType", channelType);
            if (StringUtils.isNotEmpty(channelId))
                intent.putExtra("channelId", channelId);
        }
        showActivity(a, cls, true, true, intent, false, false);
    }
    
    public static void showMaillListActivity(Activity a, boolean isSecondLvList, int channelType, String channelId, boolean isGoBack) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "isSecondLvList", isSecondLvList, "channelType", channelType, "channelId", channelId, "isGoBack", isGoBack);
        Class<?> cls = null;
        if (!isSecondLvList) {
            if (ChannelManager.isMainMsgChannel(channelId))
                cls = MsgListActivity.class;
            else
                cls = RecyclerMainListActivity.class;
        } else {
            cls = RecyclerSysMailActivity.class;
        }
        Intent intent = new Intent(a, cls);
        if (intent != null) {
            intent.putExtra("isSecondLvList", isSecondLvList);
            intent.putExtra("isGoBack", isGoBack);
            if (channelType >= 0)
                intent.putExtra("channelType", channelType);
            if (StringUtils.isNotEmpty(channelId))
                intent.putExtra("channelId", channelId);
        }

        showActivity(a, cls, true, false, intent, false, isGoBack);
    }

    public static void showSecondChannelList(final int channelType, final String channelId, final boolean isGoBack) {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ChannelManager.isMainMsgChannel(channelId)) {
                            ChatChannel channel = ChannelManager.getInstance().getMainMsgChannelById(channelId);
                            if (channel != null) {


                                if (channelId.equals(MailManager.CHANNELID_MOD))
                                    channel.nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_TABNAME_MOD);
                                else if (channelId.equals(MailManager.CHANNELID_MESSAGE))
                                    channel.nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_MESSAGE);
                                else if (channelId.equals(MailManager.CHANNELID_DRIFTING_BOTTLE))
                                    channel.nameText = LanguageManager.getLangByKey(LanguageKeys.MAIL_NAME_DRIFTING_BOTTLE);
                                else if (channelId.equals(MailManager.CHANNELID_NEAR_BY)) {
                                    channel.nameText = LanguageManager.getLangByKey(LanguageKeys.TITLE_NEARBY_MSG);

                                }
                            }
                            showMaillListActivity(ChatServiceController.hostActivity, false, channelType, channelId, isGoBack);
                        } else
                            showChannelListActivity(ChatServiceController.hostActivity, true, channelType, channelId, isGoBack);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showSecondChannelListFrom2dx(final int channelType, final String channelId, final boolean isGoBack) {
        if (StringUtils.isNotEmpty(channelId) && channelId.equals(MailManager.CHANNELID_NEAR_BY)) {
            LogUtil.trackNearby("nearby_click_new_tip");
            NearByManager.getInstance().setEnter_list_type(2);
        }
        showSecondChannelList(channelType, channelId, isGoBack);
    }

    public static void playVideoActivity(Activity a, boolean isGoBack, String urlVideo, String urlThumb) {
        Intent intent = new Intent(a, PlayVideoActivity.class);
        intent.putExtra("isGoBack", isGoBack);
        intent.putExtra("urlVideo", urlVideo);
        intent.putExtra("urlThumb", urlThumb);
        showActivity(a, PlayVideoActivity.class, true, false, intent, false, isGoBack);
    }

    public static void showRecordVideoActivity(Activity a, boolean isGoBack) {
        Intent intent = new Intent(a, RecordVideoActivity.class);
        intent.putExtra("isGoBack", isGoBack);
        showActivity(a, RecordVideoActivity.class, true, false, intent, false, isGoBack);
    }

    public static void showPublishAllianceShareActivity(Activity a, boolean clearTop) {
        showActivity(a, AllianceShareActivity.class, true, clearTop, null, false, false);
    }

    public static void showAllianceShareListActivity(Activity a) {
        showActivity(a, AllianceShareListActivity.class, true, true, null, false, false);
    }

    public static void showChatRoomSettingActivity(Activity a) {
        showActivity(a, ChatRoomSettingActivity.class, true, false, null, false, false);
    }

    public static void showRealtimeVoiceRoomActivity() {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showRealtimeVoiceRoomActivity(ChatServiceController.hostActivity);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showRealtimeVoiceRoomActivity(Activity a) {
        showActivity(a, RealtimeVoiceRoomSettingActivity.class, true, false, null, false, false);
    }

    public static void showChatRoomNameModifyActivity(Activity a) {
        showActivity(a, ChatRoomNameModifyActivity.class, true, false, null, false, false);
    }

    public static void showImagePagerActivity(Activity a, Intent intent) {
        showActivity(a, ImagePagerActivity.class, true, false, intent, false, false);
    }

    public static void showAllianceShareDetailActivity(Activity a, Intent intent) {
        showActivity(a, AllianceShareDetailActivity.class, true, false, intent, false, false);
    }

    public static void showAllianceShareCommentListActivity(Activity a) {
        showActivity(a, AllianceShareCommentListActivity.class, true, false, null, false, false);
    }

    public static void showAudioActivity(Activity a, boolean isGoBack) {
        Intent intent = new Intent(a, MsgActivity.class);
        intent.putExtra("isGoBack", isGoBack);
        showActivity(a, MsgActivity.class, true, false, intent, false, isGoBack);
    }

    public static void showWriteMailActivity(Activity a, boolean clearTop, String roomName, String uidStr, String nameStr) {
        LogUtil.trackPageView("ShowWriteMail");
        Intent intent = null;

        if (StringUtils.isNotEmpty(roomName) || StringUtils.isNotEmpty(uidStr) || StringUtils.isNotEmpty(nameStr)) {
            intent = new Intent(a, WriteMailActivity.class);
            intent.putExtra("roomName", roomName);
            intent.putExtra("memberUids", uidStr);
            intent.putExtra("memberNames", nameStr);
        }

        showActivity(a, WriteMailActivity.class, true, clearTop, intent, false, clearTop);
    }

    private static void showActivity(Activity a, Class<?> cls, boolean newTask, boolean clearTop, Intent intent, boolean requestResult,
                                     boolean popStackAnimation) {
        ArrayList<Object> args = new ArrayList<Object>();
        args.add("class");
        args.add(cls.getSimpleName());
        if (intent != null) {
            for (Iterator<String> iterator = intent.getExtras().keySet().iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                args.add(key);
                args.add(intent.getExtras().get(key));
            }
        }
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_VIEW, (Object[]) args.toArray(new Object[0]));

        ChatServiceController.isNativeOpenning = true;
        ChatServiceController.isNativeShowing = true;
        ChatServiceController.isReturningToGame = false;
        ChannelListFragment.preventSecondChannelId = false;
        if (a instanceof ICocos2dxScreenLockListener) {
            MyActionBarActivity.previousActivity = (ICocos2dxScreenLockListener) a;
        }

        Intent i = intent != null ? intent : new Intent(a, cls);
        if (clearTop)
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!requestResult) {
            a.startActivity(i);
        } else {
            a.startActivityForResult(i, 0);
        }

        if (!popStackAnimation) {
            a.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            a.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    private static ArrayList<Activity> activityStack = new ArrayList<Activity>();

    public static void pushActivity(Activity a) {
        if (!activityStack.contains(a)) {
            activityStack.add(a);
        } else {
            LogUtil.printVariablesWithFuctionName(Log.WARN, LogUtil.TAG_VIEW, "pushActivity already have", activityStack.size());
        }
        LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "activityStack.size()", activityStack.size());
    }

    public static void popActivity(Activity a) {
        if (activityStack.contains(a)) {
            activityStack.remove(a);
        }
        LogUtil.printVariables(Log.INFO, LogUtil.TAG_VIEW, "activityStack.size()", activityStack.size());
    }

    public static int getNativeActivityCount() {
        return activityStack.size();
    }

    public static void clearActivityStack() {
        activityStack.clear();
    }

    public static void postIsChatRoomMemberFlag(String groupId, boolean flag) {
        ChannelManager.getInstance().setIsMemberFlag(groupId, flag);

        if (ChatServiceController.hostActivity == null)
            return;
        ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getChatFragment() != null) {
                        if (ChatServiceController.getChatFragment().isSelectMemberBtnEnable()) {
                            ChatServiceController.getChatFragment().refreshMemberSelectBtn();
                            ChatServiceController.getChatFragment().setSelectMemberBtnState();
                        }
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public static void connect2WS() {
        // SharePreferenceUtil.setPreferenceString(ChatServiceController.hostActivity, SharePreferenceUtil.getChatSessionTokenKey(), null);
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_STATUS);
        if (WebSocketManager.isWebSocketEnabled()) {
            if (!ChatServiceController.getInstance().isActuallyUsingDummyHost()) {
                WebSocketManager.getInstance().setStatusListener(WebSocketStatusHandler.getInstance());
            }
            WebSocketManager.getInstance().connect();
        }
    }

    public static void postChannelInfo(final String channelInfo) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "channelInfo", channelInfo);
        ChannelManager.getInstance().isGetingNewMsg = false;

        long startTime = System.currentTimeMillis();
        ChannelManager.getInstance().handleChannelInfo(channelInfo);

        if (ChatServiceController.hostActivity == null)
            return;
        ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ChatServiceController.getChatFragment() != null) {
                        ChatServiceController.getChatFragment().refreshToolTip();
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
        long offsetTime = System.currentTimeMillis() - startTime;
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "handleChannelInfo耗时", offsetTime);
    }

    public static String getChannelInfo() {
        ConfigManager.setMailPullState(true);
        String result = "";
        try {
            result = ChannelManager.getInstance().getChannelInfo();
            if (StringUtils.isNotEmpty(result))
                ChannelManager.getInstance().isGetingNewMsg = true;
        } catch (Exception e) {
            LogUtil.printException(e);
        }
        return result;
    }

    public static void setAutoTranlateMode(int mode) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "mode", mode);
        ConfigManager.autoTranlateMode = mode;
    }

    public static void setLangSettingAutoTranslateEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "enable", enable);
        ConfigManager.autoTranlateEnableBySetting = enable;
    }

    public static void postAutoTranlateMode(boolean auto_client_translate, boolean client_translate_google, boolean auto_translate_by_user) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "auto_client_translate", auto_client_translate, "client_translate_google", client_translate_google, "auto_translate_by_user",
                auto_translate_by_user);
        ConfigManager.autoTranlateEnable = auto_client_translate;
        ConfigManager.autoTranlateByGoogle = client_translate_google;
        ConfigManager.autoTranlateEnableBySetting = auto_translate_by_user;
    }

    public static void setDisableLang(String disableLang) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "disableLang", disableLang);
        TranslateManager.getInstance().disableLang = disableLang;
    }

    public static void setMailSave(String mailId, int saveFlag) {
        MailData mail = DBManager.getInstance().getSysMailByID(mailId);
        if (mail != null) {
            if (SwitchUtils.mailStatusEnable) {
                // 从游戏那边拿过来的数据在数据库里更新状态
                mail.setSave(saveFlag);
                // 更新数据库里的mail 信息
                DBManager.getInstance().updateMail(mail);
            } else {
                // 如果是关闭状态的话就不做处理
            }

            ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());

            if (channel == null || channel.mailDataList == null)
                return;
            channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
            DBManager.getInstance().updateChannel(channel);

            MailData changeMail = null;
            boolean needMoveToTrash = false;

            for (int i = 0; i < channel.mailDataList.size(); i++) {
                MailData mailData = channel.mailDataList.get(i);
                if (mailData != null && mailId.equals(mailData.getUid())) {
                    if (mailData.getSave() != saveFlag) {
                        mailData.setSave(saveFlag);
                        DBManager.getInstance().updateMail(mailData);
                        changeMail = mailData;
                        if (/*
                             * (saveFlag == 1 &&
							 * channel.channelID.equals(MailManager
							 * .CHANNELID_RECYCLE_BIN)) ||
							 */(saveFlag == 0 && StringUtils.isNotEmpty(mail.getRewardId()) && mail.getRewardStatus() == 1 && !mail
                                .isUnread())) {
                            needMoveToTrash = true;
                        }
                    }
                    break;
                }
            }

            if (changeMail != null) {

                if (needMoveToTrash) {
                    ChatChannel originalChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
                            changeMail.getChannelId());
                    if (originalChannel != null) {
                        if (originalChannel.channelID.equals(MailManager.CHANNELID_RECYCLE_BIN))
                            changeMail.setRecycleTime(TimeManager.getInstance().getCurrentTimeMS());
                        else
                            changeMail.setRecycleTime(-1);
                        originalChannel.addNewMailData(changeMail);
                    }
                    channel.mailDataList.remove(changeMail);
                    channel.mailUidList.remove(changeMail.getUid());
                }

                ChannelListFragment.onMailAdded();
            }

        }
    }

    private static boolean moveMailToTrash(ChatChannel channel, String mailId) {
        if (StringUtils.isEmpty(mailId))
            return false;
        boolean hasUnread = false;
        MailData deleteMail = null;
        if (channel != null && channel.mailDataList != null && channel.mailDataList.size() > 0) {
            for (int i = 0; i < channel.mailDataList.size(); i++) {
                MailData mailData = channel.mailDataList.get(i);
                if (mailData != null && mailId.equals(mailData.getUid())) {
                    if (mailData.isUnread()) {
                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailItem.isUnread()", mailData.isUnread());
                        mailData.setStatus(1);
                        channel.unreadCount--;
                        hasUnread = true;
                    }
                    if (mailData.hasReward())
                        mailData.setRewardStatus(1);
                    deleteMail = mailData;
                    break;
                }
            }

        }

        if (deleteMail == null) {
            deleteMail = DBManager.getInstance().getSysMailByID(mailId);
            if (deleteMail != null) {
                deleteMail.setNeedParseByForce(true);
                deleteMail = MailManager.getInstance().parseMailDataContent(deleteMail);
                if (deleteMail.isUnread()) {
                    deleteMail.setStatus(1);
                    hasUnread = true;
                    ChannelManager.getInstance().calulateAllChannelUnreadNum();
                }
                if (deleteMail.hasReward())
                    deleteMail.setRewardStatus(1);
            }
        }

        if (deleteMail != null) {
            ChatChannel recycleBinChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
                    MailManager.CHANNELID_RECYCLE_BIN);
            if (recycleBinChannel != null) {
                deleteMail.setRecycleTime(TimeManager.getInstance().getCurrentTimeMS());
                recycleBinChannel.addNewMailData(deleteMail);
            }
            DBManager.getInstance().updateMail(deleteMail);
            if (channel != null) {
                if (channel.mailDataList != null && channel.mailDataList.size() > 0)
                    channel.mailDataList.remove(deleteMail);
                if (channel.mailUidList != null && channel.mailUidList.size() > 0)
                    channel.mailUidList.remove(deleteMail.getUid());
            }
            ChannelListFragment.onMailAdded();
        }
        return hasUnread;
    }

    public static void setMailRewardStatus(String mailId) {
        MailData mail = DBManager.getInstance().getSysMailByID(mailId);
        if (mail != null) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailId", mailId, "channelId", mail.getChannelId());
            if (moveMailToTrash(channel, mailId)) {
                channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                DBManager.getInstance().updateChannel(channel);
                JniController.getInstance().excuteJNIVoidMethod("deleteMutiMailFromServer", new Object[]{mailId});
            }
            if (mail.isUnread()) {
                if (ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 1)
                    DBManager.getInstance().getLatestUnReadReportByType(1);
                else if (ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 2)
                    DBManager.getInstance().getLatestUnReadReportByType(2);
            }
        }
    }

    public static void setMutiMailRewardStatusInDB(final String mailUids) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids);
        if (StringUtils.isEmpty(mailUids))
            return;

        Runnable runable = new Runnable() {

            @Override
            public void run() {
                try {
                    LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "start setMutiMailRewardStatusInDB", mailUids);
                    boolean hasOldNewBattleMail = false;
                    boolean hasOldNewDetectMail = false;
                    String[] mailUidArr = mailUids.split(",");

                    List<ChatChannel> channelList = new ArrayList<ChatChannel>();
                    List<String> channelIdList = DBManager.getInstance().getUpdateChannelIds(mailUids);
                    if (channelIdList != null) {
                        for (String channelId : channelIdList) {
                            if (StringUtils.isNotEmpty(channelId)) {
                                ChatChannel sysChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
                                if (sysChannel != null && !channelList.contains(sysChannel))
                                    channelList.add(sysChannel);
                            }
                        }
                    }

                    for (int i = 0; i < mailUidArr.length; i++) {
                        if (StringUtils.isNotEmpty(mailUidArr[i])) {
                            if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mailUidArr[i]) == 1)
                                hasOldNewBattleMail = true;
                            else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mailUidArr[i]) == 2)
                                hasOldNewDetectMail = true;
                        }
                    }

                    DBManager.getInstance().updateSystemMailInDB(mailUids, DBManager.CONFIG_TYPE_REWARD, false);

                    for (ChatChannel channel : channelList) {
                        if (channel != null) {
                            channel.queryUnreadSysMailCountFromDB();
                            channel.unreadCount = channel.getUnreadSysMailCountInDB();
                            channel.querySysMailCountFromDB();
                            channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                            DBManager.getInstance().updateChannel(channel);
                        }
                    }

                    ChannelManager.getInstance().parseChannelList(channelList);
                    ChannelManager.getInstance().calulateAllChannelUnreadNum();

                    if (hasOldNewBattleMail)
                        DBManager.getInstance().getLatestUnReadReportByType(1);
                    if (hasOldNewDetectMail)
                        DBManager.getInstance().getLatestUnReadReportByType(2);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };
        MailManager.getInstance().runOnExecutorService(runable);

    }

    public static void setMutiMailRewardStatus(String mailUids) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids);
        if (StringUtils.isEmpty(mailUids))
            return;

        if (MailManager.cocosMailListEnable) {
            setMutiMailRewardStatusInDB(mailUids);
            return;
        }

        try {
            boolean hasOldNewBattleMail = false;
            boolean hasOldNewDetectMail = false;
            String[] mailUidArr = mailUids.split(",");
            String deleteUids = "";
            for (int i = 0; i < mailUidArr.length; i++) {
                if (StringUtils.isNotEmpty(mailUidArr[i])) {
                    MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[i]);
                    if (mail != null) {
                        if (mail.isUnread()) {
                            if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 1)
                                hasOldNewBattleMail = true;
                            else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 2)
                                hasOldNewDetectMail = true;
                        }

                        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL,
                                mail.getChannelId());

                        if (channel == null || channel.mailDataList == null)
                            return;
                        if (moveMailToTrash(channel, mailUidArr[i])) {
                            channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                            DBManager.getInstance().updateChannel(channel);
                            deleteUids = ChannelManager.appendStr(deleteUids, mailUidArr[i]);
                        }

                    }
                }
            }

            if (hasOldNewBattleMail)
                DBManager.getInstance().getLatestUnReadReportByType(1);
            if (hasOldNewDetectMail)
                DBManager.getInstance().getLatestUnReadReportByType(2);

            if (StringUtils.isNotEmpty(deleteUids)) {
                JniController.getInstance().excuteJNIVoidMethod("deleteMutiMailFromServer", new Object[]{deleteUids});
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        ChannelManager.getInstance().calulateAllChannelUnreadNum();
        // if (ChatServiceController.hostActivity == null)
        // return;
        // ChatServiceController.hostActivity.runOnUiThread(new Runnable()
        // {
        // @Override
        // public void run()
        // {
        // try
        // {
        // if (ChatServiceController.getChannelListFragment() != null)
        // {
        // ChatServiceController.getChannelListFragment().notifyDataSetChanged();
        // }
        // }
        // catch (Exception e)
        // {
        // LogUtil.printException(e);
        // }
        // }
        // });
    }

    private static boolean isKnightMail(String[] mailUidArr) {
        if (mailUidArr.length > 0) {
            MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[0]);
            if (mail != null) {
                mail.setNeedParseByForce(true);
                MailData mailData = MailManager.getInstance().parseMailDataContent(mail);
                if (mailData != null && mailData.isKnightMail())
                    return true;
            }
        }
        return false;
    }

    public static void setMutiChatMailStatusByConfigType(String fromUids, int configType, int contactMode) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "fromUids", fromUids, "configType", configType, "contactMode",
                contactMode);

        if (StringUtils.isEmpty(fromUids)
                || !(configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_SAVE || configType == DBManager.CONFIG_TYPE_DELETE))
            return;
        String[] fromUidArr = fromUids.split(",");

        boolean hasOldNewUserChat = false;
        for (int i = 0; i < fromUidArr.length; i++) {
            if (StringUtils.isNotEmpty(fromUidArr[i])) {
                String channelId = fromUidArr[i];
                if (ChatServiceController.isModContactMod(contactMode) && !channelId.equals(DBDefinition.CHANNEL_ID_POSTFIX_MOD))
                    channelId += DBDefinition.CHANNEL_ID_POSTFIX_MOD;
                else if (ChatServiceController.isDriftingBottleContactMod(contactMode) && !channelId.equals(DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE))
                    channelId += DBDefinition.CHANNEL_ID_POSTFIX_DRIFTING_BOTTLE;
                else if (ChatServiceController.isNearbyContactMod(contactMode) && !channelId.equals(DBDefinition.CHANNEL_ID_POSTFIX_NEARBY))
                    channelId += DBDefinition.CHANNEL_ID_POSTFIX_NEARBY;
                String channelId1 = channelId;
                if (channelId1.startsWith(DBDefinition.CHANNEL_ID_PREFIX_STANDALONG))
                    channelId1 = channelId1.substring((DBDefinition.CHANNEL_ID_PREFIX_STANDALONG).length());
                ChatTable chatTable1 = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_USER, channelId1);
                ChatChannel channel1 = ChannelManager.getInstance().getChannel(chatTable1);
                if ((configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_DELETE) && !hasOldNewUserChat
                        && channel1 != null && channel1.isUnread() && ChannelManager.getInstance().hasNewestUserChat(channel1.channelID))
                    hasOldNewUserChat = true;

                if (channel1 != null && configType == DBManager.CONFIG_TYPE_DELETE)
                    ChannelManager.getInstance().deleteChannel(channel1);

                String channelId2 = channelId;
                if (!channelId2.startsWith(DBDefinition.CHANNEL_ID_PREFIX_STANDALONG))
                    channelId2 = DBDefinition.CHANNEL_ID_PREFIX_STANDALONG + channelId2;
                ChatTable chatTable2 = ChatTable.createChatTable(DBDefinition.CHANNEL_TYPE_USER, channelId2);
                ChatChannel channel2 = ChannelManager.getInstance().getChannel(chatTable2);
                if (channel2 != null && configType == DBManager.CONFIG_TYPE_DELETE)
                    ChannelManager.getInstance().deleteChannel(channel2);
                if ((configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_DELETE) && !hasOldNewUserChat
                        && channel2 != null && channel2.isUnread() && ChannelManager.getInstance().hasNewestUserChat(channel2.channelID))
                    hasOldNewUserChat = true;
            }
        }

        List<ChatChannel> channelList = new ArrayList<ChatChannel>();
        ChatChannel channel = null;
        if (contactMode == 0)
            channel = ChannelManager.getInstance().getMessageChannel();
        else if (contactMode == 2)
            channel = ChannelManager.getInstance().getDriftingBottleChannel();
        else if (contactMode == 3)
            channel = ChannelManager.getInstance().getNearbyChannel();
        else if (contactMode == 1)
            channel = ChannelManager.getInstance().getModChannel();
        if (channel != null && !channelList.contains(channel))
            channelList.add(channel);
        ChannelManager.getInstance().parseChannelList(channelList);

        if (hasOldNewUserChat)
            ChannelManager.getInstance().getNewUserChatChannelId();

        if (configType == DBManager.CONFIG_TYPE_DELETE) {
            if (ChatServiceController.getBaseListActivity() != null)
                ChatServiceController.getBaseListActivity().afterDeleteMsgChannel();
            if (ChatServiceController.getChannelListFragment() != null)
                ChatServiceController.getChannelListFragment().afterDeleteMsgChannel();
        }
    }

    public static void setMutiMailStatusByConfigTypeInDB(final String mailUids, final int configType, final boolean isUnLock, final boolean isKnightMail) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids, "configType", configType, "isUnLock",
                isUnLock, "isKnightMail", isKnightMail);

        if (StringUtils.isEmpty(mailUids)
                || !(configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_SAVE || configType == DBManager.CONFIG_TYPE_DELETE))
            return;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                List<ChatChannel> channelList = new ArrayList<ChatChannel>();

                if (configType == DBManager.CONFIG_TYPE_DELETE && isKnightMail) {
                    ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_KNIGHT);
                    if (channel != null) {
                        channel.unreadCount = 0;
                        channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();

                        channelList.add(channel);
                        ChannelManager.getInstance().parseChannelList(channelList);
                        ChannelManager.getInstance().calulateAllChannelUnreadNum();

                        ChannelManager.getInstance().removeChannelFromMap(channel.getChatTable().getChannelName());
                        DBManager.getInstance().deleteDialogMailChannel(channel.getChatTable());
                    }
                } else {
                    String[] mailUidArr = mailUids.split(",");
                    boolean hasDetectMail = false;
                    boolean hasOldNewBattleMail = false;
                    boolean hasOldNewDetectMail = false;

                    List<String> channelIdList = DBManager.getInstance().getUpdateChannelIds(mailUids);
                    if (channelIdList != null) {
                        for (String channelId : channelIdList) {
                            if (StringUtils.isNotEmpty(channelId)) {
                                ChatChannel sysChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
                                if (sysChannel != null && !channelList.contains(sysChannel))
                                    channelList.add(sysChannel);
                            }
                        }
                    }

                    try {
                        for (int i = 0; i < mailUidArr.length; i++) {
                            if (StringUtils.isNotEmpty(mailUidArr[i])) {
                                if (configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_DELETE) {
                                    if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mailUidArr[i]) == 1)
                                        hasOldNewBattleMail = true;
                                    else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mailUidArr[i]) == 2)
                                        hasOldNewDetectMail = true;
                                }

                                // if (!hasDetectMail && mail.getType() == MailManager.MAIL_DETECT_REPORT)
                                // hasDetectMail = true;
                            }
                        }

                        DBManager.getInstance().updateSystemMailInDB(mailUids, configType, isUnLock);

                        for (ChatChannel channel : channelList) {
                            if (channel != null) {
                                if (configType != DBManager.CONFIG_TYPE_SAVE) {
                                    channel.queryUnreadSysMailCountFromDB();
                                    channel.unreadCount = channel.getUnreadSysMailCountInDB();
                                }

                                if (configType == DBManager.CONFIG_TYPE_READ) {
                                    channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                                    DBManager.getInstance().updateChannel(channel);
                                } else if (configType == DBManager.CONFIG_TYPE_DELETE) {
                                    channel.querySysMailCountFromDB();
                                }
                            }
                        }

                        if (hasOldNewBattleMail)
                            DBManager.getInstance().getLatestUnReadReportByType(1);
                        if (hasOldNewDetectMail)
                            DBManager.getInstance().getLatestUnReadReportByType(2);

                        if (hasDetectMail)
                            DBManager.getInstance().getDetectMailInfo();

                        ChannelManager.getInstance().parseChannelList(channelList);

                        ChannelManager.getInstance().calulateAllChannelUnreadNum();

                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            }
        };

        MailManager.getInstance().runOnExecutorService(runnable);
    }

    public static void setMutiMailStatusByConfigType(String mailUids, int configType, boolean isUnLock, boolean isKnightMail) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", mailUids, "configType", configType, "isUnLock",
                isUnLock, "isKnightMail", isKnightMail);

        if (StringUtils.isEmpty(mailUids)
                || !(configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_SAVE || configType == DBManager.CONFIG_TYPE_DELETE))
            return;
        String[] mailUidArr = mailUids.split(",");
        boolean hasDetectMail = false;
        boolean hasOldNewBattleMail = false;
        boolean hasOldNewDetectMail = false;
        ChatChannel channel = null;

        if (configType == DBManager.CONFIG_TYPE_DELETE && isKnightMail(mailUidArr)) {
            deleteMail("knight", DBDefinition.CHANNEL_TYPE_OFFICIAL, -1);
            return;
        }

        try {
            List<ChatChannel> channelList = new ArrayList<ChatChannel>();
            for (int i = 0; i < mailUidArr.length; i++) {
                if (StringUtils.isNotEmpty(mailUidArr[i])) {
                    MailData mail = DBManager.getInstance().getSysMailByID(mailUidArr[i]);
                    if (mail != null) {
                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mail.isUnread()", mail.isUnread());
                        channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail.getChannelId());

                        if (channel != null && !channelList.contains(channel))
                            channelList.add(channel);
                        if (configType == DBManager.CONFIG_TYPE_READ && mail.isUnread()) {
                            if (channel != null)
                                channel.unreadCount--;
                            mail.setStatus(1);
                            DBManager.getInstance().updateMail(mail);
                            if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 1)
                                hasOldNewBattleMail = true;
                            else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 2)
                                hasOldNewDetectMail = true;
                        } else if (configType == DBManager.CONFIG_TYPE_SAVE) {
                            if (!isUnLock && !mail.isLock()) {
                                mail.setSave(1);
                                DBManager.getInstance().updateMail(mail);
                            } else if (isUnLock && mail.isLock()) {
                                mail.setSave(0);
                                DBManager.getInstance().updateMail(mail);
                            }
                        } else if (configType == DBManager.CONFIG_TYPE_DELETE) {
                            if (mail.isUnread()) {
                                if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 1)
                                    hasOldNewBattleMail = true;
                                else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 2)
                                    hasOldNewDetectMail = true;
                            }
                            ChannelManager.getInstance().deleteSysMailFromChannel(channel, mail, true);
                            if (!hasDetectMail && mail.getType() == MailManager.MAIL_DETECT_REPORT)
                                hasDetectMail = true;
                            continue;
                        }

                        if (channel != null && channel.mailDataList != null) {
                            for (int j = 0; j < channel.mailDataList.size(); j++) {
                                MailData mailData = channel.mailDataList.get(j);
                                if (mailData != null && mailUidArr[i].equals(mailData.getUid())) {
                                    if (configType == DBManager.CONFIG_TYPE_READ && mailData.isUnread()) {
                                        mailData.setStatus(1);
                                        if (StringUtils.isNotEmpty(mailData.getRewardId()) && mailData.getRewardStatus() == 1) {
                                            ChatChannel recycleBinChannel = ChannelManager.getInstance().getChannel(
                                                    DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_RECYCLE_BIN);
                                            if (recycleBinChannel != null) {
                                                mailData.setRecycleTime(TimeManager.getInstance().getCurrentTimeMS());
                                                recycleBinChannel.addNewMailData(mailData);
                                            }
                                            DBManager.getInstance().updateMail(mailData);
                                            channel.mailDataList.remove(mailData);
                                            channel.mailUidList.remove(mailData.getUid());
                                            ChannelListFragment.onMailAdded();
                                        }
                                        break;
                                    } else if (configType == DBManager.CONFIG_TYPE_SAVE) {
                                        if (!isUnLock && !mailData.isLock()) {
                                            mailData.setSave(1);
                                        } else if (isUnLock && mailData.isLock()) {
                                            mailData.setSave(0);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        if (configType == DBManager.CONFIG_TYPE_READ) {
                            channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                            DBManager.getInstance().updateChannel(channel);
                        }
                    }

                }
            }

            if (hasOldNewBattleMail)
                DBManager.getInstance().getLatestUnReadReportByType(1);
            if (hasOldNewDetectMail)
                DBManager.getInstance().getLatestUnReadReportByType(2);

            if (hasDetectMail)
                DBManager.getInstance().getDetectMailInfo();
            if (channel != null && channel.channelType == DBDefinition.CHANNEL_TYPE_OFFICIAL && configType == DBManager.CONFIG_TYPE_DELETE) {
                channel.querySysMailCountFromDB();
                channel.queryUnreadSysMailCountFromDB();
            }

            ChannelManager.getInstance().calulateAllChannelUnreadNum();
            ChannelManager.getInstance().parseChannelList(channelList);

            if (ChatServiceController.getChannelListFragment() != null)
                ChatServiceController.getChannelListFragment().notifyDataSetChanged();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    public static void setMutiMailStatusByChannelID(String channelId, int configType, boolean isUnLock) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "channelId", channelId, "configType", configType, "isUnLock", isUnLock);
        if (StringUtils.isEmpty(channelId))
            return;

        boolean hasOldNewBattleMail = false;
        boolean hasOldNewDetectMail = false;
        List<ChatChannel> channelList = new ArrayList<ChatChannel>();

        if (channelId.equals(MailManager.CHANNELID_RESOURCE) || channelId.equals(MailManager.CHANNELID_RESOURCE_HELP)
                || channelId.equals(MailManager.CHANNELID_MONSTER) || channelId.equals(MailManager.CHANNELID_NEW_WORLD_BOSS)) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
            if (channel != null) {
                channelList.add(channel);
                List<String> unreadUids = channel.getMailUidArrayByConfigType(configType);
                for (int i = 0; i < unreadUids.size(); i++) {
                    String uid = unreadUids.get(i);
                    if (StringUtils.isNotEmpty(uid)) {
                        MailData mail = DBManager.getInstance().getSysMailByID(uid);
                        if (mail != null) {
                            if ((configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_DELETE) && mail.isUnread()) {
                                if (configType == DBManager.CONFIG_TYPE_READ) {
                                    mail.setStatus(1);
                                    DBManager.getInstance().updateMail(mail);
                                }
                                if (!hasOldNewBattleMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 1)
                                    hasOldNewBattleMail = true;
                                else if (!hasOldNewDetectMail && ChannelManager.getInstance().hasNewestReport(mail.getUid()) == 2)
                                    hasOldNewDetectMail = true;
                            } else if (configType == DBManager.CONFIG_TYPE_SAVE) {
                                if (!isUnLock && !mail.isLock()) {
                                    mail.setSave(1);
                                    DBManager.getInstance().updateMail(mail);
                                } else if (isUnLock && mail.isLock()) {
                                    mail.setSave(0);
                                    DBManager.getInstance().updateMail(mail);
                                }
                            }
                        }
                    }
                }

                for (int j = 0; j < channel.mailDataList.size(); j++) {
                    MailData mailData = channel.mailDataList.get(j);
                    if (mailData != null) {
                        if (configType == DBManager.CONFIG_TYPE_READ && mailData.isUnread()) {
                            mailData.setStatus(1);
                        } else if (configType == DBManager.CONFIG_TYPE_SAVE) {
                            if (!isUnLock && !mailData.isLock()) {
                                mailData.setSave(1);
                            } else if (isUnLock && mailData.isLock()) {
                                mailData.setSave(0);
                            }
                        }
                    }
                }

                if (configType == DBManager.CONFIG_TYPE_READ) {
                    channel.unreadCount = 0;
                    channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                    DBManager.getInstance().updateChannel(channel);
                    ChannelManager.getInstance().calulateAllChannelUnreadNum();
                }

            }
        } else if (ChannelManager.isMainMsgChannel(channelId)) {
            List<ChatChannel> channelArr = ChannelManager.getInstance().getAllMsgChannelById(channelId);
            if (channelArr != null && channelArr.size() > 0) {
                for (int i = 0; i < channelArr.size(); i++) {
                    ChatChannel channel = channelArr.get(i);
                    if (channel != null)
                        channel.markAsRead();
                }
            }

            ChatChannel channel = ChannelManager.getInstance().getMainMsgChannelById(channelId);

            if (channel != null) {
                if (!channelList.contains(channel))
                    channelList.add(channel);
                channel.unreadCount = 0;
                channel.latestModifyTime = TimeManager.getInstance().getCurrentTimeMS();
                DBManager.getInstance().updateChannel(channel);
                ChannelManager.getInstance().calulateAllChannelUnreadNum();
            }
        }

        ChannelManager.getInstance().parseChannelList(channelList);

        if (hasOldNewBattleMail)
            DBManager.getInstance().getLatestUnReadReportByType(1);
        if (hasOldNewDetectMail)
            DBManager.getInstance().getLatestUnReadReportByType(2);

        if (ChatServiceController.getChannelListFragment() != null)
            ChatServiceController.getChannelListFragment().notifyDataSetChanged();
    }

    public static void setMutiMailStatusByType(int type, int configType, boolean isUnLock) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_MSG, "mailUids", type, "configType", configType, "isUnLock", isUnLock);

        if (type < 0
                || !(configType == DBManager.CONFIG_TYPE_READ || configType == DBManager.CONFIG_TYPE_SAVE || configType == DBManager.CONFIG_TYPE_DELETE))
            return;
        String channelId = "";
        if (type == MailManager.MAIL_RESOURCE)
            channelId = MailManager.CHANNELID_RESOURCE;
        else if (type == MailManager.MAIL_RESOURCE_HELP)
            channelId = MailManager.CHANNELID_RESOURCE_HELP;
        else if (type == MailManager.MAIL_ATTACKMONSTER)
            channelId = MailManager.CHANNELID_MONSTER;
        else if (type == MailManager.MAIL_NEW_WORLD_BOSS)
            channelId = MailManager.CHANNELID_NEW_WORLD_BOSS;
        else if (type == MailManager.MAIL_MOD_PERSONAL || type == MailManager.MAIL_MOD_AUDIO_SELF_SEND ||
                type == MailManager.MAIL_MOD_SEND || type == MailManager.MAIL_MOD_AUDIO_OTHER_SEND)
            channelId = MailManager.CHANNELID_MOD;
        else if (type == MailManager.MAIL_DRIFTING_BOTTLE_SELF_SEND || type == MailManager.MAIL_DRIFTING_BOTTLE_OTHER_SEND)
            channelId = MailManager.CHANNELID_DRIFTING_BOTTLE;
        else if (type == MailManager.MAIL_SELF_SEND || type == MailManager.MAIL_USER || type == MailManager.MAIL_Alliance_ALL
                || type == MailManager.MAIL_AUDIO_SELF_SEND || type == MailManager.MAIL_AUDIO_OTHER_SEND
                || type == MailManager.MAIL_EXPRESSION_USER_SELF_SEND || type == MailManager.MAIL_EXPRESSION_USER_OTHER_SEND)
            channelId = MailManager.CHANNELID_MESSAGE;
        setMutiMailStatusByChannelID(channelId, configType, isUnLock);
    }

    public static void postMailUpdate(final String updateData) {
        if (updateData.equals(""))
            return;

        Runnable runable = new Runnable() {

            @Override
            public void run() {
                ChannelManager.mailUpdateData = updateData;
                try {
                    System.out.println("updateData:" + updateData);
                    MailUpdateData updateDate = JSON.parseObject(updateData, MailUpdateData.class);
                    ChannelManager.getInstance().updateMailData(updateDate);

                    System.out.println("updateData end");
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };

        MailManager.getInstance().runOnExecutorService(runable);

    }

    public static String getLastMailUpdateTime() {
        String ret = "";
        long latestModifyTime = ChannelManager.getInstance().getLatestSysMailModifyTime();

        if (latestModifyTime > 0) {
            ret = Long.toString(latestModifyTime);
        }
        return ret;
    }

    public static void postMailDealStatus(String mailUid) {
        if (mailUid.equals(""))
            return;
        ChannelManager.getInstance().dealMailFrom2dx(mailUid);
    }

    public static void postTranslatedResult(String jsonRet) {
        if (StringUtils.isNotEmpty(jsonRet)) {
            try {
                TranslatedByLuaResult result = JSON.parseObject(jsonRet, TranslatedByLuaResult.class);
                if (result != null && StringUtils.isNotEmpty(result.getOriginalMsg())
                        && TranslateManager.getInstance().isInTranslateQueue(result.getOriginalMsg())) {
                    TranslateManager.getInstance().handleTranslateResult(result);
                }
            } catch (Exception e) {
                LogUtil.printException(e);
            }
        }
    }

    public static void postTranslateByLuaStart() {
        TranslateManager.isTranslatedByLuaStart = true;
    }

    public static void postUIShow() {
        TranslateManager.isUIShow = true;
    }

    public static void setMailNewUIEnable(boolean newUIEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "newUIEnable", newUIEnable);
        ChatServiceController.isNewMailUIEnable = newUIEnable;
    }

    public static void setMailSortType(int sortType) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "sortType", sortType);
        ChatServiceController.sortType = sortType;
    }

    public static boolean isStickMsg(String msg) {
        return StickManager.getPredefinedEmoj(msg) != null;
    }

    public static void setDefaultTranslateEnable(boolean isEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
        ChatServiceController.isDefaultTranslateEnable = isEnable;
    }

    public static void setFriendEnable(boolean isEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
        ChatServiceController.isFriendEnable = isEnable;
    }

    public static void setDetectInfoEnable(boolean isEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
        ChatServiceController.isDetectInfoEnable = isEnable;
    }

    public static void setStandaloneServerEnable(int keyIndex, boolean isEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "keyIndex", keyIndex, "isEnable", isEnable);
        switch (keyIndex) {
            case 1:
                ConfigManager.useWebSocketServer = isEnable;
                break;
            case 2:
                ConfigManager.isRecieveFromWebSocket = isEnable;
                break;
            // case 3:
            // ConfigManager.isSendFromWebSocket = isEnable;
            // break;
        }
    }

    public static void setWebSocketSendEnable(boolean isEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "isEnable", isEnable);
        ConfigManager.isSendFromWebSocket = isEnable;
    }

    public static void setXMEnable(int keyIndex, boolean isEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE, "keyIndex", keyIndex, "isEnable", isEnable);
        switch (keyIndex) {
            case 1:
                ConfigManager.isXMEnabled = isEnable;
                break;
            case 2:
                ConfigManager.isXMAudioEnabled = isEnable;
                break;
            case 3:
                ConfigManager.isXMVedioEnabled = isEnable;
                break;
        }
    }

    public static void rmDataBaseFile() {
        DBManager.getInstance().rmDatabaseFile();
    }

    public static void getDetectMailByMailUid(String mailUid) {
        if (StringUtils.isNotEmpty(mailUid)) {
            MailData mail = DBManager.getInstance().getSysMailByID(mailUid);
            if (mail != null) {
                if (MailManager.cocosMailListEnable) {
                    mail.setNeedParseByForce(false);
                    mail = MailManager.getInstance().parseMailDataContent(mail);
                    mail = ServiceInterface.handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mail, true, false);
                } else {
                    mail.setNeedParseByForce(true);
                    mail = MailManager.getInstance().parseMailDataContent(mail);
                }

                try {
                    String jsonStr = JSON.toJSONString(mail);
                    MailManager.getInstance().transportMailInfo(jsonStr, MailManager.SHOW_ITEM_DETECT_REPORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showActivitySysMail(String mailUid) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid);
        if (StringUtils.isNotEmpty(mailUid)) {
            MailData mail = DBManager.getInstance().getSysMailByID(mailUid);
            if (MailManager.getInstance().isInTransportedMailList(mailUid)) {
                System.out.println("showActivitySysMail isInTransportedMailList");
                MailManager.getInstance().setShowingMailUid("");

                if (mail != null && mail.hasReward()) {
                    LocalConfig config = ConfigManager.getInstance().getLocalConfig();
                    boolean hasShowed = (config != null && config.isFirstRewardTipShowed());
                    JniController.getInstance().excuteJNIVoidMethod("postFirstRewardAnimationShowed", new Object[]{Boolean.valueOf(hasShowed)});
                }
                JniController.getInstance().excuteJNIVoidMethod("setActionAfterResume",
                        new Object[]{"showMailPopup", mailUid, "", "", Boolean.valueOf(false)});
            } else {
                if (mail != null) {
                    System.out.println("showActivitySysMail isNotInTransportedMailList");
                    showActivityMail(mail);
                } else {
                    MailManager.getInstance().setDelayShowActivityMailUid(mailUid);
                }
            }
        }
    }

    public static void showActivityMail(MailData mail) {
        if (mail != null) {
            MailData mailData = null;
            if (MailManager.cocosMailListEnable) {
                mail.setNeedParseByForce(false);
                mailData = MailManager.getInstance().parseMailDataContent(mail);
                mailData = ServiceInterface.handleMailData(DBDefinition.CHANNEL_TYPE_OFFICIAL, mailData, true, false);
            } else {
                mail.setNeedParseByForce(true);
                mailData = MailManager.getInstance().parseMailDataContent(mail);
            }
            if (mailData != null) {
                try {
                    String jsonStr = JSON.toJSONString(mailData);
                    MailManager.getInstance().transportMailInfo(jsonStr, MailManager.SHOW_ITEM_ACTIVITY);
                    MailManager.getInstance().setDelayShowActivityMailUid("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loadMoreMailFromAndroid(String channelId) {
        if (StringUtils.isNotEmpty(channelId)) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
            if (channel != null && channel.mailDataList != null && channel.mailDataList.size() > 0) {
                MailData lastMail = channel.mailDataList.get(channel.mailDataList.size() - 1);
                if (lastMail != null) {
                    ChannelManager.getInstance().loadMoreSysMailFromDB(channel, lastMail.getCreateTime());
                }
            }
        }
    }

    public static void setChannelPopupOpen(String channelId) {
        ChannelManager.currentOpenedChannel = channelId;
    }

    public static void postMutiRewardItem(String jsonStr) {
        try {
            final FlyMutiRewardInfo flyMutiReward = JSON.parseObject(jsonStr, FlyMutiRewardInfo.class);
            if (flyMutiReward != null) {
                if (ChatServiceController.hostActivity == null)
                    return;
                ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ChatServiceController.getChannelListFragment() != null) {
                                ChatServiceController.getChannelListFragment().showMutiRewardFlyAnimation(flyMutiReward);
                            }
                        } catch (Exception e) {
                            LogUtil.printException(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean handleRedPackageInfo(String uidStr, int status) {
        Map<String, MsgItem> map = ChannelManager.getInstance().getUnHandleRedPackageMap();
        boolean hasCounteryRedPackage = false;
        boolean hasAllianceRedPackage = false;
        if (map == null || !map.containsKey(uidStr))
            return false;
        MsgItem msgItem = map.get(uidStr);
        if (msgItem != null && msgItem.sendState == MsgItem.UNHANDLE) {
            ChatChannel channel = null;
            if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_COUNTRY) {
                channel = ChannelManager.getInstance().getCountryChannel();
                hasCounteryRedPackage = true;
            } else if (msgItem.channelType == DBDefinition.CHANNEL_TYPE_ALLIANCE) {
                channel = ChannelManager.getInstance().getAllianceChannel();
                hasAllianceRedPackage = true;
            }
            if (channel != null) {

                if (msgItem.sendState != status && msgItem.sendState != MsgItem.HANDLEDISABLE) {
                    msgItem.sendState = status;
                    DBManager.getInstance().updateMessage(msgItem, channel.getChatTable());
                }

                for (int j = 0; j < channel.msgList.size(); j++) {
                    MsgItem item = channel.msgList.get(j);
                    if (item != null && msgItem.attachmentId.equals(item.attachmentId)) {
                        // 做一个内存状态的不修改
                        if (item.sendState != status && msgItem.sendState != MsgItem.HANDLEDISABLE) {
                            item.sendState = status;
                            channel.msgList.set(j, item);
                        }
                        break;
                    }
                }

                if (msgItem.sendState == MsgItem.UNHANDLE && status != MsgItem.UNHANDLE)
                    map.remove(uidStr);
            }
        }
        return hasCounteryRedPackage | hasAllianceRedPackage;
    }

    public static void postRedPackageGotUids(String redpackageInfo) {
        String[] uidArray = redpackageInfo.split(",");
        boolean hasRedPackageChange = false;
        for (int i = 0; i < uidArray.length; i++) {
            if (StringUtils.isNotEmpty(uidArray[i])) {
                String[] redPackageInfoArr = uidArray[i].split("\\|");
                if (redPackageInfoArr.length == 2) {
                    hasRedPackageChange = handleRedPackageInfo(redPackageInfoArr[0], Integer.parseInt(redPackageInfoArr[1]));
                }

            }
        }

        // if (ChatServiceController.hostActivity == null ||
        // !hasRedPackageChange)
        // return;
        // notifyDataSetChangedChatFragment();
    }

    public static void notifyDataSetChangedChatFragment() {
        if (ChatServiceController.getChatFragment() != null) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(ChatServiceController.getCurrentChannelType());
            if (channel != null)
                ChatServiceController.getChatFragment().notifyDataSetChanged(channel.channelType, channel.channelID, false);
        }
    }

    // 调用jni的获取当前等级的回调
    public static void setRedPackageOpenEnable(boolean enable, int redEnvelopesCastleLevel) {
        // 将 c++ 传回来的数据进行保存
        ChatServiceController.redPakcageOpenEnable = enable;
        ChatServiceController.redEnvelopesCastleLevel = redEnvelopesCastleLevel;

    }


    public static void postRedPackageStatus(String redPackageUid, int status) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "redPackageUid", redPackageUid, "status", status);
        MsgItem currentPopupItem = null;// ChatServiceController.getChatFragment().getCurrentRedPackageItem();
        if (ChatServiceController.getChatFragment() != null) {
            currentPopupItem = ChatServiceController.getChatFragment().getCurrentRedPackageItem();
        }
        if (StringUtils.isEmpty(redPackageUid) || status <= 0) {
            if (ChatServiceController.getChatFragment() != null)
                ChatServiceController.getChatFragment().hideRedPackageConfirm();

            if (currentPopupItem != null)
                ChatServiceController.doHostAction("pickRedPackage", "", "", currentPopupItem.attachmentId, true);
            return;
        }
        if (ChatServiceController.getChatFragment() != null) {

            if (currentPopupItem != null) {
                if (currentPopupItem.attachmentId.equals(redPackageUid) && currentPopupItem.sendState == MsgItem.UNHANDLE
                        && status != MsgItem.UNHANDLE) {
                    currentPopupItem.sendState = status;
                    ChatServiceController.getChatFragment().showRedPackageConfirm(currentPopupItem);
                } else {
                    ChatServiceController.getChatFragment().hideRedPackageConfirm();
                    ChatServiceController.doHostAction("pickRedPackage", "", "", currentPopupItem.attachmentId, true);
                }
            }
        }
        boolean hasChange = handleRedPackageInfo(redPackageUid, status);
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasChange", hasChange);
        // if (ChatServiceController.hostActivity == null || !hasChange)
        // return;
        // notifyDataSetChangedChatFragment();
    }

    public static void postRedPackageDuringTime(int time) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "time", time);
        ChatServiceController.red_package_during_time = time;
    }

    public static String getFriendLatestMails(String uids) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "uids", uids);
        if (StringUtils.isEmpty(uids))
            return "";
        String[] uidArr = uids.split("_");
        List<FriendLatestMail> friendMailList = new ArrayList<FriendLatestMail>();
        for (int i = 0; i < uidArr.length; i++) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_USER, uidArr[i]);
            if (channel != null) {
                MsgItem mail = channel.getLatestUserMail();
                if (mail != null) {
                    String latestMsg = mail.msg;
                    if (StringUtils.isNotEmpty(mail.translateMsg))
                        latestMsg = mail.translateMsg;
                    FriendLatestMail friendMail = new FriendLatestMail(uidArr[i], latestMsg);
                    if (friendMail != null)
                        friendMailList.add(friendMail);
                }
            }
        }

        try {
            String friendMailJson = JSON.toJSONString(friendMailList);
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "friendMailJson", friendMailJson);
            return friendMailJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getChatLatestMessage() {
        ChatChannel countryChannel = ChannelManager.getInstance().getCountryChannel();
        ChatChannel allianceChannel = ChannelManager.getInstance().getAllianceChannel();
        ChatChannel customChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_CUSTOM_CHAT);
        ChatChannel battleChannel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_BATTLE_FIELD);
        LatestChannelChatInfo chatInfo = new LatestChannelChatInfo();
        if (countryChannel != null) {
            MsgItem latestCountryMsg = DBManager.getInstance().getChatLatestMsg(countryChannel.getChatTable(), true);
            if (latestCountryMsg != null) {
                LatestChatInfo countryInfo = new LatestChatInfo();
                countryInfo.setMsgInfo(latestCountryMsg);
                chatInfo.setLatestCountryChatInfo(countryInfo);
            }
        }
        if (allianceChannel != null) {
            MsgItem latestAllianceMsg = DBManager.getInstance().getChatLatestMsg(allianceChannel.getChatTable(), true);
            if (latestAllianceMsg != null) {
                LatestChatInfo allianceInfo = new LatestChatInfo();
                allianceInfo.setMsgInfo(latestAllianceMsg);
                chatInfo.setLatestAllianceChatInfo(allianceInfo);
            }
        }

        if (customChannel != null) {
            MsgItem latestCutomMsg = null;
            if (customChannel.isRandomChatRoomChannel()) {
                if (customChannel.msgList != null && customChannel.msgList.size() > 0)
                    latestCutomMsg = customChannel.msgList.get(customChannel.msgList.size() - 1);
            } else {
                latestCutomMsg = DBManager.getInstance().getChatLatestMsg(customChannel.getChatTable(), true);
            }
            if (latestCutomMsg != null) {
                LatestChatInfo customInfo = new LatestChatInfo();
                customInfo.setMsgInfo(latestCutomMsg);
                chatInfo.setLatestCustomChatInfo(customInfo);
            }
        }

        if (battleChannel != null && ChatServiceController.getInstance().needCrossServerBattleChat()) {
            MsgItem latestBattleMsg = DBManager.getInstance().getChatLatestMsg(battleChannel.getChatTable(), true);
            if (latestBattleMsg != null) {
                LatestChatInfo batlleInfo = new LatestChatInfo();
                batlleInfo.setMsgInfo(latestBattleMsg);
                chatInfo.setLatestBattleChatInfo(batlleInfo);
            }
        }

        try {
            String lateChatJson = JSON.toJSONString(chatInfo);
            return lateChatJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void sendChatLatestMessage(ChatChannel channel) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_WS_SEND, "msg+sendChatLatestMessage，发到前端");
        if (channel == null || (channel.channelType == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD && !ChatServiceController.getInstance().needCrossServerBattleChat()))
            return;
        MsgItem latestMsgItem = null;
        if (channel.isRandomChatRoomChannel()) {
            if (channel.msgList != null && channel.msgList.size() > 0)
                latestMsgItem = channel.msgList.get(channel.msgList.size() - 1);
        } else
            latestMsgItem = DBManager.getInstance().getChatLatestMsg(channel.getChatTable(), false);
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "latestMsgItem", latestMsgItem);
        if (latestMsgItem != null) {
            ChatServiceController.getInstance().postLatestChatMessage(latestMsgItem);
        }
    }

    public static void postKingUid(String kingUid) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "kingUid", kingUid);
        ChatServiceController.kingUid = kingUid;
    }

    public static void postBanTime(String banTime) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "banTime", banTime);
        ChatServiceController.banTime = banTime;
    }

    public static void postShieldUids(String shieldUids) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "shieldUids", shieldUids);
        String[] shieldArr = shieldUids.split("_");
        for (int i = 0; i < shieldArr.length; i++) {
            if (StringUtils.isNotEmpty(shieldArr[i])) {
                UserManager.getInstance().addRestrictUser(shieldArr[i], UserManager.BLOCK_LIST);
            }
        }
    }

    public static void postServerType(int serverType, String originalServerName) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "serverType", serverType, "originalServerName", originalServerName);
        ChatServiceController.serverType = serverType;
        ChatServiceController.originalServerName = originalServerName;
    }

    public static void showAllianceDialog() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_CORE);
        ChatServiceController.needShowAllianceDialog = true;
    }

    public static void postAddedMailListMail(String mailUid) {

        if (StringUtils.isEmpty(mailUid))
            return;
        String showMailUid = MailManager.getInstance().getShowingMailUid();
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid, "showMailUid", showMailUid);
        if (StringUtils.isNotEmpty(showMailUid) && (showMailUid.equals(mailUid) || MailManager.getInstance().isInTransportedMailList(showMailUid))) {
            MailData mailData = DBManager.getInstance().getSysMailByID(showMailUid);
            if (mailData != null && mailData.hasReward()) {
                LocalConfig config = ConfigManager.getInstance().getLocalConfig();
                boolean hasShowed = (config != null && config.isFirstRewardTipShowed());
                JniController.getInstance().excuteJNIVoidMethod("postFirstRewardAnimationShowed", new Object[]{Boolean.valueOf(hasShowed)});
            }
            ChatServiceController.doHostAction("showMailPopup", showMailUid, "", "", true, true);
            MailManager.getInstance().setShowingMailUid("");
        }
        MailManager.getInstance().addMailInTransportedList(mailUid);
    }

    public static String getNeighborMail(String mailUid, int type) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid, "type", type);
        if (StringUtils.isEmpty(mailUid) || !(type == 1 || type == 2))
            return "";
        MailData mail = DBManager.getInstance().getSysMailByID(mailUid);
        if (mail != null) {
            if (type == 1)
                return MailManager.getInstance().transportNeiberMailData(mail, true, false);
            else if (type == 2)
                return MailManager.getInstance().transportNeiberMailData(mail, false, true);
        }
        return "";
    }

    public static void postSwitch(String switchKey, String switchValue) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "switchKey", switchKey, "switchValue", switchValue);
        if (StringUtils.isEmpty(switchKey))
            return;
        if (switchKey.equals("chat_k10"))
            ChatServiceController.switch_chat_k10 = switchValue;
        else if (switchKey.equals("chat_k11"))
            ChatServiceController.switch_chat_k11 = switchValue;
        else if (switchKey.equals("chat_bubble_k1")) {
            if (StringUtils.isNotEmpty(switchValue))
                ChatServiceController.isNewYearStyleMsg = switchValue.equals("1") ? true : false;
        }
    }

    public static void postPlayerLevel(int playerLevel, int mainCityLevel, boolean nameNicked) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "playerLevel", playerLevel, "mainCityLevel", mainCityLevel, "nameNicked", nameNicked);
        ChatServiceController.currentLevel = playerLevel;
        ChatServiceController.currentMainCityLevel = mainCityLevel;
        ChatServiceController.nameNicked = nameNicked;
    }

    public static void mergeConfig() {
        System.out.println("mergeConfig");
        // ConfigManager.getInstance().updateLocalPicConfig();
        // ConfigManager.getInstance().mergeRemoteDynamicImageMap();
    }

    /**
     * 在ImperialScene::onEnter()触发，早于setCurrentUserId，此时还不能saveLoginData
     */
    public static void initXiaoMiSDK(String appId, String appKey, String pid, String pkey, String guid, String b2token) {
        // demo有，但似乎非必要
        // GlobalData.initialize(ChatServiceController.hostActivity,
        // Integer.parseInt(appId));

        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_XM, "appId", appId, "appKey", appKey, "pid", pid, "pkey", pkey, "guid",
                guid, "b2token", b2token);
        XiaoMiToolManager.getInstance().initSDK(ChatServiceController.hostActivity, appId, appKey, pid, pkey, guid, b2token);
    }

    public static void recordVideo() {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.showRecordVideoActivity(ChatServiceController.hostActivity, false);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void playVideo(final String urlVideo, final String urlThumb) {
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ServiceInterface.playVideoActivity(ChatServiceController.hostActivity, false, urlVideo, urlThumb);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void postMoreMailInfo(boolean hasMoreMail, String latestMailUid) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "hasMoreMail", hasMoreMail, "latestMailUid", latestMailUid);
        MailManager.hasMoreNewMailToGet = hasMoreMail;
        MailManager.latestMailUidFromGetNew = latestMailUid;
        if (!hasMoreMail)
            ConfigManager.setMailPullState(false);

        // if(hasMoreMail)
        // return;

    }

    public static String getLatestSystemMailInfo() {
        // 在这里修改逻辑，返回的不是最新的mail,而是上次拉取时保存的mail基本信息，下次拉取时从这里开始，
        // 当前方法是在初始化的时候会调用，要确认第一次初始化时的mail 消息，如果为空的情况下，返回的是默认的消息，包含几个字段：creatime,count,uid
        return ChannelManager.getInstance().getLatestSystemMailInfo();
    }

    public static void onDownloadMediaFile(int type, String url) {
        if (ChatServiceController.hostActivity != null) {
            XiaoMiToolManager.getInstance().downloadMediaFile(type, url);
        }
    }

    public static void serverSaveVideoSuccess() {
        if (ChatServiceController.hostActivity != null) {
            Log.d(XiaoMiToolManager.XIAO_MI_LOG_TAG, "serverSaveVideoSuccess");
        }
    }

    public static void setAllianceShareEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        AllianceShareManager.isAllianceShareEnable = enable;
    }

    public static void setBattleChatEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "setBattleChatEnable enable", enable);
        ChatServiceController.isBattleChatEnable = enable;
    }

    public static void setTranslateWithAgentEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "setTranslateWithAgentEnable enable", enable);
        TranslateManager.translateByAgentEnable = enable;
    }

    public static void setUserChatStandaloneEnable(boolean pm_standalone_read, boolean pm_standalone_write) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "pm_standalone_read", pm_standalone_read, "pm_standalone_write", pm_standalone_write);
        ConfigManager.pm_standalone_read = pm_standalone_read;
        ConfigManager.pm_standalone_write = pm_standalone_write;
    }

    public static void onGetMultiUserInfoActualCalled() {
        UserManager.getInstance().onServerActualCalled();
    }

    public static void closeFireBaseService() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        AllianceShareManager.getInstance().goOffline();
    }

    public static void openFireBaseService() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        AllianceShareManager.getInstance().goOnline();
    }

    public static void clearAllOpendMailRecord() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        MailManager.getInstance().clearMailInTransportList();
    }

    public static void notifyGameEventType(final int type) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type);

        // if (ConfigManager.network_state != type)
        // {
        // ConfigManager.network_state = type;
        // if(ChatServiceController.getCurrentActivity()!=null)
        // {
        // ChatServiceController.getCurrentActivity().runOnUiThread(new
        // Runnable()
        // {
        // @Override
        // public void run()
        // {
        // try
        // {
        // if(ChatServiceController.getCurrentActivity()!=null)
        // ChatServiceController.getCurrentActivity().refreshNetWorkState();
        // }
        // catch (Exception e)
        // {
        // LogUtil.printException(e);
        // }
        // }
        // });
        // }
        // }
    }

    public static void notifyWebSocketEventType(final int type) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type);
        if (ConfigManager.websocket_network_state != type) {
            ConfigManager.websocket_network_state = type;
            if (ChatServiceController.getCurrentActivity() != null && (ChatServiceController.getCurrentActivity() instanceof ChatActivity
                    || ChatServiceController.getCurrentActivity() instanceof NearByActivity)
                    && (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_COUNTRY
                    || ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_BATTLE_FIELD
                    || ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_ALLIANCE
                    || (ChatServiceController.getCurrentChannelType() == DBDefinition.CHANNEL_TYPE_USER && ConfigManager.pm_standalone_read))) {
                ChatServiceController.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ChatServiceController.getCurrentActivity() != null)
                                ChatServiceController.getInstance().refreshNetWorkState();
                        } catch (Exception e) {
                            LogUtil.printException(e);
                        }
                    }
                });
            }

        }

    }

    public static int getAllianceShareNoticeNum() {
        return AllianceShareManager.getInstance().getAllianceShareNoticeNum();
    }

    public static void savePngToAlbum(final String filePath) {
        if (ChatServiceController.getInstance().hostActivity == null) {
            return;
        }
        ChatServiceController.getInstance().hostActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap ret = BitmapFactory.decodeFile(filePath);
                    if (ret != null) {
                        FileVideoUtils.saveImageToAlbum(ChatServiceController.getInstance().hostActivity, ret);
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        });
    }

    public static int getPhotoRight() {
        boolean flag = PermissionManager.isExternalStoragePermissionsAvaiable();
        if (flag) {
            return 0;
        }
        // PermissionManager.getExternalStoragePermissionForPNG();
        int right = 4;
        return right;
    }

    public static void setFirstRewardAnimationShowed() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);

        LocalConfig config = ConfigManager.getInstance().getLocalConfig();
        boolean changed = false;
        if (config != null && !config.isFirstRewardTipShowed()) {
            config.setFirstRewardTipShowed(true);
            changed = true;
        } else if (config == null) {
            config = new LocalConfig();
            config.setFirstRewardTipShowed(true);
            ConfigManager.getInstance().setLocalConfig(config);
            changed = true;
        }

        if (changed)
            ConfigManager.getInstance().saveLocalConfig();
    }

    public static String getCountryRoomId() {
        return SwitchUtils.mqttEnable ? MqttManager.getCountryRoomId() : WebSocketManager.getCountryRoomId();
    }

    public static void setNewestAllianceShareTime(long shareTime) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "shareTime", shareTime);
        AllianceShareManager.remoteNewestAllianceShareTime = shareTime;
    }

    public static void setAllianceTreasureHideEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.isAllianceTreasureHide = enable;
    }

    public static void setDriftingBottleEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        MailManager.isDriftingBottleEnable = enable;
    }

    public static void setBannerEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChannelManager.bannerEnable = enable;
    }

    public static void setNewClientTranslateCompanyEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        TranslateManager.client_translate_company_new = enable;
    }

    public static void getNewMailExistStatus() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    ChannelManager.getInstance().getNewUserChatChannelId();
                    DBManager.getInstance().getLatestUnReadReportByType(1);
                    DBManager.getInstance().getLatestUnReadReportByType(2);
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };
        MailManager.getInstance().runOnExecutorService(run);
    }

    public static void showNewestMail(int type) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "type", type);
        ChannelManager.getInstance().showNewMailFrom2dx(type);
    }

    public static void setNewLastUpdateTime(int time) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "time", time);
        ChatServiceController.newLastUpdateTime = time;
        if (UserManager.getInstance().getCurrentUser() != null) {
            UserManager.getInstance().getCurrentUser().lastUpdateTime = time;
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "lastUpdateTime", UserManager.getInstance().getCurrentUser().lastUpdateTime);
            if (!SwitchUtils.mqttEnable)
                WebSocketManager.getInstance().setUserInfo();
            else
                MqttManager.getInstance().setUserInfo();
        }
    }

    public static void setBannerData(String json) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "json", json);
        if (StringUtils.isNotEmpty(json)) {
            try {
                List<BannerInfo> list = JSON.parseArray(json, BannerInfo.class);
                ChannelManager.getInstance().setBannerInfoList(list);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getDriftingBottleNum() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        return ChannelManager.getInstance().getDriftingBottleNum();
    }

    public static void setAllianceSystemEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.allianceSysChannelEnable = enable;
    }

    public static void setCountrySystemEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.countrySysChannelEnable = enable;
    }

    public static void setRandomChatEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.randomChatEnable = enable;
    }

    public static void setNameRestricEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.name_check_enable = enable;
    }

    public static void shareBattleExport(String mailUid, String url, String msg, int channelType) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "mailUid", mailUid, "url", url, "msg", msg);
        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_FIGHT);
        if (channel != null) {
            MailData mail = channel.getSysMailByUid(mailUid);
            if (mail != null && mail instanceof BattleMailData) {
                BattleMailData battle = (BattleMailData) mail;
                if (battle != null) {
                    ShareMsgExtra shareExtra = battle.getShareExtra();
                    if (shareExtra != null) {
                        MediaContent mediaContent = shareExtra.getMediaContent();
                        if (mediaContent == null)
                            mediaContent = new MediaContent();
                        mediaContent.setType(1);
                        mediaContent.setUrl(url);
                        shareExtra.setMediaContent(mediaContent);
                        String shareExtraStr = JSON.toJSONString(shareExtra);
                        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "shareExtraStr", shareExtraStr);
                        if (!SwitchUtils.mqttEnable)
                            WebSocketManager.getInstance().sendRoomSysMsg(msg, TimeManager.getInstance().getCurrentTime(), channelType, MsgItem.MSG_TYPE_NEW_BATTLE_REPORT, shareExtraStr);
                        else
                            MqttManager.getInstance().sendRoomSysMsg(msg, TimeManager.getInstance().getCurrentTime(), channelType, MsgItem.MSG_TYPE_NEW_BATTLE_REPORT, shareExtraStr);
                    }
                }

            }
        }
    }

    public static void clearCurrentAccountData() {
        JniController.getInstance().excuteJNIVoidMethod("postUnreadMailNum", new Object[]{Integer.valueOf(0)});
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        DBManager.getInstance().clearDatabase(true, true);
    }

    public static void initDatabase(boolean isAccountChanged, boolean isNewUser) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "isAccountChanged", isAccountChanged, "isNewUser", isNewUser);
        DBManager.initDatabase(isAccountChanged, isNewUser);
    }

    public static void sendCountryOldChatFromStandalone(int channelType, int post, String msg, String attachmentId, String dialog, String dialogArr) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelType", channelType, "post", post, "msg", msg, "attachmentId", attachmentId, "dialog", dialog, "dialogArr",
                dialogArr);
        if (SwitchUtils.mqttEnable)
            sendChatMsgFromGameToMqtt(channelType, post, msg, attachmentId, dialog, dialogArr);
        else
            sendChatMsgFromGameToWS(channelType, post, msg, attachmentId, dialog, dialogArr);
    }

    private static void sendChatMsgFromGameToWS(int channelType, int post, String msg, String attachmentId, String dialog, String dialogArr) {
        try {
            org.json.JSONObject extraJson = null;
            if (StringUtils.isNotEmpty(attachmentId))
                extraJson = new org.json.JSONObject(attachmentId);
            else
                extraJson = new org.json.JSONObject();
            extraJson.put("post", post);
            if (StringUtils.isNotEmpty(dialog))
                extraJson.put("dialog", dialog);
            if (StringUtils.isNotEmpty(dialogArr)) {
                org.json.JSONArray dialogarrJson = new org.json.JSONArray(dialogArr);
                extraJson.put("msgarr", dialogarrJson);
            }
            if (ChatServiceController.newLastUpdateTime > 0) {
                long lastUpdateTime = (long) ChatServiceController.newLastUpdateTime * 1000;
                extraJson.put("lastUpdateTime", lastUpdateTime);
            }
            WebSocketManager.getInstance().sendChatMsgFromGame(msg, TimeManager.getInstance().getCurrentTime(), channelType, extraJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendChatMsgFromGameToMqtt(int channelType, int post, String msg, String attachmentId, String dialog, String dialogArr) {
        try {
            JSONObject extraJson = null;
            if (StringUtils.isNotEmpty(attachmentId))
                extraJson = JSON.parseObject(attachmentId);
            else
                extraJson = new JSONObject();
            extraJson.put("post", post);
            if (StringUtils.isNotEmpty(dialog))
                extraJson.put("dialog", dialog);
            if (StringUtils.isNotEmpty(dialogArr)) {
                org.json.JSONArray dialogarrJson = new org.json.JSONArray(dialogArr);
                extraJson.put("msgarr", dialogarrJson);
            }
            if (ChatServiceController.newLastUpdateTime > 0) {
                long lastUpdateTime = (long) ChatServiceController.newLastUpdateTime * 1000;
                extraJson.put("lastUpdateTime", lastUpdateTime);
            }
            MqttManager.getInstance().sendChatMsgFromGame(msg, TimeManager.getInstance().getCurrentTime(), channelType, extraJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendCountryNewChatFromStandalone(int channelType, int post, String msg, String description, String actionArgs, String mediaContent, String title, String icon) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
    }

    public static void setChatSessionEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        SwitchUtils.chatSessionEnable = enable;
    }

    public static void setDragonTowerMailEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        MailManager.dragonTowerMailEnable = enable;
    }

    public static void onCreateChatSession(String session, String expire) {
        
        try{
            SharePreferenceUtil.isCreatingSession = false;
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_ALL, "session", session, "expire", expire);
            LogUtil.trackChatConnectTimeTime(TimeManager.getInstance().getCurrentTimeMS() - SharePreferenceUtil.startAuthTime,
                                             LogUtil.CHAT_CONNECT_GET_SESSION);
            // expire 为空或者是 null的情况下做判断，设置一个默认值
            if (expire == null || expire.equals("")){
                SharePreferenceUtil.setChatSession(session, 0);
            }else {
                SharePreferenceUtil.setChatSession(session, Integer.parseInt(expire));
            }
            LoginShareDataUtil.saveLoginData();
            if (!SwitchUtils.mqttEnable)
                WebSocketManager.getInstance().connect();
            else {
                if (WebSocketManager.isWebSocketEnabled())
                    MqttManager.getInstance().init();
            }
        }catch (Exception e){
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_ALL, "session", session, "expire", expire);
        }
        
    }

    public static void postChatSendError(int channelType, String channelId, String sendLocalTime, String errorCode, long banTimeStemp, String roomId) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelType", channelType, "channelId", channelId, "sendLocalTime", sendLocalTime, "errorCode", errorCode, "banTimeStemp",
                banTimeStemp, "roomId", roomId);
        int channeltype = channelType;
        if (channeltype == DBDefinition.CHANNEL_TYPE_COUNTRY && StringUtils.isNotEmpty(roomId)) {
            if (ChatServiceController.isBattleChatEnable && (roomId.startsWith("dragon_") || roomId.contains(WebSocketManager.GROUP_COUNTRY)))
                channeltype = DBDefinition.CHANNEL_TYPE_BATTLE_FIELD;
        }
        ChatChannel channel = ChannelManager.getInstance().getChannel(channeltype, channelId);
        if (channel != null && channel.sendingMsgList != null) {
            for (int j = 0; j < channel.sendingMsgList.size(); j++) {
                MsgItem sendMsg = channel.sendingMsgList.get(j);
                if (sendMsg == null)
                    continue;
                if (sendMsg.sendLocalTime != 0 && sendLocalTime.equals("" + sendMsg.sendLocalTime)) {
                    sendMsg.sendState = MsgItem.SEND_FAILED;
                    sendMsg.sendErrorCode = errorCode;
                    sendMsg.sendErrorBanTime = banTimeStemp;
                    if (ChatServiceController.getChatFragment() != null)
                        ChatServiceController.getChatFragment().notifyDataSetChanged(channelType, channelId, false);
                }
            }
        }

    }

    public static void getChannelListData() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        try {
            String ret = "";
            ArrayList<ChannelListItem> channelArray = ChannelManager.getInstance().getAllMailChannel();
            if (channelArray == null)
                return;
            List<ChannelMainListInfo> list = new ArrayList<ChannelMainListInfo>();
            Iterator<ChannelListItem> iterator = channelArray.iterator();
            while (iterator.hasNext()) {
                ChatChannel channel = (ChatChannel) iterator.next();
                if (channel != null) {
                    ChannelMainListInfo mainItem = new ChannelMainListInfo();
                    mainItem.setItemInfo(channel);
                    if (mainItem.getChannelItemType() >= 0)
                        list.add(mainItem);
                }
            }
            ret = JSON.toJSONString(list);
            LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "ret", ret);
            JniController.getInstance().excuteJNIVoidMethod("postMailRootDataJson", new Object[]{ret});
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getChannelIdByChannelItemType(int type) {
        if (type < 0)
            return "";
        String channeId = "";
        switch (type) {
            // case 0:
            // channeId = MailManager.CHANNELID_MESSAGE;
            // break;
            // case 1:
            // channeId = MailManager.CHANNELID_DRIFTING_BOTTLE;
            // break;
            case 3:
                channeId = MailManager.CHANNELID_ALLIANCE;
                break;
            case 4:
                channeId = MailManager.CHANNELID_FIGHT;
                break;
            case 5:
                channeId = MailManager.CHANNELID_EVENT;
                break;
            case 6:
                channeId = MailManager.CHANNELID_STUDIO;
                break;
            case 7:
                channeId = MailManager.CHANNELID_SYSTEM;
                break;
            case 8:
                channeId = MailManager.CHANNELID_DRAGON_TOWER;
                break;
            case 9:
                channeId = MailManager.CHANNELID_RECYCLE_BIN;
                break;
            // case 10:
            // channeId = MailManager.CHANNELID_MOD;
            // break;
            case 11:
                channeId = MailManager.CHANNELID_RESOURCE;
                break;
            case 12:
                channeId = MailManager.CHANNELID_MONSTER;
                break;
            case 13:
                channeId = MailManager.CHANNELID_NEW_WORLD_BOSS;
                break;
            case 14:
                channeId = MailManager.CHANNELID_KNIGHT;
                break;
        }
        return channeId;
    }

    public static void loadMoreSystemMail(int channelItemType, long lastMailCreateTime) {
        String channelId = getChannelIdByChannelItemType(channelItemType);
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelItemType", channelItemType, "lastMailCreateTime", lastMailCreateTime, "channelId", channelId, "lastMailCreateTime",
                lastMailCreateTime);
        if (StringUtils.isEmpty(channelId)) {
            JniController.getInstance().excuteJNIVoidMethod("postSysMailLoadComplete", new Object[]{Integer.valueOf(0)});
            return;
        }
        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
        if (channel != null) {
            ChannelManager.getInstance().loadMoreSysMailFromDBForGame(channel, lastMailCreateTime);
        } else {
            JniController.getInstance().excuteJNIVoidMethod("postSysMailLoadComplete", new Object[]{Integer.valueOf(0)});
        }
    }

    public static void loadMoreRecycleBinSystemMail() {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG);
        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, MailManager.CHANNELID_RECYCLE_BIN);
        if (channel != null)
            ChannelManager.getInstance().loadMoreRecycleBinMailFromDBForGame(channel);
        else
            JniController.getInstance().excuteJNIVoidMethod("postSysMailLoadComplete", new Object[]{Integer.valueOf(0)});
    }

    public static void setGameMailListEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        MailManager.cocosMailListEnable = enable;
    }

    public static void setNearByEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        MailManager.nearbyEnable = enable;
    }

    public static void setExpressionEnable(boolean expressPanelEnable, boolean expressSubEnable) {
        ChatServiceController.expressionPanelEnable = expressPanelEnable;
        ChatServiceController.expressionSubEnable = expressSubEnable;
    }

    public static void setDragonObserverEnable(boolean chatEnable, boolean danmuEnable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "chatEnable", chatEnable, "danmuEnable", danmuEnable);
        ChatServiceController.dragonObserverEnable = chatEnable;
        ChatServiceController.dragonObserverDanmuEnable = danmuEnable;
    }

    public static void setNearByLikeEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        MailManager.nearbyLikeEnable = enable;
    }

    public static void setNearbyLikeCountLimit(int count) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "count", count);
        NearByManager.todayLikeNumLimit = count;
    }

    public static void setTranslateDevelopEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.translateDevelopEnable = enable;
    }

    public static void setRealtimeVoiceEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        ChatServiceController.realtime_voice_enable = enable;
    }

    public static void setMqttEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        SwitchUtils.mqttEnable = enable;
    }

    //zp 设置联盟分享Firebase数据库是否打开同步，2017.7.6最近今天google后台看下载量最大在680G，用此开关控制是否同步
    public static void setAllianceShareFirebaseKeepSynEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        SwitchUtils.allianceShareFirebaseKeepSynEnable = enable;
    }

    // 设置邮件加锁是否需要开启开关。
    public static void setMailSwitchEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        SwitchUtils.mailStatusEnable = enable;
    }

    // 设置打点开关
    public static void setOPTLogSwitchEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        SwitchUtils.optcLogEnable = enable;
    }
    
    // 设置自定义聊天开关
    public static void setCustomSocketEnable(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "enable", enable);
        SwitchUtils.customWebsocketEnable = enable;
    }

    public static void readAllMainMsgChannel(String channelId) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelId", channelId);
        ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_USER, channelId);
        ChannelManager.getInstance().readAllMainMsgChannel(channel);
    }

    public static void trackAction(String action) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "action", action);
        LogUtil.trackAction(action);
    }

    public static String getOperateUidsByChannelItemType(int channelListItemType, int operateType) {
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelListItemType", channelListItemType, "operateType", operateType);
        String channelId = getChannelIdByChannelItemType(channelListItemType);
        LogUtil.printVariablesWithFuctionName(Log.INFO, LogUtil.TAG_DEBUG, "channelId", channelId);
        String uids = "";
        if (StringUtils.isNotEmpty(channelId)) {
            ChatChannel channel = ChannelManager.getInstance().getChannel(DBDefinition.CHANNEL_TYPE_OFFICIAL, channelId);
            if (channel != null) {
                if (operateType == 1) {
                    List<String> unreadUidList = channel.getUnreadMailUids();
                    if (unreadUidList != null && unreadUidList.size() > 0)
                        uids = ChannelListFragment.getUidsByArray(unreadUidList);

                } else if (operateType == 2) {
                    List<String> rewardUidList = channel.getRewardMailUids();
                    if (rewardUidList != null && rewardUidList.size() > 0)
                        uids = ChannelListFragment.getUidsByArray(rewardUidList);
                } else if (operateType == 3) {
                    List<MailData> mailList = DBManager.getInstance().getSysMailFromDB(channel.channelID, -1);
                    if (mailList != null) {
                        for (int i = 0; i < mailList.size(); i++) {
                            MailData mailData = (MailData) mailList.get(i);
                            if (mailData != null && mailData.canDelete())
                                uids = ChannelManager.appendStr(uids, mailData.getUid());
                        }
                    }
                }
            }
        }
        return uids;
    }

    public static void postUserDicArrayFromGame(final List<HashMap<String, Object>> data) {
        UserManager.getInstance().parseUserInfoArray(data);
    }

    public static void postMailDicArrayFromGame(final List<HashMap<String, Object>> data, final boolean isGetNew) {
        LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_MSG);
        Runnable runable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (data != null) {
                        LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_MSG, "size", data.size());
                        List<MailData> mailDataList = new ArrayList<MailData>();
                        for (int i = 0; i < data.size(); i++) {
                            HashMap<String, Object> map = data.get(i);
                            if (map != null) {
                                Set<String> keySet = map.keySet();
                                if (keySet != null) {
                                    LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_MSG, "keySet size", keySet.size());
                                    MailData mail = new MailData();
                                    for (String key : keySet) {
                                        if (StringUtils.isNotEmpty(key)) {
                                            LogUtil.printVariablesWithFuctionName(Log.DEBUG, LogUtil.TAG_MSG, "key", key, "value", map.get(key));
                                            if (key.equals("uid"))
                                                mail.setUid(map.get(key).toString());
                                            else if (key.equals("type"))
                                                mail.setType(Integer.parseInt(map.get(key).toString()));
                                            else if (key.equals("title"))
                                                mail.setTitle(map.get(key).toString());
                                            else if (key.equals("contents"))
                                                mail.setContents(map.get(key).toString());
                                            else if (key.equals("status"))
                                                mail.setStatus(Integer.parseInt(map.get(key).toString()));
                                            else if (key.equals("save"))
                                                mail.setSave(Integer.parseInt(map.get(key).toString()));
                                            else if (key.equals("itemIdFlag"))
                                                mail.setItemIdFlag(Integer.parseInt(map.get(key).toString()));
                                            else if (key.equals("rewardStatus"))
                                                mail.setRewardStatus(Integer.parseInt(map.get(key).toString()));
                                            else if (key.equals("createTime")) {
                                                long time = Long.parseLong(map.get(key).toString());
                                                mail.setCreateTime(time);
                                            } else if (key.equals("rewardId"))
                                                mail.setRewardId(map.get(key).toString());
                                            else if (key.equals("fromUser"))
                                                mail.setFromUser(map.get(key).toString());
                                            else if (key.equals("fromName"))
                                                mail.setFromName(map.get(key).toString());
                                            else if (key.equals("reply"))
                                                mail.setReply(Integer.parseInt(map.get(key).toString()));
                                        }
                                    }
                                    if (StringUtils.isNotEmpty(mail.getUid()))
                                        mailDataList.add(mail);
                                }
                            }
                        }

                        if (isGetNew)
                            MailManager.getInstance().saveNewMailDataFromServer(mailDataList);
                        else
                            MailManager.getInstance().receiveNewMailDataFromServer(mailDataList);
                    }
                } catch (Exception e) {
                    LogUtil.printException(e);
                }
            }
        };

        MailManager.getInstance().runOnExecutorService(runable);
    }

    private static DanmuService danmuService;
    private static boolean danmuServiceConnected = false;

    private static final ServiceConnection danmuConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "connected");
            DanmuBinder danmuBinder = (DanmuBinder) service;
            if (danmuBinder != null) {
                danmuService = danmuBinder.getService();
                if (danmuService != null) {
                    danmuServiceConnected = true;
                    if (ChatServiceController.getCurrentActivity() != null)
                        danmuService.showDanmu(ChatServiceController.getCurrentActivity());
                    else
                        danmuService.showDanmu(ChatServiceController.hostActivity);
                }
            }
        }
    };

    public static void showChatDanmu(Activity a) {
        if (!danmuServiceConnected) {
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
            Intent intent = new Intent(a, DanmuService.class);
            a.bindService(intent, danmuConnection, Context.BIND_AUTO_CREATE);
        } else {
            DanmuManager.getInstance().showDanmu();
        }
    }

    public static RealtimeVoiceService realtimeVoiceService;
    public static boolean realtimeVoiceServiceConnected = false;

    private static final ServiceConnection realtimeVoicConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            realtimeVoiceService = null;
            realtimeVoiceServiceConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "connected");
            RealtimeVoiceBinder realtimeVoiceBinder = (RealtimeVoiceBinder) service;
            if (realtimeVoiceBinder != null) {
                realtimeVoiceService = realtimeVoiceBinder.getService();
                if (realtimeVoiceService != null) {
                    realtimeVoiceServiceConnected = true;
                    realtimeVoiceService.startConnectVoiceRoom(ChatServiceController.hostActivity);
                }
            }
        }
    };

    public static void showRealtimeVoice(final Activity a) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "realtimeVoiceServiceConnected", realtimeVoiceServiceConnected);
        if (!realtimeVoiceServiceConnected && realtimeVoiceService == null) {
            LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
            Intent intent = new Intent(a, RealtimeVoiceService.class);
            a.bindService(intent, realtimeVoicConnection, Context.BIND_AUTO_CREATE);
        } else {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    if (realtimeVoiceService != null)
                        realtimeVoiceService.startConnectVoiceRoom(a);
                }
            };
            new Thread(runnable).start();

        }
    }

    public static void leaveRealtimeRoom() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "realtimeVoiceServiceConnected", realtimeVoiceServiceConnected);
        if (realtimeVoiceServiceConnected && realtimeVoiceService != null)
            realtimeVoiceService.hungUp();
    }

    public static void exitRealtimeVoice() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "realtimeVoiceServiceConnected", realtimeVoiceServiceConnected);
        if (realtimeVoiceServiceConnected && realtimeVoiceService != null)
            realtimeVoiceService.exitRealtimeVoice();
    }

    public static void enableAudio(boolean enable) {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG, "enable", enable);
        if (realtimeVoiceServiceConnected && realtimeVoiceService != null)
            realtimeVoiceService.enableAudio(enable);
    }

    public static boolean isWebRTCInitialized() {
        if (realtimeVoiceServiceConnected && realtimeVoiceService != null)
            return realtimeVoiceService.isInitialized();
        else
            return false;
    }

    public static void unbindRealtimeVoice() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
        if (!ChatServiceController.realtime_voice_enable)
            return;
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ChatServiceController.hostActivity != null && realtimeVoiceServiceConnected && realtimeVoiceService != null) {
                            ChatServiceController.hostActivity.unbindService(realtimeVoicConnection);
                        }
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showRealtimeVoice() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
        if (!ChatServiceController.realtime_voice_enable)
            return;
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        showRealtimeVoice(ChatServiceController.hostActivity);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showDanmu() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        showChatDanmu(ChatServiceController.hostActivity);
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void showDanmuInput() {
        LogUtil.printVariablesWithFuctionName(Log.VERBOSE, LogUtil.TAG_DEBUG);
        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DanmuManager.getInstance().showDanmuInput();
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }

    public static void hideDanmu() {

        if (ChatServiceController.hostActivity != null) {
            ChatServiceController.hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DanmuManager.getInstance().hideDanmu();
                    } catch (Exception e) {
                        LogUtil.printException(e);
                    }
                }
            });
        }
    }
}
