package com.king.whichone;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.king.whichone.adapter.DishGridAdapter;
import com.king.whichone.bean.DishBean;
import com.king.whichone.database.DishDao;

public class MainActivity extends BaseActivity {

	private static final int ADD_DISH_REQUEST = 0;
	ActionBar actionBar;
	RelativeLayout mDishListLayout;
	GridView mDishGridView;
	ArrayList<DishBean> mItemList = new ArrayList<DishBean>();
	DishGridAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dish_grid_view);
		findView();
		initView();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	private void findView() {
		actionBar = getActionBar();
		mDishListLayout = (RelativeLayout) findViewById(R.id.dish_grid_layout);
		mDishGridView = (GridView) findViewById(R.id.dish_grid);
	}

	private void initView() {
		setAdapter();
	}

	public void setAdapter() {
		mItemList = (ArrayList<DishBean>) new DishDao(this).getAll();
		mAdapter = new DishGridAdapter(this, mItemList);
		mDishGridView.setAdapter(mAdapter);
		if (mItemList != null && mItemList.size() == 0) {
			LayoutInflater inflater = LayoutInflater.from(this);
			RelativeLayout helpLayout = (RelativeLayout) inflater.inflate(
					R.layout.dish_help_layout, null);
			helpLayout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.setVisibility(View.GONE);
					mDishListLayout.removeView(v);
					return false;
				}
			});
			mDishListLayout.addView(helpLayout, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			helpLayout.bringToFront();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch (item.getItemId()) {
		case R.id.action_add:
			intent.setClass(this, AddNewDishActivity.class);
			startActivityForResult(intent, ADD_DISH_REQUEST);
			break;

		case R.id.action_shake:
			intent.setClass(this, ShakeDishActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int arg1, Intent arg2) {
		if (requestCode == ADD_DISH_REQUEST && arg1 == RESULT_OK) {
			setAdapter();
		}
	}

}
