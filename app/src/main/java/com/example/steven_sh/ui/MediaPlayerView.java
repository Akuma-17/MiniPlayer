package com.example.steven_sh.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.steven_sh.miniplayer.MiniViewService;
import com.example.steven_sh.miniplayer.R;

@Deprecated
public class MediaPlayerView extends FrameLayout {

    private ImageView mMiniPlayerButton;

    private ImageView mVideoScreen;

    public MediaPlayerView(Context context) {
        super(context);
        initView(context);
    }

    public MediaPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MediaPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_player_view, this, true);
        mMiniPlayerButton = view.findViewById(R.id.mini_player_btn);
        mMiniPlayerButton.setOnClickListener(v -> {
//            MiniPlayerService.startMiniPlayer(context, this);

            int[] locationInScreen = new int[2];
            mVideoScreen.getLocationOnScreen(locationInScreen);
//            MiniViewService.startMiniPlayer(context, getRootView(), this, locationInScreen[0], locationInScreen[1]);
        });
        mVideoScreen = view.findViewById(R.id.video_screen);
        mVideoScreen.setDrawingCacheEnabled(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public ImageView getScreenShot() {
        Bitmap bitmap = mVideoScreen.getDrawingCache(true);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bitmap);
        return imageView;
    }
}
