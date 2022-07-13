package com.elex.chatservice.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangyan on 2017/10/9.
 */
//{
//        founder = 100001;
//        id = 100001149993047165086318;
//        members =
//        (
//        100001,
//        100002,
//        100003,
//        100004,
//        100005
//        );
//        name = "zp-test";
//        };
public class RoomInfo implements Serializable{
    @SerializedName("id")
    public String id;
    @SerializedName("founder")
    public String founder;
    @SerializedName("name")
    public String name;
}
