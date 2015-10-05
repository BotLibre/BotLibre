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

#import <Foundation/Foundation.h>
#import "LibreAction.h"
#import "LibreBrowse.h"
#import "LibreTemplatesAction.h"
#import "LibreCreateBotViewController.h"

@implementation LibreTemplatesAction

- (void) response: (LibreBrowse*) config {
    ((LibreCreateBotViewController*)self.controller).sdk.templates = config.instances;
    [((LibreCreateBotViewController*)self.controller) stopBusy];
    [((LibreCreateBotViewController*)self.controller) updateTemplates: config.instances];
}

- (void) error: (NSString*) error {
    [super error: error];
    [((LibreCreateBotViewController*)self.controller) stopBusy];
}

@end