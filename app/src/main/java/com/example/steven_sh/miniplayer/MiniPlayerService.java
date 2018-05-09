package com.example.steven_sh.miniplayer;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.steven_sh.ui.MediaPlayerView;
import com.example.steven_sh.util.DisplayMetrics;

import butterknife.BindView;
import butterknife.ButterKnife;

@Deprecated
public class MiniPlayerService extends Service {

    static class ViewHolder {

        @BindView(R.id.mini_view_top_container)
        FrameLayout rootView;

        @BindView(R.id.mini_view_direct_container)
        RelativeLayout miniViewDirectContainer;

        ViewHolder(Context context) {
            rootView = (FrameLayout)LayoutInflater.from(context).inflate(R.layout.mini_player_view, null);
            ButterKnife.bind(this, rootView);
        }
    }

    private static View sOriginalView;

    private WindowManager mWindowManager;

    private ViewHolder mViewHolder;

    private MediaPlayerView mMiniView;

    private WindowManager.LayoutParams mMiniViewWindowLayoutParams;

    public static void startMiniPlayer(Context context, View originalView) {
        sOriginalView = originalView;
        context.startService(new Intent(context, MiniPlayerService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (sOriginalView == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        initView(getPositionInWindow(sOriginalView), getApplicationContext());
        return START_STICKY;
    }

    private void initView(Point initPosition, Context context) {
        mViewHolder = new ViewHolder(context);
        mMiniViewWindowLayoutParams = new WindowManager.LayoutParams(
                getOriginalViewWidth(),
                getOriginalViewHeight(),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        //gravity에 따라 루트 뷰를 윈도우에 배치하고, layoutParams.x, y에 따라 translate시키는 것 같다.
        // 그리고 root view의 좌상단이 윈도우의 "원점"이 되는 것 같다.
        mMiniViewWindowLayoutParams.gravity = Gravity.FILL | Gravity.LEFT | Gravity.TOP; //이 gravity는 스크린 내에서 "윈도우"의 포지션을 정하는 것이다.(기준점이 스테이터스바 포함할 수도 있고 아닐 수도 있다. 플래그에 의해 결정됨.)
        mMiniViewWindowLayoutParams.x = 0; //이건 LEFT을 했을때만 유효함. 안했을 경우는 무시된다. 탑에서 마진이다.
        mMiniViewWindowLayoutParams.y = 0; //이건 TOP을 했을때만 유효함. 시작기준이 스테이터스바 밑이다.
        mMiniViewWindowLayoutParams.width = DisplayMetrics.getDisplayWidth();
        mMiniViewWindowLayoutParams.height = DisplayMetrics.getDisplayHeight();
        //FLAG_LAYOUT_IN_SCREEN 이걸 설정하면 스크린 최상단이 시작 기준이다.

        mViewHolder.rootView.setLayoutParams(new ViewGroup.LayoutParams(
                DisplayMetrics.getDisplayWidth(), DisplayMetrics.getDisplayHeight()));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getOriginalViewWidth(), getOriginalViewHeight());
        layoutParams.leftMargin = initPosition.x;
        layoutParams.topMargin = initPosition.y;
        mViewHolder.miniViewDirectContainer.addView(mMiniView = new MediaPlayerView(context), layoutParams);

        try {
            //윈도우가 여기서 만들어지는 것 같다... 명시적으로 위도우를 만드는 코드가 없음... 윈도우의 크기와 스크린 내에서 위치는 두 번째 파라미터인 LayoutParams에 의해 결정되는 것 같다.
            mWindowManager.addView(mViewHolder.rootView, mMiniViewWindowLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }

        startMoveAnimation();
    }

    private void startMoveAnimation() {
        mMiniView.setPivotX(sOriginalView.getWidth());
        mMiniView.setPivotY(0);
        mMiniView.animate()
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(1000)
                .translationY(-1 * (getPositionInWindow(sOriginalView).y - DisplayMetrics.getStatusBarHeight(getResources())))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mMiniView.getLayoutParams();
                    layoutParams.leftMargin = 0;
                    layoutParams.topMargin = 0;

                    mViewHolder.rootView.getLayoutParams().width = getMiniViewDefaultWidth();
                    mViewHolder.rootView.getLayoutParams().height = getMiniViewDefaultHeight();
                    mViewHolder.miniViewDirectContainer.getLayoutParams().width = getMiniViewDefaultWidth();
                    mViewHolder.miniViewDirectContainer.getLayoutParams().height = getMiniViewDefaultHeight();

                    mMiniViewWindowLayoutParams.width = getMiniViewDefaultWidth();
                    mMiniViewWindowLayoutParams.height = getMiniViewDefaultHeight();
                    mMiniViewWindowLayoutParams.x = DisplayMetrics.getDisplayWidth() - getMiniViewDefaultWidth();
                    mMiniViewWindowLayoutParams.y = DisplayMetrics.getStatusBarHeight(getResources());

                    mViewHolder.miniViewDirectContainer.removeView(mMiniView);
                    mViewHolder.miniViewDirectContainer.addView(mMiniView);
                    mWindowManager.updateViewLayout(mViewHolder.rootView, mMiniViewWindowLayoutParams);
                })
                .start();
    }

    private void startMoveAnimation(int initialX, int initialY, int finalX, int finalY) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            int x = (int)(initialX + (finalX - initialX) * (float)animation.getAnimatedValue());
            int y = (int)(initialY + (finalY - initialY) * (float)animation.getAnimatedValue());

            mMiniViewWindowLayoutParams.x = x;
            mMiniViewWindowLayoutParams.y = y;

            try {
                mWindowManager.updateViewLayout(mViewHolder.rootView, mMiniViewWindowLayoutParams);
            } catch (Exception ignored) {
            }
        });
        valueAnimator.start();
    }

    private Point getPositionInWindow(View anchorView) {
        Point result = new Point();
            final int coordinate[] = new int[2];
            anchorView.getLocationInWindow(coordinate); //이건 이 뷰가 붙은 "윈도우" 내에서 뷰의 위치다.
            //anchorView.getLocationInScreen(); 이렇게 하면 스크린 내에서 뷰의 위치다. 보통 액티비티의 윈도우는 스크린 전체이므로 액티비티 안의 뷰에서는 두 경우 같은 결과가 나온다.
            result.x = coordinate[0];
            result.y = coordinate[1];

        return result;
    }

    private int getMiniViewDefaultWidth() {
        return sOriginalView.getWidth() / 2;
    }

    private int getMiniViewDefaultHeight() {
        return sOriginalView.getHeight() / 2;
    }

    private int getOriginalViewWidth() {
        if (sOriginalView == null) {
            return 0;
        }

        return sOriginalView.getWidth();
    }

    private int getOriginalViewHeight() {
        if (sOriginalView == null) {
            return 0;
        }

        return sOriginalView.getHeight();
    }
}
