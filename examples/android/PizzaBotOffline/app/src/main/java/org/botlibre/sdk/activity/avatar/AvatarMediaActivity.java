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

package org.botlibre.sdk.activity.avatar;

import org.botlibre.sdk.activity.EmotionalState;
import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.activity.actions.HttpSaveAvatarMediaAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Activity for editing an avatar media.
 */
public class AvatarMediaActivity extends LibreActivity {
	protected VideoView videoView;
	
	public MediaPlayer audioPlayer;
	public String currentAudio;

	public boolean videoError;
	public AvatarMedia media;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_avatar_media);

		AvatarConfig instance = (AvatarConfig)MainActivity.instance;
		this.media = (AvatarMedia)MainActivity.avatarMedia;

		setTitle(Utils.stripTags(instance.name));
		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(instance.name));
        HttpGetImageAction.fetchImage(this, instance.avatar, findViewById(R.id.icon));

		((TextView) findViewById(R.id.nameText)).setText(this.media.name);
		((TextView) findViewById(R.id.emotionsText)).setText(this.media.emotions);
		((TextView) findViewById(R.id.actionsText)).setText(this.media.actions);
		((TextView) findViewById(R.id.posesText)).setText(this.media.poses);
		((CheckBox) findViewById(R.id.talkingCheckBox)).setChecked(this.media.talking);
		((CheckBox) findViewById(R.id.hdCheckBox)).setChecked(this.media.hd);
		
		videoView = (VideoView)findViewById(R.id.videoView);
		resetVideoErrorListener();
		videoError = false;
		
		findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				play();
			}
		});
		
		findViewById(R.id.videoLayout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				play();
			}
		});
		
		String[] values = new String[EmotionalState.values().length];
		for (int index = 0; index < EmotionalState.values().length; index++) {
			values[index] = EmotionalState.values()[index].name().toLowerCase();
		}
		final AutoCompleteTextView emotionsText = (AutoCompleteTextView)findViewById(R.id.emotionsText);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.select_dialog_item, values);
        emotionsText.setThreshold(0);
        emotionsText.setAdapter(adapter);
        emotionsText.setOnTouchListener(new View.OnTouchListener() {
	    	   @Override
	    	   public boolean onTouch(View v, MotionEvent event) {
	    		   emotionsText.showDropDown();
	    		   return false;
	    	   }
	    	});
        
		final AutoCompleteTextView actionsText = (AutoCompleteTextView)findViewById(R.id.actionsText);
        adapter = new ArrayAdapter(this,
                android.R.layout.select_dialog_item, new String[]{
                	"smile",
                	"frown",
                	"laugh",
                	"scream",
                	"sit",
                	"jump",
                	"bow",
                	"nod",
                	"shake-head",
                	"slap",
        			"kiss",
        			"burp",
        			"fart"
			    
        });
        actionsText.setThreshold(0);
        actionsText.setAdapter(adapter);
        actionsText.setOnTouchListener(new View.OnTouchListener() {
	    	   @Override
	    	   public boolean onTouch(View v, MotionEvent event) {
	    		   actionsText.showDropDown();
	    		   return false;
	    	   }
	    	});
		
		final AutoCompleteTextView posesText = (AutoCompleteTextView)findViewById(R.id.posesText);
        adapter = new ArrayAdapter(this,
                android.R.layout.select_dialog_item, new String[]{
                	"sitting",
                	"lying",
                	"walking",
                	"running",
                	"jumping",
                	"fighting",
                	"sleeping",
                	"dancing"
			    
        });
        posesText.setThreshold(0);
        posesText.setAdapter(adapter);
        posesText.setOnTouchListener(new View.OnTouchListener() {
	    	   @Override
	    	   public boolean onTouch(View v, MotionEvent event) {
	    		   posesText.showDropDown();
	    		   return false;
	    	   }
	    	});

		HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.imageView));
		play();
	}

	public void resetVideoErrorListener() {
		videoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.wtf("Video error", "what:" + what + " extra:" + extra);
				videoError = true;
				return true;
			}
		});
	}
	
	public void play() {
		if (this.media.isVideo()) {
			findViewById(R.id.imageView).setVisibility(View.GONE);
			findViewById(R.id.videoLayout).setVisibility(View.VISIBLE);
			playVideo(this.media.media, false);
		} else if (this.media.isAudio()) {
			((ImageView)findViewById(R.id.imageView)).setImageResource(R.drawable.audio);
			findViewById(R.id.videoLayout).setVisibility(View.GONE);
			playAudio(this.media.media, false, true, true);
		} else {
			findViewById(R.id.videoLayout).setVisibility(View.GONE);
			HttpGetImageAction.fetchImage(this, this.media.media, (ImageView)findViewById(R.id.imageView));
		}
	}
 
	@Override
	public void onDestroy() {	
		if (this.audioPlayer != null) {
			try {
				this.audioPlayer.stop();
			} catch (Exception ignore) {}
			try {
				this.audioPlayer.release();
			} catch (Exception ignore) {}
		}
		super.onDestroy();
	}
	
	public void playVideo(String video, boolean loop) {
		if (loop) {
			videoView.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
				}
			});
		}
		try {
			Uri videoUri = HttpGetVideoAction.fetchVideo(this, video);
			if (videoUri == null) {
				videoUri = Uri.parse(MainActivity.connection.fetchImage(video).toURI().toString());
			}
			videoView.setVideoURI(videoUri);
			videoView.start();
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}
	
	public MediaPlayer playAudio(String audio, boolean loop, boolean cache, boolean start) {
		try {
			Uri audioUri = null;
			if (cache) {
				audioUri = HttpGetVideoAction.fetchVideo(this, audio);
			}
			if (audioUri == null) {
				audioUri = Uri.parse(MainActivity.connection.fetchImage(audio).toURI().toString());
			}
			final MediaPlayer audioPlayer = new MediaPlayer();
			audioPlayer.setDataSource(getApplicationContext(), audioUri);
			audioPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.wtf("Audio error", "what:" + what + " extra:" + extra);
					audioPlayer.stop();
					audioPlayer.release();
					return true;
				}
			});
			audioPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					audioPlayer.release();
				}
			});
			audioPlayer.prepare();
			audioPlayer.setLooping(loop);
			if (start) {
				audioPlayer.start();
			}
			return audioPlayer;
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
			return null;
		}
	}
    
    public void saveMedia(View view) {
        AvatarMedia config = new AvatarMedia();
        config.mediaId = this.media.mediaId;
        config.type = this.media.type;
        config.instance = MainActivity.instance.id;
        
    	EditText text = (EditText) findViewById(R.id.nameText);
    	config.name = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.emotionsText);
    	config.emotions = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.actionsText);
    	config.actions = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.posesText);
    	config.poses = text.getText().toString().trim();
    	CheckBox check = ((CheckBox) findViewById(R.id.talkingCheckBox));
    	config.talking = check.isChecked();
    	check = ((CheckBox) findViewById(R.id.hdCheckBox));
    	config.hd = check.isChecked();
        
        HttpAction action = new HttpSaveAvatarMediaAction(AvatarMediaActivity.this, config);
    	action.execute();
    }

	public VideoView getVideoView() {
		return videoView;
	}

	public void setVideoView(VideoView videoView) {
		this.videoView = videoView;
	}

	public MediaPlayer getAudioPlayer() {
		return audioPlayer;
	}

	public void setAudioPlayer(MediaPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	public String getCurrentAudio() {
		return currentAudio;
	}

	public void setCurrentAudio(String currentAudio) {
		this.currentAudio = currentAudio;
	}
}
