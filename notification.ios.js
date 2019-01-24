import { NativeModules } from "react-native";
const NativeRNNotifications = NativeModules.RNNotifications; // eslint-disable-line no-unused-vars

export default class IOSNotification {
  _id: string;
  _data: Object;
  _alert: string | Object;
  _sound: string;
  _badge: number;
  _category: string;
  _type: string; // regular / managed
  _thread: string;

  constructor(notification: Object) {
    this._data = {};

    if (notification.aps &&
      notification.aps["content-available"] &&
      notification.aps["content-available"] === 1 &&
      !notification.aps.alert &&
      !notification.aps.sound &&
      notification.managedAps) {
      // managed notification
      if (typeof notification.managedAps.alert == "object") {
        if ("body" in notification.managedAps.alert) {
          this._alert = notification.managedAps.alert.body;
        }
        if ("title" in notification.managedAps.alert) {
          this._title = notification.managedAps.alert.title;
        }
      } else if (typeof notification.managedAps.alert == "string") {
        this._alert = notification.managedAps.alert;
        this._title = notification.managedAps.title;
      }
      this._sound = notification.managedAps.sound;
      this._badge = notification.aps.badge;
      this._category = notification.managedAps.category;
      this._type = "managed";
      this._thread = notification.aps["thread-id"];
    } else if (
      notification.aps &&
      notification.aps.alert) {
      // regular notification
      if (typeof notification.aps.alert == "object") {
        if ("body" in notification.aps.alert) {
          this._alert = notification.aps.alert.body;
        }
        if ("title" in notification.aps.alert) {
          this._title = notification.aps.alert.title;
        }
      } else if (typeof notification.aps.alert == "string") {
        this._alert = notification.aps.alert;
        this._title = notification.aps.title;
      }
      this._sound = notification.aps.sound;
      this._badge = notification.aps.badge;
      this._category = notification.aps.category;
      this._type = "regular";
      this._thread = notification.aps["thread-id"];
    }

    this._id = notification.__id;

    Object.keys(notification).filter(key => key !== "aps").forEach(key => {
      this._data[key] = notification[key];
    });
  }

  finish(fetchResult) {
    if (this._id) {
      NativeRNNotifications.onFinishRemoteNotification(this._id, fetchResult);
    }
  }

  getMessage(): ?string | ?Object {
    return this._alert;
  }

  getTitle(): ?string {
    return this._title;
  }

  getSound(): ?string {
    return this._sound;
  }

  getBadgeCount(): ?number {
    return this._badge;
  }

  getCategory(): ?string {
    return this._category;
  }

  getData(): ?Object {
    return this._data;
  }

  getType(): ?string {
    return this._type;
  }

  getThread(): ?string {
    return this._thread;
  }
}
