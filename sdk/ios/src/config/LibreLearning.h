/******************************************************************************
 *
 *  Copyright 2015 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse @property License, Version 1.0 (the "License");
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

#import <Foundation/Foundation.h>
#import "LibreConfig.h"

/**
 * This object configures how a bot learns, and who is learns from.
 * It can be used to configure a bot.
 * The content instance ID is inherited from LibreConfig.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@interface LibreLearning : LibreConfig

/**
 * Configure who the bot learns from, ("Everyone", "Users", "Members", "Administrators").
 * Learning should be disabled for commercial or trained bots/virtual agents.
 */
@property NSString* learningMode;
/**
 * Configure who the bot can be corrected by, ("Everyone", "Users", "Members", "Administrators").
 * Members, or Administrator is recommended for commercial or trained bots/virtual agents.
 */
@property NSString* correctionMode;
/**
 * Integer percentage value used to match questions to similar questions to find a response.
 * The bot will only respond if the questions sufficiently matches, otherwise it will use a default response.
 * Default is 50% for 1v1 conversations (chat, direct message, mention, email).
 */
@property NSString* conversationMatchPercentage;
/**
 * Integer percentage value used to match questions to similar questions to find a response.
 * The bot will only respond if the questions sufficiently matches.
 * Default is 90% for discussions (chatroom, IRC, tweet search).
 */
@property NSString* discussionMatchPercentage;
/**
 * Comprehension is an advanced feature that lets the bot analyze learned responses and generate its own
 * Self program scripts, and formula responses based on its understanding of the words and its knowledgebase.
 * Caution should be used in enabling this feature, as the bot will be able to change its own program scripts.
 */
@property BOOL enableComprehension;
/**
 * Configure if users can associate emotional states with their phrases, or corrections.
 * If emotions are enbabled the bot will associate the phrases with the emotional states,
 * which will influence the bot's mood.
 */
@property BOOL enableEmoting;
/**
 * Configures if the bot should have an emotional state, and learn, and express emotions.
 * The emotions can be disable to improve performance.
 */
@property BOOL enableEmotions;
/**
 * The bot's consciousness tracks what is on the bot's mind,
 * through keeping track of a conscious state of input phrases, words, and knowledge.
 * The consciousness influences responses and relationships through finding the response or
 * relationship with the most conscious state.
 * The consciousness can be disable to improve performance.
 */
@property BOOL enableConsciousness;
/**
 * Configures the automatic lookup of all new words the bot encounters.
 * The words definition, meanings, and synonyms will be imported from Wiktionary.
 * Currently only English Wiktionary is used, so this should be disabled for non-English bots.
 * This can be disabled to improve performance.
 */
@property BOOL enableWiktionary;
/**
 * Configure if similar questions and responses should be searched for if no
 * exact or scripted response can be found for a question.
 * This can be disabled to improve performance.
 */
@property BOOL enableResponseMatch;
/**
 * Configure if the bot should track which words and tenses follow or preceed other words.
 * This knowledge can be used to choose the correct grammatical word for a meaning or knowledge item.
 * This can be disabled to improve performance.
 */
@property BOOL learnGrammar;
/**
 * Configures if formula responses should be fixed to uppercase the first word, and lowercase following words.
 */
@property BOOL fixFormulaCase;
/**
 * Configures if the bot should check exact question/response matches first, before running
 * its scripts.
 */
@property BOOL checkExactMatchFirst;
/** Time in milliseconds to timeout a script. */
@property NSString* scriptTimeout;
/** Time in milliseconds to timeout a response search. */
@property NSString* responseMatchTimeout;


@end
