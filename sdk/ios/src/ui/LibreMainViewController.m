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

#import "LibreMainViewController.h"
#import "LibreBrowseViewController.h"
#import "LibreCredentials.h"
#import "LibreSDKConnection.h"
#import "LibreChatMessage.h"
#import "LibreChatResponse.h"
#import "LibreAction.h"
#import "LibreUI.h"
#import "LibreBrowse.h"
#import "LibreBrowseAction.h"
#import "LibreCreateBotViewController.h"
#import "LibreSignInViewController.h"
#import "LibreSignUpViewController.h"
#import "LibreUser.h"
#import "LibreUserProfileViewController.h"
#import "LibreEditProfileViewController.h"
#import "LibreConnectAction.h"
#import "LibreSearchBotViewController.h"

@implementation LibreMainViewController {
    UIBarButtonItem* propertiesMenu;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    propertiesMenu = [LibreUI newMenuButton: self selector: @selector(propertiesMenu)];
    [self reset];
    self.navigationItem.title = @"Project libre!";
    
    UIImage* image = [UIImage imageNamed:@"projectlibre.png"];
    UIImageView* splash = [[UIImageView alloc] initWithImage: image];
    splash.contentMode = UIViewContentModeScaleAspectFit;
    splash.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);
    [splash setTranslatesAutoresizingMaskIntoConstraints:NO];
    [self.view addSubview: splash];
    [LibreUI constrain: splash top: 62 in: self.view];
    [LibreUI constrain: splash left: 2 in: self.view];
    [LibreUI constrain: splash right: -2 in: self.view];
    
    UIButton* create = [LibreUI newButton: @"Create" in: self.view];
    [LibreUI constrain: create bottom: -2 in: self.view];
    [LibreUI constrain: create left: 2 in: self.view];
    [LibreUI constrain: create right: -2 in: self.view];
    [LibreUI constrain: create height: 30 in: self.view];
    [create addTarget: self action: @selector(openCreate) forControlEvents: UIControlEventTouchUpInside];
    
    UIButton* browse = [LibreUI newButton: @"Browse" in: self.view];
    [LibreUI constrain: browse left: 2 in: self.view];
    [LibreUI constrain: browse right: -2 in: self.view];
    [LibreUI constrain: browse bottom: -2 to: create in: self.view];
    [LibreUI constrain: browse height: 30 in: self.view];
    
    NSLayoutConstraint* constraint = [NSLayoutConstraint
                                     constraintWithItem: splash
                                     attribute:NSLayoutAttributeBottom
                                     relatedBy:NSLayoutRelationEqual
                                     toItem:browse
                                     attribute:NSLayoutAttributeTop
                                     multiplier:1
                                     constant:-2];
    [self.view addConstraint: constraint];
    
    [browse addTarget: self action: @selector(openBrowse) forControlEvents: UIControlEventTouchUpInside];
    
    NSString* userid = [[NSUserDefaults standardUserDefaults] stringForKey: @"user"];
    NSString* token = [[NSUserDefaults standardUserDefaults] stringForKey:@"token"];
    
    if (userid != nil && userid.length > 0 && token != nil && token.length > 0) {
        LibreUser* user = [LibreUser alloc];
        user.user = userid;
        user.token = token;
        LibreConnectAction* action = [LibreConnectAction alloc];
        action.controller = self;
        [self.sdk connect: user action: action];
    }
}

- (void) propertiesMenu {
    UIActionSheet* menu;
    if (self.sdk.user == nil) {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                    delegate: self
                    cancelButtonTitle: @"Cancel"
                    destructiveButtonTitle: nil
                    otherButtonTitles:@"Sign In", @"Sign Up", @"Search", @"Website", nil];
    } else {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                    delegate: self
                    cancelButtonTitle: @"Cancel"
                    destructiveButtonTitle: nil
                    otherButtonTitles: @"My Bots", @"Search", @"Sign Out", @"View Profile", @"Edit Profile", @"Website", nil];
    }
    [menu showFromBarButtonItem: propertiesMenu animated: YES];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (self.sdk.user == nil) {
        switch (buttonIndex) {
            case 0:
                [self signIn];
                break;
            case 1:
                [self signUp];
                break;
            case 2:
                [self search];
                break;
            case 3:
                [self website];
                break;
            default:
                break;
        }
    } else {
        switch (buttonIndex) {
            case 0:
                [self myBots];
                break;
            case 1:
                [self search];
                break;
            case 2:
                [self signOut];
                break;
            case 3:
                [self viewUser];
                break;
            case 4:
                [self editUser];
                break;
            case 5:
                [self website];
                break;
                
            default:
                break;
        }
    }
}

- (void) reset {
    if (self.sdk.user == nil) {
        UIBarButtonItem* signIn = [LibreUI newToolBarButton: @"login" highlight: @"login2" in: self selector: @selector(signIn)];
        self.navigationItem.rightBarButtonItems = [[NSArray alloc] initWithObjects: propertiesMenu, signIn, nil];
    } else {
        UIBarButtonItem* signOut = [LibreUI newToolBarButton: @"logout" highlight: @"logout2" in: self selector: @selector(signOut)];
        
        UIImage* userImage = [self.sdk fetchImage: self.sdk.user.avatar];
        if (userImage == nil) {
            userImage = [self.sdk defaultUserAvatar];
        }
        if (userImage == nil) {
            self.navigationItem.rightBarButtonItems = [[NSArray alloc] initWithObjects: propertiesMenu, signOut, nil];
            return;
        }
        userImage = [LibreUI resizeImage: userImage in: CGSizeMake(20, 20)];
        UIBarButtonItem* viewUser = [LibreUI newToolBarImageButton: userImage highlight: userImage in: self selector: @selector(viewUser)];
        self.navigationItem.rightBarButtonItems = [[NSArray alloc] initWithObjects: propertiesMenu, signOut, viewUser, nil];
    }

}

- (void)website {
     [[UIApplication sharedApplication] openURL: [NSURL URLWithString:@"https://www.botlibre.com"]];
}

- (void)signIn {
    LibreSignInViewController *view = [[LibreSignInViewController alloc] init];
    view.parent = self;
    [self.navigationController pushViewController: view animated: YES];
}

- (void)signUp {
    LibreSignUpViewController *view = [[LibreSignUpViewController alloc] init];
    view.parent = self;
    [self.navigationController pushViewController: view animated: YES];
}

- (void)viewUser {
    LibreUserProfileViewController *view = [[LibreUserProfileViewController alloc] init];
    view.user = self.sdk.user;
    [self.navigationController pushViewController: view animated: YES];
}

- (void)editUser {
    LibreEditProfileViewController *view = [[LibreEditProfileViewController alloc] init];
    [self.navigationController pushViewController: view animated: YES];
}

- (void)signOut {
    [self.sdk disconnect];
    [self reset];
    
    [[NSUserDefaults standardUserDefaults] removeObjectForKey: @"user"];
    [[NSUserDefaults standardUserDefaults] removeObjectForKey: @"token"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void) search {
    LibreSearchBotViewController* view = [[LibreSearchBotViewController alloc] init];
    view.parent = self;
    [self.navigationController pushViewController: view animated: YES];
}
    
- (void)openCreate {
    if (self.sdk.user == nil) {
        [self error: @"You must sign in first"];
        return;
    }
    LibreCreateBotViewController *view = [[LibreCreateBotViewController alloc] init];
    [self.navigationController pushViewController: view animated: YES];
}

- (void)openBrowse {
    [self showBusy];
    LibreBrowse* browse = [LibreBrowse alloc];
    browse.type = @"Bot";
    LibreBrowseAction* action = [LibreBrowseAction alloc];
    action.controller = self;
    [self.sdk browse: browse action: action];
}

- (void) myBots {
    [self showBusy];
    LibreBrowse* browse = [LibreBrowse alloc];
    browse.type = @"Bot";
    browse.typeFilter = @"Personal";
    LibreBrowseAction* action = [LibreBrowseAction alloc];
    action.controller = self;
    [self.sdk browse: browse action: action];
}

- (void) connectResponse: (LibreUser*) response {
    [self reset];
}

@end
