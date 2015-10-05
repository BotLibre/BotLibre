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

#import "LibreSignInViewController.h"
#import "LibreUI.h"
#import "LibreUser.h"
#import "LibreMainViewController.h"
#import "LibreSignUpViewController.h"

@implementation LibreSignInViewController {
    UITextField* userText;
    UITextField* passwordText;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    userText = [LibreUI newText: @"User ID (or email)" in: self.view  controller: self];
    userText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    [LibreUI constrainTop: userText in: self.view];
    
    passwordText = [LibreUI newText: @"Password" in: self.view controller: self anchor: userText];
    passwordText.secureTextEntry = YES;
    
    UIButton* signIn = [LibreUI newButton: @"Sign In" in: self.view anchor: passwordText];
    [LibreUI okButton: signIn];
    [signIn addTarget: self action: @selector(signIn) forControlEvents: UIControlEventTouchUpInside];
    
    UIButton* signUp = [LibreUI newButton: @"Sign Up" in: self.view];
    [LibreUI constrain: signUp bottom: -2 in: self.view];
    [LibreUI constrain: signUp left: 2 in: self.view];
    [LibreUI constrain: signUp right: -2 in: self.view];
    [signUp addTarget: self action: @selector(signUp) forControlEvents: UIControlEventTouchUpInside];
    
    self.navigationItem.title = @"Sign In";

}

- (BOOL)textFieldShouldReturn: (UITextField*)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)signIn {
    [self showBusy];
    
    LibreUser* user = [LibreUser alloc];
    user.user = userText.text;
    user.password = passwordText.text;
    [self.sdk connect: user action: self];
}

- (void)signUp {
    LibreSignUpViewController *view = [[LibreSignUpViewController alloc] init];
    view.parent = self.parent;
    
    NSMutableArray *viewControllers = [NSMutableArray arrayWithArray:[[self navigationController] viewControllers]];
    [viewControllers removeLastObject];
    [viewControllers addObject: view];
    [[self navigationController] setViewControllers: viewControllers animated: YES];
}

- (void) response: (LibreUser*) config {
    self.sdk.user = config;
    [[NSUserDefaults standardUserDefaults] setValue: config.user forKey: @"user"];
    [[NSUserDefaults standardUserDefaults] setValue: config.token forKey: @"token"];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    [self stopBusy];
    [self.parent reset];
    [self.navigationController popViewControllerAnimated:YES];
}

@end
