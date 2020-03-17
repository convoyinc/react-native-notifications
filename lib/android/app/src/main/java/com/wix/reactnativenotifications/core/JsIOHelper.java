package com.wix.reactnativenotifications.core;

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
        ReactContext reactContext = appLifecycleFacade.getRunningReactContext();
        if (reactContext != null && appLifecycleFacade.isReactInitialized()) {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
            return true;
        }
        return false;
    }
}
