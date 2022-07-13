package com.elex.chatservice.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by wangyan on 2017/10/9.
 */
public class RoomGroupExtra implements Serializable{
    @SerializedName("post")
    public int post;
    @SerializedName("media")
    public String media;

}
