//
//  RNCWebViewBridge.h
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/6/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNCWebViewBridge : NSObject

+ (instancetype)bridgeForWebView:(id)webView callback:(void(^)(NSMutableDictionary *data))callback;

@end

NS_ASSUME_NONNULL_END
