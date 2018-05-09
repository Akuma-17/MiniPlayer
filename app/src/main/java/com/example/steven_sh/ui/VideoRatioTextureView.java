package com.example.steven_sh.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class VideoRatioTextureView extends TextureView {

    private float mVideoRatio = 16f / 9f;

    public void setVideoRatio(float videoRatio) {
        this.mVideoRatio = videoRatio;
        requestLayout();
    }

    public VideoRatioTextureView(Context context) {
        super(context);
    }

    public VideoRatioTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoRatioTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (mVideoRatio != 0) {
            float viewAspectRatio = (float) width / height;
            float aspectDeformation = mVideoRatio / viewAspectRatio - 1;
            if (aspectDeformation > 0) {
                height = (int) (width / mVideoRatio);
            } else if (aspectDeformation < 0) {
                width = (int) (height * mVideoRatio);
            }
        }

        setMeasuredDimension(width, height);
        requestLayout();
    }
}
