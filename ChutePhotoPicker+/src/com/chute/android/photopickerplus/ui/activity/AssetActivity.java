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
package com.chute.android.photopickerplus.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.Toast;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.ui.fragment.FragmentRoot;
import com.chute.android.photopickerplus.ui.fragment.FragmentSingle;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.chute.android.photopickerplus.ui.listener.ListenerAccountAssetsSelection;
import com.chute.android.photopickerplus.ui.listener.ListenerImageSelection;
import com.chute.android.photopickerplus.ui.listener.ListenerVideoSelection;
import com.chute.android.photopickerplus.util.AppUtil;
import com.chute.android.photopickerplus.util.Constants;
import com.chute.android.photopickerplus.models.enums.PhotoFilterType;
import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.chute.android.photopickerplus.util.intent.IntentUtil;
import com.chute.android.photopickerplus.util.intent.PhotosIntentWrapper;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.api.authentication.AuthenticationFactory;
import com.chute.sdk.v2.model.AccountModel;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ListResponseModel;
import com.chute.sdk.v2.utils.PreferenceUtil;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

/**
 * Activity for displaying the content of the selected service.
 * 
 * This activity is used to display albums and assets for both local and remote
 * services in a GridView.
 * 
 */
public class AssetActivity extends FragmentActivity implements
		ListenerFilesCursor, ListenerFilesAccount {

	private PhotoFilterType filterType;
	private PhotosIntentWrapper wrapper;
	private FragmentRoot fragmentRoot;
	private FragmentSingle fragmentSingle;
	private AccountModel account;
	private List<Integer> selectedAccountsPositions;
	private List<String> selectedImagesPaths;
	private List<String> selectedVideosPaths;
	private ListenerAccountAssetsSelection listenerAccountsSelection;
	private ListenerImageSelection listenerImagesSelection;
	private ListenerVideoSelection listenerVideosSelection;
	private String folderId;
	private AccountType accountType;

	public void setAssetsSelectListener(
			ListenerAccountAssetsSelection adapterListener) {
		this.listenerAccountsSelection = adapterListener;
	}

	public void setImagesSelectListener(ListenerImageSelection adapterListener) {
		this.listenerImagesSelection = adapterListener;
	}

	public void setVideosSelectListener(ListenerVideoSelection adapterListener) {
		this.listenerVideosSelection = adapterListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.gc_activity_assets);

		selectedAccountsPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS)
				: null;

		selectedImagesPaths = savedInstanceState != null ? savedInstanceState
				.getStringArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS) : null;

		selectedVideosPaths = savedInstanceState != null ? savedInstanceState
				.getStringArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS) : null;


		folderId = savedInstanceState != null ? savedInstanceState
				.getString(Constants.KEY_FOLDER_ID) : null;

		wrapper = new PhotosIntentWrapper(getIntent());
		account = wrapper.getAccount();
		filterType = wrapper.getFilterType();

		fragmentRoot = (FragmentRoot) getSupportFragmentManager()
				.findFragmentById(R.id.gcFragmentAssets);
		fragmentRoot.setRetainInstance(true);
		fragmentRoot.updateFragment(account, filterType,
				selectedAccountsPositions, selectedImagesPaths,
				selectedVideosPaths);
	}

	@Override
	public void onAccountFilesSelect(AssetModel assetModel) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this, assetModel);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onCursorAssetsSelect(AssetModel assetModel) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this, assetModel);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onDeliverCursorAssets(ArrayList<String> assetPathList) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this,
				AppUtil.getPhotoCollection(assetPathList));
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onDeliverAccountFiles(ArrayList<AssetModel> assetModelList) {
		IntentUtil.deliverDataToInitialActivity(AssetActivity.this,
				assetModelList);
		setResult(RESULT_OK);
		finish();

	}

	@Override
	public void onAccountFolderSelect(AccountModel account, String folderId) {
		this.folderId = folderId;
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments, FragmentSingle
				.newInstance(account, folderId, selectedAccountsPositions),
				Constants.TAG_FRAGMENT_FILES);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(Constants.KEY_FOLDER_ID, folderId);
		List<Integer> accountPositions = new ArrayList<Integer>();
		List<String> imagePaths = new ArrayList<String>();
		List<String> videoPaths = new ArrayList<String>();
		if (listenerAccountsSelection != null
				&& listenerAccountsSelection.getSocialPhotosSelection() != null) {
			accountPositions.addAll(listenerAccountsSelection
					.getSocialPhotosSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS,
					(ArrayList<Integer>) accountPositions);
		}
		if (listenerImagesSelection != null
				&& listenerImagesSelection.getCursorImagesSelection() != null) {
			imagePaths.addAll(listenerImagesSelection
					.getCursorImagesSelection());
			outState.putStringArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS,
					(ArrayList<String>) imagePaths);
		}
		if (listenerVideosSelection != null
				&& listenerVideosSelection.getCursorVideosSelection() != null) {
			videoPaths.addAll(listenerVideosSelection
					.getCursorVideosSelection());
			outState.putStringArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS,
					(ArrayList<String>) videoPaths);
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		fragmentSingle = (FragmentSingle) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
		if (fragmentSingle != null) {
			fragmentSingle.setRetainInstance(true);
			fragmentSingle.updateFragment(account, folderId,
					selectedAccountsPositions);
		}
	}

	@Override
	public void onSessionExpired(AccountType accountType) {
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		AuthenticationFactory.getInstance().startAuthenticationActivity(
				AssetActivity.this, accountType);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			GCAccounts.allUserAccounts(getApplicationContext(),
					new AccountsCallback()).executeAsync();
		}
	}

	private final class AccountsCallback implements
			HttpCallback<ListResponseModel<AccountModel>> {

		@Override
		public void onSuccess(ListResponseModel<AccountModel> responseData) {
			if (accountType == null) {
				accountType = PhotoPickerPreferenceUtil.get().getAccountType();
			}
			if (responseData.getData().size() == 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.no_albums_found),
						Toast.LENGTH_SHORT).show();
				return;
			}
			for (AccountModel accountModel : responseData.getData()) {
				if (accountModel.getType().equals(accountType.getLoginMethod())) {
					PreferenceUtil.get().saveAccount(accountModel);
					accountClicked(accountModel.getId(), accountType.name()
							.toLowerCase(), accountModel.getShortcut());
				}
			}
		}

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			ALog.d("Http Error: " + responseStatus.getStatusCode() + " "
					+ responseStatus.getStatusMessage());
		}

	}

	public void accountClicked(String accountId, String accountName,
			String accountShortcut) {
		selectedAccountsPositions = null;
		selectedImagesPaths = null;
		selectedVideosPaths = null;
		if (fragmentRoot != null) {
			fragmentRoot.setRetainInstance(true);
			fragmentRoot.updateFragment(account, filterType,
					selectedAccountsPositions, selectedImagesPaths,
					selectedVideosPaths);
		}
	}

}