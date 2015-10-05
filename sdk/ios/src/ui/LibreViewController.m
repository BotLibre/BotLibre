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

#import "LibreViewController.h"
#import "LibreCredentials.h"
#import "LibreSDKConnection.h"
#import "LibreImageViewController.h"
#import "LibrePickerViewController.h"
#import "LibreTagsAction.h"
#import "LibreCategoriesAction.h"
#import "LibreContent.h"
#import "LibreUI.h"

@implementation LibreViewController

UIActivityIndicatorView* spinner;

- (id)init
{
    self = [super init];
    if (self) {
        self.sdk = [LibreSDKConnection sdk];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationController.navigationBar.backgroundColor = [UIColor grayColor];
    self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

- (void)registerForKeyboardNotifications {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillShow:)
                                                 name:UIKeyboardWillChangeFrameNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillBeHidden:)
                                                 name:UIKeyboardWillHideNotification object:nil];
}

- (void)keyboardWillShow:(NSNotification*)notification {
}

- (void)keyboardWillBeHidden:(NSNotification*)notification {
}


- (void) reset {
    
}

- (void)showBusy {
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleGray];
    int x = [LibreUI screenWidth] / 2;
    int y = [LibreUI screenHeight] / 2;
    if ([LibreUI isPortrait]) {
        spinner.center = CGPointMake(x, y);
    } else {
        spinner.center = CGPointMake(y, x);
    }
    spinner.hidesWhenStopped = YES;
    [self.view addSubview: spinner];
    [spinner startAnimating];
}

- (void) stopBusy {
    if (spinner == nil) {
        return;
    }
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    [spinner stopAnimating];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)zoomImage: (UIImage*) image title: (NSString*)title {
    LibreImageViewController *view = [[LibreImageViewController alloc] init];
    view.image = image;
    view.title = title;
    [self.navigationController pushViewController: view animated: YES];
}

- (void)choose: (NSArray*) choices default: (NSString*)choice title: (NSString*)title source: (UIView*)source {
    LibrePickerViewController *view = [[LibrePickerViewController alloc] init];
    view.choices = choices;
    view.choice = choice;
    view.title = title;
    view.delegate = (id <LibrePickerDelegate>)self;
    view.source = source;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) choice: (NSString*) choice source: (UIView*) source {
    if ([source isKindOfClass: [UILabel class]]) {
        ((UILabel*)source).text = choice;
    } else if ([source isKindOfClass: [UITextField class]]) {
        ((UITextField*)source).text = choice;
    }
    
}

-(BOOL) webView:(UIWebView *)inWeb shouldStartLoadWithRequest:(NSURLRequest *)inRequest navigationType:(UIWebViewNavigationType)inType {
    if ( inType == UIWebViewNavigationTypeLinkClicked ) {
        [[UIApplication sharedApplication] openURL:[inRequest URL]];
        return NO;
    }
    
    return YES;
}

- (void) updateTags: (NSMutableArray*) newTags {
    self.tags = [[NSMutableArray alloc] init];
    for (id tag in newTags) {
        [self.tags addObject: ((LibreContent*)tag).name];
    }
    
}

- (void) updateCategories: (NSMutableArray*) newCategories {
    self.categories = newCategories;
}

- (void) response: (LibreConfig*) config {
    [self stopBusy];
}

- (void) finished {
    [self stopBusy];
}

- (void) error: (NSString*) error {
    [self stopBusy];
    UIAlertView *alert = [[UIAlertView alloc]
                          initWithTitle: @"Error"
                          message: error
                          delegate: nil
                          cancelButtonTitle: @"OK"
                          otherButtonTitles: nil];
    [alert show];
}

- (void) fetchTags {
    LibreTagsAction* tagsAction = [LibreTagsAction alloc];
    tagsAction.controller = self;
    LibreContent* content = [LibreContent alloc];
    content.type = @"Bot";
    [self.sdk fetchTags: content action: tagsAction];
}

- (void) fetchCategories {
    LibreCategoriesAction* categoriesAction = [LibreCategoriesAction alloc];
    categoriesAction.controller = self;
    LibreContent* content = [LibreContent alloc];
    content.type = @"Bot";
    [self.sdk fetchCategories: content action: categoriesAction];
}

@end
