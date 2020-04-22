import { NativeCommandsSender } from './NativeCommandsSender';
import { NotificationCompletion } from '../interfaces/NotificationCompletion';
import { Platform, AppState } from 'react-native';
import {NotificationIOS} from "../DTO/NotificationIOS";
import {Notification} from "..";
import { NotificationActionResponse } from '../interfaces/NotificationActionResponse';

export class CompletionCallbackWrapper {
  constructor(
    private readonly nativeCommandsSender: NativeCommandsSender
  ) {}

  public wrapReceivedBackgroundCallback(callback: Function): (notification: Notification) => void {
    return (notification) => {
      if (!this.applicationIsVisible()) {
        this.wrapReceivedAndInvoke(callback, notification, true);
      }
    }
  }

  public wrapReceivedForegroundCallback(callback: Function): (notification: Notification) => void {
    return (notification) => {
      if (this.applicationIsVisible()) {
        this.wrapReceivedAndInvoke(callback, notification, false);
      }
    }
  }

  private wrapReceivedAndInvoke(callback: Function, notification: Notification, background: boolean) {
    const completion = (response: NotificationCompletion | string) => {
      if (Platform.OS === 'ios') {
        const identifier = (notification as unknown as NotificationIOS).identifier;
        if (background) {
          this.nativeCommandsSender.finishHandlingBackgroundAction(identifier, response as string);
        } else {
          this.nativeCommandsSender.finishPresentingNotification(identifier, response as NotificationCompletion);
        }
      }
    };

    callback(notification, completion);
  }

  public wrapOpenedCallback(callback: Function): (notification: Notification, action?: NotificationActionResponse) => void {
    return (notification, action) => {
      const completion = () => {
        if (Platform.OS === 'ios') {
          this.nativeCommandsSender.finishHandlingAction((notification as unknown as NotificationIOS).identifier);
        }
      };

      callback(notification, completion, action);
    }
  }

  private applicationIsVisible(): Boolean {
    return AppState.currentState !== 'background';
  }
}
