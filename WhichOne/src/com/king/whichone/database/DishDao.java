package com.king.whichone.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.king.whichone.bean.DishBean;

public class DishDao {
	private DBHelper mDbHelper;

	public DishDao(Context context) {
		mDbHelper = new DBHelper(context);
	}

	public void insert(DishBean dish) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			db.execSQL("insert into " + DBHelper.DATA_NAME + " ("
					+ DBHelper.COLUMN_DISH_NAME + ","
					+ DBHelper.COLUMN_IMAGE_PATH + ","
					+ DBHelper.COLUMN_DISH_DES + ") values(?,?,?)",
					new Object[] { dish.getDishName(), dish.getImagePath(),
							dish.getDishDes() });
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

	public boolean delete(DishBean dish) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int rows = db.delete(DBHelper.DATA_NAME, DBHelper.COLUMN_DISH_NAME
				+ "=?", new String[] { dish.getDishName() });
		if (rows == 0) {
			return false;
		} else
			return true;
	}

	public List<DishBean> getAll() {
		List<DishBean> persons = new ArrayList<DishBean>();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db
				.rawQuery("select * from " + DBHelper.DATA_NAME, null);
		while (cursor.moveToNext()) {
			persons.add(new DishBean(cursor.getString(1), cursor.getString(2),
					cursor.getString(3)));
		}
		return persons;
	}
}
