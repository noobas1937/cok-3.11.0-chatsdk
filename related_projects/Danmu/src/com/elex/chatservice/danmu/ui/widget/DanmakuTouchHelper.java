package com.elex.chatservice.danmu.ui.widget;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.elex.chatservice.danmu.controller.IDanmakuView;
import com.elex.chatservice.danmu.model.BaseDanmaku;
import com.elex.chatservice.danmu.model.IDanmakuIterator;
import com.elex.chatservice.danmu.model.IDanmakus;
import com.elex.chatservice.danmu.model.android.Danmakus;

/**
 * Created by kmfish on 2015/1/25.
 */
public class DanmakuTouchHelper {

    private final GestureDetector mTouchDelegate;
    private IDanmakuView danmakuView;
    private RectF mDanmakuBounds;

    private final android.view.GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent event) {
            if(danmakuView != null) {
                IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
                if (onDanmakuClickListener != null) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            IDanmakus clickDanmakus = touchHitDanmaku(event.getX(), event.getY());
            boolean isEventConsumed = false;
            if (null != clickDanmakus && !clickDanmakus.isEmpty()) {
                isEventConsumed = performDanmakuClick(clickDanmakus);
            }
            if (!isEventConsumed) {
                isEventConsumed = performViewClick();
            }
            return isEventConsumed;
        }
    };

    private DanmakuTouchHelper(IDanmakuView danmakuView) {
        this.danmakuView = danmakuView;
        this.mDanmakuBounds = new RectF();
        this.mTouchDelegate = new GestureDetector(((View)danmakuView).getContext(), mOnGestureListener);
    }

    public static synchronized DanmakuTouchHelper instance(IDanmakuView danmakuView) {
        return new DanmakuTouchHelper(danmakuView);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mTouchDelegate.onTouchEvent(event);
    }

    private boolean performDanmakuClick(IDanmakus danmakus) {
        IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
        if (onDanmakuClickListener != null) {
            return onDanmakuClickListener.onDanmakuClick(danmakus);
        }
        return false;
    }

    private boolean performViewClick() {
        IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
        if (onDanmakuClickListener != null) {
            return onDanmakuClickListener.onViewClick(danmakuView);
        }
        return false;
    }

    private IDanmakus touchHitDanmaku(float x, float y) {
        IDanmakus hitDanmakus = new Danmakus();
        mDanmakuBounds.setEmpty();

        IDanmakus danmakus = danmakuView.getCurrentVisibleDanmakus();
        if (null != danmakus && !danmakus.isEmpty()) {
            IDanmakuIterator iterator = danmakus.iterator();
            while (iterator.hasNext()) {
                BaseDanmaku danmaku = iterator.next();
                if (null != danmaku) {
                    mDanmakuBounds.set(danmaku.getLeft(), danmaku.getTop(), danmaku.getRight(), danmaku.getBottom());
                    if (mDanmakuBounds.contains(x, y)) {
                        hitDanmakus.addItem(danmaku);
                    }
                }
            }
        }

        return hitDanmakus;
    }

}
