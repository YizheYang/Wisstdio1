package com.github.YizheYang;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity" ;
	private String path = "http://shibe.online/api/shibes?count=1&urls=true&httpsUrls=true";
	private RecyclerView recyclerView;
	private ProgressBar pgb;

	private List<Picture> pictureList = new ArrayList<>();
	private int i;
	private Picture tempPicture;
	protected final int pictureNum = 31;

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
				if (pictureList.size() != 0) {
					pgb.setVisibility(View.GONE);
					Message re = new Message();
					re.what = 2;
					refresh.sendMessage(re);
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
				PictureAdapter adapter = new PictureAdapter(pictureList);
				recyclerView.setAdapter(adapter);
				GridLayoutManager LayoutManager = new GridLayoutManager(MainActivity.this, 3);
				recyclerView.setLayoutManager(LayoutManager);
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
		pgb = (ProgressBar)findViewById(R.id.progressbar);

		recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
		initPicture();

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (!recyclerView.canScrollVertically(1)){
					pictureList = new ArrayList<>();
					initPicture();
				}
				Log.d(TAG, "recyclerView.canScrollVertically(-1) " + recyclerView.canScrollVertically(-1));
				Log.d(TAG, "recyclerView.canScrollVertically(1) " + recyclerView.canScrollVertically(1));
			}
		});

//		ScrollView scr = new ScrollView();
//		scr.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//			@Override
//			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//			}
//		});
//		RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
//		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//		recyclerView.setLayoutManager(linearLayoutManager);
//		PictureAdapter adapter = new PictureAdapter(pictureList);
//		recyclerView.setAdapter(adapter);
	}

	private void initPicture(){
		for (i = 0;i < pictureNum;i++){
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
						obj[1] = url.getAuthority();
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