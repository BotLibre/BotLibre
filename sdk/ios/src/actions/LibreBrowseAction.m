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
#import "LibreBrowseAction.h"
#import "LibreBrowseViewController.h"

@implementation LibreBrowseAction

- (void) response: (LibreBrowse*) config {
    [((LibreViewController*)self.controller) stopBusy];
    if ([self.controller isKindOfClass: [LibreBrowseViewController class]]) {
        LibreBrowseViewController* view = (LibreBrowseViewController*)self.controller;
        view.parent = (LibreViewController*)self.controller;
        view.instances = config.instances;
        [view reset];
    } else {
        LibreBrowseViewController* view = [[LibreBrowseViewController alloc] init];
        view.parent = (LibreViewController*)self.controller;
        view.instances = config.instances;
        [self.controller.navigationController pushViewController: view animated: YES];
    }
}

- (void) error: (NSString*) error {
    [super error: error];
    [((LibreViewController*)self.controller) stopBusy];
}

@end