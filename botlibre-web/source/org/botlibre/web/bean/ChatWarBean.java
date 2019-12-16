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

import java.util.logging.Level;

import org.botlibre.BotException;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.bean.BrowseBean.InstanceSort;
import org.botlibre.web.rest.ChatWarConfig;


public class ChatWarBean extends ServletBean {
	
	boolean speak = true;
	boolean allowSpeech = true;
	boolean showAvatar = true;
	boolean showChatLog = true;
	
	String topic = "Hello";
	String winner = "";
	
	BotBean instance1;
	BotBean instance2;
	
	public ChatWarBean() {
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public BotBean getInstance1() {
		if (this.instance1 == null) {
			this.instance1 = new BotBean();
			this.instance1.setLoginBean(this.loginBean);
			this.instance1.setInstanceSort(InstanceSort.Rank);
		}
		return instance1;
	}

	public void setInstance1(BotBean instance1) {
		this.instance1 = instance1;
	}

	public BotBean getInstance2() {
		if (this.instance2 == null) {
			this.instance2 = new BotBean();
			this.instance2.setLoginBean(this.loginBean);
			this.instance2.setInstanceSort(InstanceSort.Rank);
		}
		return instance2;
	}

	public void setInstance2(BotBean instance2) {
		this.instance2 = instance2;
	}

	public boolean getShowChatLog() {
		return showChatLog;
	}

	public void setShowChatLog(boolean showChatLog) {
		this.showChatLog = showChatLog;
	}

	public boolean getAllowSpeech() {
		return allowSpeech;
	}

	public void setAllowSpeech(boolean allowSpeech) {
		this.allowSpeech = allowSpeech;
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}

	@Override
	public void disconnect() {
		this.topic = "Hello";
		this.winner = "";
		this.allowSpeech = true;
		this.showAvatar = true;
		this.showChatLog = true;
		if (this.instance1 != null) {
			this.instance1.disconnect();
		}
		if (this.instance2 != null) {
			this.instance2.disconnect();
		}
	}
	
	public BotInstance getWon() {
		if (String.valueOf(this.instance1.getInstanceId()).equals(this.winner)) {
			return this.instance1.getInstance();
		} else {
			return this.instance2.getInstance();
		}
	}
	
	public BotInstance getLoss() {
		if (String.valueOf(this.instance1.getInstanceId()).equals(this.winner)) {
			return this.instance2.getInstance();
		} else {
			return this.instance1.getInstance();
		}
	}
	
	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}
	
	public void vote(ChatWarConfig config) {
		if (!isLoggedIn()) {
			throw new BotException("You must sign in first");
		}
		if (config.secret == null || !String.valueOf((Long.valueOf(config.secret) - getUserId().length())).equals(Site.UPGRADE_SECRET)) {
			AdminDatabase.instance().log(Level.WARNING, "Chat war failed: authorization");
			throw new BotException("Chat war failed authorization");
		}
		startWar(config.winner, config.looser, config.topic);
		endWar(config.winner);
	}

	public void endWar(String winner) {
		if (!isLoggedIn()) {
			error(new BotException("You must be signed in to vote"));
			this.instance1.disconnect();
			this.instance2.disconnect();
		}
		this.winner = winner;
		BotBean won = null;
		BotBean loss = null;
		if (String.valueOf(this.instance1.getInstanceId()).equals(winner)) {
			won = this.instance1;
			loss = this.instance2;
		} else {
			won = this.instance2;
			loss = this.instance1;
		}
		int rank1 = won.getInstance().getRank();
		int rank2 = loss.getInstance().getRank();
		if (rank1 == 0 && rank2 == 0) {
			rank1 = 1;
		} else if (rank1 < rank2) {
			rank1++;
		} else if (rank1 == rank2) {
			rank1++;
		}
		won.setInstance(AdminDatabase.instance().updateWins(won.getInstance(), won.getInstance().getWins() + 1, won.getInstance().getLosses(), rank1));		
		loss.setInstance(AdminDatabase.instance().updateWins(loss.getInstance(), loss.getInstance().getWins(), loss.getInstance().getLosses() + 1, rank2));
		this.instance1.disconnect();
		this.instance2.disconnect();
	}
	
	public boolean startWar(String id1, String id2, String topic) {
		this.topic = topic;
		if (this.instance1 != null) {
			this.instance1.disconnect();
		}
		if (this.instance2 != null) {
			this.instance2.disconnect();
		}
		this.instance1 = new BotBean();
		this.instance1.setLoginBean(this.loginBean);
		if (!this.instance1.validateInstance(id1)) {
			return false;
		}
		if (this.instance1.getInstance().isFlagged()) {
			error(new BotException(this.instance1.getInstanceName() + " has been flagged for offensive content"));
			return false;
		}
		
		this.instance2 = new BotBean();
		this.instance2.setLoginBean(this.loginBean);
		if (!this.instance2.validateInstance(id2)) {
			return false;
		}
		if (this.instance2.getInstance().isFlagged()) {
			error(new BotException(this.instance2.getInstanceName() + " has been flagged for offensive content"));
			return false;
		}
		return true;
	}

	public boolean getShowAvatar() {
		return showAvatar;
	}

	public void setShowAvatar(boolean showAvatar) {
		this.showAvatar = showAvatar;
	}

	public boolean getSpeak() {
		return speak;
	}

	public void setSpeak(boolean speak) {
		this.speak = speak;
	}
}
