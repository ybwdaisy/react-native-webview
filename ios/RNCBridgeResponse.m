//
//  RNCBridgeResponse.m
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/8/17.
//

#import "RNCBridgeResponse.h"

@implementation RNCBridgeResponse

- (NSMutableDictionary *)getProperties {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc]init];
    [dic setValue:[NSNumber numberWithInt:self.status] forKey:@"status"];
    [dic setValue:self.msg forKey:@"msg"];
    [dic setValue:self.data forKey:@"data"];
    return dic;
}

@end
