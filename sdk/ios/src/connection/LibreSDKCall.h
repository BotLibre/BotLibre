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
 * Connection call class for a REST service connection.
 */
@interface LibreSDKCall : NSObject <NSURLConnectionDelegate>

@property NSURLConnection* currentConnection;
@property NSMutableData* responseData;
@property LibreConfig* responseConfig;
@property id <LibreAction> currentAction;
@property BOOL error;
@property BOOL debug;

- (void) get: (NSString*) url;

- (void) post: (NSString*) url data: (NSString*) xml;

- (void) post: (NSString*) url image: (UIImage*)image data: (NSString*) xml;

@end
