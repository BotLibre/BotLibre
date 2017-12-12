/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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
package org.botlibre.knowledge.micro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.botlibre.knowledge.BasicRelationship;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.BinaryData;

/**
 * MicroMemory stores the memory to an optimized binary file using a custom serializer.
 * The entire memory is loaded into memory.
 * This memory is fast and good for embedded environments such as Android, but does not scale to large memory sizes.
 */
public class MicroMemory extends BasicMemory {

	public static File storageDir = null;
	public static String storageFileName = "memory.ser";

	public static void reset() {
		File file = new File(storageDir, storageFileName);
		if (file.exists()) {
			file.delete();
		}
		file = new File(storageDir, storageFileName + "x");
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
		long start = System.currentTimeMillis();
		super.shutdown();
		File file = new File(storageDir, storageFileName);
		DataOutputStream stream = createFile(file);
		getBot().log(this, "Saving memory to file", Level.INFO, file);
		try {
			List<Vertex> vertices = new ArrayList<Vertex>(((BasicNetwork) getLongTermMemory()).getVerticesById().values());
			List<Relationship> relationships = new ArrayList<Relationship>(vertices.size());
			for (Vertex vertex : vertices) {
				saveVertex(vertex, stream);
				for (Iterator<Relationship> iterator = vertex.allRelationships(); iterator.hasNext();) {
					relationships.add(iterator.next());
				}
			}
			stream.writeLong(0);
			for (Relationship relationship : relationships) {
				saveRelationship(relationship, stream);
			}
			stream.writeLong(0);
			stream.flush();
			stream.close();
			long time = System.currentTimeMillis() - start;
			getBot().log(this, "Memory saved (size, file size, time)", Level.INFO, getLongTermMemory().size(), file.length(), time);
		} catch (IOException exception) {
			getBot().log(this, exception);
			throw new MemoryStorageException(exception);
		}
		
	}

	@Override
	public void restore() throws MemoryStorageException {
		restore(storageFileName, true);
	}

	@Override
	public void restore(String database, boolean isSchema) throws MemoryStorageException {
		if (database.isEmpty()) {
			database = storageFileName;
		}
		File file = new File(storageDir, database);
		getBot().log(this, "Restoring memory from file", Level.INFO, file, file.length());
		Network longTermMemory = new BasicNetwork();
		longTermMemory.setBot(getBot());
		if (file.exists()) {
			try {
				long start = System.currentTimeMillis();
				DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				List<Vertex> vertices = (List<Vertex>) restoreVertex(file, inputStream);
				for (Vertex vertex : vertices) {
				     longTermMemory.addVertex(vertex);
				}
				List<Relationship> relationships = (List<Relationship>) restoreRelationship(file, longTermMemory, inputStream);
				for (Relationship relationship : relationships) {
					relationship.getSource().addRelationship(relationship, true);
					((BasicNetwork) longTermMemory).addRelationship(relationship);
				}
				inputStream.close();
				long time = System.currentTimeMillis() - start;
				getBot().log(this, "Memory restored from file (vertices, relationships, file size, time)", Level.INFO, longTermMemory.size(), relationships.size(), file.length(), time);
			} catch (Exception exception) {
				getBot().log(this, exception);
				throw new MemoryStorageException(exception);
			}
		}
		setLongTermMemory(longTermMemory);
	}
	
	public List<Relationship> restoreRelationship(File file, Network longTermMemory, DataInputStream inputStream) {
		List<Relationship> relations = new ArrayList<Relationship>();
		try {
			while (true) {
				long relationID = inputStream.readLong();
				BasicRelationship relation = new BasicRelationship();
				if (relationID == 0){
					break;
				} else {
					relation.setId(relationID);
				}
				long soruceID = inputStream.readLong();
				long typeID = inputStream.readLong();
				long targetID = inputStream.readLong();
				long metaID = inputStream.readLong();
				int index = inputStream.readInt();
				float correctness = inputStream.readFloat();
				relation.setSource(longTermMemory.findById(soruceID));
				relation.setType(longTermMemory.findById(typeID));
				relation.setTarget(longTermMemory.findById(targetID));
				if (metaID != 0) {
					relation.setMeta(longTermMemory.findById(metaID));
				}
				relation.setIndex(index);
				relation.setCorrectness(correctness);
				relations.add(relation);
			}
		} catch (FileNotFoundException exception) {
			getBot().log(this, exception);
		} catch (IOException exception) {
			getBot().log(this, exception);
		} 
		return relations;
	}
	
	public List<Vertex> restoreVertex(File file, DataInputStream inputStream) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		try {
			while (true) {
				BasicVertex vertex = new BasicVertex();
				long num = inputStream.readLong();
				if (num == 0) {
					break;
				} else {
					vertex.setId(num);
				}
				String dataType = inputStream.readUTF();
				if (dataType.equals("")) {
					inputStream.readUTF(); //read dataValue = ""
					vertex.setDataType(null);
					vertex.setDataValue(null);
				} else {
					vertex.setDataType(dataType);
					if (vertex.getData() instanceof BinaryData || vertex.getDataType().equals("Binary")){
						int size = inputStream.readInt();
						byte[] bytes = new byte[size];
						inputStream.read(bytes);
						BinaryData bd = new BinaryData();
						bd.setBytes(bytes);
						vertex.setData(bd);
					} else {
						String dataValue = inputStream.readUTF();
						vertex.setDataValue(dataValue);
					}
				}
				String name = inputStream.readUTF();
				vertex.setName(name);
				vertices.add(vertex);
			}
		} catch (FileNotFoundException exception) {
			getBot().log(this, exception);
		} catch (IOException exception) {
			getBot().log(this, exception);
		}
		return vertices;
	}

	public void saveVertex(Vertex vertex, DataOutputStream stream) {
		try {
			stream.writeLong(vertex.getId());
			if (vertex.getDataType() != null && !vertex.getDataType().isEmpty()) {
				stream.writeUTF(vertex.getDataType());
				if (vertex.getData() instanceof BinaryData) {
					stream.writeInt(((BinaryData)vertex.getData()).getBytes().length);
					stream.write(((BinaryData)vertex.getData()).getBytes());
				} else {// else it will just write the normal value
					stream.writeUTF(vertex.getDataValue());
				}
			} else {
				stream.writeUTF("");
				stream.writeUTF("");
			}
			if (vertex.getName() == null) {
				stream.writeUTF("");
			} else {
				stream.writeUTF(vertex.getName());
			}
		} catch (IOException exception) {
			getBot().log(this, exception);
		}
	}

	public void saveRelationship(Relationship relationship, DataOutputStream stream) {
		try {
			stream.writeLong(relationship.getId());
			stream.writeLong(relationship.getSource().getId());
			stream.writeLong(relationship.getType().getId());
			stream.writeLong(relationship.getTarget().getId());
			if (relationship.getMeta() != null) {
				stream.writeLong(relationship.getMeta().getId());
			} else {
				stream.writeLong(0);
			}
			stream.writeInt(relationship.getIndex());
			stream.writeFloat(relationship.getCorrectness());
		} catch (IOException exception) {
			getBot().log(this, exception);
		}
	}

	public DataOutputStream createFile(File fileName) {
		try {
			DataOutputStream dataStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			return dataStream;
		} catch (IOException exception) {
			getBot().log(this, exception);
		}
		return null;
	}
}

