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
package org.botlibre.test;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.thought.language.Language;

/**
 * Test the state decompiler.
 */

public class TestAIMLDecompile extends TestAIML2 {

	@BeforeClass
	public static void setup() {
		TestAIML2.setup();
		
		Bot bot = Bot.createInstance();
		
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Collection<Relationship> states = new ArrayList<Relationship>(language.getRelationships(Primitive.STATE));
		for (Relationship state : states) {
			String code = SelfDecompiler.getDecompiler().decompileStateMachine(state.getTarget(), network);
			Vertex newState = SelfCompiler.getCompiler().parseStateMachine(code, false, network);
			language.replaceRelationship(state, newState);
			network.removeRelationship(state);
		}
		network.save();
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

