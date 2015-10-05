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

#import "LibreBrowseViewController.h"
#import "LibreUI.h"
#import "LibreBot.h"
#import "LibreChatViewController.h"
#import "LibreBotViewController.h"
#import "LibreViewBotAction.h"
#import "LibreContent.h"
#import "LibreCategoriesAction.h"
#import "LibreBrowse.h"
#import "LibreBrowseAction.h"
#import "LibreSearchBotViewController.h"
#import "LibreOpenChatAction.h"
#import "LibreChooseCategoryViewController.h"

@implementation LibreBrowseViewController {
    LibreBot* selection;
    UIBarButtonItem* propertiesMenu;
    UITableView* table;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    UIView *superview = self.view;
    
    UIButton* chat = [LibreUI newButton: @"Chat" in: self.view];
    [LibreUI okButton: chat];
    [LibreUI constrain: chat bottom: -2 in: self.view];
    [LibreUI constrain: chat left: 2 ratio: 0.5 in: self.view];
    [LibreUI constrain: chat right: -2 in: self.view];
    [chat addTarget: self action: @selector(openChat) forControlEvents: UIControlEventTouchUpInside];
    
    UIButton* view = [LibreUI newButton: @"View" in: self.view];
    [LibreUI constrain: view bottom: -2 in: self.view];
    [LibreUI constrain: view left: 2 in: self.view];
    [LibreUI constrain: view right: -2 ratio: 0.5 in: self.view];
    [view addTarget: self action: @selector(openView) forControlEvents: UIControlEventTouchUpInside];
    
    table = [[UITableView alloc ] initWithFrame: self.view.bounds style: UITableViewStylePlain];
    table.rowHeight = 62;
    [table setTranslatesAutoresizingMaskIntoConstraints:NO];
    [superview addSubview: table];
    
    [LibreUI constrain: table top: 64 in: self.view];
    [LibreUI constrain: table left: 0 in: self.view];
    [LibreUI constrain: table right: 0 in: self.view];
    [LibreUI constrain: table bottom: -4 to: chat in: self.view];
    
    table.delegate = self;
    table.dataSource = self;
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(openChat)];
    tapGesture.numberOfTapsRequired = 2;
    tapGesture.cancelsTouchesInView = NO;
    [table addGestureRecognizer: tapGesture];
    
    self.navigationItem.title = @"Browse";
    
    propertiesMenu = [LibreUI newMenuButton: self selector: @selector(propertiesMenu)];
    UIBarButtonItem* searchMenu = [LibreUI newToolBarButton: @"search" highlight: @"search2" in: self selector: @selector(search)];
    
    self.navigationItem.rightBarButtonItems = [[NSArray alloc] initWithObjects: propertiesMenu, searchMenu, nil];
    
    [self fetchCategories];
}

- (void) reset {
    [table reloadData];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.instances count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableItem";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle reuseIdentifier: simpleTableIdentifier];
        cell.detailTextLabel.numberOfLines = 2;
    }
    
    LibreBot* bot = [self.instances objectAtIndex: indexPath.row];
    cell.textLabel.text = bot.name;
    UIImage* image = [self.sdk fetchImage: bot.avatar];
    if (image != nil) {
        image = [LibreUI resizeImage: image in: CGSizeMake(58, 58)];
        cell.imageView.image = image;
    }
    cell.detailTextLabel.text = bot.instanceDescription;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    selection = [self.instances objectAtIndex: indexPath.item];
}


- (void) propertiesMenu {
    UIActionSheet* menu;
    if (self.sdk.user == nil) {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                                           delegate: self
                                  cancelButtonTitle: @"Cancel"
                             destructiveButtonTitle: nil
                                  otherButtonTitles: @"Browse Featured", @"Browse Category", @"Search", nil];
    } else {
        menu = [[UIActionSheet alloc] initWithTitle: nil
                                           delegate: self
                                  cancelButtonTitle: @"Cancel"
                             destructiveButtonTitle: nil
                                  otherButtonTitles: @"My Bots", @"Browse Featured", @"Browse Category", @"Search", nil];
    }
    [menu showFromBarButtonItem: propertiesMenu animated: YES];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (self.sdk.user == nil) {
        switch (buttonIndex) {
            case 0:
                [self featured];
                break;
            case 1:
                [self browseCategory];
                break;
            case 2:
                [self search];
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
                [self featured];
                break;
            case 2:
                [self browseCategory];
                break;
            case 3:
                [self search];
                
            default:
                break;
        }
    }
}

- (void) search {
    if ([self.parent isKindOfClass: [LibreSearchBotViewController class]]) {
        [self.navigationController popViewControllerAnimated:YES];
        return;
    }
    LibreSearchBotViewController* view = [[LibreSearchBotViewController alloc] init];
    view.parent = self;
    
    NSMutableArray *viewControllers = [NSMutableArray arrayWithArray:[[self navigationController] viewControllers]];
    [viewControllers removeLastObject];
    [viewControllers addObject: view];
    [[self navigationController] setViewControllers: viewControllers animated: YES];
}

- (void) browseCategory {
    LibreChooseCategoryViewController* view = [[LibreChooseCategoryViewController alloc] init];
    view.instances = self.categories;
    view.delegate = self;
    view.source = nil;
    [self.navigationController pushViewController: view animated: YES];
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

- (void) featured {
    [self showBusy];
    LibreBrowse* browse = [LibreBrowse alloc];
    browse.type = @"Bot";
    browse.typeFilter = @"Featured";
    LibreBrowseAction* action = [LibreBrowseAction alloc];
    action.controller = self;
    [self.sdk browse: browse action: action];
}

- (void) openChat {
    if (selection == nil) {
        [[[UIAlertView alloc] initWithTitle: @"Error" message: @"Selection required" delegate:nil cancelButtonTitle: @"OK" otherButtonTitles:nil]show];
        return;
    }
    if (selection.isExternal) {
        [[[UIAlertView alloc] initWithTitle: @"Error" message: @"Cannot chat with external bots" delegate:nil cancelButtonTitle: @"OK" otherButtonTitles:nil]show];
        return;
    }
    
    LibreOpenChatAction* action = [LibreOpenChatAction alloc];
    action.controller = self;
    [self.sdk fetch: selection action: action];
}

- (void) chatResponse: (LibreBot*) response {
    [self stopBusy];
    LibreChatViewController *view = [[LibreChatViewController alloc] init];
    view.instance = response;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) openView {
    if (selection == nil) {
        [[[UIAlertView alloc] initWithTitle: @"Error" message: @"Selection required" delegate:nil cancelButtonTitle: @"OK" otherButtonTitles:nil]show];
        return;
    }

    [self showBusy];
    LibreViewBotAction* action = [LibreViewBotAction alloc];
    action.controller = self;
    [self.sdk fetch: selection action: action];
}

- (void) viewResponse: (LibreBot*) response {
    [self stopBusy];
    LibreBotViewController *view = [[LibreBotViewController alloc] init];
    view.instance = response;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) choice: (NSString*)choice source: (UIView*) source {
    [self showBusy];
    LibreBrowse* browse = [LibreBrowse alloc];
    browse.type = @"Bot";
    browse.category = choice;
    LibreBrowseAction* action = [LibreBrowseAction alloc];
    action.controller = self;
    [self.sdk browse: browse action: action];
}

@end
