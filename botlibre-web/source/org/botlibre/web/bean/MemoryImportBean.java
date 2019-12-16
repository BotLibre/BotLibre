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

public class MemoryImportBean extends ScriptBean {
	
	public MemoryImportBean() {
		this.languageFilter = "JSON";
	}

	@Override
	public List<Script> getAllInstances(Domain domain) {
		try {
			List<Script> results = AdminDatabase.instance().getAllScripts(this.page, this.pageSize, this.languageFilter, this.categoryFilter, this.nameFilter, this.userFilter, 
					this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, true);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllScriptsCount(this.languageFilter, this.categoryFilter, this.nameFilter, this.userFilter, 
							this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, true);
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
		return "memory-import";
	}
	
	@Override
	public void writeSearchFields(StringWriter writer) {
		writer.write("<div class='search-div'><span class='search-span'>Language</span> ");
		writer.write("<select id='searchselect' name='language-filter' onchange='this.form.submit()'>\n");
		writer.write("<option value='JSON' " + getLanguageCheckedString("JSON") + ">JSON</option>\n");
		writer.write("<option value='CSV' " + getLanguageCheckedString("CSV") + ">CSV</option>\n");
		writer.write("<option value='Set' " + getLanguageCheckedString("Set") + ">Set</option>\n");
		writer.write("<option value='Map' " + getLanguageCheckedString("Map") + ">Map</option>\n");
		writer.write("<option value='Properties' " + getLanguageCheckedString("Properties") + ">Properties</option>\n");
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
	public void importData(HttpServletRequest request, boolean pin) {
		try {
			MemoryBean bean = getLoginBean().getBean(MemoryBean.class);
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
				bean.importData(script.getName(), script.getSourceCode(), script.getLanguage(), pin);
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
}
