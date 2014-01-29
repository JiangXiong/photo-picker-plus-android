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

import android.content.Context;
import android.widget.Toast;

import com.chute.android.photopickerplus.R;

/**
 * {@link NotificationUtil} contains a lot of common methods considering
 * manipulations of toast messages.
 */
public class NotificationUtil {

	private static Toast toast;

	public static void makeSingleShowToast(Context context, int message) {
		if (toast != null) {
			toast.cancel();
		}
		toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void makeToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void makeToast(Context context, int stringId) {
		Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show();
	}

	public static void makeConnectionProblemToast(Context context) {
		Toast.makeText(context, R.string.http_exception, Toast.LENGTH_SHORT)
				.show();
	}

	public static void makeExpiredSessionLogginInAgainToast(Context context) {
		Toast.makeText(context, R.string.expired_session_logging_in,
				Toast.LENGTH_SHORT).show();
	}

	public static void showPhotosAdapterToast(Context context, int count) {
		String text = context.getResources().getQuantityString(
				R.plurals.numberOfLoadedPhotos, count, count);
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void showAlbumsAdapterToast(Context context, int count) {
		String text = context.getResources().getQuantityString(
				R.plurals.numberOfLoadedAlbums, count, count);
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

}
