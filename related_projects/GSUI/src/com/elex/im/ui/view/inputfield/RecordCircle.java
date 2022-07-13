package com.elex.im.ui.view.inputfield;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.elex.im.ui.R;
import com.elex.im.ui.view.ChatFragment;
import com.elex.im.ui.view.misc.messenger.AndroidUtilities;

public class RecordCircle extends View {
	private final int COLOR_RECORD_CIRCLE_BACK = 0xff407448; //0xff5795cc;
	private final int COLOR_RECORD_CIRCLE_PAINT = 0x0d000000;
	
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintRecord = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable micDrawable;
    private float scale;
    private float amplitude;
    private float animateToAmplitude;
    private float animateAmplitudeDiff;
    private long lastUpdateTime;

    public RecordCircle(Context context) {
        super(context);
        paint.setColor(COLOR_RECORD_CIRCLE_BACK);
        paintRecord.setColor(COLOR_RECORD_CIRCLE_PAINT);
        micDrawable = getResources().getDrawable(R.drawable.voice_mic_pressed);
    }

    public void setAmplitude(double value) {
        animateToAmplitude = (float) Math.min(100, value) / 100.0f;
        animateAmplitudeDiff = (animateToAmplitude - amplitude) / 150.0f;
        lastUpdateTime = System.currentTimeMillis();
        invalidate();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float value) {
        scale = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cx = getMeasuredWidth() / 2;
        int cy = getMeasuredHeight() / 2;
        float sc;
        float alpha;
        if (scale <= 0.5f) {
            alpha = sc = scale / 0.5f;
        } else if (scale <= 0.75f) {
            sc = 1.0f - (scale - 0.5f) / 0.25f * 0.1f;
            alpha = 1;
        } else {
            sc = 0.9f + (scale - 0.75f) / 0.25f * 0.1f;
            alpha = 1;
        }
        long dt = System.currentTimeMillis() - lastUpdateTime;
        if (animateToAmplitude != amplitude) {
            amplitude += animateAmplitudeDiff * dt;
            if (animateAmplitudeDiff > 0) {
                if (amplitude > animateToAmplitude) {
                    amplitude = animateToAmplitude;
                }
            } else {
                if (amplitude < animateToAmplitude) {
                    amplitude = animateToAmplitude;
                }
            }
            invalidate();
        }
        lastUpdateTime = System.currentTimeMillis();
        if (amplitude != 0) {
            canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, (AndroidUtilities.dp(42) * (float) ChatFragment.getAudioUIScale() + AndroidUtilities.dp(20) * (float) ChatFragment.getAudioUIScale() * amplitude) * scale, paintRecord);
        }
        canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, AndroidUtilities.dp(42) * (float) ChatFragment.getAudioUIScale() * sc, paint);
        int micWidth = (int) (micDrawable.getIntrinsicWidth() * ChatFragment.getAudioUIScale());
        int micHeight = (int) (micDrawable.getIntrinsicHeight() * ChatFragment.getAudioUIScale());
        micDrawable.setBounds(cx - micWidth / 2, cy - micHeight / 2, cx + micWidth / 2, cy + micHeight / 2);
        micDrawable.setAlpha((int) (255 * alpha));
        micDrawable.draw(canvas);
    }
}
