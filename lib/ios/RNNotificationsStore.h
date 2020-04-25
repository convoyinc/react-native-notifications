#import <Foundation/Foundation.h>
@import UserNotifications;

@interface RNNotificationsStore : NSObject

+ (instancetype)sharedInstance;

- (void)completeAction:(NSString *)completionKey;
- (void)completePresentation:(NSString *)completionKey withPresentationOptions:(UNNotificationPresentationOptions)presentationOptions;
- (void)completeBackgroundAction:(NSString *)completionKey withBackgroundFetchResult:(UIBackgroundFetchResult)backgroundFetchResult;
- (void)setActionCompletionHandler:(void (^)(void))completionHandler withCompletionKey:(NSString *)completionKey;
- (void)setPresentationCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler withCompletionKey:(NSString *)completionKey;
- (void)setBackgroundActionCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler withCompletionKey:(NSString *)completionKey;
- (void)setInitialNotification:(NSDictionary *)initialNotification withFetchCompletionHandler:(void (^)(UIBackgroundFetchResult))fetchCompletionHandler;

- (NSDictionary *)getInitialNotification;
- (void (^)(void))getActionCompletionHandler:(NSString *)key;
- (void (^)(UNNotificationPresentationOptions))getPresentationCompletionHandler:(NSString *)key;

@end
