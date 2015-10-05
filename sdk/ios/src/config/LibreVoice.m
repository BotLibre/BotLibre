/******************************************************************************
 *
 *  Copyright 2015 Paphus Solutions Inc.
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

#import "LibreVoice.h"

/**
 * This object configures the voice for a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@implementation LibreVoice

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<voice"];
    [self writeCredentials: xml];
    if (self.voice != nil && self.voice.length > 0) {
        [xml appendFormat:@" voice=\"%@\"", self.voice];
    }
    if (self.voiceType != nil && self.voiceType.length > 0) {
        [xml appendFormat:@" voiceType=\"%@\"", self.voiceType];
    }
    if (self.language != nil && self.language.length > 0) {
        [xml appendFormat:@" language=\"%@\"", self.language];
    }
    if (self.pitch != nil && self.pitch.length > 0) {
        [xml appendFormat:@" pitch=\"%@\"", self.voiceType];
    }
    if (self.speechRate != nil && self.speechRate.length > 0) {
        [xml appendFormat:@" speechRate=\"%@\"", self.speechRate];
    }
    
    [xml appendString: @"></voice>"];
    return xml;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.voice = [attributeDict objectForKey: @"voice"];
        self.voiceType = [attributeDict objectForKey: @"voiceType"];
        self.language = [attributeDict objectForKey: @"language"];
        self.pitch = [attributeDict objectForKey: @"pitch"];
        self.speechRate = [attributeDict objectForKey: @"speechRate"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}


@end
