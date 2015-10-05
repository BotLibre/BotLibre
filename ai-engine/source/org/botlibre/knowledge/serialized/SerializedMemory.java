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
package org.botlibre.knowledge.serialized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.botlibre.api.knowledge.MemoryStorageException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.knowledge.BasicMemory;
import org.botlibre.knowledge.BasicNetwork;

/**
 * Defines a set of networks that make up a knowledge base.
 * Defines long term, short term and flash networks.
 * Basic implementation using serialization for persistence.
 */

public class SerializedMemory extends BasicMemory {
	
	public static String knowledgeBaseFileName = "memory.ser";
	
	public void save() throws MemoryStorageException {
		super.save();
		File file = new File(knowledgeBaseFileName);
		try {
			FileOutputStream fileStream = new FileOutputStream(file);
			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(getLongTermMemory());
			objectStream.flush();
			objectStream.close();
			fileStream.flush();
			fileStream.close();
		} catch (IOException exception) {
			throw new MemoryStorageException(exception);
		}
		
	}
		
	public void restore() throws MemoryStorageException {
		File file = new File(knowledgeBaseFileName);
		Network longTermMemory;
		if (! file.exists()) {
			longTermMemory = new BasicNetwork();
			longTermMemory.setBot(getBot());
		} else {
			try {
				FileInputStream fileStream = new FileInputStream(file);
				ObjectInputStream objectStream = new ObjectInputStream(fileStream);
				longTermMemory = (Network) objectStream.readObject();
				objectStream.close();
				fileStream.close();
			} catch (Exception exception) {
				throw new MemoryStorageException(exception);
			}
		}
		setLongTermMemory(longTermMemory);
	}
		
}

