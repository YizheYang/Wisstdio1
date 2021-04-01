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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

	private List<Picture> mPictureList;
	public static View mHeaderView;
	public static final int TYPE_BUTTON = 0;
	public static final int TYPE_NORMAL = 1;

	static class ViewHolder extends RecyclerView.ViewHolder{
		TextView text;
		ImageView image;

		public ViewHolder(View view){
			super(view);
			if(itemView == mHeaderView) return;
			text = (TextView)view.findViewById(R.id.dogpath);
			image = (ImageView)view.findViewById(R.id.dogimage);
		}
	}

	public PictureAdapter(List<Picture> PictureList) {
		mPictureList = PictureList;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if(mHeaderView != null && viewType == TYPE_BUTTON) return new ViewHolder(mHeaderView);
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

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof GridLayoutManager){
			final GridLayoutManager gridLayoutManager = (GridLayoutManager)layoutManager;
			gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return getItemViewType(position) == TYPE_BUTTON ? gridLayoutManager.getSpanCount() : 1;
				}
			});
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(mHeaderView == null) return TYPE_NORMAL;
		if(position == 0) return TYPE_BUTTON;
		return TYPE_NORMAL;
	}

	public void setHeaderView(View headerView){
		mHeaderView = headerView;
		notifyItemInserted(0);
	}
}
