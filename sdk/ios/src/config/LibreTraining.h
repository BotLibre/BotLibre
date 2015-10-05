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
 * This object teaches a bot a new response.
 * The content instance ID is inherited from LibreConfig.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@interface LibreTraining : LibreConfig

/** Type of response ("Response", "Greeting", "DefaultResponse"). */
@property NSString* operation;
/** The question phrase or pattern (i.e. "hello", "what is your name", "Pattern:^ help ^"). */
@property NSString* question;
/** The response phrase or formula (i.e. "Hello there.", "Formula:"My name is {:target}."", "What would you like help with?"). */
@property NSString* response;
/** The topic for the response (optional) */
@property NSString* topic;
/** The keywords for the response (optional, csv) */
@property NSString* keywords;
/** The required words for the response (optional, csv) */
@property NSString* required;
/** The previous response or pattern for the response (optional) */
@property NSString* previous;
/** Set if the previous response is required. */
@property BOOL requirePrevious;
/** The emotions to associate with the response (optional, csv, i.e. love, like, hate, anger, surprise, laughter) */
@property NSString* emotions;
/** The actions to associate with the response (optional, csv, i.e. smile, laugh, kiss) */
@property NSString* actions;
/** The poses to associate with the response (optional, csv, i.e. dancing, sleeping, sitting) */
@property NSString* poses;

@end
