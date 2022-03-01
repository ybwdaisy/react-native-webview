//
//  RNCWebViewBridge.m
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/6/30.
//

#import "RNCWebViewBridge.h"
#import "RNCAsyncStorage.h"
#import "WebViewJavascriptBridge.h"
#import "RNCBridgeResponse.h"

#define SHARE_CACHE_DIR @"shareCache"

@implementation RNCWebViewBridge

typedef void (^MessageCallback)(NSMutableDictionary *data);

+ (instancetype)bridgeForWebView:(id)webView callback:(MessageCallback)callback {
    NSMutableArray *base64Array = [NSMutableArray arrayWithCapacity:0];
    WebViewJavascriptBridge *bridge = [WebViewJavascriptBridge bridgeForWebView:webView];
    // 获取登录token
    [bridge registerHandler:@"getToken" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self getStorageByKey:@"token" callback:^(NSDictionary *data) {
            RNCBridgeResponse *response = [[RNCBridgeResponse alloc]init];
            [response setStatus:ResponseStatusSuccess];
            [response setMsg:@"getToken:ok"];
            [response setData:data];
            responseCallback([response getProperties]);
        }];
    }];
    // 获取当前选中唾液盒
    [bridge registerHandler:@"getBoxInfo" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self getStorageByKey:@"box" callback:^(NSDictionary *data) {
            RNCBridgeResponse *response = [[RNCBridgeResponse alloc]init];
            [response setStatus:ResponseStatusSuccess];
            [response setMsg:@"getBoxInfo:ok"];
            [response setData:data];
            responseCallback([response getProperties]);
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
    // 微信授权
    [bridge registerHandler:@"wechatAuth" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"wechatAuth" widthData:data andType:MessageTypeWechatAuth messageCallback:^(NSMutableDictionary *data) {
            if (callback) {
                callback(data);
            }
        } responseCallback:^(id responseData) {
            responseCallback(responseData);
        }];
    }];
    // 分享弹窗
    [bridge registerHandler:@"share" handler:^(id data, WVJBResponseCallback responseCallback) {
        [self handleMessage:@"share" widthData:data andType:MessageTypeShare messageCallback:^(NSMutableDictionary *data) {
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
        RNCBridgeResponse *response = [[RNCBridgeResponse alloc]init];
        [response setStatus:ResponseStatusSuccess];
        [response setMsg:@"getDeviceInfo:ok"];
        [response setData:event];
        responseCallback([response getProperties]);
    }];
    
    // 分片上传 Base64 图片
    [bridge registerHandler:@"uploadBase64Image" handler:^(id data, WVJBResponseCallback responseCallback) {
        if (data == nil) {
            return;
        }
        NSString *total = [data objectForKey:@"length"];
        NSInteger totalCount = [total intValue];
        NSInteger arrayCount = [base64Array count];
        if (arrayCount < totalCount - 1) {
            [base64Array addObject:data];
            RNCBridgeResponse *response = [[RNCBridgeResponse alloc]init];
            [response setStatus:ResponseStatusSuccess];
            [response setMsg:@"uploadBase64Image:ok"];
            [response setData:nil];
            responseCallback([response getProperties]);
            return;
        }
        [base64Array addObject:data];
        RNCBridgeResponse *response = [[RNCBridgeResponse alloc]init];
        [response setStatus:ResponseStatusSuccess];
        [response setMsg:@"uploadBase64Image:ok"];
        [response setData:nil];
        responseCallback([response getProperties]);
        
        NSString *base64String = @"";
        for(NSMutableDictionary *arrayItem in base64Array) {
            NSString *fragment = [arrayItem objectForKey:@"fragment"];
            base64String = [base64String stringByAppendingString:fragment];
        }
        NSString *imagePath = [self saveBase64ImgToLocal:base64String];
        
        NSMutableDictionary *responseData = [[NSMutableDictionary alloc]init];
        [responseData setValue:imagePath forKey:@"imagePath"];
        
        NSMutableDictionary *result = [self createEventData:MessageTypeLocalImagePath withData:responseData];
        if (callback) {
            callback(result);
        }
        [base64Array removeAllObjects];

    }];
    
    return bridge;
}

+ (NSMutableDictionary *)handleCallJavaScriptMethod:(NSString *)handlerName data:(id)data {
    // TODO
    return nil;
}

+ (NSString *)saveBase64ImgToLocal:(NSString *)base64String {
    if (base64String != nil) {
        NSURL *url = [NSURL URLWithString:base64String];
        NSData *imageData = [NSData dataWithContentsOfURL:url];
        UIImage *image = [UIImage imageWithData:imageData];
        NSString *imagePath = [self saveImageToCacheUseImage:image];
        return imagePath;
    }
    return @"";
}

+ (void)getStorageByKey:(NSString *)key callback:(void(^)(NSDictionary *data))callback {
    RNCAsyncStorage *storage = [[RNCAsyncStorage alloc] init];
    dispatch_sync(storage.methodQueue, ^{
        [storage multiGet:@[@"persist:primary"] callback:^(NSArray *response) {
            if (response[0] == NSNull.null && [response[1] isKindOfClass:NSArray.class] && [response[1][0] isKindOfClass:NSArray.class]) {
                NSData *jsonData = [response[1][0][1] dataUsingEncoding:NSUTF8StringEncoding];
                NSDictionary *parsedData = [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:nil];
                NSString *targetJsonString = [parsedData objectForKey:key];
                if (targetJsonString != nil) {
                    NSData *targetData = [targetJsonString dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary *targetParsedData = [NSJSONSerialization JSONObjectWithData:targetData options:kNilOptions error:nil];
                    callback(targetParsedData);
                } else {
                    callback(nil);
                }
            } else {
                callback(nil);
            }
        }];
    });
}

+ (NSMutableDictionary *)createEventData:(MessageType)type withData:(id)data {
    NSMutableDictionary *result = [[NSMutableDictionary alloc]init];
    NSMutableDictionary *event = [[NSMutableDictionary alloc]init];
    [event setValue:[self convertMessageType:type] forKey:@"type"];
    [event setValue:data forKey:@"data"];
    [result setObject:event forKey:@"data"];
    return result;
}

+ (void)handleMessage:(NSString*)name widthData:(id)data andType:(MessageType)type messageCallback:(MessageCallback)messageCallback responseCallback:(WVJBResponseCallback)responseCallback {
    NSMutableDictionary *result = [self createEventData:type withData:data];
    if (messageCallback) {
        messageCallback(result);
    }
    if (responseCallback) {
        RNCBridgeResponse *response = [[RNCBridgeResponse alloc]init];
        [response setStatus:ResponseStatusSuccess];
        [response setMsg:[name stringByAppendingString:@":ok"]];
        [response setData:data];
        responseCallback([response getProperties]);
    }
}

+ (NSString *)saveImageToCacheUseImage:(UIImage *)image {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *documentPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    NSString *cachePath = [documentPath stringByAppendingPathComponent:SHARE_CACHE_DIR];
    if (![fileManager fileExistsAtPath:cachePath]) {
        [fileManager createDirectoryAtPath:cachePath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    NSDate *currentDate = [NSDate dateWithTimeIntervalSinceNow:0];
    NSTimeInterval currentTime = [currentDate timeIntervalSince1970] * 1000;
    NSString *timeString = [NSString stringWithFormat:@"%.0f", currentTime];
    NSString *imagePath = [NSString stringWithFormat:@"/share_image_%@.png", timeString];
    NSString *imageAbsolutePath = [cachePath stringByAppendingPathComponent:imagePath];
    BOOL result = [UIImagePNGRepresentation(image) writeToFile:imageAbsolutePath atomically:YES];
    if (result) {
        return imageAbsolutePath;
    }
    return @"";
}

+ (NSString *)convertMessageType:(MessageType) type {
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
        case MessageTypeWechatAuth:
            return @"wechatAuth";
        case MessageTypeShare:
            return @"share";
        case MessageTypeShareSession:
            return @"shareSession";
        case MessageTypeShareTimeline:
            return @"shareTimeline";
        case MessageTypeShareFeed:
            return @"shareFeed";
        case MessageTypeLocalImagePath:
            return @"localImagePath";
        default:
            return @"";
    }
}

@end;
