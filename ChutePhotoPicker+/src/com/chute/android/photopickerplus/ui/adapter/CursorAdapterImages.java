/**
 * The MIT License (MIT)

Copyright (c) 2013 Chute

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.chute.android.photopickerplus.ui.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.config.PhotoPicker;
import com.chute.android.photopickerplus.models.MediaResultModel;
import com.chute.android.photopickerplus.models.enums.MediaType;
import com.chute.android.photopickerplus.ui.activity.AssetActivity;
import com.chute.android.photopickerplus.ui.activity.ServicesActivity;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.chute.android.photopickerplus.ui.listener.ListenerImageSelection;
import com.chute.android.photopickerplus.util.AppUtil;

public class CursorAdapterImages extends BaseCursorAdapter implements
		ListenerImageSelection {

	private ListenerFilesCursor listener;

	public CursorAdapterImages(Context context, Cursor c,
			ListenerFilesCursor listener) {
		super(context, c);
		this.listener = listener;
		if (context.getResources().getBoolean(R.bool.has_two_panes)) {
			((ServicesActivity) context).setImagesSelectListener(this);
		} else {
			((AssetActivity) context).setImagesSelectListener(this);
		}
	}

	@Override
	public List<String> getCursorImagesSelection() {
		final ArrayList<String> photos = new ArrayList<String>();
		final Iterator<String> iterator = tick.values().iterator();
		while (iterator.hasNext()) {
			photos.add(iterator.next());
		}
		return photos;
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
	public void setViewClickListener(View view, String path, int position) {
		view.setOnClickListener(new ImageClickListener(path));

	}

	@Override
	public void setPlayButtonVisibility(ImageView imageView) {
		imageView.setVisibility(View.GONE);

	}

	private final class ImageClickListener implements OnClickListener {
		private String path;

		private ImageClickListener(String path) {
			this.path = path;
		}

		@Override
		public void onClick(View v) {
			if (PhotoPicker.getInstance().isMultiPicker()) {
				toggleTick(path);
			} else {
				listener.onCursorAssetsSelect(AppUtil.getMediaModel(createMediaResultModel(path)));
			}

		}

	}
	
	public List<MediaResultModel> getSelectedFilePaths() {
		final List<MediaResultModel> deliverList = new ArrayList<MediaResultModel>();
		final Iterator<String> iterator = tick.values().iterator();
		while (iterator.hasNext()) {
			String url = iterator.next();
//			MediaResultModel resultModel = new MediaResultModel();
//			resultModel.setImageUrl(url);
//			resultModel.setThumbnail(url);
//			resultModel.setMediaType(MediaType.VIDEO);
			deliverList.add(createMediaResultModel(url));
		}
		return deliverList;
	}
	
	
	public void toggleTick(String path) {
		if (tick.containsKey(path)) {
			tick.remove(path);
		} else {
			tick.put(path, path);
		}
		notifyDataSetChanged();
	}

	@Override
	public void loadImageView(ImageView imageView, Cursor cursor) {
		String path = cursor.getString(dataIndex);
		Uri uri = Uri.fromFile(new File(path));
		loader.displayImage(uri.toString(), imageView, null);
		
	}
	
	private MediaResultModel createMediaResultModel(String path) {
		MediaResultModel model = new MediaResultModel();
		model.setImageUrl(path);
		model.setThumbnail(path);
		model.setMediaType(MediaType.IMAGE);
		return model;
	}


}
