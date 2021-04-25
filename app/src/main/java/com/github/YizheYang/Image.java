/*
 * Copyright <2021> WISStudio Inc.
 */
package com.github.YizheYang;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 图片类，是加载的数据的类型
 * @author 一只羊
 */
public class Image {

	public Bitmap image;
	public String message;

	public Image(String message, Bitmap image){
		this.image = image;
		this.message = message;
	}

	/**
	 * @return 返回bitmap类型的图片数据
	 */
	public Bitmap getImage(){
		return image;
	}

	/**
	 * @return 返回String类型的信息
	 */
	public String getMessage(){
		return message;
	}

	/**
	 * @param bStream用来将bitmap转化为byte数组，option用来改变图片的格式和压缩图片，scale是计算缩小比例
	 * @return 返回压缩的图片，以节省内存
	 */
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
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
//		return Bitmap.createScaledBitmap(image ,130 ,130 ,true);//这是原来的缩小方法，但是后来发现所占的内存并没有减小
	}
}
