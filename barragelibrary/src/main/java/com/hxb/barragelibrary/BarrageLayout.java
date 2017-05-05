package com.hxb.barragelibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.nfc.Tag;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by hxb on 2017/4/22.
 */

public class BarrageLayout extends RelativeLayout {

    public static final String TAG = "BarrageLayout";
    private int mWidth = -1;    //控件宽
    private int mHeight = -1;  //控件高
    private LinkedBlockingQueue<Object> mQueue = new LinkedBlockingQueue<>();


    private Map<View, Info> mInfoMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService mTakeExecutor;
    private BarrageAdapter mAdapter;

    private View mBarrageView;

    public BarrageLayout(Context context) {
        super(context);
    }

    public BarrageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarrageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            widthMode = MeasureSpec.EXACTLY;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightMode = MeasureSpec.EXACTLY;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mWidth == -1 || mHeight == -1) {
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }

    public void setAdapter(BarrageAdapter adapter) {
        mAdapter = adapter;
    }



    public void showBarrage(Object entity) {
        if (entity == null) {
            return;
        }
        mQueue.offer(entity);

    }


    public void start() {
        if (mTakeExecutor != null) {
            return;
        }

        mTakeExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final Object entity = mQueue.take();
                    final CountDownLatch countDownLatch = new CountDownLatch(1);

                    Log.d(TAG, "----------------将一条弹幕添加到BarrageLayout中------------------");
                    addView(entity, countDownLatch);


                    countDownLatch.await();

                    Info newInfo = new Info();
                    newInfo.barrageHeight = mBarrageView.getMeasuredHeight();
                    newInfo.y = -1;

                    Log.d(TAG, "寻找弹幕的Y...,弹幕的高度为:"+newInfo.barrageHeight);
                    while (newInfo.y == -1) {
                        newInfo.y = getRandomY(newInfo.barrageHeight);

                        Iterator iterator = mInfoMap.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry entry = (Map.Entry) iterator.next();
                            Info existentInfo = (Info) entry.getValue();

                            int dTop = Math.abs(newInfo.y - existentInfo.y);
                            if (newInfo.y <= existentInfo.y && dTop < newInfo.barrageHeight) {
                                //在上面
                                newInfo.y = -1;
                                break;
                            } else if (newInfo.y >= existentInfo.y && dTop < existentInfo.barrageHeight) {
                                //在下面
                                newInfo.y = -1;
                                break;
                            }
                        }

                    }
                    Log.d(TAG, "找到了随机数Y: " + newInfo.y);
                    mInfoMap.put(mBarrageView, newInfo);
                    moveBarrage(entity, mBarrageView, newInfo);
                    mBarrageView = null;
                    mTakeExecutor.execute(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        mTakeExecutor.execute(runnable);
    }

    public void stop(){
        if (mTakeExecutor != null) {
            mTakeExecutor.shutdown();
            mTakeExecutor = null;
        }
    }

    private void moveBarrage(final Object entity, final View view, final Info newInfo) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //显示弹幕
                int barrageWidth = view.getMeasuredWidth();
                view.setY(newInfo.y);
                int flyTime = mAdapter.getFlyTime(entity, view);
                Interpolator interpolator = mAdapter.getInterpolator(entity, view);
                final ObjectAnimator anim = ObjectAnimator.ofFloat(view, "x", -barrageWidth);
                anim.setDuration(flyTime);
                anim.setInterpolator(interpolator);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (!newInfo.isRemoveFromMap) {
                            float xPluswidth = view.getX() + view.getWidth();
                            if (xPluswidth <= mWidth) {
                                mInfoMap.remove(view);
                                newInfo.isRemoveFromMap = true;
                            }

                        }
                    }
                });
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //动画结束了
                        BarrageLayout.this.removeView(view);
                        mAdapter.onViewRemoved(entity,view);
                    }
                });
                anim.start();
            }
        };
        post(runnable);
    }


    private class Info {
        int y;
        int barrageHeight;
        boolean isRemoveFromMap;
    }

    /**
     * 加弹幕View添加到弹幕layout上
     *
     * @param entity
     * @param countDownLatch
     */
    private void addView(final Object entity, final CountDownLatch countDownLatch) {
        post(new Runnable() {
            @Override
            public void run() {
                mBarrageView = mAdapter.getViewByEntity(entity);
                //将弹幕View放在layout的最右边
                mBarrageView.setX(mWidth);
                addView(mBarrageView);
                mAdapter.refreshView(entity, mBarrageView);

                final ViewTreeObserver viewTreeObserver = mBarrageView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        viewTreeObserver.removeGlobalOnLayoutListener(this);
                        countDownLatch.countDown();
                    }
                });

            }
        });
    }





    private int getRandomY(int barrageHeight) {
        Random random = new Random();
        int dh = mHeight - barrageHeight;
        if (dh <= 0)
            return 0;
        return random.nextInt(dh);
    }


}
