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
#import "LibreAction.h"

@protocol LibrePickerDelegate

- (void) choice: (NSString*) choice source: (UIView*) source;

@end

@interface LibreViewController : UIViewController <UIWebViewDelegate, LibreAction, LibrePickerDelegate>

@property LibreSDKConnection* sdk;
@property LibreViewController* parent;
@property NSMutableArray* categories;
@property NSMutableArray* tags;

- (void)registerForKeyboardNotifications;

- (void) reset;

- (void) showBusy;

- (void) stopBusy;

- (void)zoomImage: (UIImage*) image title: (NSString*)title;

- (void)choose: (NSArray*) choices default: (NSString*)choice title: (NSString*)title source: (UIView*)source;

- (BOOL) webView:(UIWebView *)inWeb shouldStartLoadWithRequest:(NSURLRequest *)inRequest navigationType:(UIWebViewNavigationType)inType;

- (void) fetchTags;

- (void) fetchCategories;

- (void) updateTags: (NSMutableArray*) tags;

- (void) updateCategories: (NSMutableArray*) categories;

@end
