/*
 * Copyright <2021> WISStudio Inc.
 */
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

/**
 * 自定义的适配器类，用于将数据放进组件里
 * @author 一只羊
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

	public View VIEW_HEADER;
	public View VIEW_FOOTER;
	private final List<Image> mImageList;
	private final int TYPE_NORMAL = 1000;
	private final int TYPE_HEADER = 1001;
	private final int TYPE_FOOTER = 1002;
	private OnItemClickListener mOnItemClickListener;
	private RecyclerView mRecyclerView;
	private final Context mContext;

	static class ViewHolder extends RecyclerView.ViewHolder{
		TextView text;
		ImageView image;

		public ViewHolder(View view){
			super(view);
			text = view.findViewById(R.id.dog_path);
			image = view.findViewById(R.id.dog_image);
		}
	}

	public ImageAdapter(List<Image> imageList, Context context) {
		this.mImageList = imageList;
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
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (!isHeaderView(position) && !isFooterView(position)) {
			if (haveHeaderView()) {
				position--;
			}
			Image image = mImageList.get(position);
			holder.image.setImageBitmap(image.getCompressedImage());
			holder.text.setText(image.getMessage());
			//自定义点击事件
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
		int count = (mImageList == null ? 0 : mImageList.size());
		if (VIEW_FOOTER != null) {
			count++;
		}
		if (VIEW_HEADER != null) {
			count++;
		}
		return count;
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
	}

	/**
	 * 让头部和尾部自成一行
	 * @param recyclerView 是获取到的recyclerview
	 */
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

	/**
	 * 添加头部
	 * @param headerView 是头部
	 */
	public void addHeaderView(View headerView) {
		if (haveHeaderView()) {
			throw new IllegalStateException("headerView has already exists!");
		} else {
			//避免出现宽度自适应
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			headerView.setLayoutParams(params);
			VIEW_HEADER = headerView;
			ifGridLayoutManager();
			notifyItemInserted(0);
		}
	}

	/**
	 * 添加尾部
	 * @param footerView 是尾部
	 */
	public void addFooterView(View footerView) {
		if (haveFooterView()) {
			throw new IllegalStateException("footerView has already exists!");
		} else {
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			footerView.setLayoutParams(params);
			VIEW_FOOTER = footerView;
			VIEW_FOOTER.setVisibility(View.VISIBLE);
			ifGridLayoutManager();
			notifyItemInserted(getItemCount() - 1);
		}
	}

	/**
	 * 获取所使用的layout
	 * @param layoutId 是所使用的layout文件的布局id
	 * @return 使用的layout
	 */
	private View getLayout(int layoutId) {
		return LayoutInflater.from(mContext).inflate(layoutId, null);
	}

	/**
	 * 在网格流的显示下让头部和尾部自成一行
	 */
	private void ifGridLayoutManager() {
		if (mRecyclerView == null) {
			return;
		}
		final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
		if (layoutManager instanceof GridLayoutManager) {
			((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return (isHeaderView(position) || isFooterView(position))
							? ((GridLayoutManager) layoutManager).getSpanCount() : 1;
				}
			});
		}
	}

	/**
	 * 判断是否已经存在头部
	 * @return true是存在，false是不存在
	 */
	private boolean haveHeaderView() {
		return VIEW_HEADER != null;
	}

	/**
	 * 判断是否已经存在尾部
	 * @return true是存在，false是不存在
	 */
	public boolean haveFooterView() {
		return VIEW_FOOTER != null;
	}

	/**
	 * 判断该位置是不是头部
	 * @param position 是view的位置
	 * @return true代表是，false代表不是
	 */
	private boolean isHeaderView(int position) {
		return haveHeaderView() && position == 0;
	}

	/**
	 * 判断该位置是不是尾部
	 * @param position 是view的位置
	 * @return true代表是，false代表不是
	 */
	private boolean isFooterView(int position) {
		return haveFooterView() && position == getItemCount() - 1;
	}

	/**
	 * 注册点击接口
	 */
	public interface OnItemClickListener {
		void onItemClick(View view, int position);
	}

}
