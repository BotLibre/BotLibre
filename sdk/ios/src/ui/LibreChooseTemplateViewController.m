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

#import "LibreChooseTemplateViewController.h"
#import "LibreUI.h"
#import "LibreBot.h"

@implementation LibreChooseTemplateViewController {
    LibreBot* selection;
    UITableView* table;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    UIView *superview = self.view;
    
    UIButton* ok = [LibreUI newButton: @"OK" in: self.view];
    [LibreUI okButton: ok];
    [LibreUI constrain: ok bottom: -2 in: self.view];
    [LibreUI constrain: ok left: 2 in: self.view];
    [LibreUI constrain: ok right: -2 in: self.view];
    [ok addTarget: self action: @selector(okay) forControlEvents: UIControlEventTouchUpInside];
    
    table = [[UITableView alloc ] initWithFrame: self.view.bounds style: UITableViewStylePlain];
    table.rowHeight = 62;
    [table setTranslatesAutoresizingMaskIntoConstraints:NO];
    [superview addSubview: table];
    
    [LibreUI constrain: table top: 64 in: self.view];
    [LibreUI constrain: table left: 0 in: self.view];
    [LibreUI constrain: table right: 0 in: self.view];
    [LibreUI constrain: table bottom: -4 to: ok in: self.view];
    
    table.delegate = self;
    table.dataSource = self;
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(okay)];
    tapGesture.numberOfTapsRequired = 2;
    tapGesture.cancelsTouchesInView = NO;
    [table addGestureRecognizer:tapGesture];

    self.navigationItem.title = @"Templates";
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 100;
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
        cell.detailTextLabel.numberOfLines = 4;
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

- (void) okay {
    [self.navigationController popViewControllerAnimated:YES];
    if (selection != nil) {
        NSString* choice = selection.name;
        [self.delegate choice: choice source: self.source];
    }
}

@end
