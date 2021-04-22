/*
 * Copyright <2021> WISStudio Inc.
 */
package com.github.YizheYang;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * 显示本地图片的第二个活动，由主活动显式启动
 * @author 一只羊
 */

public class SecondActivity extends AppCompatActivity {

	private final ArrayList<Image> imageList = new ArrayList<>();
	private ImageAdapter adapter;
	private MySQLiteOpenHelper myHelper;
	private ProgressBar pgb;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		this.setTitle(R.string.second_title);
		pgb = findViewById(R.id.progressbar_top2);
		RecyclerView recyclerView = findViewById(R.id.recyclerView2);
		recyclerView.setLayoutManager(new GridLayoutManager(SecondActivity.this, 3));
		adapter = new ImageAdapter(imageList, SecondActivity.this);
		recyclerView.setAdapter(adapter);
		myHelper = new MySQLiteOpenHelper(SecondActivity.this, "Image.db", null, 1);
		initImage();
	}

	/**
	 * 用来加载本地的图片
	 * @param db是数据库对象，cs是从中读取到的信息，再将信息加载出来放到数组中，然后用handler更新UI
	 */
	private void initImage() {
		new Thread(() -> {
			String msg, lct;
			Message img;
			SQLiteDatabase db = myHelper.getWritableDatabase();
			Cursor cs = db.query("SavedImage", null, null, null,
					null, null, null);
			if (cs.moveToFirst()) {
				do {
					msg = cs.getString(cs.getColumnIndex("MESSAGE"));
					lct = cs.getString(cs.getColumnIndex("LOCATION"));
					imageList.add(new Image(msg, BitmapFactory.decodeFile(lct)));
					img = new Message();
					img.what = 1;
					addImage.sendMessage(img);
				} while (cs.moveToNext());
			} else {
				img = new Message();
				img.what = 0;
				addImage.sendMessage(img);
			}
			cs.close();
		}).start();
	}

	/**
	 * 更新UI
	 */
	@SuppressLint("HandlerLeak")
	private final Handler addImage = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			Image tempImage;
			if (msg.what == 1) {
				adapter.notifyDataSetChanged();
				pgb.setVisibility(View.GONE);
			} else {
				Toast.makeText(SecondActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
			}
		}
	};
}
