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

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.thought.consciousness.Consciousness;
import org.botlibre.thought.language.Comprehension;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.CorrectionMode;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.ContentRating;

public class LearningBean extends ServletBean {
	
	
	public LearningBean() {
	}

	public String isLearningModeSelected(String type) {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		LearningMode mode = bot.mind().getThought(Language.class).getLearningMode();
		if (mode == null) {
			return "";
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String isNLPSelected(int nlpVersion) {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		int nlp = bot.mind().getThought(Language.class).getNLP();
		if (nlp == nlpVersion) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isCorrectionModeSelected(String type) {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		CorrectionMode mode = bot.mind().getThought(Language.class).getCorrectionMode();
		if (mode == null) {
			return "";
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String getCorrectionMode() {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		return bot.mind().getThought(Language.class).getCorrectionMode().name();
	}

	public String getLearningMode() {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		return bot.mind().getThought(Language.class).getLearningMode().name();
	}

	public String getLanguage() {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		String lang = bot.mind().getThought(Language.class).getLanguage();
		if (lang == null) {
			return "en";
		}
		return lang;
	}

	public boolean getEnableEmoting() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getEnableEmote();
	}

	public boolean getEnableResponseMatch() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getEnableResponseMatch();
	}

	public boolean getCheckExactMatchFirst() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getCheckExactMatchFirst();
	}

	public boolean getCheckSynonyms() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getCheckSynonyms();
	}

	public boolean getLearnGrammar() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getLearnGrammar();
	}

	public boolean getSplitParagraphs() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getSplitParagraphs();
	}

	public boolean getSynthesizeResponse() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getSynthesizeResponse();
	}

	public boolean getFixFormulaCase() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getFixFormulaCase();
	}

	public boolean getReduceQuestions() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getReduceQuestions();
	}
	
	public boolean getPenalizeExtraWords() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getPenalizeExtraWords();
	}

	public boolean getTrackCase() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getTrackCase();
	}

	public boolean getAimlCompatibility() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Language.class).getAimlCompatibility();
	}

	public int getDiscussionMatchPercentage() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return (int)(bot.mind().getThought(Language.class).getDiscussionMatchPercentage() * 100);
	}
	
	public int getFragmentMatchPercentage() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return (int)(bot.mind().getThought(Language.class).getFragmentMatchPercentage() * 100);
	}
	
	public float getExtraWordPenalty() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return (float)(bot.mind().getThought(Language.class).getExtraWordPenalty());
	}

	public int getLearningRatePercentage() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return (int)(bot.mind().getThought(Language.class).getLearningRate() * 100);
	}

	public int getConversationMatchPercentage() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return (int)(bot.mind().getThought(Language.class).getConversationMatchPercentage() * 100);
	}

	public int getResponseMatchTimeout() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return bot.mind().getThought(Language.class).getMaxResponseMatchProcess();
	}

	public boolean getAllowJavaScript() {
		if (getBotBean().getInstance() == null) {
			return false;
		}
		return getBotBean().getInstance().getAllowJavaScript();
	}

	public boolean getDisableFlag() {
		if (getBotBean().getInstance() == null) {
			return false;
		}
		return getBotBean().getInstance().getDisableFlag();
	}

	public int getStateTimeout() {
		Bot bot = getBot();
		if (bot == null) {
			return 0;
		}
		return bot.mind().getThought(Language.class).getMaxStateProcess();
	}

	public boolean getEnableEmotions() {
		Bot bot = getBot();
		if (bot == null) {
			return true;
		}
		return bot.mood().isEnabled();
	}

	public boolean getEnableComprehension() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Comprehension.class).isEnabled();
	}

	public boolean getEnableConsciousness() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.mind().getThought(Consciousness.class).isEnabled();
	}

	public boolean getEnableWiktionary() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		return bot.awareness().getSense(Wiktionary.class).isEnabled();
	}

	public void save(String learn, String correct,
			String stateTimeout, String responseTimeout, String conversationMatch, String discussionMatch,
			boolean emote, Boolean emotions, Boolean disableFlag, Boolean allowJavaScript, boolean comprehend, boolean conscious, boolean enableWiktionary, Boolean responseMatching,
			Boolean checkExactMatchFirst, Boolean checkSynonyms, Boolean fixFormulaCase, Boolean reduceQuestions, Boolean trackCase, Boolean aimlCompatibility, Boolean learnGrammar, Boolean splitParagraphs, Boolean synthesize, String learningRate, 
			String nlp, String lang, String fragmentMatch, Boolean penalizeExtraWords, String extraWordPenalty) {
		
		nlp = Utils.sanitize(nlp);
		lang = Utils.sanitize(lang);
		Language language = getBot().mind().getThought(Language.class);
		language.setEnableEmote(emote);
		if (responseMatching != null) {
			language.setEnableResponseMatch(responseMatching);
		}
		if (checkExactMatchFirst != null) {
			language.setCheckExactMatchFirst(checkExactMatchFirst);
		}
		if (checkSynonyms != null) {
			language.setCheckSynonyms(checkSynonyms);
		}
		if (stateTimeout != null) {
			language.setMaxStateProcess(Integer.valueOf(stateTimeout));
		}
		if (responseTimeout != null) {
			language.setMaxResponseMatchProcess(Integer.valueOf(responseTimeout));
		}
		if (conversationMatch != null) {
			language.setConversationMatchPercentage(Integer.valueOf(conversationMatch) / 100f);
		}
		if (discussionMatch != null) {
			language.setDiscussionMatchPercentage(Integer.valueOf(discussionMatch) / 100f);
		}
		if (learningRate != null) {
			language.setLearningRate(Integer.valueOf(learningRate) / 100f);
		}
		if (allowJavaScript != null && getBotBean().getInstance().getAllowJavaScript() != allowJavaScript) {
			if (allowJavaScript) {
				if (!this.loginBean.isSuper()) {
					if (!Site.COMMERCIAL) {
						throw new BotException("JavaScript support is only offered in our commercial version");
					}
				}
				if (LearningMode.valueOf(learn) != LearningMode.Disabled) {
					throw new BotException("For security reasons JavaScript is not allowed if learning is enabled");
				}
				if (CorrectionMode.valueOf(correct) == CorrectionMode.Everyone || CorrectionMode.valueOf(correct) == CorrectionMode.Users) {
					throw new BotException("For security reasons JavaScript is not allowed if correction is everyone or users");
				}
			}
			getBotBean().setInstance(AdminDatabase.instance().updateInstanceSettings(getBotBean().getInstanceId(), allowJavaScript, null));
		}
		if (disableFlag != null && getBotBean().getInstance().getDisableFlag() != disableFlag) {
			if (disableFlag) {
				if (!this.loginBean.isSuper()) {
					if (!Site.COMMERCIAL) {
						throw new BotException("Disabling of flag is only offered in our commercial version");
					}
				}
				if (LearningMode.valueOf(learn) != LearningMode.Disabled) {
					throw new BotException("Disabling of flag is not allowed if learning is enabled");
				}
				if (CorrectionMode.valueOf(correct) == CorrectionMode.Everyone || CorrectionMode.valueOf(correct) == CorrectionMode.Users) {
					throw new BotException("Disabling of flag is not allowed if correction is everyone or users");
				}
			}
			getBotBean().setInstance(AdminDatabase.instance().updateInstanceSettings(getBotBean().getInstanceId(), null, disableFlag));
		}
		// Require bot to be rate mature if learning enabled.
		if (getBotBean().getInstance().getContentRating() != ContentRating.Mature && getBotBean().getInstance().getContentRating() != ContentRating.Adult) {
			if (LearningMode.valueOf(learn) == LearningMode.Everyone || LearningMode.valueOf(learn) == LearningMode.Users) {
				throw new BotException("You must first set your bot's content rating to 'mature' to enable learning from users");
			}
			if (CorrectionMode.valueOf(correct) == CorrectionMode.Everyone || CorrectionMode.valueOf(correct) == CorrectionMode.Users) {
				throw new BotException("You must first set your bot's content rating to 'mature' to enable correction from users");
			}
		}
		if (learnGrammar != null) {
			language.setLearnGrammar(learnGrammar);
		}
		if (splitParagraphs != null) {
			language.setSplitParagraphs(splitParagraphs);
		}
		if (synthesize != null) {
			language.setSynthesizeResponse(synthesize);
		}
		if (fixFormulaCase != null) {
			language.setFixFormulaCase(fixFormulaCase);
		}
		if (reduceQuestions != null) {
			language.setReduceQuestions(reduceQuestions);
		}
		if (nlp != null) {
			language.setNLP(Integer.valueOf(nlp));
		}
		if (lang != null) {
			language.setLanguage(lang);
		}
		if (fragmentMatch != null) {
			language.setFragmentMatchPercentage(Integer.valueOf(fragmentMatch) / 100f);
		}
		if (penalizeExtraWords != null) {
			language.setPenalizeExtraWords(penalizeExtraWords);
		}
		if (extraWordPenalty != null) {
			language.setExtraWordPenalty(Float.valueOf(extraWordPenalty));
		}
		if (trackCase != null) {
			language.setTrackCase(trackCase);
		}
		if (aimlCompatibility != null) {
			language.setAimlCompatibility(aimlCompatibility);
		}
		language.setLearningMode(LearningMode.valueOf(learn));
		language.setCorrectionMode(CorrectionMode.valueOf(correct));
		language.saveProperties();
		
		if (emotions != null) {
			getBot().mood().setEnabled(emotions);
			getBot().mood().saveProperties();
		}
		
		if (comprehend) {
			if (!this.loginBean.isSuper()) {
				if (!Site.DEDICATED) {
					throw new BotException("Comprehension is only supported for dedicated servers");
				}
			}
		}
		Comprehension comprehension = getBot().mind().getThought(Comprehension.class);
		comprehension.setEnabled(comprehend);
		comprehension.saveProperties();

		if (conscious) {
			if (!this.loginBean.isSuper()) {
				if (!Site.DEDICATED) {
					throw new BotException("Consciousness is only supported for dedicated servers");
				}
			}
		}
		Consciousness consciousness = getBot().mind().getThought(Consciousness.class);
		consciousness.setEnabled(conscious);
		consciousness.saveProperties();
		
		Wiktionary wiktionary = getBot().awareness().getSense(Wiktionary.class);
		wiktionary.setIsEnabled(enableWiktionary);
		wiktionary.saveProperties();
	}
}
