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
package org.botlibre.web.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.Flaggable;
import org.botlibre.web.admin.User;

@Entity
public class ChatMessage extends Flaggable {
	@Column(length=1024)
	protected String message;
	protected String nick;
	protected String targetNick;
	protected boolean isPrivate;
	protected boolean isWhisper;
	@OneToOne(fetch=FetchType.LAZY)
	protected ChatChannel channel;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@OneToOne(fetch=FetchType.LAZY)
	protected User target;

	public ChatMessage() {
		this.message = "";
		this.nick = "";
	}

	public ChatMessage(String message) {
		this.message = message;
	}
	
	public boolean isWhisper() {
		return isWhisper;
	}

	public void setWhisper(boolean isWhisper) {
		this.isWhisper = isWhisper;
	}

	public String getTargetNick() {
		return targetNick;
	}

	public void setTargetNick(String targetNick) {
		this.targetNick = targetNick;
	}

	@Override
	public String getTypeName() {
		return "Message";
	}
	
	@Override
	public boolean checkProfanity() {
		super.checkProfanity();
		if (getChannel() == null) {
			return Utils.checkProfanity(this.message);
		}
		return Utils.checkProfanity(this.message, getChannel().getContentRatingLevel());
	}

	@Override
	public void checkConstraints() {
		super.checkConstraints();
		if (this.message.length() >= 1024) {
			throw new BotException("Text size limit exceeded");
		}
		if (this.nick.length() >= 255) {
			throw new BotException("Text size limit exceeded");
		}
		Utils.checkHTML(this.nick);
		Utils.checkScript(this.message);
		this.nick = Utils.sanitize(this.nick);
		this.message = Utils.sanitize(this.message);
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public User getTarget() {
		return target;
	}

	public void setTarget(User target) {
		this.target = target;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message != null && message.length() > 1020) {
			message = message.substring(0, 1020);
		}
		this.message = message;
	}

	public ChatChannel getChannel() {
		return channel;
	}

	public void setChannel(ChatChannel channel) {
		this.channel = channel;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.message + ")";
	}
}
