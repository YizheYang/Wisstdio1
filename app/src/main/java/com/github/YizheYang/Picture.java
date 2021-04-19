package com.github.YizheYang;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Picture {

	public Bitmap image;

	public String message;

	public Picture(String message ,Bitmap image){
		this.image = image;
		this.message = message;
	}

	public Picture(String message){
		this.message = message;
	}

	public Picture(Bitmap image){
		this.image = image;
	}

	public Bitmap getImage(){
		return image;
	}

	public Bitmap getCompressedImage(){
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, bStream);
		byte[] data = bStream.toByteArray();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;
		int temp = Math.max(image.getHeight(), image.getWidth());
		int scale = (temp / 130);
		if ((temp / 130) * 130 < temp) {
			scale = (temp / 130) + 1;
		}
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data,0, data.length, options);

//		Matrix matrix = new Matrix();
//		matrix.setScale(0.5f, 0.5f);
//		return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

//		return Bitmap.createScaledBitmap(image, 130, 130,true);
	}

	public String getMessage(){
		return message;
	}

	public void run(){
		String path = "http://shibe.online/api/shibes?count=1&urls=true&httpsUrls=true";
//		Picture tempPicture = new Picture("0", null);
		try {
//			Log.d(TAG, "run: " + Thread.currentThread().getId());
			URL url = new URL(getExactUrl(path));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5 * 1000);
			connection.setReadTimeout(8 * 1000);
			if (connection.getResponseCode() == 200){
				InputStream in = connection.getInputStream();
				Bitmap bitmap = BitmapFactory.decodeStream(in);
				this.message = url.getPath();
				this.image = bitmap;
//				Log.d(TAG, "run: " + url.getPath());
//				Log.d(TAG, "run: " + bitmap);
//				tempPicture = new Picture(url.getPath(), bitmap);
//				Log.d(TAG, "run: " + tempPicture);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return tempPicture;
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

	public Picture initPicture(){
		String path = "http://shibe.online/api/shibes?count=1&urls=true&httpsUrls=true";
		final Picture[] tempPicture = new Picture[1];
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
		//			Log.d(TAG, "run: " + Thread.currentThread().getId());
					URL url = new URL(getExactUrl(path));
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(5 * 1000);
					connection.setReadTimeout(8 * 1000);
					if (connection.getResponseCode() == 200){
						InputStream in = connection.getInputStream();
						Bitmap bitmap = BitmapFactory.decodeStream(in);
						//				Log.d(TAG, "run: " + url.getPath());
						//				Log.d(TAG, "run: " + bitmap);
						tempPicture[0] = new Picture(url.getPath(), bitmap);
						//				Log.d(TAG, "run: " + tempPicture);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return tempPicture[0];
	}

}
