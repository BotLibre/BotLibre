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

#import "LibreChatMessage.h"

/**
 * This object models a chat message sent to a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to XML for web API usage.
 */
@implementation LibreChatMessage

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<chat"];
    [self writeCredentials: xml];
    if (self.conversation != nil && self.conversation.length > 0) {
        [xml appendFormat:@" conversation=\"%@\"", self.conversation];
    }
    if (self.emote != nil && self.emote.length > 0) {
        [xml appendFormat:@" emote=\"%@\"", self.emote];
    }
    if (self.action != nil && self.action.length > 0) {
        [xml appendFormat:@" action=\"%@\"", self.action];
    }
    if (self.speak) {
        [xml appendFormat:@" speak=\"true\""];
    }
    if (self.correction) {
        [xml appendFormat:@" correction=\"true\""];
    }
    if (self.offensive) {
        [xml appendFormat:@" offensive=\"true\""];
    }
    if (self.disconnect) {
        [xml appendFormat:@" disconnect=\"true\""];
    }
    [xml appendString: @">"];
    if (self.message != nil) {
        [xml appendString: @"<message>"];
        [xml appendString: [self escapeHTML: self.message]];
        [xml appendString: @"</message>"];
    }
    [xml appendString: @"</chat>"];
    return xml;
}

@end
