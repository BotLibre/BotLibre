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

#import <Foundation/Foundation.h>
#import "LibreConfig.h"

/**
 * This object models a chat message sent to a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to XML for web API usage.
 */
@interface LibreChatMessage : LibreConfig

/** Normally not used, only require to access private instances, but a token should be used instead (user and token are inherited from config). */
@property NSString* password;
/** Sets the message to be a correction to the bot's last response. */
@property BOOL correction;
/** Flags the bot's last response as offensive. */
@property BOOL offensive;
/** Ends the conversation. Conversation should be terminated to converse server resources.  The message can be blank. */
@property BOOL disconnect;
/** 
 * Attaches an emotion to the user's message, one of:
 *  NONE,
 *  LOVE, LIKE, DISLIKE, HATE,
 *	RAGE, ANGER, CALM, SERENE,
 *	ECSTATIC, HAPPY, SAD, CRYING,
 *	PANIC, AFRAID, CONFIDENT, COURAGEOUS,
 *	SURPRISE, BORED,
 *	LAUGHTER, SERIOUS
 */
@property NSString* emote;
/** Attaches an action to the user's messages, such as "laugh", "smile", "kiss". */
@property NSString* action;
/** The user's message text. */
@property NSString* message;
/** The conversation id for the message.  This will be returned from the first response, and must be used for all subsequent messages to maintain the conversational state.  Without the conversation id, the bot has no context for the reply. */
@property NSString* conversation;
/** Sets if the voice audio should be generated for the bot's response. */
@property BOOL speak;

@end
