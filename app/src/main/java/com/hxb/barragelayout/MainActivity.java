package com.hxb.barragelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hxb.barragelibrary.BarrageAdapter;
import com.hxb.barragelibrary.BarrageLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private BarrageLayout mBarrageLayout;

    private String[] mImages = {"http://img27.51tietu.net/pic/2017-011500/20170115001256mo4qcbhixee164299.jpg",
            "http://www.zhlzw.com/UploadFiles/Article_UploadFiles/201204/20120412123929822.jpg",
            "http://img.taopic.com/uploads/allimg/140319/235038-1403191S94457.jpg"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBarrageLayout = (BarrageLayout) findViewById(R.id.barrage_layout);
        mBarrageLayout.setAdapter(new BarrageAdapter() {
            @Override
            public View getViewByEntity(Object entity) {

                if (entity instanceof BarrageBean1) {
                    View view = View.inflate(MainActivity.this, R.layout.item_barrage1, null);
                    return view;
                } else {
                    View view = View.inflate(MainActivity.this, R.layout.item_barrage2, null);
                    return view;
                }

            }

            @Override
            public void refreshView(Object entity, View view) {

                if (entity instanceof BarrageBean1) {
                    ImageView image = (ImageView) view.findViewById(R.id.image);
                    TextView textView = (TextView) view.findViewById(R.id.text1);
                    Glide.with(MainActivity.this).load(((BarrageBean1) entity).image).into(image);
                    textView.setText(((BarrageBean1) entity).text);
                } else {
                    BarrageBean2 barrageBean2 = (BarrageBean2) entity;
                    TextView text = (TextView) view.findViewById(R.id.text);
                    text.setText(barrageBean2.text);
                }

            }

        });

        mBarrageLayout.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBarrageLayout.stop();
    }

    public void send1(View view) {
        BarrageBean1 bean = new BarrageBean1();
        bean.text = System.currentTimeMillis() + "";
        bean.image = mImages[new Random().nextInt(mImages.length)];
        mBarrageLayout.showBarrage(bean);
    }

    public void send2(View view) {
        BarrageBean2 barrageBean2 = new BarrageBean2();
        barrageBean2.text = "我是弹幕二" + new Random().nextInt(10000);
        mBarrageLayout.showBarrage(barrageBean2);
    }

    class BarrageBean1 {
        String text;
        String image;
    }

    class BarrageBean2 {
        String text;
    }
}
