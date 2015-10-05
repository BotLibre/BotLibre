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

#import "LibreWebMedium.h"
#import "LibreSDKConnection.h"
#import "LibreCredentials.h"
#import "LibreUI.h"

/**
 * Abstract content class.
 * This object models a content object such as a bot, forum, or channel.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a content.
 */
@implementation LibreWebMedium

- (NSString*) type {
    return @"";
}

- (LibreWebMedium*) copyWithZone: (NSZone*)zone {
    LibreWebMedium* instance = [[self class] alloc];
    instance.id = self.id;
    instance.name = self.name;
    instance.isAdmin = self.isAdmin;
    instance.isAdult = self.isAdult;
    instance.isPrivate = self.isPrivate;
    instance.isHidden = self.isHidden;
    instance.accessMode = self.accessMode;
    instance.isFlagged = self.isFlagged;
    instance.instanceDescription = self.instanceDescription;
    instance.details = self.details;
    instance.disclaimer = self.disclaimer;
    instance.tags = self.tags;
    instance.categories = self.categories;
    instance.flaggedReason = self.flaggedReason;
    instance.creator = self.creator;
    instance.creationDate = self.creationDate;
    instance.lastConnectedUser = self.lastConnectedUser;
    instance.license = self.license;
    instance.website = self.website;
    instance.avatar = self.avatar;
    instance.connects = self.connects;
    instance.dailyConnects = self.dailyConnects;
    instance.weeklyConnects = self.weeklyConnects;
    instance.monthlyConnects = self.monthlyConnects;
    return instance;
}

- (LibreWebMedium*) credentials {
    LibreWebMedium* config = [[self class] alloc];
    config.id = self.id;
    return config;
}

- (NSString*) stats {
    return @"";
}

- (BOOL)isEqual:(id)other {
    if ([other isKindOfClass: [LibreWebMedium class]]) {
        if (self.id == nil) {
            return [super isEqual: other];
        }
        return [self.id isEqualToString: ((LibreWebMedium*)other).id];
    }
    return false;
}

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<"];
    [xml appendString: [self type]];
    [self writeCredentials: xml];
    [self writeXMLAttributes: xml];
    [xml appendFormat: @">"];
    [self writeXMLElements: xml];
    [xml appendString: @"</"];
    [xml appendString: [self type]];
    [xml appendString: @">"];
    return xml;
}

- (void) writeXMLAttributes: (NSMutableString*)xml {
    if (self.id != nil) {
        [xml appendFormat: @" id=\"%@\"", self.id];
    }
    if (self.name != nil) {
        [xml appendFormat: @" name=\"%@\"", self.name];
    }
    if (self.isPrivate) {
        [xml appendString: @" isPrivate=\"true\""];
    }
    if (self.isHidden) {
        [xml appendString: @" isHidden=\"true\""];
    }
    if (self.accessMode != nil && self.accessMode.length > 0) {
        [xml appendFormat: @" accessMode=\"%@\"", self.accessMode];
    }
    if (self.isAdult) {
        [xml appendString: @" isAdult=\"true\""];
    }
    if (self.isFlagged) {
        [xml appendString: @" isFlagged=\"true\""];
    }
}

- (void) writeXMLElements: (NSMutableString*)xml {
    if (self.instanceDescription != nil) {
        [xml appendString: @"<description>"];
        [xml appendString: [self escapeHTML: self.instanceDescription]];
        [xml appendString: @"</description>"];
    }
    if (self.details != nil) {
        [xml appendFormat: @"<details>"];
        [xml appendString: [self escapeHTML: self.details]];
        [xml appendFormat: @"</details>"];
    }
    if (self.disclaimer != nil) {
        [xml appendFormat: @"<disclaimer>"];
        [xml appendString: [self escapeHTML: self.disclaimer]];
        [xml appendFormat: @"</disclaimer>"];
    }
    if (self.categories != nil) {
        [xml appendFormat: @"<categories>"];
        [xml appendString: self.categories];
        [xml appendFormat: @"</categories>"];
    }
    if (self.tags != nil) {
        [xml appendFormat: @"<tags>"];
        [xml appendString: self.tags];
        [xml appendFormat: @"</tags>"];
    }
    if (self.license != nil) {
        [xml appendFormat: @"<license>"];
        [xml appendString: self.license];
        [xml appendFormat: @"</license>"];
    }
    if (self.website != nil) {
        [xml appendFormat: @"<website>"];
        [xml appendString: self.website];
        [xml appendFormat: @"</website>"];
    }
    if (self.flaggedReason != nil) {
        [xml appendFormat: @"<flaggedReason>"];
        [xml appendString: self.flaggedReason];
        [xml appendFormat: @"</flaggedReason>"];
    }
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString*)qualifiedName attributes:(NSDictionary *)attributeDict {
    if (self.parseElement == nil) {
        [super parser: parser didStartElement: elementName namespaceURI: namespaceURI qualifiedName: qualifiedName attributes: attributeDict];
        self.id = [attributeDict objectForKey: @"id"];
        self.name = [attributeDict objectForKey: @"name"];
        self.creationDate = [attributeDict objectForKey: @"creationDate"];
        self.isPrivate = [[attributeDict objectForKey: @"isPrivate"] boolValue];
        self.isHidden = [[attributeDict objectForKey: @"isHidden"] boolValue];
        self.isExternal = [[attributeDict objectForKey: @"isExternal"] boolValue];
        self.accessMode = [attributeDict objectForKey: @"accessMode"];
        self.isAdmin = [[attributeDict objectForKey: @"isAdmin"] boolValue];
        self.isAdult = [[attributeDict objectForKey: @"isAdult"] boolValue];
        self.isFlagged = [[attributeDict objectForKey: @"isFlagged"] boolValue];
        self.creator = [attributeDict objectForKey: @"creator"];
        self.creationDate = [attributeDict objectForKey: @"creationDate"];
        self.connects = [attributeDict objectForKey: @"connects"];
        self.dailyConnects = [attributeDict objectForKey: @"dailyConnects"];
        self.weeklyConnects = [attributeDict objectForKey: @"weeklyConnects"];
        self.monthlyConnects = [attributeDict objectForKey: @"monthlyConnects"];
    } else {
        self.parseElement = [[NSString alloc] initWithString: elementName];
    }
}

- (void) parser:(NSXMLParser*)parser foundCharacters:(NSString*)string {
    if ([self.parseElement isEqualToString: @"description"]) {
        if (self.instanceDescription == nil) {
            self.instanceDescription = [NSMutableString string];
        }
        [self.instanceDescription appendString: string];
    } else if ([self.parseElement isEqualToString: @"details"]) {
        if (self.details == nil) {
            self.details = [NSMutableString string];
        }
        [self.details appendString: string];
    } else if ([self.parseElement isEqualToString: @"disclaimer"]) {
        if (self.disclaimer == nil) {
            self.disclaimer = [NSMutableString string];
        }
        [self.disclaimer appendString: string];
    } else if ([self.parseElement isEqualToString: @"categories"]) {
        if (self.categories == nil) {
            self.categories = [NSMutableString string];
        }
        [self.categories appendString: string];
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
    } else if ([self.parseElement isEqualToString: @"lastConnectedUser"]) {
        if (self.lastConnectedUser == nil) {
            self.lastConnectedUser = [NSMutableString string];
        }
        [self.lastConnectedUser appendString: string];
    } else if ([self.parseElement isEqualToString: @"license"]) {
        if (self.license == nil) {
            self.license = [NSMutableString string];
        }
        [self.license appendString: string];
    } else if ([self.parseElement isEqualToString: @"website"]) {
        if (self.website == nil) {
            self.website = [NSMutableString string];
        }
        [self.website appendString: string];
    } else if ([self.parseElement isEqualToString: @"avatar"]) {
        if (self.avatar == nil) {
            self.avatar = [NSMutableString string];
        }
        [self.avatar appendString: string];
    }
}

@end
