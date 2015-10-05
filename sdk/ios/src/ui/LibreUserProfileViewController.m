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

#import "LibreUserProfileViewController.h"
#import "LibreUI.h"
#import "LibreUser.h"
#import "LibreMainViewController.h"
#import "LibreEditProfileViewController.h"
#import "LibreUpdateUserIconAction.h"

@implementation LibreUserProfileViewController {
    UIBarButtonItem* propertiesMenu;
    UILabel* userLabel;
    UIWebView* webView;
    UIImageView* iconView;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (self.user == nil) {
        return;
    }
    self.navigationItem.title = self.user.user;
    
    propertiesMenu = [LibreUI newMenuButton: self selector: @selector(propertiesMenu)];
    self.navigationItem.rightBarButtonItem = propertiesMenu;
    
    UIView* group = [[UIView alloc] init];
    group.backgroundColor = [UIColor whiteColor];
    [group setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: group];
    [LibreUI constrainTop: group in: self.view];
    [LibreUI constrain: group height: 50 in: self.view];
    
    userLabel = [LibreUI newLabel: self.user.user in: group controller: self];
    userLabel.textColor = [UIColor blackColor];
    [LibreUI constrain: userLabel top: 2 in: group];
    [LibreUI constrain: userLabel left: 2 in: group];
    
    UIImage* userImage = [self.sdk fetchImage: self.sdk.user.avatar];
    if (userImage == nil) {
        userImage = [self.sdk defaultUserAvatar];
    }
    iconView = [LibreUI newImageView: userImage in: group controller: self];
    [LibreUI constrain: iconView top: 2 in: group];
    [LibreUI constrain: iconView right: -2 in: group];
    [LibreUI constrain: iconView height: 50 in: group];
    [LibreUI constrain: iconView width: 100 in: group];
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    tapGesture.numberOfTapsRequired = 1;
    tapGesture.cancelsTouchesInView = NO;
    iconView.userInteractionEnabled = YES;
    [iconView addGestureRecognizer:tapGesture];
    
    webView = [LibreUI newWebView: @"" in: self.view controller: self];
    [LibreUI constrain: webView top: 2 to: group in: self.view];
    [LibreUI constrain: webView bottom: -2 in: self.view];
    [LibreUI constrain: webView left: 2 in: self.view];
    [LibreUI constrain: webView right: -2 in: self.view];
    
    [self reset];
}

- (void) resetIcon {
    if (self.user == nil) {
        return;
    }
    
    UIImage* userImage = [self.sdk fetchImage: self.user.avatar];
    if (userImage == nil) {
        userImage = [self.sdk defaultUserAvatar];
    }
    iconView.image = userImage;
}

- (void) reset {
    if (self.user == nil) {
        return;
    }
    
    NSMutableString* html = [NSMutableString string];
    [html appendString: @"<style>td { font-size:14px; }</style><table>"];
    if (self.user.showName && self.user.name != nil) {
        [html appendString: @"<tr><td><b>Name</b><td>"];
        [html appendString: self.user.name];
        [html appendString: @"</span></td></tr>"];
    }
    if ([self.user.website length] > 0) {
        [html appendString: @"<tr><td><b>Website</b></td>"];
        [html appendFormat: @"<td><a href='%@'>%@</a></td></tr>", self.user.website, self.user.website];
    }
    
    [html appendString: @"<tr><td><b>Joined</b></td>"];
    [html appendFormat: @"<td>%@</td></tr>", self.user.joined];
    [html appendString: @"<tr><td><b>Connects</b></td>"];
    [html appendFormat: @"<td>%@</td></tr>", self.user.connects];
    [html appendString: @"<tr><td><b>Last Connect</b></td>"];
    [html appendFormat: @"<td>%@</td></tr>", self.user.lastConnect];
    if (self.user.bots > 0) {
        [html appendString: @"<tr><td><b>Bots</b></td>"];
        [html appendFormat: @"<td>%@</td></tr>", self.user.bots];
    }
    if (self.user.posts > 0) {
        [html appendString: @"<tr><td><b>Posts</b></td>"];
        [html appendFormat: @"<td>%@</td></tr>", self.user.posts];
    }
    if (self.user.messages > 0) {
        [html appendString: @"<tr><td><b>Messages</b></td>"];
        [html appendFormat: @"<td>%@</td></tr>", self.user.messages];
    }
    
    [html appendString: @"</table></p>"];
    if (self.user.bio != nil) {
        [html appendString: self.user.bio];
    }
    
    [webView loadHTMLString: html baseURL: nil];
}

- (void) propertiesMenu {
    UIActionSheet* menu;
    if (self.sdk.user == nil) {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                                           delegate: self
                                  cancelButtonTitle: @"Cancel"
                             destructiveButtonTitle: nil
                                  otherButtonTitles:@"Flag User", nil];
    } else {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                                           delegate: self
                                  cancelButtonTitle: @"Cancel"
                             destructiveButtonTitle: nil
                                  otherButtonTitles:@"Edit Profile", @"Change Icon", nil];
    }
    [menu showFromBarButtonItem: propertiesMenu animated: YES];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (self.sdk.user == nil) {
        switch (buttonIndex) {
            case 0:
                [self flagUser];
                break;
            default:
                break;
        }
    } else {
        switch (buttonIndex) {
            case 0:
                [self editUser];
                break;
            case 1:
                [self changeIcon];
                break;
            default:
                break;
        }
    }
}

-(void)handleTap:(UIGestureRecognizer *)sender {
    [self zoomImage: iconView.image title: self.user.user];
}

- (void)editUser {
    LibreEditProfileViewController *view = [[LibreEditProfileViewController alloc] init];
    view.parent = self;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) changeIcon {
    UIImagePickerController* picker= [[UIImagePickerController alloc]init];
    picker.delegate = self;
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    [self presentViewController:picker animated: YES completion:nil];
}

- (void) updateIconResponse: (LibreUser*) user {
    [self stopBusy];
    self.sdk.user = user;
    self.user = user;
    [self resetIcon];
}

- (void) flagUser {
    
}

- (void) imagePickerController: (UIImagePickerController*) picker
                didFinishPickingImage: (UIImage*) image
                editingInfo:(NSDictionary*) info
{
    [picker dismissViewControllerAnimated: YES completion:nil];
    
    LibreUpdateUserIconAction* action = [LibreUpdateUserIconAction alloc];
    action.controller = self;
    
    [self.sdk updateIcon: image user: self.user action: action];
}

- (void) imagePickerControllerDidCancel:(UIImagePickerController *) picker {
    [picker dismissViewControllerAnimated: YES completion:nil];
}

@end
