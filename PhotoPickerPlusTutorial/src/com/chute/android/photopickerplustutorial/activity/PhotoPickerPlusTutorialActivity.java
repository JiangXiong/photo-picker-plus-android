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
package com.chute.android.photopickerplustutorial.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.models.enums.MediaType;
import com.chute.android.photopickerplus.util.intent.PhotoPickerPlusIntentWrapper;
import com.chute.android.photopickerplustutorial.R;
import com.chute.android.photopickerplustutorial.adapter.GridAdapter;
import com.chute.sdk.v2.model.AssetModel;

public class PhotoPickerPlusTutorialActivity extends FragmentActivity {

	public static final String KEY_SELECTED_ITEMS = "keySelectedItems";
	public static final String KEY_VIDEO_PATH = "keyVideoPath";
	private GridView grid;
	private GridAdapter adapter;
	private ArrayList<AssetModel> accountMediaList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gc_activity_photo_picker);

		Button startButton = (Button) findViewById(R.id.gcButtonPhotoPicker);
		startButton.setOnClickListener(new OnPhotoPickerClickListener());
		grid = (GridView) findViewById(R.id.gcGrid);
		if (savedInstanceState != null) {
			accountMediaList = savedInstanceState
					.getParcelableArrayList(KEY_SELECTED_ITEMS);
			adapter = new GridAdapter(PhotoPickerPlusTutorialActivity.this,
					accountMediaList);
		} else {
			adapter = new GridAdapter(PhotoPickerPlusTutorialActivity.this,
					new ArrayList<AssetModel>());
		}
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new MediaItemClickListener());

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			grid.setNumColumns(getResources().getInteger(
					R.integer.grid_columns_landscape));
		}

	}

	private class OnPhotoPickerClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			PhotoPickerPlusIntentWrapper wrapper = new PhotoPickerPlusIntentWrapper(
					PhotoPickerPlusTutorialActivity.this);
			wrapper.startActivityForResult(PhotoPickerPlusTutorialActivity.this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		final PhotoPickerPlusIntentWrapper wrapper = new PhotoPickerPlusIntentWrapper(
				data);
		accountMediaList = wrapper.getMediaCollection();
		adapter.changeData(accountMediaList);
		ALog.d(wrapper.getMediaCollection().toString());

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(KEY_SELECTED_ITEMS, accountMediaList);
		super.onSaveInstanceState(outState);
	}

	private final class MediaItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			AssetModel asset = adapter.getItem(position);
			String type = asset.getType();
			if (type.equals(MediaType.VIDEO.name().toLowerCase())) {
				if (asset.getVideoUrl().contains("http")) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(asset.getVideoUrl()));
					startActivity(intent);
				} else {
					Intent intent = new Intent(getApplicationContext(),
							VideoPlayerActivity.class);
					intent.putExtra(KEY_VIDEO_PATH, asset.getUrl());
					startActivity(intent);
				}
			}
		}

	}
}
