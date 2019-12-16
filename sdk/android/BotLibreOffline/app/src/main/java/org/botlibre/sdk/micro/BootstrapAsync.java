package org.botlibre.sdk.micro;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.micro.MicroMemory;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpUIAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.ScriptSourceConfig;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.consciousness.Consciousness;
import org.botlibre.thought.language.Comprehension;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Bootstrap the bot the first time from an online response list file or script.
 */
public class BootstrapAsync extends HttpUIAction {
	public BootstrapAsync(Activity activity) {
		super(activity);
	}

	protected String aimlScriptId;
	protected String selfScriptId;
	protected String greetingScriptId;
	InstanceConfig config;
	MicroConnection connection;
	Bot bot=null;
	boolean noInternet = false;
	
	//passing InstanceConfig and the Scripts Versions.
	public synchronized void bootstrapBot(InstanceConfig config, String aimlScriptId, String greetingScriptId) {
		this.greetingScriptId = greetingScriptId;
		this.aimlScriptId = aimlScriptId;
		this.config = config;	
		this.execute();
	}

	protected boolean isResetRequired() {
		// Check if bot memory exists.
		if (!MicroMemory.checkExists()) {
			Log.e("edebug", "bot doesn't exists!");
			return true;
		}
		try{
		// Check if version is changed.
		SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
		String aimlVersion = cookies.getString(this.aimlScriptId, null);
		String greetingVersion = cookies.getString(this.greetingScriptId, null);
		if (aimlVersion == null || aimlVersion.isEmpty() || greetingVersion == null || greetingVersion.isEmpty()) {
			Log.e("edebug", "botVersion is different");
			return true;
		}
		Log.e("isRestartRequired", "fetchScripts");
		ScriptConfig aimlScript = new ScriptConfig();
		aimlScript.id = this.aimlScriptId;
		aimlScript = MainActivity.connection.fetch(aimlScript);
		if (!aimlVersion.equals(aimlScript.version)) {
			Log.e("edebug", "ScriptVersion1 is different old: " + aimlVersion + " new: "+ aimlScript.version);
			return true;
		}
		ScriptConfig greetingScript = new ScriptConfig();
		greetingScript.id = this.greetingScriptId;
		greetingScript = MainActivity.connection.fetch(greetingScript);
		if (!greetingVersion.equals(greetingScript.version)) {
			Log.e("edebug", "ScriptVersion2 is different old: " + greetingVersion + " new: "+ greetingScript.version);
			return true;
		}
		Log.e("isRestartRequired", "fetchScripts DONE!");
		} catch (Exception ignore) {
			Log.e("ErrorDebug",ignore.getMessage());
			return false;
		}
		return false;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			MicroMemory.storageFileName = this.config.id;
			this.connection = (MicroConnection)MainActivity.connection;
			Log.e("edebug", "adding config.id storageFileName");
			// Check if bot is already loaded first.
			if (connection.getBot(this.config.id) != null) {
				return "";
			}
			// Check if bot memory exists.
			boolean resetRequired = isResetRequired();
			
			// If no reset is required, just load the bot.
			if (!resetRequired) {
				try {
					Log.e("!resetRequired", "no reset required");
					this.bot = Bot.createInstance(Bot.CONFIG_FILE, this.config.id, true);
					Language language = bot.mind().getThought(Language.class);
					language.setLearningMode(LearningMode.Disabled);
					language.setLearnGrammar(false);
					Comprehension comprehension = bot.mind().getThought(Comprehension.class);
					comprehension.setEnabled(false);
					Consciousness consciousness = bot.mind().getThought(Consciousness.class);
					consciousness.setEnabled(false);
					Sense sense = bot.awareness().getSense(Wiktionary.class);
					sense.setIsEnabled(false);
					Log.e("edebug", "done no rest required");
				} catch (Exception failed) {
					// If the load fails, then reset the bot.
					Log.e("edebug", "faild no reset required");
					Log.wtf("Load failed", failed);
					resetRequired = true;
				}
			}
			// If a reset is required the bot must be rebuilt from scratch and reimport the AIML and greeting files.
			if (resetRequired) {
				Log.e("edebug", "reset required");
				// Delete the old file if exists.
				MicroMemory.reset();
				// Fetch the scripts source code.
				ScriptConfig aimlScript = new ScriptConfig();
				aimlScript.id = this.aimlScriptId;
				ScriptSourceConfig aimlSource = connection.getScriptSource(aimlScript);
				ScriptConfig greetingScript = new ScriptConfig();
				greetingScript.id = this.greetingScriptId;
				ScriptSourceConfig greetingSource = connection.getScriptSource(greetingScript);
				
				MicroMemory.reset();
				// Create new bot.
				bot = Bot.createInstance(Bot.CONFIG_FILE, this.config.id, true);
				Language language = bot.mind().getThought(Language.class);
				language.setLearningMode(LearningMode.Disabled);
				language.setLearnGrammar(false);
				Comprehension comprehension = bot.mind().getThought(Comprehension.class);
				comprehension.setEnabled(false);
				Consciousness consciousness = bot.mind().getThought(Consciousness.class);
				consciousness.setEnabled(false);
				Sense sense = bot.awareness().getSense(Wiktionary.class);
				sense.setIsEnabled(false);
				//if bootstrapSystem was true, it will load the default script.
				// Do not need to bootstrap, as only uses AIML.
				new Bootstrap().bootstrapSystem(bot, false);
				// Load AIML file.
				language.loadAIML(aimlSource.source, "ami", true, false, false);
				// Load greeting response list.
				bot.awareness().getSense(TextEntry.class).loadChat(greetingSource.source, "Response List", false, true);

				// Shutdown will save the file.
				bot.shutdown();
				// Reload the bot from the file.
				bot = Bot.createInstance(Bot.CONFIG_FILE, this.config.id, true);
				language = bot.mind().getThought(Language.class);
				language.setLearningMode(LearningMode.Disabled);
				language.setLearnGrammar(false);
				comprehension = bot.mind().getThought(Comprehension.class);
				comprehension.setEnabled(false);
				consciousness = bot.mind().getThought(Consciousness.class);
				consciousness.setEnabled(false);
				sense = bot.awareness().getSense(Wiktionary.class);
				sense.setIsEnabled(false);
				
				SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
				cookies.putString(this.aimlScriptId, aimlSource.versionName);
				cookies.putString(this.greetingScriptId, greetingSource.versionName);
				cookies.commit();
				Log.e("edebug", "done reset required");
			}
			// Finally, add the bot to the connections active bots.
			connection.addBot(this.config.id, bot);
		} catch (Exception exception) {
			Log.wtf("Load failed", exception);
			Log.e("BootstrapAsync", exception.getMessage());
			noInternet = true;
//			MicroMemory.reset();
			return null;
		}
		return null;
	}

	/**
	 * Load, compile, and add the state machine from the Self source code.
	 */
	public void loadSelf(Bot bot, String text) {
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		SelfCompiler compiler = SelfCompiler.getCompiler();
		Vertex stateMachine = compiler.parseStateMachine(text, false, network);
		SelfCompiler.getCompiler().pin(stateMachine);
		language.addRelationship(Primitive.STATE, stateMachine);
		network.save();
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}