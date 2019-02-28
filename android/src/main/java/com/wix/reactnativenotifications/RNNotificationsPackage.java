package com.wix.reactnativenotifications;

import android.app.Application;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RNNotificationsPackage implements ReactPackage {

    private final Application mApplication;
    private RNNotificationsNativeCallback mNativeCallback;

    public RNNotificationsPackage(Application application) {
        mApplication = application;
    }

    public void addNativeCallback(RNNotificationsNativeCallback rnNotificationsNativeCallback) {
        mNativeCallback = rnNotificationsNativeCallback;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RNNotificationsModule(mApplication, mNativeCallback, reactContext));
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
