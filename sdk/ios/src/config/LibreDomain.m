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

#import "LibreDomain.h"

/**
 * This object models a domain.
 * It can be used from a chat UI, or with the Libre Web API.
 * A domain is an isolated content space to create bots and other content in (such as a commpany, project, or school).
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a domain instance.
 */
@implementation LibreDomain

- (NSString*) type {
    return @"domain";
}

- (void) writeXMLAttributes: (NSMutableString*)xml {
    [super writeXMLAttributes: xml];
    
    if (self.creationMode != nil && self.creationMode.length > 0) {
        [xml appendFormat: @" creationMode=\"%@\"", self.creationMode];
    }
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.creationMode = [attributeDict objectForKey: @"creationMode"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }        
}

@end
