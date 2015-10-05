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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.botlibre.api.knowledge.MemoryEventListener;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.BasicThought;

/**
 * A sub-conscious thought that processes active memory in the background.
 */
public abstract class SubconsciousThought extends BasicThought {
	public int threshold = 200;
	public int delay = 0;
	
	protected Queue<Vertex> activeMemoryBackLog = new ConcurrentLinkedQueue<Vertex>();
	
	protected MemoryEventListener listener;
	
	public SubconsciousThought() { }
	
	/**
	 * Determine if the thought should first wait for conscious thoughts to process the input.
	 */
	public boolean isConsciousProcessingRequired() {
		return false;
	}
	
	/**
	 * Add a listener to the memory to be notified when new active memory.
	 */
	@Override
	public void awake() {
		super.awake();
		this.listener = new MemoryEventListener() {
			public void addActiveMemory(Vertex vertex) {
				if (isStopped || !isEnabled || !bot.mind().isConscious()) {
					return;
				}
				if (getActiveMemoryBackLog().size() > SubconsciousThought.this.threshold) {
					bot.log(this, "Subconscious backlog threshold reached, clearing backlog", Level.WARNING);
					getActiveMemoryBackLog().clear();
				}
				getActiveMemoryBackLog().add(vertex);
				synchronized (SubconsciousThought.this) {
					SubconsciousThought.this.notify();
				}
			}
		};
		this.bot.memory().addListener(this.listener);
	}
	
	public void stop() {
		super.stop();
		this.bot.memory().removeListener(this.listener);
	}
	
	/**
	 * Analyze the active memory.
	 * Output the active article to the senses.
	 */
	@Override
	public void think() {
		if (this.isStopped || !this.isEnabled || !this.bot.mind().isConscious()) {
			getActiveMemoryBackLog().clear();
			return;
		}
		Vertex vertex = null;
		synchronized (this) {
			vertex = getActiveMemoryBackLog().poll();
			if (vertex == null) {
				try {
					wait(1000);
				} catch (InterruptedException exception) {}
				if (this.isStopped || !this.isEnabled || !this.bot.mind().isConscious()) {
					getActiveMemoryBackLog().clear();
					return;
				}
			}
		}
		if (vertex == null) {
			vertex = getActiveMemoryBackLog().poll();
		}
		if (vertex != null) {
			try {
				Thread.sleep(this.delay);
				if (this.isStopped || !this.isEnabled || !this.bot.mind().isConscious()) {
					getActiveMemoryBackLog().clear();
					return;
				}
				Network memory = this.bot.memory().newMemory();
				vertex = memory.createVertex(vertex);
				int abort = 0;
				if (isConsciousProcessingRequired()) {
					while ((abort < 20) && !vertex.hasRelationship(Primitive.CONTEXT)) {
						Thread.sleep(1000);
						if (this.isStopped || !this.isEnabled || !this.bot.mind().isConscious()) {
							getActiveMemoryBackLog().clear();
							return;
						}
						memory = this.bot.memory().newMemory();
						vertex = memory.createVertex(vertex);
						abort++;
					}
				}
				if (abort < 20) {
	 				boolean commit = processInput(vertex, memory);
					if (commit && isEnabled() && !isStopped() && this.bot.mind().isConscious()) {
						memory.save();
					}
				}
			} catch (Exception failed) {
				log(failed);
			}
		}
	}

	/**
	 * Process the active memory in the isolated memory in the background.
	 * Return if memory should be saved, or discarded.
	 */
	public abstract boolean processInput(Vertex vertex, Network network);
	
	/**
	 * Thoughts can be conscious or sub-conscious.
	 * A conscious thought is run by the mind single threaded with exclusive access to the short term memory.
	 * A sub-conscious thought is run concurrently, and must run in its own memory space.
	 */
	@Override
	public boolean isConscious() {
		return false;
	}

	public Queue<Vertex> getActiveMemoryBackLog() {
		return activeMemoryBackLog;
	}

	public void setActiveMemoryBackLog(Queue<Vertex> activeMemoryBackLog) {
		this.activeMemoryBackLog = activeMemoryBackLog;
	}
	
}
