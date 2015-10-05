/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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
 * This object models a user.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to connect, create, edit, or browse a user instance.
 */
@interface LibreUser : LibreConfig

/** Password, require to connect a user, or create a user. */
@property NSString* password;
/** New password for editting a user's password (password is old password). */
@property NSString* password2;
/** Optional password hint, in case password is forgotten. */
@property NSString* hint;
/** Optional real name of the user. */
@property NSString* name;
/** The real name can be hidden from other users. */
@property BOOL showName;
/** Email, required for message notification, and to reset password. */
@property NSString* email;
/** Optional user's website. */
@property NSString* website;
/** Optional user's bio. */
@property NSMutableString* bio;
@property BOOL over18;
/** Read-only, server local URL for user's avatar image. */
@property NSMutableString* avatar;

/** Read-only, total user connects. */
@property NSString* connects;
/** Read-only, total bots created. */
@property NSString* bots;
/** Read-only, total forum posts. */
@property NSString* posts;
/** Read-only, total chat messages. */
@property NSString* messages;
/** Read-only, date user joined. */
@property NSString* joined;
/** Read-only, date of user's last connect. */
@property NSString* lastConnect;

@end
