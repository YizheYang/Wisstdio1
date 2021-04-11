package com.github.YizheYang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

	private final List<Picture> mPictureList;
	private OnItemClickListener mOnItemClickListener;

	static class ViewHolder extends RecyclerView.ViewHolder{
		TextView text;
		ImageView image;

		public ViewHolder(View view){
			super(view);
			text = (TextView)view.findViewById(R.id.dog_path);
			image = (ImageView)view.findViewById(R.id.dog_image);
		}
	}

	public PictureAdapter(List<Picture> PictureList) {
		mPictureList = PictureList;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Picture picture = mPictureList.get(position);
		holder.image.setImageBitmap(picture.getCompressedImage());
		holder.text.setText(picture.getMessage());
		if(mOnItemClickListener != null){
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mOnItemClickListener.onItemClick(v, position);
				}
			});
		}
//		holder.text.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				System.out.println(position);
//			}
//		});
	}

	@Override
	public int getItemCount() {
		return mPictureList.size();
	}

	public interface OnItemClickListener {
		void onItemClick(View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

}
