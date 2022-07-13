package com.elex.chatservice.model;

/**
 * Created by wangyan on 2017/10/9.
 */

public enum RoomGroupCmd {
    ROOMGROUP_PUSH(0,"push.chat.roomgroup"),
    ROOMGROUP_CREATE(1,"roomgroup.create"),
    ROOMGROUP_INVITE(2,"roomgroup.invite"),
    ROOMGROUP_MODIFYNAME(3,"roomgroup.modifyname"),
    ROOMGROUP_KICK(4,"roomgroup.kick"),
    ROOMGROUP_QUIT(5,"roomgroup.quit"),
    ROOMGROUP_ROOMALL(6,"roomgroup.all");

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_SYSTEM = 100;
    public static final int TYPE_CREATE = 101;
    public static final int TYPE_INVITE = 102;
    public static final int TYPE_MODIFY = 105;
    public static final int TYPE_KICK = 103;
    public static final int TYPE_QUIT = 104;

    public String cmd;

    public int value;

    RoomGroupCmd(int value, String cmd) {
        this.value = value;
        this.cmd = cmd;
    }

    public static RoomGroupCmd get(String cmd){
        for (RoomGroupCmd roomGroupCmd : RoomGroupCmd.values()) {
            if(roomGroupCmd.cmd.equals(cmd)){
                return roomGroupCmd;
            }
        }
        return ROOMGROUP_PUSH;
    }

    public static boolean contains(String cmd){
        for (RoomGroupCmd roomGroupCmd : RoomGroupCmd.values()) {
            if(roomGroupCmd.cmd.equals(cmd)){
                return true;
            }
        }
        return false;
    }
    
    public static boolean isSystemCmd(int type) {
        return type >= TYPE_CREATE && type <= TYPE_MODIFY;
    }
}
