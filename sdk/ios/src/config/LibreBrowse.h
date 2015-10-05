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
 * This object models the web API browse operation.
 * It can be used to search a set of instances (bots, forums, or channels).
 */
@interface LibreBrowse : LibreConfig

/** Filters instances by access type, "Public", "Private", "Personal". */
@property NSString* typeFilter;
/** Filters instances by categories (csv) */
@property NSString* category;
/** Filters instances by tags (csv) */
@property NSString* tag;
/** Filters instances by name */
@property NSString* filter;
/** Sorts instances, "name", "date", "size", "stars", "thumbs up", "thumbs down", "last connect", "connects", "connects today", "connects this week ", "connects this month" */
@property NSString* sort;

/** Stores the result of the browse operation, a set of instance objects. */
@property NSMutableArray* instances;

@end
