package com.github.YizheYang;

import android.graphics.Bitmap;
import android.media.Image;

public class Picture {

	private Bitmap image;

	private String message;

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

	public String getMessage(){
		return message;
	}

}
