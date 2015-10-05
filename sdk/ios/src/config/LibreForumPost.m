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

#import "LibreForumPost.h"

/**
 * This object models a forum post.
 * It can be used from a forum UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * You must set the forum id as the forum of the forum post.
 * A forum post that has a parent (parent forum post id) is a reply.
 */
@implementation LibreForumPost

LibreForumPost* currentInstance;

- (BOOL) isEqual: (id)other {
    if ([other isKindOfClass: [LibreForumPost class]]) {
        if (self.id == nil) {
            return [super isEqual: other];
        }
        return [self.id isEqualToString: (((LibreForumPost*)other).id)];
    }
    return false;
}

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<forum-post"];
    [self writeCredentials: xml];
    if (self.id != nil && self.id.length > 0) {
        [xml appendFormat: @" id=\"%@\"", self.id];
    }
    if (self.parent != nil && self.parent.length > 0) {
        [xml appendFormat: @" parent=\"%@\"", self.parent];
    }
    if (self.forum != nil && self.forum.length > 0) {
        [xml appendFormat: @" forum=\"%@\"", self.forum];
    }
    if (self.isFeatured) {
        [xml appendString: @" isFeatured=\"true\""];
    }
    [xml appendString: @">"];

    if (self.topic != nil) {
        [xml appendString: @"<topic>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.topic]];
        [xml appendString: @"</topic>"];
    }
    if (self.details != nil) {
        [xml appendString: @"<details>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.details]];
        [xml appendString: @"</details>"];
    }
    if (self.tags != nil) {
        [xml appendString: @"<tags>"];
        [xml appendFormat: @"%@", self.tags];
        [xml appendString: @"</tags>"];
    }
    [xml appendString: @"</forum-post>"];
    return xml;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if ([elementName isEqualToString: @"replies"]) {
        self.replies = [[NSMutableArray alloc] init];
        currentInstance = [[self class] alloc];
        return;
    } else if (currentInstance != nil) {
        [currentInstance parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        return;
    }
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.id = [attributeDict objectForKey: @"id"];
        self.parent = [attributeDict objectForKey: @"parent"];
        self.forum = [attributeDict objectForKey: @"forum"];
        self.views = [attributeDict objectForKey: @"views"];
        self.dailyViews = [attributeDict objectForKey: @"dailyViews"];
        self.weeklyViews = [attributeDict objectForKey: @"weeklyViews"];
        self.monthlyViews = [attributeDict objectForKey: @"monthlyViews"];
        self.isAdmin = [[attributeDict objectForKey: @"isAdmin"] boolValue];
        self.replyCount = [attributeDict objectForKey: @"replyCount"];
        self.isFlagged = [[attributeDict objectForKey: @"isFlagged"] boolValue];
        self.isFeatured = [[attributeDict objectForKey: @"isFeatured"] boolValue];
        self.creator = [attributeDict objectForKey: @"creator"];
        self.creationDate = [attributeDict objectForKey: @"creationDate"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    if (currentInstance != nil) {
        [currentInstance parser: parser foundCharacters: string];
        return;
    }

    if ([self.parseElement isEqualToString: @"summary"]) {
        if (self.summary == nil) {
            self.summary = [NSMutableString string];
        }
        [self.summary appendString: string];
    } else if ([self.parseElement isEqualToString: @"details"]) {
        if (self.details == nil) {
            self.details = [NSMutableString string];
        }
        [self.details appendString: string];
    } else if ([self.parseElement isEqualToString: @"detailsText"]) {
        if (self.detailsText == nil) {
            self.detailsText = [NSMutableString string];
        }
        [self.detailsText appendString: string];
    } else if ([self.parseElement isEqualToString: @"topic"]) {
        if (self.topic == nil) {
            self.topic = [NSMutableString string];
        }
        [self.topic appendString: string];
    } else if ([self.parseElement isEqualToString: @"tags"]) {
        if (self.tags == nil) {
            self.tags = [NSMutableString string];
        }
        [self.tags appendString: string];
    } else if ([self.parseElement isEqualToString: @"flaggedReason"]) {
        if (self.flaggedReason == nil) {
            self.flaggedReason = [NSMutableString string];
        }
        [self.flaggedReason appendString: string];
    } else if ([self.parseElement isEqualToString: @"avatar"]) {
        if (self.avatar == nil) {
            self.avatar = [NSMutableString string];
        }
        [self.avatar appendString: string];
    }
}

- (void) parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if (currentInstance != nil) {
        if ([elementName isEqualToString: @"forum-post"]) {
            [self.replies addObject: currentInstance];
            currentInstance = [[self class ] alloc];
        } else if ([elementName isEqualToString: @"replies"]) {
            currentInstance = nil;
        } else {
            [currentInstance parser: parser didEndElement: elementName namespaceURI: namespaceURI qualifiedName: qName];
        }
    }
}


@end
