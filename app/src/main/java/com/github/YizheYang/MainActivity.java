package com.github.YizheYang;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.MainThread;
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
	Dialog dialog;
	AlertDialog.Builder builder;
	private List<Picture> pictureList = new ArrayList<>();
	private int i;
	private Picture tempPicture;
	protected int pictureNum = 3 * 5;
	protected int spanCount = 3;

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
				if (pictureList.size() == pictureNum) {
					pgb_top.setVisibility(View.GONE);
					Message re = new Message();
					re.what = 2;
					refresh.sendMessage(re);
//					PictureAdapter adapter = new PictureAdapter(pictureList);
//					recyclerView.setAdapter(adapter);
//					GridLayoutManager LayoutManager = new GridLayoutManager(MainActivity.this, 3);
//					recyclerView.setLayoutManager(LayoutManager);
				}
			}else{
				Toast.makeText(MainActivity.this ,"error" ,Toast.LENGTH_SHORT).show();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private final Handler refresh = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if(msg.what == 2){
				recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, spanCount));
				adapter = new PictureAdapter(pictureList, MainActivity.this);
//				adapter.addHeaderView(LayoutInflater.from(MainActivity.this).inflate(R.layout.refresh_top, null));
				adapter.addFooterView(LayoutInflater.from(MainActivity.this).inflate(R.layout.refresh_bottom, null));
				recyclerView.setAdapter(adapter);
//				Rect rect = new Rect();
//				rect.set(recyclerView.getLeft(),recyclerView.getTop(),recyclerView.getRight(),recyclerView.getBottom());
//				recyclerView.layout(rect.left, rect.top - adapter.VIEW_HEADER.getMeasuredHeight() - 10, rect.right, rect.bottom + adapter.VIEW_FOOTER.getMeasuredHeight() + 10);
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
				largeImage.setImageBitmap(bitmap);
				largeImage.setTag(data[1]);
				largeImage.setVisibility(View.VISIBLE);
				dialog.show();
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
		initPicture();

//		largeImage = (ImageView)findViewById(R.id.largeImageTest);
//		builder = new AlertDialog.Builder(MainActivity.this);
//		builder.setView(largeImage);
//		builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Toast.makeText(MainActivity.this, "yes", Toast.LENGTH_SHORT).show();
//			}
//		});
//		builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Toast.makeText(MainActivity.this,"no", Toast.LENGTH_SHORT).show();
//			}
//		});
//		builder.create();
//		builder.show();

		dialog = new Dialog(MainActivity.this, R.style.edit_AlertDialog_style);
		dialog.setContentView(R.layout.activity_main);
		dialog.setCanceledOnTouchOutside(true);
		largeImage = (ImageView)dialog.findViewById(R.id.largeImageTest);
		largeImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		largeImage.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				saveBitmap(pictureList.get((int)largeImage.getTag()).getImage(), pictureList.get((int)largeImage.getTag()).getMessage());
				return true;
			}
		});

		swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "refresh" , Toast.LENGTH_SHORT).show();
						swipe.setRefreshing(false);
					}
				}, 2000);
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

//		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//				super.onScrollStateChanged(recyclerView, newState);
////				if (!recyclerView.canScrollVertically(1)){
////					new Handler().postDelayed(new Runnable() {
////						@Override
////						public void run() {
////							if (!recyclerView.canScrollVertically(1)){
//////								pgb_top.setVisibility(View.VISIBLE);
//////								pictureList = new ArrayList<>();
//////								initPicture();
////
//////								Message large = new Message();
//////								large.what = 3;
//////								enlarge.sendMessage(large);
////							}else if(!recyclerView.canScrollVertically(-1)){
//////								pgb_bottom.setVisibility(View.VISIBLE);
//////								pictureNum *= 2;
//////								initPicture();
////							}
////						}
////					}, 3000);
////				}
////				Log.d(TAG, "recyclerView.canScrollVertically(bottom) " + recyclerView.canScrollVertically(-1));
////				Log.d(TAG, "recyclerView.canScrollVertically(top) " + recyclerView.canScrollVertically(1));
//			}
//		});


//		RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
//		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//		recyclerView.setLayoutManager(linearLayoutManager);
//		PictureAdapter adapter = new PictureAdapter(pictureList);
//		recyclerView.setAdapter(adapter);
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
				img = findViewById(R.id.dog_image);
				img.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
//						ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//						bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
//						byte[] data = bStream.toByteArray();
//						Intent intent = new Intent().setClass(MainActivity.this ,SecondActivity.class);
//						intent.putExtra("extra_data", data);
////						startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, img, "sharedView").toBundle());
//						startActivity(intent);

					}
				});

				break;
			default:
		}
		return true;
	}

	private void saveBitmap(Bitmap bitmap, String message)
	{
		requestAllPower();//check permission
		String name = message.substring(message.indexOf("/") + 8, message.indexOf("."));
		String result = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, name,message);
		Toast.makeText(MainActivity.this, "保存成功!", Toast.LENGTH_SHORT).show();
		Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(result));
		sendBroadcast(scannerIntent);

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


	private void initPicture(){
		for (i = 0 ; i < pictureNum ; i++){
			sendRequestWitHttpURLConnection();
		}

//		for (i = 0;i < 10;i++){
//			//tempPicture.run();
//			Picture temp = new Picture("0" ,null);
//			tempPicture = temp.initPicture();
//
//			pictureList.add(tempPicture);
//		}
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
		while ((line = br.readLine()) != null){
			res.append(line);
		}
		String result = (String)res.toString();
		result = result.substring(result.indexOf("[") + 2 , result.indexOf("]") - 1);//cut
		return result;
	}

	private void sendRequestWitHttpURLConnection(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "run: " + Thread.currentThread().getId());
					URL url = new URL(getExactUrl(path));
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(5 * 1000);
					connection.setReadTimeout(8 * 1000);
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
//						Log.d(TAG, "run: " + url.getPath());
//						Log.d(TAG, "run: " + bitmap);
					}
					else{
						Message message = new Message();
						message.what = 0;
						handler.sendMessage(message);
					}
					sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

//	private void parseJSONWithGSON(String jsonData){
//		Gson gson = new Gson();
//		List<Picture> pictureList = gson.fromJson(jsonData ,new TypeToken<List<Picture>>(){}.getType());
//		for (Picture picture : pictureList){
//			Message message = new Message();
//			message.what = 1;
//			message.obj = picture;
//			handler.sendMessage(message);
//		}
//
//	}

}