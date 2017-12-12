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

package org.botlibre.sdk.activity.livechat;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.botlibre.sdk.LiveChatConnection;
import org.botlibre.sdk.LiveChatListener;
import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.ViewUserActivity;
import org.botlibre.sdk.activity.ChatActivity.WebAppInterface;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelFileAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelImageAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetLiveChatUsersAction;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.MediaConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.TextStream;
import org.botlibre.sdk.util.Utils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import org.botlibre.sdk.R;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * Activity for live chat and chatrooms.
 * To launch this activity from your app you can use the HttpFetchAction passing the channel id or name as a config, and launch=true.
 */
public class LiveChatActivity extends LibreActivity implements TextToSpeech.OnInitListener {
    static final int RESULT_SPEECH = 1;
    
    protected TextToSpeech tts;
    protected LiveChatConnection connection;
    protected EditText textView;
    protected boolean speak = false;
    protected boolean chime = true;
    protected MediaPlayer chimePlayer;
    protected String childActivity = "";
    protected long startTime;
    protected boolean closing;
    protected ChannelConfig instance;
    
    public String html = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.startTime = System.currentTimeMillis();
        
        setContentView(R.layout.activity_livechat);

		this.instance = (ChannelConfig)MainActivity.instance;
		/*if (this.instance.showAds && (MainActivity.user == null || MainActivity.user.type == null || MainActivity.user.type.equals("Basic"))) {
	        AdView mAdView = (AdView) findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder().build();
	        mAdView.loadAd(adRequest);
		} else {
	        AdView mAdView = (AdView) findViewById(R.id.adView);
    		mAdView.setVisibility(View.GONE);
		}*/

        setTitle(Utils.stripTags(this.instance.name));
		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(this.instance.name));
        HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.icon));
                
        this.tts = new TextToSpeech(this, this);

        this.textView = (EditText) findViewById(R.id.messageText);
        this.textView.setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				submitChat();
				return false;
			}
		});

        WebView webView = (WebView) findViewById(R.id.logText);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	try {
            		view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            	} catch (Exception failed) {
            		return false;
            	}
                return true;
            }
        });

		ImageButton  button = (ImageButton) findViewById(R.id.speakButton);
		button.setOnClickListener(new View.OnClickListener() { 
            @Override
            public void onClick(View v) { 
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
 
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    textView.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

		final ListView list = (ListView) findViewById(R.id.usersList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
		   public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
				UserConfig userConfig = (UserConfig)list.getItemAtPosition(position);
				if (userConfig != null) {
					setText(userConfig.user + ": ");
				}
			}
		});
		
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					View imageLayout = findViewById(R.id.usersList);
					if (imageLayout.getVisibility() == View.VISIBLE) {
						imageLayout.setVisibility(View.GONE);
					} else {
						imageLayout.setVisibility(View.VISIBLE);					
					}
					return true;
				}
				return false;
			}
		};
		final GestureDetector detector = new GestureDetector(this, listener);
		findViewById(R.id.logText).setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});

		try {
			this.connection = new LiveChatConnection(MainActivity.connection.getCredentials(),
					new LiveChatListener() {						
						@Override
						public void message(String message) {
							response(message);							
						}
						
						@Override
						public void info(String message) {
							response(message);							
						}
						
						@Override
						public void error(String message) {
							response(message);							
						}
					    
					    public void updateUsers(String csv) {
							HttpAction action = new HttpGetLiveChatUsersAction(LiveChatActivity.this, csv);
							action.execute();
							return;
					    }
						
						@Override
						public void closed() {	}		
					});
			this.connection.connect((ChannelConfig)this.instance, MainActivity.user);
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
		}
		
		WebView responseView = (WebView) findViewById(R.id.logText);
		responseView.getSettings().setJavaScriptEnabled(true);
		responseView.getSettings().setDomStorageEnabled(true);
		responseView.addJavascriptInterface(new WebAppInterface(this), "Android");
	}
	
	@Override
	public void onResume() {
		if ((MainActivity.instance instanceof ChannelConfig) && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = (ChannelConfig)MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		super.onResume();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
		if (this.childActivity.equals("")) {
	        if (requestCode == RESULT_SPEECH) {
	            if (resultCode == RESULT_OK && null != data) {
	 
	                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	                if (text != null) {
	                	textView.setText(text.get(0));
	                	submitChat();
	                }
	            }
	            return;
	        }
	        return;
		}
        
	    if (resultCode != RESULT_OK) {
			return;
		}
	    if (data == null || data.getData() == null) {
			this.childActivity = "";
			return;
		}
	    
		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			MediaConfig config = new MediaConfig();
			config.name = MainActivity.getFileNameFromPath(file);
			config.type = MainActivity.getFileTypeFromPath(file);
			config.instance = this.instance.id;
			if (this.childActivity.equals("sendImage")) {
				HttpAction action = new HttpCreateChannelImageAttachmentAction(this, file, config);
				action.execute().get();
			} else {
				HttpAction action = new HttpCreateChannelFileAttachmentAction(this, file, config);
				action.execute().get();				
			}
			this.childActivity = "";
		} catch (Exception exception) {
			this.childActivity = "";
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
    }

	public void submitChat() {
		EditText v = (EditText) findViewById(R.id.messageText);
		String input = v.getText().toString().trim();
		if (input.equals("")) {
			return;
		}
		final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
		scroll.postDelayed(new Runnable() {
		    public void run() {
		    	scroll.fullScroll(View.FOCUS_DOWN);
		    }
		}, 200);
		scroll.postDelayed(new Runnable() {
		    public void run() {
		    	scroll.fullScroll(View.FOCUS_DOWN);
		    }
		}, 400);
		
		
		this.connection.sendMessage(input);
		
		v.setText("");
	}

	public void setText(String text) {
		EditText edit = (EditText) findViewById(R.id.messageText);
		edit.setText(text);
	}

	public void sendMessage(String text) {
		this.connection.sendMessage(text);
	}

	public void sendFile() {
		Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
		upload.setType("*/*");
		this.childActivity = "sendFile";
		try {
			startActivityForResult(upload, 1);
		} catch (Exception notFound) {
			this.childActivity = "sendFile";
			upload = new Intent(Intent.ACTION_GET_CONTENT);
			upload.setType("file/*");
			startActivityForResult(upload, 1);
		}
	}

	public void sendImage() {
		Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
		upload.setType("image/*");
		this.childActivity = "sendImage";
		startActivityForResult(upload, 1);
	}
	
	public void sendFile(MediaConfig media) {
		String message = "file: " + media.name + " : " + media.type + " : http://"
					+ MainActivity.connection.getCredentials().host + MainActivity.connection.getCredentials().app + "/" + media.file;
		setText(message);
		this.connection.sendMessage(message);
		setText("");
	}
	
	public void whisper(UserConfig user) {
		if (user == null) {
			setText("whisper: ");
		} else {
			setText("whisper: " + user.user + ": ");			
		}
	}

	public void boot(UserConfig user) {
		if (user == null) {
			setText("boot: ");
		} else {
			setText("boot: " + user.user);
			this.connection.boot(user.user);
		}
		setText("");
	}

	public void pvt(UserConfig user) {
		if (user == null) {
			setText("private: ");
		} else {
			setText("private: " + user.user);
			this.connection.pvt(user.user);
		}
		setText("");
	}
    
    /**
     * Clear the log.
     */
    public void clear(View view) {
    	WebView log = (WebView) findViewById(R.id.logText);
		log.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
    }

	public void menu(View view) {
		openOptionsMenu();
	}

	public void sendFile(View view) {
		sendFile();
	}

	public void sendImage(View view) {
		sendImage();
	}

	public void exit(View view) {
    	this.connection.exit();
	}

	public void accept(View view) {
    	this.connection.accept();
	}

	public void chime(View view) {
    	this.chime = !this.chime;
    	Button button = (Button) findViewById(R.id.chimeButton);
    	if (this.chime) {
    		button.setBackgroundResource(R.drawable.sound);
    	} else {
    		button.setBackgroundResource(R.drawable.mute);    		
    	}
	}

	public void speech(View view) {
    	this.speak = !this.speak;
    	Button button = (Button) findViewById(R.id.speechButton);
    	if (this.speak) {
    		button.setBackgroundResource(R.drawable.voice);
    	} else {
    		button.setBackgroundResource(R.drawable.voiceoff);    		
    	}
	}
 
    @Override
    public void onDestroy() {
    	this.closing = true;
		if (this.connection != null) {
			try {
				this.connection.disconnect();
			} catch (Exception exception) { }
		}
		
        if (this.tts != null) {
        	this.tts.stop();
        	this.tts.shutdown();
        }
		if (this.chimePlayer != null) {
			this.chimePlayer.stop();
			this.chimePlayer.release();
		}
        super.onDestroy();
    }
 
    @Override
    public void onInit(int status) {
 
        if (status == TextToSpeech.SUCCESS) {
 
        	Locale locale = null;
        	VoiceConfig voice = MainActivity.voice;
        	if (voice != null && voice.language != null && voice.language.length() > 0) {
        		locale = new Locale(MainActivity.voice.language);
        	} else {
        		locale = Locale.US;
        	}
            int result = this.tts.setLanguage(locale);
            
			float pitch = 1;
			if (voice != null && voice.pitch != null && voice.pitch.length() > 0) {
				try {
					pitch = Float.valueOf(voice.pitch);
				} catch (Exception exception) {}
			}
			float speechRate = 1;
			if (voice != null && voice.speechRate != null && voice.speechRate.length() > 0) {
				try {
					speechRate = Float.valueOf(voice.speechRate);
				} catch (Exception exception) {}
			}
        	this.tts.setPitch(pitch);
        	this.tts.setSpeechRate(speechRate);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
 
    }
 
    public void chime() {
    	if (this.chimePlayer == null) {
    		this.chimePlayer = MediaPlayer.create(this, R.raw.chime);
    	}
    	this.chimePlayer.start();
    }

	public void viewUser(UserConfig user) {
		if (user == null) {
        	MainActivity.showMessage("Select user", this);
			return;
		}
		MainActivity.viewUser = user;
		
        Intent intent = new Intent(this, ViewUserActivity.class);
        startActivity(intent);
	}
    
    public void toggleKeepAlive() {
    	this.connection.setKeepAlive(!this.connection.isKeepAlive());
    }
    
    public void clearLog() {
    	this.html = "";

    	WebView log = (WebView) findViewById(R.id.logText);
		log.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
    }
    
    public void response(String text) {
    	if (this.closing) {
    		return;
    	}
    	String user = "";
    	String message = text;
    	int index = text.indexOf(':');
    	if (index != -1) {
    		user = text.substring(0, index);
    		message = text.substring(index + 2, text.length());
    	}
		if (user.equals("Online")) {
			HttpAction action = new HttpGetLiveChatUsersAction(this, message);
			action.execute();
			return;
		}
		if (user.equals("Media")) {
			return;
		}
		if (user.equals("Channel")) {
			return;
		}

		WebView log = (WebView) findViewById(R.id.logText);
		
		message = Utils.linkHTML(message);
		if (message.contains("<") && message.contains(">")) {
			message = linkPostbacks(message);
		}
		this.html = this.html + "<b>" + user + "</b> <span style='font-size:10px;color:grey'>"
					+ Utils.displayTime(new Date()) + "</span><br/>"
					+ Utils.linkHTML(message) + "<br/>";
		log.loadDataWithBaseURL(null, this.html, "text/html", "utf-8", null);
		
		final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
		scroll.postDelayed(new Runnable() {
		    public void run() {
		    	scroll.fullScroll(View.FOCUS_DOWN);
		    }
		}, 200);
		scroll.postDelayed(new Runnable() {
		    public void run() {
		    	scroll.fullScroll(View.FOCUS_DOWN);
		    }
		}, 400);
		
		if ((System.currentTimeMillis() - startTime) < (1000 * 5)) {
	    	return;
		}
				
		boolean speak = this.speak;
		boolean chime = this.chime;
		if (user.equals("Error") || user.equals("Info")) {
			speak = false;
		} else if (MainActivity.user == null) {
			if (user.startsWith("anonymous")) {
				speak = false;
				chime = false;
			}
		} else {
			if (user.equals(MainActivity.user.user)) {
				speak = false;
				chime = false;
			}			
		}
		if (speak) {
			this.tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		} else if (chime) {
			if (this.chimePlayer != null && this.chimePlayer.isPlaying()) {
				return;
			}
			chime();
		}
    }
    
    public void setUsers(List<UserConfig> users) {
		ListView list = (ListView) findViewById(R.id.usersList);
		list.setAdapter(new UserListAdapter(this, R.id.usersList, users));
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_livechat, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        WebMediumConfig instance = this.instance;
        
        boolean isAdmin = (MainActivity.user != null) && instance.isAdmin;
        if (!isAdmin) {
        	menu.findItem(R.id.menuBoot).setEnabled(false);
        }
        if (this.speak) {
    	    menu.findItem(R.id.menuSpeak).setChecked(true);        	
        }
	    return true;
	}
	/**
	 * Add JavaScript to the HTML to raise postback events to send messages to the bot.
	 */
	public String linkPostbacks(String html) {
		
		if (html.contains("button")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("<button", true));
				if (!stream.atEnd()) {
					String element = stream.upTo('>', true);
					String button = stream.upTo('<', false);
					writer.write(" onclick=\"Android.postback('" + button + "')\" ");
					writer.write(element);
					writer.write(button);
				}
			}
			html = writer.toString();
		}
		if (html.contains("chat:")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("href=\"", true));
				if (stream.atEnd()) {
					break;
				}
				String protocol = stream.upTo(':', true);
				if (!protocol.equals("chat:")) {
					writer.write(protocol);
					continue;
				}
				String chat = stream.upTo('"', false);
				writer.write("#\"");
				writer.write(" onclick=\"Android.postback('" + chat + "')\" ");
			}
			html = writer.toString();
		}
		if (html.contains("select")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("<select", true));
				if (!stream.atEnd()) {
					writer.write(" onchange=\"Android.postback(this.value)\" ");
				}
			}
			html = writer.toString();
		}
		return html;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		ListView list = (ListView) findViewById(R.id.usersList);
		UserConfig user = (UserConfig)list.getItemAtPosition(list.getCheckedItemPosition());
        switch (item.getItemId())
        {
	    case R.id.menuSendImage:
	    	sendImage();
	        return true;
	    case R.id.menuSendFile:
	    	sendFile();
	        return true;
	    case R.id.menuPing:
	    	this.connection.ping();
	        return true;
        case R.id.menuWhisper:
        	whisper(user);
            return true;
        case R.id.menuPrivate:
        	pvt(user);
            return true;
        case R.id.menuAccept:
	    	this.connection.accept();
            return true;
        case R.id.menuBoot:
        	boot(user);
            return true;
        case R.id.menuClear:
        	clearLog();
            return true;
        case R.id.menuExit:
	    	this.connection.exit();
            return true;
        case R.id.menuViewUser:
	    	viewUser(user);
            return true;
        case R.id.menuSpeak:
        	this.speak = !this.speak;
        	item.setChecked(this.speak);
            return true;
        case R.id.menuChime:
        	this.chime = !this.chime;
        	item.setChecked(this.chime);
            return true;
        case R.id.menuKeepAlive:
        	toggleKeepAlive();
        	item.setChecked(this.connection.isKeepAlive());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    public class WebAppInterface {
	    Context context;

	    WebAppInterface(Context context) {
	    	this.context = context;
	    }
	    
	    
	    @JavascriptInterface
	    public void postback(final String message) {
	    	try {
				final EditText messageText = (EditText) findViewById(R.id.messageText);
		    	messageText.post(new Runnable() {
					@Override
					public void run() {
						try {
					    	messageText.setText(message);
					    	submitChat();
				    	} catch (Throwable error) {
				    		error.printStackTrace();
				    	}
					}
				});
	    	} catch (Throwable error) {
	    		error.printStackTrace();
	    	}
	    }
	}
}
