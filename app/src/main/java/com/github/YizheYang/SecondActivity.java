package com.github.YizheYang;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Thread.sleep;

public class SecondActivity extends AppCompatActivity {
	private static final String TAG = "SecondActivity";
	private ImageView imageView;

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1){
				Bitmap bitmap = (Bitmap) msg.obj;
				imageView.setImageBitmap(bitmap);
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		imageView = (ImageView)findViewById(R.id.largeImage);
		byte[] data = getIntent().getByteArrayExtra("extra_data");
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		Message msg = new Message();
		msg.what = 1;
		msg.obj = bitmap;
		handler.sendMessage(msg);

		findViewById(R.id.largeImage).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityCompat.finishAfterTransition(SecondActivity.this);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){
			case R.id.save:
				Toast.makeText(SecondActivity.this, "saved", Toast.LENGTH_SHORT).show();
				break;
			default:
		}
		return true;
	}

	private void savebitmap(Bitmap bitmap)
	{
		//创建文件，因为不存在2级目录，所以不用判断exist，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
		File file=new File("/pic/mfw.jpg");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			//压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			Toast.makeText(SecondActivity.this, "写入成功!", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			bitmap.recycle();
			bitmap = null;
		}

	}

	//	private String getExactUrl(String url) throws IOException {
//		URL u = new URL(url);
//		HttpURLConnection con = (HttpURLConnection) u.openConnection();
//		con.setRequestMethod("GET");
//		con.setConnectTimeout(5 * 1000);
//		con.setReadTimeout(8 * 1000);
//		InputStream in = con.getInputStream();
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//		StringBuilder res = new StringBuilder();
//		String line;
//		while ((line = br.readLine()) != null){
//			res.append(line);
//		}
//		String result = (String)res.toString();
//		result = result.substring(result.indexOf("[") + 2 , result.indexOf("]") - 1);//cut
//		return result;
//	}
//
//	private void sendRequestWitHttpURLConnection(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Log.d(TAG, "run: " + Thread.currentThread().getId());
//					URL url = new URL(getExactUrl(path));
//					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//					connection.setRequestMethod("GET");
//					connection.setConnectTimeout(5 * 1000);
//					connection.setReadTimeout(8 * 1000);
//					if (connection.getResponseCode() == 200){
//						InputStream in = connection.getInputStream();
//						Bitmap bitmap = BitmapFactory.decodeStream(in);
//						Object[] obj = new Object[2];
//						obj[0] = bitmap;
//						obj[1] = url.getAuthority();
//						Message pic = new Message();
//						pic.what = 1;
//						pic.obj = obj;
//						handler.sendMessage(pic);
//						//						Log.d(TAG, "run: " + url.getPath());
//						//						Log.d(TAG, "run: " + bitmap);
//					}
//					else{
//						Message message = new Message();
//						message.what = 0;
//						handler.sendMessage(message);
//					}
//					sleep(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
//	}
}