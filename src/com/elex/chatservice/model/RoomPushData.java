package com.elex.chatservice.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wangyan on 2017/10/9.
 */
//data =
//        {
//        appId = 100001;
//        extra = "<null>";
//        group = "";
//        msg = "100001,100002,100003";
//        originalLang = "";
//        roomId = 100001149993047165086318;
//        "room_info" =
//        {
//        founder = 100001;
//        id = 100001149993047165086318;
//        members =
//        (
//        100002,
//        100003,
//        100001
//        );
//        name = "zp-test";
//        };
//        sendTime = 1499925710616;
//        sender = 100001;
//        senderInfo =
//        {
//        lastUpdateTime = 1483067919;
//        userName = "zp-mini";
//        };
//        seqId = 1;
//        serverTime = 1499925710616;
//        trans = ();
//        type = 101;
//        };
public class RoomPushData implements Serializable {
    @SerializedName("appId")
    public String appId;
    @SerializedName("group")
    public String group;
    @SerializedName("msg")
    public String msg;
    @SerializedName("originalLang")
    public String originalLang;
    @SerializedName("roomId")
    public String roomId;
    @SerializedName("room_info")
    public RoomInfoPush roomInfo;
    @SerializedName("sendTime")
    public long sendTime;
    @SerializedName("sender")
    public String sender;
    @SerializedName("senderInfo")
    public RoomGroupSenderInfo senderInfo;
    @SerializedName("seqId")
    public String seqId;
    @SerializedName("serverTime")
    public long serverTime;
//    @SerializedName("trans")
//    public String trans;
    @SerializedName("type")
    public int type;
    public RoomGroupExtra extra;
}
