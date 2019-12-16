/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;


public abstract class InstanceManager<T> {
	public static int MIN_SIZE = 10;
	public static int MAX_SIZE = 100;
	public static long MAX_AGE = 1000 * 60 * 30; // 30 minutes
	public static long MAX_SCAVENGE = 1000 * 60 * 10; // 10 minutes
	
	public class InstanceInfo implements Comparable<InstanceInfo> {
		InstanceInfo(T instance) {
			this.instance = instance;
			this.age = System.currentTimeMillis();
			this.key = nextId();
		}
		InstanceInfo(T instance, long id) {
			this.instance = instance;
			this.age = System.currentTimeMillis();
			this.key = nextId(id);
		}
		
		public T instance;
		public long age;
		public long key;
		public boolean removed = false;
		
		public int compareTo(InstanceInfo info) {
			if (age > info.age) {
				return 1;
			} else if (age < info.age) {
				return -1;
			}
			return 0;
		}
	}

	protected Random random = new Random();
	protected boolean alive;
	protected boolean scavenging;
	protected long scavengingStart;
	protected InstanceInfo scavenged;
	protected boolean deadlocked;
	protected int minSize = MIN_SIZE;
	protected int maxSize = MAX_SIZE;
	protected long maxAge = MAX_AGE;
	protected long maxScavenge = MAX_SCAVENGE;
	protected long nextId;
	protected Queue<InstanceInfo> queue = new ConcurrentLinkedQueue<InstanceInfo>();
	protected Map<Long, InstanceInfo> instances = new ConcurrentHashMap<Long, InstanceInfo>();
	protected Thread scavenger;
	protected int scavenges = 0;
		
	public InstanceManager() {
		this.alive = true;
		resetScavenger();
	}
	
	public void resetScavenger() {
		scavenger = new Thread() {
			public void run() {
				while (alive) {
					try {
						scavenge();
						if (scavenger != Thread.currentThread()) {
							break;
						}
					} catch (Throwable exception) {
						AdminDatabase.instance().log(exception);						
					}
				}
			}
		};
		scavenger.start();
	}
	
	public Thread getScavenger() {
		return scavenger;
	}

	public void setScavenger(Thread scavenger) {
		this.scavenger = scavenger;
	}

	public int getScavenges() {
		return scavenges;
	}

	public void setScavenges(int scavenges) {
		this.scavenges = scavenges;
	}

	public void shutdown() {
		this.alive = false;
	}
	
	private void scavenge() {
		synchronized (this) {
			try {
				wait(this.maxAge);
			} catch (InterruptedException exception) {}
		}
		this.scavengingStart = System.currentTimeMillis();
		this.scavenging = true;
		trimCache();
		if (this.scavenger == Thread.currentThread()) {
			this.scavenging = false;
		}
	}
	
	public boolean isScavenging() {
		return scavenging;
	}

	public void setScavenging(boolean scavenging) {
		this.scavenging = scavenging;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public long getScavengingStart() {
		return scavengingStart;
	}

	public void setScavengingStart(long scavengingStart) {
		this.scavengingStart = scavengingStart;
	}

	public InstanceInfo getScavenged() {
		return scavenged;
	}

	public void setScavenged(InstanceInfo scavenged) {
		this.scavenged = scavenged;
	}

	public boolean isDeadlocked() {
		return deadlocked;
	}

	public void setDeadlocked(boolean deadlocked) {
		this.deadlocked = deadlocked;
	}

	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}

	public long getMaxScavenge() {
		return maxScavenge;
	}

	public void setMaxScavenge(long maxScavenge) {
		this.maxScavenge = maxScavenge;
	}

	private long nextId() {
		long id = Math.abs(this.random.nextLong());
		while (instances.containsKey(id)) {
			id = Math.abs(this.random.nextLong());
		}
		return id;
	}

	private long nextId(long requestedId) {
		long id = Math.abs(requestedId);
		while (instances.containsKey(id)) {
			id = Math.abs(this.random.nextLong());
		}
		return id;
	}
	
	private synchronized void trimMin() {
		while (this.queue.size() > this.minSize) {
			this.queue.poll();
		}
		if ((this.instances.size() > this.maxSize) || Utils.checkLowMemory()) {
			notify();
			if (this.instances.size() > (this.maxSize * 1.5)) {
				this.scavenger.interrupt();
			}
			if (this.scavenging && (this.instances.size() > (this.maxSize * 2))
						&& (System.currentTimeMillis() - this.scavengingStart) > this.maxScavenge) {
				// Thread may be blocked, setup a new thread.
				this.deadlocked = true;
				this.scavengingStart = System.currentTimeMillis();
				resetScavenger();
			}
		}
	}
	
	public synchronized void clearCache() {
		notify();
	}
	
	private void trimCache() {
		this.scavenges++;
		try {
			AdminDatabase.instance().log(Level.INFO, "Trim cache (instances, queue)", this, this.instances.size(), this.queue.size());
			Set<InstanceInfo> set = new HashSet<InstanceInfo>(this.queue.size());
			set.addAll(queue);
			if (Utils.checkLowMemory()) {
				AdminDatabase.instance().log(Level.WARNING, "Low memory - clearing instance cache", this);
				for (Iterator<Map.Entry<Long, InstanceInfo>> iterator = this.instances.entrySet().iterator(); iterator.hasNext(); ) {
					InstanceInfo info = iterator.next().getValue();
					if (!set.contains(info)) {
						AdminDatabase.instance().log(Level.INFO, "Removing non-queued", info.instance);
						iterator.remove();
						if (!info.removed) {
							AdminDatabase.instance().log(Level.INFO, "Removing instance", info.instance);
							this.scavenged = info;
							shutdown(info.instance);
						}
						info.removed = true;
						AdminDatabase.instance().log(Level.INFO, "Removed", info.instance);
					}
				}
				for (Iterator<InstanceInfo> iterator = this.queue.iterator(); iterator.hasNext(); ) {
					InstanceInfo info = iterator.next();
					if ((System.currentTimeMillis() - info.age) > this.maxAge) {
						AdminDatabase.instance().log(Level.INFO, "Removing old instance", info.instance);
						iterator.remove();
						this.instances.remove(info.key);
						if (!info.removed) {
							AdminDatabase.instance().log(Level.INFO, "Expiring instance", info.instance);
							this.scavenged = info;
							shutdown(info.instance);
						}
						info.removed = true;
						AdminDatabase.instance().log(Level.INFO, "Removed", info.instance);
					}
				}
			}
			for (Iterator<Map.Entry<Long, InstanceInfo>> iterator = this.instances.entrySet().iterator(); iterator.hasNext(); ) {
				InstanceInfo info = iterator.next().getValue();
				if ((System.currentTimeMillis() - info.age) > this.maxAge) {
					if (!set.contains(info)) {
						AdminDatabase.instance().log(Level.INFO, "Removing", info.instance);
						iterator.remove();
						if (!info.removed) {
							AdminDatabase.instance().log(Level.INFO, "Expiring instance", info.instance);
							this.scavenged = info;
							shutdown(info.instance);
						}
						info.removed = true;
						AdminDatabase.instance().log(Level.INFO, "Removed", info.instance);
					}
				}
			}
			if (this.instances.size() > this.maxSize) {
				AdminDatabase.instance().log(Level.INFO, "Max size - clearing instances by age");
				List<InstanceInfo> sorted = new ArrayList<InstanceInfo>(this.instances.values());
				Collections.sort(sorted);
				int index = 0;
				while (this.instances.size() > (this.maxSize/2) && index < sorted.size()) {
					InstanceInfo info = sorted.get(index);
					AdminDatabase.instance().log(Level.INFO, "Removing", info.instance);
					info = this.instances.remove(info.key);
					if (!info.removed) {
						AdminDatabase.instance().log(Level.INFO, "Expiring instance", info.instance);
						this.scavenged = info;
						shutdown(info.instance);
					}
					info.removed = true;
					index++;
					AdminDatabase.instance().log(Level.INFO, "Removed", info.instance);
				}
			}
			AdminDatabase.instance().log(Level.INFO, "Trim cache finished (instances, queue)", this, this.instances.size(), this.queue.size());
		} catch (Exception exception) {
			AdminDatabase.instance().log(Level.SEVERE, "Trim cache failed", exception);			
		}
	}
	
	public abstract void shutdown(T instance);
	
	public synchronized Long addInstance(T instance) {
		InstanceInfo info = new InstanceInfo(instance);
		this.queue.add(info);
		this.instances.put(info.key, info);
		trimMin();
		return info.key;
	}
	
	public synchronized Long addInstance(T instance, long id) {
		if (id == 0L) {
			return 0L;
		}
		InstanceInfo info = new InstanceInfo(instance, id);
		this.queue.add(info);
		this.instances.put(info.key, info);
		trimMin();
		return info.key;
	}
	
	public synchronized T getInstance(Long id) {
		if (id == null || id == 0L) {
			return null;
		}
		InstanceInfo info = this.instances.get(id);
		if (info == null) {
			return null;
		}
		info.age = System.currentTimeMillis();
		this.queue.remove(info);
		this.queue.add(info);
		trimMin();
		return info.instance;
	}
	
	public synchronized T removeInstance(Long id) {
		InstanceInfo info = this.instances.remove(id);
		if (info == null) {
			return null;
		}
		info.removed = true;
		this.queue.remove(info);
		return info.instance;
	}

	public Queue<InstanceInfo> getQueue() {
		return queue;
	}

	public void setQueue(Queue<InstanceInfo> queue) {
		this.queue = queue;
	}

	public Map<Long, InstanceInfo> getInstances() {
		return instances;
	}

	public void setInstances(Map<Long, InstanceInfo> instances) {
		this.instances = instances;
	}
	
}
