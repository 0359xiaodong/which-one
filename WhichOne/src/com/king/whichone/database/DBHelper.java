package com.king.whichone.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "dish.db";
	private static final int version = 1;
	public static final String DATA_NAME = "dish_table";
	public static final String COLUMN_DISH_NAME = "dish_name";
	public static final String COLUMN_IMAGE_PATH = "image_path";
	public static final String COLUMN_DISH_DES = "dish_des";

	private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ DATA_NAME + " (dishid integer primary key autoincrement, "
			+ COLUMN_DISH_NAME + " text, " + COLUMN_IMAGE_PATH + " text,"
			+ COLUMN_DISH_DES + " text)";
	private static final String SQL_DELETE = "DROP TABLE IF EXISTS "
			+ DATA_NAME;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE);
		onCreate(db);
	}

}
