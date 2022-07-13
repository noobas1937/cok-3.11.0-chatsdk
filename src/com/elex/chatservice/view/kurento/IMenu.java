package com.elex.chatservice.view.kurento;

import android.content.Context;
import android.widget.RelativeLayout;


public interface IMenu {
    /**
     * 绑定context,可以在这个方法里做一些初始话的操作
     *
     * @param floatBall 悬浮球
     * @param context 传入的是application context，可以用来获取宽度和长度等值
     */
    void onAttach(VoiceFloatBall floatBall,Context context);

    /**
     * 添加悬浮球菜单的内容
     *
     * @param parent
     */
    void addMenu(RelativeLayout parent);

    /**
     * 悬浮球右侧的菜单是否可用
     *
     * @return
     */
    boolean isRightMenuEnable();

    /**
     * 悬浮球左侧的菜单是否可用
     *
     * @return
     */
    boolean isLeftMenuEnable();

    void showingRightMenu();

    void showingLeftMenu();
    
    void setMenuTip(String tip);

    /**
     * 获取菜单的高度
     *
     * @return
     */
    int getMenuHeight();

    /**
     * 获取菜单的宽度
     *
     * @return
     */
    int getMenuWidth();
}
