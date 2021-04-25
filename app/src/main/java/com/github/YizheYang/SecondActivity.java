/*
 * Copyright <2021> WISStudio Inc.
 */
package com.github.YizheYang;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

/**
 * 显示本地图片的第二个活动，由主活动显式启动
 * @author 一只羊
 */

public class SecondActivity extends AppCompatActivity {

	private final ArrayList<Image> imageList = new ArrayList<>();
	private ImageAdapter adapter;
	private ProgressBar pgb;
	private ImageView large2;
	private AlertDialog.Builder builder;
	private SQLiteDatabase db;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		this.setTitle(R.string.second_title);
		pgb = findViewById(R.id.progressbar_top2);
		MySQLiteOpenHelper myHelper = new MySQLiteOpenHelper(SecondActivity.this, "Image.db", null, 1);
		db = myHelper.getWritableDatabase();

		RecyclerView recyclerView = findViewById(R.id.recyclerView2);
		recyclerView.setLayoutManager(new GridLayoutManager(SecondActivity.this, 3));
		adapter = new ImageAdapter(imageList, SecondActivity.this);
		recyclerView.setAdapter(adapter);
		adapter.setOnItemClickListener((view, position) -> {
			Message large = new Message();
			large.what = 3;
			Object[] data = new Object[2];
			data[0] = imageList.get(position).getImage();
			data[1] = position;
			large.obj = data;
			displayLarge.sendMessage(large);
		});

		View view = LayoutInflater.from(SecondActivity.this).inflate(R.layout.large_image, null);
		LinearLayout linearLayout = view.findViewById(R.id.largeImageLayout);
		large2 = view.findViewById(R.id.largeImage2);
		builder = new AlertDialog.Builder(SecondActivity.this, R.style.edit_AlertDialog_style).setView(linearLayout);
		builder.setPositiveButton("删除", (dialog, which) -> {
			deleteImage(imageList.get((int) large2.getTag()).getMessage());
			((ViewGroup) view.getParent()).removeView(view);
			dialog.dismiss();
		});
		builder.setNegativeButton("取消", (dialog, which) -> {
			((ViewGroup) view.getParent()).removeView(view);
			dialog.dismiss();
		});
		builder.setNeutralButton("保存", (dialog, which) -> {
			saveBitmap(imageList.get((int) large2.getTag()).getImage(),
					imageList.get((int) large2.getTag()).getMessage());
			((ViewGroup) view.getParent()).removeView(view);
			dialog.dismiss();
		});

		initImage();
	}

	/**
	 * 更新UI
	 */
	@SuppressLint("HandlerLeak")
	private final Handler addImage = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				adapter.notifyDataSetChanged();
				pgb.setVisibility(View.GONE);
			} else {
				Toast.makeText(SecondActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * 更新放大显示的UI
	 */
	@SuppressLint("HandlerLeak")
	private final Handler displayLarge = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 3) {
				Object[] data = (Object[]) msg.obj;
				Bitmap bm = (Bitmap) data[0];
				large2.setImageBitmap(bm);
				large2.setTag(data[1]);
				large2.setVisibility(View.VISIBLE);
				builder.create();
				builder.show();
			}
		}
	};

	/**
	 * 用来加载本地的图片
	 * @param db是数据库对象，cs是从中读取到的信息，再将信息加载出来放到数组中，然后用handler更新UI
	 */
	private void initImage() {
		new Thread(() -> {
			String msg, lct;
			Bitmap bm;
			Message img;
			Cursor cs = db.query("SavedImage", null, null, null, null, null, null);
			if (cs.moveToFirst()) {
				do {
					msg = cs.getString(cs.getColumnIndex("MESSAGE"));
					lct = cs.getString(cs.getColumnIndex("LOCATION"));
					bm = BitmapFactory.decodeFile(lct);
					if (bm != null) {
						imageList.add(new Image(msg, bm));
						img = new Message();
						img.what = 1;
						addImage.sendMessage(img);
					} else {
						db.delete("SavedImage", "MESSAGE = ?", new String[]{msg});
					}
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
	 * 将图片保存到相册并且将相关信息存到数据库
	 * @param bitmap 是传进的图像
	 * @param message 是图像的信息
	 */
	private void saveBitmap(Bitmap bitmap, String message) {
		String name, result;
		Intent it;
		ContentValues cv;
		Cursor cs;
		name = message.substring(message.indexOf("/") + 8, message.indexOf("."));
		result = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, name, message);
		cs = db.query("SavedImage", null, "MESSAGE = ?", new String[]{message}, null, null, null);
		if (cs.moveToFirst()) {
			do {
				if (cs.getString(cs.getColumnIndex("MESSAGE")).equals(message)) {
					Toast.makeText(SecondActivity.this, "图片已存在", Toast.LENGTH_SHORT).show();
					return;
				}
			} while (cs.moveToNext());
		}
		it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(result));
		sendBroadcast(it);
		Toast.makeText(SecondActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
		cv = new ContentValues();
		cv.put("MESSAGE", message);
		cv.put("LOCATION", Environment.getExternalStorageDirectory() + "/Pictures/" + name + ".jpg");
		cv.put("URL", "https://cdn.shibe.online/shibes/" + name + ".jpg");
		db.insert("SavedImage", null, cv);
		cs.close();
	}

	/**
	 * 删除选中的已经保存的图片
	 * @param message 是选中的图片的信息
	 */
	private void deleteImage(String message) {
		Uri uri;
		ContentResolver cr;
		String name, location, where;
		Intent scanIntent;
		Uri contentUri;
		uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		cr = getApplicationContext().getContentResolver();
		name = message.substring(message.indexOf("/") + 8, message.indexOf("."));
		location = Environment.getExternalStorageDirectory() + "/Pictures/" + name + ".jpg";
		where = MediaStore.Images.Media.DATA + "='" + location + "'";
		cr.delete(uri, where, null);
		scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		contentUri = Uri.fromFile(new File(location));
		scanIntent.setData(contentUri);
		sendBroadcast(scanIntent);
		db.delete("SavedImage", "MESSAGE = ?", new String[]{message});
		Toast.makeText(SecondActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
	}
}
