package org.botlibre.sdk.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DTO for XML chat config.
 */
public class ChatResponse extends Config {	
	public String conversation;
	public String message;
	public String question;
	public String emote;
	public String action;
	public String pose;
	public String avatar;
	public String avatarType;
	public String avatarTalk;
	public String avatarTalkType;
	public String avatarAction;
	public String avatarActionType;
	public String avatarActionAudio;
	public String avatarActionAudioType;
	public String avatarAudio;
	public String avatarAudioType;
	public String avatarBackground;
	public String speech;
	
	public boolean isVideo() {
		return this.avatarType != null && this.avatarType.indexOf("video") != -1;
	}

	public void parseXML(Element element) {
		this.conversation = element.getAttribute("conversation");
		this.emote = element.getAttribute("emote");
		this.action = element.getAttribute("action");
		this.pose = element.getAttribute("pose");
		this.avatar = element.getAttribute("avatar");
		this.avatarType = element.getAttribute("avatarType");
		this.avatarTalk = element.getAttribute("avatarTalk");
		this.avatarTalkType = element.getAttribute("avatarTalkType");
		this.avatarAction = element.getAttribute("avatarAction");
		this.avatarActionType = element.getAttribute("avatarActionType");
		this.avatarActionAudio = element.getAttribute("avatarActionAudio");
		this.avatarActionAudioType = element.getAttribute("avatarActionAudioType");
		this.avatarAudio = element.getAttribute("avatarAudio");
		this.avatarAudioType = element.getAttribute("avatarAudioType");
		this.avatarBackground = element.getAttribute("avatarBackground");
		this.speech = element.getAttribute("speech");

		Node node = element.getElementsByTagName("message").item(0);
		if (node != null) {
			this.message = node.getTextContent();
		}
		node = element.getElementsByTagName("question").item(0);
		if (node != null) {
			this.question = node.getTextContent();
		}
	}
}