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

#import "LibreBotViewController.h"
#import "LibreUI.h"
#import "LibreBot.h"
#import "LibreMainViewController.h"
#import "LibreEditBotViewController.h"
#import "LibreUpdateInstanceIconAction.h"
#import "LibreChatViewController.h"
#import "LibreDeleteInstanceAction.h"

@implementation LibreBotViewController {
    UIBarButtonItem* propertiesMenu;
    UILabel* nameLabel;
    UIWebView* detailsWebView;
    UIWebView* webView;
    UIImageView* iconView;
    NSString* alert;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (self.instance == nil) {
        return;
    }
    self.navigationItem.title = self.instance.name;
    
    propertiesMenu = [LibreUI newMenuButton: self selector: @selector(propertiesMenu)];
    self.navigationItem.rightBarButtonItem = propertiesMenu;
    
    int size = MIN([LibreUI screenHeight], [LibreUI screenWidth]) / 2.5;
    
    UIView* group = [[UIView alloc] init];
    group.backgroundColor = [UIColor whiteColor];
    [group setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: group];
    [LibreUI constrainTop: group in: self.view];
    [LibreUI constrain: group height: size in: self.view];
    
    UIImage* image = [self.sdk fetchImage: self.instance.avatar];
    if (image == nil) {
        image = [self.sdk defaultBotAvatar];
    }
    iconView = [LibreUI newImageView: image in: group controller: self];
    [LibreUI constrain: iconView top: 2 in: group];
    [LibreUI constrain: iconView left: 2 in: group];
    [LibreUI constrain: iconView height: size in: group];
    [LibreUI constrain: iconView width: size in: group];
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    tapGesture.numberOfTapsRequired = 1;
    tapGesture.cancelsTouchesInView = NO;
    iconView.userInteractionEnabled = YES;
    [iconView addGestureRecognizer:tapGesture];
    
    detailsWebView = [LibreUI newWebView: @"" in: group controller: self];
    [LibreUI constrain: detailsWebView top: 2 in: group];
    [LibreUI constrain: detailsWebView right: -2 in: group];
    [LibreUI constrain: detailsWebView left: 2 to: iconView in: group];
    [LibreUI constrain: detailsWebView bottom: -2 in: group];
    
    webView = [LibreUI newWebView: @"" in: self.view controller: self];
    [LibreUI constrain: webView top: 2 to: group in: self.view];
    [LibreUI constrain: webView left: 2 in: self.view];
    [LibreUI constrain: webView right: -2 in: self.view];
    
    if (self.instance.isExternal) {
        [LibreUI constrain: webView bottom: -2 in: self.view];
    } else {
        UIButton* chat = [LibreUI newButton: @"Chat" in: self.view];
        [LibreUI okButton: chat];
        [LibreUI constrain: chat bottom: -2 in: self.view];
        [LibreUI constrain: chat left: 2 in: self.view];
        [LibreUI constrain: chat right: -2 in: self.view];
        [chat addTarget: self action: @selector(openChat) forControlEvents: UIControlEventTouchUpInside];
        
        [LibreUI constrain: webView bottom: -2 to: chat in: self.view];
    }
    
    [self reset];
}

- (void) resetIcon {
    if (self.instance == nil) {
        return;
    }
    
    UIImage* image = [self.sdk fetchImage: self.instance.avatar];
    if (image == nil || self.instance.isFlagged) {
        image = [self.sdk defaultBotAvatar];
    }
    iconView.image = image;
}

- (void) reset {
    if (self.instance == nil) {
        return;
    }
    
    NSMutableString* html = [NSMutableString string];
    
    [html appendString: @"<p><b>"];
    [html appendString: self.instance.name];
    [html appendString: @"</b><br/>"];
    
    if (self.instance.isFlagged) {
        [html appendString: @"<p><span style='color:red'>This bot has been flagged as offensive</span><br/>"];
    }
    
    [html appendString: @"<span style=\"font-size:12px;\">"];
    [html appendFormat: @"%@<br/>", self.instance.categories];
    if ([self.instance.tags length] > 0) {
        [html appendFormat: @"%@<br/>", self.instance.tags];
    }
    
    if ([self.instance.website length] > 0) {
        [html appendFormat: @"<a href='%@'>%@</a><br/>", self.instance.website, self.instance.website];
    }
    if ([self.instance.license length] > 0) {
        [html appendFormat: @"%@<br/>", self.instance.license];
    }
    
    [html appendFormat: @"by %@<br/>", self.instance.creator];
    [html appendFormat: @"on %@<br/>", self.instance.creationDate];
    [html appendFormat: @"%@ neurons<br/>", self.instance.size];
    
    [html appendFormat: @"%@ connects, %@ today, %@ week, %@ month<br/>",
     self.instance.connects, self.instance.dailyConnects,
     self.instance.weeklyConnects, self.instance.monthlyConnects];
    
    [html appendString: @"</span></p>"];
    
    [detailsWebView loadHTMLString: html baseURL: nil];
    
    html = [NSMutableString string];
    
    if ([self.instance.instanceDescription length] > 0) {
        [html appendString: @"<p>"];
        [html appendString: self.instance.instanceDescription];
        [html appendString: @"</p>"];
    }
    if ([self.instance.details length] > 0) {
        [html appendString: @"<p><small>"];
        [html appendString: self.instance.details];
        [html appendString: @"</small></p>"];
    }
    if ([self.instance.disclaimer length] > 0) {
        [html appendString: @"<p><small>"];
        [html appendString: self.instance.disclaimer];
        [html appendString: @"</small></p>"];
    }
    
    [webView loadHTMLString: html baseURL: nil];
}

- (void) propertiesMenu {
    UIActionSheet* menu;
    if (!self.instance.isAdmin) {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                                           delegate: self
                                  cancelButtonTitle: @"Cancel"
                             destructiveButtonTitle: nil
                                  otherButtonTitles:@"Flag Bot", @"Website", nil];
    } else {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                                           delegate: self
                                  cancelButtonTitle: @"Cancel"
                             destructiveButtonTitle: nil
                                  otherButtonTitles:@"Edit Bot", @"Change Icon", @"Delete Bot", @"Website - Admin", nil];
    }
    [menu showFromBarButtonItem: propertiesMenu animated: YES];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (!self.instance.isAdmin) {
        switch (buttonIndex) {
            case 0:
                [self flag];
                break;
            case 1:
                [self website];
                break;
            default:
                break;
        }
    } else {
        switch (buttonIndex) {
            case 0:
                [self edit];
                break;
            case 1:
                [self changeIcon];
                break;
            case 2:
                [self delete];
                break;
            case 3:
                [self website];
            default:
                break;
        }
    }
}

-(void)handleTap:(UIGestureRecognizer *)sender {
    [self zoomImage: iconView.image title: self.instance.name];
}

- (void)edit {
    LibreEditBotViewController *view = [[LibreEditBotViewController alloc] init];
    view.parent = self;
    view.instance = self.instance;
    [self.navigationController pushViewController: view animated: YES];
}

- (void)flag {
    alert = @"flag";
    UIAlertView* alertView = [[UIAlertView alloc]
                              initWithTitle: @"Flag Bot"
                              message: @"Enter the reason for flagging the bot as offensive"
                              delegate:self cancelButtonTitle: @"Cancel"  otherButtonTitles:@"Flag", nil];
    alertView.alertViewStyle = UIAlertViewStylePlainTextInput;
    [alertView show];
}

- (void)delete {
    alert = @"delete";
    UIAlertView* alertView = [[UIAlertView alloc]
                              initWithTitle: @"Delete Bot"
                              message: @"This will delete the bot and all of its content"
                              delegate: self cancelButtonTitle: @"No"  otherButtonTitles:@"Yes", nil];
    [alertView show];
}


- (void)alertView: (UIAlertView *) alertView clickedButtonAtIndex: (NSInteger) buttonIndex
{
    if (buttonIndex == 0) {
        return;
    }
    if ([alert isEqualToString: @"flag"]) {
        NSString* reason = [[alertView textFieldAtIndex:0] text];
        if (reason == nil || reason.length == 0) {
            [self error: @"You must enter a valid reason for flagging the bot"];
            return;
        }
        self.instance.isFlagged = true;
        self.instance.flaggedReason = [NSMutableString stringWithString: reason];
        [self showBusy];
        [self.sdk flag: self.instance action: self];
    } else if ([alert isEqualToString: @"delete"]) {
        [self showBusy];
        LibreDeleteInstanceAction* action = [LibreDeleteInstanceAction alloc];
        action.controller = self;
        [self.sdk delete: self.instance action: action];
    }
}

- (void) finished {
    [self reset];
    [self resetIcon];
    [self stopBusy];
}

- (void) deleteSuccessful {
    [self stopBusy];
    [self.navigationController popViewControllerAnimated: YES];
    [self.navigationController popViewControllerAnimated: YES];
}

- (void) changeIcon {
    UIImagePickerController* picker= [[UIImagePickerController alloc]init];
    picker.delegate = self;
    picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    [self presentViewController:picker animated: YES completion:nil];
}

- (void)website {
    [[UIApplication sharedApplication] openURL: [NSURL URLWithString: [NSString stringWithFormat: @"https://www.botlibre.com/browse?id=%@", self.instance.id]]];
}

- (void) updateIconResponse: (LibreWebMedium*) instance {
    [self stopBusy];
    self.instance = (LibreBot*)instance;
    [self resetIcon];
}

- (void) openChat {
    LibreChatViewController *view = [[LibreChatViewController alloc] init];
    view.instance = self.instance;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) imagePickerController: (UIImagePickerController*) picker
         didFinishPickingImage: (UIImage*) image
                   editingInfo:(NSDictionary*) info
{
    [picker dismissViewControllerAnimated: YES completion:nil];
    
    LibreUpdateInstanceIconAction* action = [LibreUpdateInstanceIconAction alloc];
    action.controller = self;
    
    [self.sdk updateIcon: image instance: self.instance action: action];
}

- (void) imagePickerControllerDidCancel:(UIImagePickerController *) picker {
    [picker dismissViewControllerAnimated: YES completion:nil];
}

@end
