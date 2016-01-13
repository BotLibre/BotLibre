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
package org.botlibre.knowledge.database;

import org.botlibre.api.knowledge.Network;
import org.botlibre.knowledge.BasicVertex;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.Session;

/**
 * Need to set the network of all vertices when loaded from database.
 */
public class MemorySessionCustomizer implements SessionCustomizer {
	public void customize(Session session) {
		session.getDescriptor(BasicVertex.class).getEventManager().addListener(new DescriptorEventAdapter() {
			@Override
			public void postClone(DescriptorEvent event) {
				if (event.getSession().isUnitOfWork()) {
					Network network = (Network)event.getSession().getProperty("network");
					if (network != null) {
						((BasicVertex)event.getSource()).setNetwork(network);
					}
				}
			}
			@Override
			public void postBuild(DescriptorEvent event) {
				AbstractSession session = event.getSession();
				while (session != null && !session.isServerSession()) {
					session = session.getParent();
				}
				//System.out.println("postBuild : " + System.identityHashCode((BasicVertex)event.getSource()) + " : " + event.getSession().isServerSession() + " : " + event.getSession().getProperty("network"));
				if (session != null) {
					Network network = (Network)event.getSession().getProperty("network");
					if (network != null) {
						((BasicVertex)event.getSource()).setNetwork(network);
					}
				}
			}
			@Override
			public void postMerge(DescriptorEvent event) {
				AbstractSession session = event.getSession();
				while (session != null && !session.isServerSession()) {
					session = session.getParent();
				}
				//System.out.println("postMerge : " + System.identityHashCode((BasicVertex)event.getSource()) + " : " + event.getSession().isServerSession() + " : " + " : " + session.getProperty("network"));
				if (session != null) {
					Network network = (Network)session.getProperty("network");
					if (network != null) {
						((BasicVertex)event.getSource()).setNetwork(network);
					}
				}
			}
		});
		
	}
}