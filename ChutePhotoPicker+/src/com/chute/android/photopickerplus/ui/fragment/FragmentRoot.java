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
package com.chute.android.photopickerplus.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.callback.ImageDataResponseLoader;
import com.chute.android.photopickerplus.config.PhotoPicker;
import com.chute.android.photopickerplus.loaders.LocalImagesAsyncTaskLoader;
import com.chute.android.photopickerplus.loaders.LocalVideosAsyncTaskLoader;
import com.chute.android.photopickerplus.models.enums.PhotoFilterType;
import com.chute.android.photopickerplus.ui.adapter.AssetAccountAdapter;
import com.chute.android.photopickerplus.ui.adapter.AssetAccountAdapter.AdapterItemClickListener;
import com.chute.android.photopickerplus.ui.adapter.CursorAdapterImages;
import com.chute.android.photopickerplus.ui.adapter.CursorAdapterVideos;
import com.chute.android.photopickerplus.ui.adapter.MergeAdapter;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.chute.android.photopickerplus.util.Constants;
import com.chute.android.photopickerplus.util.NotificationUtil;
import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.model.AccountAlbumModel;
import com.chute.sdk.v2.model.AccountBaseModel;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.AccountModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ResponseModel;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

public class FragmentRoot extends Fragment implements AdapterItemClickListener {

	private GridView gridView;
	private CursorAdapterImages adapterImages;
	private CursorAdapterVideos adapterVideos;
	private AssetAccountAdapter adapterAccounts;
	private MergeAdapter adapterMerge;
	private TextView textViewSelectPhotos;
	private View emptyView;

	private boolean isMultipicker;
	private boolean supportVideos;
	private boolean supportImages;
	private List<Integer> selectedAccountsPositions;
	private List<String> selectedImagePaths;
	private List<String> selectedVideoPaths;
	private AccountModel account;
	private PhotoFilterType filterType;
	private AccountType accountType;
	private ListenerFilesCursor cursorListener;
	private ListenerFilesAccount accountListener;

	public static FragmentRoot newInstance(AccountModel account,
			PhotoFilterType filterType,
			List<Integer> selectedAccountsPositions,
			List<String> selectedImagePaths, List<String> selectedVideoPaths) {
		FragmentRoot frag = new FragmentRoot();
		frag.account = account;
		frag.filterType = filterType;
		frag.selectedAccountsPositions = selectedAccountsPositions;
		frag.selectedImagePaths = selectedImagePaths;
		frag.selectedVideoPaths = selectedVideoPaths;
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		cursorListener = (ListenerFilesCursor) activity;
		accountListener = (ListenerFilesAccount) activity;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gc_fragment_assets, container,
				false);

		textViewSelectPhotos = (TextView) view
				.findViewById(R.id.gcTextViewSelectPhotos);
		gridView = (GridView) view.findViewById(R.id.gcGridViewAssets);
		emptyView = view.findViewById(R.id.gc_empty_view_layout);
		gridView.setEmptyView(emptyView);

		Button ok = (Button) view.findViewById(R.id.gcButtonOk);
		Button cancel = (Button) view.findViewById(R.id.gcButtonCancel);

		ok.setOnClickListener(new OkClickListener());
		cancel.setOnClickListener(new CancelClickListener());

		if (savedInstanceState == null) {
			updateFragment(account, filterType, selectedAccountsPositions,
					selectedImagePaths, selectedVideoPaths);
		}

		gridView.setNumColumns(getResources().getInteger(
				R.integer.grid_columns_assets));

		return view;
	}

	public void updateFragment(AccountModel account,
			PhotoFilterType filterType,
			List<Integer> selectedAccountsPositions,
			List<String> selectedImagePaths, List<String> selectedVideoPaths) {

		isMultipicker = PhotoPicker.getInstance().isMultiPicker();
		supportVideos = PhotoPicker.getInstance().supportVideos();
		supportImages = PhotoPicker.getInstance().supportImages();
		this.filterType = filterType;
		this.selectedAccountsPositions = selectedAccountsPositions;
		this.account = account;

		if ((filterType == PhotoFilterType.ALL_PHOTOS)
				|| (filterType == PhotoFilterType.CAMERA_ROLL)) {
			if (supportImages) {
				getActivity().getSupportLoaderManager().initLoader(1, null,
						new ImagesLoaderCallback(selectedImagePaths));
			}
			if (supportVideos) {
				getActivity().getSupportLoaderManager().initLoader(2, null,
						new VideosLoaderCallback(selectedVideoPaths));
			}
		} else if (filterType == PhotoFilterType.SOCIAL_PHOTOS
				&& getActivity() != null) {
			accountType = PhotoPickerPreferenceUtil.get().getAccountType();
			GCAccounts.accountRoot(getActivity().getApplicationContext(),
					accountType.name().toLowerCase(), account.getShortcut(),
					new RootCallback()).executeAsync();
		}

		adapterMerge = new MergeAdapter();
		adapterImages = new CursorAdapterImages(getActivity(), null,
				cursorListener);
		adapterVideos = new CursorAdapterVideos(getActivity(), null,
				cursorListener);
		adapterMerge.addAdapter(adapterVideos);
		adapterMerge.addAdapter(adapterImages);
		gridView.setAdapter(adapterMerge);
		if (isMultipicker == true) {
			textViewSelectPhotos.setText(getActivity().getApplicationContext()
					.getResources().getString(R.string.select_photos));
		} else {
			textViewSelectPhotos.setText(getActivity().getApplicationContext()
					.getResources().getString(R.string.select_a_photo));
		}

	}

	private final class RootCallback implements
			HttpCallback<ResponseModel<AccountBaseModel>> {

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			if (getActivity() != null) {
				if (responseStatus.getStatusCode() == Constants.HTTP_ERROR_CODE_UNAUTHORIZED) {
					NotificationUtil
							.makeExpiredSessionLogginInAgainToast(getActivity()
									.getApplicationContext());
					accountListener.onSessionExpired(accountType);
				} else {
					NotificationUtil.makeConnectionProblemToast(getActivity()
							.getApplicationContext());
				}
			}
			toggleEmptyViewErrorMessage();

		}

		public void toggleEmptyViewErrorMessage() {
			emptyView.setVisibility(View.GONE);
		}

		@Override
		public void onSuccess(ResponseModel<AccountBaseModel> responseData) {
			if (responseData != null && getActivity() != null) {
				adapterAccounts = new AssetAccountAdapter(getActivity(),
						responseData.getData(), FragmentRoot.this);
				gridView.setAdapter(adapterAccounts);
				if (adapterAccounts.getCount() == 0) {
					emptyView.setVisibility(View.GONE);
				}

				if (selectedAccountsPositions != null) {
					for (int position : selectedAccountsPositions) {
						adapterAccounts.toggleTick(position);
					}
				}

				if (isMultipicker == true) {
					textViewSelectPhotos.setText(getActivity()
							.getApplicationContext().getResources()
							.getString(R.string.select_photos));
				} else {
					textViewSelectPhotos.setText(getActivity()
							.getApplicationContext().getResources()
							.getString(R.string.select_a_photo));
				}
				NotificationUtil.showPhotosAdapterToast(getActivity()
						.getApplicationContext(), adapterAccounts.getCount());
			}

		}

	}

	/*
	 * DEVICE IMAGES LOADER
	 */
	private final class ImagesLoaderCallback implements LoaderCallbacks<Cursor> {

		private List<String> imagePaths;

		private ImagesLoaderCallback(List<String> imagePaths) {
			this.imagePaths = imagePaths;
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
			return new LocalImagesAsyncTaskLoader(getActivity()
					.getApplicationContext(), filterType);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			if (cursor == null) {
				return;
			}

			emptyView.setVisibility(View.GONE);
			adapterImages.changeCursor(cursor);

			if (imagePaths != null) {
				for (String path : imagePaths) {
					adapterImages.toggleTick(path);
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub

		}

	}

	/*
	 * DEVICE VIDEOS LOADER
	 */
	private final class VideosLoaderCallback implements LoaderCallbacks<Cursor> {

		private List<String> videoPaths;

		private VideosLoaderCallback(List<String> videoPaths) {
			this.videoPaths = videoPaths;
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
			return new LocalVideosAsyncTaskLoader(getActivity()
					.getApplicationContext(), filterType);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			if (cursor == null) {
				return;
			}

			emptyView.setVisibility(View.GONE);
			adapterVideos.changeCursor(cursor);

			if (videoPaths != null) {
				for (String path : videoPaths) {
					adapterVideos.toggleTick(path);
				}
			}

		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub

		}

	}

	private final class CancelClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			getActivity().finish();
		}

	}

	private final class OkClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (filterType == PhotoFilterType.SOCIAL_PHOTOS) {
				if (!adapterAccounts.getPhotoCollection().isEmpty()) {
					ImageDataResponseLoader.postImageData(getActivity()
							.getApplicationContext(), adapterAccounts
							.getPhotoCollection(), accountListener);
				}
			} else if ((filterType == PhotoFilterType.ALL_PHOTOS)
					|| (filterType == PhotoFilterType.CAMERA_ROLL)) {
				ArrayList<String> deliverList = new ArrayList<String>();
				if (!adapterImages.getSelectedFilePaths().isEmpty()) {
					deliverList.addAll(adapterImages.getSelectedFilePaths());
				}
				if (!adapterVideos.getSelectedFilePaths().isEmpty()) {
					deliverList.addAll(adapterVideos.getSelectedFilePaths());
				}
				cursorListener.onDeliverCursorAssets(deliverList);
			}
		}
	}

	@Override
	public void onFolderClicked(int position) {
		AccountAlbumModel album = (AccountAlbumModel) adapterAccounts
				.getItem(position);
		accountListener.onAccountFolderSelect(account, album.getId());

	}

	@Override
	public void onFileClicked(int position) {
		if (isMultipicker == true) {
			adapterAccounts.toggleTick(position);
		} else {
			ArrayList<AccountMediaModel> accountMediaModelList = new ArrayList<AccountMediaModel>();
			accountMediaModelList.add((AccountMediaModel) adapterAccounts
					.getItem(position));
			ImageDataResponseLoader.postImageData(getActivity()
					.getApplicationContext(), accountMediaModelList,
					accountListener);
		}

	}

}