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

#import "LibreSearchBotViewController.h"
#import "LibreUI.h"
#import "LibreBot.h"
#import "LibreBotViewController.h"
#import "LibreContent.h"
#import "LibreBrowse.h"
#import "LibreBrowseAction.h"
#import "LibreChooseCategoryViewController.h"

@implementation LibreSearchBotViewController {
    UITableViewCell* nameCell;
    UITextField* nameText;
    UITableViewCell* categoriesCell;
    UITextField* categoriesText;
    UITableViewCell* tagsCell;
    UITextField* tagsText;
    UITableViewCell* sortCell;
    UILabel* sortChoice;
    
    UITableViewCell* typeCell;
    UISegmentedControl* typeChoice;
    
    UITableView* table;
    
    NSLayoutConstraint* searchBottom;
    
    NSArray* sorts;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    sorts = @[@"name", @"date", @"size", @"stars", @"thumbs up", @"thumbs down",
                    @"last connect", @"connects", @"connects today", @"connects this week ", @"connects this month"];

    self.navigationItem.title = @"Search";
    
    table = [[UITableView alloc] initWithFrame: self.view.bounds style: UITableViewStylePlain];
    table.delegate = self;
    table.dataSource = self;
    table.allowsSelection = NO;
    [table setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: table];
    [LibreUI constrain: table top: 2 in: self.view];
    [LibreUI constrain: table left: 2 in: self.view];
    [LibreUI constrain: table right: -2 in: self.view];
    
    nameCell = [[UITableViewCell alloc] init];
    nameText = [LibreUI newText: @"Name" inCell: nameCell controller: self];
    
    categoriesCell = [[UITableViewCell alloc] init];
    UIView* group = [[UIView alloc] initWithFrame: CGRectInset(categoriesCell.contentView.bounds, 5, 0)];
    [categoriesCell addSubview: group];
    categoriesText = [LibreUI newText: @"Categories" in: group controller: self];
    [LibreUI constrain: categoriesText top: 7 in: group];
    [LibreUI constrain: categoriesText left: 4 in: group];
    [LibreUI constrain: categoriesText right: -30 in: group];
    UIButton* button = [LibreUI choiceButtonIn: group];
    [LibreUI constrain: button top: 4 in: group];
    [LibreUI constrain: button right: -2 in: group];
    [button addTarget: self action: @selector(addCategory) forControlEvents: UIControlEventTouchUpInside];
    
    tagsCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(tagsCell.contentView.bounds, 5, 0)];
    [tagsCell addSubview: group];
    tagsText = [LibreUI newText: @"Tags" in: group controller: self];
    tagsText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    [LibreUI constrain: tagsText top: 7 in: group];
    [LibreUI constrain: tagsText left: 4 in: group];
    [LibreUI constrain: tagsText right: -30 in: group];
    button = [LibreUI choiceButtonIn: group];
    [LibreUI constrain: button top: 4 in: group];
    [LibreUI constrain: button right: -2 in: group];
    [button addTarget: self action: @selector(addTag) forControlEvents: UIControlEventTouchUpInside];
    
    typeCell = [[UITableViewCell alloc] init];
    typeChoice = [LibreUI newSegmentedControl: @[@"Public Bots", @"Private Bots", @"My Bots"] inCell: typeCell controller: self];
    
    sortCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(sortCell.contentView.bounds, 5, 0)];
    [sortCell addSubview: group];
    UILabel* label = [LibreUI newLabel: @"Sort" in: group controller: self];
    [LibreUI constrain: label top: 10 in: group];
    [LibreUI constrain: label left: 2 in: group];
    sortChoice = [LibreUI newLabel: (NSString*)sorts[0] in: group controller: self];
    sortChoice.text = @"connects";
    sortChoice.textColor = [UIColor blackColor];
    [LibreUI constrain: sortChoice top: 10 in: group];
    [LibreUI constrain: sortChoice left: 12 to: label in: group];
    button = [LibreUI choiceButtonIn: group];
    [LibreUI constrain: button top: 4 in: group];
    [LibreUI constrain: button left: 2 to: sortChoice in: group];
    [button addTarget: self action: @selector(chooseSort) forControlEvents: UIControlEventTouchUpInside];
    
    UIButton* search = [LibreUI newButton: @"Search" in: self.view];
    [LibreUI okButton: search];
    searchBottom = [LibreUI constrain: search bottom: -2 in: self.view];
    [LibreUI constrain: search left: 2 in: self.view];
    [LibreUI constrain: search right: -2 in: self.view];
    [search addTarget: self action: @selector(search) forControlEvents: UIControlEventTouchUpInside];
    
    [LibreUI constrain: table bottom: -2 to: search in: self.view];
    
    [self registerForKeyboardNotifications];
    
    [self fetchTags];
    [self fetchCategories];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 5;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.row) {
        case 0 : return typeCell;
        case 1 : return nameCell;
        case 2 : return categoriesCell;
        case 3 : return tagsCell;
        case 4 : return sortCell;
    }
    return nil;
}

- (BOOL)textFieldShouldReturn: (UITextField*)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)keyboardWillShow:(NSNotification*)notification
{
    NSDictionary *info = [notification userInfo];
    NSValue *kbFrame = [info objectForKey: UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardFrame = [kbFrame CGRectValue];
    CGFloat height = [LibreUI isPortrait] ? keyboardFrame.size.height : keyboardFrame.size.width;
    searchBottom.constant = -height;
    [self.view layoutIfNeeded];
}

- (void)keyboardWillBeHidden:(NSNotification*)notification
{
    searchBottom.constant = -2;
    [self.view layoutIfNeeded];
}

- (void) addCategory {
    LibreChooseCategoryViewController* view = [[LibreChooseCategoryViewController alloc] init];
    view.instances = self.categories;
    view.delegate = self;
    view.source = categoriesText;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) addTag {
    [self choose: self.tags default: @"" title: @"Add Tag" source: tagsText];
}

- (void) chooseSort {
    [self choose: sorts default: sortChoice.text title: @"Sort" source: sortChoice];
}

- (void)search {
    [self showBusy];
    LibreBrowse* browse = [LibreBrowse alloc];
    browse.type = @"Bot";
    int index = (int)typeChoice.selectedSegmentIndex;
    if (index == 0) {
        browse.typeFilter = @"Public";
    } else if (index == 1) {
        browse.typeFilter = @"Private";
    } else if (index == 2) {
        browse.typeFilter = @"Personal";
    }
    browse.category = categoriesText.text;
    browse.tag = tagsText.text;
    browse.filter = nameText.text;
    browse.sort = sortChoice.text;

    LibreBrowseAction* action = [LibreBrowseAction alloc];
    action.controller = self;
    [self.sdk browse: browse action: action];
}

- (void) choice: (NSString*)choice source: (UIView*) source {
    if (source == tagsText) {
        if (tagsText.text.length > 0) {
            tagsText.text = [NSString stringWithFormat: @"%@, %@", tagsText.text, choice];
        } else {
            tagsText.text = choice;
        }
    } else if (source == categoriesText) {
        if (categoriesText.text.length > 0) {
            categoriesText.text = [NSString stringWithFormat: @"%@, %@", categoriesText.text, choice];
        } else {
            categoriesText.text = choice;
        }
    } else {
        [super choice: choice source: source];
    }
}

@end
