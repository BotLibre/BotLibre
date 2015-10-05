/******************************************************************************
 *
 *  Copyright 2015 Paphus Solutions Inc.
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

#import "LibreAvatar.h"

/**
 * This object models an avatar instance.
 * It can be used from the avatar or bot admin UI.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, link, or browse an avatar instance.
 */
@implementation LibreAvatar

- (NSString*) type {
    return @"avatar";
}

- (void) writeXMLAttributes: (NSMutableString*)xml {
    [super writeXMLAttributes: xml];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

@end
