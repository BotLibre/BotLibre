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

#import "LibreLearning.h"

/**
 * This object configures how a bot learns, and who is learns from.
 * It can be used to configure a bot.
 * The content instance ID is inherited from LibreConfig.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@implementation LibreLearning

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<learning"];
    [self writeCredentials: xml];
    if (self.learningMode != nil && self.learningMode.length > 0) {
        [xml appendFormat:@" learningMode=\"%@\"", self.learningMode];
    }
    if (self.correctionMode != nil && self.correctionMode.length > 0) {
        [xml appendFormat:@" correctionMode=\"%@\"", self.correctionMode];
    }
    if (self.conversationMatchPercentage != nil && self.conversationMatchPercentage.length > 0) {
        [xml appendFormat:@" conversationMatchPercentage=\"%@\"", self.conversationMatchPercentage];
    }
    if (self.discussionMatchPercentage != nil && self.discussionMatchPercentage.length > 0) {
        [xml appendFormat:@" discussionMatchPercentage=\"%@\"", self.discussionMatchPercentage];
    }
    [xml appendFormat:@" enableComprehension=\"%@\"", self.enableComprehension ? @"true" : @"false"];
    [xml appendFormat:@" enableEmoting=\"%@\"", self.enableEmoting ? @"true" : @"false"];
    [xml appendFormat:@" enableEmotions=\"%@\"", self.enableEmotions ? @"true" : @"false"];
    [xml appendFormat:@" enableConsciousness=\"%@\"", self.enableConsciousness ? @"true" : @"false"];
    [xml appendFormat:@" enableWiktionary=\"%@\"", self.enableWiktionary ? @"true" : @"false"];
    [xml appendFormat:@" enableResponseMatch=\"%@\"", self.enableResponseMatch ? @"true" : @"false"];
    [xml appendFormat:@" learnGrammar=\"%@\"", self.learnGrammar ? @"true" : @"false"];
    [xml appendFormat:@" fixFormulaCase=\"%@\"", self.fixFormulaCase ? @"true" : @"false"];
    [xml appendFormat:@" checkExactMatchFirst=\"%@\"", self.checkExactMatchFirst ? @"true" : @"false"];
    if (self.scriptTimeout != nil && self.scriptTimeout.length > 0) {
        [xml appendFormat:@" scriptTimeout=\"%@\"", self.scriptTimeout];
    }
    if (self.responseMatchTimeout != nil && self.responseMatchTimeout.length > 0) {
        [xml appendFormat:@" responseMatchTimeout=\"%@\"", self.responseMatchTimeout];
    }
    
    [xml appendString: @"></learning>"];
    return xml;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.learningMode = [attributeDict objectForKey: @"learningMode"];
        self.correctionMode = [attributeDict objectForKey: @"correctionMode"];
        self.conversationMatchPercentage = [attributeDict objectForKey: @"conversationMatchPercentage"];
        self.discussionMatchPercentage = [attributeDict objectForKey: @"discussionMatchPercentage"];
        self.enableComprehension = [[attributeDict objectForKey: @"enableComprehension"] boolValue];
        self.enableEmoting = [[attributeDict objectForKey: @"enableEmoting"] boolValue];
        self.enableEmotions = [[attributeDict objectForKey: @"enableEmotions"] boolValue];
        self.enableConsciousness = [[attributeDict objectForKey: @"enableConsciousness"] boolValue];
        self.enableWiktionary = [[attributeDict objectForKey: @"enableWiktionary"] boolValue];
        self.enableResponseMatch = [[attributeDict objectForKey: @"enableResponseMatch"] boolValue];
        self.learnGrammar = [[attributeDict objectForKey: @"learnGrammar"] boolValue];
        self.fixFormulaCase = [[attributeDict objectForKey: @"fixFormulaCase"] boolValue];
        self.checkExactMatchFirst = [[attributeDict objectForKey: @"checkExactMatchFirst"] boolValue];
        self.scriptTimeout = [attributeDict objectForKey: @"scriptTimeout"];
        self.responseMatchTimeout = [attributeDict objectForKey: @"responseMatchTimeout"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}


@end
