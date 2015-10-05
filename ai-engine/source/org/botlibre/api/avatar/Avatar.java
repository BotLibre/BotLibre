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
package org.botlibre.api.avatar;

import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.ImageData;
import org.botlibre.knowledge.Primitive;

/**
 * Allows visual expression of emotions and state.
 */

public interface Avatar {
		
	void shutdown();
	
	void awake();
	
	void pool();

	ImageData getCurrentImage();
	
	Bot getBot();
	
	Primitive getPrimitive();
	
	void setBot(Bot Bot);
	
	String getAction();

	void setAction(String action);

	String getPose();

	void setPose(String pose);

	String getEmote();

	void setEmote(String emote);

	/**
	 * Evaluate the output and express in the Avatar's image.
	 */
	void evaluateOutput(Vertex output);
	
	/**
	 * Evaluate the input response for actions and poses.
	 */
	void evaluateResponse(Vertex output, Vertex response, Vertex meta);

	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);

}

