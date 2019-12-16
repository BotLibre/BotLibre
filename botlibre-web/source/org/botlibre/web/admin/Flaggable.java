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
package org.botlibre.web.admin;

import java.io.StringWriter;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

@MappedSuperclass
public class Flaggable implements Cloneable {
	@Id
	@GeneratedValue
	protected Long id;
	protected boolean isFlagged;
	protected String flaggedReason;
	protected String flaggedUser;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	protected int thumbsUp;
	protected int thumbsDown;;
	protected float stars;
	@OneToOne(fetch=FetchType.LAZY)
	protected Domain domain;

	/**
	 * Format the text that may be HTML, or may be text, or markup, or a mix.
	 */
	public static String formatHTMLOutput(String text) {
		if (text == null) {
			return "";
		}
		int index = text.indexOf('<');
		int index2 = text.indexOf('>');
		boolean isHTML = (index != -1) && (index2 > index);
		boolean isMixed = isHTML && text.contains("[code]");
		if (isHTML && !isMixed) {
			return text;
		}
		if (!isMixed && ((index != -1) || (index2 != -1))) {
			text = text.replace("<", "&lt;");
			text = text.replace(">", "&gt;");
		}
		TextStream stream = new TextStream(text);
		StringWriter writer = new StringWriter();
		boolean bullet = false;
		boolean nbullet = false;
		while (!stream.atEnd()) {
			String line = stream.nextLine();
			if (!isMixed && (line.contains("http://") || line.contains("https://"))) {
				line = Utils.linkHTML(line);
			}
			TextStream lineStream = new TextStream(line);
			boolean firstWord = true;
			boolean span = false;
			boolean cr = true;
			while (!lineStream.atEnd()) {
				while (!isMixed && firstWord && lineStream.peek() == ' ') {
					lineStream.next();
					writer.write("&nbsp;");
				}
				String whitespace = lineStream.nextWhitespace();
				writer.write(whitespace);
				if (lineStream.atEnd()) {
					break;
				}
				String word = lineStream.nextWord();
				if (!isMixed && nbullet && firstWord && !word.equals("#")) {
					writer.write("</ol>\n");
					nbullet = false;
				} else if (!isMixed && bullet && firstWord && !word.equals("*")) {
					writer.write("</ul>\n");
					bullet = false;
				}
				if (firstWord && word.equals("[")) {
					String peek = lineStream.peekWord();
					if ("code".equals(peek)) {
						lineStream.nextWord();
						String next = lineStream.nextWord();
						String lang = "javascript";
						int lines = 20;
						if ("lang".equals(next)) {
							lineStream.skip();
							lang = lineStream.nextWord();
							if ("\"".equals(lang)) {
								lang = lineStream.nextWord();
								lineStream.skip();
							}
							next = lineStream.nextWord();
						}
						if ("lines".equals(next)) {
							lineStream.skip();
							String value = lineStream.nextWord();
							if ("\"".equals(value)) {
								value = lineStream.nextWord();
								lineStream.skip();
							}
							lineStream.skip();
							try {
								lines = Integer.valueOf(value);
							} catch (NumberFormatException ignore) {}
						}
						String id = "code" + stream.getPosition();
						writer.write("<div style=\"width:100%;height:" + lines * 14 + "px;max-width:none\" id=\"" + id + "\">");
						String code = lineStream.upToAll("[code]");
						if (code.indexOf('<') != -1) {
							code = code.replace("<", "&lt;");
						}
						if (code.indexOf('>') != -1) {
							code = code.replace(">", "&gt;");
						}
						writer.write(code);
						while (lineStream.atEnd() && !stream.atEnd()) {
							line = stream.nextLine();
							lineStream = new TextStream(line);
							while (lineStream.peek() == ':') {
								lineStream.next();
								writer.write("&nbsp;&nbsp;&nbsp;&nbsp;");								
							}
							code = lineStream.upToAll("[code]");
							if (code.indexOf('<') != -1) {
								code = code.replace("<", "&lt;");
							}
							if (code.indexOf('>') != -1) {
								code = code.replace(">", "&gt;");
							}
							writer.write(code);
						}
						lineStream.skip("[code]".length());
						writer.write("</div>\n");

						writer.write("<script>\n");
						writer.write("var " + id + " = ace.edit('" + id + "');\n");
						writer.write(id + ".getSession().setMode('ace/mode/" + lang + "');\n");
						writer.write(id + ".setReadOnly(true);\n");
						writer.write("</script>\n");
					} else {
						writer.write(word);
					}
				} else if (!isMixed && firstWord && word.equals("=")) {
					int count = 2;
					String token = word;
					while (!lineStream.atEnd() && lineStream.peek() == '=') {
						lineStream.skip();
						count++;
						token = token + "=";
					}
					String header = lineStream.upToAll(token);
					if (lineStream.atEnd()) {
						writer.write(token);
						writer.write(header);
					} else {
						lineStream.skip(token.length());
						writer.write("<h");
						writer.write(String.valueOf(count));
						writer.write(">");
						writer.write(header);
						writer.write("</h");
						writer.write(String.valueOf(count));
						writer.write(">");
						cr = false;
					}
				} else if (!isMixed && firstWord && word.equals(":")) {
					span = true;
					int indent = 1;
					while (!lineStream.atEnd() && lineStream.peek() == ':') {
						lineStream.skip();				
						indent++;
					}
					writer.write("<span style=\"display:inline-block;text-indent:");
					writer.write(String.valueOf(indent * 20));
					writer.write("px;\">");	
				} else if (!isMixed && firstWord && word.equals("*")) {
					if (!bullet) {
						writer.write("<ul>");
						bullet = true;
					}
					writer.write("<li>");
					cr = false;
				} else if (!isMixed && firstWord && word.equals("#")) {
					if (!nbullet) {
						writer.write("<ol>");
						nbullet = true;
					}
					writer.write("<li>");
					cr = false;
				} else {
					writer.write(word);
				}
				firstWord = false;
			}
			if (!isMixed && span) {
				writer.write("</span>");
			}
			if (!isMixed && cr) { 
				writer.write("<br/>\n");
			}
		}
		if (!isMixed && bullet) {
			writer.write("</ul>");
		}
		if (!isMixed && nbullet) {
			writer.write("</ol>");
		}
		return writer.toString();
	}

	/**
	 * Format the text to add links and escape < or > (but not both).
	 */
	public static String formatBasicHTMLOutput(String text) {
		if (text == null) {
			return "";
		}
		int index = text.indexOf('<');
		int index2 = text.indexOf('>');
		boolean isHTML = (index != -1) && (index2 > index);
		if (isHTML) {
			return text;
		}
		if ((index != -1) || (index2 != -1)) {
			text = text.replace("<", "&lt;");
			text = text.replace(">", "&gt;");
		}
		TextStream stream = new TextStream(text);
		StringWriter writer = new StringWriter();
		boolean cr = true;
		while (!stream.atEnd()) {
			String line = stream.nextLine();
			TextStream lineStream = new TextStream(line);
			while (!lineStream.atEnd()) {
				String whitespace = lineStream.nextWhitespace();
				writer.write(whitespace);
				if (lineStream.atEnd()) {
					break;
				}
				String word = lineStream.nextWord();
				if (word.startsWith("http://") || word.startsWith("https://")) {
					writer.write("<a target=\"_blank\" href=\"");
					writer.write(word);
					writer.write("\">");
					writer.write(word);
					writer.write("</a>");
				} else {
					writer.write(word);
				}
			}
			if (cr) { 
				writer.write("<br/>\n");
			}
		}
		return writer.toString();
	}
	
	public Flaggable() {
	}
	
	public Flaggable clone() {
		try {
			return (Flaggable)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}
	
	public boolean checkProfanity() {
		return Utils.checkProfanity(this.flaggedReason);
	}
	
	public void checkConstraints() {
		if ((this.flaggedReason != null) && (this.flaggedReason.length() >= 1024)) {
			throw new BotException("Text size limit exceeded");
		}
	}
	
	public int getThumbsUp() {
		return thumbsUp;
	}

	public void setThumbsUp(int thumbsUp) {
		this.thumbsUp = thumbsUp;
	}

	public int getThumbsDown() {
		return thumbsDown;
	}

	public void setThumbsDown(int thumbsDown) {
		this.thumbsDown = thumbsDown;
	}

	public float getStars() {
		return stars;
	}

	public void setStars(float stars) {
		this.stars = stars;
	}

	public String getCreationDateString() {
		return this.creationDate.toString();
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.id + ")";
	}
	
	public boolean isFlagged() {
		return isFlagged;
	}

	public void setFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}

	public String getFlaggedReason() {
		return flaggedReason;
	}

	public void setFlaggedReason(String flaggedReason) {
		this.flaggedReason = flaggedReason;
	}

	public String getFlaggedUser() {
		return flaggedUser;
	}

	public void setFlaggedUser(String flaggedUser) {
		this.flaggedUser = flaggedUser;
	}

	public Flaggable detach() {
		try {
			Flaggable detched = getClass().newInstance();
			detched.setId(getId());
			return detched;
		} catch (Exception ignore) {
			return this;
		}
	}
	
	public void preDelete(EntityManager em) {
		
	}
	
	public String getTypeName() {
		return getClass().getSimpleName();
	}
	
	public String getDisplayName() {
		return getTypeName();
	}
}
