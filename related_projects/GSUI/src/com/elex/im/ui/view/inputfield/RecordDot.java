package com.elex.im.ui.view.inputfield;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.elex.im.ui.R;
import com.elex.im.ui.view.misc.messenger.AndroidUtilities;

public class RecordDot extends View {

    private Drawable dotDrawable;
    private float alpha;
    private long lastUpdateTime;
    private boolean isIncr;

    public RecordDot(Context context) {
        super(context);

        dotDrawable = getResources().getDrawable(R.drawable.voice_rec);
    }

    public void resetAlpha() {
        alpha = 1.0f;
        lastUpdateTime = System.currentTimeMillis();
        isIncr = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        dotDrawable.setBounds(0, 0, AndroidUtilities.dp(11), AndroidUtilities.dp(11));
        dotDrawable.setAlpha(185 + (int) (70 * alpha));
        long dt = (System.currentTimeMillis() - lastUpdateTime);
        if (!isIncr) {
            alpha -= dt / 200.0f;
            if (alpha <= 0) {
                alpha = 0;
                isIncr = true;
            }
        } else {
            alpha += dt / 200.0f;
            if (alpha >= 1) {
                alpha = 1;
                isIncr = false;
            }
        }
        lastUpdateTime = System.currentTimeMillis();
        dotDrawable.draw(canvas);
        invalidate();
    }
}