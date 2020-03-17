package com.wix.reactnativenotifications.fcm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.wix.reactnativenotifications.core.JsIOHelper;

import static com.wix.reactnativenotifications.Defs.LOGTAG;
import static com.wix.reactnativenotifications.Defs.TOKEN_RECEIVED_EVENT_NAME;

public class FcmToken implements IFcmToken {

    final protected Context mAppContext;
    final private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final Runnable sendTokenToJsRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mAppContext) {
                final ReactInstanceManager instanceManager = ((ReactApplication) mAppContext).getReactNativeHost().getReactInstanceManager();
                final ReactContext reactContext = instanceManager.getCurrentReactContext();
                // Note: Cannot assume react-context exists cause this is an async dispatched service.
                if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
                    Bundle tokenMap = new Bundle();
                    tokenMap.putString("deviceToken", sToken);
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(TOKEN_RECEIVED_EVENT_NAME, Arguments.fromBundle(tokenMap));
                }
            }
        }
    };

    protected static String sToken;

    protected FcmToken(Context appContext) {
        if (!(appContext instanceof ReactApplication)) {
            throw new IllegalStateException("Application instance isn't a react-application");
        }
        mAppContext = appContext;
    }

    public static IFcmToken get(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof INotificationsFcmApplication) {
            return ((INotificationsFcmApplication) appContext).getFcmToken(context);
        }
        return new FcmToken(appContext);
    }

    @Override
    public void onNewTokenReady() {
        synchronized (mAppContext) {
            refreshToken();
        }
    }

    @Override
    public void onManualRefresh() {
        synchronized (mAppContext) {
            if (sToken == null) {
                Log.i(LOGTAG, "Manual token refresh => asking for new token");
                refreshToken();
            } else {
                Log.i(LOGTAG, "Manual token refresh => publishing existing token ("+sToken+")");
                sendTokenToJS();
            }
        }
    }

    @Override
    public void onAppReady() {
        synchronized (mAppContext) {
            if (sToken == null) {
                Log.i(LOGTAG, "App initialized => asking for new token");
                refreshToken();
            } else {
                // Except for first run, this should be the case.
                Log.i(LOGTAG, "App initialized => publishing existing token ("+sToken+")");
                sendTokenToJS();
            }
        }
    }

    protected void refreshToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                sToken = instanceIdResult.getToken();
                Log.i(LOGTAG, "FCM has a new token" + "=" + sToken);
                sendTokenToJS();
            }
        });
    }

    /**
     * This method can be called from a background thread.  The call to getReactInstanceManager()
     * can end up calling createReactInstanceManager() which must be called from the UI thread.
     *
     * Because of this restriction we make a point to always post this runnable to a main thread.
     */
    protected void sendTokenToJS() {
        mainThreadHandler.post(sendTokenToJsRunnable);
    }
}
