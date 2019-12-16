/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.bean;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.self.SelfCompiler;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

public class TrainingBean extends ServletBean {
	
	public TrainingBean() {
	}

	public List<Vertex> getDefaultResponses() {
		List<Vertex> responses = new ArrayList<Vertex>();
		Network memory = getBot().memory().newMemory();
		List<Relationship> relationships = memory.createVertex(Language.class).orderedRelationships(Primitive.RESPONSE);
		if (relationships != null) {
			for (Relationship relationship : relationships) {
				responses.add(relationship.getTarget());
			}
		}
		return responses;
	}
	
	public void removeDefaultResponses(String defaultResponse) {
		if ((defaultResponse == null) || defaultResponse.equals("")) {
			throw new BotException("Please select a response to remove");
		}
		Network memory = getBot().memory().newMemory();
		Long id = null;
		try {
			id = Long.valueOf(defaultResponse);
		} catch (Exception exception) {}
		Vertex response = null;
		if (id == null) {
			response = memory.createVertex(defaultResponse);			
		} else {
			response = memory.findById(Long.valueOf(id));
			if (response == null) {
				return;
			}
		}
		Vertex language = memory.createVertex(Language.class);
		Relationship relationship = language.getRelationship(Primitive.RESPONSE, response);
		if (relationship == null) {
			return;
		}
		relationship.getTarget().setPinned(false);
		if (relationship.getTarget().instanceOf(Primitive.FORMULA)) {
			SelfCompiler.getCompiler().unpin(relationship.getTarget());
		}
		language.internalRemoveRelationship(relationship);
		memory.save();
	}
	
	public void addDefaultResponses(String response) {
		if (response == null || response.trim().equals("")) {
			return;
		}
		if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(response, getBotBean().getInstance().getContentRatingLevel())) {
			throw BotException.offensive();
		}
		Utils.checkScript(response);
		Network memory = getBot().memory().newMemory();
		Vertex language = memory.createVertex(Language.class);
		Vertex sentence = memory.createSentence(response);
		SelfCompiler.getCompiler().pin(sentence);
		language.addRelationship(Primitive.RESPONSE, sentence);
		memory.save();
	}

	public List<Vertex> getGreetings() {
		List<Vertex> greetings = new ArrayList<Vertex>();
		Network memory = getBot().memory().newMemory();
		List<Relationship> relationships = memory.createVertex(Language.class).orderedRelationships(Primitive.GREETING);
		if (relationships != null) {
			for (Relationship relationship : relationships) {
				greetings.add(relationship.getTarget());
			}
		}
		return greetings;
	}
	
	public void removeGreeting(String greeting) {
		if ((greeting == null) || greeting.equals("")) {
			throw new BotException("Please select a greeting to remove");
		}
		Network memory = getBot().memory().newMemory();
		Long id = null;
		try {
			id = Long.valueOf(greeting);
		} catch (Exception exception) {}
		Vertex response = null;
		if (id == null) {
			response = memory.createVertex(greeting);			
		} else {
			response = memory.findById(Long.valueOf(id));
			if (response == null) {
				return;
			}
		}
		Vertex language = memory.createVertex(Language.class);
		Relationship relationship = language.getRelationship(Primitive.GREETING, response);
		if (relationship == null) {
			return;
		}
		relationship.getTarget().setPinned(false);
		if (relationship.getTarget().instanceOf(Primitive.FORMULA)) {
			SelfCompiler.getCompiler().unpin(relationship.getTarget());
		}
		language.internalRemoveRelationship(relationship);
		memory.save();
	}
	
	public void addGreeting(String greeting) {
		if (greeting == null || greeting.trim().equals("")) {
			return;
		}
		if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(greeting, getBotBean().getInstance().getContentRatingLevel())) {
			throw BotException.offensive();
		}
		Utils.checkScript(greeting);
		Network memory = getBot().memory().newMemory();
		Vertex language = memory.createVertex(Language.class);
		Vertex sentence =  memory.createSentence(greeting);
		SelfCompiler.getCompiler().pin(sentence);
		language.addRelationship(Primitive.GREETING, sentence);
		memory.save();
	}
	
	public String getName(Vertex vertex) {
		if (vertex == null) {
			return "null";
		}
		Vertex name = vertex.mostConscious(Primitive.WORD);
		if (name == null) {
			if (vertex.getName() != null) {
				return vertex.getName();
			}
			if (vertex.getData() != null) {
				return String.valueOf(vertex.getName());
			}
			return vertex.getId().toString();
		}
		return (String)name.getData();
	}
	
	public void addQuestionResponses(String question, String response) {
		if (question == null || response == null) {
			return;
		}
		question = question.trim();
		response = response.trim();
		if (question.equals("") || response.equals("")) {
			return;
		}
		if (!getBotBean().getInstance().isAdult() && (Utils.checkProfanity(question, getBotBean().getInstance().getContentRatingLevel()) || (Utils.checkProfanity(response, getBotBean().getInstance().getContentRatingLevel())))) {
			throw BotException.offensive();
		}
		Utils.checkScript(question);
		Utils.checkScript(response);
		StringWriter writer = new StringWriter();
		String userName = getUserId();
		if (!isLoggedIn()) {
			userName = "anonymous";
		}
		writer.write(userName + ": ");
		writer.write(question);
		writer.write("\n");
		writer.write("self: ");
		writer.write(response);
		writer.write("\n");
		ResponseListParser.parser().processChatLog(writer.toString(), true, false, false, getBot());
		Network memory = getBot().memory().newMemory();
		Vertex questionVertex = memory.createVertex(question);
		questionVertex.setPinned(true);
		Vertex responseVertex = memory.createVertex(response);
		responseVertex.setPinned(true);
		if (responseVertex.instanceOf(Primitive.FORMULA)) {
			SelfCompiler.getCompiler().pin(responseVertex);
		}
		Relationship relationship = questionVertex.getRelationship(Primitive.RESPONSE, responseVertex);
		if (relationship != null) {
			// 90%
			relationship.setCorrectness(Math.max(1.0f, relationship.getCorrectness()));
		}
		memory.save();
	}
}
