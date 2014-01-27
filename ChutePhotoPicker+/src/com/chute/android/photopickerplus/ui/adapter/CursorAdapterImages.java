package com.chute.android.photopickerplus.ui.adapter;

import com.chute.android.photopickerplus.config.PhotoPicker;
import com.chute.android.photopickerplus.ui.fragment.CursorFilesListener;
import com.chute.android.photopickerplus.util.AppUtil;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CursorAdapterImages extends BaseCursorAdapter {

	private CursorFilesListener listener;

	public CursorAdapterImages(FragmentActivity context, Cursor c,
			CursorFilesListener listener) {
		super(context, c);
		this.listener = listener;
	}

	@Override
	public int getDataIndex(Cursor cursor) {
		if (cursor == null) {
			return 0;
		} else {
			return cursor.getColumnIndex(MediaStore.Images.Media.DATA);
		}
	}

	@Override
	public void setViewClickListener(View view, int position) {
		view.setOnClickListener(new ItemClickListener(position));

	}

	@Override
	public void setPlayButtonVisibility(ImageView imageView) {
		imageView.setVisibility(View.GONE);

	}

	private final class ItemClickListener implements OnClickListener {

		private int position;

		private ItemClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if (PhotoPicker.getInstance().isMultiPicker()) {
				toggleTick(position);
			} else {
				listener.onCursorAssetsSelect(AppUtil
						.getMediaModel(getItem(position)));
			}

		}

	}

}
