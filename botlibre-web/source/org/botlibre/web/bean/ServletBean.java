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
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletContext;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.emotion.EmotionalState;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;

public abstract class ServletBean implements Cloneable {

	protected LoginBean loginBean;
	
	protected int page = 0;
	protected int pageSize = 56;
	protected int resultsSize = 0;
	
	public ServletBean() {
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int getResultsSize() {
		return resultsSize;
	}

	public void setResultsSize(int resultsSize) {
		this.resultsSize = resultsSize;
	}
	
	public ServletBean clone() {
		try {
			return (ServletBean)super.clone();
		} catch (CloneNotSupportedException exception) {
			throw new Error(exception);
		}
	}

	public String http() {
		if (Site.HTTPS || this.loginBean.isHttps()) {
			return "https";
		} else {
			return "http";
		}
	}

	/**
	 * Initialize the Bot instance for the bean.
	 */
	public void initialize(Bot bot) {
	}

	/**
	 * Initialize the Bot instance for the bean.
	 */
	public void reInitialize(Bot bot) {
	}
	
	public void disconnect() {
	}
	
	public void disconnectInstance() {
	}

	/**
	 * Return the associated Bot instance.
	 */
	public Bot getBot() {
		if (getBotBean() == null) {
			return null;
		}
		return getBotBean().getBot();
	}

	public BotBean getBotBean() {
		if (this.loginBean == null) {
			return null;
		}
		return this.loginBean.getBotBean();
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		setLoginBean(loginBean, null);
	}

	public void setLoginBean(LoginBean loginBean, ServletContext context) {
		this.loginBean = loginBean;
		loginBean.getBeans().put(this.getClass(), this);
	}
	
	public void setUser(User user) {
		this.loginBean.setUser(user);
	}
	
	public User getUser() {
		return this.loginBean.getUser();
	}
	
	public String getUserId() {
		return this.loginBean.getUserId();
	}
	
	public boolean isLoggedIn() {
		return this.loginBean.isLoggedIn();
	}
	
	public boolean hasValidApplicationId() {
		if (!isLoggedIn() || getUser().getApplicationId() == null) {
			return false;
		}
		if (Site.COMMERCIAL) {
			try {
				AdminDatabase.instance().validateApplicationId(String.valueOf(getUser().getApplicationId()), null);
			} catch (Exception invalid) {
				invalid.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public void checkLogin() {
		this.loginBean.checkLogin();
	}

	public void error(Throwable error) {
		this.loginBean.error(error);
	}
	
	public static String encode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			return "";
		}
	}
	
	public static String decode(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			return "";
		}
	}
	
	public String encodeURI(String url) {
		return encode(url);
	}

	public String getAllEmotionString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		EmotionalState[] states = EmotionalState.values();
		for (EmotionalState emotion : states) {
			if (emotion.isSentiment()) {
				continue;
			}
			writer.write("\"");
			writer.write(emotion.name().toLowerCase());
			writer.write("\"");
			if (count < states.length)
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}
	
	public String getAllSentimentString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		String[] sentiments = new String[]{"none", "great", "good", "bad", "terrible"};
		for (String sentiment : sentiments) {
			writer.write("\"");
			writer.write(sentiment);
			writer.write("\"");
			if (count < sentiments.length)
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}
	
	public String getAllActionsString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		String[] actions = new String[]{"none", "smile", "frown", "laugh", "scream", "burp", "kiss", "sneeze", "fart", "wave", "bow", "jump", "sit", "kneel", "nod", "shake-head"};
		for (String action : actions) {
			writer.write("\"");
			writer.write(action);
			writer.write("\"");
			if (count < actions.length)
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}

	public String getAllPosesString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		String[] poses = new String[]{"none", "default", "talking", "screaming", "yelling", "kissing", "waving", "jumping", "dancing", "crying", "running",  "sitting", "sleeping", "kneeling", "lying"};
		for (String pose : poses) {
			writer.write("\"");
			writer.write(pose);
			writer.write("\"");
			if (count < poses.length)
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}
	
	public void writeAddThisHTML(Writer out) {
		try {
			out.write("<script type='text/javascript' src='//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-52f7c9d84fd4f43d' async='async'></script>\n");
			out.write("<div class='addthis_sharing_toolbox'></div>\n");
			//out.write("<script type='text/javascript' src='//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-52f7c9d84fd4f43d' async='async'></script>\n");
			//out.write("<div class='addthis_native_toolbox'></div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public  String getPostAction() {
		return "browse";
	}
	
	public String getBrowseAction() {
		return getPostAction();
	}

	@SuppressWarnings("rawtypes")
	public void writePagingString(Writer writer, List instances) {
		try {
			DomainBean domainBean = getLoginBean().getBean(DomainBean.class);
			String domain = domainBean.domainURL();
			if (getPage() > 0) {
				writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + (getPage() - 1) + domain + "\">");
				writer.write(this.loginBean.translate("Previous"));
				writer.write("</a>\n");
				if ((instances.size() > 0) && instances.size() >= getPageSize()) {
					writer.write(" | ");
				}
			}
			if (getResultsSize() > getPageSize()) {
				if (instances.size() >= getPageSize()) {
					writer.write(" <a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + (getPage() + 1) + domain + "\">");
					writer.write(this.loginBean.translate("Next"));
					writer.write("</a>");
				}
				if (getResultsSize() > (20 * getPageSize())) {
					int max =  getResultsSize() / (getPageSize());
					if ((getPage() - 5) <= 5) {
						for (int index = 0; index < (getPage() + 5); index++) {
							writer.write(" | ");
							if (index == getPage()) {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\"><b>" + (index + 1) + "</b></a>");
							} else {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\">" + (index + 1) + "</a>");
							}
						}
					} else {
						for (int index = 0; index < 5; index++) {
							writer.write(" | ");
							if (index == getPage()) {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\"><b>" + (index + 1) + "</b></a>");
							} else {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\">" + (index + 1) + "</a>");
							}
						}
						if ((getPage() + 10) < max) {
							writer.write(" ... ");
							boolean first = true;
							for (int index = getPage() - 5; index < (getPage() + 5); index++) {
								if (!first) {
									writer.write(" | ");
								} else {
									first = false;
								}
								if (index == getPage()) {
									writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\"><b>" + (index + 1) + "</b></a>");
								} else {
									writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\">" + (index + 1) + "</a>");
								}
							}
						}
					}
					writer.write(" ... ");
					boolean first = true;
					if ((getPage() + 10) >= max) {
						for (int index = getPage() - 5 ; index <= max; index++) {
							if (!first) {
								writer.write(" | ");
							} else {
								first = false;
							}
							if (index == getPage()) {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\"><b>" + (index + 1) + "</b></a>");
							} else {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\">" + (index + 1) + "</a>");
							}
						}
					} else {
						for (int index = max - 4; index <= max; index++) {
							if (!first) {
								writer.write(" | ");
							} else {
								first = false;
							}
							if (index == getPage()) {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\"><b>" + (index + 1) + "</b></a>");
							} else {
								writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\">" + (index + 1) + "</a>");
							}
						}
					}
				} else {
					for (int index = 0; (index * getPageSize()) < getResultsSize(); index++) {
						writer.write(" | ");
						if (index == getPage()) {
							writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\"><b>" + (index + 1) + "</b></a>");
						} else {
							writer.write("<a class=\"menu\" href=\"" + getBrowseAction() + "?page=" + index + domain + "\">" + (index + 1) + "</a>");
						}
					}
				}
			}
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		}
	}

	
	public void checkMemory() {
		if ((getBotBean().getInstance().getMemoryLimit() > 0) && (getBot().memory().getLongTermMemory().size() > getBotBean().getInstance().getMemoryLimit() * 1.2)) {
			throw new BotException("Memory size exceeded, importing has been disable until nightly forgetfullness task runs");
		}
	}
}
