//
//  RNCWebViewBridge.h
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/6/30.
//

#import <Foundation/Foundation.h>

typedef enum {
    MessageTypeOpen,
    MessageTypeClose,
    MessageTypeNavConfig,
    MessageTypeShareData,
    MessageTypeSessionShareData,
    MessageTypeTimelineShareData,
    MessageTypeFeedShareData,
    MessageTypeShareSession,
    MessageTypeShareTimeline,
    MessageTypeShareFeed,
    MessageTypeLocalImagePath,
} MessageType;

@interface RNCWebViewBridge : NSObject

+ (instancetype)bridgeForWebView:(id)webView callback:(void(^)(NSMutableDictionary *data))callback;

+ (NSMutableDictionary *)handleCallJavaScriptMethod:(NSString *)handlerName data:(id)data;

@end
