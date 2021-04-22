/*
 * Copyright <2021> WISStudio Inc.
 */
package com.github.YizheYang;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * 用来创建自己的数据库表格
 * @author 一只羊
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

	protected Context mContext;
	public static final String CREATE = "create table SavedImage ("
			+ "id integer primary key autoincrement, "
			+ "MESSAGE text, "
			+ "LOCATION text, "
			+ "URL text)";

	public MySQLiteOpenHelper(@Nullable Context context, @Nullable String name,
							  @Nullable SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE);
		Toast.makeText(mContext, "Create database succeeded", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}