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
 * This object configures the voice for a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@interface LibreVoice : LibreConfig

/** Voice name. */
@property NSString* voice;
/** Set type of voice should ("server", "device"). */
@property NSString* voiceType;
/** Voice language code (en, fr, en_US, etc.) */
@property NSString* language;
@property NSString* pitch;
@property NSString* speechRate;

@end
