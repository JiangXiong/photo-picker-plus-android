package com.chute.android.photopickerplus.models;

import com.chute.android.photopickerplus.models.enums.MediaType;

public class MediaResultModel {

	private String url;
	private String thumbnail;
	private MediaType mediaType;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MediaResultModel [url=");
		builder.append(url);
		builder.append(", thumbnail=");
		builder.append(thumbnail);
		builder.append(", mediaType=");
		builder.append(mediaType);
		builder.append("]");
		return builder.toString();
	}

}
