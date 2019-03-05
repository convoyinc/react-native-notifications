@import UIKit;

#import <PushKit/PushKit.h>

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#if __has_include(<React/RCTConvert.h>)
#import <React/RCTConvert.h>
#elif __has_include("React/RCTConvert.h")
#import "React/RCTConvert.h"
#else
#import "RCTConvert.h"
#endif

@interface RCTConvert (UILocalNotification)
+ (UILocalNotification *)UILocalNotification:(id)json;
@end

@interface RCTConvert (UIBackgroundFetchResult)
+(UIBackgroundFetchResult *)UIBackgroundFetchResult:(id)json;
@end

@interface RNNotifications : NSObject <RCTBridgeModule>

typedef void (^RCTRemoteNotificationCallback)(UIBackgroundFetchResult result);

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
+ (void)didFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
+ (void)didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings;
+ (void)didUpdatePushCredentials:(PKPushCredentials *)credentials forType:(NSString *)type;

+ (void)didReceiveRemoteNotification:(NSDictionary *)notification;
+ (void)didReceiveRemoteNotification:(NSDictionary *)notification fetchCompletionHandler:(RCTRemoteNotificationCallback)completionHandler;
+ (void)didReceiveLocalNotification:(UILocalNotification *)notification;

+ (void)handleActionWithIdentifier:(NSString *)identifier forRemoteNotification:(NSDictionary *)userInfo withResponseInfo:(NSDictionary *)responseInfo completionHandler:(void (^)())completionHandler;
+ (void)handleActionWithIdentifier:(NSString *)identifier forLocalNotification:(UILocalNotification *)notification withResponseInfo:(NSDictionary *)responseInfo completionHandler:(void (^)())completionHandler;

@end
