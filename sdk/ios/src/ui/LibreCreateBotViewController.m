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

#import "LibreCreateBotViewController.h"
#import "LibreUI.h"
#import "LibreBot.h"
#import "LibreBotViewController.h"
#import "LibreTemplatesAction.h"
#import "LibreTagsAction.h"
#import "LibreCategoriesAction.h"
#import "LibreContent.h"
#import "LibreChooseTemplateViewController.h"
#import "LibreChooseCategoryViewController.h"

@implementation LibreCreateBotViewController {
    UITableViewCell* nameCell;
    UITextField* nameText;
    UITableViewCell* descriptionCell;
    UITextView* descriptionText;
    UITableViewCell* detailsCell;
    UITextView* detailsText;
    UITableViewCell* disclaimerCell;
    UITextView* disclaimerText;
    UITableViewCell* categoriesCell;
    UITextField* categoriesText;
    UITableViewCell* tagsCell;
    UITextField* tagsText;
    UITableViewCell* websiteCell;
    UITextField* websiteText;
    UITableViewCell* licenseCell;
    UITextField* licenseText;
    UITableViewCell* templateCell;
    UITextField* templateText;
    
    UITableViewCell* forkCell;
    UISwitch* forkSwitch;
    UITableViewCell* privateCell;
    UISwitch* privateSwitch;
    UITableViewCell* hiddenCell;
    UISwitch* hiddenSwitch;
    UITableViewCell* accessCell;
    UILabel* accessChoice;
    
    UITableView* table;
    
    NSLayoutConstraint* createBottom;
    
    NSArray* accessModes;
    NSArray* licenses;
    NSMutableArray* templates;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    accessModes = LibreSDKConnection.accessModes;
    licenses = LibreSDKConnection.licenses;
    
    self.navigationItem.title = @"Create Bot";
    
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
    
    descriptionCell = [[UITableViewCell alloc] init];
    UIView* group = [[UIView alloc] initWithFrame: CGRectInset(descriptionCell.contentView.bounds, 5, 0)];
    [descriptionCell addSubview: group];
    UILabel* label = [LibreUI newLabel: @"Description" in: group controller: self];
    [LibreUI constrain: label top: 2 in: group];
    [LibreUI constrain: label left: 2 in: group];
    [LibreUI constrain: label right: -2 in: group];
    descriptionText = [LibreUI newTextViewIn: group controller: self];
    [LibreUI constrain: descriptionText top: 2 to: label in: group];
    [LibreUI constrain: descriptionText left: 2 in: group];
    [LibreUI constrain: descriptionText right: -2 in: group];
    [LibreUI constrain: descriptionText height: 40 in: group];
    descriptionText.delegate = self;
    
    detailsCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(detailsCell.contentView.bounds, 5, 0)];
    [detailsCell addSubview: group];
    label = [LibreUI newLabel: @"Details (optional)" in: group controller: self];
    [LibreUI constrain: label top: 2 in: group];
    [LibreUI constrain: label left: 2 in: group];
    [LibreUI constrain: label right: -2 in: group];
    detailsText = [LibreUI newTextViewIn: group controller: self];
    [LibreUI constrain: detailsText top: 2 to: label in: group];
    [LibreUI constrain: detailsText left: 2 in: group];
    [LibreUI constrain: detailsText right: -2 in: group];
    [LibreUI constrain: detailsText height: 40 in: group];
    detailsText.delegate = self;
    
    disclaimerCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(disclaimerCell.contentView.bounds, 5, 0)];
    [disclaimerCell addSubview: group];
    label = [LibreUI newLabel: @"Disclaimer (optional)" in: group controller: self];
    [LibreUI constrain: label top: 2 in: group];
    [LibreUI constrain: label left: 2 in: group];
    [LibreUI constrain: label right: -2 in: group];
    disclaimerText = [LibreUI newTextViewIn: group controller: self];
    [LibreUI constrain: disclaimerText top: 2 to: label in: group];
    [LibreUI constrain: disclaimerText left: 2 in: group];
    [LibreUI constrain: disclaimerText right: -2 in: group];
    [LibreUI constrain: disclaimerText height: 40 in: group];
    detailsText.delegate = self;
    
    categoriesCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(categoriesCell.contentView.bounds, 5, 0)];
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
    
    licenseCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(licenseCell.contentView.bounds, 5, 0)];
    [licenseCell addSubview: group];
    licenseText = [LibreUI newText: @"License (optional)" in: group controller: self];
    [LibreUI constrain: licenseText top: 7 in: group];
    [LibreUI constrain: licenseText left: 4 in: group];
    [LibreUI constrain: licenseText right: -30 in: group];
    button = [LibreUI choiceButtonIn: group];
    [LibreUI constrain: button top: 4 in: group];
    [LibreUI constrain: button right: -2 in: group];
    [button addTarget: self action: @selector(chooseLicense) forControlEvents: UIControlEventTouchUpInside];
    
    websiteCell = [[UITableViewCell alloc] init];
    websiteText = [LibreUI newText: @"Website (optional)" inCell: websiteCell controller: self];
    websiteText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    
    templateCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(templateCell.contentView.bounds, 5, 0)];
    [templateCell addSubview: group];
    templateText = [LibreUI newText: @"Template" in: group controller: self];
    templateText.autocapitalizationType = UITextAutocapitalizationTypeNone;
    [LibreUI constrain: templateText top: 7 in: group];
    [LibreUI constrain: templateText left: 4 in: group];
    [LibreUI constrain: templateText right: -30 in: group];
    button = [LibreUI choiceButtonIn: group];
    [LibreUI constrain: button top: 4 in: group];
    [LibreUI constrain: button right: -2 in: group];
    [button addTarget: self action: @selector(chooseTemplate) forControlEvents: UIControlEventTouchUpInside];
    
    privateCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(privateCell.contentView.bounds, 5, 0)];
    [privateCell addSubview: group];
    UILabel* privateLabel = [LibreUI newLabel: @"Private" in: group controller: self];
    [LibreUI constrain: privateLabel top: 10 in: group];
    [LibreUI constrain: privateLabel left: 2 in: group];
    privateSwitch = [LibreUI newSwitch: NO in: group controller: self];
    [LibreUI constrain: privateSwitch top: 2 in: group];
    [LibreUI constrain: privateSwitch left: 12 to: privateLabel in: group];
    
    hiddenCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(hiddenCell.contentView.bounds, 5, 0)];
    [hiddenCell addSubview: group];
    UILabel* hiddenLabel = [LibreUI newLabel: @"Hidden" in: group controller: self];
    [LibreUI constrain: hiddenLabel top: 10 in: group];
    [LibreUI constrain: hiddenLabel left: 2 in: group];
    hiddenSwitch = [LibreUI newSwitch: NO in: group controller: self];
    [LibreUI constrain: hiddenSwitch top: 2 in: group];
    [LibreUI constrain: hiddenSwitch left: 12 to: hiddenLabel in: group];
    
    accessCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(accessCell.contentView.bounds, 5, 0)];
    [accessCell addSubview: group];
    label = [LibreUI newLabel: @"Access Mode" in: group controller: self];
    [LibreUI constrain: label top: 10 in: group];
    [LibreUI constrain: label left: 2 in: group];
    accessChoice = [LibreUI newLabel: (NSString*)accessModes[0] in: group controller: self];
    accessChoice.textColor = [UIColor blackColor];
    [LibreUI constrain: accessChoice top: 10 in: group];
    [LibreUI constrain: accessChoice left: 12 to: label in: group];
    UIButton* accessButton = [LibreUI choiceButtonIn: group];
    [LibreUI constrain: accessButton top: 4 in: group];
    [LibreUI constrain: accessButton left: 2 to: accessChoice in: group];
    [accessButton addTarget: self action: @selector(chooseAccessMode) forControlEvents: UIControlEventTouchUpInside];
    
    forkCell = [[UITableViewCell alloc] init];
    group = [[UIView alloc] initWithFrame: CGRectInset(forkCell.contentView.bounds, 5, 0)];
    [forkCell addSubview: group];
    UILabel* forkLabel = [LibreUI newLabel: @"Allow Forking" in: group controller: self];
    [LibreUI constrain: forkLabel top: 10 in: group];
    [LibreUI constrain: forkLabel left: 2 in: group];
    forkSwitch = [LibreUI newSwitch: YES in: group controller: self];
    [LibreUI constrain: forkSwitch top: 2 in: group];
    [LibreUI constrain: forkSwitch left: 12 to: forkLabel in: group];
    
    UIButton* create = [LibreUI newButton: @"Create" in: self.view];
    [LibreUI okButton: create];
    createBottom = [LibreUI constrain: create bottom: -2 in: self.view];
    [LibreUI constrain: create left: 2 in: self.view];
    [LibreUI constrain: create right: -2 in: self.view];
    [create addTarget: self action: @selector(create) forControlEvents: UIControlEventTouchUpInside];
    
    [LibreUI constrain: table bottom: -2 to: create in: self.view];
    
    [self registerForKeyboardNotifications];
    
    LibreTemplatesAction* action = [LibreTemplatesAction alloc];
    action.controller = self;
    [self.sdk fetchTemplates: action];
    
    [self fetchTags];
    [self fetchCategories];
}

- (void) updateTemplates: (NSMutableArray*) newTemplates {
    templates = newTemplates;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 13;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.row) {
        case 1 : return 80;
        case 2 : return 80;
        case 3 : return 80;
    }
    return 44;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    switch (indexPath.row) {
        case 0 : return nameCell;
        case 1 : return descriptionCell;
        case 2 : return detailsCell;
        case 3 : return disclaimerCell;
        case 4 : return categoriesCell;
        case 5 : return tagsCell;
        case 6 : return websiteCell;
        case 7 : return licenseCell;
        case 8 : return templateCell;
        case 9 : return privateCell;
        case 10 : return hiddenCell;
        case 11 : return accessCell;
        case 12 : return forkCell;
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
    createBottom.constant = -height;
    [self.view layoutIfNeeded];
}

- (void)keyboardWillBeHidden:(NSNotification*)notification
{
    createBottom.constant = -2;
    [self.view layoutIfNeeded];
}

- (void) chooseAccessMode {
    [self choose: accessModes default: accessChoice.text title: @"Access Mode" source: accessChoice];
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

- (void) chooseTemplate {
    LibreChooseTemplateViewController* view = [[LibreChooseTemplateViewController alloc] init];
    view.instances = templates;
    view.delegate = self;
    view.source = templateText;
    [self.navigationController pushViewController: view animated: YES];
}

- (void) chooseLicense {
    [self choose: licenses default: licenseText.text title: @"Licenses" source: licenseText];
}

- (void)create {
    [self showBusy];
    
    LibreBot* instance = [LibreBot alloc];
    instance.name = nameText.text;
    instance.license = [NSMutableString stringWithString: licenseText.text];
    instance.website = [NSMutableString stringWithString: websiteText.text];
    instance.template = [NSMutableString stringWithString: templateText.text];
    instance.instanceDescription = [NSMutableString stringWithString: descriptionText.text];
    instance.details = [NSMutableString stringWithString: detailsText.text];
    instance.disclaimer = [NSMutableString stringWithString: disclaimerText.text];
    instance.categories = [NSMutableString stringWithString: categoriesText.text];
    instance.tags = [NSMutableString stringWithString: tagsText.text];
    instance.isPrivate = privateSwitch.on;
    instance.isHidden = hiddenSwitch.on;
    instance.allowForking = forkSwitch.on;
    instance.accessMode = accessChoice.text;
    
    [self.sdk create: instance action: self];
}

- (void) response: (LibreBot*) response {
    [self stopBusy];
    
    LibreBotViewController *view = [[LibreBotViewController alloc] init];
    view.instance = response;
    
    NSMutableArray *viewControllers = [NSMutableArray arrayWithArray:[[self navigationController] viewControllers]];
    [viewControllers removeLastObject];
    [viewControllers addObject: view];
    [[self navigationController] setViewControllers: viewControllers animated: YES];
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
