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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotAttachment;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.BrowseBean.InstanceSort;

public class AttachmentsBean extends ServletBean {
	String duration = "";
	boolean selectAll;
	List<BotAttachment> attachmentsResults = new ArrayList<BotAttachment>();

	public AttachmentsBean() {
	}
	
	public String getDurationCheckedString(String duration) {
		if (duration.equals(this.duration)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getAllCheckedString() {
		if (this.selectAll) {
			return "checked=\"checked\"";
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public void deleteAttachments(HttpServletRequest request) {
		this.selectAll = false;
		Set<Long> ids = new HashSet<Long>();
		for (Object parameter : request.getParameterMap().entrySet()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
			String key = entry.getKey();
			try {
				ids.add(Long.valueOf(key));
			} catch (NumberFormatException ignore) {}
		}
		for (Iterator<BotAttachment> iterator = this.attachmentsResults.iterator(); iterator.hasNext(); ) {
			BotAttachment attachment = iterator.next();
			if (ids.contains(attachment.getMediaId())) {
				AdminDatabase.instance().deleteBotAttachment(attachment);
				iterator.remove();
			}
		}
	}
	
	public void queryAttachments(String duration) {
		this.selectAll = false;
		this.duration = Utils.sanitize(duration);
		if (duration.equals("") || duration.equals("none")) {
			this.attachmentsResults = new ArrayList<BotAttachment>();
			return;
		}
		try {
			Calendar start = Calendar.getInstance();
			start.add(Calendar.DAY_OF_YEAR, -1);
			if (duration.equals("week")) {
				start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) - 7);
			} else if (duration.equals("month")) {
				start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) - 30);
			} else if (duration.equals("all")) {
				start = null;
			}
			this.attachmentsResults = AdminDatabase.instance().getAllBotAttachments(getBotBean().getInstance(), 0, 1000, start, null, InstanceFilter.Public, InstanceSort.Date, getUser(), this.loginBean.getDomain());
		} catch (Exception failed) {
			error(failed);
			this.attachmentsResults = new ArrayList<BotAttachment>();
		}
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		this.duration = "";
		this.selectAll = false;
		this.attachmentsResults = new ArrayList<BotAttachment>();
	}

	public List<BotAttachment> getAttachmentsResults() {
		return attachmentsResults;
	}

	public void setAttachmentsResults(List<BotAttachment> attachmentsResults) {
		this.attachmentsResults = attachmentsResults;
	}

	public boolean getSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
}
