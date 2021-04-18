package com.github.YizheYang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

	private final List<Picture> mPictureList;
	private OnItemClickListener mOnItemClickListener;

	private RecyclerView mRecyclerView;
	private Context mContext;
	View VIEW_HEADER;
	View VIEW_FOOTER;
	private final int TYPE_NORMAL = 1000;
	private final int TYPE_HEADER = 1001;
	private final int TYPE_FOOTER = 1002;

	static class ViewHolder extends RecyclerView.ViewHolder{
		TextView text;
		ImageView image;

		public ViewHolder(View view){
			super(view);
			text = (TextView)view.findViewById(R.id.dog_path);
			image = (ImageView)view.findViewById(R.id.dog_image);
		}
	}

	public PictureAdapter(List<Picture> PictureList ,Context context) {
		this.mPictureList = PictureList;
		this.mContext = context;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_FOOTER) {
			return new ViewHolder(VIEW_FOOTER);
		} else if (viewType == TYPE_HEADER) {
			return new ViewHolder(VIEW_HEADER);
		} else {
			View view = LayoutInflater.from(mContext).inflate(R.layout.picture_item, parent, false);
			return new ViewHolder(view);
		}
//		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_item, parent, false);
//		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
//		Picture picture = mPictureList.get(position);
//		holder.image.setImageBitmap(picture.getCompressedImage());
//		holder.text.setText(picture.getMessage());
//		if(mOnItemClickListener != null){
//			holder.itemView.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					mOnItemClickListener.onItemClick(v, position);
//				}
//			});
//		}
		if (!isHeaderView(position) && !isFooterView(position)) {
			if (haveHeaderView()) {
				position--;
			}
			Picture picture = mPictureList.get(position);
			holder.image.setImageBitmap(picture.getCompressedImage());
			holder.text.setText(picture.getMessage());
			if(mOnItemClickListener != null){
				int finalPosition = position;
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mOnItemClickListener.onItemClick(v, finalPosition);
					}
				});
			}
		}
	}

	@Override
	public int getItemCount() {
		int count = (mPictureList == null ? 0 : mPictureList.size());
		if (VIEW_FOOTER != null) {
			count++;
		}
		if (VIEW_HEADER != null) {
			count++;
		}
		return count;
//		return mPictureList.size();
	}

	public interface OnItemClickListener {
		void onItemClick(View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}

	@Override
	public int getItemViewType(int position) {
		if (isHeaderView(position)) {
			return TYPE_HEADER;
		} else if (isFooterView(position)) {
			return TYPE_FOOTER;
		} else {
			return TYPE_NORMAL;
		}
//		return position;
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		try {
			if (mRecyclerView == null) {
				mRecyclerView = recyclerView;
			}
			ifGridLayoutManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private View getLayout(int layoutId) {
		return LayoutInflater.from(mContext).inflate(layoutId, null);
	}

	public void addHeaderView(View headerView) {
		if (haveHeaderView()) {
			throw new IllegalStateException("hearview has already exists!");
		} else {
			//避免出现宽度自适应
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			headerView.setLayoutParams(params);
			VIEW_HEADER = headerView;
//			VIEW_HEADER.setVisibility(View.INVISIBLE);
			ifGridLayoutManager();
			notifyItemInserted(0);
		}

	}

	public void addFooterView(View footerView) {
		if (haveFooterView()) {
			throw new IllegalStateException("footerView has already exists!");
		} else {
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			footerView.setLayoutParams(params);
			VIEW_FOOTER = footerView;
			VIEW_FOOTER.setVisibility(View.VISIBLE);
			ifGridLayoutManager();
			notifyItemInserted(getItemCount() - 1);
		}
	}

	private void ifGridLayoutManager() {
		if (mRecyclerView == null) return;
		final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
		if (layoutManager instanceof GridLayoutManager) {
			final GridLayoutManager.SpanSizeLookup originalSpanSizeLookup = ((GridLayoutManager) layoutManager).getSpanSizeLookup();
			((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return (isHeaderView(position) || isFooterView(position)) ? ((GridLayoutManager) layoutManager).getSpanCount() : 1;
				}
			});
		}
	}

	private boolean haveHeaderView() {
		return VIEW_HEADER != null;
	}

	public boolean haveFooterView() {
		return VIEW_FOOTER != null;
	}

	private boolean isHeaderView(int position) {
		return haveHeaderView() && position == 0;
	}

	private boolean isFooterView(int position) {
		return haveFooterView() && position == getItemCount() - 1;
	}

}
