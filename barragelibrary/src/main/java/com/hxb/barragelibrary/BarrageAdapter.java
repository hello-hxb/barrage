package com.hxb.barragelibrary;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Created by hxb on 2017/4/22.
 */

public abstract class BarrageAdapter {
    public abstract View getViewByEntity(Object entity);

    public abstract void refreshView(Object entity, View view);


    public void onViewRemoved(Object entity, View view) {

    }


    public Interpolator getInterpolator(Object entity, View view) {
        Interpolator[] interpolators = {new LinearInterpolator(),new AccelerateDecelerateInterpolator()};
        return interpolators[new Random().nextInt(interpolators.length)];
    }


    public int getFlyTime(Object entity, View view) {
        return 10 * 1000;
    }

}
