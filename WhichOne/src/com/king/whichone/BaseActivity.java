package com.king.whichone;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		DishApp.mScreenWidth = Math.min(dm.widthPixels, dm.heightPixels);
		DishApp.mScreenHeight = Math.max(dm.widthPixels, dm.heightPixels);
	}
}
