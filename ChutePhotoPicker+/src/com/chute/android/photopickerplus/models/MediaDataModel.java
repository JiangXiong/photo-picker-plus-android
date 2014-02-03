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
package com.chute.android.photopickerplus.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link MediaDataModel} class encapsulates asset information regarding the
 * image and thumbnail URLs.
 * 
 */
public class MediaDataModel implements Parcelable {

	/**
	 * Asset's image URL.
	 */
	@JsonProperty("image_url")
	private String imageUrl;

	/**
	 * Asset's video URL.
	 */
	@JsonProperty("video_url")
	private String videoUrl;

	/**
	 * Asset's thumbnail URL.
	 */
	@JsonProperty("thumbnail")
	private String thumbnail;

	@JsonProperty("file_type")
	private String fileType;

	public MediaDataModel() {
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(imageUrl);
		dest.writeString(videoUrl);
		dest.writeString(thumbnail);
		dest.writeString(fileType);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MediaDataModel [imageUrl=");
		builder.append(imageUrl);
		builder.append(", videoUrl=");
		builder.append(videoUrl);
		builder.append(", thumbnail=");
		builder.append(thumbnail);
		builder.append(", fileType=");
		builder.append(fileType);
		builder.append("]");
		return builder.toString();
	}

	public MediaDataModel(Parcel in) {
		this();
		imageUrl = in.readString();
		videoUrl = in.readString();
		thumbnail = in.readString();
		fileType = in.readString();
	}

	public static final Parcelable.Creator<MediaDataModel> CREATOR = new Parcelable.Creator<MediaDataModel>() {

		@Override
		public MediaDataModel createFromParcel(Parcel in) {
			return new MediaDataModel(in);
		}

		@Override
		public MediaDataModel[] newArray(int size) {
			return new MediaDataModel[size];
		}

	};

}
