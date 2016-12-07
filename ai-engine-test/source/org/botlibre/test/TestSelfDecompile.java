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
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.Self4Compiler;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Test the state decompiler.
 */

public class TestSelfDecompile extends TestSelf {

	@BeforeClass
	public static void setup() {
		SelfCompiler.setCompiler(new Self4Compiler());
		bootstrap();
		Bot bot = Bot.createInstance();
		
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Collection<Relationship> states = new ArrayList<Relationship>(language.getRelationships(Primitive.STATE));
		for (Relationship state : states) {
			String code = SelfDecompiler.getDecompiler().decompileStateMachine(state.getTarget(), network);
			SelfCompiler compiler = SelfCompiler.getCompiler();
			if ("Self".equals(state.getTarget().getName())) {
				compiler = new Self4Compiler();
			}
			Vertex newState = compiler.parseStateMachine(code, false, network);
			language.replaceRelationship(state, newState);
			network.removeRelationship(state);
		}
		network.save();
		
		network = bot.memory().newMemory();
		language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Vertex script = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("test.self"), "", false, network);
		language.setRelationship(Primitive.STATE, script);
		network.save();
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

