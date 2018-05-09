package com.example.steven_sh.miniplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.steven_sh.ui.VideoView;
import com.example.steven_sh.util.DisplayMetrics;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.functions.Functions;

public class MiniViewService extends Service {

    private static VideoView sOriginalVideoView;

    private static View sDecorView;

    private static int sXPosInScreen;

    private static int sYPosInScreen;

    private ImageView mScreenShot;

    private VideoView mVideoView;

    static class ViewHolder {

        @BindView(R.id.mini_view_top_container)
        FrameLayout rootView;

        ViewHolder(Context context) {
            rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.mini_player_view, null);
            ButterKnife.bind(this, rootView);
        }
    }

    public static void startMiniPlayer(Context context,
                                       View decorView,
                                       VideoView originalVideoView,
                                       int xPosInScreen,
                                       int yPosInScreen) {
        sOriginalVideoView = originalVideoView;
        sDecorView = decorView;
        sXPosInScreen = xPosInScreen;
        sYPosInScreen = yPosInScreen;
        context.startService(new Intent(context, MiniViewService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mVideoView = new VideoView(getApplicationContext(), true);
        mVideoView.cloneVideoView(sOriginalVideoView);
        startMoveAnimation();
        return START_NOT_STICKY;
    }

    private void startMoveAnimation() {
        if (sOriginalVideoView == null) {
            return;
        }

        mScreenShot = sOriginalVideoView.getScreenShot();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sOriginalVideoView.getWidth(), sOriginalVideoView.getHeight());
        layoutParams.leftMargin = sXPosInScreen;
        layoutParams.topMargin = sYPosInScreen;
        ((ViewGroup)sDecorView).addView(mScreenShot, layoutParams);

        mScreenShot.setPivotX(sOriginalVideoView.getWidth());
        mScreenShot.setPivotY(0);
        mScreenShot.animate()
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(700)
                .translationY(-1 * (sYPosInScreen - DisplayMetrics.getStatusBarHeight(getResources())))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    addFloatingWindow();
                    mVideoView.play();
                    Single.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(signal -> ((ViewGroup)sDecorView).removeView(mScreenShot), Functions.emptyConsumer());
                })
                .start();
    }

    private void addFloatingWindow() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                sOriginalVideoView.getWidth() / 2,
                sOriginalVideoView.getHeight() / 2,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.x = DisplayMetrics.getDisplayWidth() - sXPosInScreen - sOriginalVideoView.getWidth() / 2;
        layoutParams.y = DisplayMetrics.getStatusBarHeight(getResources());
        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        ImageView imageView = sOriginalVideoView.getScreenShot();
        imageView.setScaleX(0.5f);
        imageView.setScaleY(0.5f);

        mVideoView.setCoverImage(imageView);
        windowManager.addView(mVideoView, layoutParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
