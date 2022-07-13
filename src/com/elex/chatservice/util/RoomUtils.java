package com.elex.chatservice.util;

import android.text.TextUtils;

import com.elex.chatservice.model.RoomGroupCmd;
import com.elex.chatservice.model.UserManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by wangyan on 2017/10/13.
 */

public class RoomUtils {
    public static int getSecond(long time) {
        if (String.valueOf(time).length() > 10) {
            return (int) (time / 1000);
        } else {
            return (int) time;
        }
    }

    public static String getMsgStr(int value, String senderUid, String members) {
        StringBuffer msgStr = new StringBuffer();
        String[] uidArr = {};
        if (TextUtils.isEmpty(members)) return "";
        if (members.contains(",")) {
            uidArr = members.split(",");
        } else if (members.contains("_")) {
            uidArr = members.split("_");
        } else {
            uidArr = new String[]{members};
        }
        switch (value) {
            case RoomGroupCmd.TYPE_CREATE:
            case RoomGroupCmd.TYPE_INVITE:
                if (UserManager.getInstance().isMySelf(senderUid)) {
                    msgStr.append("你将");
                } else {
                    msgStr.append(UserManager.getInstance().getUserNameStr(new String[]{senderUid})).append("将");
                }
                msgStr.append(UserManager.getInstance().getUserNameStr(uidArr)).append("加入聊天");
                break;
            case RoomGroupCmd.TYPE_MODIFY:
                if (UserManager.getInstance().isMySelf(senderUid)) {
                    msgStr.append("你将");
                } else {
                    msgStr.append(UserManager.getInstance().getUserNameStr(new String[]{senderUid})).append("将");
                }
                msgStr.append("群组名称改为").append(members);
                break;
            case RoomGroupCmd.TYPE_KICK:
                if (UserManager.getInstance().isMySelf(senderUid)) {
                    msgStr.append("你将");
                } else {
                    msgStr.append(UserManager.getInstance().getUserNameStr(new String[]{senderUid})).append("将");
                }
                if (uidArr.length == 1) {
                    if (UserManager.getInstance().isMySelf(uidArr[0])) {
                        msgStr.append("你");
                    } else {
                        msgStr.append(UserManager.getInstance().getUserNameStr(uidArr));
                    }
                }
                msgStr.append("移出聊天");
                break;
            case RoomGroupCmd.TYPE_QUIT:
                msgStr.append(UserManager.getInstance().getUserNameStr(new String[]{senderUid})).append("退出聊天");
                break;
        }
        return msgStr.toString();
    }

    public static ArrayList<String> getUidArr(String members) {
        ArrayList<String> uidList = new ArrayList<String>();
        if (TextUtils.isEmpty(members)) return uidList;
        String[] uidArr = {};
        if (members.contains(",")) {
            uidArr = members.split(",");
        } else if (members.contains("_")) {
            uidArr = members.split("_");
        } else {
            uidArr = new String[]{members};
        }
        if (uidArr != null && uidArr.length > 0) {
            for (int i = 0; i < uidArr.length; i++) {
                if (uidArr[i] != null) {
                }
                uidList.add(uidArr[i]);
            }
        }
        return uidList;
    }

    public static ArrayList<String> addUidsToArr(ArrayList<String> memberUidArray, ArrayList<String> uidArr) {
        if (memberUidArray == null || uidArr == null) return new ArrayList<String>();
        for (int i = 0; i < uidArr.size(); i++) {
            if (!TextUtils.isEmpty(uidArr.get(i))
                    && !memberUidArray.contains(uidArr.get(i))) {
                memberUidArray.add(uidArr.get(i));
            }
        }
        return memberUidArray;
    }

    public static ArrayList<String> removeUidsFromArr(ArrayList<String> memberUidArray, ArrayList<String> uidArr) {
        if (memberUidArray == null || uidArr == null) return new ArrayList<String>();
        for (int i = uidArr.size() - 1; i > -1; i--) {
            if (!TextUtils.isEmpty(uidArr.get(i))
                    && memberUidArray.contains(uidArr.get(i))) {
                memberUidArray.remove(uidArr.get(i));
            }
        }
        return memberUidArray;
    }

    public static ArrayList<String> removeDuplicate(ArrayList<String> oldList) {
        ArrayList<String> newList = new ArrayList<String>();
        if (oldList == null || oldList.size() == 0) return newList;
        HashSet<String> hashSet = new HashSet<String>(oldList);
        newList.addAll(hashSet);
        return newList;
    }
}
