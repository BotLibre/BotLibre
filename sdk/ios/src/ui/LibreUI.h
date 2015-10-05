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

#import "LibreConfig.h"

@interface LibreUI : NSObject

+ (NSLayoutConstraint*) constrain: (UIView*) view width: (int) width in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view height: (int) height in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view bottom: (int) value in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view bottom: (int) value to: (UIView*) anchor in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view top: (int) value in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view top: (int) value to: (UIView*)anchor in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view left: (int) value in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view left: (int) value ratio: (float) ratio in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view left: (int) value to: (UIView*) anchor in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view right: (int) value in: (UIView*) parent;

+ (NSLayoutConstraint*) constrain: (UIView*) view right: (int) value ratio: (float) ratio in: (UIView*) parent;

+ (void) constrainTop: (UIView*) view in: (UIView*) parent;

+ (UIButton*) choiceButtonIn: (UIView*) view;

+ (UIButton*) newButton: (NSString*)title in: (UIView*) view;

+ (UIButton*) newButton: (NSString*)title in: (UIView*) view anchor: (UIView*)anchor;

+ (UIBarButtonItem*) newToolBarButton: (NSString*) file highlight: file2 in: (UIViewController*) controler selector: (SEL) selector;

+ (UIBarButtonItem*) newToolBarImageButton: (UIImage*) image highlight: (UIImage*) image2 in: (UIViewController*) controller selector: (SEL)selector;
    
+ (UIBarButtonItem*) newMenuButton: (UIViewController*) controler selector: (SEL) selector;

+ (UIImage*) resizeImage: (UIImage*)image in: (CGSize)viewSize;

+ (UIImage*) fetchImage: (NSString*)url;

+ (BOOL) isPortrait;

+ (CGFloat) displayHeight;

+ (UIView*) anchor: (UIView*)view to: (UIView*)anchor in: (UIView*)parent;

+ (UITextField*) newText: (NSString*)name in: (UIView*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller;

+ (UITextField*) newText: (NSString*)name inCell: (UITableViewCell*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller;
    
+ (UITextField*) newText: (NSString*)name in: (UIView*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller anchor: (UIView*)anchor;

+ (UITextView*) newTextViewIn: (UIView*) parent
                 controller: (UIViewController *) controller;

+ (UISwitch*) newSwitch: (BOOL)value in: (UIView*) parent
             controller: (UIViewController <UITextFieldDelegate> *) controller;

+ (UISegmentedControl*) newSegmentedControl: (NSArray*)values in: (UIView*) parent
                                 controller: (UIViewController*) controller;

+ (UISegmentedControl*) newSegmentedControl: (NSArray*)values inCell: (UITableViewCell*) parent
                                 controller: (UIViewController*) controller;

+ (UIPickerView*) newPickerIn: (UIView*) parent
                       controller: (UIViewController <UIPickerViewDataSource, UIPickerViewDelegate> *) controller;

+ (UITextView*) newTextViewIn: (UIView*) parent
                 controller: (UIViewController *) controller anchor: (UIView*)anchor;

+ (UILabel*) newLabel: (NSString*)name in: (UIView*) parent controller: (UIViewController*) controller anchor: (UIView*)anchor;

+ (UILabel*) newLabel: (NSString*)name in: (UIView*) parent controller: (UIViewController*) controller;

+ (void) okButton: (UIButton*) button;

+ (UIWebView*) newWebView: (NSString*)html in: (UIView*) parent controller: (UIViewController*) controller;

+ (UIImageView*) newImageView: (UIImage*)image in: (UIView*) parent controller: (UIViewController*) controller;

+ (CGFloat) screenWidth;

+ (CGFloat) screenHeight;

/**
 * Convert the text into HTML links.
 * i.e. http:// => <a href="http://
 */
+ (NSString*) linkHTML: (NSString*) text;

@end
