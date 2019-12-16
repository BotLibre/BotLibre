/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.avatar.AvatarTestActivity;
import org.botlibre.sdk.config.AvatarMessage;
import org.botlibre.sdk.config.ChatResponse;

import org.botlibre.offline.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

public class HttpAvatarMessageAction extends HttpAction {
	AvatarMessage config;
	ChatResponse response;

	public HttpAvatarMessageAction(Activity activity, AvatarMessage config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.response = MainActivity.connection.avatarMessage(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	protected void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
		try {
			final AvatarTestActivity activity = (AvatarTestActivity)this.activity;

			ImageView imageView = (ImageView)activity.findViewById(R.id.imageView);
			final VideoView videoView = (VideoView)activity.findViewById(R.id.videoView);
			View videoLayout = activity.findViewById(R.id.videoLayout);
			
			if (MainActivity.sound && this.response.avatarActionAudio != null && this.response.avatarActionAudio.length() > 0) {
				// Action audio
				activity.playAudio(this.response.avatarActionAudio, false, true, true);
			}
			if (MainActivity.sound && this.response.avatarAudio != null && this.response.avatarAudio.length() > 0) {
				// Background audio
				if (!this.response.avatarAudio.equals(activity.currentAudio)) {
					if (activity.audioPlayer != null) {
						activity.audioPlayer.stop();
						activity.audioPlayer.release();
					}
					activity.audioPlayer = activity.playAudio(this.response.avatarAudio, true, true, true);
				}
			} else if (activity.audioPlayer != null) {
				activity.audioPlayer.stop();
				activity.audioPlayer.release();
				activity.audioPlayer = null;
			}
			
			if (!MainActivity.disableVideo && !activity.videoError && this.response.isVideo()) {
				// Video avatar
				if (imageView.getVisibility() != View.GONE || videoLayout.getVisibility() != View.GONE) {
					if (imageView.getVisibility() == View.VISIBLE) {
						imageView.setVisibility(View.GONE);
					}
					if (videoLayout.getVisibility() == View.GONE) {
						videoLayout.setVisibility(View.VISIBLE);
					}
				}
				if (this.response.avatarAction != null && this.response.avatarAction.length() > 0) {
					// Action video
					videoView.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							mp.setLooping(false);
						}
					});
					videoView.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							activity.resetVideoErrorListener();
							videoView.setOnCompletionListener(null);
							activity.playVideo(response.avatar, true);
							activity.response(response);
						}
					});
					videoView.setOnErrorListener(new OnErrorListener() {
						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							activity.resetVideoErrorListener();
							activity.playVideo(response.avatar, true);
							activity.response(response);
							return true;
						}
					});
					activity.playVideo(this.response.avatarAction, false);
					return;
				} else {
					activity.playVideo(this.response.avatar, true);
				}
			} else {
				// Image avatar
				if (imageView.getVisibility() != View.GONE || videoLayout.getVisibility() != View.GONE) {
					if (imageView.getVisibility() == View.GONE) {
						imageView.setVisibility(View.VISIBLE);
					}
					if (videoLayout.getVisibility() == View.VISIBLE) {
						videoLayout.setVisibility(View.GONE);
					}
				}
				if (response.isVideo()) {
					HttpGetImageAction.fetchImage(this.activity, MainActivity.instance.avatar, imageView);
				} else {
					HttpGetImageAction.fetchImage(this.activity, this.response.avatar, imageView);					
				}
			}

			activity.response(this.response);
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
}