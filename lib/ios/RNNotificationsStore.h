#import <Foundation/Foundation.h>
@import UserNotifications;

typedef void (^SimpleBlock)();

@interface RNNotificationsStore : NSObject

@property (nonatomic, assign) BOOL hasInitialNotificationBeenFetched;

+ (instancetype)sharedInstance;

- (void)completeAction:(NSString *)completionKey;
- (void)completePresentation:(NSString *)completionKey withPresentationOptions:(UNNotificationPresentationOptions)presentationOptions;
- (void)setActionCompletionHandler:(void (^)(void))completionHandler withCompletionKey:(NSString *)completionKey;
- (void)setPresentationCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler withCompletionKey:(NSString *)completionKey;
- (void)setInitialNotification:(NSDictionary *)initialNotification fetchCompletionHandler:(SimpleBlock)fetchCompletionHandler;

- (NSDictionary *)getInitialNotification;
- (void (^)(void))getActionCompletionHandler:(NSString *)key;
- (void (^)(UNNotificationPresentationOptions))getPresentationCompletionHandler:(NSString *)key;

@end
