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

#import "LibreUI.h"

@implementation LibreUI

static NSCache* imageCache;

+ (NSLayoutConstraint*) constrain: (UIView*) view width: (int) width in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute:NSLayoutAttributeWidth
                  relatedBy:NSLayoutRelationEqual
                  toItem:nil
                  attribute:NSLayoutAttributeNotAnAttribute
                  multiplier:1
                  constant:width];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view height: (int) height in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute:NSLayoutAttributeHeight
                  relatedBy:NSLayoutRelationEqual
                  toItem:nil
                  attribute:NSLayoutAttributeNotAnAttribute
                  multiplier:1
                  constant:height];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view bottom: (int) value in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeBottom
                  relatedBy: NSLayoutRelationEqual
                  toItem: parent
                  attribute: NSLayoutAttributeBottom
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view bottom: (int) value to: (UIView*) anchor in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeBottom
                  relatedBy: NSLayoutRelationEqual
                  toItem: anchor
                  attribute: NSLayoutAttributeTop
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view top: (int) value in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeTop
                  relatedBy: NSLayoutRelationEqual
                  toItem: parent
                  attribute: NSLayoutAttributeTop
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view top: (int) value to: (UIView*) anchor in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeTop
                  relatedBy: NSLayoutRelationEqual
                  toItem: anchor
                  attribute: NSLayoutAttributeBottom
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view left: (int) value in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeLeft
                  relatedBy: NSLayoutRelationEqual
                  toItem: parent
                  attribute: NSLayoutAttributeLeft
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view left: (int) value ratio: (float) ratio in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeLeft
                  relatedBy: NSLayoutRelationEqual
                  toItem: parent
                  attribute: NSLayoutAttributeRight
                  multiplier: ratio
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view left: (int) value to: (UIView*) anchor in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeLeft
                  relatedBy: NSLayoutRelationEqual
                  toItem: anchor
                  attribute: NSLayoutAttributeRight
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view right: (int) value in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeRight
                  relatedBy: NSLayoutRelationEqual
                  toItem: parent
                  attribute: NSLayoutAttributeRight
                  multiplier: 1
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (NSLayoutConstraint*) constrain: (UIView*) view right: (int) value ratio: (float) ratio in: (UIView*) parent {
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint
                  constraintWithItem: view
                  attribute: NSLayoutAttributeRight
                  relatedBy: NSLayoutRelationEqual
                  toItem: parent
                  attribute: NSLayoutAttributeRight
                  multiplier: ratio
                  constant: value];
    [parent addConstraint: constraint];
    return constraint;
}

+ (void) constrainTop: (UIView*) view in: (UIView*) parent {
    [LibreUI constrain: view top: 66 in: parent];
    [LibreUI constrain: view left: 2 in: parent];
    [LibreUI constrain: view right: -2 in: parent];
}

+ (UIButton*) choiceButtonIn: (UIView*) view {
    UIButton* button = [UIButton buttonWithType: UIButtonTypeRoundedRect];
    [button setTitle: @"▼" forState: UIControlStateNormal];
    [button setTranslatesAutoresizingMaskIntoConstraints:NO];
    [view addSubview: button];
    return button;
}

+ (UIButton*) newButton: (NSString*)title in: (UIView*) view {
    UIButton* button = [UIButton buttonWithType: UIButtonTypeCustom];
    [button setTitle: title forState: UIControlStateNormal];
    [button setTitleColor:[UIColor whiteColor] forState: UIControlStateNormal];
    [button setTitleColor:[UIColor lightGrayColor] forState: UIControlStateHighlighted];
    button.backgroundColor = [UIColor grayColor];
    [button setTranslatesAutoresizingMaskIntoConstraints:NO];
    [view addSubview: button];
    return button;
}

+ (UIButton*) newButton: (NSString*)title in: (UIView*) view anchor: (UIView*)anchor  {
    UIButton* button = [self newButton: title in: view];
    [LibreUI constrain: button top: 2 to: anchor in: view];
    [LibreUI constrain: button left: 2 in: view];
    [LibreUI constrain: button right: -2 in: view];
    [LibreUI constrain: button height: 40 in: view];
    return button;
}

+ (UIBarButtonItem*) newToolBarButton: (NSString*) file highlight: (NSString*) file2 in: (UIViewController*) controller selector: (SEL)selector {
    
    UIImage* image = [UIImage imageNamed: file];
    UIImage* image2 = [UIImage imageNamed: file2];
    UIButton* face = [UIButton buttonWithType: UIButtonTypeCustom];
    [face addTarget:controller action: selector forControlEvents: UIControlEventTouchUpInside];
    [face setImage:image forState: UIControlStateNormal];
    [face setImage:image2 forState: UIControlStateHighlighted];
    UIBarButtonItem* button = [[UIBarButtonItem alloc] initWithCustomView:face];
    [face sizeToFit];
    [button setTarget: controller];
    return button;
}

+ (UIBarButtonItem*) newToolBarImageButton: (UIImage*) image highlight: (UIImage*) image2 in: (UIViewController*) controller selector: (SEL)selector {
    
    UIButton* face = [UIButton buttonWithType: UIButtonTypeCustom];
    [face addTarget:controller action: selector forControlEvents: UIControlEventTouchUpInside];
    [face setImage:image forState: UIControlStateNormal];
    [face setImage:image2 forState: UIControlStateHighlighted];
    UIBarButtonItem* button = [[UIBarButtonItem alloc] initWithCustomView:face];
    [face sizeToFit];
    [button setTarget: controller];
    return button;
}

+ (UIBarButtonItem*) newMenuButton: (UIViewController*) controller selector: (SEL) selector {
    return [self newToolBarButton: @"menu" highlight: @"menu2" in: controller selector: selector];
}

+ (UIImage*) resizeImage: (UIImage*)image in: (CGSize)viewSize {
    UIGraphicsBeginImageContextWithOptions(viewSize, NO, UIScreen.mainScreen.scale);
    CGSize imageSize = image.size;
    float hfactor = imageSize.width / viewSize.width;
    float vfactor = imageSize.height / viewSize.height;
    float factor = fmax(hfactor, vfactor);
    float newWidth = imageSize.width / factor;
    float newHeight = imageSize.height / factor;
    CGRect newRect = CGRectMake(0, 0, newWidth, newHeight);
    [image drawInRect:newRect];
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

+ (NSCache*) imageCache {
    if (imageCache == nil) {
        imageCache = [[NSCache alloc] init];
    }
    return imageCache;
}

/**
 * Convert the text into HTML links.
 * i.e. http:// => <a href="http://
 */
+ (NSString*) linkHTML: (NSString*) text {
    BOOL http = [text rangeOfString: @"http"].location != NSNotFound;
    BOOL www = [text rangeOfString: @"www."].location != NSNotFound;
    BOOL email = [text rangeOfString: @"@"].location != NSNotFound;
    if (!http && !www && !email) {
        return text;
    }
    if ([text rangeOfString: @"<"].location != NSNotFound && [text rangeOfString: @">"].location != NSNotFound) {
        return text;
    }
    if (http) {
        NSRange searchedRange = NSMakeRange(0, [text length]);
        NSString* pattern = @"\\b(?:https?|ftp|file):\\/\\/[a-z0-9-+&@#\\/%?=~_|!:,.;]*[a-z0-9-+&@#\\/%=~_|]";
        NSRegularExpression* regex = [NSRegularExpression
                                        regularExpressionWithPattern: pattern
                                        options: NSRegularExpressionCaseInsensitive
                                        error: nil];
        NSArray* matches = [regex matchesInString: text options:0 range: searchedRange];
        NSMutableString* writer = [NSMutableString string];
        int index = 0;
        for (NSTextCheckingResult* match in matches) {
            int next = (int)[match range].location;
            [writer appendString: [text substringWithRange: NSMakeRange(index, next - index)]];
            NSString* url = [text substringWithRange:[match range]];
            if ([url rangeOfString: @".png" options: NSCaseInsensitiveSearch].location != NSNotFound
                        || [url rangeOfString: @".jpg" options: NSCaseInsensitiveSearch].location != NSNotFound
                        || [url rangeOfString: @".jpeg" options: NSCaseInsensitiveSearch].location != NSNotFound
                        || [url rangeOfString: @".gif" options: NSCaseInsensitiveSearch].location != NSNotFound) {
                [writer appendString:
                        [NSString stringWithFormat: @"<a href='%@' target='_blank'><img src='%@' height='50'></a>", url, url]];
            } else if ([url rangeOfString: @".mp4" options: NSCaseInsensitiveSearch].location != NSNotFound
                        || [url rangeOfString: @".webm" options: NSCaseInsensitiveSearch].location != NSNotFound
                        || [url rangeOfString: @".ogg" options: NSCaseInsensitiveSearch].location != NSNotFound) {
                [writer appendString:
                        [NSString stringWithFormat: @"<a href='%@' target='_blank'><video src='%@' height='50'></a>", url, url]];
            } else if ([url rangeOfString: @".wav" options: NSCaseInsensitiveSearch].location != NSNotFound
                        || [url rangeOfString: @".mp3" options: NSCaseInsensitiveSearch].location != NSNotFound) {
                [writer appendString:
                        [NSString stringWithFormat: @"<a href='%@' target='_blank'><audio src='%@' controls>audio</a>", url, url]];
            } else {
                [writer appendString: [NSString stringWithFormat: @"<a href='%@' target='_blank'>%@</a>", url, url]];
            }
        }
        text = writer;
    } else if (www) {
        NSRange searchedRange = NSMakeRange(0, [text length]);
        NSString* pattern = @"((www\\.)[^\\s]+)";
        NSRegularExpression* regex = [NSRegularExpression
                                      regularExpressionWithPattern: pattern
                                      options: NSRegularExpressionCaseInsensitive
                                      error: nil];
        NSArray* matches = [regex matchesInString: text options:0 range: searchedRange];
        NSMutableString* writer = [NSMutableString string];
        int index = 0;
        for (NSTextCheckingResult* match in matches) {
            int next = (int)[match range].location;
            [writer appendString: [text substringWithRange: NSMakeRange(index, next - index)]];
            NSString* url = [text substringWithRange:[match range]];
            [writer appendString:
                 [NSString stringWithFormat: @"<a href='http://%@' target='_blank'>%@</a>", url, url]];
        }
        text = writer;
    } else if (email) {
        NSRange searchedRange = NSMakeRange(0, [text length]);
        NSString* pattern = @"(([a-zA-Z0-9_\\-\\.]+)@[a-zA-Z_]+?(?:\\.[a-zA-Z]{2,6}))+";
        NSRegularExpression* regex = [NSRegularExpression
                                      regularExpressionWithPattern: pattern
                                      options: NSRegularExpressionCaseInsensitive
                                      error: nil];
        NSArray* matches = [regex matchesInString: text options:0 range: searchedRange];
        NSMutableString* writer = [NSMutableString string];
        int index = 0;
        for (NSTextCheckingResult* match in matches) {
            int next = (int)[match range].location;
            [writer appendString: [text substringWithRange: NSMakeRange(index, next - index)]];
            NSString* url = [text substringWithRange:[match range]];
            [writer appendString:
             [NSString stringWithFormat: @"<a href='mailto:%@' target='_blank'>%@</a>", url, url]];
        }
        text = writer;
    }
    
    return text;
}

+ (UIImage*) fetchImage: (NSString*)url {
    UIImage* image = [[self imageCache] objectForKey: url];
    if (image != nil) {
        return image;
    }
    image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString: url]]];
    if (image == nil) {
        return nil;
    }
    [imageCache setObject: image forKey: url];
    return image;
}

+ (BOOL) isPortrait {
    return UIDeviceOrientationIsPortrait((int)[UIApplication sharedApplication].statusBarOrientation);
}

+ (CGFloat) displayHeight {
    CGRect screenBound = [[UIScreen mainScreen] bounds];
    CGSize screenSize = screenBound.size;
    if ([self isPortrait]) {
        return screenSize.height;
    } else {
        return screenSize.width;
    }
}

+ (UITextField*) newText: (NSString*)name in: (UIView*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller {
    UITextField* text = [[UITextField alloc] init];
    text.borderStyle = UITextBorderStyleRoundedRect;
    text.returnKeyType = UIReturnKeyDone;
    text.clearButtonMode = UITextFieldViewModeWhileEditing;
    text.placeholder = name;
    text.delegate = controller;
    [text setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: text];
    //[LibreUI constrain: text height: 40 in: parent];
    return text;
}

+ (UITextField*) newText: (NSString*)name inCell: (UITableViewCell*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller {
    UITextField* text = [[UITextField alloc] initWithFrame: CGRectInset(parent.contentView.bounds, 15, 0)];
    text.returnKeyType = UIReturnKeyDone;
    text.clearButtonMode = UITextFieldViewModeWhileEditing;
    text.placeholder = name;
    text.delegate = controller;
    [parent addSubview: text];
    return text;
}

+ (UITextField*) newText: (NSString*)name in: (UIView*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller anchor: (UIView*)anchor  {
    UITextField* text = [self newText: name in: parent controller: controller];
    [LibreUI constrain: text top: 2 to: anchor in: parent];
    [LibreUI constrain: text left: 2 in: parent];
    [LibreUI constrain: text right: -2 in: parent];
    return text;
}

+ (UIView*) anchor: (UIView*)view to: (UIView*)anchor in: (UIView*)parent {
    [LibreUI constrain: view top: 2 to: anchor in: parent];
    [LibreUI constrain: view left: 2 in: parent];
    [LibreUI constrain: view right: -2 in: parent];
    return view;
}

+ (UISwitch*) newSwitch: (BOOL)value in: (UIView*) parent
              controller: (UIViewController*) controller {
    UISwitch* view = [[UISwitch alloc] init];
    view.on = value;
    [view setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: view];
    return view;
}

+ (UISegmentedControl*) newSegmentedControl: (NSArray*)values in: (UIView*) parent
             controller: (UIViewController*) controller {
    UISegmentedControl* view = [[UISegmentedControl alloc] init];
    int index = 0;
    for (NSString* value in values) {
        [view insertSegmentWithTitle: value atIndex: index animated:NO];
        index++;
    }
    [view setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: view];
    return view;
}

+ (UISegmentedControl*) newSegmentedControl: (NSArray*)values inCell: (UITableViewCell*) parent
                                 controller: (UIViewController*) controller {
    UISegmentedControl* view = [[UISegmentedControl alloc] initWithFrame: CGRectInset(parent.contentView.bounds, 15, 8)];
    int index = 0;
    for (NSString* value in values) {
        [view insertSegmentWithTitle: value atIndex: index animated:NO];
        index++;
    }
    view.selectedSegmentIndex = 0;
    [parent addSubview: view];
    return view;
}

+ (UIPickerView*) newPickerIn: (UIView*) parent
                       controller: (UIViewController <UIPickerViewDataSource, UIPickerViewDelegate> *) controller {
    UIPickerView* view = [[UIPickerView alloc] init];
    view.dataSource = controller;
    view.delegate = controller;
    [view setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: view];
    return view;
}

+ (UILabel*) newLabel: (NSString*)name in: (UIView*) parent controller: (UIViewController*) controller {
    UILabel* label = [[UILabel alloc] init];
    label.text = name;
    label.textColor = [UIColor grayColor];
    [label setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: label];
    return label;
}

+ (UILabel*) newLabel: (NSString*)name in: (UIView*) parent
              controller: (UIViewController <UITextFieldDelegate> *) controller anchor: (UIView*)anchor  {
    UILabel* label = [self newLabel: name in: parent controller: controller];
    [LibreUI constrain: label top: 2 to: anchor in: parent];
    [LibreUI constrain: label left: 2 in: parent];
    [LibreUI constrain: label right: -2 in: parent];
    //[LibreUI constrain: label height: 30 in: parent];
    return label;
}

+ (UITextView*) newTextViewIn: (UIView*) parent controller: (UIViewController <UITextViewDelegate> *) controller {
    UITextView* text = [[UITextView alloc] init];
    [text setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: text];
    return text;
}

+ (UITextView*) newTextViewIn: (UIView*) parent
                 controller: (UIViewController <UITextViewDelegate> *) controller anchor: (UIView*)anchor  {
    UITextView* text = [self newTextViewIn: parent controller: controller];
    [LibreUI constrain: text top: 2 to: anchor in: parent];
    [LibreUI constrain: text left: 2 in: parent];
    [LibreUI constrain: text right: -2 in: parent];
    return text;
}

+ (void) okButton: (UIButton*) button {
    button.backgroundColor = [UIColor colorWithRed:(0/255.0) green:(153/255.0) blue:(0/255.0) alpha:1];
}

+ (UIWebView*) newWebView: (NSString*)html in: (UIView*) parent controller: (UIViewController <UIWebViewDelegate> *) controller {
    UIWebView* view = [[UIWebView alloc] init];
    [view loadHTMLString: html baseURL: nil];
    [view setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: view];
    view.delegate = controller;
    return view;
}

+ (UIImageView*) newImageView: (UIImage*)image in: (UIView*) parent controller: (UIViewController*) controller {
    UIImageView* view = [[UIImageView alloc] initWithImage: image];
    view.contentMode = UIViewContentModeScaleAspectFit;
    view.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);
    [view setTranslatesAutoresizingMaskIntoConstraints: NO];
    [parent addSubview: view];
    return view;
}

+ (CGFloat) screenWidth {
    CGRect screenBound = [[UIScreen mainScreen] bounds];
    CGSize screenSize = screenBound.size;
    return screenSize.width;
}

+ (CGFloat) screenHeight {
    CGRect screenBound = [[UIScreen mainScreen] bounds];
    CGSize screenSize = screenBound.size;
    return screenSize.height;
}

@end
