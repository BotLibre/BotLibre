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
 * This object configures a bot or content's user access.
 * It can be used to add members, or administrators to a bot or content.
 * The content instance ID is inherited from LibreConfig.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@interface LibreUserAdmin : LibreConfig

/** Type of user administration operation ("AddUser", "RemoveUser", "AddAdmin", "RemoveAdmin"). */
@property NSString* operation;
/** User ID to perform the administration on. */
@property NSString* operationUser;

@end
