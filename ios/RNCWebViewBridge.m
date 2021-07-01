//
//  RNCWebViewBridge.m
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/6/30.
//

#import "RNCWebViewBridge.h"
#import "RNCAsyncStorage.h"
#import "WebViewJavascriptBridge.h"


@implementation RNCWebViewBridge

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
    MessageTypeShareFeed
} MessageType;

typedef void (^MessageCallback)(NSMutableDictionary *data);

- (instancetype)bridgeForWebView:(id)webView callback:(MessageCallback)callback {
    WebViewJavascriptBridge *bridge = [WebViewJavascriptBridge bridgeForWebView:webView];
    // 获取登录token
    [bridge registerHandler:@"getToken" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self getStorageByKey:@"token" callback:^(NSDictionary * _Nonnull data) {
            responseCallback(@{
                @"status": @0,
                @"msg": @"getBoxInfo:ok",
                @"data": data,
            });
        }];
    }];
    // 获取当前选中唾液盒
    [bridge registerHandler:@"getBoxInfo" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self getStorageByKey:@"box" callback:^(NSDictionary * _Nonnull data) {
            responseCallback(@{
                @"status": @0,
                @"msg": @"getBoxInfo:ok",
                @"data": data,
            });
        }];
    }];
    // 打开RN页面
    [bridge registerHandler:@"openPage" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"openPage" widthData:data andType:MessageTypeOpen messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 关闭浏览器窗口
    [bridge registerHandler:@"closeWindow" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"closeWindow" widthData:data andType:MessageTypeClose messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 配置导航
    [bridge registerHandler:@"setNavConfig" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"setNavConfig" widthData:data andType:MessageTypeNavConfig messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 配置分享到微信信息，如果不调用 updateSessionShareData 和 updateTimelineShareData 方法会取此配置
    [bridge registerHandler:@"setShareData" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"setShareData" widthData:data andType:MessageTypeShareData messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 配置分享到微信会话信息
    [bridge registerHandler:@"updateSessionShareData" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"updateSessionShareData" widthData:data andType:MessageTypeSessionShareData messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 配置分享到微信朋友圈信息
    [bridge registerHandler:@"updateTimelineShareData" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"updateTimelineShareData" widthData:data andType:MessageTypeTimelineShareData messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 配置分享到社区信息
    [bridge registerHandler:@"updateFeedShareData" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"updateFeedShareData" widthData:data andType:MessageTypeFeedShareData messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 分享到微信会话
    [bridge registerHandler:@"shareToSession" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"shareToSession" widthData:data andType:MessageTypeShareSession messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 分享到微信朋友圈
    [bridge registerHandler:@"shareToTimeline" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"shareToTimeline" widthData:data andType:MessageTypeShareTimeline messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 分享到社区
    [bridge registerHandler:@"shareToFeed" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"shareToFeed" widthData:data andType:MessageTypeShareFeed messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];

    // 获取设备信息
    [bridge registerHandler:@"getDeviceInfo" handler:^(id data, WVJBResponseCallback responseCallback) {
        NSMutableDictionary<NSString *, id> *event = [[NSMutableDictionary alloc]init];
        CGFloat width = [UIScreen mainScreen].bounds.size.width;
        CGFloat height = [UIScreen mainScreen].bounds.size.height;
        [event addEntriesFromDictionary: @{@"width": [NSNumber numberWithInt:width]}];
        [event addEntriesFromDictionary: @{@"height": [NSNumber numberWithInt:height]}];
        if (@available(iOS 11.0, *)) {
            UIEdgeInsets safeAreaInsets = [UIApplication sharedApplication].keyWindow.safeAreaInsets;
            [event addEntriesFromDictionary:@{@"safeAreaInsets": @{
                @"top": [NSNumber numberWithInt:(int)safeAreaInsets.top],
                @"right": [NSNumber numberWithInt:(int)safeAreaInsets.right],
                @"bottom": [NSNumber numberWithInt:(int)safeAreaInsets.bottom],
                @"left": [NSNumber numberWithInt:(int)safeAreaInsets.left]
            }}];
        }
        [event addEntriesFromDictionary:@{@"version": [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]}];
        responseCallback(@{
          @"status": @0,
          @"msg": @"getDeviceInfo:ok",
          @"data": event,
        });
    }];
    
    return bridge;
}

- (void)getStorageByKey:(NSString *)key callback:(void(^)(NSDictionary *data))callback {
    RNCAsyncStorage *storage = [[RNCAsyncStorage alloc] init];
    dispatch_sync(storage.methodQueue, ^{
        [storage multiGet:@[@"persist:primary"] callback:^(NSArray *response) {
            if (response[0] == NSNull.null && [response[1] isKindOfClass:NSArray.class] && [response[1][0] isKindOfClass:NSArray.class]) {
                NSData *jsonData = [response[1][0][1] dataUsingEncoding:NSUTF8StringEncoding];
                NSDictionary *parsedData = [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:nil];
                callback([parsedData objectForKey:key]);
            } else {
                callback(@{});
            }
        }];
    });
}

- (void)handleMessage:(NSString*)name widthData:(id)data andType:(MessageType)type messageCallback:(MessageCallback)messageCallback responseCallback:(WVJBResponseCallback)responseCallback {
    NSMutableDictionary *event = [[NSMutableDictionary alloc]init];
    [event addEntriesFromDictionary: @{@"type": [self convertToString:type]}];
    if (data) {
        [event addEntriesFromDictionary: @{@"data": data}];
    }
    if (messageCallback) {
        messageCallback(event);
    }
    if (responseCallback) {
        responseCallback(@{
            @"status": @0,
            @"msg": [name stringByAppendingString:@":ok"],
            @"data": event,
        });
    }
}

- (NSString *)convertToString:(MessageType) type {
    switch (type) {
        case MessageTypeOpen:
            return @"open";
        case MessageTypeClose:
            return @"close";
        case MessageTypeNavConfig:
            return @"navConfig";
        case MessageTypeShareData:
            return @"shareData";
        case MessageTypeSessionShareData:
            return @"sessionShareData";
        case MessageTypeTimelineShareData:
            return @"timelineShareData";
        case MessageTypeFeedShareData:
            return @"feedShareData";
        case MessageTypeShareSession:
            return @"shareSession";
        case MessageTypeShareTimeline:
            return @"shareTimeline";
        case MessageTypeShareFeed:
            return @"shareFeed";
        default:
            return @"";
    }
}

@end;
