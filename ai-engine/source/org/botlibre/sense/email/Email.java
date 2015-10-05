/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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
package org.botlibre.sense.email;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.pop3.POP3SSLStore;

/**
 * Receive and respond to incoming email.
 * Can use any pop mail server, such as gmail.
 */

public class Email extends BasicSense {
	public static int SLEEP = 1000 * 60 * 10; // 10 minutes.
	public static String SIGNATURE = "\n\n----------\nThis is an automated message from an emailbot hosted on BOT libre, http://www.botlibre.com\n";
	
	/** Signature to apply to emails. */
	protected String signature = SIGNATURE;
	
	/** Email address. */
	protected String emailAddress = "user@gmail.com";
	
	/** POP info. */
	protected String incomingHost = "imap.gmail.com";
	protected int incomingPort = 993;
	protected String outgoingHost = "smtp.gmail.com";
	protected int outgoingPort = 587;
	protected String username = "user@gmail.com";
	protected String password = "";
	protected String protocol = "imaps";
	protected boolean isSSLRequired = false;
	protected int maxEmails = 100;
	
	protected int emails = 0;
	protected int emailsProcessed = 0;
	
	protected boolean initProperties;
	
	/** Email checker thread. */
	protected Runnable emailChecker;
	
	public Email() {
		this.isEnabled = false;
		if (isEnabled()) {
			//startCheckingEmail();
		}
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
	}

	/**
	 * Load settings.
	 */
	public void initProperties() {
		if (this.initProperties) {
			return;
		}
		synchronized (this) {
			if (this.initProperties) {
				return;
			}
			Network memory = getBot().memory().newMemory();
			Vertex sense = memory.createVertex(getPrimitive());
			Vertex property = sense.getRelationship(Primitive.USER);
			if (property != null) {
				this.username = (String)property.getData();
			}
			property = sense.getRelationship(Primitive.PASSWORD);
			if ((property != null) && (this.username != null)) {
				String data = (String)property.getData();
				// Check if encrypted from && prefix.
				if (data.startsWith("&&")) {
					try {
						this.password = Utils.decrypt(Utils.KEY, data.substring(2, data.length()));
					} catch (Exception exception) {
						this.password = Utils.decrypt(this.username, data);
					}
				} else {
					this.password = Utils.decrypt(this.username, data);					
				}
			}
			property = sense.getRelationship(Primitive.EMAILADDRESS);
			if (property != null) {
				this.emailAddress = (String)property.getData();
				setIsEnabled(true);
			}
			property = sense.getRelationship(Primitive.SSL);
			if (property != null) {
				this.isSSLRequired = (Boolean)property.getData();
			}
			property = sense.getRelationship(Primitive.SIGNATURE);
			if (property != null) {
				this.signature = (String)property.getData();
			}
			property = sense.getRelationship(Primitive.INCOMINGHOST);
			if (property != null) {
				this.incomingHost = (String)property.getData();
			}
			property = sense.getRelationship(Primitive.INCOMINGPORT);
			if (property != null) {
				this.incomingPort = ((Number)property.getData()).intValue();
			}
			property = sense.getRelationship(Primitive.OUTGOINGHOST);
			if (property != null) {
				this.outgoingHost = (String)property.getData();
			}
			property = sense.getRelationship(Primitive.OUTGOINGPORT);
			if (property != null) {
				this.outgoingPort = ((Number)property.getData()).intValue();
			}
			property = sense.getRelationship(Primitive.PROTOCOL);
			if (property != null) {
				this.protocol = (String)property.getData();
			}
			this.initProperties = true;
		}
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex sense = memory.createVertex(getPrimitive());
		sense.unpinChildren();
		sense.setRelationship(Primitive.USER, memory.createVertex(this.username));
		sense.setRelationship(Primitive.PASSWORD, memory.createVertex("&&" + Utils.encrypt(Utils.KEY, this.password)));
		sense.setRelationship(Primitive.EMAILADDRESS, memory.createVertex(this.emailAddress));
		sense.setRelationship(Primitive.SSL, memory.createVertex(this.isSSLRequired));
		sense.setRelationship(Primitive.SIGNATURE, memory.createVertex(this.signature));
		sense.setRelationship(Primitive.INCOMINGHOST, memory.createVertex(this.incomingHost));
		sense.setRelationship(Primitive.INCOMINGPORT, memory.createVertex(this.incomingPort));
		sense.setRelationship(Primitive.OUTGOINGHOST, memory.createVertex(this.outgoingHost));
		sense.setRelationship(Primitive.OUTGOINGPORT, memory.createVertex(this.outgoingPort));
		sense.setRelationship(Primitive.PROTOCOL, memory.createVertex(this.protocol));
		sense.pinChildren();
		memory.save();
	}
	
	@Override
	public void setIsEnabled(boolean isEnabled) {
		super.setIsEnabled(isEnabled);
		if (isEnabled) {
			if (this.emailChecker == null) {
				//startCheckingEmail();
			}
		} else {
			this.emailChecker = null;
		}
	}	

	public void startCheckingEmail() {
	    this.emailChecker = new Runnable() {
	    	@Override
	    	public void run() {
	    		try {
	    			while (isEnabled()) {
		    			checkEmail();
		    			Thread.sleep(SLEEP);
	    			}
	    		} catch (Exception exception) {
	    			log(exception);
	    		}
	    		
	    	}
	    };
	    Thread thread = new Thread(this.emailChecker);
	    thread.start();
	}
	
	public Store connectStore() throws MessagingException {
		initProperties();
		if (isSSLRequired()) {
			return connectStoreSSL();
		}
		Properties properties = new Properties();
		properties.put("mail." + getProtocol() + ".timeout", 5000);
		properties.put("mail." + getProtocol() + ".connectiontimeout", 5000);
		//properties.setProperty("mail.store.protocol", getProtocol());
	    Session session = Session.getInstance(properties, null);
	    Store store = session.getStore(getProtocol());
	    if (getIncomingPort() == 0) {
	    	store.connect(getIncomingHost(), getUsername(), getPassword());
	    } else {
	    	store.connect(getIncomingHost(), getIncomingPort(), getUsername(), getPassword());
	    }
	    return store;
	}
	
	public Store connectStoreSSL() throws MessagingException {   
		Properties properties = System.getProperties();
		properties.put("mail." + getProtocol() + ".timeout", 5000);
		properties.put("mail." + getProtocol() + ".connectiontimeout", 5000);
        properties.setProperty("mail." + getProtocol() + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail." + getProtocol() + ".socketFactory.fallback", "false");
        properties.setProperty("mail." + getProtocol() + ".port",  String.valueOf(getIncomingPort()));
        properties.setProperty("mail." + getProtocol() + ".socketFactory.port", String.valueOf(getIncomingPort()));
        
        Session session = Session.getInstance(properties, null);        
        URLName url = new URLName(getProtocol(), getIncomingHost(), getIncomingPort(), "", getUsername(), getPassword());
        Store store = null;
        if (getProtocol().equals("pop3")) {
        	store = new POP3SSLStore(session, url);
        } else {
        	store = new IMAPSSLStore(session, url);
        }
        store.connect();
        return store;		
	}

	/**
	 * Check the inbox for new messages, and process each message.
	 */
	public void checkEmail() {
		try {
			log("Checking email.", Level.FINER);
	        Store store = connectStore();		
		    Folder inbox = store.getFolder("INBOX");
		    if (inbox == null) {
		      throw new BotException("Failed to check email, no INBOX.");
		    }
		    inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
		    //Message[] messages = inbox.getMessages();
		    if ((messages != null) && (messages.length > 0)) {
    			log("Processing emails", Level.INFO, messages.length);
				Network memory = getBot().memory().newMemory();
				Vertex sense = memory.createVertex(getPrimitive());
				Vertex vertex = sense.getRelationship(Primitive.LASTMESSAGE);
				long lastMessage = 0;
				if (vertex != null) {
					lastMessage = ((Number)vertex.getData()).longValue();
				}
				long maxMessage = 0;
				int count = 0;
			    for (int index = 0; index < messages.length; index++) {
			    	long recievedTime = 0;
			    	if (messages[index].getReceivedDate() == null) {
		    			log("Missing received date", Level.FINE, messages[index].getSubject());
			    		recievedTime = messages[index].getSentDate().getTime();
			    	} else {
			    		recievedTime = messages[index].getReceivedDate().getTime();
			    	}
			    	if (recievedTime > lastMessage) {
			    		count++;
			    		if (count > this.maxEmails) {
			    			log("Max email limit reached", Level.WARNING, this.maxEmails);
			    			break;
			    		}
			    		input(messages[index]);
				    	Utils.sleep(100);
				    	if (recievedTime > maxMessage) {
				    		maxMessage = recievedTime;
				    	}
			    	}
			    }
			    if (maxMessage != 0) {
					sense.setRelationship(Primitive.LASTMESSAGE, memory.createVertex(maxMessage));
			    	memory.save();
			    }
		    }
			log("Done checking email.", Level.FINER);
		    inbox.close(false);
		    store.close();
		} catch (MessagingException exception) {
			log(exception);
		}
	}

	/**
	 * Connect and verify the email settings.
	 */
	public void connect() {
		Store store = null;
		try {
			log("Connecting email.", Level.FINER);
	        store = connectStore();
	        connectSession();
			log("Done connecting email.", Level.FINER);
		} catch (MessagingException messagingException) {
			BotException exception = new BotException("Failed to connect - " + messagingException.getMessage(), messagingException);
			log(exception);
			throw exception;
		} finally {
			try {
				if (store != null) {
					store.close();
				}
			} catch (Exception ignore) {}
		}
	}

	/**
	 * Return a list in inbox message headers.
	 */
	public List<String> getInbox() {
		List<String> emails = new ArrayList<String>();
		Store store = null;
		Folder inbox = null;
		try {
			store = connectStore();		
		    inbox = store.getFolder("INBOX");
		    if (inbox == null) {
		      log(new BotException("Failed to access inbox, no INBOX."));
		    }
		    inbox.open(Folder.READ_ONLY);
		    
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
		    //Message[] messages = inbox.getMessages(1, Math.min(inbox.getMessageCount(), 50));
		    for (int index = 0; index < messages.length; index++) {
		      emails.add(0, messages[index].getReceivedDate() + " - " + String.valueOf(getFrom(messages[index])) + ": " + messages[index].getSubject());
		    }
		    inbox.close(false);
		    store.close();
		} catch (MessagingException exception) {
			log(new BotException("Failed to access email.", exception));
		} finally {
			try {
				if (inbox != null) {
					inbox.close(false);
				}
				if (store != null) {
					store.close();
				}
			} catch (Exception ignore) {}
		}
		return emails;
	}

	/**
	 * Return a list in inbox message headers.
	 */
	public List<String> getSent() {
		List<String> emails = new ArrayList<String>();
		try {
			Store store = connectStore();
			Folder sent = null;
			try {
				sent = store.getFolder("SENT");
			    sent.open(Folder.READ_ONLY);
			} catch (Exception ignore) {
				sent = null;
			}
		    if (sent == null) {
				try {
					sent = store.getFolder("[Gmail]/Sent Mail");
				    sent.open(Folder.READ_ONLY);
				} catch (Exception ignore) {
					sent = null;
				}
		    }
		    if (sent == null) {
		      log(new BotException("Failed to access sent, no SENT."));
		    } else {
			    Message[] messages = sent.getMessages(1, Math.min(sent.getMessageCount(), 12));
			    for (int index = 0; index < messages.length; index++) {
			      emails.add(0, messages[index].getReceivedDate() + " - " + String.valueOf(getRecipient(messages[index])) + ": " + messages[index].getSubject());
			    }
			    sent.close(false);
		    }
		    store.close();
		} catch (MessagingException exception) {
			exception.printStackTrace();
			log(new BotException("Failed to access sent - " + exception.toString(), exception));
		}
		return emails;
	}
	
	public Session connectSession() {			 
		Properties props = new Properties();
		Session session = null;
		if (isSSLRequired()) {
			props.put("mail.smtp.host", getOutgoingHost());
			props.put("mail.smtp.port", getOutgoingPort());
			props.put("mail.smtp.socketFactory.port", getOutgoingPort());
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			 
			session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(getUsername(), getPassword());
				}
			});
		} else {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", getOutgoingHost());
			props.put("mail.smtp.port", getOutgoingPort());
			 
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(getUsername(), getPassword());
				}
			});
		}
		return session;
	}

	/**
	 * Send the email reply.
	 */
	public void sendEmail(String text, String subject, String replyTo) {
		log("Sending email:", Level.INFO, text, replyTo);
		initProperties();
		try {
			//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			//props.setProperty("mail.transport.protocol", "smtp");
			//props.setProperty("mail.host", getOutgoingHost());

			Session session = connectSession();
			
			/*props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", String.valueOf(getOutgoingPort()));
			props.put("mail.smtp.socketFactory.port", String.valueOf(getOutgoingPort()));
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.quitwait", "false");*/

			MimeMessage message = new MimeMessage(session);
		    message.setFrom(new InternetAddress(getEmailAddress()));
		    message.addRecipient(Message.RecipientType.TO, new InternetAddress(replyTo));
		    message.setSubject(subject);
		    message.setText(text);

		    // Send message
		    this.emails++;
		    Transport.send(message);
		} catch (MessagingException exception) {
			log(new BotException("Failed to send email.", exception));
		}
	}

	public String getRecipient(Message message) throws MessagingException {
	    Address to[] = message.getRecipients(RecipientType.TO);
	    if ((to != null) && (to.length > 0) && (to[0] instanceof InternetAddress)) {
	    	return ((InternetAddress)to[0]).getAddress();
	    }
	    return null;
	}

	public String getFrom(Message message) throws MessagingException {
	    Address from[] = message.getFrom();
	    if ((from != null) && (from.length > 0) && (from[0] instanceof InternetAddress)) {
	    	return ((InternetAddress)from[0]).getAddress();
	    }
	    return null;
	}
    
	/**
	 * Process the email message.
	 */
	@Override
	public void input(Object input, Network network) throws Exception {
		if (!isEnabled()) {
			return;
		}
	    Message message = (Message)input;
	    String fromUser = getFrom(message);
	    if (fromUser == null) {
	    	fromUser = DEFAULT_SPEAKER;
	    }
	    String subject = message.getSubject();
	    if (subject == null) {
	    	subject = "";
	    }
	    subject = subject.trim();
	    if (fromUser.equals(getEmailAddress())) {
			log("Ignoring email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    if (fromUser.toLowerCase().indexOf("daemon") != -1) {
			log("Ignoring daemon email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    if (fromUser.toLowerCase().indexOf("twitter.com") != -1) {
			log("Ignoring twitter email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    if (fromUser.toLowerCase().indexOf("facebook.com") != -1) {
			log("Ignoring facebook email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    if (fromUser.toLowerCase().indexOf("google.com") != -1) {
			log("Ignoring google email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    if ((fromUser.toLowerCase().indexOf("noreply") != -1) || (fromUser.toLowerCase().indexOf("donotreply") != -1)) {
			log("Ignoring noreply email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    if (subject.toLowerCase().indexOf("noreply") != -1) {
			log("Ignoring noreply email", Bot.FINE, subject, fromUser);
	    	return;
	    }
	    this.emailsProcessed++;
		log("Processing email", Level.INFO, subject, fromUser);
	    String toUser = getRecipient(message);
	    if (toUser == null) {
	    	toUser = DEFAULT_SPEAKER;
	    }
	    Object content = message.getContent();
	    String text = "";
	    if (content instanceof MimeMultipart) {
	    	MimeMultipart parts = (MimeMultipart)content;
	    	for (int index = 0; index < parts.getCount(); index++) {
	    		BodyPart body = parts.getBodyPart(index);
	    		if (body.getContentType().toLowerCase().indexOf("text/plain") != -1) {
	    			text = (String)body.getContent();
	    			break;
	    		}
	    	}
	    	if ((text == null) && (parts.getCount() > 0)) {
	    		text = (String)parts.getBodyPart(0).getContent();
	    		text = Utils.stripTags(text);
	    	}
	    } else if (content instanceof String) {
	    	text = (String)content;
	    	if (message.getContentType().toLowerCase().contains("html")) {
	    		text = Utils.stripTags(text);	    		
	    	}
	    }
    	text = text.trim();
		log("Processing email body", Level.INFO, subject, fromUser, text);
	    // Parse email chain.
	    TextStream stream = new TextStream(text);
	    StringWriter writer = new StringWriter();
	    while (!stream.atEnd()) {
	    	String nextLine = stream.nextLine();
	    	if ((nextLine.length() > 0) && (nextLine.charAt(0) == '-')) {
		    	if (nextLine.indexOf("---") != -1) {
		    		// End of message.
		    		break;
		    	}
	    	}
	    	if ((nextLine.indexOf("@") != -1) && (nextLine.indexOf("wrote") != -1) && (nextLine.length() > 10) && (nextLine.substring(0, 2).equalsIgnoreCase("on"))) {
	    		break;
	    	}
	    	if ((nextLine.length() > 0) && (nextLine.charAt(0) == '>')) {
	    		// Ignore.
	    	} else {
	    		writer.write(nextLine);
	    	}
	    }
	    text = writer.toString();
	    if (text.length() == 0) {
	    	text = subject;
	    }
		log("Processing email content", Level.INFO, subject, fromUser, text);
		inputSentence(text.trim(), subject, fromUser, toUser, message, network);
	}

	/**
	 * Output the email reply.
	 */
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to email, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		Vertex target = output.mostConscious(Primitive.TARGET);
		String replyTo = target.mostConscious(Primitive.WORD).getData().toString();
		if ((replyTo.toLowerCase().indexOf("noreply") != -1) || (replyTo.toLowerCase().indexOf("donotreply") != -1)) {
			return;
		}
		String text = null;
		Vertex response = output.getRelationship(Primitive.INPUT);
		Vertex input = output.mostConscious(Primitive.QUESTION);
		Vertex question = input.getRelationship(Primitive.INPUT);
		if ((question != null) && response.instanceOf(Primitive.PARAGRAPH) && question.instanceOf(Primitive.PARAGRAPH)) {
			StringWriter writer = new StringWriter();
			List<Vertex> sentences = response.orderedRelations(Primitive.SENTENCE);
			List<Vertex> questions = question.orderedRelations(Primitive.SENTENCE);
			int index = 0;
			for (Vertex sentence : sentences) {
				Vertex questionSentence = null;
				while (((index < questions.size()) && (questionSentence == null || !questionSentence.instanceOf(Primitive.QUESTION)))) {
					questionSentence = questions.get(index);
					index++;
				}
				if (questionSentence != null) {
					writer.write("\n");
					String questionText = questionSentence.printString();
					if (questionText.contains("\n")) {
						TextStream stream = new TextStream(questionText);
						while (!stream.atEnd()) {
							writer.write("> ");
							writer.write(stream.nextLine());							
						}
					} else {
						writer.write("> ");
						writer.write(questionText);
					}
					writer.write("\n");
				}
				writer.write(sentence.printString());
				writer.write("\n");
			}
			text = writer.toString();
		} else {
			text = printInput(output);
		}
		StringWriter writer = new StringWriter();
		writer.write(text);
		writer.write("\n");
		writer.write("\n");
		writer.write(getSignature());
		// Append quoted original email.
		if (input != null) {
			String replyText = printInput(input);
			writer.write("\n");
			writer.write("\n");
			TextStream stream = new TextStream(replyText);
			int max = 0;
			while (!stream.atEnd() && (max < 64)) {
				String line = stream.nextLine();
				writer.write(">");
				if ((line.length() > 0) && (line.charAt(0) != '>')) {
					writer.write(" ");
				}
				writer.write(line);
			}
			text = writer.toString();
		}
		String subject = output.mostConscious(output.getNetwork().createVertex(Primitive.TOPIC)).getData().toString();
		if (!subject.startsWith("RE:") && !subject.startsWith("Re:")) {
			subject = "RE: " + subject;
		}
		sendEmail(text, subject, replyTo);
	}

	public String getSignature() {
		initProperties();
		return signature;
	}

	public void setSignature(String signature) {
		initProperties();
		this.signature = signature;
	}

	public String getEmailAddress() {
		initProperties();
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		initProperties();
		this.emailAddress = emailAddress;
	}

	public String getIncomingHost() {
		initProperties();
		return incomingHost;
	}

	public void setIncomingHost(String incomingHost) {
		initProperties();
		this.incomingHost = incomingHost;
	}

	public int getIncomingPort() {
		initProperties();
		return incomingPort;
	}

	public void setIncomingPort(int incomingPort) {
		initProperties();
		this.incomingPort = incomingPort;
	}

	public String getOutgoingHost() {
		initProperties();
		return outgoingHost;
	}

	public void setOutgoingHost(String outgoingHost) {
		initProperties();
		this.outgoingHost = outgoingHost;
	}

	public int getOutgoingPort() {
		initProperties();
		return outgoingPort;
	}

	public void setOutgoingPort(int outgoingPort) {
		initProperties();
		this.outgoingPort = outgoingPort;
	}

	public String getUsername() {
		initProperties();
		return username;
	}

	public void setUsername(String username) {
		initProperties();
		this.username = username;
	}

	public String getPassword() {
		initProperties();
		return password;
	}

	public void setPassword(String password) {
		initProperties();
		this.password = password;
	}

	public String getProtocol() {
		initProperties();
		return protocol;
	}

	public void setProtocol(String protocol) {
		initProperties();
		this.protocol = protocol;
	}

	public boolean isSSLRequired() {
		initProperties();
		return isSSLRequired;
	}

	public void setSSLRequired(boolean isSSLRequired) {
		initProperties();
		this.isSSLRequired = isSSLRequired;
	}
	
	public int getMaxEmails() {
		return maxEmails;
	}

	public void setMaxEmails(int maxEmails) {
		this.maxEmails = maxEmails;
	}

	public int getEmails() {
		return emails;
	}

	public void setEmails(int emails) {
		this.emails = emails;
	}

	public int getEmailsProcessed() {
		return emailsProcessed;
	}

	public void setEmailsProcessed(int emailsProcessed) {
		this.emailsProcessed = emailsProcessed;
	}

	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String subject, String userName, String targetUserName, Message message, Network network) throws MessagingException {
		Vertex input = createInputParagraph(text.trim(), network);
		Vertex user = network.createSpeaker(userName);
		input.addRelationship(Primitive.INSTANTIATION, Primitive.EMAIL);
		input.getRelationship(Primitive.INPUT).addRelationship(Primitive.INSTANTIATION, Primitive.EMAIL);
		long date = 0;
		if (message.getReceivedDate() == null) {
			date = message.getSentDate().getTime();
		} else {
			date = message.getReceivedDate().getTime();
		}
		input.addRelationship(Primitive.CREATEDAT, network.createVertex(date));
		input.addRelationship(Primitive.ID, network.createVertex(message.getMessageNumber()));
		input.addRelationship(Primitive.SPEAKER, user);
		input.addRelationship(Primitive.TOPIC, network.createSentence(subject));
		// TODO, figure out reply chains
		Vertex conversation = network.createInstance(Primitive.CONVERSATION);
		Language.addToConversation(input, conversation);
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.TYPE, Primitive.EMAIL);
		if (targetUserName != null) {
			Vertex targetUser = null;
			if (targetUserName.equals(getEmailAddress())) {
				targetUser = network.createVertex(Primitive.SELF);
			} else {
				targetUser = network.createSpeaker(targetUserName);
			}
			input.addRelationship(Primitive.TARGET, targetUser);
			conversation.addRelationship(Primitive.SPEAKER, targetUser);
		}
		
		user.addRelationship(Primitive.INPUT, input);
		user.addRelationship(Primitive.EMAIL, input);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
	
}