package com.github.YizheYang;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity" ;
	private String path = "http://shibe.online/api/shibes?count=1&urls=true&httpsUrls=true";
	private SwipeRefreshLayout swipe;
	private RecyclerView recyclerView;
//	private OverScrollLayout Layout;
	private ProgressBar pgb_top;
	private ProgressBar pgb_bottom;
	private ScrollView scrollView;
	private ImageView img;
	PictureAdapter adapter;
	public ImageView largeImage;
	private ImageView largeImage2;
	Dialog dialog;
	AlertDialog.Builder builder;
	private List<Picture> pictureList = new ArrayList<>();
	private int i;
	private Picture tempPicture;
	private MySQLiteOpenHelper myHelper;
	protected int firstNum = 3 * 4;
	protected int moreNum = 3 * 2;
	protected int allNum = firstNum;
	protected int spanCount = 3;
	protected boolean isNew = true;

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			tempPicture = new Picture("0", null);
			if (msg.what == 1) {
				Object[] objects = (Object[])msg.obj;
				Bitmap bitmap = (Bitmap) objects[0];
				String message = (String) objects[1];
				tempPicture = new Picture(message, bitmap);
				pictureList.add(tempPicture);
				//pgb.setProgress(pictureList.size());
				if (((pictureList.size() == allNum) && isNew) || !isConnect()) {
					pgb_top.setVisibility(View.GONE);
					isNew = false;
					Message re = new Message();
					re.what = 2;
					refresh.sendMessage(re);
//					PictureAdapter adapter = new PictureAdapter(pictureList);
//					recyclerView.setAdapter(adapter);
//					GridLayoutManager LayoutManager = new GridLayoutManager(MainActivity.this, 3);
//					recyclerView.setLayoutManager(LayoutManager);
				}else if (pictureList.size() == allNum) {
					pgb_top.setVisibility(View.GONE);
					Message re = new Message();
					re.what = 1;
					refresh.sendMessage(re);
				}
			}else{
				Toast.makeText(MainActivity.this ,"ERROR" ,Toast.LENGTH_SHORT).show();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private final Handler refresh = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1){
//				recyclerView.layout(recyclerView.getLeft(), recyclerView.getTop(), recyclerView.getRight(), recyclerView.getBottom() + adapter.VIEW_FOOTER.getMeasuredHeight());
				adapter.notifyDataSetChanged();
			}else if (msg.what == 2){
				recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, spanCount));
				adapter = new PictureAdapter(pictureList, MainActivity.this);
//				adapter.addHeaderView(LayoutInflater.from(MainActivity.this).inflate(R.layout.refresh_top, null));
				if (isConnect()) {
					adapter.addFooterView(LayoutInflater.from(MainActivity.this).inflate(R.layout.refresh_bottom, null));
				}else {
					adapter.addFooterView(LayoutInflater.from(MainActivity.this).inflate(R.layout.local_display, null));
				}
				recyclerView.setAdapter(adapter);
				adapter.setOnItemClickListener(new PictureAdapter.OnItemClickListener() {
					@Override
					public void onItemClick(View view, int position) {
//						Bitmap bitmap = pictureList.get(position).getCompressedImage();
//						ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//						bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
//						byte[] data = bStream.toByteArray();
//						Intent intent = new Intent().setClass(MainActivity.this ,SecondActivity.class);
//						intent.putExtra("extra_data", data);
//						startActivity(intent);

						Message large = new Message();
						large.what = 3;
						Object[] data = new Object[2];
						data[0] = pictureList.get(position).getImage();
						data[1] = position;
						large.obj = data;
						enlarge.sendMessage(large);
					}
				});
			}
		}
	};
	@SuppressLint("HandlerLeak")
	private final Handler enlarge = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 3){
				Object[] data = (Object[])msg.obj;
				Bitmap bitmap = (Bitmap) data[0];
				largeImage2.setImageBitmap(bitmap);
				largeImage2.setTag(data[1]);
				largeImage2.setVisibility(View.VISIBLE);
//				dialog.show();
				builder.show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		if(getSupportActionBar() != null){
//			getSupportActionBar().hide();
//		}
		pgb_top = (ProgressBar)findViewById(R.id.progressbar_top);
		pgb_bottom = (ProgressBar)findViewById(R.id.progressbar_bottom);
		recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
//		largeImage = (ImageView)findViewById(R.id.largeImageTest);
//		Layout = (OverScrollLayout) findViewById(R.id.layout);
		myHelper = new MySQLiteOpenHelper(MainActivity.this, "Image.db", null, 1);
		myHelper.getWritableDatabase();
		initPicture();


		View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.large_image, null);
		LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.largeImageLayout);
		largeImage2 = (ImageView)view.findViewById(R.id.largeImage2);
		builder = new AlertDialog.Builder(MainActivity.this ,R.style.edit_AlertDialog_style).setView(linearLayout);
		builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveBitmap(pictureList.get((int)largeImage2.getTag()).getImage(), pictureList.get((int)largeImage2.getTag()).getMessage());
				((ViewGroup) view.getParent()).removeView(view);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((ViewGroup) view.getParent()).removeView(view);
				dialog.dismiss();
			}
		});
		builder.create();

//		dialog = new Dialog(MainActivity.this, R.style.edit_AlertDialog_style);
//		dialog.setContentView(R.layout.activity_main);
//		dialog.setCanceledOnTouchOutside(true);
//		largeImage = (ImageView)dialog.findViewById(R.id.largeImageTest);
//		largeImage.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//		largeImage.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				saveBitmap(pictureList.get((int)largeImage.getTag()).getImage(), pictureList.get((int)largeImage.getTag()).getMessage());
//				return true;
//			}
//		});

		swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				pgb_top.setVisibility(View.VISIBLE);
				allNum = firstNum;
				pictureList.clear();
				adapter.notifyDataSetChanged();
				isNew = true;
				initPicture();
				swipe.setRefreshing(false);
			}
		});

//		Layout.setScrollListener(new OverScrollLayout.ScrollListener() {
//			@Override
//			public void onScroll() {
//				if (adapter.VIEW_HEADER.getVisibility() == View.VISIBLE) {
//					Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
//					new Handler().postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							adapter.VIEW_HEADER.setVisibility(View.INVISIBLE);
//						}
//					},2000);
//				}else if (adapter.VIEW_FOOTER.getVisibility() == View.VISIBLE) {
//					Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
//					new Handler().postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							adapter.VIEW_FOOTER.setVisibility(View.INVISIBLE);
//						}
//					},2000);
//
//				}
//			}
//		});

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (!recyclerView.canScrollVertically(-1)){
					if (isConnect()) {
						allNum += moreNum;
						initPicture();
					}
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){
			case R.id.test:
				Toast.makeText(MainActivity.this,"text", Toast.LENGTH_SHORT).show();
//				img = findViewById(R.id.dog_image);
//				img.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
////						Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
////						ByteArrayOutputStream bStream = new ByteArrayOutputStream();
////						bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
////						byte[] data = bStream.toByteArray();
////						Intent intent = new Intent().setClass(MainActivity.this ,SecondActivity.class);
////						intent.putExtra("extra_data", data);
//////						startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, img, "sharedView").toBundle());
////						startActivity(intent);
//
//					}
//				});
				break;
			default:
		}
		return true;
	}

	private void saveBitmap(Bitmap bitmap, String message) {
		requestAllPower();//check permission
		String name = message.substring(message.indexOf("/") + 8, message.indexOf("."));
		String result = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, name, message);
		Log.d(TAG, "saveBitmap: " + name);
		Log.d(TAG, "environment: " + Environment.getExternalStorageDirectory());
		Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(result));
		sendBroadcast(scannerIntent);
		Toast.makeText(MainActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
		SQLiteDatabase db = myHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("MESSAGE", message);
		values.put("LOCATION", Environment.getExternalStorageDirectory() + "/Pictures/" + name + ".jpg");
		values.put("URL", "https://cdn.shibe.online/shibes/" + name + ".jpg");
		db.insert("SavedImage",null,values);
		db.close();
//		//创建文件，因为不存在2级目录，所以不用判断exist，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
//		String path = "/" + name + ".jpg";
//		Log.d(TAG, "saveBitmap: " + Environment.getExternalStorageDirectory() + getPackageName());
////		File dir = new File(Environment.getExternalStorageDirectory() + "/" + getPackageName());
////		File file = new File(dir, path);
//		File file = new File( "data/data/com.github.YizheYang" + path);
//		try {
//			Log.d(TAG, "exist " + file.exists());
//			if (!file.exists()){
//				//file.mkdirs();
//				file.createNewFile();
//			}
//			FileOutputStream fileOutputStream = new FileOutputStream(file);
//			//压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
//			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//			fileOutputStream.flush();
//			fileOutputStream.close();
//			Toast.makeText(MainActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			bitmap.recycle();
//		}
//		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//		Uri uri = Uri.fromFile(file);
//		intent.setData(uri);
//		getApplicationContext().sendBroadcast(intent);
	}

	public void requestAllPower() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			//refuse == true
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				//toast to explain
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, 1);
			}
		}
	}

	private boolean isConnect() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			return true;
		} else {
			return false;
		}
	}

	private void initPicture() {
		if (isConnect()) {
			for (i = 0 ; i < firstNum; i++){
				sendRequestWitHttpURLConnection();
			}
		}else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					SQLiteDatabase db = myHelper.getWritableDatabase();
					Cursor cursor = db.query("SavedImage", null, null, null, null, null, null);
					if (cursor.moveToFirst()) {
						do {
							String message = cursor.getString(cursor.getColumnIndex("MESSAGE"));
							String location = cursor.getString(cursor.getColumnIndex("LOCATION"));
							Log.d(TAG, "message: " + message);
							Log.d(TAG, "location " + location);
							Bitmap bitmap = BitmapFactory.decodeFile(location);
							Log.d(TAG, "bitmap: " + bitmap.getHeight());
							Object[] obj = new Object[2];
							obj[0] = bitmap;
							obj[1] = message;
							Message pic = new Message();
							pic.what = 1;
							pic.obj = obj;
							handler.sendMessage(pic);
						} while (cursor.moveToNext());
					}else {
						Message pic = new Message();
						pic.what = 0;
						handler.sendMessage(pic);
					}
					cursor.close();
				}
			}).start();
		}
	}

	private String getExactUrl(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection con = (HttpURLConnection) u.openConnection();
		con.setRequestMethod("GET");
		con.setConnectTimeout(5 * 1000);
		con.setReadTimeout(8 * 1000);
		InputStream in = con.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder res = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			res.append(line);
		}
		String result = res.toString();
		result = result.substring(result.indexOf("[") + 2, result.indexOf("]") - 1);//cut
		in.close();
		br.close();
		return result;
	}

	private void sendRequestWitHttpURLConnection(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL(getExactUrl(path));
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(50 * 1000);
					connection.setReadTimeout(80 * 1000);
					if (connection.getResponseCode() == 200){
						InputStream in = connection.getInputStream();
						Bitmap bitmap = BitmapFactory.decodeStream(in);
						Object[] obj = new Object[2];
						obj[0] = bitmap;
						obj[1] = url.getPath();
						Message pic = new Message();
						pic.what = 1;
						pic.obj = obj;
						handler.sendMessage(pic);
						in.close();
//						Log.d(TAG, "run: " + url.getPath());
//						Log.d(TAG, "run: " + bitmap);
					} else{
						Message pic = new Message();
						pic.what = 0;
						handler.sendMessage(pic);
					}
					sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}