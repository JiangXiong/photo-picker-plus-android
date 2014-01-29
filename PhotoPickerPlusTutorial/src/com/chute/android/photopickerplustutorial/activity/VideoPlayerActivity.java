package com.chute.android.photopickerplustutorial.activity;

import com.chute.android.photopickerplustutorial.R;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends FragmentActivity {

	private VideoView videoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gc_activity_video_player);

		String videoUrl = getIntent().getExtras().getString(
				PhotoPickerPlusTutorialActivity.KEY_VIDEO_PATH);
		initVideo(videoUrl);

	}

	private void initVideo(String url) {
		videoView = (VideoView) findViewById(R.id.videoView);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		Uri uri = Uri.parse(url);
		videoView.setMediaController(mediaController);
		videoView.setVideoURI(uri);
		videoView.requestFocus();
		videoView.start();
	}

}
