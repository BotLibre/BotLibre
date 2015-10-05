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

/**
 * This object models a forum post.
 * It can be used from a forum UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * You must set the forum id as the forum of the forum post.
 * A forum post that has a parent (parent forum post id) is a reply.
 */
@interface LibreForumPost : LibreConfig

@property NSString* id;
@property NSMutableString* topic;
@property NSMutableString* summary;
@property NSMutableString* details;
@property NSMutableString* detailsText;
@property NSString* forum;
@property NSMutableString* tags;
@property BOOL isAdmin;
@property BOOL isFlagged;
@property NSMutableString* flaggedReason;
@property BOOL isFeatured;
@property NSString* creator;
@property NSString* creationDate;
@property NSString* views;
@property NSString* dailyViews;
@property NSString* weeklyViews;
@property NSString* monthlyViews;
@property NSString* replyCount;
@property NSString* parent;
@property NSMutableString* avatar;
@property NSMutableArray* replies;

@end
