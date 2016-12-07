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

package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.botlibre.sdk.BOTlibreCredentials;
import org.botlibre.sdk.Credentials;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpConnectAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpFetchOrCreateAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetTemplatesAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.DomainConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.ForumPostConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.LearningConfig;
import org.botlibre.sdk.config.ResponseConfig;
import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.ScriptSourceConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.sdk.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;

/**
 * Main view, allows connect, browse and content creation.
 * Also stores a lot of shared data, such as the connection, current user/instance/conversation.
 * You do not need to use the MainActivity in your app, but it needs to be there so the other activities can access the shared data.
 * You can reuse any of the activities in your own app, such as just the Chat, Forum, LiveChat, or user management activities.
 * You can customize the code and layouts any way you wish for your own app, or just use the SDKConnection, or LiveChatConnection API.
 * <p>
 * You can create an app to access a single bot, forum, or channel instance using this MainActivity class.
 * You will need to create your bot, forum, or channel using your service provider website, or mobile app (Bot Libre, Bot Libre for Business, Forums Libre, Live Chat Libre).
 * <p>
 * You only need to set the applicationId, launchType, and launchInstanceId or launchInstanceName.
 * You will also want to replace the logo.png in res/drawable and update the application name and version in the AndroidManifest,
 * then you can package the app into your own apk file and upload it to Google Play or any other site.
 * The app is yours, you can charge for it, or give it away for free, just ensure you keep the above license in the code.
 */
public class MainActivity extends LibreActivity {
	public static final boolean DEBUG = false;
	public static final boolean ADULT = false;

	/**
	 * Enter your application ID here.
	 * You can get an application ID from any of the services websites (Bot Libre, Bot Libre for Business, Forums Libre, Live Chat Libre)
	 */
	public static String applicationId = "enter your application id here";

	/**
	 * Configure your connection credentials here.
	 * Choose which service provider you wish to connect to.
	 */
	public static SDKConnection connection;

	/**
	 * To launch into a specific domain, enter the domain id here.
	 */
	public static String domainId = null;
	public static DomainConfig domain;
	public static String defaultType = "Bots";

	public static boolean showAds = true;

	/**
	 * Choose your service provider using the correct credentials.
	 */
	static {
		connection = new SDKConnection(new BOTlibreCredentials(applicationId));
		//connection = new SDKConnection(new BotLibreForBusinessCredentials(applicationId));
		//connection = new SDKConnection(new FORUMSlibreCredentials(applicationId));
		//connection = new SDKConnection(new LIVECHATlibreCredentials(applicationId));
		if (domainId != null) {
			domain = new DomainConfig();
			domain.id = domainId;
			connection.setDomain(domain);
		}
		if (DEBUG) {
			showAds = false;
			connection.setDebug(true);
		}
	}
	public static String WEBSITE = "http://santa.botlibre.com";
	public static String SERVER = "botlibre.com";

	/**
	 * If you are building a single instance app, then you can set the instance id or name here,
	 * and use this activity to launch it.
	 */
	public static String launchInstanceId = "143"; // i.e. "171"
	public static String launchInstanceName = "Santa Bot"; // i.e. "Help Bot"

	/**
	 * If you are building a single instance app, then you can set the launchType to
	 * have this activity launch the bot, forum, or channel.
	 */
	public static LaunchType launchType = LaunchType.Bot;
	public enum LaunchType {Browse, Bot, Forum, Channel}

	public static boolean sound = true;
	public static boolean disableVideo;
	public static boolean webm = true;
	public static boolean hd;
	public static boolean deviceVoice;
	public static boolean customVoice;
	public static boolean translate;

	public static WebMediumConfig instance;
	public static ForumPostConfig post;
	public static UserConfig user;
	public static UserConfig viewUser;
	public static String type = "Bots";
	public static BotModeConfig botMode = new BotModeConfig();
	public static VoiceConfig voice = new VoiceConfig();
	public static LearningConfig learning = new LearningConfig();
	public static AvatarMedia avatarMedia;
	public static ResponseConfig response;
	public static ScriptSourceConfig script;
	public static String conversation;
	public static String template = "";
	public static String currentPhotoPath;
	public static Object[] templates;
	public static Object[] tags;
	public static Object[] categories;
	public static Object[] forumTags;
	public static Object[] forumPostTags;
	public static Object[] forumCategories;
	public static Object[] channelTags;
	public static Object[] channelCategories;
	public static Object[] avatarTags;
	public static Object[] scriptTags;
	public static Object[] scriptCategories;
	public static Object[] avatarCategories;
	public static boolean showImages = true;
	public static BrowseConfig browse = null;
	public static BrowseConfig browsePosts = null;
	public static List<WebMediumConfig> instances = new ArrayList<WebMediumConfig>();
	public static List<ForumPostConfig> posts = new ArrayList<ForumPostConfig>();
	public static List<AvatarMedia> avatarMedias = new ArrayList<AvatarMedia>();
	public static MainActivity current;
	public static boolean browsing;
	public static boolean searching;
	public static boolean searchingPosts;
	public static boolean wasDelete;
	public static boolean importingBotScript = false;
	public static boolean importingBotLog = false;
	public static String[] languages = new String[]{
			"",
			"af - Afrikaans", "sq - Albanian", "ar - Arabic", "hy - Armenian", "az - Azerbaijani",
			"ba - Bashkir", "eu - Basque", "be - Belarusian", "bn - Bengali", "bs - Bosnian", "bg - Bulgarian",
			"ca - Catalan", "zh - Chinese", "hr - Croatian", "cs - Czech", "da - Danish", "nl - Dutch", "en - English", "et - Estonian", "fi - Finnish", "fr - French",
			"gl - Galician", "ka - Georgian", "de - German", "el - Greek", "gu - Gujarati", "ht - Haitian", "he - Hebrew", "hi - Hindi", "hu - Hungarian",
			"is - Icelandic", "id - Indonesian", "ga - Irish", "it - Italian", "ja - Japanese", "kn - Kannada", "kk - Kazakh", "ky - Kirghiz", "ko - Korean",
			"la - Latin", "lv - Latvian", "lt - Lithuanian", "mk - Macedonian", "mg - Malagasy", "ms - Malay", "mt - Maltese", "mn - Mongolian", "no - Norwegian",
			"fa - Persian", "pl - Polish", "pt - Portuguese", "pa - Punjabi", "ro - Romanian", "ru - Russian",
			"sr - Serbian", "si - Sinhalese", "sk - Slovak", "sl - Slovenian", "es - Spanish", "sw - Swahili", "sv - Swedish",
			"tl - Tagalog", "tg - Tajik", "ta - Tamil", "tt - Tatar", "th - Thai", "tr - Turkish",
			"udm - Udmurt", "uk - Ukrainian", "ur - Urdu", "uz - Uzbek", "vi - Vietnamese", "cy - Welsh"
	};
	public static String[] servers = new String[]{"www.botlibre.com", "twitter.botlibre.com", "www.paphuslivechat.com", "www.livechatlibre.com", "www.forumslibre.com"};
	public static String[] types = new String[]{"Bots", "Avatars", "Scripts", "Forums", "Live Chat", "Domains", "Chat Bot Wars"};
	public static String[] channelTypes = new String[]{"ChatRoom", "OneOnOne"};
	public static String[] accessModes = new String[]{"Everyone", "Users", "Members", "Administrators"};
	public static String[] mediaAccessModes = new String[]{"Everyone", "Users", "Members", "Administrators", "Disabled"};
	public static String[] learningModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	public static String[] correctionModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	public static String[] scriptLanguages = new String[]{"Self", "AIML", "Response List", "Chat Log", "CVS"};
	public static String[] botModes = new String[]{"ListenOnly", "AnswerOnly", "AnswerAndListen"};
	public static String[] responseTypes = new String[]{"conversations", "responses", "greetings", "default", "flagged"};
	public static String[] durations = new String[]{"all", "day", "week", "month"};
	public static String[] inputTypes = new String[]{"all", "chat", "tweet", "post", "directmessage", "email"};
	public static String[] responseRestrictions = new String[]{"", "exact", "keyword", "required", "topic", "label", "previous",
			"repeat", "missing-keyword", "missing-required", "missing-topic", "patterns", "templates", "flagged", "corrections",
			"emotions", "actions", "poses"};
	public static String[] voices = new String[]{
			"cmu-slt",
			"cmu-slt-hsmm",
			"cmu-bdl-hsmm",
			"cmu-rms-hsmm",
			"dfki-prudence",
			"dfki-prudence-hsmm",
			"dfki-spike",
			"dfki-spike-hsmm",
			"dfki-obadiah",
			"dfki-obadiah-hsmm",
			"dfki-poppy",
			"dfki-poppy-hsmm",
			"bits1-hsmm",
			"bits3",
			"bits3-hsmm",
			"dfki-pavoque-neutral-hsmm",
			"camille",
			"camille-hsmm-hsmm",
			"jessica_voice-hsmm",
			"pierre-voice-hsmm",
			"enst-dennys-hsmm",
			"istc-lucia-hsmm",
			"voxforge-ru-nsh",
			"dfki-ot",
			"dfki-ot-hsmm",
			"cmu-nk",
			"cmu-nk-hsmm"
	};
	public static String[] voiceNames = new String[]{
			"English : US : Female : SLT",
			"English : US : Female : SLT (hsmm)",
			"English : US : Male : BDL (hsmm)",
			"English : US : Male : RMS (hsmm)",
			"English : GB : Female : Prudence",
			"English : GB : Female : Prudence (hsmm)",
			"English : GB : Male : Spike",
			"English : GB : Male : Spike (hsmm)",
			"English : GB : Male : Obadiah",
			"English : GB : Male : Obadiah (hsmm)",
			"English : GB : Female : Poppy",
			"English : GB : Female : Poppy (hsmm)",
			"German : DE : Female : Bits1 (hsmm)",
			"German : DE : Male : Bits3",
			"German : DE : Male : Bits3 (hsmm)",
			"German : DE : Male : Pavoque (hsmm)",
			"French : FR : Female : Camille",
			"French : FR : Female : Camille (hsmm)",
			"French : FR : Female : Jessica (hsmm)",
			"French : FR : Male : Pierre (hsmm)",
			"French : FR : Male : Dennys (hsmm)",
			"Italian : IT : Male : Lucia (hsmm)",
			"Russian : RU : Male : NSH (hsmm)",
			"Turkish : TR : Male : OT",
			"Turkish : TR : Male : OT (hsmm)",
			"Telugu : TE : Female : NK",
			"Telugu : TE : Female : NK (hsmm)"
	};

	Menu menu;

	public static String getFilePathFromURI(Context context, Uri uri) {
		String scheme = uri.getScheme();
		if (scheme.equals("file")) {
			return uri.getPath(); // uri.getLastPathSegment();
		} else if (scheme.equals("content")) {
			Cursor cursor = null;
			try {
				//String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
				//        + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
				//String i = MediaStore.Files.FileColumns.DATA;
				String[] proj = { MediaStore.Images.Media.DATA };
				cursor = context.getContentResolver().query(uri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String path = cursor.getString(column_index);
				if (path == null) {
					return uri.getPath();
				}
				return path;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		} else {
			return uri.getPath();
		}
	}

	public static String getFileNameFromPath(String path) {
		int index = path.lastIndexOf("/");
		return path.substring(index + 1, path.length());
	}

	public static String getFileTypeFromPath(String path) {
		int index = path.lastIndexOf(".");
		String ext = path.substring(index + 1, path.length());
		if (ext.equalsIgnoreCase("webm")) {
			return "video/webm";
		}
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				>= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	@SuppressWarnings("rawtypes")
	public static Class getActivity(WebMediumConfig config) {
		if (config instanceof InstanceConfig) {
			return BotActivity.class;
		}
		return null;
	}

	public static void error(String message, Exception exception, Activity activity) {
		try {
			if (DEBUG) {
				System.out.println(String.valueOf(message));
				if (exception != null) {
					exception.printStackTrace();
				}
			}
			if (message == null) {
				message = "";
			}
			if (message.contains("<html>")) {
				message = "Server Error, ensure you are connected to the Internet";
			}
			MainActivity.showMessage(message, activity);
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}

	public static void showMessage(String message, Activity activity) {
		AlertDialog dialog = new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});
		dialog.show();
	}

	public static void prompt(String message, Activity activity, EditText text, DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setView(text);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}

	public static void confirm(String message, Activity activity, DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}

	public static Object[] getAllTemplates(Activity activity) {
		if (templates == null) {
			try {
				HttpGetTemplatesAction action = new HttpGetTemplatesAction(activity);
				action.postExecute(action.execute().get());
				if (action.getException() != null) {
					templates = new String[0];
				}
			} catch (Exception ignore) {}
		}
		return templates;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		current = this;

		if (user == null) {
			SharedPreferences cookies = getPreferences(Context.MODE_PRIVATE);
			String user = cookies.getString("user", null);
			String token = cookies.getString("token", null);

			if ((user != null) && (token != null)) {
				UserConfig config = new UserConfig();
				config.user = user;
				config.token = token;
				HttpConnectAction action = new HttpConnectAction(this, config, false);
				action.execute();
			}

			String voice = cookies.getString("voice", null);
			String language = cookies.getString("language", null);
			String translate = cookies.getString("translate", null);
			String nativeVoice = cookies.getString("nativeVoice", null);

			if (voice != null) {
				MainActivity.customVoice = true;
				MainActivity.voice = new VoiceConfig();
				MainActivity.voice.voice = voice;
				MainActivity.voice.language = language;
				MainActivity.voice.nativeVoice = Boolean.valueOf(nativeVoice);
				MainActivity.deviceVoice = MainActivity.voice.nativeVoice;
			}
			if (translate != null) {
				MainActivity.translate = true;
				MainActivity.customVoice = true;
				MainActivity.voice = new VoiceConfig();
				MainActivity.voice.language = language;
				MainActivity.voice.nativeVoice = Boolean.valueOf(nativeVoice);
				MainActivity.deviceVoice = MainActivity.voice.nativeVoice;
			}
		}

		if ((launchType == LaunchType.Bot) || (launchType == LaunchType.Channel))  {
			setContentView(R.layout.activity_main_chat);
		} else if (launchType == LaunchType.Forum)  {
			setContentView(R.layout.activity_main_browse);
		} else {
			setContentView(R.layout.activity_main);
		}

		resetView();
		searching = false;


		hd = isTablet(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			ArrayAdapter adapter = new ArrayAdapter(this,
					android.R.layout.simple_spinner_dropdown_item, MainActivity.types);
			spin.setAdapter(adapter);
			spin.setSelection(Arrays.asList(MainActivity.types).indexOf(MainActivity.type));
			spin.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					resetLast();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
				}
			});
		}
		if (domain != null) {
			setTitle(Utils.stripTags(domain.name));
			HttpGetImageAction.fetchImage(this, domain.avatar, (ImageView)findViewById(R.id.splash));
		}

		if (MainActivity.user == null) {
			findViewById(R.id.viewUserButton).setVisibility(View.GONE);
			findViewById(R.id.logoutButton).setVisibility(View.GONE);
			findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.logoutButton).setVisibility(View.VISIBLE);
			findViewById(R.id.loginButton).setVisibility(View.GONE);
			findViewById(R.id.viewUserButton).setVisibility(View.VISIBLE);
			HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.viewUserButton));
		}
		resetMenu(this.menu);
		resetLast();
	}

	public void resetLast() {
		String type = MainActivity.defaultType;
		if (type == null) {
			type = MainActivity.defaultType;
		}
		Button button = (Button)findViewById(R.id.lastButton);
		if (button != null) {
			SharedPreferences cookies = getPreferences(Context.MODE_PRIVATE);
			String last = null;
			if (type.equals("Forums")) {
				last = cookies.getString("forum", null);
			} else if (type.equals("Live Chat")) {
				last = cookies.getString("channel", null);
			} else if (type.equals("Domains")) {
				last = cookies.getString("domain", null);
			} else if (type.equals("Avatars")) {
				last = cookies.getString("avatar", null);
			} else if (type.equals("Scripts")){
				last = cookies.getString("script", null);
			} else {
				last = cookies.getString("instance", null);
			}

			if (last != null) {
				button.setText(last);
				button.setVisibility(View.VISIBLE);
			} else {
				button.setVisibility(View.GONE);
			}
		}
		
		
	}

	public void openLast(View view) {
		
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		SharedPreferences cookies = getPreferences(Context.MODE_PRIVATE);
		String last = null;
		if (type.equals("Forums")) {
			last = cookies.getString("forum", null);
		} else if (type.equals("Live Chat")) {
			last = cookies.getString("channel", null);
		} else if (type.equals("Domains")) {
			last = cookies.getString("domain", null);
		} else if (type.equals("Avatars")) {
			last = cookies.getString("avatar", null);
		} else if (type.equals("Scripts")){
			last = cookies.getString("script", null);
		} else {
			last = cookies.getString("instance", null);
		}
		
		if (last == null) {
			MainActivity.showMessage("Invalid cookie", this);
			return;
		}
		

		WebMediumConfig config = null;
		if (type.equals("Forums")) {
			config = new ForumConfig();
		} else if (type.equals("Live Chat")) {
			config = new ChannelConfig();
		} else if (type.equals("Domains")) {
			config = new DomainConfig();
		} else if (type.equals("Avatars")) {
			config = new AvatarConfig();
		} else if (type.equals("Scripts")){
			config = new ScriptConfig();
		} else {
			config = new InstanceConfig();
		}
		config.name = last;

		HttpAction action = new HttpFetchAction(this, config);
		action.execute();
	}

	@Override
	public void onResume() {
		searching = false;
		browsing = false;
		searchingPosts = false;
		importingBotLog = false;
		importingBotScript = false;
		resetView();
		if (user != null) {
			SharedPreferences.Editor cookies = getPreferences(Context.MODE_PRIVATE).edit();
			cookies.putString("user", MainActivity.user.user);
			cookies.putString("token", MainActivity.user.token);
			cookies.commit();
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu_main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		resetMenu(menu);
		return true;
	}

	public void resetMenu(Menu menu) {
		if (menu == null) {
			return;
		}
		for (int index = 0; index < menu.size(); index++) {
			menu.getItem(index).setEnabled(true);        	
		}
		if (MainActivity.user == null) {
			menu.findItem(R.id.menuSignOut).setEnabled(false);
			menu.findItem(R.id.menuViewUser).setEnabled(false);
			menu.findItem(R.id.menuEditUser).setEnabled(false);
		} else {
			menu.findItem(R.id.menuSignIn).setEnabled(false);
			menu.findItem(R.id.menuSignUp).setEnabled(false);
		}

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menuChangeLanguage:
			changeLanguage(this, null);
			return true;
		case R.id.menuSignIn:
			login();
			return true;
		case R.id.menuSignUp:
			createUser();
			return true;
		case R.id.menuSignOut:
			logout();
			return true;
		case R.id.menuEditUser:
			editUser();
			return true;
		case R.id.menuViewUser:
			viewUser();
			return true;
		case R.id.menuWebsite:
			openWebsite();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void login(View view) {
		login();
	}

	public void logout(View view) {
		logout();
	}

	public void login() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	public void logout() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		connection.disconnect();
		user = null;
		instance = null;
		conversation = null;
		post = null;
		posts = new ArrayList<ForumPostConfig>();
		instances = new ArrayList<WebMediumConfig>();
		domain = null;
		tags = null;
		categories= null;
		learning = null;
		voice = null;
		customVoice = false;
		translate = false;

		SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putString("user", null);
		editor.putString("token", null);
		editor.putString("instance", null);
		editor.putString("forum", null);
		editor.putString("channel", null);
		editor.putString("domain", null);
		editor.putString("avatar", null);
		editor.putString("voice", null);
		editor.putString("language", null);
		editor.putString("nativeVoice", null);
		editor.putString("translate", null);
		editor.commit();

		HttpGetImageAction.clearFileCache(this);

		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.layout.menu_main, popup.getMenu());
		onPrepareOptionsMenu(popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		popup.show();
	}

	public void createUser() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
	}

	public void editUser() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, EditUserActivity.class);
		startActivity(intent);
	}

	public void viewUser(View view) {
		viewUser();
	}

	public void viewUser() {
		viewUser = user;
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, ViewUserActivity.class);
		startActivity(intent);
	}

	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE));
		startActivity(intent);
	}

	/**
	 * Open the details screen.
	 */
	public void about() {
		WebMediumConfig config = null;
		if (launchType == LaunchType.Bot) {
			config = new InstanceConfig();
		} else if (launchType == LaunchType.Forum) {
			config = new ForumConfig();
		} else if (launchType == LaunchType.Channel) {
			config = new ChannelConfig();
		}
		config.id = launchInstanceId;
		config.name = launchInstanceName;

		HttpAction action = new HttpFetchAction(this, config, false);
		action.execute();    	
	}

	/**
	 * Start a chat session with the hard coded instance.
	 */
	public void launch(View view) {
		WebMediumConfig config = null;
		if (launchType == LaunchType.Bot) {
			config = new InstanceConfig();
		} else if (launchType == LaunchType.Forum) {
			config = new ForumConfig();
		} else if (launchType == LaunchType.Channel) {
			config = new ChannelConfig();
		}
		config.id = launchInstanceId;
		config.name = launchInstanceName;

		HttpAction action = new HttpFetchAction(this, config, true);
		action.execute();
	}

	/**
	 * View the user's personal instance.
	 */
	public void browseMyBot() {
		if (user == null) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("You must sign in first");
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					login();			    	  
				}
			});
			dialog.show();
			return;
		}
		WebMediumConfig config = new InstanceConfig();
		config.name = "Bot " + MainActivity.user.user;

		HttpAction action = new HttpFetchOrCreateAction(this, config, false);
		action.execute();
	}

	/**
	 * Start a chat session with the user's personal instance.
	 */
	public void launchMyBot(View view) {
		if (user == null) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("You must sign in first");
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					login();			    	  
				}
			});
			dialog.show();
			return;
		}
		WebMediumConfig config = new InstanceConfig();
		config.name = "Bot " + MainActivity.user.user;

		HttpAction action = new HttpFetchOrCreateAction(this, config, true);
		action.execute();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void changeLanguage(Activity activty, final DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activty).create();
		dialog.setMessage("Select your language");
		final Spinner spin = new Spinner(activty);
		ArrayAdapter adapter = new ArrayAdapter(activty,
				android.R.layout.simple_spinner_dropdown_item, MainActivity.languages);
		spin.setAdapter(adapter);
		int index = -1;
		if (MainActivity.voice != null && MainActivity.voice.language != null) {
			index = Arrays.asList(MainActivity.languages).indexOf(MainActivity.voice.language);
		}
		spin.setSelection(index);
		dialog.setView(spin);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String lang = (String)spin.getSelectedItem();
				if (lang.isEmpty()) {
					MainActivity.translate = false;
					MainActivity.customVoice = false;
					MainActivity.voice = new VoiceConfig();
					MainActivity.deviceVoice = false;

					SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
					cookies.putString("translate", null);
					cookies.putString("language", null);
					cookies.putString("nativeVoice", null);
					cookies.putString("voice", null);
					cookies.commit();
				} else {
					MainActivity.translate = true;
					if (MainActivity.voice == null) {
						MainActivity.voice = new VoiceConfig();
					}
					MainActivity.voice.language = lang.substring(0, 2);
					MainActivity.voice.nativeVoice = true;
					MainActivity.deviceVoice = true;
					MainActivity.customVoice = true;

					SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
					cookies.putString("translate", "true");
					cookies.putString("language", MainActivity.voice.language);
					cookies.putString("nativeVoice", String.valueOf(MainActivity.voice.nativeVoice));
					cookies.commit();
				}
				if (listener != null) {
					listener.onClick(dialog, whichButton);
				}
			}
		});
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}
}
