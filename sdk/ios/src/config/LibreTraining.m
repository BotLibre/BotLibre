/******************************************************************************
 *
 *  Copyright 2015 Paphus Solutions Inc.
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

#import "LibreTraining.h"

/**
 * This object teaches a bot a new response.
 * The content instance ID is inherited from LibreConfig.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
 */
@implementation LibreTraining

- (NSString*) toXML {
    NSMutableString* xml = [NSMutableString string];
    [xml appendString: @"<training"];
    [self writeCredentials: xml];
    if (self.operation != nil && self.operation.length > 0) {
        [xml appendFormat:@" operation=\"%@\"", self.operation];
    }
    if (self.requirePrevious) {
        [xml appendFormat:@" requirePrevious=\"true\""];
    }
    
    [xml appendString: @">"];
    
    if (self.question != nil) {
        [xml appendString: @"<question>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.question]];
        [xml appendString: @"</question>"];
    }
    if (self.response != nil) {
        [xml appendString: @"<response>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.response]];
        [xml appendString: @"</response>"];
    }
    if (self.topic != nil) {
        [xml appendString: @"<topic>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.topic]];
        [xml appendString: @"</topic>"];
    }
    if (self.keywords != nil) {
        [xml appendString: @"<keywords>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.keywords]];
        [xml appendString: @"</keywords>"];
    }
    if (self.required != nil) {
        [xml appendString: @"<required>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.required]];
        [xml appendString: @"</required>"];
    }
    if (self.previous != nil) {
        [xml appendString: @"<previous>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.previous]];
        [xml appendString: @"</previous>"];
    }
    if (self.emotions != nil) {
        [xml appendString: @"<emotions>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.emotions]];
        [xml appendString: @"</emotions>"];
    }
    if (self.actions != nil) {
        [xml appendString: @"<actions>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.actions]];
        [xml appendString: @"</actions>"];
    }
    if (self.poses != nil) {
        [xml appendString: @"<poses>"];
        [xml appendFormat: @"%@", [self escapeHTML: self.poses]];
        [xml appendString: @"</poses>"];
    }

    [xml appendString: @"</training>"];
    return xml;
}


@end
