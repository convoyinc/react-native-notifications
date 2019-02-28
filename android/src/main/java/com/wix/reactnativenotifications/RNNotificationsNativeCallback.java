package com.wix.reactnativenotifications;

import com.facebook.react.bridge.WritableMap;

public interface RNNotificationsNativeCallback {
    void onEventNotSentToJS(String eventName, WritableMap data);
}
