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

#import "LibreConfig.h"

@class LibreSDKConnection;

/**
 * Abstract content class.
 * This object models a content object such as a bot, forum, or channel.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a content.
 */
@interface LibreWebMedium : LibreConfig

/** Instance ID. */
@property NSString* id;
/** Instance name. */
@property NSString* name;
/** Read-only, returns if connected user is the content's admin. */
@property BOOL isAdmin;
@property BOOL isAdult;
/** Sets if the content is private to the creator, and its members. */
@property BOOL isPrivate;
/** Sets if the conent will be visible and searchable in the content directory. */
@property BOOL isHidden;
/** Sets the access mode for the content, ("Everyone", "Users", "Members", "Administrators"). */
@property NSString* accessMode;
/** Returns if the content has been flagged, or used to flag content as offensive (reason required). */
@property BOOL isFlagged;
/** Returns why the content has been flagged, or used to flag content as offensive. */
@property NSMutableString* flaggedReason;
/** Can be used to create a link to external content in the content directory. */
@property BOOL isExternal;
/** Optional description of the content. */
@property NSMutableString* instanceDescription;
/** Optional restrictions or details of the content. */
@property NSMutableString* details;
/** Optional warning or disclaimer of the content. */
@property NSMutableString* disclaimer;
/** Tags to classify the content (csv). */
@property NSMutableString* tags;
/** Categories to categorize the content under (csv). */
@property NSMutableString* categories;
/** Read-only, returns content's creator's user ID. */
@property NSString* creator;
/** Read-only, returns content's creation date. */
@property NSString* creationDate;
/** Read-only, returns last user to access content */
@property NSMutableString* lastConnectedUser;
/** Optional license to license the content under. */
@property NSMutableString* license;
/** Optional website related to the content. */
@property NSMutableString* website;
/** Read-only, server local URL to content's avatar image. */
@property NSMutableString* avatar;
/** Read-only, returns content's toal connects. */
@property NSString* connects;
/** Read-only, returns content's daily connects. */
@property NSString* dailyConnects;
/** Read-only, returns content's weekly connects. */
@property NSString* weeklyConnects;
/** Read-only, returns content's monthly connects. */
@property NSString* monthlyConnects;

/** Internal parsing field. */
@property NSArray* instances;

- (NSString*) type;

- (LibreWebMedium*) credentials;

- (NSString*) stats;

- (void) writeXMLAttributes: (NSMutableString*)xml;

- (void) writeXMLElements: (NSMutableString*)xml;

- (LibreWebMedium*) copyWithZone: (NSZone*)zone;

@end
