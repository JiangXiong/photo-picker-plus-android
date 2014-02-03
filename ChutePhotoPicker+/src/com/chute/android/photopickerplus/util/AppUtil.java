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
package com.chute.android.photopickerplus.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;

import com.araneaapps.android.libs.logger.ALog;
import com.chute.android.photopickerplus.models.MediaResultModel;
import com.chute.android.photopickerplus.models.enums.MediaType;
import com.chute.sdk.v2.model.AccountAlbumModel;
import com.chute.sdk.v2.model.AccountBaseModel;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.utils.Utils;

/**
 * The {@link AppUtil} class contains helper methods including:
 * <ul>
 * <li>Get image thumbnail URL in 100x100 dimension
 * <li>Get chache directorium
 * <li>Check if the device has an image capture bug
 * <li>Set ImageView dimensions according to the device size
 * <li>Create {@link AssetModel}(s) using the image path
 * </ul>
 * 
 */
public class AppUtil {

	private static String SDCARD_FOLDER_CACHE = Environment
			.getExternalStorageDirectory() + "/Android/data/%s/files/";

	public static String getThumbSmallUrl(String urlNormal) {
		return Utils.getCustomSizePhotoURL(urlNormal, 100, 100);
	}

	public static File getTempImageFile(Context context) {
		final File path = getAppCacheDir(context);
		if (!path.exists()) {
			path.mkdirs();
		}
		File f = new File(path, "temp_image.jpg");
		if (f.exists() == false) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				ALog.w(e.getMessage() + " " + e);
			}
		}
		return f;
	}

	@SuppressLint("NewApi")
	public static Uri getTempVideoFile() {
		if (Environment.getExternalStorageState() == null) {
			return null;
		}
		File mediaStorage = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				"_VIDEO");
		if (!mediaStorage.exists() && !mediaStorage.mkdirs()) {
			ALog.e("Failed to create directory: " + mediaStorage);
			return null;
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		File mediaFile = new File(mediaStorage, "VID_" + timeStamp + ".mp4");
		return Uri.fromFile(mediaFile);
	}

	public static String getImagePath(Context context, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(context.getContentResolver(),
				inImage, "Title", null);
		return path;
	}
	
	
	public static String getImageUri(Context inContext, Bitmap inImage) {
	    Uri uri = Uri.parse(getImagePath(inContext, inImage));
	    return getRealPathFromURI(inContext, uri);
	}
	

	public static String getRealPathFromURI(Context context, Uri uri) {
		Cursor cursor = null;
	    try {
	        String[] proj = { MediaStore.Images.Media.DATA };
	        cursor = context.getContentResolver().query(uri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
	    } finally {
	        if (cursor != null) {
	            cursor.close();
	        }
	    }
	}

	public static File getAppCacheDir(Context context) {
		return new File(String.format(SDCARD_FOLDER_CACHE,
				context.getPackageName()));
	}

	public static boolean hasImageCaptureBug() {
		/* list of known devices with image capturing bug. */
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");
		devices.add("SEMC/X10i_1232-9897/X10i");

		return devices.contains(android.os.Build.BRAND + "/"
				+ android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);
	}

	public static String getPath(Context context, Uri uri)
			throws NullPointerException {
		final String[] projection = { MediaColumns.DATA };
		final Cursor cursor = context.getContentResolver().query(uri,
				projection, null, null, null);
		final int column_index = cursor
				.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static String getVideoPath(Context context, Uri videoUri) {
		String result = null;
		final String[] VIDEOTHUMBNAIL_TABLE = new String[] {

		Video.Media._ID, // 0
				Video.Media.DATA, // 1 from android.provider.MediaStore.Video

		};
		// Uri videoUri = MediaStore.Video.Thumbnails.getContentUri("external");

		Cursor c = context.getContentResolver().query(videoUri,
				VIDEOTHUMBNAIL_TABLE, Video.Thumbnails.VIDEO_ID, null, null);

		if ((c != null) && c.moveToFirst()) {
			result = c.getString(1);
		}
		return result;
	}

	public final static String asUpperCaseFirstChar(final String target) {

		if ((target == null) || (target.length() == 0)) {
			return target;
		}
		return Character.toUpperCase(target.charAt(0))
				+ (target.length() > 1 ? target.substring(1) : "");
	}

	
	public static ArrayList<AssetModel> getPhotoCollection(
			List<MediaResultModel> resultList) {
		final ArrayList<AssetModel> collection = new ArrayList<AssetModel>();
		for (MediaResultModel result : resultList) {
			final AssetModel model = new AssetModel();
			model.setThumbnail(Uri.fromFile(new File(result.getThumbnail())).toString());
			model.setUrl(result.getImageUrl());
			model.setVideoUrl(result.getVideoUrl());
			model.setType(result.getMediaType().name().toLowerCase());
			collection.add(model);
		}
		return collection;
	}

	public static AssetModel getMediaModel(String path, MediaType type) {
		final AssetModel model = new AssetModel();
		path = Uri.fromFile(new File(path)).toString();
		model.setThumbnail(path);
		model.setUrl(path);
		model.setType(type.name().toLowerCase());
		return model;
	}

	public static String convertMediaUriToPath(Context context, Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(uri, proj, null,
				null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}

	public static AccountBaseModel filterFiles(
			AccountBaseModel accountBaseModel, boolean supportImages,
			boolean supportVideos) {
		AccountBaseModel model = new AccountBaseModel();
		List<AccountAlbumModel> folders = accountBaseModel.getFolders();
		List<AccountMediaModel> files = new ArrayList<AccountMediaModel>();
		List<AccountMediaModel> videos = new ArrayList<AccountMediaModel>();
		List<AccountMediaModel> images = new ArrayList<AccountMediaModel>();
		if (accountBaseModel.getFiles() != null) {
			for (AccountMediaModel file : accountBaseModel.getFiles()) {
				if (file.getVideoUrl() != null && supportVideos == true) {
					videos.add(file);
				}
				if (file.getVideoUrl() == null && supportImages == true) {
					images.add(file);
				}
			}
		}
		files.addAll(images);
		files.addAll(videos);
		model.setFiles(files);
		model.setFolders(folders);
		return model;
	}
	
	
	
}
