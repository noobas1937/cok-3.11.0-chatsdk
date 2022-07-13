package com.elex.chatservice.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wangyan on 2017/10/9.
 */
//senderInfo =
//        {
//        lastUpdateTime = 1483067919;
//        userName = "zp-mini";
//        };
public class RoomGroupSenderInfo implements Serializable{
    @SerializedName("lastUpdateTime")
    public long lastUpdateTime;
    @SerializedName("userName")
    public String userName;

}
