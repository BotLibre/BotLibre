/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License"];
 *  you may not use self file except in compliance with the License.
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

#import "LibreBot.h"

/**
 * This object models a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a bot instance.
 */
@implementation LibreBot

- (NSString*) stats {
    return [NSString stringWithFormat: @"%@ connects, %@ today, %@ week, %@ month", self.connects, self.dailyConnects, self.weeklyConnects, self.monthlyConnects];
}

- (NSString*) type {
    return @"instance";
}

- (LibreBot*) copyWithZone: (NSZone*)zone {
    LibreBot* instance = (LibreBot*)[super copyWithZone: zone];
    instance.template = self.template;
    instance.size = self.size;
    return instance;
}

- (void) writeXMLAttributes: (NSMutableString*)xml {
    [super writeXMLAttributes: xml];
    if (self.allowForking) {
        [xml appendFormat:@" allowForking=\"true\""];
    }
}

- (void) writeXMLElements: (NSMutableString*)xml {
    [super writeXMLElements: xml];
    if (self.template != nil) {
        [xml appendString: @"<template>"];
        [xml appendString: self.template];
        [xml appendString: @"</template>"];
    }
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.allowForking = [[attributeDict objectForKey: @"allowForking"] boolValue];
        self.size = [attributeDict objectForKey: @"size"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    [super parser: parser foundCharacters: string];
    
    if ([self.parseElement isEqualToString: @"template" ]) {
        self.template = string;
    }
}

@end
