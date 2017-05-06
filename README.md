
# barrage
可以支持不同种类型的View,实现弹幕效果.
## Compile

	compile 'com.hxb.barrage:1.0.0'
 
## 演示

实际效果比这个好

 ![image](https://github.com/hello-hxb/barrage/blob/master/barragelayout.png)

 ![image](https://github.com/hello-hxb/barrage/blob/master/barrage.gif)
 
## 用法
1. 在xml中引用
		     
		<com.hxb.barragelibrary.BarrageLayout
			android:id="@+id/barrage_layout"
		        android:layout_width="match_parent"
		        android:layout_height="match_parent">
		</com.hxb.barragelibrary.BarrageLayout>
2. 设置adapter

        mBarrageLayout.setAdapter(new BarrageAdapter() {
            @Override
            public View getViewByEntity(Object entity) {
				        //根据不同的实体获取不同的弹幕view的类型
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
			        	//修改对应的弹幕view
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
3. 开始工作

		mBarrageLayout.start();
    
4. 根据不同的实体显示不同弹幕,比如

		mBarrageLayout.showBarrage(barrageBean1);
		mBarrageLayout.showBarrage(barrageBean2);
	 
5. 在合适的位置停止工作,比如:

		//activity 的 onDestroy方法中
		protected void onDestroy() {
			super.onDestroy();
			mBarrageLayout.stop();
		}

