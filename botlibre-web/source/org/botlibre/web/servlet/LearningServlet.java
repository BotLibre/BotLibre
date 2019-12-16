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
package org.botlibre.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.web.bean.LearningBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/learning")
@SuppressWarnings("serial")
public class LearningServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		BotBean botBean = loginBean.getBotBean();
		LearningBean bean = loginBean.getBean(LearningBean.class);

		String save = (String)request.getParameter("save");
		String learn = request.getParameter("learningMode");
		String nlp = request.getParameter("nlpVersion");
		String correct = request.getParameter("correctionMode");
		String scriptTimeout = request.getParameter("scriptTimeout");
		String responseMatchTimeout = request.getParameter("responseMatchTimeout");
		String conversationMatchPercentage = request.getParameter("conversationMatchPercentage");
		String discussionMatchPercentage = request.getParameter("discussionMatchPercentage");
		String fragmentMatchPercentage = request.getParameter("fragmentMatchPercentage");
		String learningRate = request.getParameter("learningRate");
		String extraWordPenalty = request.getParameter("extraWordPenalty");
		String lang = request.getParameter("lang");
		boolean emote = "on".equals(request.getParameter("emote"));
		boolean emotions = "on".equals(request.getParameter("emotions"));
		boolean comprehension = "on".equals(request.getParameter("comprehension"));
		boolean consciousness = "on".equals(request.getParameter("consciousness"));
		boolean wiktionary = "on".equals(request.getParameter("wiktionary"));
		boolean responsMatch = "on".equals(request.getParameter("responsMatch"));
		boolean exactMatchFirst = "on".equals(request.getParameter("exactMatchFirst"));
		boolean checkSynonyms = "on".equals(request.getParameter("checkSynonyms"));
		boolean learnGrammar = "on".equals(request.getParameter("learnGrammar"));
		boolean splitParagraphs = "on".equals(request.getParameter("splitParagraphs"));
		boolean synthesize = "on".equals(request.getParameter("synthesizeResponse"));
		boolean fixFormulaCase = "on".equals(request.getParameter("fixFormulaCase"));
		boolean reduceQuestions = "on".equals(request.getParameter("reduceQuestions"));
		boolean trackCase = "on".equals(request.getParameter("trackCase"));
		boolean aimlCompatibility = "on".equals(request.getParameter("aimlCompatibility"));
		boolean disableFlag = "on".equals(request.getParameter("disableFlag"));
		boolean allowJavaScript = "on".equals(request.getParameter("allowJavaScript"));
		boolean penalizeExtraWords = "on".equals(request.getParameter("penalizeExtraWords"));
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				response.sendRedirect("learning.jsp");
				return;
			}
			botBean.checkAdmin();
			if (save != null) {
				bean.save(learn, correct, scriptTimeout, responseMatchTimeout, conversationMatchPercentage, discussionMatchPercentage,
								emote, emotions, disableFlag, allowJavaScript, comprehension, consciousness, wiktionary, responsMatch, exactMatchFirst, checkSynonyms,
								fixFormulaCase, reduceQuestions, trackCase, aimlCompatibility, learnGrammar, splitParagraphs, synthesize, learningRate,
								nlp, lang, fragmentMatchPercentage, penalizeExtraWords, extraWordPenalty);
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("learning.jsp");
	}
}
