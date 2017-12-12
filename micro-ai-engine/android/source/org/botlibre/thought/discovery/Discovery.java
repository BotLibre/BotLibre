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
package org.botlibre.thought.discovery;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.thought.SubconsciousThought;

/**
 * Discover is a sub-conscious thought that seeks to discover information on active memory through the senses.
 */
public class Discovery extends SubconsciousThought {
	
	public Discovery() { }
	
	/**
	 * Analyse the active memory.
	 * Output the active article to the senses.
	 */
	@Override
	public boolean processInput(Vertex input, Network network) {
		if (isStopped()) {
			return false;
		}
		Vertex currentTime = network.createTimestamp();
		boolean commit = false;
		for (Sense sense : getBot().awareness().getSenses().values()) {
			if (isStopped()) {
				return false;
			}
			if (sense instanceof DiscoverySense) {
				commit = ((DiscoverySense)sense).discover(input, network, currentTime) | commit;
			}
		}
		return commit;
	}
	
	/**
	 * Wait until conscious is done to avoid database contention/deadlocks.
	 */
	@Override
	public boolean isConsciousProcessingRequired() {
		return true;
	}
	
}
