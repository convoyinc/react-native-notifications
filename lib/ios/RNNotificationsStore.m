#import "RNNotificationsStore.h"

@interface RNNotificationsStore()

@property (nonatomic, retain) NSDictionary* initialNotification;
@property (nonatomic, copy) SimpleBlock initialNotificationFetchCompletionHandler;

@end

@implementation RNNotificationsStore
NSMutableDictionary* _actionCompletionHandlers;
NSMutableDictionary* _presentationCompletionHandlers;

+ (instancetype)sharedInstance {
    static RNNotificationsStore *sharedInstance = nil;
    static dispatch_once_t onceToken;
    
    dispatch_once(&onceToken, ^{
        sharedInstance = [[RNNotificationsStore alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init {
    self = [super init];
    _actionCompletionHandlers = [NSMutableDictionary new];
    _presentationCompletionHandlers = [NSMutableDictionary new];
    return self;
}

- (void)setActionCompletionHandler:(void (^)(void))completionHandler withCompletionKey:(NSString *)completionKey {
    _actionCompletionHandlers[completionKey] = completionHandler;
}

- (void)setPresentationCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler withCompletionKey:(NSString *)completionKey {
    _presentationCompletionHandlers[completionKey] = completionHandler;
}

- (void)setInitialNotification:(NSDictionary *)initialNotification fetchCompletionHandler:(SimpleBlock)fetchCompletionHandler {
    self.initialNotification = initialNotification;
    self.initialNotificationFetchCompletionHandler = fetchCompletionHandler;
}

- (NSDictionary *)getInitialNotification {
    self.jsIsReady = YES;

    if (self.initialNotification && self.initialNotificationFetchCompletionHandler) {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.initialNotificationFetchCompletionHandler();
        });
    }

    NSDictionary *initialNotification = self.initialNotification;
    self.initialNotification = nil;
    return initialNotification;
}

- (void (^)(void))getActionCompletionHandler:(NSString *)key {
    return _actionCompletionHandlers[key];
}

- (void (^)(UNNotificationPresentationOptions))getPresentationCompletionHandler:(NSString *)key {
    return _presentationCompletionHandlers[key];
}

- (void)completeAction:(NSString *)completionKey {
    void (^completionHandler)() = (void (^)())[_actionCompletionHandlers valueForKey:completionKey];
    if (completionHandler) {
        completionHandler();
        [_actionCompletionHandlers removeObjectForKey:completionKey];
    }
}

- (void)completePresentation:(NSString *)completionKey withPresentationOptions:(UNNotificationPresentationOptions)presentationOptions {
    void (^completionHandler)() = (void (^)(UNNotificationPresentationOptions))[_presentationCompletionHandlers valueForKey:completionKey];
    if (completionHandler) {
        completionHandler(presentationOptions);
        [_presentationCompletionHandlers removeObjectForKey:completionKey];
    }
}

@end
