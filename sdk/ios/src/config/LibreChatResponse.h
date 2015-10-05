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

#import "LibreConfig.h"

/**
 * This object models a chat message received from a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@interface LibreChatResponse : LibreConfig

/** The conversation id for the message.  This will be returned from the first response, and must be used for all subsequent messages to maintain the conversational state.  Without the conversation id, the bot has no context for the reply. */
@property NSString* conversation;
/** Server relative URL for the avatar image or video. */
@property NSString* avatar;
/** Avatar MIME file type, (mpeg, webm, ogg, jpeg, png) */
@property NSString* avatarType;
/** Server relative URL for the avatar action image or video. */
@property NSString* avatarAction;
/** Avatar action MIME file type, (mpeg, webm, ogg, jpeg, png) */
@property NSString* avatarActionType;
/** Server relative URL for the avatar talking image or video. */
@property NSString* avatarTalk;
/** Avatar talk MIME file type, (mpeg, webm, ogg, jpeg, png) */
@property NSString* avatarTalkType;
/** Server relative URL for the avatar action audio image or video. */
@property NSString* avatarActionAudio;
/** Avatar action audio MIME file type,  (mpeg, wav) */
@property NSString* avatarActionAudioType;
/** Server relative URL for the avatar audio image or video. */
@property NSString* avatarAudio;
/** Avatar audio MIME file type,  (mpeg, wav) */
@property NSString* avatarAudioType;
/** Server relative URL for the avatar background image. */
@property NSString* avatarBackground;
/** Server relative URL for the avatar speech audio file. */
@property NSString* speech;
/**
 * Emotion attached to the bot's message, one of:
 *  NONE,
 *  LOVE, LIKE, DISLIKE, HATE,
 *	RAGE, ANGER, CALM, SERENE,
 *	ECSTATIC, HAPPY, SAD, CRYING,
 *	PANIC, AFRAID, CONFIDENT, COURAGEOUS,
 *	SURPRISE, BORED,
 *	LAUGHTER, SERIOUS
 */
@property NSString* emote;
/** Action for the bot's messages, such as "laugh", "smile", "kiss", or mobile directive (for virtual assistants). */
@property NSString* action;
/** Pose for the bot's messages, such as "dancing", "sitting", "sleeping". */
@property NSString* pose;
/** The bot's message text. */
@property NSMutableString* message;
/** Optional text to the original question. */
@property NSMutableString* question;

- (BOOL) isVideo;

- (BOOL) isWebM;

@end
