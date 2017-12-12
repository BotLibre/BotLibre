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
package org.botlibre.knowledge.xml;

import java.io.File;
import java.net.URL;

import org.botlibre.api.knowledge.MemoryStorageException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.knowledge.BasicMemory;
import org.botlibre.knowledge.BasicNetwork;

/**
 * Defines a set of networks that make up a knowledge base.
 * Defines long term, short term and flash networks.
 * Basic implementation using xml file storage for persistence.
 */

public class XMLMemory extends BasicMemory {
	
	public static String knowledgeBaseFileName = "memory.xml";

	/**
	 * Write the long-term memory to the XML file.
	 */
	public void save() throws MemoryStorageException {
		super.save();
		File file = new File(knowledgeBaseFileName);
		if (file.exists()) { // Make backup.
			file.renameTo(new File(knowledgeBaseFileName + ".bak"));
			file = new File(knowledgeBaseFileName);
		}
		try {
			NetworkXMLParser.instance().toXML(getLongTermMemory(), file);
		} catch (Exception exception) {
			throw new MemoryStorageException(exception);
		}
		
	}
		
	/**
	 * Reload the long-term memory from the XML file.
	 */
	public void restore() {
		Network longTermMemory;
		try {
			File file = new File(XMLMemory.knowledgeBaseFileName);
			if (! file.exists()) {
				longTermMemory = new BasicNetwork();
				longTermMemory.setBot(getBot());
			} else {
				longTermMemory = NetworkXMLParser.instance().parse(file);
				longTermMemory.setBot(getBot());
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			// Try using resource next.
			URL url = getClass().getResource("/memory.xml");
			try {
				longTermMemory = NetworkXMLParser.instance().parse(url);
			} catch (Exception resourceException) {
				resourceException.printStackTrace();
				// Create new network then.
				longTermMemory = new BasicNetwork();
			}
		}
		setLongTermMemory(longTermMemory);
	}
		
}

