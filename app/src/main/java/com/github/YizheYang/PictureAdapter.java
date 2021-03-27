package com.github.YizheYang;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

	private List<Picture> mPictureList;

	static class ViewHolder extends RecyclerView.ViewHolder{
		TextView text;
		ImageView image;

		public ViewHolder(View view){
			super(view);
			text = (TextView)view.findViewById(R.id.dogpath);
			image = (ImageView)view.findViewById(R.id.dogimage);
		}
	}

	public PictureAdapter(List<Picture> PictureList) {
		mPictureList = PictureList;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_item, parent, false);
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Picture picture = mPictureList.get(position);
		holder.image.setImageBitmap(picture.getImage());
		holder.text.setText(picture.getMessage());
	}

	@Override
	public int getItemCount() {
		return mPictureList.size();
	}
}
