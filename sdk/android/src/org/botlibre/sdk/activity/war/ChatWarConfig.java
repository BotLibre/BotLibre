package org.botlibre.sdk.activity.war;

import java.io.StringWriter;

import org.botlibre.sdk.config.Config;


/**
 * DTO for XML chat war config.
 */
public class ChatWarConfig extends Config {
	public String winner;
	public String looser;
	public String topic;
	public String secret;
	
	public ChatWarConfig() {
		
	}

	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<chat-war");
		writeCredentials(writer);

		if (this.winner != null) {
			writer.write(" winner=\"" + this.winner + "\"");
		}

		if (this.looser != null) {
			writer.write(" looser=\"" + this.looser + "\"");
		}

		if (this.topic != null) {
			writer.write(" topic=\"" + this.topic + "\"");
		}

		if (this.secret != null) {
			writer.write(" secret=\"" + this.secret + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}