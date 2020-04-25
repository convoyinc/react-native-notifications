import { EmitterSubscription } from 'react-native';
import { NativeEventsReceiver } from '../adapters/NativeEventsReceiver';
import { Registered, RegistrationError } from '../interfaces/NotificationEvents';
import { CompletionCallbackWrapper } from '../adapters/CompletionCallbackWrapper';
import { Notification } from '../DTO/Notification';
import { NotificationCompletion, NotificationBackgroundFetchResult } from '../interfaces/NotificationCompletion';
import { NotificationActionResponse } from '../interfaces/NotificationActionResponse';
export declare class EventsRegistry {
    private nativeEventsReceiver;
    private completionCallbackWrapper;
    constructor(nativeEventsReceiver: NativeEventsReceiver, completionCallbackWrapper: CompletionCallbackWrapper);
    registerRemoteNotificationsRegistered(callback: (event: Registered) => void): EmitterSubscription;
    registerNotificationReceivedForeground(callback: (notification: Notification, completion: (response: NotificationCompletion) => void) => void): EmitterSubscription;
    registerNotificationReceivedBackground(callback: (notification: Notification, completion: (response: NotificationBackgroundFetchResult) => void) => void): EmitterSubscription;
    registerNotificationOpened(callback: (notification: Notification, completion: () => void, action?: NotificationActionResponse) => void): EmitterSubscription;
    registerRemoteNotificationsRegistrationFailed(callback: (event: RegistrationError) => void): EmitterSubscription;
}
