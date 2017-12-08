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
package org.botlibre.tool;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.sense.BasicTool;
import org.botlibre.sense.http.Http;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * JavaScript JSON class.
 */

public class JSON extends BasicTool {

	public JSON() {
	}

	/**
	 * Self API.
	 * Parse the JSON string into a Self object.
	 */
	public Vertex parse(Vertex source, Vertex text) {
		Network network = source.getNetwork();
		JSONObject root = (JSONObject)JSONSerializer.toJSON(text.printString().trim());
		if (root == null) {
			return null;
		}
		Vertex object = getBot().awareness().getSense(Http.class).convertElement(root, network);
		return object;
	}

	/**
	 * Self API.
	 * Convert the object to a JSON string.
	 */
	public Vertex stringify(Vertex source, Vertex jsonObject) {
		try {
			Network network = source.getNetwork();
			String data = getBot().awareness().getSense(Http.class).convertToJSON(jsonObject);
			return network.createVertex(data);
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}
	
}