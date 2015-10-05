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

#import "LibreContent.h"
#import "LibreAction.h"

/**
 * DTO to parse response of a list of names.
 * This is used for categories, tags, and templates.
 */
@implementation LibreContent {
    LibreContent* currentInstance;
}

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<content"];
    [self writeCredentials: xml];
    [xml appendString: @"></content>"];
    return xml;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if ([elementName isEqualToString: @"tagConfigs"] || [elementName isEqualToString: @"categoryConfigs"]) {
        self.instances = [[NSMutableArray alloc] init];
        currentInstance = [LibreContent alloc];
    } else if ([elementName isEqualToString: @"tag"] || [elementName isEqualToString: @"category"]) {
        currentInstance.name = [attributeDict objectForKey: @"name"];
        currentInstance.icon = [attributeDict objectForKey: @"icon"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    if (currentInstance != nil) {
        if ([self.parseElement isEqualToString: @"description"]) {
            if (currentInstance.contentDescription == nil) {
                currentInstance.contentDescription = [NSMutableString string];
            }
            [currentInstance.contentDescription appendString: string];
        }
    }
}

- (void) parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if (currentInstance != nil) {
        if ([elementName isEqualToString: @"tag"] || [elementName isEqualToString: @"category"]) {
            [self.instances addObject: currentInstance];
            currentInstance = [LibreContent alloc];
        } else if ([elementName isEqualToString: @"tagConfigs"] || [elementName isEqualToString: @"categoryConfigs"]) {
            currentInstance = nil;
        }
    }
}

- (void) parserDidEndDocument:(NSXMLParser *)parser {    
    if (self.responseAction != nil) {
        [self.responseAction response: self];
    }
}


@end
