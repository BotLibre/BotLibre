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

package org.botlibre.sdk.activity.war;

import java.util.HashMap;

import org.botlibre.sdk.activity.ChatActivity;
import org.botlibre.sdk.activity.ChatListAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChatAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.WarAction;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Activity for chat bot wars.
 */
public class WarActivity extends ChatActivity {
	protected VideoView video1View;
	protected ImageView image1View;
	protected View video1Layout;
	protected InstanceConfig instance1;
	protected String conversation1;
	
	protected VideoView video2View;
	protected ImageView image2View;
	protected View video2Layout;
	protected InstanceConfig instance2;
	protected String conversation2;
	
	protected int currentBot = 0;
	protected int count = 0;
	
	protected boolean finished = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.superOnCreate(savedInstanceState);
		setContentView(R.layout.activity_war);

		this.instance = StartWarActivity.bot1;
		this.instance1 = StartWarActivity.bot1;
		this.instance2 = StartWarActivity.bot2;
		if (this.instance == null) {
			return;
		}
		this.count = 0;
		this.currentBot = 0;
		this.conversation1 = null;
		this.conversation2 = null;
		this.finished = false;
		/*if (MainActivity.showAds) {
	        AdView mAdView = (AdView) findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder().build();
	        mAdView.loadAd(adRequest);
		} else {
	        AdView mAdView = (AdView) findViewById(R.id.adView);
    		mAdView.setVisibility(View.GONE);
		}*/
		
		tts = new TextToSpeech(this, this);

		videoView = (VideoView)findViewById(R.id.videoView);
		video1View = videoView;
		video2View = (VideoView)findViewById(R.id.video2View);
		resetVideoErrorListener();
		resetVideoErrorListener2();
		videoError = false;
		
		imageView = (ImageView)findViewById(R.id.imageView);
		image1View = imageView;
		image2View = (ImageView)findViewById(R.id.image2View);
		videoLayout = findViewById(R.id.videoLayout);
		video1Layout = videoLayout;
		video2Layout = findViewById(R.id.video2Layout);

		if (MainActivity.translate) {
			findViewById(R.id.yandex).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.yandex).setVisibility(View.GONE);
		}
		
		ListView list = (ListView) findViewById(R.id.chatList);
		list.setAdapter(new ChatListAdapter(this, R.layout.chat_list, this.messages));
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		HttpGetImageAction.fetchImage(this, instance.avatar, imageView);
		HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.responseImageView));

		HttpGetImageAction.fetchImage(this, instance2.avatar, (ImageView)findViewById(R.id.image2View));
		
		ChatConfig config = new ChatConfig();
		config.instance = instance.id;
		config.message = StartWarActivity.topic;
		config.avatar = this.avatarId;
		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}
		config.avatarHD = MainActivity.hd;
		config.speak = !MainActivity.deviceVoice;
		HttpAction action = new HttpChatAction(WarActivity.this, config);
		action.execute();
	}

	public void resetVideoErrorListener2() {
		video2View.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.wtf("Video error", "what:" + what + " extra:" + extra);
				videoError = true;
				return true;
			}
		});
	}

	public void submitChat(String message) {
		if (this.finished) {
			return;
		}
		ChatConfig config = new ChatConfig();
		config.instance = this.instance.id;
		config.conversation = MainActivity.conversation;
		config.speak = !MainActivity.deviceVoice;
		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}
		config.avatarHD = MainActivity.hd;
		
		config.message = message;
		
		
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ListView list = (ListView) findViewById(R.id.chatList);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
			}
			
		});
		
		HttpChatAction action = new HttpChatAction(WarActivity.this, config);
		action.execute();

		HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.responseImageView));
		WebView responseView = (WebView) findViewById(R.id.responseText);
		responseView.loadDataWithBaseURL(null, "thinking...", "text/html", "utf-8", null);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_war, menu);
		return true;
	}
	
	public void resetMenu() {
		if (this.menu == null) {
			return;
		}
		this.menu.findItem(R.id.menuSound).setChecked(MainActivity.sound);
		this.menu.findItem(R.id.menuDeviceVoice).setChecked(MainActivity.deviceVoice);
		this.menu.findItem(R.id.menuDisableVideo).setChecked(MainActivity.disableVideo || this.videoError);
		this.menu.findItem(R.id.menuHD).setChecked(MainActivity.hd);
		this.menu.findItem(R.id.menuWebm).setChecked(MainActivity.webm);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
	    case R.id.menuChangeLanguage:
	    	MainActivity.changeLanguage(this, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					resetTTS();
					if (MainActivity.translate) {
						findViewById(R.id.yandex).setVisibility(View.VISIBLE);
					} else {
						findViewById(R.id.yandex).setVisibility(View.GONE);
					}
				}
			});
	        return true;
		case R.id.menuSound:
			toggleSound();
			return true;
		case R.id.menuDeviceVoice:
			toggleDeviceVoice();
			return true;
		case R.id.menuDisableVideo:
			toggleDisableVideo();
			return true;
		case R.id.menuHD:
			MainActivity.hd = !MainActivity.hd;
			return true;
		case R.id.menuWebm:
			MainActivity.webm = !MainActivity.webm;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.finished = true;
		
		switchBots();
		ChatConfig config = new ChatConfig();
		config.instance = this.instance.id;
		config.conversation = MainActivity.conversation;
		config.disconnect = true;
		
		HttpChatAction action = new HttpChatAction(this, config);
		action.execute();
	}
 
	public void response(final ChatResponse response) {
		if (this.finished) {
			return;
		}
		try {
			this.response = response;
			this.response.instance = this.instance.id;
			
			String status = "";
			if (response.emote != null && !response.emote.equals("NONE")) {
				status = status + response.emote.toLowerCase();
			}
			if (response.action != null) {
				if (!status.isEmpty()) {
					status = status + " ";
				}
				status = status + response.action;
			}
			if (response.pose != null) {
				if (!status.isEmpty()) {
					status = status + " ";
				}
				status = status + response.pose;
			}
			TextView statusView = (TextView) findViewById(R.id.statusText);
			statusView.setText(status);
			
			if (response.message == null) {
				return;
			}
			final String text = response.message;
			
			final ListView list = (ListView) findViewById(R.id.chatList);
			
			
			list.post(new Runnable() {
		        @Override
		        public void run() {
		        	messages.add(response);
		        	((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
		        	list.invalidateViews();
		        	if (list.getCount() > 2) {
			        	list.setSelection(list.getCount() - 2);
		        	}
		        	
		        }
		    });
			
			WebView responseView = (WebView) findViewById(R.id.responseText);
			responseView.loadDataWithBaseURL(null, Utils.linkHTML(text), "text/html", "utf-8", null);
			
			boolean talk = (text.trim().length() > 0) && (MainActivity.deviceVoice || (this.response.speech != null && this.response.speech.length() > 0));
			if (MainActivity.sound && talk) {
				if (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.isVideoTalk()) {
					videoView.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							try {
								mp.setLooping(true);
								if (!MainActivity.deviceVoice) {
									// Voice audio
									MediaPlayer mediaPlayer = playAudio(response.speech, false, false, false);
									mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
										@Override
										public void onCompletion(MediaPlayer mp) {
											mp.release();
											videoView.post(new Runnable() {
												public void run() {
													cycleVideo(response);
												}
											});
										}
									});
									mediaPlayer.start();
								} else {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
									tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
								}
							} catch (Exception exception) {
								Log.wtf(exception.getMessage(), exception);
							}
						}
					});
					playVideo(this.response.avatarTalk, false);
				} else if (talk) {
					if (!MainActivity.deviceVoice) {
						// Voice audio
						playAudio(this.response.speech, false, false, true);
					} else {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
						this.tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
					}
				}
			} else if (talk && (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.avatarTalk != null)) {
				videoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mp.setLooping(false);
					}
				});
				videoView.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						videoView.setOnCompletionListener(null);
						cycleVideo(response);
					}
				});
				playVideo(this.response.avatarTalk, false);
			}
		} catch (Exception exception) {
			Log.wtf(exception.getMessage(), exception);
		}
		
		if (this.count >= 20) {
			done();
			return;
		}
		this.count++;
		WarAction action = new WarAction(this, this.response);
		action.execute();
	}
	
	public void done() {
		CharSequence[] items = {this.instance1.name, this.instance2.name, "Continue war"};
		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vote for the winner");
        builder.setSingleChoiceItems(items, -1,
        		new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int item) {
			            switch(item) {
			                case 0:
			                      StartWarActivity.winner = instance1;
			                      StartWarActivity.looser = instance2;
			                      finish();
			                      break;
			                case 1:
			                      StartWarActivity.winner = instance2;
			                      StartWarActivity.looser = instance1;
			                      finish();
			                      break;
			                case 2:
			            		count = 0;
			            		WarAction action = new WarAction(WarActivity.this, response);
			            		action.execute();
			            }
			            dialog.dismiss();    
			            }
			        });
        dialog = builder.create();
        dialog.show();
	}
	
	public String getAvatarIcon(ChatResponse config) {
		if (this.avatar != null) {
			return this.avatar.avatar;
		}
		if (config == null || config.isVideo()) {
			if (config != null && config.instance == null) {
				config.instance = this.instance.id;
			}
			if (config != null && config.instance != null && config.instance.equals(this.instance2.id)) {
				return this.instance2.avatar;
			}
			return this.instance1.avatar;
		}
		return config.avatar;
	}

	public void cycleVideo(final ChatResponse response) {
		if (response.instance != null && !response.instance.equals(this.instance.id)) {
			if (response.instance.equals(this.instance1.id)) {
				this.imageView = this.image1View;
				this.videoView = this.video1View;
				this.videoLayout = this.video1Layout;
			} else {
				this.imageView = this.image2View;
				this.videoView = this.video2View;
				this.videoLayout = this.video2Layout;
			}
		}
		super.cycleVideo(response);
		if (this.instance == this.instance1 && this.imageView != this.image1View) {
			this.imageView = this.image1View;
			this.videoView = this.video1View;
			this.videoLayout = this.video1Layout;
		} else if (this.instance == this.instance2 && this.imageView != this.image2View) {
			this.imageView = this.image2View;
			this.videoView = this.video2View;
			this.videoLayout = this.video2Layout;
		}
	}
	
	public void switchBots() {
		if (this.currentBot == 0) {
			this.conversation1 = MainActivity.conversation;
			MainActivity.conversation = this.conversation2;
			this.currentBot = 1;
			this.instance = this.instance2;
			this.imageView = this.image2View;
			this.videoView = this.video2View;
			this.videoLayout = this.video2Layout;
		} else {
			this.conversation2 = MainActivity.conversation;
			MainActivity.conversation = this.conversation1;
			this.currentBot = 0;
			this.instance = this.instance1;
			this.imageView = this.image1View;
			this.videoView = this.video1View;
			this.videoLayout = this.video1Layout;
		}
	}
}
