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

#import "LibreSDKConnection.h"
#import "LibreCredentials.h"
#import "LibreChatMessage.h"
#import "LibreChatResponse.h"
#import "LibreAction.h"
#import "LibreWebMedium.h"
#import "LibreBrowse.h"
#import "LibreBot.h"
#import "LibreDomain.h"
#import "LibreUser.h"
#import "LibreUI.h"
#import "LibreContent.h"
#import "LibreSDKCall.h"

@implementation LibreSDKConnection

static NSArray* types;
static NSArray* channelTypes;
static NSArray* accessModes;
static NSArray* learningModes;
static NSArray* correctionModes;
static NSArray* botModes;
static NSArray* licenses;

static LibreSDKConnection* sdk;
static LibreCredentials* credentials;

+ (LibreCredentials*) credentials {
    if (credentials == nil) {
        // ** You must enter your Bot Libre or Paphus app ID here.
        credentials = [LibreCredentials botLibreApplication: @"your-app-id"];
    }
    return credentials;
}

+ (LibreSDKConnection*) sdk {
    if (sdk == nil) {
        sdk = [LibreSDKConnection credentials: [LibreSDKConnection credentials]];
        sdk.debug = true;
    }
    return sdk;
}

+ (void)initialize {
    types = @[@"Bots", @"Forums", @"Live Chat", @"Domains", @"Avatars"];
    channelTypes = @[@"ChatRoom", @"OneOnOne"];
    accessModes = @[@"Everyone", @"Users", @"Members", @"Administrators"];
    learningModes = @[@"Disabled", @"Administrators", @"Users", @"Everyone"];
    correctionModes = @[@"Disabled", @"Administrators", @"Users", @"Everyone"];
    botModes = @[@"ListenOnly", @"AnswerOnly", @"AnswerAndListen"];
    licenses = @[@"Copyright, all rights reserved",
                    @"Public Domain",
                    @"Creative Commons Attribution 3.0 Unported License",
                    @"GNU General Public License 3.0",
                    @"Apache License, Version 2.0",
                    @"Eclipse Public License 1.0"];}

+ (NSArray*) types {
    return types;
}

+ (NSArray*) channelTypes {
    return channelTypes;
}

+ (NSArray*) accessModes {
    return accessModes;
}

+ (NSArray*) learningModes {
    return learningModes;
}

+ (NSArray*) correctionModes {
    return correctionModes;
}

+ (NSArray*) botModes {
    return botModes;
}

+ (NSArray*) licenses {
    return licenses;
}

/**
 * Create an SDK connection with the credentials.
 * Use the Credentials subclass specific to your server.
 */
+ (LibreSDKConnection*) credentials: (LibreCredentials*) credentials {
    LibreSDKConnection* connection = [LibreSDKConnection alloc];
    connection.credentials = credentials;
    return connection;
}

/**
 * Validate the user credentials (password, or token).
 * The user details are returned (with a connection token, password removed).
 * The user credentials are soted in the connection, and used on subsequent calls.
 * An exception is thrown if the connect failed.
 */
- (void) connect: (LibreUser*) config action: (id <LibreAction>) action {
    [self fetchUser: config action: action];
}

/**
 * Disconnect from the connection.
 * An LibreSDKConnection does not keep a live connection, but this resets its connected user and domain.
 */
- (void) disconnect {
    self.user = nil;
    self.domain = nil;
}

/**
 * Fetch the user details for the user credentials.
 * A token or password is required to validate the user.
 */
-(void) fetchUser: (LibreUser*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [LibreUser alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/check-user", self.credentials.url] data: [config toXML]];
}

/**
 * Fetch the content details from the server.
 * The id or name and domain of the object must be set.
 */
- (void) fetch: (LibreWebMedium*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [[config class] alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/check-%@", self.credentials.url, [config type]] data: [config toXML]];
}

/**
 * Flag the content as offensive.
 */
- (void) flag: (LibreWebMedium*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/flag-%@", self.credentials.url, [config type]] data: [config toXML]];
}

/**
 * Delete the content.
 */
- (void) delete: (LibreWebMedium*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/delete-%@", self.credentials.url, [config type]] data: [config toXML]];
}

/**
 * Return the default user image icon.
 */
- (UIImage*) defaultUserAvatar {
    return [self fetchImage: @"/images/user-thumb.jpg"];
}

/**
 * Return the default bot image icon.
 */
- (UIImage*) defaultBotAvatar {
    return [self fetchImage: @"/images/bot-thumb.jpg"];
}

/**
 * Fetch the image from the server and return as a cached UIImage.
 */
- (UIImage*) fetchImage: (NSString*)imageName {
    return [LibreUI fetchImage: [self getURL: imageName]];
}

/**
 * Return the full url for the server file.
 */
- (NSString*) getURL: (NSString*)imageName {
    if (imageName == nil) {
        return nil;
    }
    return [NSString stringWithFormat:@"http://%@/%@", self.credentials.host, imageName];
}

/**
 * Fetch the list of categories for the type, and domain.
 */
- (void) fetchCategories: (LibreContent*) config action: (id <LibreAction>) action {
    if (self.categories != nil) {
        LibreBrowse* config = [LibreBrowse alloc];
        config.instances = self.categories;
        [action response: config];
        return;
    }
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = config;
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@%@", self.credentials.url, @"/get-categories"] data: [config toXML]];
}

/**
 * Fetch the list of tags for the type, and domain.
 */
- (void) fetchTags: (LibreContent*) config action: (id <LibreAction>) action {
    if (self.tags != nil) {
        LibreBrowse* config = [LibreBrowse alloc];
        config.instances = self.tags;
        [action response: config];
        return;
    }
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = config;
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@%@", self.credentials.url, @"/get-tags"] data: [config toXML]];
}

/**
 * Fetch the list of bot templates.
 */
- (void) fetchTemplates: (id <LibreAction>) action {
    if (self.templates != nil) {
        LibreBrowse* config = [LibreBrowse alloc];
        config.instances = self.templates;
        [action response: config];
        return;
    }
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [LibreBrowse alloc];
    call.currentAction = action;
    [call get: [NSString stringWithFormat:@"%@%@", self.credentials.url, @"/get-all-templates"]];
}

/**
 * Process the bot chat message and return the bot's response.
 * The LibreChat should contain the conversation id if part of a conversation.
 * If a new conversation the conversation id is returned in the response.
 */
- (void) chat: (LibreChatMessage*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [LibreChatResponse alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@%@", self.credentials.url, @"/post-chat"] data: [config toXML]];
}

/**
 * Return the list of content for the browse criteria.
 * The type defines the content type (one of Bot, Forum, Channel, Domain).
 */
- (void) browse: (LibreBrowse*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = config;
    call.currentAction = action;
    NSString* type = @"";
    if ([[config type] isEqualToString: @"Forum"]) {
        type = @"/get-forums";
    } else if ([[config type] isEqualToString: @"Channel"]) {
        type = @"/get-channels";
    } else if ([[config type] isEqualToString: @"Domain"]) {
        type = @"/get-domains";
    } else {
        type = @"/get-instances";
    }
    [call post: [NSString stringWithFormat:@"%@%@", self.credentials.url, type] data: [config toXML]];
}

/**
 * Create a new user.
 */
- (void) createUser: (LibreUser*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [LibreUser alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/create-user", self.credentials.url] data: [config toXML]];
}

/**
 * Update an existing user.
 */
- (void) updateUser: (LibreUser*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [LibreUser alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/update-user", self.credentials.url] data: [config toXML]];
}

/**
 * Update the user's icon.
 * The file will be uploaded to the server.
 */
- (void) updateIcon: (UIImage*) image user: (LibreUser*) config action: (id <LibreAction>) action {
    if (image.size.width > 300 || image.size.height > 300) {
        image = [LibreUI resizeImage: image in: CGSizeMake(300, 300)];
    }
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [LibreUser alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/update-user-icon", self.credentials.url] image: image data: [config toXML]];
}

/**
 * Update the instance's icon.
 * The file will be uploaded to the server.
 */
- (void) updateIcon: (UIImage*) image instance: (LibreWebMedium*) config action: (id <LibreAction>) action {
    if (image.size.width > 300 || image.size.height > 300) {
        image = [LibreUI resizeImage: image in: CGSizeMake(300, 300)];
    }
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [[config class] alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/update-%@-icon", self.credentials.url, [config type]] image: image data: [config toXML]];
}

/**
 * Create a new content.
 * The content will be returned to the action with its new id.
 */
- (void) create: (LibreWebMedium*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [[config class] alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/create-%@", self.credentials.url, [config type]] data: [config toXML]];
}

/**
 * Update content.
 */
- (void) update: (LibreWebMedium*) config action: (id <LibreAction>) action {
    [config addCredentials: self];
    LibreSDKCall* call = [LibreSDKCall alloc];
    call.debug = self.debug;
    call.responseConfig = [[config class] alloc];
    call.currentAction = action;
    [call post: [NSString stringWithFormat:@"%@/update-%@", self.credentials.url, [config type]] data: [config toXML]];
}

@end
