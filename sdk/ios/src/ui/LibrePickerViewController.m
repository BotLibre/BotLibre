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

#import "LibrePickerViewController.h"
#import "LibreUI.h"

@implementation LibrePickerViewController {
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
    
    self.navigationItem.title = self.title;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.choices.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableItem";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault reuseIdentifier: simpleTableIdentifier];
    }
    
    NSString* value = [self.choices objectAtIndex: indexPath.row];
    cell.textLabel.text = value;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    self.choice = [self.choices objectAtIndex: indexPath.item];
}

- (void) okay {
    [self.navigationController popViewControllerAnimated:YES];
    
    [self.delegate choice: self.choice source: self.source];
}

@end
