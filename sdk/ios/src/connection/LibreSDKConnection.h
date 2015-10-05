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
#import "LibreAction.h"

@class LibreCredentials;
@class LibreChatMessage;
@class LibreUser;
@class LibreDomain;
@class LibreWebMedium;
@class LibreBrowse;
@class LibreContent;

/**
 * Connection class for a REST service connection.
 * The SDK connection give you access to the paphus or libre server services using a REST API.
 * <p>
 * The services include:
 * <ul>
 * <li> User management (account creation, validation)
 * <li> Bot access, chat, and administration
 * <li> Forum access, posting, and administration
 * <li> Live chat access, chat, and administration
 * <li> Domain access, and administration
 * </ul>
 */
@interface LibreSDKConnection : NSObject

@property LibreUser* user;
@property LibreDomain* domain;
@property LibreCredentials* credentials;
@property BOOL debug;

@property NSMutableArray* templates;
@property NSMutableArray* categories;
@property NSMutableArray* tags;

+ (LibreSDKConnection*) sdk;
+ (NSArray*) types;
+ (NSArray*) channelTypes;
+ (NSArray*) accessModes;
+ (NSArray*) learningModes;
+ (NSArray*) correctionModes;
+ (NSArray*) botModes;
+ (NSArray*) licenses;

/**
 * Create an SDK connection with the credentials.
 * Use the Credentials subclass specific to your server.
 */
+ (LibreSDKConnection*) credentials: (LibreCredentials*) credentials;

/**
 * Return the default user image icon.
 */
- (UIImage*) defaultUserAvatar;

/**
 * Return the default bot image icon.
 */
- (UIImage*) defaultBotAvatar;

/**
 * Validate the user credentials (password, or token).
 * The user details are returned (with a connection token, password removed).
 * The user credentials are soted in the connection, and used on subsequent calls.
 * An exception is thrown if the connect failed.
 */
-(void) connect: (LibreUser*) config action: (id <LibreAction>) action;

/**
 * Disconnect from the connection.
 * An LibreSDKConnection does not keep a live connection, but this resets its connected user and domain.
 */
-(void) disconnect;

/**
 * Fetch the user details for the user credentials.
 * A token or password is required to validate the user.
 */
-(void) fetchUser: (LibreUser*) config action: (id <LibreAction>) action;

/**
 * Fetch the content details from the server.
 * The id or name and domain of the object must be set.
 */
-(void) fetch: (LibreWebMedium*) config action: (id <LibreAction>) action;

/**
 * Flag the content as offensive.
 */
- (void) flag: (LibreWebMedium*) config action: (id <LibreAction>) action;

/**
 * Delete the content.
 */
- (void) delete: (LibreWebMedium*) config action: (id <LibreAction>) action;

/**
 * Fetch the image from the server and return as a cached UIImage.
 */
- (UIImage*) fetchImage: (NSString*)imageName;

/**
 * Fetch the list of categories for the type, and domain.
 */
- (void) fetchCategories: (LibreContent*) config action: (id <LibreAction>) action;

/**
 * Fetch the list of tags for the type, and domain.
 */
- (void) fetchTags: (LibreContent*) config action: (id <LibreAction>) action;

/**
 * Fetch the list of bot templates.
 */
- (void) fetchTemplates: (id <LibreAction>) action;

/**
 * Return the full url for the server file.
 */
- (NSString*) getURL: (NSString*)imageName;
    
/**
 * Process the bot chat message and return the bot's response.
 * The LibreChat should contain the conversation id if part of a conversation.
 * If a new conversation the conversation id is returned in the response.
 */
- (void) chat: (LibreChatMessage*) config action: (id <LibreAction>) action;

/**
 * Return the list of content for the browse criteria.
 * The type defines the content type (one of Bot, Forum, Channel, Domain).
 */
- (void) browse: (LibreBrowse*) config action: (id <LibreAction>) action;

/**
 * Create a new user.
 */
- (void) createUser: (LibreUser*) config action: (id <LibreAction>) action;

/**
 * Update an existing user.
 */
- (void) updateUser: (LibreUser*) config action: (id <LibreAction>) action;

/**
 * Update the user's icon.
 * The file will be uploaded to the server.
 */
- (void) updateIcon: (UIImage*) image user: (LibreUser*) config action: (id <LibreAction>) action;

/**
 * Update the instance's icon.
 * The file will be uploaded to the server.
 */
- (void) updateIcon: (UIImage*) image instance: (LibreWebMedium*) config action: (id <LibreAction>) action;

/**
 * Create a new content.
 * The content will be returned to the action with its new id.
 */
- (void) create: (LibreWebMedium*) config action: (id <LibreAction>) action;

/**
 * Update content.
 */
- (void) update: (LibreWebMedium*) config action: (id <LibreAction>) action;

@end
