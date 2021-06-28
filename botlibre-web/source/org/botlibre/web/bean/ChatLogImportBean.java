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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.botlibre.BotException;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.script.Script;

public class ChatLogImportBean extends ScriptBean {
	
	public ChatLogImportBean() {
		this.languageFilter = "Response List";
	}

	public boolean isImport() {
		return true;
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

	@Override
	public String getPostAction() {
		return "chatlog-import";
	}
	
	@Override
	public void writeSearchFields(StringWriter writer) {
		writer.write("<div class='search-div'><span class='search-span'>");
		writer.write(this.loginBean.translate("Language"));
		writer.write("</span> ");
		writer.write("<select id='searchselect' name='language-filter' onchange='this.form.submit()'>\n");
		writer.write("<option value='' " + getLanguageCheckedString("") + "></option>\n");
		writer.write("<option value='Chat Log' " + getLanguageCheckedString("Chat Log") + ">Chat Log</option>\n");
		writer.write("<option value='Response List' " + getLanguageCheckedString("Response List") + ">Response List</option>\n");
		writer.write("<option value='CSV List' " + getLanguageCheckedString("CSV List") + ">CSV List</option>\n");
		writer.write("<option value='AIML' " + getLanguageCheckedString("AIML") + ">AIML</option>\n");
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
	
	@SuppressWarnings("unchecked")
	public void importChatLogs(HttpServletRequest request, boolean createStates, boolean merge, boolean debug, boolean comprehension, boolean pin, boolean autoReduce) {
		try {
			ChatLogBean bean = getLoginBean().getBean(ChatLogBean.class);
			bean.checkMemory();
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
				bean.importChatLog(script.getSourceCode(), script.getLanguage(), comprehension, pin, autoReduce);
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void importChatLog(String idValue, boolean comprehension, boolean pin) {
		try {
			ChatLogBean bean = getLoginBean().getBean(ChatLogBean.class);
			bean.checkMemory();
			long id = 0;
			try {
				id = Long.valueOf(idValue);
			} catch (NumberFormatException ignore) {
				throw new BotException("Invalid id.");
			}
			Script script = AdminDatabase.instance().validate(Script.class, id, getUserId());
			bean.importChatLog(script.getSourceCode(), script.getLanguage(), comprehension, pin, false);
		} catch (Exception exception) {
			error(exception);
		}
	}
	
}
