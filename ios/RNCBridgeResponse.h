//
//  RNCBridgeResponse.h
//  react-native-webview
//
//  Created by ybw-macbook-pro on 2021/8/17.
//

#import <Foundation/Foundation.h>

typedef enum {
    ResponseStatusSuccess = 0,
    ResponseStatusCancel = 1,
    ResponseStatusFail = -1
} ResponseStatus;

@interface RNCBridgeResponse : NSObject

@property (nonatomic, assign) ResponseStatus status;
@property (nonatomic, strong) NSString *msg;
@property (nonatomic, strong) id data;

- (NSMutableDictionary *)getProperties;

@end
