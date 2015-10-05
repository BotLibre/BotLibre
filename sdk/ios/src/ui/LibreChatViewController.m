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

#import <MediaPlayer/MediaPlayer.h>
#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>
#import "LibreChatViewController.h"
#import "LibreUI.h"
#import "LibreBot.h"
#import "LibreChatMessage.h"
#import "LibreChatAction.h"
#import "LibreChatResponse.h"
#import "LibreUser.h"

@implementation LibreChatViewController {
    NSMutableArray* chatLog;
    NSString* conversation;
    NSString* responseState;
    LibreChatResponse* currentResponse;
    UIImage* botImage;
    UIImage* userImage;
    UIView* tapView;

    UITextField* chatText;
    NSLayoutConstraint* chatBottom;
    NSLayoutConstraint* imageHeight;
    NSLayoutConstraint* imageRight;
    NSLayoutConstraint* imageBottom;
    NSLayoutConstraint* chatLogTop;
    NSLayoutConstraint* chatLogLeft;
    NSLayoutConstraint* chatLogHeight;
    NSLayoutConstraint* responseViewHeight;
    NSLayoutConstraint* responseViewTop;
    UIImageView* imageView;
    UITableView* chatLogTable;
    UIWebView* responseView;
    
    UIButton* propertiesMenu;
    UIButton* speechButton;
    UIButton* flagButton;
    UIButton* correctionButton;
    
    MPMoviePlayerController* player;
    AVSpeechSynthesizer* speechSynthesizer;
    AVSpeechSynthesisVoice* voice;
    AVPlayer* actionAudioPlayer;
    AVPlayer* poseAudioPlayer;
    AVPlayer* speechAudioPlayer;
    NSString* poseAudio;
}

static BOOL speech;
static BOOL video;
static BOOL deviceVoice;

+ (void)initialize {
    speech = YES;
    video = YES;
}

- (void) viewDidDisappear:(BOOL)animated {
    [self leave];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    chatText = [[UITextField alloc] init];
    chatText.borderStyle = UITextBorderStyleRoundedRect;
    chatText.returnKeyType = UIReturnKeyDone;
    chatText.clearButtonMode = UITextFieldViewModeWhileEditing;
    chatText.delegate = self;
    chatText.placeholder = @"You say";
    [chatText setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: chatText];
    [LibreUI constrain: chatText left: 2 in: self.view];
    [LibreUI constrain: chatText right: -2 in: self.view];
    [LibreUI constrain: chatText height: 40 in: self.view];
    
    responseView = [LibreUI newWebView: @"" in: self.view controller: self];
    [LibreUI constrain: responseView left: 2 in: self.view];
    [LibreUI constrain: responseView right: -2 in: self.view];
    [LibreUI constrain: responseView bottom: -2 to: chatText in: self.view];
    UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action: @selector(zoomResponse)];
    tapGesture.numberOfTapsRequired = 2;
    tapGesture.delegate = self;
    tapGesture.cancelsTouchesInView = NO;
    responseView.userInteractionEnabled = YES;
    [responseView addGestureRecognizer: tapGesture];

    
    UIImage* image = [self.sdk fetchImage: self.instance.avatar];
    if (image == nil) {
        image = [self.sdk defaultBotAvatar];
    }
    imageView = [LibreUI newImageView: image in: self.view controller: self];
    [LibreUI constrain: imageView top: 62 in: self.view];
    [LibreUI constrain: imageView left: 0 in: self.view];
    tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action: @selector(zoomImage)];
    tapGesture.numberOfTapsRequired = 1;
    tapGesture.cancelsTouchesInView = NO;
    imageView.userInteractionEnabled = YES;
    [imageView addGestureRecognizer: tapGesture];

    chatLogTable = [[UITableView alloc ] initWithFrame: self.view.bounds style: UITableViewStylePlain];
    [chatLogTable setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: chatLogTable];
    [LibreUI constrain: chatLogTable right: 0 in: self.view];
    [LibreUI constrain: chatLogTable bottom: -2 to: responseView in: self.view];
    tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action: @selector(zoomLog)];
    tapGesture.numberOfTapsRequired = 2;
    tapGesture.cancelsTouchesInView = NO;
    chatLogTable.userInteractionEnabled = YES;
    [chatLogTable addGestureRecognizer: tapGesture];
    
    [self deviceOrientationDidChangeNotification: nil];
    
    chatLogTable.delegate = self;
    chatLogTable.dataSource = self;
    
    self.navigationItem.title = self.instance.name;
    
    [self registerForKeyboardNotifications];
    
    [[NSNotificationCenter defaultCenter]
        addObserver:self
        selector:@selector(deviceOrientationDidChangeNotification:)
        name:UIDeviceOrientationDidChangeNotification
        object:nil];
    
    player = [[MPMoviePlayerController alloc] init];
    [[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(videoFinished:) name:MPMoviePlayerPlaybackDidFinishNotification object:player];
    [[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(videoStatus:) name:MPMoviePlayerLoadStateDidChangeNotification object: nil];
    player.scalingMode = MPMovieScalingModeAspectFit;
    player.controlStyle =  MPMovieControlStyleNone;
    player.shouldAutoplay = YES;
    player.repeatMode = YES;
    player.backgroundView.backgroundColor = [UIColor clearColor];
    player.view.backgroundColor = [UIColor clearColor];
    for (UIView* subView in player.view.subviews) {
        subView.backgroundColor = [UIColor clearColor];
    }
    [self.view addSubview: player.view];
    tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action: @selector(zoomImage)];
    tapGesture.numberOfTapsRequired = 1;
    tapGesture.cancelsTouchesInView = NO;
    tapView = [[UIView alloc] initWithFrame: player.view.bounds];
    tapView.userInteractionEnabled = YES;
    [tapView addGestureRecognizer: tapGesture];
    [player.view addSubview: tapView];
    
    UIView* toolbar = [[UIView alloc] init];
    [toolbar setTranslatesAutoresizingMaskIntoConstraints: NO];
    [self.view addSubview: toolbar];
    [LibreUI constrain: toolbar left: 2 in: self.view];
    [LibreUI constrain: toolbar right: -2 in: self.view];
    [LibreUI constrain: toolbar height: 40 in: toolbar];
    chatBottom = [LibreUI constrain: toolbar bottom: -2 in: self.view];
    
    [LibreUI constrain: chatText bottom: -2 to: toolbar in: self.view];
    
    propertiesMenu = [UIButton buttonWithType: UIButtonTypeCustom];
    [propertiesMenu setTranslatesAutoresizingMaskIntoConstraints: NO];
    [propertiesMenu addTarget: self action: @selector(propertiesMenu) forControlEvents: UIControlEventTouchUpInside];
    [propertiesMenu setImage: [UIImage imageNamed: @"menu3"] forState: UIControlStateNormal];
    [propertiesMenu sizeToFit];
    [toolbar addSubview: propertiesMenu];
    [LibreUI constrain: propertiesMenu left: 2 in: toolbar];
    [LibreUI constrain: propertiesMenu top: 2 in: toolbar];
    [LibreUI constrain: propertiesMenu height: 32 in: toolbar];
    [LibreUI constrain: propertiesMenu width: 32 in: toolbar];
    
    speechButton = [UIButton buttonWithType: UIButtonTypeCustom];
    [speechButton setTranslatesAutoresizingMaskIntoConstraints: NO];
    [speechButton addTarget: self action: @selector(toggleSpeech) forControlEvents: UIControlEventTouchUpInside];
    [speechButton setImage: [UIImage imageNamed: @"sound"] forState: UIControlStateNormal];
    [speechButton setImage: [UIImage imageNamed: @"mute"] forState: UIControlStateSelected];
    [speechButton sizeToFit];
    speechButton.selected = !speech;
    [toolbar addSubview: speechButton];
    [LibreUI constrain: speechButton left: 8 to: propertiesMenu in: toolbar];
    [LibreUI constrain: speechButton top: 2 in: toolbar];
    [LibreUI constrain: speechButton height: 32 in: toolbar];
    [LibreUI constrain: speechButton width: 32 in: toolbar];
    
    correctionButton = [UIButton buttonWithType: UIButtonTypeCustom];
    [correctionButton setTranslatesAutoresizingMaskIntoConstraints: NO];
    [correctionButton addTarget: self action: @selector(toggleCorrection) forControlEvents: UIControlEventTouchUpInside];
    [correctionButton setImage: [UIImage imageNamed: @"wrong2"] forState: UIControlStateNormal];
    [correctionButton setImage: [UIImage imageNamed: @"wrong"] forState: UIControlStateSelected];
    [correctionButton sizeToFit];
    [toolbar addSubview: correctionButton];
    [LibreUI constrain: correctionButton left: 8 to: speechButton in: toolbar];
    [LibreUI constrain: correctionButton top: 2 in: toolbar];
    [LibreUI constrain: correctionButton height: 32 in: toolbar];
    [LibreUI constrain: correctionButton width: 32 in: toolbar];
    
    flagButton = [UIButton buttonWithType: UIButtonTypeCustom];
    [flagButton setTranslatesAutoresizingMaskIntoConstraints: NO];
    [flagButton addTarget: self action: @selector(toggleFlag) forControlEvents: UIControlEventTouchUpInside];
    [flagButton setImage: [UIImage imageNamed: @"flag2"] forState: UIControlStateNormal];
    [flagButton setImage: [UIImage imageNamed: @"flag"] forState: UIControlStateSelected];
    [flagButton sizeToFit];
    [toolbar addSubview: flagButton];
    [LibreUI constrain: flagButton left: 8 to: correctionButton in: toolbar];
    [LibreUI constrain: flagButton top: 2 in: toolbar];
    [LibreUI constrain: flagButton height: 32 in: toolbar];
    [LibreUI constrain: flagButton width: 32 in: toolbar];
    
    chatLog = [[NSMutableArray alloc] init];
    botImage = nil;
    userImage = nil;
    conversation = nil;
    
    [self greet];
}

- (void) toggleSpeech {
    speech = !speech;
    speechButton.selected = !speech;
}

- (void) toggleDeviceVoice {
    deviceVoice = !deviceVoice;
}

- (void) toggleVideo {
    video = !video;
}

- (void) toggleFlag {
    flagButton.selected = !flagButton.selected;
}

- (void) toggleCorrection {
    correctionButton.selected = !correctionButton.selected;
}

- (void) propertiesMenu {
    UIActionSheet* menu =
            [[UIActionSheet alloc] initWithTitle: nil
                                        delegate: self
                                        cancelButtonTitle: @"Cancel"
                                        destructiveButtonTitle: nil
                                        otherButtonTitles:
                                            speech ? @"Mute" : @"Speech",
                                            deviceVoice ? @"Server Voice" : @"Device Voice",
                                            video ? @"Disable Video" : @"Enable Video",
                                            @"Correct response",
                                            @"Flag response as offensive", nil];
    [menu showInView: self.view];
}


- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex {
    switch (buttonIndex) {
        case 0:
            [self toggleSpeech];
            break;
        case 1:
            [self toggleDeviceVoice];
            break;
        case 2:
            [self toggleVideo];
            break;
        case 3:
            [self toggleCorrection];
            break;
        case 4:
            [self toggleFlag];
            break;
        default:
            break;
    }
}

- (void) deviceOrientationDidChangeNotification: (NSNotification*)notification {
    if ([LibreUI isPortrait]) {
        [self.view removeConstraint: imageRight];
        [self.view removeConstraint: imageBottom];
        [self.view removeConstraint: imageHeight];
        [self.view removeConstraint: chatLogLeft];
        [self.view removeConstraint: chatLogTop];
        [self.view removeConstraint: responseViewHeight];
        [self.view removeConstraint: chatLogHeight];
        [self.view removeConstraint: responseViewTop];
        if (imageView.hidden && chatLogTable.hidden) {
            imageHeight = [LibreUI constrain: imageView height: 0 in: self.view];
            chatLogHeight = [LibreUI constrain: chatLogTable height: 0 in: self.view];
            responseViewTop = [LibreUI constrain: responseView top: 62 in: self.view];
        } else if (chatLogTable.hidden) {
            imageBottom = [LibreUI constrain: imageView bottom: -2 to: responseView in: self.view];
            imageRight = [LibreUI constrain: imageView right: 0 in: self.view];
            responseViewHeight = [LibreUI constrain: responseView height: 60 in: self.view];
        } else if (imageView.hidden) {
            chatLogTop = [LibreUI constrain: chatLogTable top: 2 to: imageView in: self.view];
            imageHeight = [LibreUI constrain: imageView height: 0 in: self.view];
            chatLogLeft = [LibreUI constrain: chatLogTable left: 0 in: self.view];
            responseViewHeight = [LibreUI constrain: responseView height: 60 in: self.view];
        } else {
            chatLogTop = [LibreUI constrain: chatLogTable top: 2 to: imageView in: self.view];
            imageRight = [LibreUI constrain: imageView right: 0 in: self.view];
            imageHeight = [LibreUI constrain: imageView height: [LibreUI displayHeight] / 2.5 in: self.view];
            chatLogLeft = [LibreUI constrain: chatLogTable left: 0 in: self.view];
            responseViewHeight = [LibreUI constrain: responseView height: 60 in: self.view];
        }
    } else {
        [self.view removeConstraint: imageRight];
        [self.view removeConstraint: imageBottom];
        [self.view removeConstraint: imageHeight];
        [self.view removeConstraint: chatLogLeft];
        [self.view removeConstraint: chatLogTop];
        [self.view removeConstraint: responseViewHeight];
        [self.view removeConstraint: chatLogHeight];
        [self.view removeConstraint: responseViewTop];
        if (imageView.hidden && chatLogTable.hidden) {
            imageHeight = [LibreUI constrain: imageView height: 0 in: self.view];
            chatLogHeight = [LibreUI constrain: chatLogTable height: 0 in: self.view];
            responseViewTop = [LibreUI constrain: responseView top: 62 in: self.view];
        } else if (imageView.hidden) {
            imageHeight = [LibreUI constrain: imageView height: 0 in: self.view];
            chatLogTop = [LibreUI constrain: chatLogTable top: 62 in: self.view];
            chatLogLeft = [LibreUI constrain: chatLogTable left: 2 in: self.view];
            responseViewHeight = [LibreUI constrain: responseView height: 60 in: self.view];
        } else {
            imageBottom = [LibreUI constrain: imageView bottom: -2 to: responseView in: self.view];
            imageHeight = [LibreUI constrain: imageView width: [LibreUI displayHeight] / 2.5 in: self.view];
            chatLogTop = [LibreUI constrain: chatLogTable top: 62 in: self.view];
            chatLogLeft = [LibreUI constrain: chatLogTable left: 2 to: imageView in: self.view];
            responseViewHeight = [LibreUI constrain: responseView height: 60 in: self.view];
        }
    }
    [self.view layoutSubviews];
    player.view.frame = imageView.frame;
    tapView.frame = player.view.frame;
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section {
    return [chatLog count];
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath {
    static NSString* simpleTableIdentifier = @"SimpleTableItem";
    
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle: UITableViewCellStyleDefault reuseIdentifier: simpleTableIdentifier];
        cell.textLabel.font = [UIFont systemFontOfSize:12];
    }
    
    id item = [chatLog objectAtIndex: indexPath.row];
    if ([item isKindOfClass: [LibreChatResponse class]]) {
        LibreChatResponse* response = item;
        cell.textLabel.text = response.message;
        cell.imageView.image = [self botImage];
    } else {
        LibreChatMessage* message = item;
        cell.textLabel.text = message.message;
        cell.imageView.image = [self userImage];
    }
    return cell;
}

- (UIImage*) botImage {
    if (botImage != nil) {
        return botImage;
    }
    botImage = [self.sdk fetchImage: self.instance.avatar];
    if (botImage == nil) {
        return nil;
    }
    botImage = [LibreUI resizeImage: botImage in: CGSizeMake(32, 32)];
    return botImage;
}

- (UIImage*) userImage {
    if (userImage != nil) {
        return userImage;
    }
    if (self.sdk.user == nil) {
        userImage = [self.sdk defaultUserAvatar];
    } else {
        userImage = [self.sdk fetchImage: self.sdk.user.avatar];
    }
    if (userImage == nil) {
        return nil;
    }
    userImage = [LibreUI resizeImage: userImage in: CGSizeMake(32, 32)];
    return userImage;
}

- (void)keyboardWillShow:(NSNotification*)notification
{
    NSDictionary *info = [notification userInfo];
    NSValue *kbFrame = [info objectForKey: UIKeyboardFrameEndUserInfoKey];
    CGRect keyboardFrame = [kbFrame CGRectValue];
    CGFloat height = [LibreUI isPortrait] ? keyboardFrame.size.height : keyboardFrame.size.width;
    CGFloat maxHeight = [LibreUI displayHeight] - height - 300;
    if (maxHeight < 0) {
        maxHeight = 0;
        if (![LibreUI isPortrait] && responseViewHeight != nil) {
            responseViewHeight.constant = 40;
        }
    }
    if (!imageView.hidden) {
        if (maxHeight < imageHeight.constant) {
            imageHeight.constant = maxHeight;
        }
    }
    chatBottom.constant = -height;
    [self.view layoutIfNeeded];
    player.view.frame = imageView.frame;
    tapView.frame = player.view.frame;
}

- (void)keyboardWillBeHidden:(NSNotification*)notification
{
    chatBottom.constant = -2;
    if (responseViewHeight != nil) {
        responseViewHeight.constant = [LibreUI isPortrait] ? 60 : 40;
    }
    if (!imageView.hidden) {
        imageHeight.constant = [LibreUI displayHeight] / 2.5;
    }
    [self.view layoutIfNeeded];
    player.view.frame = imageView.frame;
    tapView.frame = player.view.frame;
}

- (BOOL) textFieldShouldReturn: (UITextField*)textField {
    [textField resignFirstResponder];
    [self sendChat];
    return YES;
}

- (BOOL)gestureRecognizer: (UIGestureRecognizer*) gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer: (UIGestureRecognizer*)otherGestureRecognizer
{
    return YES;
}

- (void) zoomImage {
    chatLogTable.hidden = !chatLogTable.hidden;
    [self deviceOrientationDidChangeNotification: nil];
}

- (void) zoomLog {
    imageView.hidden = !imageView.hidden;
    [self deviceOrientationDidChangeNotification: nil];
}

- (void) zoomResponse {
    if (imageView.hidden && chatLogTable.hidden) {
        imageView.hidden = NO;
        chatLogTable.hidden = NO;
    } else {
        imageView.hidden = YES;
        chatLogTable.hidden = YES;
    }
    [self deviceOrientationDidChangeNotification: nil];
}

- (void) greet {
    LibreChatMessage* message = [LibreChatMessage alloc];
    message.instance = self.instance.id;
    if (speech && !deviceVoice) {
        message.speak = YES;
    }
    LibreChatAction* action = [LibreChatAction alloc];
    action.controller = self;
    [self.sdk chat: message action: action];
}

- (void) leave {
    [[self speechSynthesizer] stopSpeakingAtBoundary: AVSpeechBoundaryImmediate];
    if (actionAudioPlayer != nil) {
        [actionAudioPlayer pause];
    }
    if (poseAudioPlayer != nil) {
        [poseAudioPlayer pause];
    }
    if (conversation == nil) {
        return;
    }
    LibreChatMessage* message = [LibreChatMessage alloc];
    message.disconnect = true;
    message.instance = self.instance.id;
    message.conversation = conversation;
    [self.sdk chat: message action: nil];
}

- (void) sendChat {
    NSString* text = chatText.text;
    if (text == nil || text.length == 0) {
        return;
    }
    
    LibreChatMessage* message = [LibreChatMessage alloc];
    message.instance = self.instance.id;
    message.conversation = conversation;
    message.message = text;
    if (speech && !deviceVoice) {
        message.speak = YES;
    }
    if (flagButton.selected) {
        message.offensive = YES;
    }
    if (correctionButton.selected) {
        message.correction = YES;
    }
    chatText.text = @"";
    
    [chatLog addObject: message];
    [chatLogTable reloadData];
    
    NSIndexPath* ipath = [NSIndexPath indexPathForRow: [chatLog count] - 1 inSection: 0];
    [chatLogTable scrollToRowAtIndexPath: ipath atScrollPosition: UITableViewScrollPositionTop animated: YES];
    
    flagButton.selected = NO;
    correctionButton.selected = NO;
    [self setResponse: @"thinking..."];

    LibreChatAction* action = [LibreChatAction alloc];
    action.controller = self;
    [self.sdk chat: message action: action];
}

- (void) setResponse: (NSString*) html {
    NSMutableString* writer = [NSMutableString string];
    [writer appendString: @"<span style=\"font-size:18px;color:blue\">"];
    [writer appendString: [LibreUI linkHTML: html]];
    [writer appendString: @"</span>"];
    [responseView loadHTMLString: writer baseURL: nil];
}

- (void) clearResponse {
    [self setResponse: @""];
}

- (void) chatResponse: (LibreChatResponse*) response {
    currentResponse = response;
    responseState = @"start";
    if (response.message == nil) {
        conversation = response.conversation;
        UIImage* avatar = [self.sdk fetchImage: response.avatar];
        if (avatar != nil) {
            imageView.image = avatar;
        }
        return;
    }
    
    [self setResponse: response.message];
    if ([LibreUI isPortrait] && (response.message.length > 50)) {
        responseViewHeight.constant = 80;
    } else {
        responseViewHeight.constant = 60;
    }
    [self.view layoutIfNeeded];
    
    conversation = response.conversation;
    [chatLog addObject: response];
    [chatLogTable reloadData];
    
    NSIndexPath* ipath = [NSIndexPath indexPathForRow: [chatLog count] - 1 inSection: 0];
    [chatLogTable scrollToRowAtIndexPath: ipath atScrollPosition: UITableViewScrollPositionTop animated: YES];
    
    if (speech) {
        if (response.avatarActionAudio != nil) {
            if (actionAudioPlayer != nil) {
                [actionAudioPlayer pause];
            }
            NSURL* url = [NSURL URLWithString: [self.sdk getURL: response.avatarActionAudio]];
            actionAudioPlayer = [AVPlayer alloc];
            actionAudioPlayer = [actionAudioPlayer initWithURL: url];
            [actionAudioPlayer play];

        }
        if (response.avatarAudio != nil && response.avatarAudio != poseAudio) {
            if (poseAudioPlayer != nil) {
                [poseAudioPlayer pause];
            }
            poseAudio = response.avatarAudio;
            NSURL* url = [NSURL URLWithString: [self.sdk getURL: response.avatarAudio]];
            poseAudioPlayer = [AVPlayer alloc];
            poseAudioPlayer = [poseAudioPlayer initWithURL: url];
            [poseAudioPlayer play];
            
            poseAudioPlayer.actionAtItemEnd = AVPlayerActionAtItemEndNone;
            
            [[NSNotificationCenter defaultCenter]
                    addObserver: self
                    selector: @selector(repeatAudio:)
                    name: AVPlayerItemDidPlayToEndTimeNotification
                    object: [poseAudioPlayer currentItem]];
        } else if (response.avatarAudio == nil && poseAudio != nil) {
            if (poseAudioPlayer != nil) {
                [poseAudioPlayer pause];
            }
            poseAudio = nil;
        }
    }
    
    if ([response isVideo]) {
        if (!video || [response isWebM]) {
            UIImage* avatar = [self.sdk fetchImage: self.instance.avatar];
            imageView.image = avatar;
            [self speak: response];
            return;
        }
        player.view.frame = imageView.frame;
        tapView.frame = player.view.frame;
        responseState = @"start";
        UIImage* splash = [player thumbnailImageAtTime: 1.0 timeOption: MPMovieTimeOptionNearestKeyFrame];
        imageView.image = splash;
        //[player setContentURL:[NSURL URLWithString: [self.sdk getURL: response.avatar]]];
        ////[player prepareToPlay];
        splash = [player thumbnailImageAtTime: 1.0 timeOption: MPMovieTimeOptionNearestKeyFrame];
        //imageView.image = splash;
        
        if (response.avatarAction != nil) {
            responseState = @"action";
            [player setContentURL:[NSURL URLWithString: [self.sdk getURL: response.avatarAction]]];
            player.view.frame = imageView.frame;
            player.shouldAutoplay = YES;
            player.repeatMode = NO;
            [self playVideo];
        } else if (response.avatarTalk != nil) {
            responseState = @"talk";
            [player setContentURL:[NSURL URLWithString: [self.sdk getURL: response.avatarTalk]]];
            player.view.frame = imageView.frame;
            player.shouldAutoplay = YES;
            player.repeatMode = YES;
            [self playVideo];
        } else {
            responseState = @"pose";
            [player setContentURL:[NSURL URLWithString: [self.sdk getURL: response.avatar]]];
            player.view.frame = imageView.frame;
            player.shouldAutoplay = YES;
            player.repeatMode = YES;
            [self playVideo];
            [self speak: response];
        }
    } else {
        UIImage* avatar = [self.sdk fetchImage: response.avatar];
        imageView.image = avatar;
        [self speak: response];
    }
}

- (void) repeatAudio: (NSNotification*) notification {
    AVPlayerItem* avPlayer = [notification object];
    [avPlayer seekToTime: kCMTimeZero];
}

- (void) speak: (LibreChatResponse*)response {
    if (!speech || response.message == nil || response.message.length == 0) {
        return;
    }
    if (!deviceVoice) {
        if (response.speech != nil && response.speech.length > 0) {
            if (speechAudioPlayer != nil) {
                [speechAudioPlayer pause];
            }
            NSURL* url = [NSURL URLWithString: [self.sdk getURL: response.speech]];
            speechAudioPlayer = [AVPlayer alloc];
            speechAudioPlayer = [speechAudioPlayer initWithURL: url];
            
            [[NSNotificationCenter defaultCenter]
                 addObserver: self
                 selector: @selector(stopTalking)
                 name: AVPlayerItemDidPlayToEndTimeNotification
                 object: [speechAudioPlayer currentItem]];
            
            [speechAudioPlayer play];
        }
    } else {
        AVSpeechUtterance* utterance = [AVSpeechUtterance speechUtteranceWithString: response.message];
        utterance.rate = 0.20;
        utterance.voice = [self voice];
        AVSpeechSynthesizer* synthesizer = [self speechSynthesizer];
        [synthesizer stopSpeakingAtBoundary: AVSpeechBoundaryImmediate];
        [synthesizer speakUtterance: utterance];
    }
}

- (void) stopTalking {
    if (video && [currentResponse isVideo]) {
        responseState = @"pose";
        [player setContentURL:[NSURL URLWithString: [self.sdk getURL: currentResponse.avatar]]];
        player.view.frame = imageView.frame;
        player.shouldAutoplay = YES;
        player.repeatMode = YES;
        [self playVideo];
    }
}

- (void) speechSynthesizer: (AVSpeechSynthesizer*)synthesizer didFinishSpeechUtterance: (AVSpeechUtterance*)utterance {
    [self stopTalking];
}

- (void) playVideo {
    [player prepareToPlay];
}


- (void) videoFinished: (NSNotification*)notification {
    if ([responseState isEqualToString: @"action"]) {
        NSString* url = currentResponse.avatarTalk;
        responseState = @"talk";
        if (currentResponse.avatarTalk == nil) {
            url = currentResponse.avatar;
            responseState = @"pose";
            [self speak: currentResponse];
        }
        [player setContentURL:[NSURL URLWithString: [self.sdk getURL: url]]];
        player.view.frame = imageView.frame;
        player.shouldAutoplay = YES;
        player.repeatMode = YES;
        [self playVideo];
    }
}

- (void) videoStatus: (NSNotification*)notification {
    if (player.loadState & (MPMovieLoadStatePlaythroughOK)) {
        [player play];
        if ([responseState isEqualToString: @"talk"]) {
            responseState = @"pose";
            [self speak: currentResponse];
        }
    }
}

- (AVSpeechSynthesisVoice*) voice {
    if (voice == nil) {
        voice = [AVSpeechSynthesisVoice voiceWithLanguage:@"en-US"];
    }
    return voice;
}

- (AVSpeechSynthesizer*) speechSynthesizer {
    if (speechSynthesizer == nil) {
        speechSynthesizer = [[AVSpeechSynthesizer alloc] init];
    }
    speechSynthesizer.delegate = self;
    return speechSynthesizer;
}

@end
