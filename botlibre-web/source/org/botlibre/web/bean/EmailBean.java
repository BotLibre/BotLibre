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

import javax.mail.MessagingException;

import org.botlibre.BotException;
import org.botlibre.sense.email.Email;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;

public class EmailBean extends ServletBean {
	
	public EmailBean() {
	}

	public String getUserName() {
		return getBot().awareness().getSense(Email.class).getUsername();
	}

	public String getPassword() {
		return getBot().awareness().getSense(Email.class).getPassword();
	}

	public String getEmailAddress() {
		return getBot().awareness().getSense(Email.class).getEmailAddress();
	}

	public String getSignature() {
		return getBot().awareness().getSense(Email.class).getSignature();
	}

	public String getIncomingHost() {
		return getBot().awareness().getSense(Email.class).getIncomingHost();
	}

	public String getIncomingPort() {
		return String.valueOf(getBot().awareness().getSense(Email.class).getIncomingPort());
	}

	public String getOutgoingHost() {
		return getBot().awareness().getSense(Email.class).getOutgoingHost();
	}

	public String getOutgoingPort() {
		return String.valueOf(getBot().awareness().getSense(Email.class).getOutgoingPort());
	}

	public String getProtocol() {
		return getBot().awareness().getSense(Email.class).getProtocol();
	}

	public boolean getSSL() {
		return getBot().awareness().getSense(Email.class).isSSLRequired();
	}
	
	public void testEmail(String text, String subject, String emailAddress) throws MessagingException {
		getBot().awareness().getSense(Email.class).sendEmail(text, subject, emailAddress, true);
	}
	
	public String getInbox() {
		StringWriter writer = new StringWriter();
		boolean first = true;
		for (String email : getBot().awareness().getSense(Email.class).getInbox()) {
			if (!first) {
				writer.write("</br>\n");
			} else {
				first = false;
			}
			writer.write(email);
		}
		return writer.toString();
	}
	
	public String getSent() {
		StringWriter writer = new StringWriter();
		boolean first = true;
		for (String email : getBot().awareness().getSense(Email.class).getSent()) {
			if (!first) {
				writer.write("</br>\n");
			} else {
				first = false;
			}
			writer.write(email);
		}
		return writer.toString();
	}

	public boolean isConnected() {
		return getBotBean().getInstance().getEnableEmail();
	}

	public void save(String userName, String password, String emailAddress, String incomingHost, String incomingPort,
			String outgoingHost, String outgoingPort, String protocol, boolean ssl, String signature, boolean reply) {
		Email sense = getBot().awareness().getSense(Email.class);
		sense.setEmailAddress(Utils.sanitize(emailAddress.trim()));
		sense.setUsername(Utils.sanitize(userName.trim()));
		sense.setPassword(password.trim());
		sense.setIncomingHost(Utils.sanitize(incomingHost.trim()));
		int port = 0;
		try {
			port = Integer.valueOf(incomingPort);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid incoming port - " + exception.getMessage(), exception);
		}
		sense.setIncomingPort(port);
		sense.setOutgoingHost(Utils.sanitize(outgoingHost.trim()));
		try {
			port = Integer.valueOf(outgoingPort);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid outgoing port - " + exception.getMessage(), exception);
		}
		sense.setOutgoingPort(port);
		sense.setProtocol(Utils.sanitize(protocol.trim()));
		sense.setSSLRequired(ssl);
		sense.setSignature(Utils.sanitize(signature));
		sense.saveProperties();
		if (reply) {
			sense.connect();
			sense.setIsEnabled(true);
			Utils.sleep(100);
			getBotBean().setInstance(AdminDatabase.instance().updateInstanceEmail(getBotBean().getInstance().getId(), true));
		} else {
			disable();
		}
	}

	public void clear() {
		Email sense = getBot().awareness().getSense(Email.class);
		sense.setUsername("");
		sense.setPassword("");
		sense.setEmailAddress("");
		sense.setProtocol("pop3");
		sense.setSSLRequired(true);
		sense.setIncomingHost("pop.gmail.com");
		sense.setIncomingPort(995);
		sense.setOutgoingHost("pop.gmail.com");
		sense.setOutgoingPort(995);
		sense.saveProperties();
		sense.setIsEnabled(false);
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}

	@Override
	public void disconnect() {
	}

	public void disable() {
		getBot().awareness().getSense(Email.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceEmail(getBotBean().getInstance().getId(), false));
	}

	public void checkEmail() {
		getBot().awareness().getSense(Email.class).checkEmail();
	}
}
