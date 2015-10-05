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

#import "LibreSignUpViewController.h"
#import "LibreUI.h"
#import "LibreUser.h"
#import "LibreMainViewController.h"

@implementation LibreSignUpViewController {
    UITextField* userText;
    UITextField* passwordText;
    UITextField* hintText;
    UITextField* emailText;
    UITextField* nameText;
    UISwitch* showNameSwitch;
    UITextField* websiteText;
    UITextView* bioText;
    UILabel* bioLabel;
    NSLayoutConstraint* createBottom;
    NSLayoutConstraint* labelTop;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    userText = [LibreUI newText: @"User ID (required)" in: self.view controller: self];
    userText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    [LibreUI constrainTop: userText in: self.view];
    
    passwordText = [LibreUI newText: @"Password (required)" in: self.view controller: self anchor: userText];
    passwordText.secureTextEntry = YES;
    
    hintText = [LibreUI newText: @"Password Hint (optional)" in: self.view controller: self anchor: passwordText];
    hintText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    
    nameText = [LibreUI newText: @"Full Name (optional)" in: self.view controller: self anchor: hintText];
    
    UILabel* showNameLabel = [LibreUI newLabel: @"Show Name" in: self.view controller: self];
    [LibreUI constrain: showNameLabel top: 8 to: nameText in: self.view];
    [LibreUI constrain: showNameLabel left: 2 in: self.view];
    showNameSwitch = [LibreUI newSwitch: YES in: self.view controller: self];
    [LibreUI constrain: showNameSwitch top: 2 to: nameText in: self.view];
    [LibreUI constrain: showNameSwitch left: 12 to: showNameLabel in: self.view];
    
    emailText = [LibreUI newText: @"Email (recommended)" in: self.view controller: self anchor: showNameSwitch];
    emailText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    
    websiteText = [LibreUI newText: @"Website (optional)" in: self.view controller: self anchor: emailText];
    websiteText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    
    UIView* group = [[UIView alloc] init];
    group.backgroundColor = [UIColor whiteColor];
    [group setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: group];
    labelTop = [LibreUI constrain: group top: 2 to: websiteText in: self.view];
    [LibreUI constrain: group left: 2 in: self.view];
    [LibreUI constrain: group right: -2 in: self.view];
    
    bioLabel = [LibreUI newLabel: @"Bio (optional)" in: group controller: self];
    [LibreUI constrain: bioLabel top: 2 in: group];
    [LibreUI constrain: bioLabel left: 2 in: group];
    [LibreUI constrain: bioLabel right: -2 in: group];
    bioText = [LibreUI newTextViewIn: group controller: self];
    [LibreUI constrain: bioText top: 2 to: bioLabel in: group];
    [LibreUI constrain: bioText left: 2 in: group];
    [LibreUI constrain: bioText right: -2 in: group];

    UIButton* signUp = [LibreUI newButton: @"Sign Up" in: self.view];
    [LibreUI okButton: signUp];
    createBottom = [LibreUI constrain: signUp bottom: -2 in: self.view];
    [LibreUI constrain: signUp left: 2 in: self.view];
    [LibreUI constrain: signUp right: -2 in: self.view];
    [signUp addTarget: self action: @selector(signUp) forControlEvents: UIControlEventTouchUpInside];
    
    [LibreUI constrain: group bottom: -2 to: signUp in: self.view];
    [LibreUI constrain: bioText bottom: -2 in: group];
    
    [self registerForKeyboardNotifications];
    
    self.navigationItem.title = @"Sign Up";
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    [bioText resignFirstResponder];
}

- (void)keyboardWillShow:(NSNotification*)notification
{
    if (![bioText isFirstResponder]) {
        return;
    }
    NSDictionary *info = [notification userInfo];
    NSValue *kbFrame = [info objectForKey: UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardFrame = [kbFrame CGRectValue];
    CGFloat height = [LibreUI isPortrait] ? keyboardFrame.size.height : keyboardFrame.size.width;
    labelTop.constant = -160;
    createBottom.constant = -height;
    [self.view layoutIfNeeded];
}

- (void)keyboardWillBeHidden:(NSNotification*)notification
{
    labelTop.constant = 2;
    createBottom.constant = -2;
    [self.view layoutIfNeeded];
}

- (BOOL)textFieldShouldReturn: (UITextField*)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)signUp {
    [self showBusy];
    
    LibreUser* user = [LibreUser alloc];
    user.user = userText.text;
    user.password = passwordText.text;
    user.hint = hintText.text;
    user.name = nameText.text;
    user.showName = showNameSwitch.on;
    user.email = emailText.text;
    user.website = websiteText.text;
    user.bio = [NSMutableString stringWithString: bioText.text];

    [self.sdk createUser: user action: self];
}

- (void) response: (LibreUser*) config {
    self.sdk.user = config;
    
    [self stopBusy];
    
    [self.parent reset];
    [self.navigationController popViewControllerAnimated:YES];
}

@end
