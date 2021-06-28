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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.TextData;
import org.botlibre.self.Self4Compiler;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.thought.language.Language;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.script.Script;
import org.botlibre.web.service.BotStats;

public class SelfBean extends ScriptBean {
	Long selectedState;
	String stateCode = "";
	Set<Long> loadedStates = new HashSet<Long>();
	
	public SelfBean() {
		this.languageFilter = "Self";
	}

	public boolean isImport() {
		return true;
	}
	
	public String getStateCheckedString(Vertex state) {
		if ((this.selectedState == null) || (state == null)) {
			return "";
		}
		if (this.selectedState.equals(state.getId())) {
			return "selected=\"selected\"";
		}
		return "";
	}

	@Override
	public String getPostAction() {
		return "script-import";
	}
	
	@Override
	public void writeSearchFields(StringWriter writer) {
		writer.write("<div class='search-div'><span class='search-span'>");
		writer.write(this.loginBean.translate("Language"));
		writer.write("</span> ");
		writer.write("<select id='searchselect' name='language-filter' onchange='this.form.submit()'>\n");
		writer.write("<option value='' " + getLanguageCheckedString("") + "></option>\n");
		writer.write("<option value='AIML' " + getLanguageCheckedString("AIML") + ">AIML</option>\n");
		writer.write("<option value='Self' " + getLanguageCheckedString("Self") + ">Self</option>\n");
		writer.write("</select>\n");
		writer.write("</div>\n");
	}
	
	@Override
	public void writeBrowseLink(StringWriter writer, Script instance, boolean bold) {
		if (instance.isFlagged()) {
			writer.write("<input type=checkbox name='" + instance.getId() + "'><span style='color:red;margin: 0 0 0;'>" + instance.getName() + "</span>\n");
		} else {
			writer.write("<input type=checkbox name='" + instance.getId() + "'><span style='margin: 0 0 0;'>" + instance.getNameHTML() + "</span>\n");
		}
	}

	@Override
	public void writeBrowseImage(StringWriter writer, Script instance) {
		writer.write("<img class='browse-thumb' src='" + getAvatarThumb(instance) + "' alt='" + instance.getName() + "'/>\n");
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	public void disconnect() {
		this.selectedState = null;
		this.stateCode = "";
		this.loadedStates = new HashSet<Long>();
	}

	@Override
	public List<Script> getAllInstances(Domain domain) {
		try {
			List<Script> results = AdminDatabase.instance().getAllScripts(this.page, this.pageSize, this.languageFilter, this.categoryFilter, this.nameFilter, this.userFilter, 
					this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain, true);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllScriptsCount(this.languageFilter, this.categoryFilter, this.nameFilter, this.userFilter, 
							this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain, true);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Script>();
		}
	}
	
	/**
	 * Return the list of language state machines.
	 */	
	public List<Vertex> getLanguageStateMachines() {
		List<Vertex> states = new ArrayList<Vertex>();
		Network memory = getBot().memory().newMemory();
		List<Relationship> relationships = memory.createVertex(Language.class).orderedRelationships(Primitive.STATE);
		if (relationships != null) {
			for (Relationship relationship : relationships) {
				states.add(relationship.getTarget());
			}
		}
		return states;
	}

	public void processRebootstrap() {
		new Bootstrap().rebootstrapMemory(getBot().memory());
	}
	
	/**
	 * Get State Language gets the language of the current script, either AIML  or Self
	 */
	public String getStateLanguage(Vertex state) {
		Network memory = getBot().memory().newMemory();
		if (state.hasRelationship(Primitive.LANGUAGE, memory.createVertex(Primitive.AIML))) { 
			return "AIML";
		} else {
			return "Self";
		}
	}
	
	/**
	 * Compile the Self code into a new state machine.
	 */
	public boolean compile(String code, String id, boolean debug, boolean optimize) {
		try {
			checkMemory();
			BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
			stats.imports++;
			long start = System.currentTimeMillis();
			setStateCode(code);
			Network memory = getBot().memory().newMemory();
			Vertex newStateMachine = null;
			Vertex oldStateMachine = null;
			if (id == null) {
				oldStateMachine = getSelectedState(memory);
			} else {
				oldStateMachine = memory.findById(Long.valueOf(id));
			}
			SelfCompiler compiler = SelfCompiler.getCompiler();
			if (!optimize) {
				compiler = new Self4Compiler();
			}
			if (code.contains("<aiml")) {
				newStateMachine = memory.createInstance(Primitive.STATE);

				newStateMachine.addRelationship(Primitive.LANGUAGE, memory.createVertex(Primitive.AIML));
				newStateMachine.addRelationship(Primitive.LANGUAGE, memory.createVertex(Primitive.SELF4));
				newStateMachine = AIMLParser.parser().parseAIML(code, true, true, false, false, newStateMachine, memory);
				newStateMachine.setName(oldStateMachine == null ? "aiml" : oldStateMachine.getName());
				TextData data = new TextData();
				data.setText(code);
				newStateMachine.addRelationship(Primitive.SOURCECODE, memory.createVertex(data));
			} else {
				//newStateMachine = compiler.parseStateMachine(code, debug, memory);
				newStateMachine = compiler.parseStateMachine(code, false, memory);
			}
			Vertex language = memory.createVertex(Language.class);
			if (oldStateMachine != null) {
				SelfCompiler.getCompiler().fastLoad(oldStateMachine);
				SelfCompiler.getCompiler().unpin(oldStateMachine);
				Vertex sourceCode = oldStateMachine.getRelationship(Primitive.SOURCECODE);
				if (sourceCode != null) {
					sourceCode.setPinned(false);
				}
				//oldStateMachine.unpinDescendants();
				for (Relationship relationship : language.orderedRelationships(Primitive.STATE)) {
					if (relationship.getTarget().equals(oldStateMachine)) {
						language.replaceRelationship(relationship, newStateMachine);
						memory.removeRelationship(relationship);
						break;
					}
				}
				setSelectedState(null);
				setStateCode("");
			} else {
				language.addRelationship(Primitive.STATE, newStateMachine);
			}
			SelfCompiler.getCompiler().pin(newStateMachine);
			Vertex sourceCode = newStateMachine.getRelationship(Primitive.SOURCECODE);
			if (sourceCode != null) {
				sourceCode.setPinned(true);
			}
			memory.save();
			this.selectedState = newStateMachine.getId();
			this.loadedStates.add(this.selectedState);
			AdminDatabase.instance().log(Level.INFO, "Script compile time", getBot(), System.currentTimeMillis() - start);
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}

	public void moveSelectStateUp(String id) {
		if (id == null) {
			throw new BotException("No script selected");
		}
		Network memory = getBot().memory().newMemory();
		setSelectedState(memory.findById(Long.valueOf(id)));
		if (this.selectedState == null) {
			return;
		}
		Vertex language = memory.createVertex(Language.class);
		Vertex stateMachine = getSelectedState(memory);
		int index = 0;
		Relationship previous = null;
		for (Relationship relationship : language.orderedRelationships(Primitive.STATE)) {
			if (relationship.getTarget().equals(stateMachine)) {
				if (index == 0) {
					return;
				}
				relationship.setIndex(index - 1);
				previous.setIndex(index);
				break;
			} else {
				previous = relationship;
			}
			index++;
		}
		memory.save();
		setSelectedState(stateMachine);
	}

	public void moveSelectStateDown(String id) {
		if (id == null) {
			throw new BotException("No script selected");
		}
		Network memory = getBot().memory().newMemory();
		setSelectedState(memory.findById(Long.valueOf(id)));
		if (this.selectedState == null) {
			return;
		}
		Vertex language = memory.createVertex(Language.class);
		Vertex stateMachine = getSelectedState(memory);
		int index = 0;
		Relationship previous = null;
		for (Relationship relationship : language.orderedRelationships(Primitive.STATE)) {
			if ((previous != null) && previous.getTarget().equals(stateMachine)) {
				relationship.setIndex(index - 1);
				previous.setIndex(index);
				break;
			} else {
				previous = relationship;
			}
			index++;
		}
		memory.save();
		setSelectedState(stateMachine);
	}

	public void newState() {
		setStateCode(Bootstrap.getNewStateText());
		setSelectedState(null);
	}

	/**
	 * Compile the uploaded self file.
	 */
	public void loadSelfFile(InputStream stream, String encoding, boolean debug, boolean optimize) {
		checkMemory();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.imports++;
		Language language = getBot().mind().getThought(Language.class);
		if (language != null) {
			language.loadSelfFile(stream, encoding, Site.MAX_UPLOAD_SIZE, debug, optimize);
		}
	}

	/**
	 * Parse the uploaded AIML file.
	 */
	public void loadAIMLFile(InputStream stream, String name, boolean createStates, boolean mergeState, boolean indexStatic, String encoding) {
		checkMemory();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.imports++;
		Language language = getBot().mind().getThought(Language.class);
		if (language != null) {
			language.loadAIMLFile(stream, name, createStates, mergeState, indexStatic, encoding, Site.MAX_UPLOAD_SIZE);
		}
	}

	/**
	 * Parse the uploaded AIML file.
	 */
	public void loadAIML(String text, String name, boolean createStates, boolean mergeState, boolean indexStatic) {
		checkMemory();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.imports++;
		Language language = getBot().mind().getThought(Language.class);
		if (language != null) {
			language.loadAIML(text, name, createStates, mergeState, indexStatic);
		}
	}

	public void editState(String id) {
		if (id == null) {
			throw new BotException("No script selected");
		}
		Network memory = getBot().memory().newMemory();
		setSelectedState(memory.findById(Long.valueOf(id)));
		if (this.selectedState == null) {
			setStateCode("");
			throw new BotException("Invalid script");
		}
		Vertex state = getSelectedState(memory);
		if ((state.getGroupId() != 0) && !this.loadedStates.contains(this.selectedState)) {
			this.loadedStates.add(this.selectedState);
			SelfCompiler.getCompiler().fastLoad(state);
		}
		String code = null;
		if (state.hasRelationship(Primitive.SOURCECODE)) {
			TextData text = (TextData)state.getRelationship(Primitive.SOURCECODE).getData();
			text = (TextData)state.getNetwork().findData(text);
			code = text.getText();
		} else {
			code = SelfDecompiler.getDecompiler().decompileStateMachine(state, memory);
		}
		setStateCode(code);
	}

	public void decompileState() {
		Network memory = getBot().memory().newMemory();
		if (this.selectedState == null) {
			throw new BotException("Invalid script");
		}
		Vertex state = getSelectedState(memory);
		if ((state.getGroupId() != 0) && !this.loadedStates.contains(this.selectedState)) {
			this.loadedStates.add(this.selectedState);
			SelfCompiler.getCompiler().fastLoad(state);
		}
		setStateCode(SelfDecompiler.getDecompiler().decompileStateMachine(state, memory));
	}

	/**
	 * Export the state.
	 */
	public void export(HttpServletResponse response, String id) throws IOException {
		if (id == null) {
			throw new BotException("No script selected");
		}
		Network memory = getBot().memory().newMemory();
		setSelectedState(memory.findById(Long.valueOf(id)));
		if (this.selectedState == null) {
			throw new BotException("Invalid script");
		}
		Vertex state = getSelectedState(memory);
		if ((state.getGroupId() != 0) && !this.loadedStates.contains(this.selectedState)) {
			this.loadedStates.add(this.selectedState);
			SelfCompiler.getCompiler().fastLoad(state);
		}
		String code = null;
		if (state.hasRelationship(Primitive.SOURCECODE)) {
			TextData text = (TextData)state.getRelationship(Primitive.SOURCECODE).getData();
			text = (TextData)state.getNetwork().findData(text);
			code = text.getText();
		} else {
			code = SelfDecompiler.getDecompiler().decompileStateMachine(state, memory);
		}
		try {
			response.setContentType("text/plain");
			response.setHeader("Content-disposition","attachment; filename=" + encodeURI(state.getName()) + ".self");
			PrintWriter writer = response.getWriter();
			writer.write(code);
			writer.flush();
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	/**
	 * Remove the selected state from the language state machines.
	 */
	public void removeSelectedState(String id) {
		if (id == null) {
			throw new BotException("No script selected");
		}
		long start = System.currentTimeMillis();
		Network memory = getBot().memory().newMemory();
		setSelectedState(memory.findById(Long.valueOf(id)));
		if (this.selectedState == null) {
			throw new BotException("Invalid script");
		}
		Vertex language = memory.createVertex(Language.class);
		Vertex stateMachine = getSelectedState(memory);
		SelfCompiler.getCompiler().fastLoad(stateMachine);
		SelfCompiler.getCompiler().unpin(stateMachine);
		//stateMachine.unpinDescendants();
		for (Relationship relationship : language.getRelationships(Primitive.STATE)) {
			if (relationship.getTarget().equals(stateMachine)) {
				language.internalRemoveRelationship(relationship);
				break;
			}
		}
		memory.save();
		setSelectedState(null);
		setStateCode("");
		AdminDatabase.instance().log(Level.INFO, "Script remove time", getBot(), System.currentTimeMillis() - start);
	}
	
	public Vertex getSelectedState(Network network) {
		if (this.selectedState == null) {
			return null;
		}
		return network.findById(this.selectedState);
	}

	public void setSelectedState(Vertex selectedState) {
		if (selectedState == null) {
			this.selectedState = null;
		} else {
			this.selectedState = selectedState.getId();
		}
	}

	public String getEditLanguage() {
		if (stateCode == null) {
			return "text";
		} else if (stateCode.contains("<aiml")) {
			return "xml";
		}
		return "self";
	}

	public String getEditSource() {
		if (stateCode == null) {
			return "";
		}
		String text = stateCode;
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	@SuppressWarnings("unchecked")
	public void importScripts(HttpServletRequest request, boolean createStates, boolean merge, boolean indexStatic, boolean debug, boolean optimize) {
		try {
			checkMemory();
			BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
			stats.imports++;
			Set<Long> ids = new HashSet<Long>();
			for (Object parameter : request.getParameterMap().entrySet()) {
				Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
				String key = entry.getKey();
				try {
					ids.add(Long.valueOf(key));
				} catch (NumberFormatException ignore) {}
			}
			if (ids.isEmpty()) {
				throw new BotException("Missing import.");
			}
			for (Long id : ids) {
				Script script = AdminDatabase.instance().validate(Script.class, id, getUserId());
				if (script.getLanguage().equals("AIML")) {
					loadAIML(script.getSourceCode(), script.getName(), createStates, merge, indexStatic);
				} else {
					setSelectedState(null);
					compile(script.getSourceCode(), null, debug, optimize);
				}
			}
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void importScript(String idValue, boolean createStates, boolean merge, boolean indexStatic, boolean debug, boolean optimize) {
		try {
			checkMemory();
			long id = Long.valueOf(idValue);
			BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
			stats.imports++;
			Script script = AdminDatabase.instance().validate(Script.class, id, getUserId());
			if (script.getLanguage().equals("AIML")) {
				loadAIML(script.getSourceCode(), script.getName(), createStates, merge, indexStatic);
			} else {
				setSelectedState(null);
				compile(script.getSourceCode(), null, debug, optimize);
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
}
