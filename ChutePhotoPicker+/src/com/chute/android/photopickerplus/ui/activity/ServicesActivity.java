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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.Toast;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.dao.MediaDAO;
import com.chute.android.photopickerplus.models.enums.PhotoFilterType;
import com.chute.android.photopickerplus.ui.fragment.FragmentEmpty;
import com.chute.android.photopickerplus.ui.fragment.FragmentRoot;
import com.chute.android.photopickerplus.ui.fragment.FragmentServices.ServiceClickedListener;
import com.chute.android.photopickerplus.ui.fragment.FragmentSingle;
import com.chute.android.photopickerplus.ui.listener.ListenerAccountAssetsSelection;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.chute.android.photopickerplus.ui.listener.ListenerImageSelection;
import com.chute.android.photopickerplus.ui.listener.ListenerVideoSelection;
import com.chute.android.photopickerplus.util.AppUtil;
import com.chute.android.photopickerplus.util.Constants;
import com.chute.android.photopickerplus.util.NotificationUtil;
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
 * Activity for displaying the services.
 * <p/>
 * This activity is used to display both local and remote services in a
 * GridView.
 */
public class ServicesActivity extends FragmentActivity implements
		ListenerFilesAccount, ListenerFilesCursor, ServiceClickedListener {

	private static final String TAG = ServicesActivity.class.getSimpleName();
	private static FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private AccountType accountType;
	private boolean dualPanes;
	private List<Integer> accountItemPositions;
	private List<String> imageItemPaths;
	private List<String> videoItemPaths;
	private String folderId;
	private AccountModel account;
	private ListenerAccountAssetsSelection listenerAssetsSelection;
	private ListenerImageSelection listenerImagesSelection;
	private ListenerVideoSelection listenerVideosSelection;
	private FragmentSingle fragmentSingle;
	private FragmentRoot fragmentRoot;
	private int photoFilterType;

	public void setAssetsSelectListener(
			ListenerAccountAssetsSelection adapterListener) {
		this.listenerAssetsSelection = adapterListener;
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
		fragmentManager = getSupportFragmentManager();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_layout);

		retrieveValuesFromBundle(savedInstanceState);

		dualPanes = getResources().getBoolean(R.bool.has_two_panes);
		if (dualPanes
				&& savedInstanceState == null
				&& getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
			replaceContentWithEmptyFragment();
		}

	}

	@Override
	public void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		Uri uri = AppUtil.getTempVideoFile();
		if (uri != null) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, Constants.CAMERA_VIDEO_REQUEST);

	}

	@Override
	public void lastVideo() {
		Uri uri = MediaDAO
				.getLastVideoFromCameraVideos(getApplicationContext());
		if (uri.toString().equals("")) {
			NotificationUtil.makeToast(getApplicationContext(), getResources()
					.getString(R.string.no_camera_photos));
		} else {
			final AssetModel model = new AssetModel();
			model.setThumbnail(uri.toString());
			model.setUrl(uri.toString());

			IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
					model);
		}
	}

	@Override
	public void takePhoto() {
		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			NotificationUtil.makeToast(getApplicationContext(),
					R.string.toast_feature_camera);
			return;
		}
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (AppUtil.hasImageCaptureBug() == false) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(AppUtil
					.getTempImageFile(ServicesActivity.this)));
		} else {
			intent.putExtra(
					android.provider.MediaStore.EXTRA_OUTPUT,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, Constants.CAMERA_PIC_REQUEST);
	}

	@Override
	public void lastPhoto() {
		Uri uri = MediaDAO
				.getLastPhotoFromCameraPhotos(getApplicationContext());
		if (uri.toString().equals("")) {
			NotificationUtil.makeToast(getApplicationContext(), getResources()
					.getString(R.string.no_camera_photos));
		} else {
			final AssetModel model = new AssetModel();
			model.setThumbnail(uri.toString());
			model.setUrl(uri.toString());

			IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
					model);
		}

	}

	@Override
	public void photoStream() {
		photoFilterType = PhotoFilterType.ALL_PHOTOS.ordinal();
		accountItemPositions = null;
		imageItemPaths = null;
		videoItemPaths = null;
		if (!dualPanes) {
			final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(
					ServicesActivity.this);
			wrapper.setFilterType(PhotoFilterType.ALL_PHOTOS);
			wrapper.startActivityForResult(ServicesActivity.this,
					PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
		} else {
			replaceContentWithRootFragment(null, PhotoFilterType.ALL_PHOTOS);
		}

	}

	@Override
	public void cameraRoll() {
		photoFilterType = PhotoFilterType.CAMERA_ROLL.ordinal();
		accountItemPositions = null;
		imageItemPaths = null;
		videoItemPaths = null;
		if (!dualPanes) {
			final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(
					ServicesActivity.this);
			wrapper.setFilterType(PhotoFilterType.CAMERA_ROLL);
			wrapper.startActivityForResult(ServicesActivity.this,
					PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
		} else {
			replaceContentWithRootFragment(null, PhotoFilterType.CAMERA_ROLL);
		}

	}

	public void accountClicked(AccountModel account) {
		photoFilterType = PhotoFilterType.SOCIAL_PHOTOS.ordinal();
		accountItemPositions = null;
		imageItemPaths = null;
		videoItemPaths = null;
		this.account = account;
		if (!dualPanes) {
			final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(
					ServicesActivity.this);
			wrapper.setFilterType(PhotoFilterType.SOCIAL_PHOTOS);
			wrapper.setAccount(account);
			wrapper.startActivityForResult(ServicesActivity.this,
					PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
		} else {
			replaceContentWithRootFragment(account,
					PhotoFilterType.SOCIAL_PHOTOS);
		}

	}

	public void replaceContentWithSingleFragment(AccountModel account,
			String folderId, List<Integer> selectedItemPositions) {
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments, FragmentSingle
				.newInstance(account, folderId, selectedItemPositions),
				Constants.TAG_FRAGMENT_FILES);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

	}

	public void replaceContentWithRootFragment(AccountModel account,
			PhotoFilterType filterType) {
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments, FragmentRoot.newInstance(
				account, filterType, accountItemPositions, imageItemPaths,
				videoItemPaths), Constants.TAG_FRAGMENT_FOLDER);
		fragmentTransaction.commit();
	}

	public void replaceContentWithEmptyFragment() {
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments,
				FragmentEmpty.newInstance(), Constants.TAG_FRAGMENT_EMPTY);
		fragmentTransaction.commit();
	}

	@Override
	public void accountLogin(AccountType type) {
		accountType = type;
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		if (PreferenceUtil.get().hasAccount(type.getLoginMethod())) {
			AccountModel account = PreferenceUtil.get().getAccount(
					type.getLoginMethod());
			accountClicked(account);
		} else {
			AuthenticationFactory.getInstance().startAuthenticationActivity(
					ServicesActivity.this, accountType);
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (requestCode == AuthenticationFactory.AUTHENTICATION_REQUEST_CODE) {
			GCAccounts.allUserAccounts(getApplicationContext(),
					new AccountsCallback()).executeAsync();
			return;
		}
		if (requestCode == PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY) {
			finish();
			return;
		}
		if (requestCode == Constants.CAMERA_PIC_REQUEST) {
			String path = "";
			File tempFile = AppUtil.getTempImageFile(getApplicationContext());
			if (AppUtil.hasImageCaptureBug() == false && tempFile.length() > 0) {
				try {
					android.provider.MediaStore.Images.Media.insertImage(
							getContentResolver(), tempFile.getAbsolutePath(),
							null, null);
					tempFile.delete();
					path = MediaDAO.getLastPhotoFromCameraPhotos(
							getApplicationContext()).toString();
				} catch (FileNotFoundException e) {
					ALog.d(TAG, "", e);
				}
			} else {
				ALog.e(TAG, "Bug " + data.getData().getPath());
				path = Uri.fromFile(
						new File(AppUtil.getPath(getApplicationContext(),
								data.getData()))).toString();
			}
			ALog.d(TAG, path);
			final AssetModel model = new AssetModel();
			model.setThumbnail(path);
			model.setUrl(path);
			model.setType(Constants.TYPE_IMAGE);
			ArrayList<AssetModel> mediaCollection = new ArrayList<AssetModel>();
			mediaCollection.add(model);
			setResult(Activity.RESULT_OK, new Intent().putExtra(
					PhotosIntentWrapper.KEY_PHOTO_COLLECTION, mediaCollection));
			finish();
		}
		if (requestCode == Constants.CAMERA_VIDEO_REQUEST) {
			Uri uriVideo = data.getData();
			File file = new File(uriVideo.getPath());

			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
					file.getAbsolutePath(),
					MediaStore.Images.Thumbnails.MINI_KIND);

			final AssetModel model = new AssetModel();
			model.setThumbnail(AppUtil.getImagePath(getApplicationContext(),
					thumbnail));
			model.setUrl(uriVideo.toString());
			model.setType(Constants.TYPE_VIDEO);
			ArrayList<AssetModel> mediaCollection = new ArrayList<AssetModel>();
			mediaCollection.add(model);
			setResult(Activity.RESULT_OK, new Intent().putExtra(
					PhotosIntentWrapper.KEY_PHOTO_COLLECTION, mediaCollection));
			finish();
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setResult(Activity.RESULT_OK,
				new Intent().putExtras(intent.getExtras()));
		ServicesActivity.this.finish();
	}

	@Override
	public void onDeliverAccountFiles(ArrayList<AssetModel> assetList) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				assetList);

	}

	@Override
	public void onDeliverCursorAssets(ArrayList<String> assetPathList) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				AppUtil.getPhotoCollection(assetPathList));

	}

	@Override
	public void onAccountFilesSelect(AssetModel assetModel) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				assetModel);
	}

	@Override
	public void onCursorAssetsSelect(AssetModel assetModel) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				assetModel);
	}

	@Override
	public void onAccountFolderSelect(AccountModel account, String folderId) {
		accountItemPositions = null;
		imageItemPaths = null;
		videoItemPaths = null;
		photoFilterType = PhotoFilterType.SOCIAL_PHOTOS.ordinal();
		this.folderId = folderId;
		this.account = account;
		replaceContentWithSingleFragment(account, folderId,
				accountItemPositions);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(Constants.KEY_FOLDER_ID, folderId);
		outState.putParcelable(Constants.KEY_ACCOUNT, account);
		outState.putInt(Constants.KEY_PHOTO_FILTER_TYPE, photoFilterType);
		List<Integer> accountPositions = new ArrayList<Integer>();
		List<String> imagePaths = new ArrayList<String>();
		List<String> videoPaths = new ArrayList<String>();
		if (listenerAssetsSelection != null
				&& listenerAssetsSelection.getSocialPhotosSelection() != null) {
			accountPositions.addAll(listenerAssetsSelection
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
		fragmentRoot = (FragmentRoot) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
		if (fragmentSingle != null
				&& photoFilterType == PhotoFilterType.SOCIAL_PHOTOS.ordinal()) {
			fragmentSingle.setRetainInstance(true);
			fragmentSingle.updateFragment(account, folderId,
					accountItemPositions);
		}
		if (fragmentRoot != null) {
			fragmentRoot.setRetainInstance(true);
			fragmentRoot.updateFragment(account,
					PhotoFilterType.values()[photoFilterType],
					accountItemPositions, imageItemPaths, videoItemPaths);
		}
	}

	private void retrieveValuesFromBundle(Bundle savedInstanceState) {
		accountItemPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS)
				: null;

		imageItemPaths = savedInstanceState != null ? savedInstanceState
				.getStringArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS) : null;

		videoItemPaths = savedInstanceState != null ? savedInstanceState
				.getStringArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS) : null;

		folderId = savedInstanceState != null ? savedInstanceState
				.getString(Constants.KEY_FOLDER_ID) : null;

		account = (AccountModel) (savedInstanceState != null ? savedInstanceState
				.getParcelable(Constants.KEY_ACCOUNT) : null);

		photoFilterType = savedInstanceState != null ? savedInstanceState
				.getInt(Constants.KEY_PHOTO_FILTER_TYPE) : 0;

	}

	@Override
	public void onDestroy() {
		Fragment fragmentFolder = fragmentManager
				.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
		Fragment fragmentFiles = fragmentManager
				.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
		if (fragmentFolder != null && fragmentFolder.isResumed()) {
			fragmentManager.beginTransaction().remove(fragmentFolder).commit();
		}
		if (fragmentFiles != null && fragmentFiles.isResumed()) {
			fragmentManager.beginTransaction().remove(fragmentFiles).commit();
		}
		super.onDestroy();
	}

	@Override
	public void onSessionExpired(AccountType accountType) {
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		AuthenticationFactory.getInstance().startAuthenticationActivity(
				ServicesActivity.this, accountType);
	}

	@Override
	public void onBackPressed() {
		fragmentRoot = (FragmentRoot) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
		fragmentSingle = (FragmentSingle) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
		if (fragmentRoot != null && fragmentRoot.isVisible()) {
			this.finish();
		} else {
			super.onBackPressed();
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
					accountClicked(accountModel);
				}
			}

		}

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			ALog.d("Http Error: " + responseStatus.getStatusCode() + " "
					+ responseStatus.getStatusMessage());
		}

	}

}