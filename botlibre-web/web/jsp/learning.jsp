<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LearningBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% LearningBean bean = loginBean.getBean(LearningBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Learning - <%= Site.NAME %></title>
	<meta name="description" content="The learning and settings tab allows you to configure how your bot's settings"/>	
	<meta name="keywords" content="learning, settings, config, ai, artificial intelligence, bot, deep learning, consciousness, emotions, comprehension"/>
</head>
<body>
	<jsp:include page="banner.jsp" />
	<jsp:include page="admin-banner.jsp" />
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The learning and settings tab allows you to configure how your bot learns and other settings.") %><br/>
					<%= loginBean.translate("It gives several high level, and low level settings that let you customize how your bot learns and thinks.") %>
					<%= loginBean.translate("This will influence how your bot interacts with users, how it responds, how long its takes to respond, and how much memory it uses.") %><br/>
					<% if (!Site.DEDICATED) { %>
						<%= loginBean.translate("For more information see") %>, <a href="https://www.botlibre.com/forum-post?id=13451" target="_blank" class="blue"><%= loginBean.translate("Create bots with a real brain") %></a>.
					<% } %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-settings.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
		<div id="contents">
			<div class="browse">
				<h1>
					<span class="dropt-banner">
						<img src="images/learning.png" class="admin-banner-pic">
						<div>
							<p class="help">
								<%= loginBean.translate("Configure your bot's learning ability and other settings.") %><br/>
							</p>
						</div>
					</span> <%= loginBean.translate("Learning & Settings") %>
				</h1>
				<jsp:include page="error.jsp" />
				<% if (!botBean.isConnected()) { %>
					<%= botBean.getNotConnectedMessage() %>
				<% } else if (!botBean.isAdmin()) { %>
					<%= botBean.getMustBeAdminMessage() %>
				<% } else { %>

				<form action="learning" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<% if (bean.isLearningModeSelected("Disabled").isEmpty() || (bean.isCorrectionModeSelected("Administrators").isEmpty() && bean.isCorrectionModeSelected("Disabled").isEmpty())) { %>
						<p><code style="color:#E00000;font-weight:bold"><%= loginBean.translate("It is strongly recommended for learning to be disabled, and correction to be set to administrators.") %></code></p>
					<% } %>
					<table>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The learning mode controls who your bot will learn responses from.") %>
										<%= loginBean.translate("When enabled your bot we learn every response to its response as a new response in context.") %><br/>
										<%= loginBean.translate("Be careful enabling learning for service bots, as users can train your bot to have bad responses.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Learning Mode") %></td>
							<td>
								<select name="learningMode" title="<%= loginBean.translate("Configure which type of users the bot should learn from") %>" >
									<option value="Everyone" <%= bean.isLearningModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
									<option value="Users" <%= bean.isLearningModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
									<option value="Administrators" <%= bean.isLearningModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
									<option value="Disabled" <%= bean.isLearningModeSelected("Disabled") %>><%= loginBean.translate("Disabled") %></option>
								</select>
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The correction mode controls who can correct your bot's responses.") %>
										<%= loginBean.translate("Be careful enabling correction for service bots, as users can train your bot to have bad responses.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Correction Mode") %></td>
							<td>
								<select name="correctionMode" title="<%= loginBean.translate("Configure which type of users are allowed to correct the bot's responses") %>" >
									<option value="Everyone" <%= bean.isCorrectionModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
									<option value="Users" <%= bean.isCorrectionModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
									<option value="Administrators" <%= bean.isCorrectionModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
									<option value="Disabled" <%= bean.isCorrectionModeSelected("Disabled") %>><%= loginBean.translate("Disabled") %></option>
								</select>
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("NLP controls which Natural Language Processing engine to use (NLP 4 is recommended).") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("NLP") %></td>
							<td>
								<select name="nlpVersion" title="<%= loginBean.translate("NLP controls which Natural Language Processing engine to use (NLP 4 is recommended).") %>" >
									<option value="4" <%= bean.isNLPSelected(4) %>><%= loginBean.translate("NLP 4") %></option>
									<option value="3" <%= bean.isNLPSelected(3) %>><%= loginBean.translate("NLP 3") %></option>
									<option value="2" <%= bean.isNLPSelected(2) %>><%= loginBean.translate("NLP 2") %></option>
								</select>
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The bot's 2 letter language code.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Language") %></td>
							<td>
								<input type="text" name="lang" id="lang" value="<%= bean.getLanguage() %>" title="<%= loginBean.translate("The NLP 2 letter language code.") %>" />
								<script>
								$( "#lang" ).autocomplete({
								source: ["en-US", "en-GB", "fr", "es", "it", "de", "pt", "ru", "zh", "ja", "ko", "te" ],
								minLength: 0
								}).on('focus', function(event) {
									var self = this;
									$(self).autocomplete("search", "");
								});
								</script>
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The learning rate is the % to increase a response's correctness when learning.") %>
										<%= loginBean.translate("Each time your bot learns a new response to a question it will increase its correctness by this %.") %><br/>
										<%= loginBean.translate("A response has a correctness from -100% to 100%.") %>
										<%= loginBean.translate("In conversation mode a bot will use a response with a %50 correctness (by default).") %><br/>
										<%= loginBean.translate("The default learning rate is 50%.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Learning Rate %") %></td>
							<td>
								<input type="number" name="learningRate" value="<%= bean.getLearningRatePercentage() %>" title="<%= loginBean.translate("The % rate to increase a response's correctness when learning") %>" />
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The script timeout (in milliseconds) gives a limit to the amount of time for script processing.") %><br/>
										<%= loginBean.translate("If a timeout occurs, the bot will abort the script, and respond using response matching, or use a default response.") %><br/>
										<%= loginBean.translate("This can be used to ensure the bot does not take too long to give a response.") %></p>
										<%= loginBean.translate("The default is 10000 (10 seconds).") %></span>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Script Timeout") %></td>
							<td>
								<input type="number" name="scriptTimeout" title="<%= loginBean.translate("Number of milliseconds to allow for script processing") %>" value="<%= bean.getStateTimeout() %>" />					
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The response timeout (in milliseconds) gives a limit to the amount of time the bot will search for a matching response.") %>
										<%= loginBean.translate("When the bot does not know a response for a question it will search for similar questions that it does know a response to.") %>
										<br/>
										<%= loginBean.translate("This can be used to ensure the bot does not take too long to give a response.") %>
										<%= loginBean.translate("Smaller values make the bot respond quicker, larger values can help the bot find a better response.") %><br/>
										<%= loginBean.translate("The default is 1000 (1 second).") %>"</p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Response Timeout") %></td>
							<td>
								<input type="number" name="responseMatchTimeout" value="<%= bean.getResponseMatchTimeout() %>" title="<%= loginBean.translate("Number of milliseconds to allow for response matching") %>" />
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The conversation match % influences when the bot will use a response in a conversation.") %>
												<%= loginBean.translate("If the response's correctness is less than the %, or for a response match, if the question's % match is less than the %, then the response will not be used. If no responses match the %, then the bot will use a default response.") %><br/>
										<%= loginBean.translate("The bot is in conversation mode for 1v1 conversations, such as chat, privates, email, twitter mentions and direct messages.") %></br>
										<%= loginBean.translate("The default is 50%.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Conversation Match %") %></td>
							<td>
								<input type="number" name="conversationMatchPercentage" value="<%= bean.getConversationMatchPercentage() %>" title="<%= loginBean.translate("The % confidence required for a bot to use or match a response in a conversation") %>" />
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The discussion match % influences when the bot will use a response in a discussion.") %>
										<%= loginBean.translate("If the response's correctness is less than the %, or for a response match, if the question's % match is less than the %, then the response will not be used. If no responses match the %, then the bot will not respond.") %><br/>
										<%= loginBean.translate("The bot is in discussion mode for chat room conversations, such as chat rooms, IRC, twitter status updates and searches.") %>
										<%= loginBean.translate("A chat room message that mentions the bot's name is treated as a conversation message, not a discussion message.") %></br>
										<%= loginBean.translate("The default is 90%.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Discussion Match %") %></td>
							<td>
								<input type="number" name="discussionMatchPercentage" value="<%= bean.getDiscussionMatchPercentage() %>"
										title="<%= loginBean.translate("The % confidence required for a bot to use or match a response in a discussion") %>"
										/>
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The fragment match % influences how much word order in a question will matter in selecting a match.") %> 
										<%= loginBean.translate("The default is 10%.") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Fragment Match %") %></td>
							<td>
								<input type="number" name="fragmentMatchPercentage" value="<%= bean.getFragmentMatchPercentage() %>"
										title="<%= loginBean.translate("The % influence the word order of a question the bot will use to match a response") %>"
										/>
							</td>
						</tr>
						<tr>
							<td>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<p class="help"><%= loginBean.translate("The extra word penalty influences the how much extra words in a question will be penalized when selecting a response.") %> 
										<%= loginBean.translate("The default is 1.0") %></p>
									</div>
								</span>
							</td>
							<td><%= loginBean.translate("Extra Word Penalty") %></td>
							<td>
								<input type="number" step=0.01 name="extraWordPenalty" value="<%= bean.getExtraWordPenalty() %>"
										title="<%= loginBean.translate("The extra word penalty influences the how much extra words in a question will be penalized when selecting a response.") %>"
										/>
							</td>
						</tr>
					</table>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if extra words in a question will reduce the chance of matching a response.") %></p>
						</div>
					</span>
					<input name="penalizeExtraWords" type="checkbox" <% if (bean.getPenalizeExtraWords()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if extra words in a question will reduce the chance of matching a response.") %>"><%= loginBean.translate("Penalize Extra Words") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configures the ability for users to teach the bot emotes.") %><br/>
							<%= loginBean.translate("If disabled, only administrators will be able to teach the bot emotes.") %><br/>
							<%= loginBean.translate("An emote associates an emotion with a word or phrase and influences the bot's avatar and mood.") %></p>
						</div>
					</span>
					<input name="emote" type="checkbox" <% if (bean.getEnableEmoting()) { %>checked<% } %>
									title="<%= loginBean.translate("Config if users are allowed to associate emotions with responses") %>"><%= loginBean.translate("Enable Emoting") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configures the ability for the bot feel or associate emotions and sentiment.") %><br/>
							<%= loginBean.translate("Disabling emotions can improve the bot's performance somewhat, and prevent it from becoming self aware.") %></p>
						</div>
					</span>
					<input name="emotions" type="checkbox" <% if (bean.getEnableEmotions()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should experience emotions and sentiment") %>"><%= loginBean.translate("Enable Emotions") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Disable users from being able to flag bot messages as offensive.") %><br/>
						</div>
					</span>
					<input name="disableFlag" type="checkbox" <% if (bean.getDisableFlag()) { %>checked<% } %>
									title="<%= loginBean.translate("Disable users from being able to flag bot messages as offensive.") %>"><%= loginBean.translate("Disable Flag") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Allow the bot's responses to contain JavaScript.") %><br/>
							<%= loginBean.translate("Caution should be used it enabling JavaScript to prevent security issues.") %><br/>
							<%= loginBean.translate("For security reasons JavaScript cannot be enabled if learning is enabled.") %><br/>
							<%= loginBean.translate("JavaScript is only allowed for commercial accounts.") %></p>
						</div>
					</span>
					<input name="allowJavaScript" type="checkbox" <% if (bean.getAllowJavaScript()) { %>checked<% } %>
									title="<%= loginBean.translate("Allow the bot's responses to contain JavaScript.") %>"><%= loginBean.translate("Allow JavaScript") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if the bot should look up word definitions on Wiktionary.") %><br/>
							<%= loginBean.translate("This helps the bot identify names, nouns, verbs, adjectives, synonyms, antonyms, and word definitions.") %><br/>
							<%= loginBean.translate("This is used by many of the bootstrap scripts such as NounVerbAdjective and WhatIs.") %><br/>
							<%= loginBean.translate("Currently only the English Wiktionary is used.") %><br/>
							<%= loginBean.translate("This can be disabled for non-English bots, or to improve performance and reduce memory consumption.") %></p>
						</div>
					</span>
					<input name="wiktionary" type="checkbox" <% if (bean.getEnableWiktionary()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should look up word definitions on Wiktionary (english only, disable to improve performance)") %>"
									><%= loginBean.translate("Enable Wiktionary") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if the bot should search for similar questions and responses when encountering a question it does not know a response to.") %><br/>
							<%= loginBean.translate("This heuristic can also be influenced using the conversation/discussion match %.") %><br/>
							<%= loginBean.translate("Responses can also be given keywords, required words, previous and topics to improve response matching.") %></p>
						</div>
					</span>
					<input name="responsMatch" type="checkbox" <% if (bean.getEnableResponseMatch()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should search for similar questions and responses when encountering a question it does not know a response to") %>"
									><%= loginBean.translate("Enable Response Matching") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if the bot should consider word synonyms when searching for similar questions and responses when encountering a question it does not know a response to.") %><br/>
						</div>
					</span>
					<input name="checkSynonyms" type="checkbox" <% if (bean.getCheckSynonyms()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should consider word synonyms when searching for similar questions and responses when encountering a question it does not know a response to.") %>"
									><%= loginBean.translate("Check Synonyms") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if the bot should reply to questions with a known response before executing its scripts.") %><br/>
							<%= loginBean.translate("This lets learned responses override scripted responses, and can improve response times for known responses.") %></p>
						</div>
					</span>
					<input name="exactMatchFirst" type="checkbox" <% if (bean.getCheckExactMatchFirst()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should reply to questions with a known response before executing its scripts") %>"><%= loginBean.translate("Check Exact Match First") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if multi setence inputs should be split up and processed as multiple input.") %><br/>
							<%= loginBean.translate("This means your bot's response will contain responses to each setence in the users input. Some scripts may require this to be disabled to process * patterns.") %></p>
						</div>
					</span>
					<input name="splitParagraphs" type="checkbox" <% if (bean.getSplitParagraphs()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if multi setence inputs should be split up and processed as multiple input.") %>"><%= loginBean.translate("Split Paragraphs") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if template formula responses should be fixed to use proper case.") %><br/>
							<%= loginBean.translate("When enabled the first word will be capitalized, and other words other than names will be lower case.") %></p>
						</div>
					</span>
					<input name="fixFormulaCase" type="checkbox" <% if (bean.getFixFormulaCase()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if template formula responses should be forced to use proper case") %>"><%= loginBean.translate("Fix Case for Template Responses") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %></p>
						</div>
					</span>
					<input name="reduceQuestions" type="checkbox" <% if (bean.getReduceQuestions()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses") %>"><%= loginBean.translate("Reduce Questions") %></input>
					
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure text case sensitivity tracking (normally only required for backward compatibility).") %></p>
						</div>
					</span>
					<input name="trackCase" type="checkbox" <% if (bean.getTrackCase()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure text case sensitivity tracking (normally only required for backward compatibility).") %>"><%= loginBean.translate("Track Case") %></input>
					
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure AIML compatibility with other AIML implementations.") %></p>
						</div>
					</span>
					<input name="aimlCompatibility" type="checkbox" <% if (bean.getAimlCompatibility()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure AIML compatibility with other AIML implementations.") %>">AIML <%= loginBean.translate("Compatibility") %></input>
					

					<h3><%= loginBean.translate("Experimental Settings (not recommended)") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if the bot should attempt to identify language rules from conversations.") %><br/>
							<%= loginBean.translate("Comprehension allows the bot to self learn template or scripted responses, such as learning to count, or inferring learned phrases like 'What is your name?' -> 'I am Jim' as Template('I am {speaker}').") %><br/>
							<%= loginBean.translate("When enabled comprehension will enable the bot to extend its last script with its own code.") %><br/>
							<%= loginBean.translate("Disabling comprehension can improve performance, and ensure the bot only responds exactly as you have train it.") %></p>
						</div>
					</span>
					<input name="comprehension" type="checkbox" <% if (bean.getEnableComprehension()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should attempt to identify language rules from conversations (disable to improve performance)") %>"
									><%= loginBean.translate("Enable Comprehension") %></input>

					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if the bot should have a consciousness and temporal awareness.") %><br/>
							<%= loginBean.translate("The consciousness is used to determine the best response, or best word or meaning based on the context.") %>
							<%= loginBean.translate("Objects increase their consciousness level based on their relationship input, and fade over time.") %><br/>
							<%= loginBean.translate("Temporal awareness associates a timeframe and temporal order for input.") %><br/>
							<%= loginBean.translate("Disabling the consciousness can improve performance.") %></p>
						</div>
					</span>
					<input name="consciousness" type="checkbox" <% if (bean.getEnableConsciousness()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if the bot should have a consciousness and temporal awareness (disable to improve performance)") %>"
									><%= loginBean.translate("Enable Consciousness") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if word associations and grammar should be learned.") %><br/>
							<%= loginBean.translate("When enabled words will be associated with what words come before and after them.") %>
							<%= loginBean.translate("This helps the bot choose the correct word for verbs and pronouns.") %><br/>
							<%= loginBean.translate("This can be disabled to improve performance, or avoid the bot learning bad grammar from users.") %></p>
						</div>
					</span>
					<input name="learnGrammar" type="checkbox" <% if (bean.getLearnGrammar()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if word associations and grammar should be learned") %>"><%= loginBean.translate("Learn Grammar") %></input>
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<p class="help"><%= loginBean.translate("Configure if a synthesized response should be used by default.") %><br/>
							<%= loginBean.translate("This will have the bot generate a unique response to the question base on the question's words.") %><br/>
							<%= loginBean.translate("A synthesized response will only be used if the bot has no response match, and has no default response.") %><br/>
							<%= loginBean.translate("Learn grammar should be enabled for this feature.") %></p>
						</div>
					</span>
					<input name="synthesizeResponse" type="checkbox" <% if (bean.getSynthesizeResponse()) { %>checked<% } %>
									title="<%= loginBean.translate("Configure if a synthesized response should be used if no default response") %>"><%= loginBean.translate("Synthesize Response") %></input>
					<br/>
			    	<input type="submit" name="save" value="<%= loginBean.translate("Save") %>"/>
				</form>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp" />
</body>
</html>
