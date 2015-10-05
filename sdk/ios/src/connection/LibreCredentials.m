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

#import "LibreCredentials.h"

/**
 * Credential used to establish a connection.
 * Requires the url, and an application id.
 * You can obtain your application id from your user details page on the hosting website.
 */
@implementation LibreCredentials

/**
 * Create a new SDK credentials for the server URL and the application id.
 */
+ (LibreCredentials*)url: (NSString*) url application: (NSString*) applicationId {
    LibreCredentials* credentials = [LibreCredentials alloc];
    credentials.url = url;
    credentials.applicationId = applicationId;
    return credentials;
}

/**
 * Credentials for use with hosted services on the BOT libre website, a free embeddable bot hosting service.
 * http://www.botlibre.com
 */
+ (LibreCredentials*)botLibreApplication: (NSString*) applicationId {
    LibreCredentials* credentials = [LibreCredentials alloc];
    credentials.app = @"";
    credentials.host = @"www.botlibre.com";
    credentials.url = @"http://www.botlibre.com/rest/botlibre";
    credentials.applicationId = applicationId;
    return credentials;
}

/**
 * Credentials for use with hosted services on the Paphus Live Chat website, a commercial live chat, chat bot, and forums hosting service.
 * http://www.paphuslivechat.com
 */
+ (LibreCredentials*)paphusApplication: (NSString*) applicationId {
    LibreCredentials* credentials = [LibreCredentials alloc];
    credentials.app = @"";
    credentials.host = @"www.paphuslivechat.com";
    credentials.url = @"http://www.paphuslivechat.com/rest/livechat";
    credentials.applicationId = applicationId;
    return credentials;
}

/**
 * Credentials for use with hosted services on the FORUMS libre website, a free embeddable forum hosting service.
 * http://www.forumslibre.com
 */
+ (LibreCredentials*)forumsLibreApplication: (NSString*) applicationId {
    LibreCredentials* credentials = [LibreCredentials alloc];
    credentials.app = @"";
    credentials.host = @"www.forumslibre.com";
    credentials.url = @"http://www.forumslibre.com/rest/forumslibre";
    credentials.applicationId = applicationId;
    return credentials;
}

/**
 * Credentials for use with hosted services on the LIVE CHAT libre website, a free embeddable live chat hosting service.
 * http://www.livechatlibre.com
 */
+ (LibreCredentials*)liveChatLibreApplication: (NSString*) applicationId {
    LibreCredentials* credentials = [LibreCredentials alloc];
    credentials.app = @"";
    credentials.host = @"www.livechatlibre.com";
    credentials.url = @"http://www.livechatlibre.com/rest/livechatlibre";
    credentials.applicationId = applicationId;
    return credentials;
}

@end
