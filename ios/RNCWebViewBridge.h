//
//  RNCWebViewBridge.h
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/6/30.
//

#import <Foundation/Foundation.h>

@interface RNCWebViewBridge : NSObject

+ (instancetype)bridgeForWebView:(id)webView callback:(void(^)(NSMutableDictionary *data))callback;

@end
