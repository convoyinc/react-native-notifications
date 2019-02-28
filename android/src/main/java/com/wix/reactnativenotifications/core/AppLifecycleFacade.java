package com.wix.reactnativenotifications.core;

import com.wix.reactnativenotifications.RNNotificationsNativeCallback;
import com.facebook.react.bridge.ReactContext;

public interface AppLifecycleFacade {

    interface AppVisibilityListener {
        void onAppVisible();
        void onAppNotVisible();
    }

    boolean isReactInitialized();
    ReactContext getRunningReactContext();
    RNNotificationsNativeCallback getNativeCallback();
    boolean isAppVisible();
    void addVisibilityListener(AppVisibilityListener listener);
    void removeVisibilityListener(AppVisibilityListener listener);
}
