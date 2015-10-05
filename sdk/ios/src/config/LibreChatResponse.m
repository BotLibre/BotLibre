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
#import "LibreChatResponse.h"

/**
 * This object models a chat message received from a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@implementation LibreChatResponse : LibreConfig

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.conversation = [attributeDict objectForKey: @"conversation"];
        self.avatar = [attributeDict objectForKey: @"avatar"];
        self.avatarType = [attributeDict objectForKey: @"avatarType"];
        self.avatarAction = [attributeDict objectForKey: @"avatarAction"];
        self.avatarActionType = [attributeDict objectForKey: @"avatarActionType"];
        self.avatarTalk = [attributeDict objectForKey: @"avatarTalk"];
        self.avatarTalkType = [attributeDict objectForKey: @"avatarTalkType"];
        self.avatarActionAudio = [attributeDict objectForKey: @"avatarActionAudio"];
        self.avatarActionAudioType = [attributeDict objectForKey: @"avatarActionAudioType"];
        self.avatarAudio = [attributeDict objectForKey: @"avatarAudio"];
        self.avatarAudioType = [attributeDict objectForKey: @"avatarAudioType"];
        self.avatarBackground = [attributeDict objectForKey: @"avatarBackground"];
        self.speech = [attributeDict objectForKey: @"speech"];
        self.emote = [attributeDict objectForKey: @"emote"];
        self.action = [attributeDict objectForKey: @"action"];
        self.pose = [attributeDict objectForKey: @"pose"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    if ([self.parseElement isEqualToString: @"message" ]) {
        if (self.message == nil) {
            self.message = [NSMutableString string];
        }
        [self.message appendString: string];
    } else if ([self.parseElement isEqualToString: @"question" ]) {
        if (self.question == nil) {
            self.question = [NSMutableString string];
        }
        [self.question appendString: string];
    }
}

- (BOOL) isVideo {
    return self.avatarType != nil && [self.avatarType rangeOfString: @"video"].location != NSNotFound;
}

- (BOOL) isWebM {
    return self.avatarType != nil && [self.avatarType rangeOfString: @"webm"].location != NSNotFound;
}

@end
