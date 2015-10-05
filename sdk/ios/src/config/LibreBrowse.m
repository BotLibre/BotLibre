//
//  LibreChatMessage.m
//  BOT libre!
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

#import "LibreUser.h"
#import "LibreBrowse.h"
#import "LibreWebMedium.h"
#import "LibreBot.h"
#import "LibreAction.h"

/**
 * This object models the web API browse operation.
 * It can be used to search a set of instances (bots, forums, or channels).
 */
@implementation LibreBrowse {
    LibreWebMedium* currentInstance;
}

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<browse"];
    [self writeCredentials: xml];
    if (self.typeFilter != nil && self.typeFilter.length > 0) {
        [xml appendFormat:@" typeFilter=\"%@\"", self.typeFilter];
    }
    if (self.sort != nil && self.sort.length > 0) {
        [xml appendFormat:@" sort=\"%@\"", self.sort];
    }
    if (self.category != nil && self.category > 0) {
        [xml appendFormat:@" category=\"%@\"", self.category];
    }
    if (self.tag != nil && self.tag.length > 0) {
        [xml appendFormat:@" tag=\"%@\"", self.tag];
    }
    if (self.filter != nil && self.filter.length > 0) {
        [xml appendFormat:@" filter=\"%@\"", self.filter];
    }
    
    [xml appendString: @"/>"];
    return xml;
}

- (LibreWebMedium*) newInstance {
    return [LibreBot alloc];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if ([elementName isEqualToString: @"instanceConfigs"]) {
        self.instances = [[NSMutableArray alloc] init];
        currentInstance = [self newInstance];
    } else if (currentInstance != nil) {
        [currentInstance parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    if (currentInstance != nil) {
        [currentInstance parser: parser foundCharacters: string];
    }
}

- (void) parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if (currentInstance != nil) {
        if ([elementName isEqualToString: @"instance"]) {
            [self.instances addObject: currentInstance];
            currentInstance = [self newInstance];
        } else if ([elementName isEqualToString: @"instanceConfigs"]) {
            currentInstance = nil;
        } else {
            [currentInstance parser: parser didEndElement: elementName namespaceURI: namespaceURI qualifiedName: qName];
        }
    }
}

- (void) parserDidEndDocument:(NSXMLParser *)parser {    
    if (self.responseAction != nil) {
        [self.responseAction response: self];
    }
}


@end
