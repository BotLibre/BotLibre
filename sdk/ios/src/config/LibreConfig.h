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

@class LibreSDKConnection;

/**
 * Abstract root class for all web API message objects.
 * Defines the required application id, and common fields.
 */
@interface LibreConfig : NSObject <NSXMLParserDelegate>

@property id <LibreAction> responseAction;

/** The application ID.  This is require to authenticate the API usage.  You can obtain your application ID from your user page. */
@property NSString* application;
/** Optional domain id, if object is not on the server's default domain. */
@property NSString* domain;
/** User ID, required for content creation, secure content access, or to identify the user. */
@property NSString* user;
/** User's access token, returned from connect web API, can be used in place of password in subsequent calls, and stored in a cookie.   The user's password should never be stored. */
@property NSString* token;
/** The id or name of the bot or content instance to access. */
@property NSString* instance;
/** Type of instance to access, ("Bot", "Forum", "Channel", "Domain") */
@property NSString* type;

/** Internal parsing element. */
@property NSString* parseElement;

- (void) addCredentials: (LibreSDKConnection*) connection;
    
- (void) parseXML: (NSData*) data;

- (void) writeCredentials: (NSMutableString*) writer;

- (NSString*) escapeHTML: (NSString*)html;

- (NSString*) toXML;

@end
