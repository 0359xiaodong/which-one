package com.king.whichone;

import android.app.Application;

public class DishApp extends Application {
	// 从资源文件中批量添加菜
	public static final boolean mIsAddDishBatch = true;
	
	public static int mScreenWidth,mScreenHeight;
	@Override
	public void onCreate() {
		super.onCreate();
	}
}
