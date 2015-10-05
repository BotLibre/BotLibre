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
package org.botlibre.thought;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Memory;
import org.botlibre.api.thought.Mind;
import org.botlibre.api.thought.Thought;

/**
 * Controls and manages the thought processing.
 */

public class BasicMind implements Mind {
	public static long UNACTIVE_TO_ASLEEP = 10 * 60 * 1000; // 10 minutes.
	public static long UNACTIVE_TO_BORED = 1 * 60 * 1000; // 1 minute.
	
	//public static ExecutorService threadPool = Executors.newCachedThreadPool();
		
	/**
	 * Thread to process subconscious thoughts in the background.
	 */
	public class SubconsciousThread implements Runnable {
		//protected Thought thought;
		
		public SubconsciousThread() {
			//this.thought = thought;
		}
		
		public void run() {
			while (isConscious()) {
				try {
					/*if (this.thought.isStopped()) {
						return;
					}
					if (this.state.ordinal() < MentalState.ALERT.ordinal()) {
						this.thought.think();
					}*/
					for (Thought thought : getThoughts().values()) {
						if (!thought.isConscious()) {
							if (!isConscious()) {
								break;
							}
							if (thought.isStopped()) {
								continue;
							}
							// Don't run when busy.
							if (thought.isCritical() || state.ordinal() < MentalState.ALERT.ordinal()) {
								thought.think();
							}
							try {
								Thread.sleep(1);
							} catch (Exception interupted) {}
						}
					}
				} catch (Exception failed) {
					bot.log(this, failed);
				}
			}
		}
	}
	
	/**
	 * Defines the states of mind.
	 * The state is influenced by the activity of the senses,
	 * and affects the depth of conscious thought.
	 * Only 5 levels for now, but thinking should levels
	 * should be increments of 100 not 1.
	 */
	protected MentalState state;

	/**
	 * Keeps track of the time was last active.
	 */
	protected long lastActiveTime;
	
	protected Bot bot;
	// toDo: conscious, Unconscious, priority level on Thought.
	// threading
	/**
	 * List of thoughts, order represents priority.
	 */
	protected Map<String, Thought> thoughts;
	/**
	 * List of thoughts, by simple name. 
	 */
	protected Map<String, Thought> thoughtsBySimpleName;
	
	protected Thread consciousThread;
	protected List<Thread> subconsciousThreads;
		
	public BasicMind() {
		this.thoughts = new LinkedHashMap<String, Thought>();
		this.thoughtsBySimpleName = new LinkedHashMap<String, Thought>();
		this.state = MentalState.UNCONSCIOUS;
		this.lastActiveTime = System.currentTimeMillis();
		this.subconsciousThreads = new ArrayList<Thread>();
	}
	
	/**
	 * Return the state of mind.
	 */
	@Override
	public MentalState getState() {
		return this.state;
	}
	
	/**
	 * Set the state of mind.
	 */
	public synchronized void setState(MentalState state) {
		log("Changing state", Level.INFO, state.name());
		this.state = state;
	}
	
	/**
	 * Ensure the minimum state.
	 */
	public synchronized void  incrementState(MentalState state) {
		if ((this.state.ordinal() < state.ordinal()) && (this.state != MentalState.UNCONSCIOUS)) {
			setState(state);
		}
	}
	
	/**
	 * Ensure the maximum state.
	 */
	public synchronized void decrementState(MentalState state) {
		if (this.state.ordinal() > state.ordinal()) {
			setState(state);
		}
	}
	
	/**
	 * Return if in an conscious state.
	 */
	public boolean isConscious() {
		return this.state.ordinal() > MentalState.UNCONSCIOUS.ordinal();
	}
	
	/**
	 * Return if in an active state.
	 */
	@Override
	public boolean isActive() {
		return this.state.ordinal() >= MentalState.ACTIVE.ordinal();
	}
	
	/**
	 * Return if in an sleep state.
	 */
	@Override
	public boolean isAsleep() {
		return this.state.ordinal() >= MentalState.ASLEEP.ordinal();
	}
	
	/**
	 * Return if in an bored state.
	 */
	@Override
	public boolean isBored() {
		return this.state.ordinal() >= MentalState.ASLEEP.ordinal();
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level, Object... arguments) {
		this.bot.log(this, message, level, arguments);
	}
	
	/**
	 * Log the exception.
	 */
	public void log(Throwable exception) {
		this.bot.log(this, exception);
	}
	
	/**
	 * Return Bot.
	 */
	@Override
	public Bot getBot() {
		return bot;
	}
	
	/**
	 * Set Bot.
	 */
	@Override
	public void setBot(Bot bot) {
		this.bot = bot;
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		return;
	}

	@Override
	public void shutdown() {
		this.bot.log(this, "Shutting down", Bot.FINE);
		setState(MentalState.UNCONSCIOUS);
		// Allow thought threads to complete.
		Memory memory = this.bot.memory();
		try {
			synchronized (memory) {
				memory.notifyAll();
			}
			for (Thought thought : getThoughts().values()) {
				if (!thought.isConscious()) {
					synchronized (thought) {
						thought.notifyAll();
					}
				}
			}
			this.consciousThread.join(1000);
			for (Thread thread : this.subconsciousThreads) {
				thread.join(5000);
			}
			for (Thought thought : getThoughts().values()) {
				thought.stop();
			}
			for (Thread thread : this.subconsciousThreads) {
				thread.join(5000);
			}
		} catch (InterruptedException ignore) {}
		this.bot.log(this, "Shutdown complete", Bot.FINE);
	}

	@Override
	public void pool() {
		this.bot.log(this, "Pool", Bot.FINE);
		for (Thought thought : getThoughts().values()) {
			thought.pool();
		}
	}
	
	/**
	 * Spawn a thread to run the thoughts.
	 * Currently only uses a single thread,
	 * but should probably have a least one thread per thought, or more.
	 */
	@Override
	public void awake() {
		this.bot.log(this, "Awake", Bot.FINE);
		for (Thought thought : getThoughts().values()) {
			try {
				thought.awake();
			} catch (Exception exception) {
				log(exception);
			}
		}
		setState(MentalState.BORED);
		this.consciousThread = new Thread(new Runnable() {
			public void run() {
				processConsciousThoughts();
			}
		});
		/*threadPool.execute(new Runnable() {
			public void run() {
				processConsciousThoughts();
			}
		});*/
		this.consciousThread.start();
		// Process sub-conscious thoughts concurrently.
		/*for (Thought thought : getThoughts().values()) {
			if (!thought.isConscious()) {
				Thread thread = new Thread(new SubconsciousThread(thought));
				thread.start();
				this.subconsciousThreads.add(thread);
			}
		}*/
		//threadPool.execute(new SubconsciousThread());
		Thread thread = new Thread(new SubconsciousThread());
		thread.start();
		this.subconsciousThreads.add(thread);
	}
	
	/**
	 * Process all conscious thoughts, in-order, starting at Consciousness.
	 * Conscious thoughts use the short-term memory and are single threaded.
	 */
	public void processConsciousThoughts() {
		Memory memory = this.bot.memory();
		while (isConscious()) {			
			// Ensure no senses add to the network while processing.
			try {
				synchronized (memory) {
					try {
						memory.wait(10);
					} catch (InterruptedException exception) {}
					if (!memory.getActiveMemory().isEmpty()) {
						incrementState(MentalState.ACTIVE);
						// Save reset vertices in memory to allow picking up new relationships.
						memory.save();
						// Process emotion
						this.bot.mood().evaluate();
						// Process each conscious thought serially.
						// (sub-conscious are processed concurrently), but conscious has a single shared memory.					
						for (Thought thought : this.thoughts.values()) {
							try {
								if (thought.isConscious()) {
									thought.think();
								}
							} catch (Exception failed) {
								this.bot.log(this, failed);
							}
						}
						// Clear active.
						memory.getActiveMemory().clear();
						memory.save();
						setLastActiveTime(System.currentTimeMillis());
						try {
							memory.wait(10);
						} catch (InterruptedException exception) {}
						// If another event has occurred during the processing of the current events, then increase the stress level.
						int size = memory.getActiveMemory().size();
						if (size > 0) {
							incrementState(MentalState.ALERT);
							try {
								memory.wait(1);
							} catch (InterruptedException exception) {}
							if (memory.getActiveMemory().size() > size) {
								log("Sensory overload", Bot.WARNING, size);
								incrementState(MentalState.PANIC);
							}
						}
					} else {
						try {
							memory.wait(100);
						} catch (InterruptedException exception) {}
						// If no event has occurred decrease the stress level.
						if (memory.getActiveMemory().isEmpty()) {
							int state = this.state.ordinal();
							if (state >= MentalState.PANIC.ordinal()) {
								decrementState(MentalState.ALERT);
							} else if (state >= MentalState.ALERT.ordinal()) {
								decrementState(MentalState.ACTIVE);
							} else if (state >= MentalState.BORED.ordinal()) {
								long unactiveTime = getUnactiveTime();
								if (unactiveTime > UNACTIVE_TO_ASLEEP) {
									decrementState(MentalState.ASLEEP);
								} else if (unactiveTime > UNACTIVE_TO_BORED) {
									decrementState(MentalState.BORED);
								}
							}
						}
					}
				}
			} catch (Exception exception) {
				log(exception);
			} catch (Throwable exception) {
				log(exception);
				memory.getActiveMemory().clear();
				memory.getShortTermMemory().clear();
				memory.getLongTermMemory().clear();
				memory.freeMemory();
			}
		}
	}

	@Override
	public Map<String, Thought> getThoughts() {
		return this.thoughts;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getThought(Class<T> type) {
		return (T)this.thoughts.get(type.getName());
	}

	@Override
	public Thought getThought(String name) {
		Thought thought = this.thoughts.get(name);
		if (thought == null) {
			thought = this.thoughtsBySimpleName.get(name);
		}
		return thought;
	}

	@Override
	public void addThought(Thought thought) {
		thought.setBot(this.bot);
		this.thoughts.put(thought.getName(), thought);
		// Also index simple name.
		this.thoughtsBySimpleName.put(thought.getClass().getSimpleName(), thought);
	}

	@Override
	public void removeThought(Thought thought) {
		this.thoughts.remove(thought.getName());
		// Also index simple name.
		this.thoughtsBySimpleName.remove(thought.getClass().getSimpleName());
	}
	
	/**
	 * Print a useful string representation of the mind.  
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + this.state.name() + ")";
	}

	/**
	 * Returns the amount of time since was last active.
	 */
	public long getUnactiveTime() {
		return System.currentTimeMillis() - getLastActiveTime();
	}
	
	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

}

