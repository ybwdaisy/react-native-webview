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

- (instancetype)bridgeForWebView:(id)webView callback:(void(^)(NSMutableDictionary *data))callback {
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
        NSMutableDictionary<NSString *, id> *event = [[NSMutableDictionary alloc]init];
        [event addEntriesFromDictionary: @{@"type": @"open"}];
        if (data) {
            [event addEntriesFromDictionary: @{@"data": data}];
        }
        if (callback) {
            callback(event);
        }
        responseCallback(@{
          @"status": @0,
          @"msg": @"openPage:ok",
          @"data": event,
        });
    }];
    // 关闭浏览器窗口
    [bridge registerHandler:@"closeWindow" handler:^(id data, WVJBResponseCallback responseCallback) {
        NSMutableDictionary<NSString *, id> *event = [[NSMutableDictionary alloc]init];
        [event addEntriesFromDictionary: @{@"type": @"close"}];
        if (data) {
            [event addEntriesFromDictionary: @{@"data": data}];
        }
        if (callback) {
            callback(event);
        }
        responseCallback(@{
          @"status": @0,
          @"msg": @"closeWindow:ok",
          @"data": event,
        });
    }];
    // 配置导航
    [bridge registerHandler:@"setNavConfig" handler:^(id data, WVJBResponseCallback responseCallback) {
        NSMutableDictionary<NSString *, id> *event = [[NSMutableDictionary alloc]init];
        [event addEntriesFromDictionary: @{@"type": @"close"}];
        if (data) {
          [event addEntriesFromDictionary: @{@"data": data}];
        }
        if (callback) {
            callback(event);
        }
        responseCallback(@{
          @"status": @0,
          @"msg": @"setNavConfig:ok",
          @"data": event,
        });
    }];
    // 获取设备信息
    [bridge registerHandler:@"getDeviceInfo" handler:^(id data, WVJBResponseCallback responseCallback) {
        NSMutableDictionary<NSString *, id> *event = [[NSMutableDictionary alloc]init];
        CGFloat width = [UIScreen mainScreen].bounds.size.width;
        CGFloat height = [UIScreen mainScreen].bounds.size.height;
        [event addEntriesFromDictionary: @{@"width": [NSNumber numberWithInt:width]}];
        [event addEntriesFromDictionary: @{@"height": [NSNumber numberWithInt:height]}];
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

@end;
