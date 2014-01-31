package com.chute.android.photopickerplus.ui.adapter;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.chute.android.photopickerplus.R;

import darko.imagedownloader.ImageLoader;

public abstract class BaseCursorAdapter extends CursorAdapter implements OnScrollListener {
	
	private static LayoutInflater inflater = null;
	public ImageLoader loader;
	protected int dataIndex;
	public Map<String, String> tick;
	protected boolean shouldLoadImages = true;

	@SuppressLint("NewApi")
	public BaseCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		loader = ImageLoader.getLoader(context.getApplicationContext());
		dataIndex = getDataIndex(c);
		tick = new HashMap<String, String>();

	}
	
	public abstract int getDataIndex(Cursor cursor);
	abstract public void setViewClickListener(View view, String path);
    abstract public void setPlayButtonVisibility(ImageView imageView);
	
	@Override
	public String getItem(int position) {
		final Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		return cursor.getString(dataIndex);
	}
	
	public static class ViewHolder {

		public ImageView imageViewThumb;
		public ImageView imageViewTick;
		public ImageView imageViewPlay;
	}


	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewHolder holder;
		View vi = inflater.inflate(R.layout.gc_adapter_assets, null);
		holder = new ViewHolder();
		holder.imageViewThumb = (ImageView) vi
				.findViewById(R.id.gcImageViewThumb);
		holder.imageViewTick = (ImageView) vi
				.findViewById(R.id.gcImageViewTick);
		holder.imageViewPlay = (ImageView) vi
				.findViewById(R.id.gcImageViewPlay);
		vi.setTag(holder);
		return vi;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
	case OnScrollListener.SCROLL_STATE_FLING:
	case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		shouldLoadImages = false;
		break;
	case OnScrollListener.SCROLL_STATE_IDLE:
		shouldLoadImages = true;
		notifyDataSetChanged();
		break;
	}
		
	}
	

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		dataIndex = getDataIndex(cursor);

	}


	




}
