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
package org.botlibre;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.botlibre.api.avatar.Avatar;
import org.botlibre.api.emotion.Emotion;
import org.botlibre.api.emotion.Mood;
import org.botlibre.api.knowledge.Memory;
import org.botlibre.api.sense.Awareness;
import org.botlibre.api.sense.Sense;
import org.botlibre.api.sense.Tool;
import org.botlibre.api.thought.Mind;
import org.botlibre.api.thought.Thought;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class represents the identity of a bot,
 * and controls module registration, startup and shutdown.
 * It defines a singleton that represents the system.
 */

public class Bot {	
	public static String PROGRAM = "Bot";
	public static String VERSION = "4.0.2 - 2016-02-29";
	
	public static final Level FINE = Level.FINE;
	public static final Level WARNING = Level.WARNING;
	public static Level DEFAULT_DEBUG_LEVEL = Level.INFO;
	public static final Level[] LEVELS = {Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL};
	
	public static String CONFIG_FILE = "config.xml";
	public static int MAX_CACHE = 100000;
	public static int MIN_CACHE = 10000;
	public static int POOL_SIZE = 20;
	private static ConcurrentMap<String, Bot> instances = new ConcurrentHashMap<String, Bot>();
	private static Queue<String> instancesQueue = new ConcurrentLinkedQueue<String>();
	
	public static Bot systemCache;
	
	public Bot parent;
	
	private Memory memory;
	private Mind mind;
	private Mood mood;
	private Avatar avatar;
	private Awareness awareness;
	private boolean filterProfanity = true;
	private String name;
	private ActiveState state = ActiveState.INIT;
	public enum ActiveState {INIT, ACTIVE, POOLED, SHUTDOWN}

	private Set<LogListener> logListeners = new HashSet<LogListener>();	
	private Level debugLevel = DEFAULT_DEBUG_LEVEL;	
	private Logger log;
		
	static {
		Logger root = Logger.getLogger("org.botlibre");
		root.setUseParentHandlers(false);
		StreamHandler out = new StreamHandler(System.out, new SimpleFormatter()) {
			@Override
			public void publish(LogRecord record) {
				super.publish(record);
				flush();
			}
		};
		out.setLevel(Level.ALL);
		root.addHandler(out);
		try {
			FileHandler file = new FileHandler("Bot.log", 5000000, 10);
			file.setFormatter(new SimpleFormatter());
			file.setLevel(Level.ALL);
			root.addHandler(file);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		try {
			FileHandler file = new FileHandler("Bot.err", 5000000, 10);
			file.setFormatter(new SimpleFormatter());
			file.setLevel(Level.WARNING);
			root.addHandler(file);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public static Bot getSystemCache() {
		return systemCache;
	}

	public static void setSystemCache(Bot systemCache) {
		Bot.systemCache = systemCache;
	}

	public static Bot createInstance() {
		return createInstance(CONFIG_FILE, "", false);
	}
	
	public static Bot createInstance(String configFile, String memory, boolean isSchema) {
		Bot Bot = new Bot();
		Bot.parseConfigFile(configFile);
		Bot.setState(ActiveState.ACTIVE);
		Bot.log(Bot, "Creating instance:", Level.INFO, configFile, memory, isSchema);
		Bot.memory().restore(memory, isSchema);
		Bot.memory().awake();
		Bot.mind().awake();
		Bot.mood().awake();
		Bot.avatar().awake();
		Bot.awareness().awake();
		return Bot;
	}
	
	public static Bot fastCreateInstance(String configFile, String memory, boolean isSchema) {
		Bot bot = new Bot();
		long start = System.currentTimeMillis();
		bot.parseConfigFile(configFile);
		long time = System.currentTimeMillis() - start;
		if (time > 500) {
			System.out.println("Connect parseConfigFile time: " + time);
		}
		bot.setState(ActiveState.ACTIVE);
		bot.log(bot, "Fast creating instance:", Level.INFO, configFile, memory, isSchema);
		start = System.currentTimeMillis();
		bot.memory().fastRestore(memory, isSchema);
		time = System.currentTimeMillis() - start;
		if (time > 500) {
			System.out.println("Connect fastRestore time: " + time);
		}
		start = System.currentTimeMillis();
		bot.memory().awake();
		bot.mind().awake();
		bot.mood().awake();
		bot.avatar().awake();
		bot.awareness().awake();
		time = System.currentTimeMillis() - start;
		if (time > 500) {
			System.out.println("Connect awake time: " + time);
		}
		return bot;
	}
	
	/**
	 * Return the cached instance from the pool if available, otherwise create a new instance.
	 */
	public static Bot createInstanceFromPool(String instanceName, boolean isSchema) {
		Bot instance = instances.remove(instanceName);
		if (instance != null) {
			instancesQueue.remove(instanceName);
			if (instance.getState() != ActiveState.POOLED) {
				instance.log(instance, "Invalid instance in pool", Level.INFO);
				instance = null;
			} else {
				instance.setState(ActiveState.ACTIVE);
				instance.log(instance, "Creating instance from pool, cache size:", Level.INFO, instanceName, instance.memory().cacheSize());
			}
		}
		if (instance == null) {
			//instance =  createInstance(CONFIG_FILE, instanceName);
			long start = System.currentTimeMillis();
			instance =  fastCreateInstance(CONFIG_FILE, instanceName, isSchema);
			long time = System.currentTimeMillis() - start;
			instance.log(instance, "Creating new instance, time, cache size:", Level.INFO, instanceName, time, instance.memory().cacheSize());
		}
		return instance;
	}
	
	public Bot() {
		this.log = Logger.getLogger("org.botlibre." + hashCode());
		this.log.setLevel(this.debugLevel);
		this.parent = systemCache;
	}
	
	public ActiveState getState() {
		return state;
	}

	public void setState(ActiveState state) {
		this.state = state;
	}

	public Bot getParent() {
		return parent;
	}

	public void setParent(Bot parent) {
		this.parent = parent;
	}
	
	public boolean getFilterProfanity() {
		return filterProfanity;
	}

	public void setFilterProfanity(boolean filterProfanity) {
		this.filterProfanity = filterProfanity;
	}
	
	/**
	 * Return the debugging level.
	 */
	public Level getDebugLevel() {
		return this.debugLevel;
	}
	
	public boolean isDebug() {
		return this.debugLevel.intValue() < Level.OFF.intValue();
	}
	
	public boolean isDebugFine() {
		return this.debugLevel.intValue() <= Level.FINE.intValue();
	}
	
	public boolean isDebugFiner() {
		return this.debugLevel.intValue() <= Level.FINER.intValue();
	}
	
	public boolean isDebugFinest() {
		return this.debugLevel.intValue() <= Level.FINEST.intValue();
	}
	
	public boolean isDebugWarning() {
		return this.debugLevel.intValue() <= Level.WARNING.intValue();
	}
	
	public boolean isDebugSever() {
		return this.debugLevel.intValue() <= Level.SEVERE.intValue();
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(Object source, String message, Level level, Object... arguments) {
		try {
			if (this.debugLevel.intValue() <= level.intValue()) {
				for (LogListener listener : getLogListeners()) {
					listener.log(source, message, level, arguments);
				}
				StringWriter writer = new StringWriter();
				writer.write(getName()
						+ " - " + Thread.currentThread()
						+ " -- " + source + ":" + message);
				for (Object argument : arguments) {
					writer.write(" - " + argument);
				}
				getLog().log(level, writer.toString());			
			}
		} catch (Exception exception) {
			System.out.println(exception);
		}
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 *
	public void log(Object source, String message, int level) {
		log(source, message, int level);
	}*/
	
	/**
	 * Log the exception.
	 */
	public void log(Object source, Throwable error) {
		try {
			if (isDebug()) {
				for (LogListener listener : getLogListeners()) {
					listener.log(error);
				}
				log(source, error.getMessage(), WARNING);
				if (!(error instanceof BotException)) {
					error.printStackTrace();
					StringWriter writer = new StringWriter();
					PrintWriter printer = new PrintWriter(writer);
					error.printStackTrace(printer);
					printer.flush();
					String stack = writer.toString();
					log(source, stack.substring(0, Math.max(100, stack.length() - 1)), Level.WARNING);
				}
			}
		} catch (Exception exception) {
			System.out.println(exception);
		}
	}
	
	/**
	 * Set the debugging level.
	 */
	public void setDebugLevel(Level level) {
		this.debugLevel = level;
		this.log.setLevel(level);
		for (LogListener listener : getLogListeners()) {
			listener.logLevelChange(level);
		}
	}
	
	/**
	 * Return the awareness.
	 * The awareness defines the senses.
	 */
	public Awareness awareness() {
		return awareness;
	}
	
	/**
	 * Set the awareness.
	 * The awareness defines the senses.
	 */
	public void setAwareness(Awareness awareness) {
		awareness.setBot(this);
		this.awareness = awareness;
	}
	
	/**
	 * Return the mind.
	 * The mind defines the thoughts.
	 */
	public Mind mind() {
		return mind;
	}
	
	/**
	 * Return the mood.
	 * The mood defines emotional states.
	 */
	public Mood mood() {
		return mood;
	}
	
	/**
	 * Return the Avatar.
	 * The Avatar expresses the emotional states.
	 */
	public Avatar avatar() {
		return avatar;
	}
	
	/**
	 * Set the mind.
	 * The mind defines the thoughts.
	 */
	public void setMind(Mind mind) {
		mind.setBot(this);
		this.mind = mind;
	}
	
	/**
	 * Set the mood.
	 * The mood defines emotional states.
	 */
	public void setMood(Mood mood) {
		mood.setBot(this);
		this.mood = mood;
	}
	
	/**
	 * Set the Avatar.
	 * The Avatar expresses the emotional state.
	 */
	public void setAvatar(Avatar avatar) {
		avatar.setBot(this);
		this.avatar = avatar;
	}
	
	/**
	 * Return the memory.
	 * The memory defines the knowledge networks.
	 */
	public Memory memory() {
		return memory;
	}
	
	/**
	 * Set the memory.
	 * The memory defines the knowledge networks.
	 */
	public void setMemory(Memory memory) {
		memory.setBot(this);
		this.memory = memory;
	}

	/**
	 * Return a Map of the property elements.
	 */
	protected Map<String, Object> getProperties(Element element) {
		NodeList properties = element.getElementsByTagName("property");
		Map<String, Object> propertyValues = new HashMap<String, Object>();
		for (int index = 0; index < properties.getLength(); index++) {
			Element property = (Element) properties.item(index);
			propertyValues.put(property.getAttribute("name"), property.getAttribute("value"));
		}
		return propertyValues;
	}
	
	/**
	 * Parses the config.xml files using the xerces xml dom parser.
	 * Loads the module implementors into the Bot system.
	 */
	@SuppressWarnings("unchecked")
	protected void parseConfigFile(String configFile) {
		// Read config xml to initialize plugins.
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			URL url = Bot.class.getResource(configFile);
			Document document = parser.parse(url.toString());
			Element root = document.getDocumentElement();

			// Parse and initialize memory.
			Element memoryElement = (Element) root.getElementsByTagName("memory").item(0);
			Element implementationClassElement = (Element) memoryElement.getElementsByTagName("implementation-class").item(0);
			Class<Memory> memoryImplementor = (Class<Memory>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
			setMemory(memoryImplementor.newInstance());
			memory().initialize(getProperties(memoryElement));
      
			// Parse and initialize mind.
			Element mindElement = (Element) root.getElementsByTagName("mind").item(0);
			implementationClassElement = (Element) mindElement.getElementsByTagName("implementation-class").item(0);
			Class<Mind> mindImplementor = (Class<Mind>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
			setMind(mindImplementor.newInstance());
			mind().initialize(getProperties(mindElement));
			NodeList thoughtElements = ((Element) mindElement.getElementsByTagName("thoughts").item(0)).getElementsByTagName("thought");
			for (int index = 0; index < thoughtElements.getLength(); index++) {
				Element thoughtElement = (Element) thoughtElements.item(index);
				implementationClassElement = (Element) thoughtElement.getElementsByTagName("implementation-class").item(0);
				Class<Thought> thoughtImplementor = (Class<Thought>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
				Thought thought = thoughtImplementor.newInstance();
				mind().addThought(thought);
				thought.initialize(getProperties(thoughtElement));
			}
			
			// Parse and initialize mood.
			Element moodElement = (Element) root.getElementsByTagName("mood").item(0);
			if (moodElement != null) {
				implementationClassElement = (Element) moodElement.getElementsByTagName("implementation-class").item(0);
				Class<Mood> moodImplementor = (Class<Mood>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
				setMood(moodImplementor.newInstance());
				mood().initialize(getProperties(moodElement));
				NodeList emotionElements = ((Element) moodElement.getElementsByTagName("emotions").item(0)).getElementsByTagName("emotion");
				for (int index = 0; index < emotionElements.getLength(); index++) {
					Element emotionElement = (Element) emotionElements.item(index);
					implementationClassElement = (Element) emotionElement.getElementsByTagName("implementation-class").item(0);
					Class<Emotion> emotionImplementor = (Class<Emotion>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
					Emotion emotion = emotionImplementor.newInstance();
					mood().addEmotion(emotion);
					emotion.initialize(getProperties(emotionElement));
				}
			}
			
			// Parse and initialize avatar.
			Element avatarElement = (Element) root.getElementsByTagName("avatar").item(0);
			implementationClassElement = (Element) avatarElement.getElementsByTagName("implementation-class").item(0);
			Class<Avatar> avatarImplementor = (Class<Avatar>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
			setAvatar(avatarImplementor.newInstance());
			avatar().initialize(getProperties(avatarElement));

			// Parse and initialize awareness.
			Element awarenessElement = (Element) root.getElementsByTagName("awareness").item(0);
			implementationClassElement = (Element) awarenessElement.getElementsByTagName("implementation-class").item(0);
			Class<Awareness> awarenessImplementor = (Class<Awareness>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
			setAwareness(awarenessImplementor.newInstance());
			NodeList senseElements = ((Element) awarenessElement.getElementsByTagName("senses").item(0)).getElementsByTagName("sense");
			for (int index = 0; index < senseElements.getLength(); index++) {
				Element senseElement = (Element) senseElements.item(index);
				implementationClassElement = (Element) senseElement.getElementsByTagName("implementation-class").item(0);
				Class<Sense> senseImplementor = (Class<Sense>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
				Sense sense = senseImplementor.newInstance();
				awareness().addSense(sense);
				sense.initialize(getProperties(senseElement));
			}
			NodeList toolElements = ((Element) awarenessElement.getElementsByTagName("tools").item(0)).getElementsByTagName("tool");
			for (int index = 0; index < toolElements.getLength(); index++) {
				Element toolElement = (Element) toolElements.item(index);
				implementationClassElement = (Element) toolElement.getElementsByTagName("implementation-class").item(0);
				Class<Tool> toolImplementor = (Class<Tool>)Class.forName(implementationClassElement.getFirstChild().getNodeValue());
				Tool tool = toolImplementor.newInstance();
				awareness().addTool(tool);
				tool.initialize(getProperties(toolElement));
			}
		} catch (Exception exception) {
			throw new InitializationException(exception);
		}		
	}
	
	/**
	 * Shutdown the system gracefully, persist memory and terminate thoughts.
	 */
	public synchronized void shutdown() {
		if (this.state == ActiveState.SHUTDOWN) {
			log(this, "Already shutdown", Level.INFO);
			return;
		}
		this.state = ActiveState.SHUTDOWN;
		log(this, "Shutting down", Level.INFO);
		try {
			awareness().shutdown();
			mind().shutdown();
			mood().shutdown();
			avatar().shutdown();
			memory().shutdown();			
		} catch (Exception exception) {
			log(this, exception);
		}
		getLogListeners().clear();
	}
	
	/**
	 * Shutdown the pooled instance.
	 */
	public static void forceShutdown(String name) {
		Bot instance = instances.remove(name);
		if (instance != null) {
			instancesQueue.remove(name);
			instance.log(instance, "Forced shutdown", Level.WARNING);
			instance.shutdown();
		}
		Utils.sleep(1000);
		DatabaseMemory.forceShutdown(name);
	}
	
	public static void clearPool() {
		while (instances.size() > 0) {
			try {
				String oldest = instancesQueue.remove();
				Bot instance = instances.remove(oldest);
				if (instance != null) {
					instance.shutdown();
				}
			} catch (Exception exception) {
				new Bot().log(instancesQueue, exception);
			}
		}		
	}
	
	/**
	 * Return the instance to the pool, or shutdown if too many instances pooled.
	 */
	public synchronized void pool() {
		if (this.state == ActiveState.SHUTDOWN) {
			log(this, "Already shutdown", Level.INFO);
			return;
		}
		if (this.state == ActiveState.POOLED) {
			log(this, "Already pooled", Level.INFO);
			return;
		}
		String name = memory().getMemoryName();
		log(this, "Pooling instance", Level.INFO, name);
		synchronized (memory()) {
			memory().getShortTermMemory().clear();
		}
		if (Utils.checkLowMemory()) {
			log(this, "Low memory - clearing server cache", Level.WARNING);
			memory().freeMemory();
		}
		if ((Utils.checkLowMemory(0.2) && (memory().cacheSize() > MIN_CACHE)) || memory().cacheSize() > MAX_CACHE) {
			log(this, "Cache too big - clearing server cache", Level.WARNING, memory().cacheSize(), MIN_CACHE, MAX_CACHE);
			memory().freeMemory();
		}
		if (instances.containsKey(name)) {
			shutdown();
			return;
		}
		while (instances.size() >= POOL_SIZE) {
			try {
				String oldest = instancesQueue.remove();
				Bot instance = instances.remove(oldest);
				if (instance != null) {
					instance.shutdown();
				}
			} catch (Exception exception) {
				log(instancesQueue, exception);
			}
		}
		try {
			awareness().pool();
			mind().pool();
			mood().pool();
			avatar().pool();
			memory().pool();			
		} catch (Exception exception) {
			log(this, exception);
		}
		// Shutdown if put not successful.
		setState(ActiveState.POOLED);
		setDebugLevel(Level.INFO);
		if (null != instances.putIfAbsent(name, this)) {
			shutdown();
			return;
		} else {
			instancesQueue.add(name);
		}
	}
	
	/**
	 * Return the instance's name.
	 * This is the real name, but defaults to the database name. 
	 */
	public String getName() {
		if (this.name == null) {
			return memory().getMemoryName();
		}
		return this.name;
	}
	
	/**
	 * Set the instance's name.
	 * This is the real name, but defaults to the database name. 
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "Bot(" + getName() + ")";
	}
	
	public String fullToString() {
		StringWriter writer = new StringWriter();
		writer.write("Bot(\n");
		
		writer.write("\tmemory: ");
		writer.write(memory().toString());
		writer.write("\n");
		
		writer.write("\tmind: ");
		writer.write(mind().toString());
		writer.write("\n");
		
		for (Iterator<Thought> thoughtsIterator = mind().getThoughts().values().iterator(); thoughtsIterator.hasNext();) {
			writer.write("\t\tthought: ");
			writer.write(thoughtsIterator.next().toString());
			writer.write("\n");
		}
		
		writer.write("\tmood: ");
		writer.write(mood().toString());
		writer.write("\n");
		
		writer.write("\tavatar: ");
		writer.write(avatar().toString());
		writer.write("\n");
		
		writer.write("\tawareness: ");
		writer.write(awareness().toString());
		writer.write("\n");

		for (Iterator<Sense> sensesIterator = awareness().getSenses().values().iterator(); sensesIterator.hasNext();) {
			writer.write("\t\tsense: ");
			writer.write(sensesIterator.next().toString());
			writer.write("\n");
		}

		for (Iterator<Tool> toolsIterator = awareness().getTools().values().iterator(); toolsIterator.hasNext();) {
			writer.write("\t\ttool: ");
			writer.write(toolsIterator.next().toString());
			writer.write("\n");
		}
		
		writer.write(")");
		return writer.toString();		
	}

	public void addLogListener(LogListener listener) {
		getLogListeners().add(listener);
	}

	public void removeLogListener(LogListener listener) {
		getLogListeners().remove(listener);
	}

	public Set<LogListener> getLogListeners() {
		return logListeners;
	}

	public void setLogListeners(Set<LogListener> logListeners) {
		this.logListeners = logListeners;
	}

	public static ConcurrentMap<String, Bot> getInstances() {
		return instances;
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

}

