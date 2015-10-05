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

#import "LibreWebMedium.h"

/**
 * This object models a forum instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a forum instance.
 */
@interface LibreForum : LibreWebMedium

/** Sets the access mode for forum post replies, ("Everyone", "Users", "Members", "Administrators"). */
@property NSString* replyAccessMode;
/** Sets the access mode for forum posts, ("Everyone", "Users", "Members", "Administrators"). */
@property NSString* postAccessMode;
/** Read-only property for the total number of posts to the forum. */
@property NSString* posts;

@end
