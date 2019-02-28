package com.wix.reactnativenotifications.core;

import com.wix.reactnativenotifications.RNNotificationsNativeCallback;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class JsIOHelper {
    public boolean sendEventToJS(String eventName, Bundle data, AppLifecycleFacade appLifecycleFacade) {
        return sendEventToJS(eventName, Arguments.fromBundle(data), appLifecycleFacade);
    }

    public boolean sendEventToJS(String eventName, WritableMap data, AppLifecycleFacade appLifecycleFacade) {
        RNNotificationsNativeCallback nativeCallback = appLifecycleFacade.getNativeCallback();
        ReactContext reactContext = appLifecycleFacade.getRunningReactContext();
        if (appLifecycleFacade.isReactInitialized() && reactContext != null) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
            return true;
        } else if (nativeCallback != null) {
            nativeCallback.onEventNotSentToJS(eventName, data);
        }
        return false;
    }
}
