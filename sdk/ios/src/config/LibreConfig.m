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

#import "LibreConfig.h"
#import "LibreSDKConnection.h"
#import "LibreAction.h"
#import "LibreCredentials.h"
#import "LibreDomain.h"
#import "LibreUser.h"

/**
 * Abstract root class for all web API message objects.
 * Defines the required application id, and common fields.
 */
@implementation LibreConfig

- (void) addCredentials: (LibreSDKConnection*) connection {
    self.application = connection.credentials.applicationId;
    if (connection.user != nil) {
        self.user = connection.user.user;
        self.token = connection.user.token;
    }
    if (connection.domain != nil) {
        self.domain = connection.domain.id;
    }
}

- (void) parseXML: (NSData*) data {
    NSXMLParser* xmlParser = [[NSXMLParser alloc] initWithData: data];
    [xmlParser setDelegate: self];
    [xmlParser parse];
}

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<config "];
    [self writeCredentials: xml];
    [xml appendString: @"></config>"];
    return xml;
}
	
- (void) writeCredentials: (NSMutableString*) writer {
    if (self.user != nil && self.user.length > 0) {
        [writer appendFormat:@" user=\"%@\"", self.user];
    }
    if (self.token != nil && self.token.length > 0) {
        [writer appendFormat:@" token=\"%@\"", self.token];
    }
    if (self.type != nil && self.type.length > 0) {
        [writer appendFormat:@" type=\"%@\"", self.type];
    }
    if (self.instance != nil && self.instance.length > 0) {
        [writer appendFormat:@" instance=\"%@\"", self.instance];
    }
    if (self.application != nil && self.application.length > 0) {
        [writer appendFormat:@" application=\"%@\"", self.application];
    }
    if (self.domain != nil && !self.domain.length > 0) {
        [writer appendFormat:@" domain=\"%@\"", self.domain];
    }
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {

    self.parseElement = [[NSString alloc] initWithString: elementName];
    
    self.user = [attributeDict objectForKey: @"user"];
    self.token = [attributeDict objectForKey: @"token"];
    self.type = [attributeDict objectForKey: @"type"];
    self.instance = [attributeDict objectForKey: @"instance"];
    self.application = [attributeDict objectForKey: @"application"];
    self.domain = [attributeDict objectForKey: @"domain"];
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string { }

- (void) parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
}

- (void) parserDidEndDocument:(NSXMLParser *)parser {
    if (self.responseAction != nil) {
        [self.responseAction response: self];
    }
}

- (NSString*) escapeHTML: (NSString*)html {
    html = [html stringByReplacingOccurrencesOfString: @"<" withString: @"&lt;"];
    html = [html stringByReplacingOccurrencesOfString: @">" withString: @"&gt;"];
    return html;
}


@end
