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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.callback.ImageDataResponseLoader;
import com.chute.android.photopickerplus.config.PhotoPicker;
import com.chute.android.photopickerplus.ui.adapter.AssetAccountAdapter;
import com.chute.android.photopickerplus.ui.adapter.AssetAccountAdapter.AdapterItemClickListener;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.chute.android.photopickerplus.util.AppUtil;
import com.chute.android.photopickerplus.util.NotificationUtil;
import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.model.AccountAlbumModel;
import com.chute.sdk.v2.model.AccountBaseModel;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.AccountModel;
import com.chute.sdk.v2.model.response.ResponseModel;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

public class FragmentSingle extends Fragment implements
		AdapterItemClickListener {

	private GridView gridView;
	private TextView textViewSelectMedia;
	private ProgressBar progressBar;

	private AccountModel account;
	private String folderId;
	private boolean isMultipicker;
	private List<Integer> selectedItemsPositions;

	private AssetAccountAdapter accountAssetAdapter;
	private ListenerFilesAccount accountListener;

	public static FragmentSingle newInstance(AccountModel account,
			String folderId, List<Integer> selectedItemPositions) {
		FragmentSingle frag = new FragmentSingle();
		frag.account = account;
		frag.folderId = folderId;
		frag.selectedItemsPositions = selectedItemPositions;
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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

		textViewSelectMedia = (TextView) view
				.findViewById(R.id.gcTextViewSelectMedia);
		gridView = (GridView) view.findViewById(R.id.gcGridViewAssets);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		updateFragment(account, folderId, selectedItemsPositions);

		Button ok = (Button) view.findViewById(R.id.gcButtonOk);
		ok.setOnClickListener(new OkClickListener());
		Button cancel = (Button) view.findViewById(R.id.gcButtonCancel);
		cancel.setOnClickListener(new CancelClickListener());

		gridView.setNumColumns(getResources().getInteger(
				R.integer.grid_columns_assets));

		return view;
	}

	public void updateFragment(AccountModel account, String folderId,
			List<Integer> selectedItemsPositions) {

		isMultipicker = PhotoPicker.getInstance().isMultiPicker();
		this.account = account;
		this.selectedItemsPositions = selectedItemsPositions;
		this.folderId = folderId;

		String encodedId = Uri.encode(folderId);
		if (getActivity() != null) {
			GCAccounts.accountSingle(
					getActivity(),
					PhotoPickerPreferenceUtil.get().getAccountType().name()
							.toLowerCase(), account.getShortcut(), encodedId,
					new AccountSingleCallback()).executeAsync();
		}

	}

	private final class AccountSingleCallback implements
			HttpCallback<ResponseModel<AccountBaseModel>> {

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			ALog.d("Http Error: " + responseStatus.getStatusMessage() + " "
					+ responseStatus.getStatusCode());
			progressBar.setVisibility(View.GONE);
			NotificationUtil.makeConnectionProblemToast(getActivity());

		}

		@Override
		public void onSuccess(ResponseModel<AccountBaseModel> responseData) {
			progressBar.setVisibility(View.GONE);
			boolean supportImages = PhotoPicker.getInstance().supportImages();
			boolean supportVideos = PhotoPicker.getInstance().supportVideos();
			if (responseData.getData() != null && getActivity() != null) {
				accountAssetAdapter = new AssetAccountAdapter(getActivity(),
						AppUtil.filterFiles(responseData.getData(),
								supportImages, supportVideos),
						FragmentSingle.this);
				gridView.setAdapter(accountAssetAdapter);

				if (selectedItemsPositions != null) {
					for (int position : selectedItemsPositions) {
						accountAssetAdapter.toggleTick(position);
					}
				}

				AppUtil.setFragmentLabel(getActivity().getApplicationContext(), textViewSelectMedia, supportImages, supportVideos, supportVideos);
				NotificationUtil.showPhotosAdapterToast(getActivity()
						.getApplicationContext(), accountAssetAdapter
						.getCount());

			}

		}

	}

	@Override
	public void onFolderClicked(int position) {
		AccountAlbumModel album = (AccountAlbumModel) accountAssetAdapter
				.getItem(position);
		accountListener.onAccountFolderSelect(account, album.getId());

	}

	@Override
	public void onFileClicked(int position) {
		if (isMultipicker == true) {
			accountAssetAdapter.toggleTick(position);
		} else {
			ArrayList<AccountMediaModel> accountMediaModelList = new ArrayList<AccountMediaModel>();
			accountMediaModelList.add((AccountMediaModel) accountAssetAdapter
					.getItem(position));
			ImageDataResponseLoader.postImageData(getActivity()
					.getApplicationContext(), accountMediaModelList,
					accountListener);
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
			if (!accountAssetAdapter.getPhotoCollection().isEmpty()) {
				ImageDataResponseLoader.postImageData(getActivity()
						.getApplicationContext(), accountAssetAdapter
						.getPhotoCollection(), accountListener);
			}
		}
	}

}