package com.example.steven_sh.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.steven_sh.miniplayer.MiniViewService;
import com.example.steven_sh.miniplayer.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.functions.Functions;

public class VideoView extends FrameLayout implements MediaPlayer.OnVideoSizeChangedListener {

    protected float ratio = 16f / 9f;

    private MediaPlayer mMediaPlayer;

    protected VideoRatioTextureView mVideoScreen;

    private ProgressBar mProgressBar;

    private ImageView mPlayButton;

    private ImageView mPauseButton;

    private ImageView mCoverImage;

    private View mMiniPlayerButton;

    private boolean mIsMiniMode;

    public VideoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoView(@NonNull Context context, boolean isMiniMode) {
        super(context);
        init(context);
        mIsMiniMode = true;
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_view, this, true);
        mVideoScreen = view.findViewById(R.id.video_screen);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mPlayButton = view.findViewById(R.id.play_button);
        mPauseButton = view.findViewById(R.id.pause_button);
        mMiniPlayerButton = view.findViewById(R.id.mini_player_button);
        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.test_video);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mCoverImage = view.findViewById(R.id.cover_image);
        initTextureView();
        setUpClickListeners();
    }

    private void setUpClickListeners() {
        mPlayButton.setOnClickListener(v -> {
            mMediaPlayer.start();
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.GONE);
        });
        mPauseButton.setOnClickListener(v -> {
            mMediaPlayer.pause();
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        });
        mMiniPlayerButton.setOnClickListener(v -> {
            mMediaPlayer.pause();
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
            mMiniPlayerButton.setVisibility(View.GONE);
            int[] location = new int[2];
            this.getLocationInWindow(location);
            MiniViewService.startMiniPlayer(getContext(), getRootView(), this, location[0], location[1]);
        });
    }

    public ImageView getScreenShot() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(Bitmap.createBitmap(mVideoScreen.getBitmap()));
        return imageView;
    }

    public void play() {
        mMediaPlayer.start();
        mPlayButton.setVisibility(View.GONE);
        mPauseButton.setVisibility(View.VISIBLE);
    }

    public void pause() {
        mMediaPlayer.pause();
        mPlayButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.GONE);
    }

    public void cloneVideoView(VideoView original) {
        mMediaPlayer.seekTo(original.mMediaPlayer.getCurrentPosition());
    }

    public void setCoverImage(ImageView image) {
        mCoverImage.setImageDrawable(image.getDrawable());
    }

    public void hideCoverImage() {
        mCoverImage.setVisibility(View.INVISIBLE);
    }

    private void initTextureView() {
        mVideoScreen.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mMediaPlayer.setSurface(new Surface(surface));

                if (mIsMiniMode) {
                    Single.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(signal -> mCoverImage.setVisibility(View.GONE), Functions.emptyConsumer());
                    mMediaPlayer.start();
                } else {
                    mMediaPlayer.start();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            layoutParams.height = displayMetrics.heightPixels / 2;
            layoutParams.width = (int)(layoutParams.height * ratio);
        } else {
            layoutParams.width = displayMetrics.widthPixels;
            layoutParams.height = (int)(layoutParams.width / ratio);
        }

        if (mIsMiniMode) {
            layoutParams.width /= 2;
            layoutParams.height /= 2;
        }

        mVideoScreen.measure(MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY));
        setMeasuredDimension(layoutParams.width, layoutParams.height);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoScreen.setVideoRatio((float)width / height);
    }
}
