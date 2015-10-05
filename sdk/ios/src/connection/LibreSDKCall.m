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

#import "LibreSDKCall.h"
#import "LibreAction.h"
#import "LibreConfig.h"

@implementation LibreSDKCall

- (void) get: (NSString*) url {
    if (self.debug) {
        NSLog(@"GET: %@", url);
    }
    NSURL *restURL = [NSURL URLWithString: url];
    NSMutableURLRequest *restRequest = [NSMutableURLRequest requestWithURL:restURL];
    [restRequest setHTTPMethod:@"GET"];
    
    if (self.currentConnection) {
        [self.currentConnection cancel];
        self.currentConnection = nil;
    }
    
    self.responseData = [NSMutableData data];
    self.error = false;
    self.currentConnection = [[NSURLConnection alloc] initWithRequest: restRequest delegate: self];
}

- (void) post: (NSString*) url data: (NSString*) xml {
    if (self.debug) {
        NSLog(@"POST: %@", url);
        NSLog(@"XML: %@", xml);
    }
    NSURL *restURL = [NSURL URLWithString: url];
    NSMutableURLRequest *restRequest = [NSMutableURLRequest requestWithURL:restURL];
    [restRequest setHTTPMethod:@"POST"];
    NSData *data = [xml dataUsingEncoding: NSUTF8StringEncoding];
    [restRequest setHTTPBody: data];
    [restRequest setValue:@"application/xml" forHTTPHeaderField: @"Content-Type"];
    [restRequest setValue:[NSString stringWithFormat:@"%ld", (long)[data length]] forHTTPHeaderField:@"Content-Length"];
    
    if (self.currentConnection) {
        [self.currentConnection cancel];
        self.currentConnection = nil;
    }
    
    self.responseData = [NSMutableData data];
    self.error = false;
    self.self.currentConnection = [[NSURLConnection alloc] initWithRequest: restRequest delegate: self];
}

- (void) post: (NSString*) url image: (UIImage*)image data: (NSString*) xml {
    if (self.debug) {
        NSLog(@"POST: %@", url);
        NSLog(@"IMAGE: %@", image);
        NSLog(@"XML: %@", xml);
    }
    NSURL *restURL = [NSURL URLWithString: url];
    NSMutableURLRequest *restRequest = [NSMutableURLRequest requestWithURL:restURL];
    [restRequest setHTTPMethod:@"POST"];
    
    NSString *boundary = @"0xKhTmLbOuNdArY";
    NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary];
    [restRequest addValue:contentType forHTTPHeaderField: @"Content-Type"];
    
    NSMutableData *body = [NSMutableData data];
    
    [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",boundary] dataUsingEncoding: NSUTF8StringEncoding]];
    
    [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"file\"; filename=\"%@.jpg\"\r\n", @"image"] dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[@"Content-Type: application/octet-stream\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[NSData dataWithData: UIImageJPEGRepresentation(image, 90)]];
    
    [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    
    [body appendData:[@"Content-Disposition: form-data; name=\"xml\"\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
    [body appendData:[xml dataUsingEncoding: NSUTF8StringEncoding]];

    [body appendData:[[NSString stringWithFormat:@"\r\n--%@--\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
    
    [restRequest setHTTPBody:body];
    [restRequest addValue:[NSString stringWithFormat:@"%d", (int)[body length]] forHTTPHeaderField: @"Content-Length"];
   
    if (self.currentConnection) {
        [self.currentConnection cancel];
        self.currentConnection = nil;
    }
    
    self.responseData = [NSMutableData data];
    self.error = false;
    self.currentConnection = [[NSURLConnection alloc] initWithRequest: restRequest delegate: self];
}

- (void)connection:(NSURLConnection*)connection didReceiveResponse:(NSURLResponse *)response {
    [self.responseData setLength:0];
    NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
    long code = [httpResponse statusCode];
    if (code != 200 && code != 204) {
        NSLog(@"self.error:%ld", code);
        self.error = true;
    }
}

- (void)connection:(NSURLConnection*)connection didReceiveData:(NSData*)data {
    if (self.debug) {
        NSLog(@"RESPONSE: %@", [[NSString alloc] initWithData: data encoding: NSUTF8StringEncoding]);
    }
    [self.responseData appendData: data];
}

- (void)connection:(NSURLConnection*)connection didFailWithError:(NSError*)exception {
    if (self.error) {
        [self.currentAction error: [exception localizedDescription]];
    }
    self.currentConnection = nil;
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    if (self.debug) {
        NSLog(@"FINISHED %@", self.currentAction);
    }
    if (self.currentAction == nil) {
        return;
    }
    if (self.error) {
        [self.currentAction error: [[NSString alloc] initWithData: self.responseData encoding: NSUTF8StringEncoding]];
    } else if (self.responseConfig != nil) {
        self.responseConfig.responseAction = self.currentAction;
        [self.responseConfig parseXML: self.responseData];
    } else {
        [self.currentAction finished];
    }
    [self.currentConnection cancel];
    self.currentConnection = nil;
    self.currentAction = nil;
    self.responseConfig = nil;
}

@end
