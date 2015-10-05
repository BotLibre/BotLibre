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

#import "LibreForum.h"

/**
 * This object models a forum instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a forum instance.
 */
@implementation LibreForum

- (NSString*) stats {
    return [NSString stringWithFormat: @"%@ posts", self.posts];
}

- (NSString*) type {
    return @"forum";
}

- (void) writeXMLAttributes: (NSMutableString*)xml {
    [super writeXMLAttributes: xml];
    if (self.replyAccessMode != nil && self.replyAccessMode.length > 0) {
        [xml appendFormat: @" replyAccessMode=\"%@\"", self.replyAccessMode];
    }
    if (self.postAccessMode != nil && self.postAccessMode.length > 0) {
        [xml appendFormat: @" postAccessMode=\"%@\"", self.postAccessMode];
    }
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.replyAccessMode = [attributeDict objectForKey: @"replyAccessMode"];
        self.postAccessMode = [attributeDict objectForKey: @"postAccessMode"];
        self.posts = [attributeDict objectForKey: @"posts"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

@end
