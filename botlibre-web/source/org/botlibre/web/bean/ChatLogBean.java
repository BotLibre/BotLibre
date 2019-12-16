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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.self.SelfCompiler;
import org.botlibre.thought.language.Language;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.owasp.encoder.Encode;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.rest.ConversationConfig;
import org.botlibre.web.rest.InputConfig;
import org.botlibre.web.rest.ResponseConfig;
import org.botlibre.web.rest.ResponseSearchConfig;
import org.botlibre.web.service.BotStats;

public class ChatLogBean extends ServletBean {
	
	public static final String CONVERSATIONS = "conversations";
	public static final String RESPONSES = "responses";
	public static final String GREETINGS = "greetings";
	public static final String DEFAULT = "default";
	public static final String PHRASES = "phrases";
	public static final String WORDS = "words";
	public static final String FLAGGED = "flagged";
	
	public static final String DATE = "date";
	public static final String DATE_DESC = "date-desc";
	public static final String QUESTION = "question";
	public static final String RESPONSE = "response";

	public static final String ALL = "all";
	public static final String CHAT = "chat";
	public static final String TWEET = "tweet";
	public static final String POST = "post";
	public static final String DIRECTMESSAGE = "directmessage";
	public static final String EMAIL = "email";
	public static final String SMS = "sms";
	public static final String TELEGRAM = "telegram";
	public static final String SKYPE = "skype";
	public static final String WECHAT = "wechat";
	public static final String KIK = "kik";
	public static final String FACEBOOKMESSENGER = "facebookmessenger";
	public static final String SLACK = "slack";
	public static final String COMMAND = "command";
	public static final String TIMER = "timer";
	public static final String WYSIWYG = "wysiwyg";
	public static final String TEXTEDITOR= "markup";
	public static final String HTML = "html";
	public static final String ALEXA = "alexa";
	public static final String GOOGLEASSISTANT = "googleAssistant";

	List<Vertex> results;
	List<Vertex> responses;
	List<Vertex> meta;
	List<String> allLabels;
	String duration = "";
	String search = CONVERSATIONS;
	String type = ALL;
	String restriction = "none";
	String sort = DATE;
	String filter = "";
	String editorType;
	
	boolean correction;
	boolean showNext;
	boolean showRepeat;
	boolean showPrevious;
	boolean showKeyWords;
	boolean showRequired;
	boolean showTopic;
	boolean showLabel;
	boolean showEmotes;
	boolean showSentiment;
	boolean showConfidence;
	boolean showActions;
	boolean showPoses;
	boolean showCondition;
	boolean showThink;
	boolean showCommand;
	boolean selectAll;
	boolean isPhrase;
	boolean autoReduce = true;
	boolean showSynonyms;
	
	List<Vertex> allResults;
	List<Vertex> allResponses;
	
	public enum EditorType {WYSIWYG, HTML}

	public ChatLogBean() {
		this.pageSize = 100;
	}
	
	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public void setShowSynonyms(boolean showSynonyms) {
		this.showSynonyms = showSynonyms;
	}
	
	public boolean getShowSynonyms() {
		return showSynonyms;
	}
	
	public void setAutoReduce(boolean autoReduce) {
		this.autoReduce = autoReduce;
	}
	
	public boolean getAutoReduce() {
		return autoReduce;
	}
	public boolean getShowCondition() {
		return showCondition;
	}

	public void setShowCondition(boolean showCondition) {
		this.showCondition = showCondition;
	}

	public boolean getShowThink() {
		return showThink;
	}

	public void setShowThink(boolean showThink) {
		this.showThink = showThink;
	}

	public boolean getShowCommand() {
		return showCommand;
	}

	public void setShowCommand(boolean showCommand) {
		this.showCommand = showCommand;
	}

	public boolean isPhrase() {
		return isPhrase;
	}

	public void setPhrase(boolean isPhrase) {
		this.isPhrase = isPhrase;
	}

	public boolean getShowLabel() {
		return showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}

	public boolean getShowRepeat() {
		return showRepeat;
	}

	public void setShowRepeat(boolean showRepeat) {
		this.showRepeat = showRepeat;
	}

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public boolean getShowRequired() {
		return showRequired;
	}

	public void setShowRequired(boolean showRequired) {
		this.showRequired = showRequired;
	}

	public boolean getShowTopic() {
		return showTopic;
	}

	public void setShowTopic(boolean showTopic) {
		this.showTopic = showTopic;
	}
	
	public boolean getShowNext() {
		return showNext;
	}

	public void setShowNext(boolean showNext) {
		this.showNext = showNext;
	}
	
	public boolean getShowPrevious() {
		return showPrevious;
	}

	public void setShowPrevious(boolean showPrevious) {
		this.showPrevious = showPrevious;
	}
	
	public boolean getShowKeyWords() {
		return showKeyWords;
	}

	public void setShowKeyWords(boolean showKeyWords) {
		this.showKeyWords = showKeyWords;
	}

	public boolean getShowEmotes() {
		return showEmotes;
	}

	public void setShowEmotes(boolean showEmotes) {
		this.showEmotes = showEmotes;
	}
	
	public void setShowSentiment(boolean showSentiment) {
		this.showSentiment = showSentiment;
	}
	
	public boolean getShowSentiment() {
		return showSentiment;
	}
	
	public void setShowConfidence(boolean showConfidence) {
		this.showConfidence = showConfidence;
	}
	
	public boolean getShowConfidence() {
		return showConfidence;
	}

	public boolean getShowActions() {
		return showActions;
	}

	public void setShowActions(boolean showActions) {
		this.showActions = showActions;
	}

	public boolean getShowPoses() {
		return showPoses;
	}

	public void setShowPoses(boolean showPoses) {
		this.showPoses = showPoses;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public boolean isCorrection() {
		return correction;
	}

	public void setCorrection(boolean correction) {
		this.correction = correction;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRestriction() {
		return restriction;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}

	public String getType(Vertex conversation) {
		if (conversation == null) {
			return "";
		}
		Vertex inputType = conversation.getRelationship(Primitive.TYPE);
		if (inputType == null || !(inputType.isPrimitive())) {
			return "Chat";
		}
		return Utils.capitalize(((Primitive)inputType.getData()).getIdentity());
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
				return String.valueOf(vertex.getData());
			}
			return vertex.getId().toString();
		}
		return (String)name.getData();
	}
	
	public String isEditorTypeSelected(String editorType) {
		if (editorType.equals(getEditorType())) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String getEditorType() {
		if (this.editorType == null) {
			if (loginBean.isMobile()) {
				this.editorType = TEXTEDITOR;
			} else {
				this.editorType = WYSIWYG;
			}
		}
		return editorType;
	}

	public void setEditorType(String editorType) {
		this.editorType = editorType;
	}
	
	public String getDurationCheckedString(String duration) {
		if (duration.equals(this.duration)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getTypeCheckedString(String type) {
		if (type.equals(this.type)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getRestrictCheckedString(String restriction) {
		if (restriction.equals(this.restriction)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getSortCheckedString(String sort) {
		if (sort.equals(this.sort)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getSearchCheckedString(String type) {
		if (type.equals(this.search)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getResponse(Vertex phrase) {
		return Utils.escapeHTML(phrase.printString());
	}
	
	public String getCondition(Vertex template, boolean html) {
		String text = template.printString();
		if (text.startsWith("Template(\"{if (true == (")) {
			text = text.substring("Template(\"{if (true == (".length(), text.length());
			if (text.endsWith(")) { \"true\" } else { \"false\" }}\")")) {
				text = text.substring(0, text.length() - ")) { \"true\" } else { \"false\" }}\")".length());
			}
		}
		if (!html) {
			return text;
		}
		return Utils.escapeHTML(text);
	}
	
	public String getThink(Vertex template, boolean html) {
		String text = template.printString();
		if (text.startsWith("Template(\"{think {")) {
			text = text.substring("Template(\"{think {".length(), text.length());
			if (text.endsWith("}}\")")) {
				text = text.substring(0, text.length() - "}}\")".length());
			}
		}
		if (!html) {
			return text;
		}
		return Utils.escapeHTML(text);
	}
	
	public String getCommand(Vertex template, boolean html) {
		String text = template.printString();
		if (text.startsWith("Template(\"{Http.toJSON(")) {
			text = text.substring("Template(\"{Http.toJSON(".length(), text.length());
			if (text.endsWith(")}\")")) {
				text = text.substring(0, text.length() - ")}\")".length());
			}
		}
		if (!html) {
			return text;
		}
		return Utils.escapeHTML(text);
	}

	public void processCancel() {
		this.correction = false;
		this.isPhrase = false;
		this.results = null;
		this.responses = null;
		this.meta = null;
		this.page = 0;
	}
	
	public boolean hasKeyWords(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> keywords = relationship.getMeta().getRelationships(Primitive.KEYWORD);
		if (keywords == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasRequired(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> keywords = relationship.getMeta().getRelationships(Primitive.REQUIRED);
		if (keywords == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasActions(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> emotions = relationship.getMeta().getRelationships(Primitive.ACTION);
		if (emotions == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasPoses(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> poses = relationship.getMeta().getRelationships(Primitive.POSE);
		if (poses == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasNext(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> nextList = relationship.getMeta().getRelationships(Primitive.NEXT);
		if (nextList == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasEmotes(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> emotions = relationship.getMeta().getRelationships(Primitive.EMOTION);
		if (emotions == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasTopic(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> keywords = relationship.getMeta().getRelationships(Primitive.TOPIC);
		if (keywords == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasCondition(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> keywords = relationship.getMeta().getRelationships(Primitive.CONDITION);
		if (keywords == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasThink(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> keywords = relationship.getMeta().getRelationships(Primitive.THINK);
		if (keywords == null) {
			return false;
		}
		return true;
	}
	
	public boolean hasCommand(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return false;
		}
		Collection<Relationship> command = relationship.getMeta().getRelationships(Primitive.COMMAND);
		if (command == null) {
			return false;
		}
		return true;
	}
	
	public String getKeyWords(Vertex question, Vertex answer, Vertex meta) {
		if (question == null || answer == null) {
			return "";
		}
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		List<Relationship> keywords = relationship.getMeta().orderedRelationships(Primitive.KEYWORD);
		if (keywords == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship keyword : keywords) {
			index++;
			if (keyword.getTarget().instanceOf(Primitive.COMPOUND_WORD)) {
				writer.write("\"");
				writer.write(keyword.getTarget().getDataValue());
				writer.write("\"");
			} else {
				writer.write(keyword.getTarget().getDataValue());
			}
			if (index < keywords.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public int getConfidence(Vertex question, Vertex answer) {
		if (question == null || answer == null) {
			return 100;
		}
		Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
		if (relationship == null) {
			return 100;
		}
		return (int) (relationship.getCorrectness() * 100);
	}
	
	public String getRequired(Vertex question, Vertex answer, Vertex meta) {
		if (question == null || answer == null) {
			return "";
		}
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		List<Relationship> required = relationship.getMeta().orderedRelationships(Primitive.REQUIRED);
		if (required == null) {
			return "";
		}
		Vertex requiredText = relationship.getMeta().getRelationship(Primitive.REQUIRED_TEXT);
		if (requiredText != null) {
			return Utils.escapeHTML(requiredText.printString());
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship word : required) {
			index++;
			writer.write(word.getTarget().printString());
			if (index < required.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public List<Relationship> getDefaultResponseRelationships(Vertex response) {
		if (response == null) {
			return null;
		}
		Network memory = response.getNetwork();
		Vertex language = memory.createVertex(getBot().mind().getThought(Language.class).getPrimitive());
		Relationship relationship = language.getRelationship(Primitive.RESPONSE, response);
		if (relationship == null) {
			return null;
		}
		List<Relationship> relationships = new ArrayList<Relationship>(1);
		relationships.add(relationship);
		return relationships;
	}
	
	public List<Relationship> getGreetingRelationships(Vertex response) {
		if (response == null) {
			return null;
		}
		Network memory = response.getNetwork();
		Vertex language = memory.createVertex(getBot().mind().getThought(Language.class).getPrimitive());
		Relationship relationship = language.getRelationship(Primitive.GREETING, response);
		if (relationship == null) {
			return null;
		}
		List<Relationship> relationships = new ArrayList<Relationship>(1);
		relationships.add(relationship);
		return relationships;
	}
	
	public String getTopic(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Collection<Relationship> topics = relationship.getMeta().orderedRelationships(Primitive.TOPIC);
		if (topics == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship topic : topics) {
			index++;
			writer.write(topic.getTarget().printString());
			if (index < topics.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getCondition(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Vertex condition = relationship.getMeta().getRelationship(Primitive.CONDITION);
		if (condition == null) {
			return "";
		}
		return getCondition(condition, true);
	}
	
	public String getThink(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = null;
		relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Vertex condition = relationship.getMeta().getRelationship(Primitive.THINK);
		if (condition == null) {
			return "";
		}
		return getThink(condition, true);
	}
	
	public String getCommand(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = null;
		relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Vertex condition = relationship.getMeta().getRelationship(Primitive.COMMAND);
		if (condition == null) {
			return "";
		}
		return getCommand(condition, true);
	}
	
	/**
	 * Find the correct response relationship for the type of response.
	 * Greetings and default response are indexed off the Language primitive object.
	 */
	public Relationship getResponseRelationship(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = null;
		if (meta != null) {
			Relationship next = meta.getRelationship(Primitive.NEXT, question);
			if (next != null && next.hasMeta()) {
				relationship = next.getMeta().getRelationship(Primitive.RESPONSE, answer);
				return relationship;
			}
			return null;
		}
		if (answer == null && (this.search.equals(GREETINGS) || this.search.equals(DEFAULT))) {
			if (question == null) {
				return null;
			}
			Vertex language = question.getNetwork().createVertex(getBot().mind().getThought(Language.class).getPrimitive());
			if (this.search.equals(GREETINGS)) {
				relationship = language.getRelationship(Primitive.GREETING, question);
			} else if (this.search.equals(DEFAULT)) {
				relationship = language.getRelationship(Primitive.RESPONSE, question);
			}
		} else if (question.isPrimitive() && this.search.equals(GREETINGS)) {
			Vertex language = question.getNetwork().createVertex(getBot().mind().getThought(Language.class).getPrimitive());
			relationship = language.getRelationship(Primitive.GREETING, answer);
		} else {
			if (question == null || answer == null) {
				return null;
			}
			relationship = question.getRelationship(Primitive.RESPONSE, answer);
		}
		return relationship;
	}
	
	public String getLabel(Vertex response) {
		if (response == null) {
			return "";
		}
		Vertex label = response.getRelationship(Primitive.LABEL);
		if (label == null) {
			return "";
		}
		return Utils.escapeHTML(label.printString());
	}
	
	public String getActions(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Collection<Relationship> actions = relationship.getMeta().getRelationships(Primitive.ACTION);
		if (actions == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship action : actions) {
			index++;
			writer.write(action.getTarget().getDataValue());
			if (index < actions.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getPoses(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Collection<Relationship> poses = relationship.getMeta().getRelationships(Primitive.POSE);
		if (poses == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship pose : poses) {
			index++;
			writer.write(pose.getTarget().getDataValue());
			if (index < poses.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getNext(Vertex question, Vertex answer) {
		Relationship relationship = getResponseRelationship(question, answer, null);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Collection<Relationship> nextList = relationship.getMeta().getRelationships(Primitive.NEXT);
		if (nextList == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship next : nextList) {
			index++;
			writer.write(next.getTarget().getDataValue());
			if (index < nextList.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getRequirePreviousChecked(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		if (relationship.getMeta().hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)) {
			return "checked";
		}
		return "";
	}
	
	public String getRequireTopicChecked(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Vertex requireTopic = relationship.getMeta();
		Vertex topicVertex = requireTopic.getRelationship(Primitive.TOPIC);
		if (topicVertex != null && relationship.getMeta().hasRelationship(Primitive.REQUIRE, Primitive.TOPIC)) {
			return "checked";
		}
		return "";
	}
	
	public String getExclusiveTopicChecked(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		Vertex exclusiveTopic = relationship.getMeta();
		Vertex topicVertex = exclusiveTopic.getRelationship(Primitive.TOPIC);
		if (topicVertex != null && topicVertex.hasRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE)) {
			return "checked";
		}
		return "";
	}
	
	public String getWordExclusiveTopicChecked(Vertex word) {
		if (word != null) {
			if (word.hasRelationship(Primitive.INSTANTIATION, Primitive.TOPIC)) {
				if (word.hasRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE)) {
					return "checked";
				}
			}
		}
		return "";
	}
	
	public String getNoRepeatChecked(Vertex question, Vertex answer) {
		if (answer != null) {
			if (answer.hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) {
				return "checked";
			}
		} else if (question != null) {
			if (question.hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) {
				return "checked";
			}
		}
		return "";
	}
	
	public String isWordKeyword(Vertex word) {
		if (word != null) {
			if (word.hasRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD)) {
				return "checked";
			}
		}
		return "";
	}
	
	public String isWordTopic(Vertex word) {
		if (word != null) {
			if (word.hasRelationship(Primitive.INSTANTIATION, Primitive.TOPIC)) {
				return "checked";
			}
		}
		return "";
	}
	
	public String getEmotes(Vertex question, Vertex answer, Vertex meta) {
		Relationship relationship = getResponseRelationship(question, answer, meta);
		if (relationship == null || !relationship.hasMeta()) {
			return "";
		}
		List<EmotionalState> emotes = getBot().mood().evaluateEmotionalStates(relationship.getMeta());
		if (emotes.isEmpty()) {
			return "";
		}
		
		StringWriter writer = new StringWriter();
		int index = 0;
		for (EmotionalState emote : emotes) {
			boolean isSentiment = emote.isSentiment();
			if (isSentiment) {
				continue;
			}
			index++;
			writer.write(emote.name().toLowerCase());
			if (index < emotes.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getActions(Vertex input) {
		Collection<Relationship> actions = input.getRelationships(Primitive.ACTION);
		if (actions == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship action : actions) {
			index++;
			writer.write(action.getTarget().getDataValue());
			if (index < actions.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getPoses(Vertex input) {
		List<Relationship> poses = input.orderedRelationships(Primitive.POSE);
		if (poses == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship pose : poses) {
			index++;
			writer.write(pose.getTarget().getDataValue());
			if (index < poses.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getCommand(Vertex input) {
		Collection<Relationship> commands = input.getRelationships(Primitive.COMMAND);
		if (commands == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship command : commands) {
			index++;
			writer.write(command.getTarget().getDataValue());
			if (index < commands.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getEmotes(Vertex input) {
		List<EmotionalState> emotes = getBot().mood().evaluateEmotionalStates(input);
		StringWriter writer = new StringWriter();
		int index = 0;
		for (EmotionalState emote : emotes) {
			boolean isSentiment = emote.isSentiment();
			if (isSentiment) {
				continue;
			}
			index++;
			writer.write(emote.name().toLowerCase());
			if (index < emotes.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getSentiment(Vertex input) {
		List<EmotionalState> sentiments = getBot().mood().evaluateEmotionalStates(input);
		StringWriter writer = new StringWriter();
		int index = 0;
		for (EmotionalState sentiment : sentiments) {
			boolean isSentiment = sentiment.isSentiment();
			if (!isSentiment) {
				continue;
			}
			index++;
			writer.write(sentiment.name().toLowerCase());
			if (index < sentiments.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getTopicValues(Vertex question, Vertex answer) {
		StringWriter writer = new StringWriter();
		if (question != null) {
			Collection<Relationship> words = question.getRelationships(Primitive.WORD);
			if (words != null) {
				int count = 0;
				for (Relationship word : words) {
					if (word.getTarget().instanceOf(Primitive.TOPIC)) {
						if (count > 0) {
							writer.write(", ");
						}
						writer.write("\"");
						writer.write(Encode.forJavaScriptBlock(word.getTarget().printString()));
						writer.write("\"");
						count++;
					}
				}
			}
		}
		if (answer != null) {
			Collection<Relationship> words = answer.getRelationships(Primitive.WORD);
			if (words != null) {
				int count = 0;
				for (Relationship word : words) {
					if (word.getTarget().instanceOf(Primitive.TOPIC)) {
						if (count > 0) {
							writer.write(", ");
						}
						writer.write("\"");
						writer.write(Encode.forJavaScriptBlock(word.getTarget().printString()));
						writer.write("\"");
						count++;
					}
				}
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	@SuppressWarnings("unchecked")
	public String getLabelValues() {
		StringWriter writer = new StringWriter();
		if (this.allLabels == null) {
			Network memory = getBot().memory().newMemory();
			Vertex instantiation = memory.createVertex(Primitive.INSTANTIATION);
			Vertex label = memory.createVertex(Primitive.LABEL);
			List<Vertex> results = memory.findAllQuery("Select v from Vertex v join v.allRelationships r where r.type.id = " + instantiation.getId() + " and r.target.id = " + label.getId() + " order by v.dataValue");
			this.allLabels = new ArrayList<String>();
			for (Vertex result : results) {
				this.allLabels.add(result.printString());
			}
		}
		int count = 0;
		for (String label : this.allLabels) {
			if (count > 0) {
				writer.write(", ");
			}
			writer.write("\"");
			writer.write(Encode.forJavaScriptBlock(label));
			writer.write("\"");
			count++;
		}
		return writer.toString();
	}
	
	public String getKeyWordValues(Vertex question) {
		StringWriter writer = new StringWriter();
		if (question != null) {
			Collection<Relationship> words = question.getRelationships(Primitive.WORD);
			if (words != null) {
				int count = 0;
				for (Relationship word : words) {
					if (word.getTarget().instanceOf(Primitive.KEYWORD)) {
						if (count > 0) {
							writer.write(", ");
						}
						writer.write("\"");
						writer.write(Encode.forJavaScriptBlock(word.getTarget().printString()));
						writer.write("\"");
						count++;
					}
				}
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getWordSynonyms(Vertex question) {
		if (question == null) {
			return "";
		}
		Collection<Relationship> synonyms = question.orderedRelationships(Primitive.SYNONYM);
		if (synonyms == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int index = 0;
		for (Relationship synonym : synonyms) {
			index++;
			if (synonym.getTarget().instanceOf(Primitive.COMPOUND_WORD)) {
				writer.write("\"");
				writer.write(synonym.getTarget().getDataValue());
				writer.write("\"");
			} else {
				writer.write(synonym.getTarget().getDataValue());
			}
			if (index < synonyms.size()) {
				writer.write(" ");
			}
		}
		return Utils.escapeHTML(writer.toString());
	}
	
	public String getRequiredValues(Vertex question) {
		StringWriter writer = new StringWriter();
		if (question != null) {
			List<Relationship> words = question.orderedRelationships(Primitive.WORD);
			if (words != null) {
				int count = 0;
				for (Relationship word : words) {
					if (count > 0) {
						writer.write(", ");
					}
					writer.write("\"");
					writer.write(Encode.forJavaScriptBlock(word.getTarget().printString()));
					writer.write("\"");
					count++;
				}
			}
		}
		return writer.toString();
	}

	public void processSave(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> inputs = lookupVertices(request, memory);
		Map<Vertex, Vertex> phraseMap = new HashMap<Vertex, Vertex>();
		for (Object[] data : inputs) {
			if (this.search.equals(CONVERSATIONS) && !this.isPhrase) {
				if (data.length != 4) {
					continue;
				}
				Vertex input = (Vertex)data[1];
				String answerText = (String)data[3];
				boolean usePrevious = "on".equals(request.getParameter("useprevious:" + input.getId()));
				boolean requirePrevious = "on".equals(request.getParameter("require-previous:" + input.getId()));
				boolean invalidate = "on".equals(request.getParameter("invalidate:" + input.getId()));
				boolean noRepeat = "on".equals(request.getParameter("norepeat:" + input.getId()));
				boolean comprehension = "on".equals(request.getParameter("comprehension:" + input.getId()));
				String keywords = request.getParameter("keywords:" + input.getId());
				String required = request.getParameter("required:" + input.getId());
				String onrepeat = request.getParameter("onrepeat:" + input.getId());
				String next = request.getParameter("next:" + input.getId());
				boolean autoReduce = "on".equals(request.getParameter("auto-reduce:" + input.getId()));
				if (onrepeat != null) {
					onrepeat = onrepeat.trim();
				}
				String topic = request.getParameter("topic:" + input.getId());
				if (topic != null) {
					topic = topic.trim();
				}
				boolean requireTopic = "on".equals(request.getParameter("require-topic:" + input.getId()));
				boolean exclusiveTopic = "on".equals(request.getParameter("exclusive-topic:" + input.getId()));
				String label = request.getParameter("label:" + input.getId());
				if (label != null) {
					label = label.trim();
					if (label.startsWith("#")) {
						label = label.substring(1, label.length());
					}
					if (label.isEmpty()) {
						label = null;
					} else {
						if (!Utils.isAlphaNumeric(label)) {
							throw new BotException("A label must be a single alpha numeric string with no spaces (use - for a space) - " + label);
						}
					}
				}
				String emotes = request.getParameter("emotes:" + input.getId());
				String actions = request.getParameter("actions:" + input.getId());
				String poses = request.getParameter("poses:" + input.getId());
				String condition = request.getParameter("condition:" + input.getId());
				String think = request.getParameter("think:" + input.getId());
				String command = request.getParameter("command:" + input.getId());
				Vertex response = input.getRelationship(Primitive.INPUT);
				Vertex questionInput = input.getRelationship(Primitive.QUESTION);
				if ((questionInput == null) || (response == null)) {
					continue;
				}
				if (answerText == null) {
					continue;
				}
				answerText = answerText.trim();
				if (answerText.trim().isEmpty()) {
					continue;
				}
				Vertex question = questionInput.getRelationship(Primitive.INPUT);
				if (question == null) {
					continue;
				}
				if (autoReduce && !question.getDataValue().startsWith("Pattern(")) {
					setAutoReduce(true);
					question = memory.createSentence(Utils.reduce(question.getDataValue()));
				} else {
					setAutoReduce(false);
				}
				if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(answerText, getBotBean().getInstance().getContentRatingLevel())) {
					throw BotException.offensive();
				}
				if (!this.loginBean.isSuper() && !getBotBean().getInstance().getAllowJavaScript()) {
					Utils.checkScript(answerText);
				}
				Vertex previousInput = null;
				Vertex previous = null;
				if (usePrevious) {
					previousInput = questionInput.getRelationship(Primitive.QUESTION);
					if (previousInput != null) {
						previous = previousInput.getRelationship(Primitive.INPUT);
					}
				}
				Vertex answer = null;
				boolean labelAnswer = false;
				if (answerText.startsWith("#")) {
					answer = createLabel(answerText, memory);
					labelAnswer = true;
				} else if (answerText.contains("{") && answerText.contains("}")
							&& !answerText.toLowerCase().startsWith("template") && !answerText.toLowerCase().startsWith("formula")) {
					answer = memory.createTemplate("Template(\"" + answerText + "\")");
				} else {
					answer = memory.createSentence(answerText);
					SelfCompiler.getCompiler().pin(answer);
				}
				if (noRepeat) {
					answer.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				}
				if (onrepeat != null && !onrepeat.trim().isEmpty()) {
					Vertex onrepeatValue = memory.createSentence(onrepeat);
					answer.addRelationship(Primitive.ONREPEAT, onrepeatValue);
					SelfCompiler.getCompiler().pin(onrepeatValue);
				}
				Vertex nextResponse = null;
				if (next != null && !next.isEmpty()) {
					if (next.startsWith("#")) {
						nextResponse = createLabel(next, memory);
					} else {
						nextResponse = memory.createSentence(next);
						SelfCompiler.getCompiler().pin(nextResponse);
					}
				}
				Language.addCorrection(questionInput, question, (invalidate ? input : null), answer, previousInput, memory);
				Language.addSentencePreviousMeta(question, answer, previous, requirePrevious, memory);
				Language.addSentenceNextMeta(question, answer, nextResponse, memory);
				Language.addSentenceKeyWordsMeta(question, answer, keywords, memory);
				Language.addSentenceRequiredMeta(question, answer, required, memory);
				Language.addSentenceTopicMeta(question, answer, topic, requireTopic, exclusiveTopic, memory);
				Language.addSentenceConditionMeta(question, answer, condition, true, memory);
				Language.addSentenceThinkMeta(question, answer, think, true, memory);
				Language.addSentenceCommandMeta(question, answer, command, true, memory);
				Language.addSentenceEmotesMeta(question, answer, emotes, memory);
				Language.addSentenceActionMeta(question, answer, actions, memory);
				Language.addSentencePoseMeta(question, answer, poses, memory);
				Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
				if (label != null && !labelAnswer) {
					Vertex labelVertex = memory.createVertex(new Primitive(label));
					if (!answer.hasRelationship(Primitive.LABEL, labelVertex)) {
						labelVertex.addRelationship(Primitive.INSTANTIATION, Primitive.LABEL);
						answer.setRelationship(Primitive.LABEL, labelVertex);
						labelVertex.setRelationship(Primitive.RESPONSE, answer);
						this.allLabels = null;
					}
				} else {
					Vertex oldLabel = answer.getRelationship(Primitive.LABEL);
					if (oldLabel != null) {
						oldLabel.internalRemoveRelationship(oldLabel.getRelationship(Primitive.INSTANTIATION, Primitive.LABEL));
						oldLabel.internalRemoveRelationship(oldLabel.getRelationship(Primitive.RESPONSE, answer));
						answer.internalRemoveRelationships(Primitive.LABEL);
					}
				}
				if (relationship != null) {
					// 100%
					relationship.setCorrectness(Math.max(1.0f, relationship.getCorrectness()));
				}
				
				if (comprehension) {
					StringWriter writer = new StringWriter();
					String name = null;
					if ((previousInput != null) && (previous != null)) {
						Vertex speaker = previousInput.getRelationship(Primitive.SPEAKER);
						name = getName(speaker);
						if (speaker.is(Primitive.SELF)) {
							name = "self";
						}
						writer.write(name);
						writer.write(": ");
						writer.write(previous.printString());
						writer.write("\n");
					}
					Vertex speaker = questionInput.getRelationship(Primitive.SPEAKER);
					name = getName(speaker);
					if (speaker.is(Primitive.SELF)) {
						name = "self";
					}
					writer.write(name);
					writer.write(": ");
					writer.write(question.printString());
					writer.write("\n");
					speaker = input.getRelationship(Primitive.SPEAKER);
					name = getName(speaker);
					if (speaker.is(Primitive.SELF)) {
						name = "self";
					}
					writer.write(name);
					writer.write(": ");
					writer.write(answerText);
					writer.write("\n");
					memory.save();
					ResponseListParser.parser().processChatLog(writer.toString(), true, false, false, getBot());
				}
			} else if ((this.isPhrase || this.search.equals(RESPONSES) || this.search.equals(PHRASES) || this.search.equals(GREETINGS) || this.search.equals(DEFAULT)) && !this.search.equals(WORDS)) {
				if ((data.length == 4) && (data[3] instanceof String)) {
					Vertex question = (Vertex)data[1];
					String id = "" + question.getId();
					Vertex response = (Vertex)data[2];
					if (response != null) {
						id = id + ":" + response.getId();
					}
					String newQuestion = request.getParameter("question:" + id);
					String next = request.getParameter("next:" + id);
					String previous = request.getParameter("previous:" + id);
					String keywords = request.getParameter("keywords:" + id);
					String required = request.getParameter("required:" + id);
					String onrepeat = request.getParameter("onrepeat:" + id);
					String condition = request.getParameter("condition:" + id);
					String think = request.getParameter("think:" + id);
					String command = request.getParameter("command:" + id);
					String confidence = request.getParameter("confidence:" + id);
					String sentiment = request.getParameter("sentiment:" + id);
					String emotes = request.getParameter("emotes:" + id);
					String actions = request.getParameter("actions:" + id);
					String poses = request.getParameter("poses:" + id);
					String respEmotes = request.getParameter("resp-emotes:" + id);
					String respActions = request.getParameter("resp-actions:" + id);
					String respPoses = request.getParameter("resp-poses:" + id);
					String metaId = request.getParameter("metaid:" + id);
					boolean noRepeat = "on".equals(request.getParameter("norepeat:" + id));
					boolean autoReduce = "on".equals(request.getParameter("auto-reduce:" + id));
					boolean requireTopic = "on".equals(request.getParameter("require-topic:" + id));
					boolean exclusiveTopic = "on".equals(request.getParameter("exclusive-topic:" + id));
					boolean requirePrevious = "on".equals(request.getParameter("require-previous:" + id));
					String topic = request.getParameter("topic:" + id);
					String label = request.getParameter("label:" + id);
					String correction = (String)data[3];
					
					this.processSaveData(question, response, newQuestion, next, previous, keywords, required, onrepeat, condition, think, command, confidence, sentiment, emotes, respActions, poses, respEmotes, respActions, respPoses, metaId, topic, label, correction, noRepeat, autoReduce, requireTopic, exclusiveTopic, requirePrevious, "", "");
					
				} else if (data.length == 3) {
					String type = (String)data[0];
					Vertex phrase = (Vertex)data[1];
					if (phraseMap.containsKey(phrase)) {
						phrase = phraseMap.get(phrase);
					}
					if (phrase.getData() == null || phrase.getData().equals("")) {
						continue;
					}
					String text = (String)data[2];
					if (type.equals("emotes")) {
						phrase.internalRemoveRelationships(Primitive.EMOTION);
						for (String emote : Utils.getWords(text)) {
							if (!emote.equals("none")) {
								try {
									EmotionalState.valueOf(emote.toUpperCase()).apply(phrase);
								} catch (Exception exception) {
									throw new BotException("Invalid emotion: " + emote);
								}
							}
						}
					} else if (type.equals("sentiment")) {
						for (String emote : Utils.getWords(text)) {
							if (!emote.equals("none")) {
								try {
									EmotionalState.valueOf(emote.toUpperCase()).apply(phrase);
								} catch (Exception exception) {
									throw new BotException("Invalid sentiment: " + emote);
								}
							}
						}
					} else if (type.equals("actions")) {
						phrase.internalRemoveRelationships(Primitive.ACTION);
						for (String action : Utils.getWords(text)) {
							if (!action.equals("none")) {
								phrase.addRelationship(Primitive.ACTION, new Primitive(action));
							}
						}
					} else if (type.equals("poses")) {
						phrase.internalRemoveRelationships(Primitive.POSE);
						for (String pose : Utils.getWords(text)) {
							if (!pose.equals("none")) {
								phrase.addRelationship(Primitive.POSE, new Primitive(pose));
							}
						}
					}
				}
			}  else if (this.search.equals(WORDS)) {
				if (data.length == 3) {
					Vertex word = (Vertex)data[1];
					String text = (String)data[2];
					String id = "" + word.getId();
					if (!data[0].equals(WORDS)) {
						continue;
					}
					word  = memory.createWord(text);
					SelfCompiler.getCompiler().pin(word);
					String emote = request.getParameter("emotes:" + id);
					String sentiment = request.getParameter("sentiment:" + id);
					String wordKeyword = request.getParameter("word-keyword:" + id);
					String synonym = request.getParameter("synonym:" + id);
					boolean exclusiveTopic = "on".equals(request.getParameter("exclusive-topic:" + id));
					boolean wordTopic = "on".equals(request.getParameter("word-topic:" + id));
					
					if (wordKeyword != null) {
						word.addRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
					} else {
						word.internalRemoveRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
					}
					
					if (wordTopic || exclusiveTopic) {
						word.addRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
						if (exclusiveTopic) {
							word.addRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
						} else {
							word.internalRemoveRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
						}
					} else {
						word.internalRemoveRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
						word.internalRemoveRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
					}
			
					if (synonym != null) {
						synonym = synonym.trim();
						word.internalRemoveRelationships(Primitive.SYNONYM);
						TextStream stream = new TextStream(synonym);
						while (!stream.atEnd()) {
							String wordSynonym = stream.nextWord();
							if (wordSynonym == null) {
								break;
							}
							// Ignore ,
							if (wordSynonym.equals(",")) {
								continue;
							}
							Vertex newSynonymVertex = null;
							if (wordSynonym.equals("\"")) {
								// Support compound keywords.
								wordSynonym = stream.nextQuotes();
							}
							newSynonymVertex = memory.createWord(wordSynonym);
							word.addRelationship(Primitive.SYNONYM, newSynonymVertex);
							newSynonymVertex.addRelationship(Primitive.SYNONYM, word);
							newSynonymVertex.setPinned(true);
						}
					}
					
					word.internalRemoveRelationships(Primitive.EMOTION);
					if (emote != null) {
						emote = emote.trim();
						if (!emote.equals("") && !emote.equals("none")) {
							for (String emotion : Utils.getWords(emote)) {
								try {
									EmotionalState.valueOf(emotion.toUpperCase()).apply(word);
								} catch (Exception exception) {
									throw new BotException("Invalid emotion: " + emote);
								}
							}
						}
					}
					if (sentiment != null) {
						sentiment = sentiment.trim();
						if (!sentiment.equals("") && !sentiment.equals("none")) {
							try {
								EmotionalState.valueOf(sentiment.toUpperCase()).apply(word);
							} catch (Exception exception) {
								throw new BotException("Invalid sentimet: " + sentiment);
							}
						}
					}
				}
			}
		}
		memory.save();
		this.correction = false;
		this.isPhrase = false;
		this.results = null;
		this.responses = null;
		this.meta = null;
	}
	
	public String displayNextResponseTable(int counter, Relationship responseRelationship, long previousNextTargetId) {
		StringWriter writer = new StringWriter();
		writeNextResponseTable(writer, counter, responseRelationship, previousNextTargetId);
		return writer.toString();
	}

	public void writeNextResponseTable(StringWriter writer, int counter, Relationship responseRelationship, long previousNextTargetId) {
		if (responseRelationship.getMeta() == null || counter > 100) {
			return;
		}
		String questionTitle = this.loginBean.translate("Select next question to edit, or for validation, invalidation, or deletion");
		String responseTitle = this.loginBean.translate("Select next response to edit, or for validation, invalidation, or deletion");
		String tableStyle = "border-style:solid;border-color:black;border-width:1px;display:none;";
		List<Relationship> nextRelationships = responseRelationship.getMeta().orderedRelationships(Primitive.NEXT);
		if (nextRelationships != null) {
			writer.write("<table id='table-meta-" + responseRelationship.getMeta().getId() + "' style='" + tableStyle + "'>\n");
			int nextIndex = 0;
			for (Relationship next : nextRelationships) {
				String style = "chat";
				if (next.getCorrectness() < 0) { style = "chat-inverse"; };
				
				writer.write("<tr id='question-row-" + responseRelationship.getMeta().getId() + "-" + String.valueOf(next.getTarget().getId()) + "'><td><table>");
				
				writer.write("<tr id='question-row-tr-" + responseRelationship.getMeta().getId() + "-" + String.valueOf(next.getTarget().getId()) + "'>\n");
				
				writer.write("<td></td>\n");
				
				writer.write("<td valign='top'>");
				writer.write("<input type='checkbox' name='next:" + String.valueOf(responseRelationship.getMeta().getId()) + ":" + String.valueOf(next.getTarget().getId()) + "' title='" + questionTitle + "'/>");
				writer.write("</td>\n");
				writer.write("<td>\n");
				writer.write("<span class='chat'>%</span>\n");
				writer.write("</td>\n");
				writer.write("<td style='width:100%;' id='question-td-id-" + responseRelationship.getMeta().getId() + "-" + next.getTarget().getId()  + "'><span class='" + style + "'>" + getResponse(next.getTarget()) + "</span></td>\n");
				writer.write("</tr>\n");
				if (next.getMeta() != null) {
					
					List<Relationship> nextResponseList = next.getMeta().orderedRelationships(Primitive.RESPONSE);
					if (nextResponseList != null && !nextResponseList.isEmpty()) {
						for (Relationship nextResponse : nextResponseList) {
							String responseId = String.valueOf(responseRelationship.getMeta().getId()) + "-" + String.valueOf(next.getTarget().getId()) + "-" + String.valueOf(nextResponse.getTarget().getId());
							writer.write("<tr id='question-response-row-" + responseId + "'>");
							writer.write("<td valign='top'>");					
							writer.write("<div class='toolbar'>");
							writer.write("<span class='dropt'>");	
							writer.write("<div class='menu' style='display:inline-flex;margin-top:-11px;height:40px;'>");
							writer.write("<div class='gear-menu'>");
							writer.write("<a href='#' id='add-question-response-" + responseId + "' onclick='showQuickAddNextResponse(this); return false;' title='" + loginBean.translate("Enter a new follow-up next question and response") + "'>");
							writer.write("<img src='images/plus.png' class='gear-menu' style='width:32px;max-width:none;'>");
							writer.write("</a></div><div class='gear-menu'>");
							writer.write("<a href='#' id='edit-question-response-" + responseId + "' onclick='editQuestionResponse(this); return false;' title='" + loginBean.translate("Edit the question and response") + "'>");
							writer.write("<img src='images/edit.png' class='gear-menu' style='width:32px;max-width:none;'>");
							writer.write("</a></div><div class='gear-menu'>");
							writer.write("<a href='#' id='delete-question-response-" + responseId + "' onclick='deleteQuestionResponse(this); return false;' title='" + loginBean.translate("Delete the response") + "'>");
							writer.write("<img src='images/remove.png' class='gear-menu' style='width:32px;max-width:none;'>");
							writer.write("</a></div></div>");
							writer.write("<img src=images/admin.svg class='gear-icon'>");
							writer.write("</span></div>");						
							writer.write("</td>");
							writer.write("<td valign='top'>");						
							writer.write("<input type=checkbox name='next-response:" + String.valueOf(responseRelationship.getMeta().getId()) + ":" + String.valueOf(next.getTarget().getId()) + ":" + String.valueOf(nextResponse.getTarget().getId()) + "' title='" + responseTitle + "'></td>");
							writer.write("<td valign='top'><span class='chat'>" + (int)(nextResponse.getCorrectness() * 100) + "</span></td>");
							writer.write("<td valign='top' style='width:100%;' id='response-td-id-" + responseRelationship.getMeta().getId() + "-" + next.getTarget().getId()  + "-" + nextResponse.getTarget().getId() +  "'><span class='chat-response'>" + getResponse(nextResponse.getTarget()) + "</span>");
							
							writeMetaDataTable(writer, next.getTarget(), nextResponse, responseRelationship.getMeta(), responseId);
							
							if (nextResponse.hasMeta() && nextResponse.getMeta().orderedRelationships(Primitive.NEXT) != null) {
								writer.write("</td></tr><tr id='tr-toggle-parent-" + responseRelationship.getMeta().getId() + "-" + String.valueOf(next.getTarget().getId()) + "-" + String.valueOf(nextResponse.getTarget().getId()) + "'><td></td><td></td><td></td><td id='td-toggle-parent-" + String.valueOf(responseRelationship.getMeta().getId()) + "-" + String.valueOf(next.getTarget().getId()) + "-" + String.valueOf(nextResponse.getTarget().getId()) + "'>");
								writer.write("<span><img src='images/circle-plus.png' class='menu-small' id='expand-table-button-" + nextResponse.getMeta().getId() + "'></span>");
								writer.write("<script>");
								writer.write("$(function() {");
								writer.write("$('#expand-table-button-" + nextResponse.getMeta().getId() + "').click(function() {");
								writer.write("$('#table-meta-" + nextResponse.getMeta().getId() + "').toggle();");
								writer.write("var src = ($(this).attr('src') === 'images/circle-plus.png') ? 'images/circle-minus.png' : 'images/circle-plus.png';");
								writer.write("$('#expand-table-button-" + nextResponse.getMeta().getId() + "').attr('src', src);");
								writer.write("});");
								writer.write("});");
								writer.write("</script>");
								writeNextResponseTable(writer, counter + 1, nextResponse, previousNextTargetId);
							}
							writer.write("</td>");
							writer.write("</tr>");
						}
					}
				}
				
				if(nextIndex < nextRelationships.size() - 1) {
					writer.write("<tr><td colspan='4'><hr/></td></tr>");
				}
				
				writer.write("</table></td></tr>");
				
				nextIndex++;
			}
			writer.write("</table>\n");
		}
	}
	
	public Vertex createLabel(String label, Network memory) {
		label = label.substring(1, label.length());
		if (!Utils.isAlphaNumeric(label)) {
			throw new BotException("A label must be a single alpha numeric string with no spaces (use - for a space) - " + label);
		}
		Vertex vertex = memory.createVertex(new Primitive(label));
		if (!vertex.hasRelationship(Primitive.INSTANTIATION, Primitive.LABEL)) {
			throw new BotException("Missing label - #" + label);
		}
		return vertex;
	}
	
	/**
	 * Process the save of a response config object (from REST API).
	 */
	public ResponseConfig processSave(ResponseConfig config) {
		Network memory = getBot().memory().newMemory();
		Vertex question = null;
		
		String originalMetaId = config.metaId;
		
		// Check for existing question (update).
		if (config.questionId != null && !config.questionId.isEmpty()) {
			long id = 0;
			try {
				id = Long.valueOf(config.questionId);
			} catch (Exception invalid) {
				throw new BotException("Invalid question id: " + config.questionId);
			}
			question = memory.findById(id);
			if (question == null) {
				throw new BotException("Missing question: " + config.questionId);
			}
		}

		Vertex response = null;
		// Check for existing response (update).
		if (config.responseId != null && !config.responseId.isEmpty()) {
			long id = 0;
			try {
				id = Long.valueOf(config.responseId);
			} catch (Exception invalid) {
				throw new BotException("Invalid response id: " + config.responseId);
			}
			response = memory.findById(id);
			if (response == null) {
				throw new BotException("Missing response: " + config.responseId);
			}
		}
		
		if(question == null) {
			question = memory.createVertex("");
		}

		if(config.type == null) {
			AdminDatabase.instance().log(Level.INFO, "processSave: config.type is null");
		}
		else {
			setSearchFromConfigType(config);
		}

		// Add the response to the question.
		if ((config.parentQuestionId != null && !config.parentQuestionId.isEmpty()) && 
				(config.parentResponseId != null && !config.parentResponseId.isEmpty())) {

			// Create a nested next response.
			long parentQuestionId = 0;
			long parentResponseId = 0;
			long parentMetaId = 0;
			try {
				parentQuestionId = Long.valueOf(config.parentQuestionId);
			} catch (Exception invalid) {
				throw new BotException("Invalid parent question id: " + config.parentQuestionId);
			}
			Vertex parentQuestion = memory.findById(parentQuestionId);
			if (parentQuestion == null) {
				throw new BotException("Missing parent question: " + config.parentQuestionId);
			}

			try {
				parentResponseId = Long.valueOf(config.parentResponseId);
			} catch (Exception invalid) {
				throw new BotException("Invalid parent response id: " + config.parentResponseId);
			}
			Vertex parentResponse = memory.findById(parentResponseId);
			Vertex parentMeta = null;
			if (config.metaId != null && !config.metaId.isEmpty()) {
				try {
					parentMetaId = Long.valueOf(config.metaId);
				} catch (Exception invalid) {
					throw new BotException("Invalid meta id: " + config.metaId);
				}
				parentMeta = memory.findById(parentMetaId);
				if (parentMeta == null) {
					throw new BotException("Missing parent meta: " + config.metaId);
				}
			}
			Relationship relationship = null;
			if (parentMeta == null) {
				if(!config.parentQuestionId.equals(config.parentResponseId)) {
					relationship = parentQuestion.getRelationship(Primitive.RESPONSE, parentResponse);
				}
				else {
					//If parentQuestionId and parentResponseId are the same, must be greeting or default response.
					Vertex language = memory.createVertex(Language.class);
					relationship = language.getRelationship(Primitive.GREETING, parentQuestion);
					if(relationship == null) {
						relationship = language.getRelationship(Primitive.RESPONSE, parentQuestion);
						if(relationship == null) {
							relationship = parentQuestion.getRelationship(Primitive.RESPONSE, parentResponse);
						}
					}
				}	
			} else {
				Relationship nextRelationship = parentMeta.getRelationship(Primitive.NEXT, parentQuestion);
				if (nextRelationship == null || nextRelationship.getMeta() == null) {
					//throw new BotException("Missing parent meta response: " + config.parentResponseId);
				} else {
					relationship = nextRelationship.getMeta().getRelationship(Primitive.RESPONSE, parentResponse);
				}
			}
			if (relationship == null) {
				//throw new BotException("Missing parent response: " + config.parentResponseId);
			} else {
				Vertex meta = memory.createMeta(relationship);
				config.metaId = String.valueOf(meta.getId());
			}
		}
		memory.save();
		
		ResponseConfig responseConfig = this.processSaveData(
				question, 
				response, 
				config.question, 
				config.next, 
				config.previous, 
				config.keywords, 
				config.required, 
				config.onRepeat, 
				config.condition, 
				config.think, 
				config.command, 
				config.correctness, 
				config.sentiment, 
				"", 
				"", 
				"", 
				config.emotions, 
				config.actions,
				config.poses, 
				config.metaId, 
				config.topic, 
				config.label, 
				config.response, //correction
				config.noRepeat != null ? config.noRepeat : false,
				config.autoReduce != null ? config.autoReduce : false, 
				config.requireTopic != null ? config.requireTopic : false, 
				config.exclusiveTopic != null ? config.exclusiveTopic : false, 
				config.requirePrevious != null ? config.requirePrevious : false,
				config.parentQuestionId,
				config.parentResponseId);
		
		responseConfig.parentQuestionId = config.parentQuestionId;
		responseConfig.parentResponseId = config.parentResponseId;
		
		memory.save();
		memory = getBot().memory().newMemory();
		
		//Root Level
		if ((config.parentQuestionId == null || config.parentQuestionId.isEmpty()) && 
				(config.parentResponseId == null || config.parentResponseId.isEmpty())) {
			question = memory.findById(Long.valueOf(responseConfig.questionId));
			responseConfig.displayHTML = this.displayQuestionVertexHTML(question);
		}
		else {
			question = memory.findById(Long.valueOf(responseConfig.parentQuestionId));
			List<Relationship> responses = null;
			
			//First Level Nested Responses
			if (originalMetaId == null || originalMetaId.isEmpty() || originalMetaId.equals(responseConfig.metaId)) {
				responses = question.orderedRelationships(Primitive.RESPONSE);
				if (responses == null) {
					responses = getDefaultResponseRelationships(question);
				}
				if (responses == null) {
					responses = getGreetingRelationships(question);
				}
			}
			
			//Second Level and Further Nested Responses
			if (responses == null) {
				question = memory.findById(Long.valueOf(originalMetaId));
				List<Relationship> nextRelationships = question.orderedRelationships(Primitive.NEXT);		
				if(nextRelationships != null) {
					responses = new ArrayList<Relationship>();
					for(Relationship next : nextRelationships) {
						if(next.hasMeta()) {
							responses.addAll(next.getMeta().orderedRelationships(Primitive.RESPONSE));
						}
					}
				}
			}
			
			if(responses != null) {
				for(Relationship r : responses) {
					if(r.getMeta() != null && r.getMeta().getId().toString().equals(responseConfig.metaId)) {
						responseConfig.displayHTML = this.displayNextResponseTable(0, r, 0);
						break;
					}
				}
			}
		}
	
		return responseConfig;
	}
	
	public boolean checkFilter(Vertex question, Relationship response) {
		if (this.search.equals(PHRASES)) {
			return false;
		}
		Vertex phrase = question;
		if (response != null) {
			phrase = response.getTarget();
		}
		if (this.filter != null) {
			String filter = this.filter;
			if (filter.indexOf('%') != -1) {
				filter =  filter.substring(0, filter.indexOf('%'));
			}
			if (!this.filter.isEmpty()) {
				if (this.restriction == null || this.restriction.isEmpty() || this.restriction.equals("none")) {
					if (phrase.printString().toLowerCase().indexOf(filter.toLowerCase()) == -1) {
						return true;
					}
				} else if (this.restriction.equals("exact")) {
					if (!phrase.printString().equalsIgnoreCase(filter)) {
						return true;
					}
				}
			}
			if (response != null) {
				if (this.restriction.equals("topic") || this.restriction.equals("keyword")
							|| this.restriction.equals("required") || this.restriction.equals("previous")) {
					if (!response.hasMeta()) {
						return true;
					}
					Primitive type = null;
					if (this.restriction.equals("topic")) {
						type = Primitive.TOPIC;
					} else if (this.restriction.equals("keyword")) {
						type = Primitive.KEYWORD;
					} else if (this.restriction.equals("required")) {
						type = Primitive.REQUIRED;
					} else if (this.restriction.equals("previous")) {
						type = Primitive.PREVIOUS;
					}
					Collection<Relationship> values =  response.getMeta().getRelationships(type);
					if (values == null) {
						return true;
					}
					if (filter.isEmpty()) {
						return false;
					}
					for (Relationship value : values) {
						if (value.getTarget().printString().toLowerCase().indexOf(filter.toLowerCase()) != -1) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public List<ConversationConfig> processConversationSearch(ResponseSearchConfig config) {
		reset();
		this.pageSize = 50;
		if (config.responseType != null&& !config.responseType.isEmpty()) {
			this.search = config.responseType;
		}
		if (config.inputType != null&& !config.inputType.isEmpty()) {
			this.type = config.inputType;
		}
		if (config.duration != null&& !config.duration.isEmpty()) {
			this.duration = config.duration;
		}
		if (config.filter != null && !config.filter.isEmpty()) {
			this.filter = config.filter;
		}
		if (config.restrict != null&& !config.restrict.isEmpty()) {
			this.restriction = config.restrict;
		}
		if (config.page != null && !config.page.isEmpty()) {
			this.page = Integer.valueOf(config.page);
		}
		processQuery();		
		List<ConversationConfig> results = new ArrayList<ConversationConfig>();
		if (this.results != null) {
			for (Vertex result : this.results) {
				if (this.search.equals(CONVERSATIONS)) {
					ConversationConfig conversation = new ConversationConfig();
					conversation.id = result.getId().toString();
					conversation.creationDate = result.getCreationDate().toString();
					Vertex type = result.getRelationship(Primitive.TYPE);
					if (type != null) {
						conversation.type = type.getDataValue();
					}
					List<Vertex> inputs = result.orderedRelations(Primitive.INPUT);
					conversation.input = new ArrayList<InputConfig>();
					if (inputs != null) {
						for (Vertex input: inputs) {
							InputConfig inputConfig = new InputConfig();
							inputConfig.id = input.getId().toString();
							inputConfig.creationDate = input.getCreationDate().toString();
							Vertex sentence = input.getRelationship(Primitive.INPUT);
							if (sentence != null) {
								inputConfig.value = sentence.printString();
							}
							Vertex speaker = input.getRelationship(Primitive.SPEAKER);
							if (speaker != null) {
								inputConfig.speaker = getName(speaker);
							}
							Vertex target = input.getRelationship(Primitive.TARGET);
							if (target != null) {
								inputConfig.target = getName(target);
							}
							conversation.input.add(inputConfig);
						}
					}
					results.add(conversation);
				}
			}
		}
		reset();
		return results;
	}

	public ResponseConfig getResponse(ResponseConfig config) {
		Network memory = this.getBot().memory().newMemory();
		Vertex result;
		Vertex foundQuestion = null;
		Vertex foundAnswer = null;
		Vertex foundMeta = null;
		
		if(config.type != null) {
			setSearchFromConfigType(config);
		}
		
		if(config.metaId == null || config.metaId.isEmpty()) {
			result = memory.findById(Long.valueOf(config.questionId));
		} else {
			result = memory.findById(Long.valueOf(config.metaId));
		}
		
		Vertex responseAnswer = memory.findById(Long.valueOf(config.responseId));
		
		ResponseConfig response = new ResponseConfig();
		List<Relationship> responses = new ArrayList<Relationship>();
		
		if(config.type.equals(GREETINGS) || config.type.equals(DEFAULT)) {
			responses.addAll(result.getAllRelationships());
			
			//TODO: Clean up this part, put into function? Duplicated from processSave
			Relationship relationship;
			Vertex language = memory.createVertex(Language.class);
			relationship = language.getRelationship(Primitive.GREETING, result);
			if(relationship == null) {
				relationship = language.getRelationship(Primitive.RESPONSE, result);
			}
			if(relationship != null) {
				foundMeta = relationship.getMeta();
			}
			
		} else {
			responses = result.orderedRelationships(Primitive.RESPONSE);
		}
		
		if(responses == null) {
			responses = result.orderedRelationships(Primitive.NEXT);
		}
		
		for(Relationship answer : responses) {
			if(answer.getTarget() == null || answer.getTarget().getId() == null) {
				continue;
			}
			
			if(answer.getTarget().getId().toString().equals(config.responseId) || (answer.getTarget().getId().toString().equals(config.questionId) /*&& answer.getMeta() != null && answer.getMeta().getRelationship(Primitive.RESPONSE) != null && answer.getMeta().getRelationship(Primitive.RESPONSE).getId().toString().equals(config.responseId)*/)) {
				
				if(answer.getSource().getId().toString().equals(config.metaId) && answer.getMeta() != null) {					
					//Nested Response
					response.questionId = answer.getTarget().getId().toString();
					response.question = answer.getTarget().printString();
					response.responseId = responseAnswer.getId().toString();
					response.response = responseAnswer.printString();
					
					response.parentQuestionId = "";
					response.parentResponseId = "";
					response.metaId = config.metaId;
					
					foundQuestion = answer.getTarget();
					foundAnswer = responseAnswer;
					foundMeta = result;
				} else {
					//Root Level Response
					response.questionId = result.getId().toString();
					response.question = result.printString();
					response.responseId = answer.getTarget().getId().toString();
					response.response = answer.getTarget().printString();
					
					foundQuestion = answer.getSource();
					foundAnswer = answer.getTarget();
					foundMeta = null;
					
				}
				response.correctness = String.valueOf((int)(answer.getCorrectness() * 100));
				break;
				
			} else if(answer.getSource().getId().toString().equals(config.responseId)) {
				//Greeting or Default Response
				response.questionId = result.getId().toString();
				response.question = result.printString();
				response.responseId = answer.getSource().getId().toString();
				response.response = answer.getSource().printString();
				
				foundQuestion = answer.getSource();
				foundAnswer = null;
				foundMeta = null;
				
				break;
			}
		}
		
		String emotes = getEmotes(foundQuestion, foundAnswer, foundMeta);
		if (!emotes.isEmpty()) {
			response.emotions = emotes;
		}
		String actions = getActions(foundQuestion, foundAnswer, foundMeta);
		if (!actions.isEmpty()) {
			response.actions = actions;
		}
		String poses = getPoses(foundQuestion, foundAnswer, foundMeta);
		if (!poses.isEmpty()) {
			response.poses = poses;
		}
		String topic = getTopic(foundQuestion, foundAnswer, foundMeta);
		if(!topic.isEmpty()) {
			response.topic = topic;
		}
		String requireTopic = getRequireTopicChecked(foundQuestion, foundAnswer, foundMeta);
		response.requireTopic = !requireTopic.isEmpty() ? true : null;
		String exclusiveTopic = getExclusiveTopicChecked(foundQuestion, foundAnswer, foundMeta);
		response.exclusiveTopic = !exclusiveTopic.isEmpty() ? true : null;
		String label = getLabel(foundAnswer);
		if(!label.isEmpty()) {
			response.label = label;
		}
		String keywords = getKeyWords(foundQuestion, foundAnswer, foundMeta);
		if(!keywords.isEmpty()) {
			response.keywords = keywords;
		}
		String required = getRequired(foundQuestion, foundAnswer, foundMeta);
		if(!required.isEmpty()) {
			response.required = required;
		}
		String requirePrevious = getRequirePreviousChecked(foundQuestion, foundAnswer, foundMeta);
		response.requirePrevious = !requirePrevious.isEmpty() ? true : null;
		String command = getCommand(foundQuestion, foundAnswer, foundMeta);
		if(!command.isEmpty()) {
			response.command = command;
		}
		String think = getThink(foundQuestion, foundAnswer, foundMeta);
		if(!think.isEmpty()) {
			response.think = think;
		}
		String condition = getCondition(foundQuestion, foundAnswer, foundMeta);
		if(!condition.isEmpty()) {
			response.condition = condition;
		}
		if (foundAnswer != null && foundAnswer.hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) {
			response.noRepeat = true;
		}
		if (foundAnswer != null && foundAnswer.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
			response.flagged = true;
		}
		
		return response;
	}
	
	public List<ResponseConfig> processSearch(ResponseSearchConfig config) {
		reset();
		this.pageSize = 100;
		if (config.responseType != null&& !config.responseType.isEmpty()) {
			this.search = config.responseType;
		}
		if (config.inputType != null&& !config.inputType.isEmpty()) {
			this.type = config.inputType;
		}
		if (config.duration != null&& !config.duration.isEmpty()) {
			this.duration = config.duration;
		}
		if (config.filter != null && !config.filter.isEmpty()) {
			this.filter = config.filter;
		}
		if (config.restrict != null&& !config.restrict.isEmpty()) {
			this.restriction = config.restrict;
		}
		if (config.page != null && !config.page.isEmpty()) {
			this.page = Integer.valueOf(config.page);
		}
		processQuery();
		List<ResponseConfig> results = new ArrayList<ResponseConfig>();
		if (this.results != null) {
			for (Vertex result : this.results) {
				if (this.search.equals(RESPONSES)) {
					List<Relationship> responses = result.orderedRelationships(Primitive.RESPONSE);
					for (Relationship answer : responses) {
						if (checkFilter(result, answer)) {
							continue;
						}
						ResponseConfig response = new ResponseConfig();
						response.questionId = result.getId().toString();
						response.question = result.printString();
						response.responseId = answer.getTarget().getId().toString();
						response.response = answer.getTarget().printString();
						
						String emotes = getEmotes(answer.getSource(), answer.getTarget(), null);
						if (!emotes.isEmpty()) {
							response.emotions = emotes;
						}
						String actions = getActions(answer.getSource(), answer.getTarget(), null);
						if (!actions.isEmpty()) {
							response.actions = actions;
						}
						String poses = getPoses(answer.getSource(), answer.getTarget(), null);
						if (!poses.isEmpty()) {
							response.poses = poses;
						}
						if (answer.hasMeta()) {
							if (answer.getMeta().hasRelationship(Primitive.TOPIC)) {
								response.topic = getTopic(answer.getSource(), answer.getTarget(), null);
							}
							if (answer.getTarget().hasRelationship(Primitive.LABEL)) {
								response.label = getLabel(answer.getTarget());
							}
							if (answer.getMeta().hasRelationship(Primitive.KEYWORD)) {
								response.keywords = getKeyWords(answer.getSource(), answer.getTarget(), null);
							}
							if (answer.getMeta().hasRelationship(Primitive.REQUIRED)) {
								response.required = getRequired(answer.getSource(), answer.getTarget(), null);
							}
							if (answer.getMeta().hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)) {
								response.requirePrevious = true;
							}
							if (answer.getMeta().hasRelationship(Primitive.COMMAND)) {
								response.command = getCommand(answer.getSource(), answer.getTarget(), null);
							}
							if (answer.getMeta().hasRelationship(Primitive.THINK)) {
								response.think = getThink(answer.getSource(), answer.getTarget(), null);
							}
							if (answer.getMeta().hasRelationship(Primitive.CONDITION)) {
								response.condition = getCondition(answer.getSource(), answer.getTarget(), null);
							}
						}
						if (answer.getTarget().hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) {
							response.noRepeat = true;
						}
						if (answer.getTarget().hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
							response.flagged = true;
						}
						response.correctness = String.valueOf((int)(answer.getCorrectness() * 100));
						
						results.add(response);
					}
				} else {
					if (!this.search.equals(PHRASES) && checkFilter(result, null)) {
						continue;
					}
					ResponseConfig response = new ResponseConfig();
					if (this.search.equals(GREETINGS)) {
						response.type = "greeting";
					} else if (this.search.equals(DEFAULT)) {
						response.type = "default";
					}
					response.response = result.printString();
					response.responseId = result.getId().toString();
					if (result.hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) {
						response.noRepeat = true;
					}
					if (result.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
						response.flagged = true;
					}
					results.add(response);
				}
			}
		}
		reset();
		return results;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processQuery() {
		this.correction = false;
		this.isPhrase = false;
		Network memory = getBot().memory().newMemory();
		if (this.search.equals(GREETINGS)) {
			Vertex language = memory.createVertex(Language.class);
			this.results = new ArrayList<Vertex>();
			this.meta = new ArrayList<Vertex>();
			List<Relationship> greetings = language.orderedRelationships(Primitive.GREETING);
			if (greetings != null) {
				for (Relationship relationship : greetings) {
					if (checkFilter(null, relationship)) {
						continue;
					}
					this.results.add(relationship.getTarget());
				}
			}
			if (this.results != null) {
				this.resultsSize = this.results.size();
			}
		} else if (this.search.equals(DEFAULT)) {
			Vertex language = memory.createVertex(Language.class);
			this.results = new ArrayList<Vertex>();
			this.meta = new ArrayList<Vertex>();
			List<Relationship> responses = language.orderedRelationships(Primitive.RESPONSE);
			if (responses != null) {
				for (Relationship relationship : responses) {
					if (checkFilter(null, relationship)) {
						continue;
					}
					this.results.add(relationship.getTarget());
				}
			}
			if (this.results != null) {
				this.resultsSize = this.results.size();
			}
		} else {
			if (this.duration.equals("") || this.duration.equals("none")) {
				this.results = null;
				this.responses = null;
				return;
			}
			CriteriaQuery criteria = buildQuery(memory, false);		
			this.results = memory.search(criteria, this.page, this.pageSize);
			if (this.page == 0) {
				this.resultsSize = this.results.size();
				if (this.resultsSize >= this.pageSize) {
					criteria = buildQuery(memory, true);
					this.resultsSize = ((Number)memory.search(criteria, 0, 1).get(0)).intValue();
				}
			}
			// This is now in query.
			/*if (this.search.equals(CONVERSATIONS ) && this.restriction.equals("engaged")) {
				if (this.results != null && this.results.size() != 0) {
					List<Vertex> tempConversationList = new ArrayList<Vertex>();
					for (Vertex conversation : this.results) {
						Collection<Relationship> inputs = conversation.getRelationships(Primitive.INPUT);
						if (inputs != null && inputs.size() >= 3) {
							tempConversationList.add(conversation);
						}
					}
					this.results = null;
					this.results = tempConversationList;
					this.resultsSize = this.results.size();
				}
			}*/
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CriteriaQuery buildQuery(Network memory, boolean count) {
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_YEAR, -1);
		if (this.duration.equals("week")) {
			start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) - 7);
		} else if (this.duration.equals("month")) {
			start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) - 30);
		} else if (this.duration.equals("all")) {
			start = null;
		}
		Vertex typeVertex = null;
		Vertex relationshipVertex = null;
		if (this.search.equals(CONVERSATIONS)) {
			typeVertex = memory.createVertex(Primitive.CONVERSATION);
		} else if (this.search.equals(RESPONSES)) {
			typeVertex = memory.createVertex(Primitive.SENTENCE);
			relationshipVertex = memory.createVertex(Primitive.RESPONSE);
		} else if (this.search.equals(FLAGGED)) {
			typeVertex = memory.createVertex(Primitive.SENTENCE);
			relationshipVertex = memory.createVertex(Primitive.ASSOCIATED);
		} else if (this.search.equals(PHRASES)) {
			typeVertex = memory.createVertex(Primitive.SENTENCE);
		} else if (this.search.equals(WORDS)) {
			typeVertex = memory.createVertex(Primitive.WORD);
			relationshipVertex = memory.createVertex(Primitive.INSTANTIATION);
		}

		CriteriaBuilder cb = memory.getCriteriaBuilder();
		CriteriaQuery criteria = cb.createQuery();
		Root root = criteria.from(BasicVertex.class);
		criteria.distinct(true);
		Join relationship = root.join("allRelationships");
		Expression where = cb.equal(relationship.get("target"), typeVertex);
		if (this.search.equals(RESPONSES) || this.search.equals(PHRASES)) {
			where = cb.or(where, cb.equal(relationship.get("target"), memory.createVertex(Primitive.PATTERN)));
		}
		where = cb.and(where, cb.equal(relationship.get("type"), memory.createVertex(Primitive.INSTANTIATION)));
		if (this.search.equals(PHRASES)) {
			where = cb.and(where, cb.isNotNull(root.get("dataValue")));
		}
		if (relationshipVertex != null) {
			Join relationship2 = root.join("allRelationships");
			where = cb.and(where, cb.equal(relationship2.get("type"), relationshipVertex));
			if (this.search.equals(FLAGGED)) {
				where = cb.and(where, cb.equal(relationship2.get("target"), memory.createVertex(Primitive.OFFENSIVE)));
				where = cb.and(where, cb.greaterThan(relationship2.get("correctness"), 0));
			}
			if (start != null) {
				where = cb.and(where, cb.greaterThanOrEqualTo(relationship2.get("creationDate"), start));
			}
		} else if (start != null) {
			where = cb.and(where, cb.greaterThanOrEqualTo(relationship.get("creationDate"), start));
		}
		if (this.search.equals(CONVERSATIONS) && (this.type != null) && !this.type.equals("all")) {
			Primitive inputType = Primitive.CHAT;
			if (this.type.equals(TWEET)) {
				inputType = Primitive.TWEET;
			} else if (this.type.equals(POST)) {
				inputType = Primitive.POST;
			} else if (this.type.equals(EMAIL)) {
				inputType = Primitive.EMAIL;
			} else if (this.type.equals(DIRECTMESSAGE)) {
				inputType = Primitive.DIRECTMESSAGE;
			} else if (this.type.equals(SMS)) {
				inputType = Primitive.SMS;
			} else if (this.type.equals(FACEBOOKMESSENGER)) {
				inputType = Primitive.FACEBOOKMESSENGER;
			} else if (this.type.equals(SLACK)) {
				inputType = Primitive.SLACK;
			} else if (this.type.equals(TELEGRAM)) {
				inputType = Primitive.TELEGRAM;
			} else if (this.type.equals(SKYPE)) {
				inputType = Primitive.SKYPE;
			} else if (this.type.equals(WECHAT)) {
				inputType = Primitive.WECHAT;
			} else if (this.type.equals(KIK)) {
				inputType = Primitive.KIK;
			} else if (this.type.equals(COMMAND)) {
				inputType = Primitive.COMMAND;
			} else if (this.type.equals(TIMER)) {
				inputType = Primitive.TIMER;
			} else if (this.type.equals(ALEXA)) {
				inputType = Primitive.ALEXA;
			} else if (this.type.equals(GOOGLEASSISTANT)) {
				inputType = Primitive.GOOGLEASSISTANT;
			}
			Path relationship3 = root.join("allRelationships");
			if (this.search.equals(CONVERSATIONS)) {
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.TYPE)));
				where = cb.and(where, cb.equal(relationship3.get("target"), memory.createVertex(inputType)));
			} else {
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INSTANTIATION)));
				where = cb.and(where, cb.equal(relationship3.get("target"), memory.createVertex(inputType)));
			}
		}
		if ((this.restriction != null) && !this.restriction.equals("none")) {
			if (this.restriction.equals("emotes") || this.restriction.equals("poses") || this.restriction.equals("actions")
						|| this.restriction.equals("command") || this.restriction.equals("synonyms") || this.restriction.equals("sentiment")) {
				Primitive value = null;
				if (this.restriction.equals("emotes") || this.restriction.equals("sentiment")) {
					value = Primitive.EMOTION;
				} else if (this.restriction.equals("poses")) {
					value = Primitive.POSE;
				} else if (this.restriction.equals("actions")) {
					value = Primitive.ACTION;
				} else if (this.restriction.equals("command")) {
					value = Primitive.COMMAND;
				} else if (this.restriction.equals("synonyms")) {
					value = Primitive.SYNONYM;
				}
				if (this.search.equals(RESPONSES)) {
					Join relationship3 = root.join("allRelationships");
					Join meta = relationship3.join("meta");
					Join relationship4 = meta.join("allRelationships");
					where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
					where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(value)));
				} else if (this.search.equals(CONVERSATIONS)) {
					Join relationship3 = root.join("allRelationships");
					Join target = relationship3.join("target");
					Join relationship4 = target.join("allRelationships");
					where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INPUT)));
					where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(value)));
				} else {
					Join relationship3 = root.join("allRelationships");
					where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(value)));
				}
			} else if (this.restriction.equals("flagged") || this.restriction.equals("corrections")) {
				Primitive value = null;
				if (this.restriction.equals("flagged")) {
					if (this.search.equals(CONVERSATIONS)) {
						value = Primitive.OFFENDED;
					} else {
						value = Primitive.OFFENSIVE;
					}
				} else if (this.restriction.equals("corrections")) {
					value = Primitive.CORRECTION;
				}
				if (this.search.equals(RESPONSES)) {
					Join relationship3 = root.join("allRelationships");
					Join target = relationship3.join("target");
					Join relationship4 = target.join("allRelationships");
					where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
					where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(Primitive.ASSOCIATED)));
					where = cb.and(where, cb.equal(relationship4.get("target"), memory.createVertex(value)));
					where = cb.and(where, cb.greaterThan(relationship4.get("correctness"), 0));
				} else if (this.search.equals(CONVERSATIONS)) {
					Join relationship3 = root.join("allRelationships");
					Join target = relationship3.join("target");
					Join relationship4 = target.join("allRelationships");
					where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INPUT)));
					where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(Primitive.ASSOCIATED)));
					where = cb.and(where, cb.equal(relationship4.get("target"), memory.createVertex(value)));
				} else {
					Join relationship3 = root.join("allRelationships");
					where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.ASSOCIATED)));
					where = cb.and(where, cb.equal(relationship3.get("target"), memory.createVertex(value)));
					where = cb.and(where, cb.greaterThan(relationship3.get("correctness"), 0));
				}
			} else if (this.restriction.equals("patterns")) {
				Join relationship3 = root.join("allRelationships");
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INSTANTIATION)));
				where = cb.and(where, cb.equal(relationship3.get("target"), memory.createVertex(Primitive.PATTERN)));
			} else if (this.restriction.equals("templates")) {
				Join relationship3 = root.join("allRelationships");
				Join target = relationship3.join("target");
				Join relationship4 = target.join("allRelationships");
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
				where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(Primitive.INSTANTIATION)));
				where = cb.and(where, cb.equal(relationship4.get("target"), memory.createVertex(Primitive.FORMULA)));
			} else if ((this.restriction.equals("missing-topic") || this.restriction.equals("missing-keyword") || this.restriction.equals("missing-required"))
						&& (!this.search.equals(CONVERSATIONS))) {
				Join relationship3 = root.join("allRelationships");
				Join meta = relationship3.join("meta", JoinType.LEFT);
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
				where = cb.and(where, cb.greaterThan(relationship3.get("correctness"), 0));
				// Can't easily check for no keyword, so just check for no meta.
				// should check is null or is other, then filter in memory after...
				where = cb.and(where, cb.isNull(meta.get("id")));
			} else if (this.restriction.equals("wordiskeyword") || this.restriction.equals("wordistopic")) {
				if (this.search.equals(WORDS)) {
					if (this.restriction.equals("wordiskeyword") && relationshipVertex != null) {
						Join relationship3 = root.join("allRelationships");
						where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INSTANTIATION)));
						where = cb.and(where, cb.equal(relationship3.get("target"), memory.createVertex(Primitive.KEYWORD)));
					} else if (this.restriction.equals("wordistopic")) {
						Join relationship3 = root.join("allRelationships");
						where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INSTANTIATION)));
						where = cb.and(where, cb.equal(relationship3.get("target"), memory.createVertex(Primitive.TOPIC)));
					}
				}
			}
		}
		Join filterJoin = null;
		if ((this.restriction != null)
					&& (this.restriction.equals("topic") || this.restriction.equals("keyword")
							|| this.restriction.equals("required") || this.restriction.equals("previous"))) {
			Primitive value = null;
			if (this.restriction.equals("topic")) {
				value = Primitive.TOPIC;
			} else if (this.restriction.equals("keyword")) {
				value = Primitive.KEYWORD;
			} else if (this.restriction.equals("required")) {
				value = Primitive.REQUIRED;
			} else if (this.restriction.equals("previous")) {
				value = Primitive.PREVIOUS;
			}
			Join relationship3 = root.join("allRelationships");
			Join meta = relationship3.join("meta");
			Join relationship4 = meta.join("allRelationships");
			Join target2 = relationship4.join("target");
			filterJoin = target2;
			where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
			where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(value)));
			where = cb.and(where, cb.like(cb.lower(target2.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
		} else if ((this.restriction != null) && this.restriction.equals("repeat")) {
			Join relationship3 = root.join("allRelationships");
			Join target2 = relationship3.join("target");
			filterJoin = target2;
			if (this.search.equals(RESPONSES)) {
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
				Join relationship4 = target2.join("allRelationships");
				Join target3 = relationship4.join("target");
				filterJoin = target3;
				where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(Primitive.ONREPEAT)));
				where = cb.and(where, cb.like(cb.lower(target3.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
			} else {
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.ONREPEAT)));
				where = cb.and(where, cb.like(cb.lower(target2.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
			}
		} else if ((this.restriction != null) && this.restriction.equals("label")) {
			Join relationship3 = root.join("allRelationships");
			Join target2 = relationship3.join("target");
			filterJoin = target2;
			if (this.search.equals(RESPONSES)) {
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
				Join relationship4 = target2.join("allRelationships");
				Join target3 = relationship4.join("target");
				filterJoin = target3;
				where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(Primitive.LABEL)));
				where = cb.and(where, cb.like(cb.lower(target3.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
			} else {
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.LABEL)));
				where = cb.and(where, cb.like(cb.lower(target2.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
			}
		} else if ((this.restriction != null) && this.restriction.equals("question")) {
			where = cb.and(where, cb.like(cb.lower(root.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
		} else if (!this.filter.isEmpty() || this.sort.equals(RESPONSE)) {
			if (this.search.equals(CONVERSATIONS)) {
				Join relationship3 = root.join("allRelationships");
				Join target = relationship3.join("target");
				Join relationship4 = target.join("allRelationships");
				Join target2 = relationship4.join("target");
				filterJoin = target2;
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.INPUT)));
				where = cb.and(where, cb.equal(relationship4.get("type"), memory.createVertex(Primitive.INPUT)));
				if (this.restriction.equals("exact")) {
					where = cb.and(where, cb.like(cb.lower(target2.get("dataValue")), this.filter.toLowerCase()));
				} else {
					where = cb.and(where, cb.like(cb.lower(target2.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
				}
			} else if (this.search.equals(RESPONSES)) {
				Join relationship3 = root.join("allRelationships");
				Join target = relationship3.join("target");
				filterJoin = target;
				where = cb.and(where, cb.equal(relationship3.get("type"), memory.createVertex(Primitive.RESPONSE)));
				where = cb.and(where, cb.greaterThan(relationship3.get("correctness"), 0));
				if (this.restriction.equals("exact")) {
					where = cb.and(where, cb.like(cb.lower(target.get("dataValue")), this.filter.toLowerCase()));
				} else {
					where = cb.and(where, cb.like(cb.lower(target.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
				}
			} else {
				if (this.restriction.equals("exact")) {
					where = cb.and(where, cb.like(cb.lower(root.get("dataValue")), this.filter.toLowerCase()));
				} else {
					where = cb.and(where, cb.like(cb.lower(root.get("dataValue")), "%" + this.filter.toLowerCase() + "%"));
				}
			}
		}
		if (this.search.equals(CONVERSATIONS) && this.restriction.equals("engaged")) {
			Subquery subquery = criteria.subquery(BasicVertex.class);
			Root input = subquery.from(BasicVertex.class);
			subquery.select(cb.count(input));

			Join relationship3 = input.join("allRelationships");
			Join target = relationship3.join("target");

			subquery.where(cb.and(cb.equal(target.get("id"), root.get("id")),
					cb.equal(relationship3.get("type"), memory.createVertex(Primitive.CONVERSATION))));
			
			where = cb.and(where, cb.lessThan(cb.literal(4), subquery));
		}
		criteria.where(where);
		if (count) {
			criteria.select(cb.countDistinct(root));
		} else {
			if ((this.sort == null) || this.sort.equals(DATE)) {
				criteria.orderBy(cb.desc(relationship.get("creationDate")));
			} else if (this.sort.equals(DATE_DESC)) {
				criteria.orderBy(cb.asc(relationship.get("creationDate")));
			} else if (this.sort.equals(RESPONSE)) {
				if (filterJoin == null) {
					criteria.orderBy(cb.asc(root.get("dataValue")));
				} else {
					criteria.orderBy(cb.asc(filterJoin.get("dataValue")));
				}
			} else if (this.sort.equals(QUESTION)) {
				criteria.orderBy(cb.asc(root.get("dataValue")));
			} else {
				criteria.orderBy(cb.desc(relationship.get("creationDate")));
			}
		}
		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> lookupVertices(HttpServletRequest request, Network memory) {
		String phrase = "";
		List<Object[]> vertices = new ArrayList<Object[]>();
		for (Object parameter : request.getParameterMap().entrySet()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
			String key = entry.getKey();
			int index = key.indexOf(':');
			if ((index != -1) && (entry.getValue().length == 1)) {
				String type = key.substring(0, index);
				int index2 = key.indexOf(":", index + 1);
				String id2 = "";
				String id3 = "";
				if (index2 == -1) {
					index2 = key.length();
				} else {
					id2 = key.substring(index2 + 1, key.length());
					if (id2.contains(":")) {
						String[] strArray = id2.split(":");
						id2 = strArray[0];
						id3 = strArray[1];
					}
				}
				if (this.isPhrase) {
					if (type.equals("question")) {
						phrase = request.getParameter(key).trim();
					}
				}
				String id = key.substring(index + 1, index2);
				if (type.equals("answer")) {
					Object[] data = new Object[4];
					data[0] = "answer";
					data[1] = (memory.findById(Long.valueOf(id)));
					if (!id2.isEmpty()) {
						data[2] = (memory.findById(Long.valueOf(id2)));
					}
					data[3] = request.getParameter(key).trim();
					if ((data[1] != null) && (!data[3].equals(""))) {
						vertices.add(data);
					} else {
						if (!phrase.equals("")) {
							data[0] = "new phrase with no response";
							data[3] = phrase;
							if ((data[1] != null) && (!data[3].equals(""))) {
								vertices.add(data);
							}
						}
					}
				} else if (type.equals("emotes")) {
					Object[] data = new Object[3];
					data[0] = "emotes";
					data[1] = (memory.findById(Long.valueOf(id)));
					data[2] = request.getParameter(key);
					if ((data[1] != null) && (!data[2].equals(""))) {
						vertices.add(data);
					}
				}  else if (type.equals("sentiment")) {
					Object[] data = new Object[3];
					data[0] = "sentiment";
					data[1] = (memory.findById(Long.valueOf(id)));
					data[2] = request.getParameter(key);
					if ((data[1] != null) && (!data[2].equals(""))) {
						vertices.add(data);
					}
				} else if (type.equals("actions")) {
					Object[] data = new Object[3];
					data[0] = "actions";
					data[1] = (memory.findById(Long.valueOf(id)));
					data[2] = request.getParameter(key);
					if ((data[1] != null) && (!data[2].equals(""))) {
						vertices.add(data);
					}
				}  else if (type.equals("poses")) {
					Object[] data = new Object[3];
					data[0] = "poses";
					data[1] = (memory.findById(Long.valueOf(id)));
					data[2] = request.getParameter(key);
					if ((data[1] != null) && (!data[2].equals(""))) {
						vertices.add(data);
					}
				} else if (type.equals("word")) {
						Object[] data = new Object[3];
						data[0] = "words";
						data[1] = (memory.findById(Long.valueOf(id)));
						data[2] = request.getParameter(key);
						if ((data[1] != null) && (!data[2].equals(""))) {
							vertices.add(data);
						}
				} else {
					if ("on".equals(entry.getValue()[0])) {
						if (type.equals("response")) {
							Object[] data = new Object[2];
							data[1] = (memory.findById(Long.valueOf(id)));
							data[0] = (memory.findById(Long.valueOf(id2)));
							if ((data[0] != null) && (data[1] != null)) {
								vertices.add(data);
							}
						} else if (type.equals("next")) {
							Object[] data = new Object[3];
							data[1] = (memory.findById(Long.valueOf(id)));
							data[2] = (memory.findById(Long.valueOf(id2)));
							data[0] = "next";
							if ((data[1] != null) && (data[2] != null)) {
								vertices.add(data);
							}
						} else if (type.equals("next-response")) {
							Object[] data = new Object[4];
							data[1] = (memory.findById(Long.valueOf(id)));
							data[2] = (memory.findById(Long.valueOf(id2)));
							data[3] = (memory.findById(Long.valueOf(id3)));
							data[0] = "next-response";
							if ((data[1] != null) && (data[2] != null)) {
								vertices.add(data);
							}
						} else if (type.equals("previous")) {
							Object[] data = new Object[3];
							data[1] = (memory.findById(Long.valueOf(id)));
							data[2] = (memory.findById(Long.valueOf(id2)));
							data[0] = "previous";
							if ((data[1] != null) && (data[2] != null)) {
								vertices.add(data);
							}
						} else if (type.equals("repeat")) {
							Object[] data = new Object[3];
							data[1] = (memory.findById(Long.valueOf(id)));
							data[2] = (memory.findById(Long.valueOf(id2)));
							data[0] = "repeat";
							if ((data[1] != null) && (data[2] != null)) {
								vertices.add(data);
							}
						} else if (type.equals("word-keyword")) {
							Object[] data = new Object[2];
							data[0] = "word-keyword";
							data[1] = (memory.findById(Long.valueOf(id)));
							if (data[1] != null) {
								vertices.add(data);
							}
						} else if (type.equals("word-topic")) {
							Object[] data = new Object[2];
							data[0] = "word-topic";
							data[1] = (memory.findById(Long.valueOf(id)));
							if (data[1] != null) {
								vertices.add(data);
							}
						} 
						else {
							Object[] data = new Object[1];
							data[0] = (memory.findById(Long.valueOf(id)));
							if (data[0] != null) {
								vertices.add(data);
							}
						}
					}
				}
			}
		}
		return vertices;
	}

	public void processInvalidate(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			throw new BotException("No responses selected");
		}
		for (Object[] data : vertices) {
			if (this.search.equals(CONVERSATIONS) && !this.isPhrase) {
				if ((data.length == 3) && (data[1] instanceof Vertex)) {
					Vertex previous = (Vertex)data[2];
					Vertex meta = (Vertex)data[1];
					meta.removeRelationship(Primitive.PREVIOUS, previous);
				} else {
					Vertex input = (Vertex)data[0];
					Vertex response = input.getRelationship(Primitive.INPUT);
					Vertex question = input.getRelationship(Primitive.QUESTION);
					if ((response != null) && (question != null)) {
						Vertex sentence = question.getRelationship(Primitive.INPUT);
						if (sentence == null) {
							continue;
						}
						sentence.removeRelationship(Primitive.RESPONSE, response);
						if (getBot().mind().getThought(Language.class).getReduceQuestions()) {
							memory.checkReduction(sentence);
							sentence.inverseAssociateAll(Primitive.REDUCTION, response, Primitive.RESPONSE);
						}
						if (input.hasRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION)) {
							input.removeRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION);
						}
					}
				}
			} else if (this.isPhrase || this.search.equals(RESPONSES) || this.search.equals(PHRASES)) {
				if ((data.length == 2) && (data[1] instanceof Vertex)) {
					Vertex question = (Vertex)data[1];
					Vertex response = (Vertex)data[0];
					question.removeRelationship(Primitive.RESPONSE, response);
					if (getBot().mind().getThought(Language.class).getReduceQuestions()) {
						memory.checkReduction(question);
						question.inverseAssociateAll(Primitive.REDUCTION, response, Primitive.RESPONSE);
					}
				} else if ((data.length == 3) && (data[1] instanceof Vertex)) {
					Vertex previous = (Vertex)data[2];
					Vertex meta = (Vertex)data[1];
					Primitive type = Primitive.PREVIOUS;
					if (data[0].equals("repeat")) {
						type = Primitive.ONREPEAT;
					}
					meta.removeRelationship(type, previous);
				}
			}
		}
		memory.save();
	}

	/**
	 * Process the response/conversation deletion (from REST API).
	 */
	public void processDelete(ResponseConfig config) {
		Network memory = getBot().memory().newMemory();
		long id = 0;
		try {
			id = Long.valueOf(config.responseId);
		} catch (Exception invalid) {
			throw new BotException("Invalid response id: " + config.responseId);
		}
		Vertex response = memory.findById(id);
		if (response == null) {
			throw new BotException("Missing response: " + config.responseId);
		}
		if (config.type != null && config.type.equals("conversation")) {
			// Delete conversation.
			memory.removeVertexAndReferences(response);
		} else if (config.type != null && config.type.equals("greeting")) {
			// Delete greeting.
			Vertex language = memory.createVertex(Language.class);
			Relationship relationship = language.getRelationship(Primitive.GREETING, response);
			if (relationship != null) {
				language.internalRemoveRelationship(relationship);
			}
		} else if (config.type != null && config.type.equals("default")) {
			// Delete default response.
			Vertex language = memory.createVertex(Language.class);
			Relationship relationship = language.getRelationship(Primitive.RESPONSE, response);
			if (relationship != null) {
				language.internalRemoveRelationship(relationship);
			}
		} else {
			// Delete response.
			if (config.metaId == null || config.metaId.equals("")) {
				// Delete root level response.
				try {
					id = Long.valueOf(config.questionId);
				} catch (Exception invalid) {
					throw new BotException("Invalid question id: " + config.questionId);
				}
				Vertex question = memory.findById(id);
				if (question == null) {
					throw new BotException("Missing question: " + config.questionId);
				}
				Relationship relationship = question.getRelationship(Primitive.RESPONSE, response);
				if (relationship != null) {
					question.internalRemoveRelationship(relationship);
					Relationship inverse = response.getRelationship(Primitive.QUESTION, question);
					if (inverse != null) {
						response.internalRemoveRelationship(inverse);
					}
				}
			} else {
				// Delete nested next response.
				long metaId = 0;
				long nextId = 0;
				try {
					metaId = Long.valueOf(config.metaId);
				} catch(Exception invalid) {
					throw new BotException("Invalid meta id: " + config.metaId);
				}
				try {
					nextId = Long.valueOf(config.questionId);
				} catch (Exception invalid) {
					throw new BotException("Invalid mext id: " + config.questionId);
				}
				Vertex next = memory.findById(nextId);
				if (next == null) {
					throw new BotException("Missing next meta: " + config.questionId);
				}
				Vertex meta = memory.findById(metaId);
				if (meta == null) {
					throw new BotException("Missing question response meta: " + config.metaId);
				}
				Relationship nextRelationship = meta.getRelationship(Primitive.NEXT, next);
				boolean remainingResponses = false;
				if (nextRelationship != null) {
					if (nextRelationship.hasMeta()) {
						Relationship responseRelationship = nextRelationship.getMeta().getRelationship(Primitive.RESPONSE, response);
						if (responseRelationship != null) {
							nextRelationship.getMeta().internalRemoveRelationship(responseRelationship);
						}
						if(nextRelationship.getMeta().hasRelationship(Primitive.RESPONSE)) {
							remainingResponses = true;
						}	
					}
					if(!remainingResponses) {
						meta.internalRemoveRelationship(nextRelationship);
					}
				}
			}
		}
		memory.save();
	}

	public void processDelete(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			throw new BotException("No responses selected");
		}
		for (Object[] data : vertices) {
			if (this.search.equals(CONVERSATIONS) && !this.isPhrase) {
				Vertex input = (Vertex)data[0];
				Vertex response = input.getRelationship(Primitive.INPUT);
				Vertex question = input.getRelationship(Primitive.QUESTION);
				if ((response != null) && (question != null)) {
					Vertex sentence = question.getRelationship(Primitive.INPUT);
					if (sentence == null) {
						continue;
					}
					Relationship relationship = sentence.getRelationship(Primitive.RESPONSE, response);
					if (relationship != null) {
						// Delete response.
						sentence.internalRemoveRelationship(relationship);
						Relationship inverse = response.getRelationship(Primitive.QUESTION, sentence);
						if (inverse != null) {
							response.internalRemoveRelationship(inverse);
						}
					} else {
						// Was not a response, so assume want whole input or a conversation deleted.
						memory.removeVertexAndReferences(input);
					}
				} else {
					// Was not a response, so assume want whole input or a conversation deleted.
					memory.removeVertexAndReferences(input);
				}
			} else if ((data.length == 3) && (data[1] instanceof Vertex)) {
				Vertex previous = (Vertex)data[2];
				Vertex meta = (Vertex)data[1];
				Primitive type = Primitive.PREVIOUS;
				if (data[0].equals("repeat")) {
					type = Primitive.ONREPEAT;
				} else if (data[0].equals("next")) {
					type = Primitive.NEXT;
				}
				Relationship relationship = meta.getRelationship(type, previous);
				if (relationship != null) {
					meta.internalRemoveRelationship(relationship);
				}
			} else if (data[0].equals("next-response") && (data.length == 4) && (data[1] instanceof Vertex)) {
				Vertex next = (Vertex)data[2];
				Vertex meta = (Vertex)data[1];
				Vertex response = (Vertex)data[3];
				Relationship relationship = meta.getRelationship(Primitive.NEXT, next);
				if (relationship != null && relationship.hasMeta()) {
					Relationship responseRelationship = relationship.getMeta().getRelationship(Primitive.RESPONSE, response);
					if (responseRelationship != null) {
						relationship.getMeta().internalRemoveRelationship(responseRelationship);
					}
				}
			} else if (this.search.equals(GREETINGS)) {
				if (data[0] instanceof Vertex) {
					Vertex greeting = (Vertex)data[0];
					Vertex language = memory.createVertex(Language.class);
					Relationship relationship = language.getRelationship(Primitive.GREETING, greeting);
					if (relationship != null) {
						language.internalRemoveRelationship(relationship);
					}
				}
			} else if (this.search.equals(DEFAULT)) {
				if (data[0] instanceof Vertex) {
					Vertex response = (Vertex)data[0];
					Vertex language = memory.createVertex(Language.class);
					Relationship relationship = language.getRelationship(Primitive.RESPONSE, response);
					if (relationship != null) {
						language.internalRemoveRelationship(relationship);
					}
				}
			} else if (this.isPhrase || this.search.equals(RESPONSES) || this.search.equals(PHRASES)) {
				if ((data.length == 2) && (data[1] instanceof Vertex)) {
					Vertex question = (Vertex)data[1];
					Vertex response = (Vertex)data[0];
					Relationship relationship = question.getRelationship(Primitive.RESPONSE, response);
					if (relationship != null) {
						question.internalRemoveRelationship(relationship);
						Relationship inverse = response.getRelationship(Primitive.QUESTION, question);
						if (inverse != null) {
							response.internalRemoveRelationship(inverse);
						}
					}
				}
			}
		}
		memory.save();
	}

	public void processValidate(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			throw new BotException("No responses selected");
		}
		for (Object[] data : vertices) {
			if (this.search.equals(CONVERSATIONS) && !this.isPhrase) {
				if ((data.length == 3) && (data[1] instanceof Vertex)) {
					Vertex previous = (Vertex)data[2];
					Vertex meta = (Vertex)data[1];
					meta.addRelationship(Primitive.PREVIOUS, previous);
				} else {
					Vertex input = (Vertex)data[0];
					Vertex response = input.getRelationship(Primitive.INPUT);
					Vertex question = input.getRelationship(Primitive.QUESTION);
					if ((response != null) && (question != null)) {
						Vertex sentence = question.getRelationship(Primitive.INPUT);
						if (sentence == null) {
							continue;
						}
						sentence.addRelationship(Primitive.RESPONSE, response);
						sentence.setPinned(true);
						response.setPinned(true);
						if (getBot().mind().getThought(Language.class).getReduceQuestions()) {
							memory.checkReduction(sentence);
							sentence.associateAll(Primitive.REDUCTION, response, Primitive.RESPONSE);
						}
					}
				}
			} else if (this.isPhrase || this.search.equals(RESPONSES) || this.search.equals(PHRASES)) {
				if ((data.length == 2) && (data[1] instanceof Vertex)) {
					Vertex question = (Vertex)data[1];
					Vertex response = (Vertex)data[0];
					question.addRelationship(Primitive.RESPONSE, response);
					question.setPinned(true);
					response.setPinned(true);
					if (getBot().mind().getThought(Language.class).getReduceQuestions()) {
						memory.checkReduction(question);
						question.associateAll(Primitive.REDUCTION, response, Primitive.RESPONSE);
					}
				} else if ((data.length == 3) && (data[1] instanceof Vertex)) {
					Vertex previous = (Vertex)data[2];
					Vertex meta = (Vertex)data[1];
					Primitive type = Primitive.PREVIOUS;
					if (data[0].equals("repeat")) {
						type = Primitive.ONREPEAT;
					}
					meta.addRelationship(type, previous);
				}
			}
		}
		memory.save();
	}
	
	public void processNewResponse() {
		Network memory = getBot().memory().newMemory();
		List<Vertex> results = new ArrayList<Vertex>(1);
		List<Vertex> responses = new ArrayList<Vertex>(1);
		List<Vertex> meta = new ArrayList<Vertex>();
		results.add(memory.createVertex(""));
		memory.save();
		responses.add(null);
		this.results = results;
		this.responses = responses;
		this.meta = meta;
		this.correction = true;
		this.isPhrase = true;
	}

	public void processCorrection(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			if (this.search.equals(CONVERSATIONS)) {
				throw new BotException("No responses selected");
			} else if (this.search.equals(WORDS)) {
				throw new BotException("No words selected");
			} else {
				throw new BotException("No phrases selected");				
			}
		}
		if (this.search.equals(CONVERSATIONS) && !this.isPhrase) {
			List<Vertex> results = new ArrayList<Vertex>(vertices.size());
			for (Object[] data : vertices) {
				if (data[0] instanceof Vertex) {
					results.add((Vertex)data[0]);
				}
			}
			if (results.isEmpty()) {
				if (this.search.equals(CONVERSATIONS)) {
					throw new BotException("No responses selected");
				} else {
					throw new BotException("No phrases selected");				
				}
			}
			this.results = results;
			this.responses = null;
			this.meta = null;
			this.correction = true;
		} else {
			List<Vertex> results = new ArrayList<Vertex>(vertices.size());
			List<Vertex> responses = new ArrayList<Vertex>(vertices.size());
			List<Vertex> meta = new ArrayList<Vertex>(vertices.size());
			for (Object[] data : vertices) {
				if (data[0] instanceof Vertex) {
					if ((data.length > 1) && data[1] instanceof Vertex) {
						results.add((Vertex)data[1]);
						responses.add((Vertex)data[0]);
						meta.add(null);
					} else {
						results.add((Vertex)data[0]);
						responses.add(null);
						meta.add(null);
					}
				} else if (data.length == 3 && WORDS.equals(data[0])) {
					results.add((Vertex)data[1]);
					responses.add(null);
					meta.add(null);
				} else if (data.length == 3 && "next".equals(data[0])) {
					results.add((Vertex)data[2]);
					responses.add(null);
					meta.add((Vertex)data[1]);
				} else if (data.length == 4 && "next-response".equals(data[0])) {
					results.add((Vertex)data[2]);
					responses.add((Vertex)data[3]);
					meta.add((Vertex)data[1]);
				} else if (data.length == 3 && data[2] instanceof Vertex) {
					results.add((Vertex)data[2]);
					responses.add(null);
					meta.add(null);
				}
			}
			if (results.isEmpty()) {
				if (this.search.equals(CONVERSATIONS)) {
					throw new BotException("No responses selected");
				} else {
					throw new BotException("No phrases selected");				
				}
			}
			this.results = results;
			this.responses = responses;
			this.meta = meta;
			this.correction = true;
		}
	}

	public void processBrowse(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			throw new BotException("No phrases selected");
		}
		List<Vertex> results = new ArrayList<Vertex>(vertices.size());
		if (this.search.equals(CONVERSATIONS)) {
			for (Object[] data : vertices) {
				if (data[0] instanceof Vertex) {
					Vertex input = (Vertex)data[0];
					Vertex phrase = input.getRelationship(Primitive.INPUT);
					if (phrase != null) {
						results.add(phrase);
					}
				} else if (data.length == 3 && data[2] instanceof Vertex) {
					results.add((Vertex)data[2]);
				}
			}
		} else {
			for (Object[] data : vertices) {
				if (data[0] instanceof Vertex) {
					if ((data.length > 1) && data[1] instanceof Vertex) {
						results.add((Vertex)data[0]);
					} else {
						results.add((Vertex)data[0]);
					}
				} else if (data.length == 3 && data[2] instanceof Vertex) {
					results.add((Vertex)data[2]);
				}
			}
		}
		List<Vertex> resultsWithQuestions = new ArrayList<Vertex>(results.size() * 2);
		for (Vertex response : results) {
			resultsWithQuestions.add(response);
			Collection<Relationship> questions = response.getRelationships(Primitive.QUESTION);
			if (questions != null) {
				for (Relationship question: questions) {
					if (question.getTarget().hasRelationship(Primitive.RESPONSE, response)) {
						resultsWithQuestions.add(question.getTarget());
					}
				}
			}
		}
		this.results = resultsWithQuestions;
		this.page = 0;
		this.responses = null;
		this.isPhrase = true;
	}
	
	public void checkMemory() {
		if ((getBotBean().getInstance().getMemoryLimit() > 0) && (getBot().memory().getLongTermMemory().size() > getBotBean().getInstance().getMemoryLimit() * 1.5)) {
			throw new BotException("Memory size exceeded, importing has been disable until nightly forgetfullness task runs");
		}
	}

	public void importFile(InputStream stream, String format, String encoding, boolean processUnderstanding, boolean pin, boolean autoReduce) {
		checkMemory();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.imports++;
		if ("AIML".equals(format)) {
			Language language = getBot().mind().getThought(Language.class);
			if (language != null) {
				language.loadAIMLFileAsLog(stream, encoding, Site.MAX_UPLOAD_SIZE, pin);
			}
		} else {
			ResponseListParser.parser().loadChatFile(stream, format, encoding, Site.MAX_UPLOAD_SIZE, processUnderstanding, pin, autoReduce, getBot());
		}
		processQuery();
	}

	public void importChatLog(String text, String format, boolean processUnderstanding, boolean pin, boolean autoReduce) {
		checkMemory();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.imports++;
		if ("AIML".equals(format)) {
			Language language = getBot().mind().getThought(Language.class);
			if (language != null) {
				language.loadAIMLAsLog(text, pin);
			}
		} else {
			ResponseListParser.parser().loadChat(text, format, processUnderstanding, pin, autoReduce, getBot());
		}
		processQuery();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void export(HttpServletResponse response, String format, String expNumPages, boolean exportGreetings, boolean exportDefaultResponses) throws IOException {
		if (this.results.isEmpty()) {
			throw new BotException("No conversations or responses to export");
		}
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		boolean responsesFormat = (format != null) && format.equals("responses");
		boolean aiml = (format != null) && format.equals("aiml");
		boolean chatlog = !responsesFormat && !aiml;

		int exportNumberPages = 0;
		if (expNumPages == null || expNumPages.equals("")) {
			exportNumberPages = 1;
		} else {
			try {
				exportNumberPages = Integer.parseInt(expNumPages.trim());
				if (exportNumberPages > (Math.round(this.getResultsSize() / this.getPageSize()) + 1)) {
					if ((Math.round(this.getResultsSize() / this.getPageSize()) + 1) == 1) {
						throw new BotException("Invalid page number. Export page entered '" + String.valueOf(exportNumberPages) + "' must be 1.");
					} else {
						throw new BotException("Invalid page number. Export page entered '" + String.valueOf(exportNumberPages) + "' must be a number between 1 and " + String.valueOf(Math.round(this.getResultsSize() / this.getPageSize()) + 1) + ".");
					}
				} else if (exportNumberPages <= 1) {
					exportNumberPages = 1;
				}
			} catch (NumberFormatException exception) {
				throw new BotException("Invalid page number. Page number entered '" + expNumPages.trim() + "' is not a number.");
			}
		}
		String ext = ".log";
		if (aiml) {
			ext = ".aiml";
		} else if (responsesFormat) {
			ext = ".res";
		}
		response.setHeader("Content-disposition","attachment; filename=" + encodeURI(getBotBean().getInstanceName()) + ext);
		if (aiml) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			writer.write("<aiml version=\"1.1\">\r\n");
		}
		boolean executeQuery = false;
		if (exportNumberPages > 1) {
			executeQuery = true;
		}
		// Iterate over the pages backwards to generate a file that matches what was imported.
		for (int index = 0; index < exportNumberPages; index++) {
			List<Vertex> currentPageResults = this.results;
			Vertex language = null;
			if (executeQuery) {
				Network memory = getBot().memory().newMemory();
				language = memory.createVertex(getBot().mind().getThought(Language.class).getPrimitive());
				CriteriaQuery criteria = buildQuery(memory, false);
				currentPageResults = memory.search(criteria, this.page + index, this.pageSize);
			}
			// If the last page, then first write the greeting/defaults.
			if (index == 0) {
				if (currentPageResults.isEmpty()) {
					Network memory = getBot().memory().newMemory();
					language = memory.createVertex(getBot().mind().getThought(Language.class).getPrimitive());
				} else {
					language = currentPageResults.get(0).getNetwork().createVertex(getBot().mind().getThought(Language.class).getPrimitive());
				}
				if (exportDefaultResponses) {
					currentPageResults.add(0, language.getNetwork().createVertex(Primitive.DEFAULT));
				}
				if (exportGreetings) {
					currentPageResults.add(0, language.getNetwork().createVertex(Primitive.GREETING));
				}
			}
			if (this.search.equals(CONVERSATIONS) && !this.isPhrase) {
				for (Vertex conversation : currentPageResults) {
					List<Vertex> inputs = conversation.orderedRelations(Primitive.INPUT);
					if (inputs != null) {
						Vertex that = null;
						Vertex previous = null;
						for (Vertex input : inputs) {
							Vertex answer = input.getRelationship(Primitive.INPUT);
							if (aiml) {
								if (previous != null) {
									writer.write("<category>\r\n");
									writer.write("\t<pattern>");
									writer.write(previous.printString().toUpperCase());
									writer.write("</pattern>\r\n");
									if (that != null) {
										writer.write("\t<that>");
										writer.write(that.printString().toUpperCase());
										writer.write("</that>\r\n");
									}
									writer.write("\t<template>");
									writer.write(answer.printString());
									writer.write("</template>\r\n");
									writer.write("</category>");
								}
							} else if (responsesFormat) {
								if (previous != null) {
									writer.write(previous.printString());
									writer.write("\r\n");
									writer.write(answer.printString());
									writer.write("\r\n");
									writer.write("\r\n");
								}
							} else {
								writer.write(getName(input.getRelationship(Primitive.SPEAKER)));
								writer.write(": ");
								if (answer != null) {
									writer.write(answer.printString());
								}
							}
							writer.write("\r\n");
							that = previous;
							previous = answer;
						}
						writer.write("\r\n");
					}
				}
			} else if (this.isPhrase || this.search.equals(RESPONSES) || this.search.equals(PHRASES) || this.search.equals(GREETINGS) || this.search.equals(DEFAULT)) {
				// Iterate over the results backwards so the file matches what was imported.
				for (int index2 = 0; index2 < currentPageResults.size(); index2++) {
					Vertex phrase = currentPageResults.get(index2);
					if (chatlog && phrase.instanceOf(Primitive.PATTERN)) {
						continue;
					}
					List<Relationship> responses = new ArrayList<Relationship>();
					if (this.search.equals(GREETINGS) || this.search.equals(DEFAULT)) {
						if (aiml) {
							continue;
						}
						if (this.search.equals(GREETINGS)) {
							Relationship relationship = language.getRelationship(Primitive.GREETING, phrase);
							if (relationship != null) {
								responses = new ArrayList<Relationship>(1);
								responses.add(relationship);
							}
						} else {
							Relationship relationship = language.getRelationship(Primitive.RESPONSE, phrase);
							if (relationship != null) {
								responses = new ArrayList<Relationship>(1);
								responses.add(relationship);
							}
						}
					} else if (phrase.is(Primitive.GREETING)) {
						responses = language.orderedRelationships(Primitive.GREETING);
					} else if (phrase.is(Primitive.DEFAULT)) {
						responses = language.orderedRelationships(Primitive.RESPONSE);
					} else {
						responses = phrase.orderedRelationships(Primitive.RESPONSE);
					}
					if (responses == null) {
						if (phrase.hasData() && !aiml) { // Some temp formula sentences do no have data.
							if (this.search.equals(GREETINGS)) {
								writer.write("greeting: ");
							} else if (this.search.equals(DEFAULT)) {
								writer.write("default: ");
							} else if (chatlog) {
								writer.write("anonymous: ");
							}
							writer.write(phrase.printString());
							writer.write("\r\n");
							writer.write("\r\n");
						}
					} else {
						// Remove inverse.
						if (!this.search.equals(GREETINGS) && !this.search.equals(DEFAULT)) {
							for (Iterator<Relationship> iterator = responses.iterator(); iterator.hasNext();) {
								Relationship nextResponse = iterator.next();
								if (nextResponse.isInverse()) {
									iterator.remove();
								}
							}
						}
						if (aiml) {
							writer.write("<category>\r\n");
							writer.write("\t<pattern>");
							if (phrase.instanceOf(Primitive.PATTERN)) {
								String text = phrase.printString();
								if (text.length() >= 10) {
									text = text.substring(9, text.length() - 1);
								}
								writer.write(text.toUpperCase());
							} else {
								writer.write(phrase.printString().toUpperCase());
							}
							writer.write("</pattern>\r\n");
							writer.write("\t<template>");
							if (responses.size() > 1) {
								writer.write("\r\n\t\t<random>\r\n");
							}
						}
						for (Relationship relationship : responses) {
							boolean isGreeting =  this.search.equals(GREETINGS) || relationship.getType().is(Primitive.GREETING);
							boolean isDefault = this.search.equals(DEFAULT) || (relationship.getType().is(Primitive.RESPONSE) && relationship.getSource() == language);
							if (chatlog && relationship.getTarget().instanceOf(Primitive.FORMULA)) {
								continue;
							}
							if (aiml) {
								if (responses.size() > 1) {
									writer.write("\t\t\t<li>");
								}
								writer.write(relationship.getTarget().printString());
								if (responses.size() > 1) {
									writer.write("</li>\r\n");
								}
							} else {
								if (!isGreeting && !isDefault) {
									if (chatlog) {
										writer.write("anonymous: ");
									}
									String text = phrase.printString();
									if (text.contains("\n") || text.startsWith("\"") || text.trim().isEmpty()) {
										writer.write("\"");
									}
									writer.write(text);
									if (text.contains("\n") || text.startsWith("\"") || text.trim().isEmpty()) {
										writer.write("\"");
									}
									writer.write("\r\n");
									List<EmotionalState> sentiments = getBot().mood().evaluateEmotionalStates(relationship.getSource());
									if (!sentiments.isEmpty()) {
										writer.write("sentiment: ");
										int count = 0;
										for (EmotionalState sentiment : sentiments) {
											boolean isSentiment = sentiment.isSentiment();
											if (!isSentiment) {
												continue;
											}
											count++;
											writer.write(sentiment.name().toLowerCase());
											if (count < sentiments.size()) {
												writer.write(" ");
											}
										}
										writer.write("\r\n");
									}
									if (this.search.equals(PHRASES)) {
										List<EmotionalState> emotes = getBot().mood().evaluateEmotionalStates(relationship.getSource());
										if (!emotes.isEmpty()) {
											writer.write("emotions: ");
											int count = 0;
											for (EmotionalState emote : emotes) {
												boolean isSentiment = emote.isSentiment();
												if (isSentiment) {
													continue;
												}
												count++;
												writer.write(emote.name().toLowerCase());
												if (count < emotes.size()) {
													writer.write(" ");
												}
											}
											writer.write("\r\n");
										}
										List<Relationship> actions = relationship.getSource().orderedRelationships(Primitive.ACTION);
										if (actions != null) {
											writer.write("actions: ");
											for (Relationship action : actions) {
												writer.write(action.getTarget().getDataValue());
												writer.write(" ");
											}
											writer.write("\r\n");
										}
										List<Relationship> poses = relationship.getSource().orderedRelationships(Primitive.POSE);
										if (poses != null) {
											writer.write("poses: ");
											for (Relationship pose : poses) {
												writer.write(pose.getTarget().getDataValue());
												writer.write(" ");
											}
											writer.write("\r\n");
										}
									}
								}
								if (isGreeting) {
									writer.write("greeting: ");
								} else if (isDefault) {
									writer.write("default: ");
								} else if (chatlog) {
									writer.write("self: ");
								}
								String text = relationship.getTarget().printString();
								if (text.contains("\n") || text.startsWith("\"") || text.trim().isEmpty()) {
									writer.write("\"");
								}
								writer.write(text);
								if (text.contains("\n") || text.startsWith("\"") || text.trim().isEmpty()) {
									writer.write("\"");
								}
								writer.write("\r\n");
								if (!chatlog) {
									exportResponseMetadata(writer, relationship, isGreeting, isDefault, 0);
								}
								writer.write("\r\n");
							}
						}
						if (aiml) {
							if (responses.size() > 1) {
								writer.write("\t\t</random>\r\n\t");
							}
							writer.write("</template>\r\n");
							writer.write("</category>\r\n");
						}
					}
				}
			} else if (this.search.equals(WORDS)) {
				for (Vertex word : currentPageResults) {
					if (!aiml) {
						if (word.hasData()) {
							writer.write("word: ");
							writer.write(word.printString());
							writer.write("\r\n");
							List<EmotionalState> emotes = getBot().mood().evaluateEmotionalStates(word);
							if (!emotes.isEmpty()) {
								writer.write("emotions: ");
								int count = 0;
								for (EmotionalState emote : emotes) {
									boolean isSentiment = emote.isSentiment();
									if (isSentiment) {
										continue;
									}
									count++;
									writer.write(emote.name().toLowerCase());
									if (count < emotes.size()) {
										writer.write(" ");
									}
								}
								writer.write("\r\n");
							}
							List<EmotionalState> sentiments = getBot().mood().evaluateEmotionalStates(word);
							if (!sentiments.isEmpty()) {
								writer.write("sentiment: ");
								int count = 0;
								for (EmotionalState sentiment : sentiments) {
									boolean isSentiment = sentiment.isSentiment();
									if (!isSentiment) {
										continue;
									}
									count++;
									writer.write(sentiment.name().toLowerCase());
									if (count < sentiments.size()) {
										writer.write(" ");
									}
								}
								writer.write("\r\n");
							}
							List<Relationship> relationships = null;
							relationships = word.orderedRelationships(Primitive.SYNONYM);
							if (relationships != null) {
								writer.write("synonyms: ");
								for (Relationship synonym : relationships) {
									if (synonym.getTarget().instanceOf(Primitive.COMPOUND_WORD)) {
										writer.write("\"");
										writer.write(synonym.getTarget().getDataValue());
										writer.write("\"");
									} else {
										writer.write(synonym.getTarget().getDataValue());
									}
									writer.write(" ");
								}
								writer.write("\r\n");
							}
							boolean hasKeyword = word.hasRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
							if (hasKeyword) {
								writer.write("keyword: ");
								writer.write(String.valueOf(true));
								writer.write(" ");
								writer.write("\r\n");
							}
							boolean hasTopic = word.hasRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
							if (hasTopic) {
								writer.write("topic: ");
								writer.write(String.valueOf(true));
								writer.write(" ");
								writer.write("\r\n");
							}
							boolean hasExclusiveTopic = word.hasRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
							if (hasExclusiveTopic) {
								writer.write("exclusive topic: ");
								writer.write(String.valueOf(true));
								writer.write(" ");
								writer.write("\r\n");
							}
						}
						writer.write("\r\n");
					}
				}
			}
		}
		if (aiml) {
			writer.write("</aiml>");
		}
		writer.flush();
	}
	
	/**
	 * Recursively write the response meta data including next responses.
	 */
	public void exportResponseMetadata(PrintWriter writer, Relationship relationship, boolean isGreeting, boolean isDefault, int counter) {
		String tab = "";
		for (int index = 0; index < counter; index++) {
			tab = tab + "\t";
		}
		if (!isGreeting && !isDefault && relationship.getCorrectness() != 1.0f) {
			writer.write(tab);
			writer.write("confidence: ");
			writer.write(String.valueOf((int) (relationship.getCorrectness() * 100)));
			writer.write("\r\n");
		}
		if (relationship.getTarget().hasRelationship(Primitive.LABEL)) {
			writer.write(tab);
			writer.write("label: ");
			writer.write(relationship.getTarget().getRelationship(Primitive.LABEL).printString());
			writer.write("\r\n");
		}
		if (relationship.getTarget().hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) {
			writer.write(tab);
			writer.write("no repeat: true");
			writer.write("\r\n");
		}
		List<Relationship> repeats = relationship.getTarget().orderedRelationships(Primitive.ONREPEAT);
		if (repeats != null) {
			for (Relationship repeat : repeats) {
				writer.write(tab);
				writer.write("on repeat: ");
				String text = repeat.getTarget().printString();
				if (text.contains("\n") || text.startsWith("\"")) {
					writer.write("\"");
				}
				writer.write(text);
				if (text.contains("\n") || text.startsWith("\"")) {
					writer.write("\"");
				}
				writer.write("\r\n");
			}
		}
		if (!relationship.hasMeta()) {
			return;
		}
		List<Relationship> meta = relationship.getMeta().orderedRelationships(Primitive.KEYWORD);
		if (meta != null) {
			writer.write(tab);
			writer.write("keywords:");
			for (Relationship keyword : meta) {
				writer.write(" ");
				if (keyword.getTarget().instanceOf(Primitive.COMPOUND_WORD)) {
					writer.write("\"");
					writer.write(keyword.getTarget().getDataValue());
					writer.write("\"");
				} else {
					writer.write(keyword.getTarget().printString());
				}
			}
			writer.write("\r\n");
		}
		Vertex requiredText = relationship.getMeta().getRelationship(Primitive.REQUIRED_TEXT);
		if (requiredText != null) {
			writer.write(tab);
			writer.write("required: ");
			writer.write(requiredText.printString());
			writer.write("\r\n");
		} else {
			meta = relationship.getMeta().orderedRelationships(Primitive.REQUIRED);
			if (meta != null) {
				writer.write(tab);
				writer.write("required:");
				for (Relationship required : meta) {
					writer.write(" ");
					if (!required.getTarget().instanceOf(Primitive.WORD) && required.getTarget().instanceOf(Primitive.FRAGMENT)) {
						writer.write("\"");
						writer.write(required.getTarget().getDataValue());
						writer.write("\"");
					} else {
						writer.write(required.getTarget().printString());
					}
				}
				writer.write("\r\n");
			}
		}
		meta = relationship.getMeta().orderedRelationships(Primitive.PREVIOUS);
		if (meta != null) {
			boolean requirePrevious = relationship.getMeta().hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);
			for (Relationship previous : meta) {
				writer.write(tab);
				if (requirePrevious) {
					writer.write("require previous: ");
				} else {
					writer.write("previous: ");
				}
				String text = previous.getTarget().printString();
				if (text.contains("\n") || text.startsWith("\"")) {
					writer.write("\"");
				}
				writer.write(text);
				if (text.contains("\n") || text.startsWith("\"")) {
					writer.write("\"");
				}
				writer.write("\r\n");
			}
		}
		meta = relationship.getMeta().orderedRelationships(Primitive.TOPIC);
		if (meta != null) {
			for (Relationship topic : meta) {
				boolean requireTopic = relationship.getMeta().hasRelationship(Primitive.REQUIRE, Primitive.TOPIC);
				boolean exclusiveTopic = topic.getTarget().hasRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
				writer.write(tab);
				if (requireTopic) {
					writer.write("require topic: ");
				} else if (exclusiveTopic) {
					writer.write("exclusive topic: ");
				} else {
					writer.write("topic: ");
				}
				writer.write(topic.getTarget().printString());
				writer.write("\r\n");
			}
		}
		Vertex vertex = relationship.getMeta().getRelationship(Primitive.CONDITION);
		if (vertex != null) {
			writer.write(tab);
			writer.write("condition: ");
			writer.write(getCondition(vertex, false));
			writer.write("\r\n");
		}
		vertex = relationship.getMeta().getRelationship(Primitive.THINK);
		if (vertex != null) {
			writer.write(tab);
			writer.write("think: ");
			writer.write(getThink(vertex, false));
			writer.write("\r\n");
		}
		vertex = relationship.getMeta().getRelationship(Primitive.COMMAND);
		if (vertex != null) {
			writer.write(tab);
			writer.write("command: ");
			writer.write(getCommand(vertex, false));
			writer.write("\r\n");
		}
		List<EmotionalState> emotes = getBot().mood().evaluateEmotionalStates(relationship.getMeta());
		if (!emotes.isEmpty()) {
			writer.write(tab);
			writer.write("emotions: ");
			int index = 0;
			for (EmotionalState emote : emotes) {
				boolean isSentiment = emote.isSentiment();
				if (isSentiment) {
					continue;
				}
				index++;
				writer.write(emote.name().toLowerCase());
				if (index < emotes.size()) {
					writer.write(" ");
				}
			}
			writer.write("\r\n");
		}
		meta = relationship.getMeta().orderedRelationships(Primitive.ACTION);
		if (meta != null) {
			writer.write(tab);
			writer.write("actions: ");
			for (Relationship action : meta) {
				writer.write(action.getTarget().getDataValue());
				writer.write(" ");
			}
			writer.write("\r\n");
		}
		meta = relationship.getMeta().orderedRelationships(Primitive.POSE);
		if (meta != null) {
			writer.write(tab);
			writer.write("poses: ");
			for (Relationship pose : meta) {
				writer.write(pose.getTarget().getDataValue());
				writer.write(" ");
			}
			writer.write("\r\n");
		}
		meta = relationship.getMeta().orderedRelationships(Primitive.NEXT);
		if (meta != null) {
			tab = tab + "\t";
			for (Relationship next : meta) {
				if (next.getMeta() != null) {
					List<Relationship> nextResponseList = next.getMeta().orderedRelationships(Primitive.RESPONSE);
					if (nextResponseList != null && !nextResponseList.isEmpty()) {
						for (Relationship nextResponse : nextResponseList) {
							writer.write("\r\n");
							writer.write(tab);
							if (next.getTarget().is(Primitive.DEFAULT)) {
								writer.write("default: ");
							} else {
								writer.write(next.getTarget().printString());
								writer.write("\r\n");
								writer.write(tab);
							}
							String text = nextResponse.getTarget().printString();
							if (text.contains("\n") || text.startsWith("\"")) {
								writer.write("\"");
							}
							writer.write(text);
							if (text.contains("\n") || text.startsWith("\"")) {
								writer.write("\"");
							}
							writer.write("\r\n");
							exportResponseMetadata(writer, nextResponse, isGreeting, isDefault, counter + 1);
						}
					}
				}
			}
		}
	}

	public void processFlag(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			throw new BotException("No phrases selected");			
		}
		for (Object[] data : vertices) {
			if (data[0] instanceof Vertex) {
				Vertex phrase = (Vertex)data[0];
				phrase.addRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
				if (phrase.instanceOf(Primitive.INPUT)) {
					Vertex input = phrase.getRelationship(Primitive.INPUT);
					input.setPinned(true);
					input.addRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
				} else {
					phrase.setPinned(true);
				}
			} else if (data[0].toString().equals(WORDS)) {
				Vertex word = (Vertex)data[1];
				word.addRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
				word.setPinned(true);
			}
		}
		memory.save();
	}

	public void processUnflag(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		if (vertices.isEmpty()) {
			throw new BotException("No phrases selected");			
		}
		for (Object[] data : vertices) {
			if (data[0] instanceof Vertex) {
				Vertex phrase = (Vertex)data[0];
				phrase.removeRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
				if (phrase.instanceOf(Primitive.INPUT)) {
					phrase.getRelationship(Primitive.INPUT).removeRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
				}
			} else if (data[0].toString().equals(WORDS)) {
				Vertex word = (Vertex)data[1];
				word.removeRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
			}
		}
		memory.save();
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		reset();
	}
	
	public void reset() {
		this.results = null;
		this.responses = null;
		this.meta = null;
		this.allLabels = null;
		this.duration = "";
		this.search = CONVERSATIONS;
		this.type = "";
		this.page = 0;
		this.resultsSize = 0;
		this.filter = "";
		this.restriction =  "none";
		this.sort =  DATE;
		this.correction = false;
		this.isPhrase = false;
	}

	public List<Vertex> getResults() {
		return results;
	}

	public void setResults(List<Vertex> results) {
		this.results = results;
	}

	public List<Vertex> getResponses() {
		return responses;
	}

	public void setResponses(List<Vertex> responses) {
		this.responses = responses;
	}
	
	public List<Vertex> getMetaList() {
		return meta;
	}
	
	public List<Vertex> getAllResults() {
		return allResults;
	}

	public List<Vertex> getAllResponses() {
		return allResponses;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processQueryAllResponses() {
		this.correction = false;
		this.isPhrase = false;
		Network memory = getBot().memory().newMemory();

		this.search = "responses";
		this.duration = "all";
		this.type = "all";
		this.restriction = "none";
		
		CriteriaQuery criteria = buildQuery(memory, false);		
		this.results = memory.search(criteria, this.page, this.pageSize);
		if (this.page == 0) {
			this.resultsSize = this.results.size();
			if (this.resultsSize >= this.pageSize) {
				criteria = buildQuery(memory, true);
				this.resultsSize = ((Number)memory.search(criteria, 0, 1).get(0)).intValue();
			}
		}
	}
	
	private ResponseConfig processSaveData(Vertex question, Vertex response, String newQuestion, String next, String previous, String keywords, String required,
			String onrepeat, String condition, String think, String command, String confidence, String sentiment,
			String emotes, String actions, String poses, String respEmotes, String respActions, String respPoses,
			String metaId, String topic, String label, String correction,
			boolean noRepeat, boolean autoReduce, boolean requireTopic, boolean exclusiveTopic, boolean requirePrevious,
			String _parentQuestionId, String _parentResponseId) {

		ResponseConfig config = new ResponseConfig();
		
		Network memory = getBot().memory().newMemory();
		Map<Vertex, Vertex> phraseMap = new HashMap<Vertex, Vertex>();
		
		if (autoReduce && !newQuestion.startsWith("Pattern(")) {
			setAutoReduce(true);
			newQuestion = Utils.reduce(newQuestion);
		} else {
			setAutoReduce(false);
		}

		Vertex questionMeta = null;
		Vertex responseMeta = null;
		if (metaId != null && !metaId.isEmpty()) {
			// For next the parent meta id is written in a hidden input.
			questionMeta = memory.findById(Long.valueOf(metaId));
			
			AdminDatabase.instance().log(Level.INFO, "question meta: " + questionMeta + ", question: " + question);

			if (question == null) {
				question = memory.createVertex("");
				questionMeta.addRelationship(Primitive.NEXT, question);
			}
			// Use the parent meta to find the next response meta.
			Relationship relationship = questionMeta.getRelationship(Primitive.NEXT, question);
	
			if (relationship != null) {
				responseMeta = memory.createMeta(relationship);
			}
			
		}
		if (onrepeat != null) {
			onrepeat = onrepeat.trim();
		}
		if (topic != null) {
			topic = topic.trim();
		}
		if (label != null) {
			label = label.trim();
			if (label.startsWith("#")) {
				label = label.substring(1, label.length());
			}
			if (label.isEmpty()) {
				label = null;
			} else {
				if (!Utils.isAlphaNumeric(label)) {
					throw new BotException("A label must be a single alpha numeric string with no spaces (use - for a space) - " + label);
				}
			}
		}
		if (correction != null) {
			correction = correction.trim();
		}
		if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(correction, getBotBean().getInstance().getContentRatingLevel())) {
			throw BotException.offensive();
		}
		if (!this.loginBean.isSuper()) {
			Utils.checkScript(correction);
		}
		if ((correction != null) && !correction.isEmpty()) {
			Vertex newResponse = null;
			boolean labelAnswer = false;
			if (correction.startsWith("#")) {
				newResponse = createLabel(correction, memory);
				labelAnswer = true;
			} else if (correction.contains("{") && correction.contains("}")
						&& !correction.toLowerCase().startsWith("template") && !correction.toLowerCase().startsWith("formula")) {
				newResponse = memory.createTemplate("Template(\"" + correction + "\")");
			} else {
				newResponse = memory.createSentence(correction);
				SelfCompiler.getCompiler().pin(newResponse);
			}
			newResponse.setPinned(true);
			if (newResponse.instanceOf(Primitive.FORMULA)) {
				SelfCompiler.getCompiler().pin(newResponse);
			}
			Vertex oldNextResponseMeta = responseMeta;
			if (questionMeta != null || (!this.search.equals(GREETINGS) && !this.search.equals(DEFAULT))) {
				Vertex oldQuestion = question;
				if (newQuestion != null) {
					if (!newQuestion.equals(question.printString())) {
						if (questionMeta != null) {
							Relationship relationship = questionMeta.getRelationship(Primitive.NEXT, question);
							if (relationship != null) {
								questionMeta.internalRemoveRelationship(relationship);
							} 
						}
						if (newQuestion.equals("#default")) {
							question = memory.createVertex(Primitive.DEFAULT);
						} else if (newQuestion.contains("*") && !newQuestion.toLowerCase().startsWith("pattern")) {
							question = memory.createPattern(newQuestion);
						} else {
							question = memory.createSentence(newQuestion);
							SelfCompiler.getCompiler().pin(question);
						}
						if (questionMeta != null) {
							Relationship relationship = questionMeta.addRelationship(Primitive.NEXT, question);
							responseMeta = memory.createMeta(relationship);
						}
					}
				}
				phraseMap.put(oldQuestion, question);
				
				// Check for phrase without response.
				if (!type.equals("new phrase with no response")) {
					Vertex oldResponseMeta = null;
					if (response != null && (!newResponse.equals(response) || oldQuestion != question)) {
						if (oldNextResponseMeta != null) {
							Relationship relationship = oldNextResponseMeta.getRelationship(Primitive.RESPONSE, response);
							if (relationship != null) {
								oldResponseMeta = relationship.getMeta();
								oldNextResponseMeta.internalRemoveRelationship(relationship);
							}
						} else {
							Relationship relationship = oldQuestion.getRelationship(Primitive.RESPONSE, response);
							if (relationship != null) {
								oldResponseMeta = relationship.getMeta();
								oldQuestion.internalRemoveRelationship(relationship);
								Relationship inverse = response.getRelationship(Primitive.QUESTION, oldQuestion);
								if (inverse != null) {
									response.internalRemoveRelationship(inverse);
								}
							}
							Collection<Relationship> reductions = oldQuestion.getRelationships(Primitive.REDUCTION);
							if (reductions != null) {
								for (Relationship reduction : reductions) {
									relationship = reduction.getTarget().getRelationship(Primitive.RESPONSE, response);
									if (relationship != null) {
										reduction.getTarget().internalRemoveRelationship(relationship);
										Relationship inverse = response.getRelationship(Primitive.QUESTION, reduction.getTarget());
										if (inverse != null) {
											response.internalRemoveRelationship(inverse);
										}
									}
								}
							}
							reductions = oldQuestion.getRelationships(Primitive.SYNONYM);
							if (reductions != null) {
								for (Relationship reduction : reductions) {
									relationship = reduction.getTarget().getRelationship(Primitive.RESPONSE, response);
									if (relationship != null) {
										reduction.getTarget().internalRemoveRelationship(relationship);
										Relationship inverse = response.getRelationship(Primitive.QUESTION, reduction.getTarget());
										if (inverse != null) {
											response.internalRemoveRelationship(inverse);
										}
									}
								}
							}
						}
					}
					Vertex previousResponse = null;
					if (previous != null && !previous.isEmpty()) {
						if (previous.startsWith("#")) {
							previousResponse = createLabel(previous, memory);
						} else {
							previousResponse = memory.createSentence(previous);
							SelfCompiler.getCompiler().pin(previousResponse);
						}
					}
					Vertex nextResponse = null;
					if (next != null && !next.isEmpty()) {
						if (next.equals("#default")) {
							nextResponse = memory.createVertex(Primitive.DEFAULT);
						} else {
							nextResponse = memory.createSentence(next);
							SelfCompiler.getCompiler().pin(nextResponse);
						}
					}
					if (responseMeta == null) {
						// Use the question and the response key if not nested.
						responseMeta = question;
					}
					// 100%
					Relationship responseRelationship = responseMeta.addRelationship(Primitive.RESPONSE, newResponse);
					responseRelationship.setCorrectness(Math.max(1.0f, responseRelationship.getCorrectness()));
					config.correctness = String.valueOf(responseRelationship.getCorrectness());
					
					// Re-add the previous and next to the new response if the response or question was changed.
					if (oldResponseMeta != null) {
						// Copy previous and next over to new response.
						List<Relationship> relationships = oldResponseMeta.orderedRelationships(Primitive.PREVIOUS);
						if (relationships != null) {
							for (Relationship relationship : relationships) {
								memory.createMeta(responseRelationship).addWeakRelationship(relationship.getType(), relationship.getTarget(), relationship.getCorrectness());
							}
						}
						relationships = oldResponseMeta.orderedRelationships(Primitive.NEXT);
						if (relationships != null) {
							for (Relationship relationship : relationships) {
								Relationship nextRelationship = memory.createMeta(responseRelationship).addWeakRelationship(relationship.getType(), relationship.getTarget(), relationship.getCorrectness());
								if (relationship.hasMeta()) {
									// Copy meta.
									nextRelationship.setMeta(relationship.getMeta());
									relationship.setMeta(null);
								}
							}
						}
					}
					
					if (confidence != null && !confidence.isEmpty()) {
						Language.setConfidence(question, newResponse, Primitive.RESPONSE, confidence, memory);
					}
					if (questionMeta == null) {
						if (!newResponse.hasRelationship(Primitive.INSTANTIATION, Primitive.WORD)) {
							// Don't associate words, as conflicts with word question relationship.
							newResponse.addRelationship(Primitive.QUESTION, question);
						}
						question.setPinned(true);
					}
					if (getBot().mind().getThought(Language.class).getReduceQuestions() && !autoReduce) {
						memory.checkReduction(question);
						question.associateAll(Primitive.REDUCTION, newResponse, Primitive.RESPONSE);
					}
					if (noRepeat) {
						newResponse.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
					} else {
						Relationship relationship = newResponse.getRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
						if (relationship != null) {
							newResponse.internalRemoveRelationship(relationship);
						}
					}
					if (onrepeat != null && !onrepeat.trim().isEmpty()) {
						Vertex onrepeatValue = memory.createSentence(onrepeat);
						newResponse.addRelationship(Primitive.ONREPEAT, onrepeatValue);
						SelfCompiler.getCompiler().pin(onrepeatValue);
					}
					if (!question.instanceOf(Primitive.PATTERN)) {
						question.associateAll(Primitive.WORD, question, Primitive.QUESTION);
					}
					Language.addSentencePreviousMeta(responseMeta, newResponse, previousResponse, requirePrevious, memory);
					Language.addSentenceNextMeta(responseMeta, newResponse, nextResponse, memory);
					Language.addSentenceKeyWordsMeta(responseMeta, newResponse, keywords, memory);
					Language.addSentenceRequiredMeta(responseMeta, newResponse, required, memory);
					Language.addSentenceTopicMeta(responseMeta, newResponse, topic, requireTopic, exclusiveTopic, memory);
					Language.addSentenceConditionMeta(responseMeta, newResponse, condition, true, memory);
					Language.addSentenceThinkMeta(responseMeta, newResponse, think, true, memory);
					Language.addSentenceCommandMeta(responseMeta, newResponse, command, true, memory);
					Language.addSentenceEmotesMeta(responseMeta, newResponse, respEmotes, memory);
					Language.addSentenceActionMeta(responseMeta, newResponse, respActions, memory);
					Language.addSentencePoseMeta(responseMeta, newResponse, respPoses, memory);
					if (label != null && !labelAnswer) {
						Vertex labelVertex = memory.createVertex(new Primitive(label));
						labelVertex.addRelationship(Primitive.INSTANTIATION, Primitive.LABEL);
						newResponse.setRelationship(Primitive.LABEL, labelVertex);
						labelVertex.setRelationship(Primitive.RESPONSE, newResponse);
						this.allLabels = null;
					} else {
						Vertex oldLabel = newResponse.getRelationship(Primitive.LABEL);
						if (oldLabel != null) {
							oldLabel.internalRemoveRelationship(oldLabel.getRelationship(Primitive.INSTANTIATION, Primitive.LABEL));
							oldLabel.internalRemoveRelationship(oldLabel.getRelationship(Primitive.RESPONSE, newResponse));
							newResponse.internalRemoveRelationships(Primitive.LABEL);
						}
					}
				}
				if (emotes != null && !emotes.equals("")) {
					question.internalRemoveRelationships(Primitive.EMOTION);
					for (String emote : Utils.getWords(emotes)) {
						if (!emote.equals("none")) {
							try {
								EmotionalState.valueOf(emote.toUpperCase()).apply(question);
							} catch (Exception exception) {
								throw new BotException("Invalid emotion: " + emote);
							}
						}
					}
				}
				if (sentiment != null && !sentiment.equals("")) {
					for (String emote : Utils.getWords(sentiment)) {
						if (!emote.equals("none")) {
							try {
								EmotionalState.valueOf(emote.toUpperCase()).apply(question);
							} catch (Exception exception) {
								throw new BotException("Invalid sentiment: " + emote);
							}
						}
					}
				} 
				if (actions != null && !actions.equals("")) {
					question.internalRemoveRelationships(Primitive.ACTION);
					for (String action : Utils.getWords(actions)) {
						if (!action.equals("none")) {
							question.addRelationship(Primitive.ACTION, new Primitive(action));
						}
					}
				}
				if (poses != null && !poses.equals("")) {
					question.internalRemoveRelationships(Primitive.POSE);
					for (String pose : Utils.getWords(poses)) {
						if (!pose.equals("none")) {
							question.addRelationship(Primitive.POSE, new Primitive(pose));
						}
					}
				}
			} else {
				Vertex language = memory.createVertex(getBot().mind().getThought(Language.class).getPrimitive());
				Relationship relationship = null;
				Primitive type = null;
				if (this.search.equals(GREETINGS)) {
					type = Primitive.GREETING;
				} else {
					type = Primitive.RESPONSE;
				}
				if (question != null && !newResponse.equals(question)) {
					relationship = language.getRelationship(type, question);
					phraseMap.put(question, newResponse);
					if (relationship != null) {
						language.internalRemoveRelationship(relationship);
					}
				}
				Vertex previousResponse = null;
				if (previous != null && !previous.isEmpty()) {
					if (previous.startsWith("#")) {
						previousResponse = createLabel(previous, memory);
					} else {
						previousResponse = memory.createSentence(previous);
						SelfCompiler.getCompiler().pin(previousResponse);
					}
				}
				Vertex nextResponse = null;
				if (next != null && !next.isEmpty()) {
					if (next.startsWith("#")) {
						nextResponse = createLabel(next, memory);
					} else {
						nextResponse = memory.createSentence(next);
						SelfCompiler.getCompiler().pin(nextResponse);
					}
				}
				// 100%
				
				Vertex oldResponseMeta = null;
				if(relationship != null) {
					oldResponseMeta = relationship.getMeta();
				}
				relationship = language.addRelationship(type, newResponse);
				relationship.setCorrectness(Math.max(1.0f, relationship.getCorrectness()));
				
				if (oldResponseMeta != null) {
					// Copy previous and next over to new response.
					List<Relationship> relationships = oldResponseMeta.orderedRelationships(Primitive.PREVIOUS);
					if (relationships != null) {
						for (Relationship rel : relationships) {
							memory.createMeta(relationship).addWeakRelationship(rel.getType(), rel.getTarget(), rel.getCorrectness());
						}
					}
					relationships = oldResponseMeta.orderedRelationships(Primitive.NEXT);
					if (relationships != null) {
						for (Relationship rel : relationships) {
							Relationship nextRelationship = memory.createMeta(relationship).addWeakRelationship(rel.getType(), rel.getTarget(), rel.getCorrectness());
							if (rel.hasMeta()) {
								// Copy meta.
								nextRelationship.setMeta(rel.getMeta());
								rel.setMeta(null);
							}
						}
					}
				}
					
				if (noRepeat) {
					newResponse.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				}
				if (onrepeat != null && !onrepeat.isEmpty()) {
					Vertex onrepeatValue = memory.createSentence(onrepeat);
					newResponse.addRelationship(Primitive.ONREPEAT, onrepeatValue);
					SelfCompiler.getCompiler().pin(onrepeatValue);
				}
				Language.addSentencePreviousMeta(language, newResponse, type, previousResponse, requirePrevious, memory);
				Language.addSentenceNextMeta(language, newResponse, type, nextResponse, memory);
				Language.addSentenceTopicMeta(language, newResponse, type, topic, requireTopic, exclusiveTopic, memory);
				Language.addSentenceConditionMeta(language, newResponse, type, condition, true, memory);
				Language.addSentenceThinkMeta(language, newResponse, type, think, true, memory);
				Language.addSentenceCommandMeta(language, newResponse, type, command, true, memory);
				Language.addSentenceEmotesMeta(language, newResponse, type, respEmotes, memory);
				Language.addSentenceActionMeta(language, newResponse, type, respActions, memory);
				Language.addSentencePoseMeta(language, newResponse, type, respPoses, memory);
				if (label != null && !labelAnswer) {
					Vertex labelVertex = memory.createVertex(new Primitive(label));
					labelVertex.addRelationship(Primitive.INSTANTIATION, Primitive.LABEL);
					newResponse.setRelationship(Primitive.LABEL, labelVertex);
					labelVertex.setRelationship(Primitive.RESPONSE, newResponse);
					this.allLabels = null;
				} else {
					Vertex oldLabel = newResponse.getRelationship(Primitive.LABEL);
					if (oldLabel != null) {
						oldLabel.internalRemoveRelationship(oldLabel.getRelationship(Primitive.INSTANTIATION, Primitive.LABEL));
						oldLabel.internalRemoveRelationship(oldLabel.getRelationship(Primitive.RESPONSE, newResponse));
						newResponse.internalRemoveRelationships(Primitive.LABEL);
					}
				}
			}
			
			if(question != null) {
				config.questionId = question.getId().toString();
				config.question = newQuestion;
				
				if(questionMeta != null) {
					config.metaId = questionMeta.getId().toString();
				}
			}
			if(newResponse != null) {
				config.responseId = newResponse.getId().toString();
				config.response = correction;
						
				if(this.search.equals(GREETINGS) || this.search.equals(DEFAULT)) {
					config.questionId = config.responseId;
				}
				
			}
		} else {
			if (noRepeat) {
				question.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
			} else {
				Relationship relationship = question.getRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				if (relationship != null) {
					question.internalRemoveRelationship(relationship);
				}
			}
			if (onrepeat != null && !onrepeat.isEmpty()) {
				Vertex onrepeatValue = memory.createSentence(onrepeat);
				question.addRelationship(Primitive.ONREPEAT, onrepeatValue);
				SelfCompiler.getCompiler().pin(onrepeatValue);
			}
		}
		
		memory.save();
		
		return config;
	}
	
	private void setSearchFromConfigType(ResponseConfig config) {
		if(config.type.equals(Primitive.GREETING.getIdentity()) || config.type.equals(GREETINGS)) {
			this.search = GREETINGS;
		} else if(config.type.equals(Primitive.DEFAULT.getIdentity())) {
			this.search = DEFAULT;
		} else if(config.type.equals(Primitive.RESPONSE.getIdentity())) {
			this.search = RESPONSES;
		} else if(config.type.equals(Primitive.WORD.getIdentity())) {
			this.search = WORDS;
		}
	}
	
	public String displayQuestionVertexHTML(Vertex question) {
		StringWriter writer = new StringWriter();
		writeQuestionVertexHTML(writer, question);
		return writer.toString();
	}
	
	private void writeQuestionVertexHTML(StringWriter writer, Vertex question) {
		ChatLogBean bean = this;

		boolean isResponses = getSearch().equals(ChatLogBean.RESPONSES);
		boolean isDefault = getSearch().equals(ChatLogBean.DEFAULT);
		boolean isGreeting = getSearch().equals(ChatLogBean.GREETINGS);
		boolean isPhrase = getSearch().equals(ChatLogBean.PHRASES) || isPhrase();
		
		writer.write("<tr id='question-row-" + question.getId() + "'><td><table>");
		
		writer.write("<tr>");
		writer.write("<td></td>");
		
		boolean offensive = question.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
		
		writer.write("<tr id='question-row-tr-" + question.getId() + "'>");
		
		if(isDefault || isGreeting) {
			String responseId = question.getId() + "-" + question.getId();
			writer.write("<td valign='top'>");
			writer.write("<div class='toolbar'>");
			writer.write("<span class='dropt'>");
			writer.write("<div class='menu' style='display:inline-flex;margin-top:-11px;height:40px;'>");
			writer.write("<div class='gear-menu'>");
			writer.write("<a href='#' id='add-question-response-" + responseId + "' onclick='showQuickAddNextResponse(this); return false;' title='" + loginBean.translate("Enter a new follow-up next question and response") + "'>");
			writer.write("<img src='images/plus.png' class='gear-menu' style='width:32px;max-width:none;'>");
			writer.write("</a>");
			writer.write("</div>");
			writer.write("<div class='gear-menu'>");
			writer.write("<a href='#' id='edit-question-response-" + responseId + "' onclick='editQuestionResponse(this); return false;' title='" + loginBean.translate("Edit the response") +"'>");
			writer.write("<img src='images/edit.png' class='gear-menu' style='width:32px;max-width:none;'>");
			writer.write("</a>");
			writer.write("</div>");
			writer.write("<div class='gear-menu'>");
			writer.write("<a href='#' id='delete-question-response-" + responseId + "' onclick='deleteQuestionResponse(this); return false;' title='" + loginBean.translate("Delete the response") +"'>");
			writer.write("<img src='images/remove.png' class='gear-menu' style='width:32px;max-width:none;'>");
			writer.write("</a></div></div>");
			writer.write("<img src='images/admin.svg' class='gear-icon'>");
			writer.write("</span></div></td>");
		} else {
			writer.write("<td></td>");
		}
		
		writer.write("<td valign='top'><input type='checkbox' " + ((bean.isSelectAll() && (isPhrase || isDefault || isGreeting)) ? "checked" : "") + " name='phrase:" + question.getId() + "' title=" + loginBean.translate("Select phrase for flagging") + "></td>");
		writer.write("<td>");
		if (!isGreeting && !isDefault) {
			writer.write("<span class='chat' title='" + loginBean.translate("The percentage correctness of the response; 100% means great response, -100% means terrible response") + "'>%</span>");
		}
		writer.write("</td>");
		
		writer.write("<td id='question-td-id-" + question.getId() + "' style='width:100%;'><span class='" + (offensive ? "chat-flagged" : "chat") + "'>" + Utils.escapeHTML(question.printString()) + "</span></td>");
		writer.write("</tr>");
		
		String sentiment = bean.getSentiment(question);
		if (!sentiment.isEmpty()) {
			writer.write("<tr>");
			writer.write("<td></td>");
			writer.write("<td></td>");
			writer.write("<td></td>");
			writer.write("<td><span class='chat-sentiment' title='" + loginBean.translate("The user's sentiment (feeling) for the question (good vs bad). Sentiment can be used to track the user's experience, and is available in the bot's analytics.") + "'>" + sentiment + "</span></td>");
			writer.write("</tr>");
		}
		
		if (question.getRelationships(Primitive.EMOTION) != null) {
			String emotes = bean.getEmotes(question);
			if (!emotes.isEmpty()) {
				writer.write("<tr>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("<td style='width:100%;'><span class='chat-emote'>" + emotes + "</span></td>");
				writer.write("</tr>");
			}
		}
		
		if (question.hasRelationship(Primitive.ACTION)) {
			String actions = bean.getActions(question);
			if (!actions.isEmpty()) {
				writer.write("<tr>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("<td style='width:100%;'><span class='chat-action'>" + actions + "</span></td>");
				writer.write("</tr>");
			}
		}
		
		if (question.hasRelationship(Primitive.POSE)) {
			String poses = bean.getPoses(question);
			if (!poses.isEmpty()) {
				writer.write("<tr>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("<td></td>");
				writer.write("<td style='width:100%;'><span class='chat-pose'>" + poses+ "</span></td>");
				writer.write("</tr>");
			}
		}
		
		List<Relationship> responses = question.orderedRelationships(Primitive.RESPONSE);
		if (isDefault) {
			responses = bean.getDefaultResponseRelationships(question);
		}
		if (isGreeting) {
			responses = bean.getGreetingRelationships(question);
		}
		
		if (responses != null) {
			//writer.write("<tr id ='question-response-table-tr-" + question.getId() + "'>");
			//writer.write("<td></td><td></td><td></td><td>");
			//writer.write("<table id='question-response-table-" + question.getId() + "'>");
			
			for (Relationship answer : responses) {
				if (bean.checkFilter(question, answer)) continue;
				String style = "chat-response";
				if (answer.getTarget().hasRelationship(Primitive.INSTANTIATION, Primitive.LABEL)) { style = "chat-label"; };
				if (answer.getTarget().hasRelationship(Primitive.REQUIRE, Primitive.NOREPEAT)) { style = "chat-repeat"; };
				if (answer.getCorrectness() < 0) { style = "chat-inverse"; };
				if (answer.getTarget().hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) { style = "chat-flagged"; };
				String responseId = question.getId() + "-" + answer.getTarget().getId();
				writer.write("<tr id='question-response-row-" + responseId + "'>");
			
				if (isDefault || isGreeting) {	
					writer.write("<td></td><td></td><td></td>");
					writer.write("<td id='response-td-id-" + responseId + "'>");
				} else {
					writer.write("<td valign='top'>");
					writer.write("<div class='toolbar'>");
					writer.write("<span class='dropt'>");
					writer.write("<div class='menu' style='display:inline-flex;margin-top:-11px;height:40px;'>");
					writer.write("<div class='gear-menu'>");
					writer.write("<a href='#' id='add-question-response-" + responseId + "' onclick='showQuickAddNextResponse(this); return false;' title='" + loginBean.translate("Enter a new follow-up next question and response") + "'>");
					writer.write("<img src='images/plus.png' class='gear-menu' style='width:32px;max-width:none;'>");
					writer.write("</a></div>");
					writer.write("<div class='gear-menu'>");
					writer.write("<a href='#' id='edit-question-response-" + responseId + "' onclick='editQuestionResponse(this); return false;' title='" +  loginBean.translate("Edit the question and response") + "'>");
					writer.write("<img src='images/edit.png' class='gear-menu' style='width:32px;max-width:none;'>");
					writer.write("</a></div>");
					writer.write("<div class='gear-menu'>");
					writer.write("<a href='#' id='delete-question-response-" + responseId + "' onclick='deleteQuestionResponse(this); return false;' title='" + loginBean.translate("Delete the response") + "'>");
					writer.write("<img src='images/remove.png' class='gear-menu' style='width:32px;max-width:none;'>");
					writer.write("</a></div></div>");
					writer.write("<img src='images/admin.svg' class='gear-icon'>");
					writer.write("</span></div></td>");
					writer.write("<td valign='top'><input type=checkbox " + ((bean.isSelectAll() && isResponses) ? "checked" : "") + " name='response:" + question.getId() + ":" + answer.getTarget().getId() +"'  title='" + loginBean.translate("Select response for validation, invalidation, flagging, or deletion") +"'></td>");
					writer.write("<td valign='top'><span class='chat'>" + (int)(answer.getCorrectness() * 100) + "</span></td>");
					writer.write("<td valign='top' id='response-td-id-" + responseId + "' style='width:100%;'><span id='response-id-" + answer.getTarget().getId() + "' class='" + style + "'>" + bean.getResponse(answer.getTarget()) + "</span>");
				}
				
				writeMetaDataTable(writer, question, answer, null, responseId);
				
				if (answer.hasMeta() && answer.getMeta().orderedRelationships(Primitive.NEXT) != null) {
					writer.write("<span><img src='images/circle-plus.png' class='menu-small' id='expand-table-button-" + answer.getMeta().getId() + "'></span>");
					//TODO: Possibly put script part in a function, duplicated in writeNextResponseTable and might be needed to be separate for edit
					writer.write("<script>");
					writer.write("$(function() {");
					//writer.write("$('#table-meta-" + answer.getMeta().getId() + "').toggle();");
					writer.write("$('#expand-table-button-" + answer.getMeta().getId() + "').click(function() {");
					writer.write("$('#table-meta-" + answer.getMeta().getId() + "').toggle();");
					writer.write("var src = ($(this).attr('src') === 'images/circle-plus.png') ? 'images/circle-minus.png' : 'images/circle-plus.png';");
					writer.write("$('#expand-table-button-" + answer.getMeta().getId() + "').attr('src', src);");
					writer.write("});");
					writer.write("});");
					writer.write("</script>");
					writer.write(bean.displayNextResponseTable(0, answer, 0));
				}
			}
			//writer.write("</table></td></tr>");
		}
		//writer.write("<tr id='row-line-" + question.getId() + "'><td colspan='4'><hr/></td></tr>");
		
		writer.write("</table></td></tr>");
	}
	
	private void writeMetaDataTable(StringWriter writer, Vertex next, Relationship nextResponse, Vertex responseMeta, String responseId) {
		if (nextResponse.getMeta() != null) {
			writer.write("<table class='meta-table' id='meta-data-table-" + responseId + "'>");
			String data = "";
			Vertex meta = nextResponse.getMeta().getRelationship(Primitive.CONDITION);
			if(meta != null) {
				data = this.getCondition(meta, true);
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-condition-" + responseId + "' class='chat-template chat-condition'>" + data + "</span></td></tr>");
				}
			}
			meta = nextResponse.getMeta().getRelationship(Primitive.THINK);
			if(meta != null) {
				data = this.getThink(meta, true);
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-think-" + responseId + "' class='chat-template chat-think'>" + data + "</span></td></tr>");
				}
			}
			meta = nextResponse.getMeta().getRelationship(Primitive.COMMAND);
			if(meta != null) {
				data = this.getCommand(meta, true);
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-command-" + responseId + "' class='chat-template chat-command'>" + data + "</span></td></tr>");
				}
			}
			String emotes = getEmotes(next, nextResponse.getTarget(), responseMeta);
			if(emotes != null && !emotes.isEmpty()) {
				writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-emote-" + responseId + "' class='chat-emote'>" + emotes + "</span></td></tr>");
			}	
			String actions = getActions(next, nextResponse.getTarget(), responseMeta);
			if(actions != null && !actions.isEmpty()) {
				writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-action-" + responseId + "' class='chat-action'>" + actions + "</span></td></tr>");	
			}
			String poses = getPoses(next, nextResponse.getTarget(), responseMeta);
			if(poses != null && !poses.isEmpty()) {	
				writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-pose-" + responseId + "' class='chat-pose'>" + poses + "</span></td></tr>");				
			}
			meta = nextResponse.getMeta().getRelationship(Primitive.TOPIC);
			if(meta != null) {
				data = this.getTopic(next, nextResponse.getTarget(), responseMeta);
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-topic-" + responseId + "' class='chat-topic'>" + data + "</span></td></tr>");
				}
			}
			meta = nextResponse.getTarget().getRelationship(Primitive.LABEL);
			if(meta != null) {
				data = this.getLabel(nextResponse.getTarget());
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-label-" + responseId + "' class='chat-label'>" + data + "</span></td></tr>");
				}
			}
			meta = nextResponse.getMeta().getRelationship(Primitive.KEYWORD);
			if(meta != null) {
				data = this.getKeyWords(next, nextResponse.getTarget(), responseMeta);
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-keyword-" + responseId + "' class='chat-keyword'>" + data + "</span></td></tr>");
				}
			}
			meta = nextResponse.getMeta().getRelationship(Primitive.REQUIRED);
			if(meta != null) {
				data = this.getRequired(next, nextResponse.getTarget(), responseMeta);
				if(data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-required-" + responseId + "' class='chat-required'>" + data + "</span></td></tr>");
				}
			}
			List<Relationship> previousRelationships = nextResponse.getMeta().orderedRelationships(Primitive.PREVIOUS);
			if (previousRelationships != null) {
				writer.write("<tr class='meta-table'><td><table class='meta-table'>");
				String previousTitle = this.loginBean.translate("Select previous response for validation, invalidation, or deletion");
				for (Relationship previous : previousRelationships) {
					String previousStyle = nextResponse.getMeta().hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)  ? "chat-require-previous" : "chat-previous";
					if (previous.getCorrectness() < 0) { previousStyle = "chat-inverse"; };
					writer.write("<tr class='edit-previous-tr'>");
					writer.write("<td valign='top'><input type='checkbox' name='previous:" + String.valueOf(nextResponse.getMeta().getId()) + ":" + String.valueOf(previous.getTarget().getId()) + "' title='" + previousTitle + "'></td>");
					writer.write("<td valign='top'><span class='chat'>" + String.valueOf((int)(previous.getCorrectness() * 100)) + "</span></td>");
					writer.write("<td style='width:100%;'><span class='" + previousStyle + "'>" + Utils.escapeHTML(previous.getTarget().printString()) + "</span></td>");
					writer.write("</tr>\n");
				}
				writer.write("</table></td></tr>");
			}
			List<Relationship> repeatRelationships = nextResponse.getTarget().orderedRelationships(Primitive.ONREPEAT);
			if (repeatRelationships != null) {
				writer.write("<tr class='meta-table'><td><table class='meta-table'>");
				String onRepeatTitle = this.loginBean.translate("Select on repeat response for validation, invalidation, or deletion");
				for (Relationship repeat : repeatRelationships) {
					String onRepeatStyle = "chat-repeat";
					if (repeat.getCorrectness() < 0) {
						onRepeatStyle = "chat-inverse";
					};
					writer.write("<tr class='edit-repeat-tr'>");
					writer.write("<td valign='top'><input type='checkbox' name='repeat:" + String.valueOf(nextResponse.getTarget().getId()) + ":" + String.valueOf(repeat.getTarget().getId()) + "' title='" + onRepeatTitle + "'></td>");
					writer.write("<td valign='top'><span class='chat'>" + String.valueOf((int)(repeat.getCorrectness() * 100)) + "</span></td>");
					writer.write("<td style='width:100%;'><span class='" + onRepeatStyle + "'>" + Utils.escapeHTML(repeat.getTarget().printString()) + "</span></td>");
					writer.write("</tr>\n");
				}
				writer.write("</table></td></tr>");
			}
			
			writer.write("</table>");
		} else {
			writer.write("<table class='meta-table' id='meta-data-table-" + responseId + "'>");
			String data = "";
			Vertex meta = null;
			String emotes = getEmotes(next, nextResponse.getTarget(), responseMeta);
			if (emotes != null && !emotes.isEmpty()) {
				writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-emote-" + responseId + "' class='chat-emote'>" + emotes + "</span></td></tr>");
			}
			String actions = getActions(next, nextResponse.getTarget(), responseMeta);
			if (actions != null && !actions.isEmpty()) {
				writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-action-" + responseId + "' class='chat-action'>" + actions + "</span></td></tr>");	
			}
			String poses = getPoses(next, nextResponse.getTarget(), responseMeta);
			if (poses != null && !poses.isEmpty()) {
				writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-pose-" + responseId + "' class='chat-pose'>" + poses + "</span></td></tr>");				
			}
			meta = nextResponse.getTarget().getRelationship(Primitive.LABEL);
			if (meta != null) {
				data = this.getLabel(nextResponse.getTarget());
				if (data != null && !data.isEmpty()) {
					writer.write("<tr class='meta-table'><td style='width:100%;'><span id='span-label-" + responseId + "' class='chat-label'>" + data + "</span></td></tr>");
				}
			}
			List<Relationship> repeatRelationships = nextResponse.getTarget().orderedRelationships(Primitive.ONREPEAT);
			if (repeatRelationships != null) {
				writer.write("<tr class='meta-table'><td><table class='meta-table'>");
				String onRepeatTitle = this.loginBean.translate("Select on repeat response for validation, invalidation, or deletion");
				for (Relationship repeat : repeatRelationships) {
					String onRepeatStyle = "chat-repeat";
					if (repeat.getCorrectness() < 0) {
						onRepeatStyle = "chat-inverse";
					};
					writer.write("<tr class='edit-repeat-tr'>");
					writer.write("<td valign='top'><input type='checkbox' name='repeat:" + String.valueOf(nextResponse.getTarget().getId()) + ":" + String.valueOf(repeat.getTarget().getId()) + "' title='" + onRepeatTitle + "'></td>");
					writer.write("<td valign='top'><span class='chat'>" + String.valueOf((int)(repeat.getCorrectness() * 100)) + "</span></td>");
					writer.write("<td style='width:100%;'><span class='" + onRepeatStyle + "'>" + Utils.escapeHTML(repeat.getTarget().printString()) + "</span></td>");
					writer.write("</tr>\n");
				}
				writer.write("</table></td></tr>");
			}
			
			writer.write("</table>");
		}
	}
}
