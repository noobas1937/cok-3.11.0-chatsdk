package com.elex.chatservice.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangyan on 2017/10/9.
 */
//{
//        clientid = 67387466;
//        cmd = "roomgroup.create";
//        pushtime = 1499925710645;
//        result =
//        {
//        id = 100001149993047165086318;
//        members = "100002,100003";
//        name = "zp-test";
//        };
//        sendTime = 1499925710406;
//        server = "gatewayuscb-d3r7";
//        serverTime = 1499925710645;
//        uid = 100001;
//        }
public class RoomResponseAll extends RoomResponse implements Serializable {
    @SerializedName("result")
    public Rooms rooms;

    public class Rooms implements Serializable {
        @SerializedName("rooms")
        public List<RoomInfoPush> roomInfoList;

    }

}
