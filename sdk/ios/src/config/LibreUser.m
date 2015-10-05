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
#import "LibreChatMessage.h"
#import "LibreSDKConnection.h"
#import "LibreCredentials.h"
#import "LibreDomain.h"
#import "LibreUI.h"

/**
 * This object models a user.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to connect, create, edit, or browse a user instance.
 */
@implementation LibreUser

- (void) addCredentials: (LibreSDKConnection*) connection {
    self.application = connection.credentials.applicationId;
    if (connection.domain != nil) {
        self.domain = connection.domain.id;
    }
}

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<user"];
    [self writeCredentials: xml];
    if (self.password != nil && self.password.length > 0) {
        [xml appendFormat:@" password=\"%@\"", self.password];
    }
    if (self.password2 != nil && self.password2.length > 0) {
        [xml appendFormat:@" newPassword=\"%@\"", self.password2];
    }
    if (self.hint != nil && self.hint.length > 0) {
        [xml appendFormat:@" hint=\"%@\"", self.hint];
    }
    if (self.name != nil && self.name.length > 0) {
        [xml appendFormat:@" name=\"%@\"", self.name];
    }
    if (self.showName) {
        [xml appendFormat:@" showName=\"%@\"", self.showName ? @"true" : @"false"];
    }
    if (self.email != nil && self.email.length > 0) {
        [xml appendFormat:@" email=\"%@\"", self.email];
    }
    if (self.website != nil && self.website.length > 0) {
        [xml appendFormat:@" website=\"%@\"", self.website];
    }
    if (self.over18) {
        [xml appendString:@" over18=\"true\""];
    }
    
    [xml appendString: @">"];
    if (self.bio != nil) {
        [xml appendString: @"<bio>"];
        [xml appendString: [self escapeHTML: self.bio]];
        [xml appendString: @"</bio>"];
    }
    [xml appendString: @"</user>"];
    return xml;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.user = [attributeDict objectForKey: @"user"];
        self.name = [attributeDict objectForKey: @"name"];
        self.token = [attributeDict objectForKey: @"token"];
        self.email = [attributeDict objectForKey: @"email"];
        self.hint = [attributeDict objectForKey: @"hint"];
        self.website = [attributeDict objectForKey: @"website"];
        self.connects = [attributeDict objectForKey: @"connects"];
        self.bots = [attributeDict objectForKey: @"bots"];
        self.messages = [attributeDict objectForKey: @"messages"];
        self.joined = [attributeDict objectForKey: @"joined"];
        self.lastConnect = [attributeDict objectForKey: @"lastConnect"];
        self.showName = [[attributeDict objectForKey: @"showName"] boolValue];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    if ([self.parseElement isEqualToString: @"bio" ]) {
        if (self.bio == nil) {
            self.bio = [NSMutableString string];
        }
        [self.bio appendString: string];
    } else if ([self.parseElement isEqualToString: @"avatar" ]) {
        if (self.avatar == nil) {
            self.avatar = [NSMutableString string];
        }
        [self.avatar appendString: string];
    }

}

- (LibreUser*) copyWithZone: (NSZone*)zone {
    LibreUser* user = [LibreUser alloc];
    user.user = self.user;
    user.token = self.token;
    user.password = self.password;
    user.password2 = self.password2;
    user.name = self.name;
    user.showName = self.showName;
    user.email = self.email;
    user.website = self.website;
    user.avatar = self.avatar;
    user.over18 = self.over18;
    return user;
}

@end
