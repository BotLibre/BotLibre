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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.api.knowledge.MemoryStorageException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BasicMemory;
import org.botlibre.knowledge.BasicNetwork;

/**
 * Defines a set of networks that make up a knowledge base.
 * Defines long term, short term and flash networks.
 * Basic implementation using serialization for persistence.
 */

public class SerializedMemory extends BasicMemory {

	public static File storageDir = null;
	public static String storageFileName = "memory.ser";

	public static void reset() {
		File file = new File(storageDir, storageFileName);
		if (file.exists()) {
			file.delete();
		}
	}
	
	public static boolean checkExists() {
		File file = new File(storageDir, storageFileName);
		return (file.exists());
	}
	
	@Override
	public void shutdown() throws MemoryStorageException {
		super.shutdown();
		File file = new File(storageDir, storageFileName);
		getBot().log(this, "Saving memory to file", Level.INFO, file);
		try {
			List<Vertex> vertices = new ArrayList<Vertex>(((BasicNetwork)getLongTermMemory()).getVertices());
			// Flatten objects to avoid stack overflow.
			List<Relationship> relationships = new ArrayList<Relationship>(vertices.size());
			for (Vertex vertex : vertices) {
				for (Iterator<Relationship> iterator = vertex.allRelationships(); iterator.hasNext(); ) {
					relationships.add(iterator.next());
				}
			}
			FileOutputStream fileStream = new FileOutputStream(file);
			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(vertices);
			objectStream.writeObject(relationships);
			objectStream.flush();
			objectStream.close();
			fileStream.flush();
			fileStream.close();
			getBot().log(this, "Memory saved", Level.INFO, getLongTermMemory().size(), file.length());
		} catch (IOException exception) {
			throw new MemoryStorageException(exception);
		}
	}

	@Override
	public void restore() throws MemoryStorageException {
		restore(storageFileName, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void restore(String database, boolean isSchema) throws MemoryStorageException {
		if (database.isEmpty()) {
			database = storageFileName;
		}
		File file = new File(storageDir, database);
		getBot().log(this, "Restoring memory from file", Level.INFO, file.length());
		Network longTermMemory = new BasicNetwork();
		longTermMemory.setBot(getBot());
		if (file.exists()) {
			try {
				FileInputStream fileStream = new FileInputStream(file);
				ObjectInputStream objectStream = new ObjectInputStream(fileStream);
				long start = System.currentTimeMillis();
				System.out.println(start);
				List<Vertex> vertices = (List<Vertex>) objectStream.readObject();
				List<Relationship> relationships = (List<Relationship>) objectStream.readObject();
				for (Vertex vertex : vertices) {
					longTermMemory.addVertex(vertex);
				}
				for (Relationship relationship : relationships) {
					relationship.getSource().addRelationship(relationship, true);
					((BasicNetwork)longTermMemory).addRelationship(relationship);
				}
				objectStream.close();
				fileStream.close();
				getBot().log(this, "Memory restored file", Level.INFO, longTermMemory.size(), file.length());
			} catch (Exception exception) {
				throw new MemoryStorageException(exception);
			}
		}
		setLongTermMemory(longTermMemory);
	}
		
}

