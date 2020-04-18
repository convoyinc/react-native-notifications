package com.wix.reactnativenotifications.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.wix.reactnativenotifications.core.AppLaunchHelper;
import com.wix.reactnativenotifications.core.AppLifecycleFacade;
import com.wix.reactnativenotifications.core.AppLifecycleFacade.AppVisibilityListener;
import com.wix.reactnativenotifications.core.AppLifecycleFacadeHolder;
import com.wix.reactnativenotifications.core.InitialNotificationHolder;
import com.wix.reactnativenotifications.core.JsIOHelper;
import com.wix.reactnativenotifications.core.NotificationIntentAdapter;
import com.wix.reactnativenotifications.core.ProxyService;

import static com.wix.reactnativenotifications.Defs.NOTIFICATION_OPENED_EVENT_NAME;
import static com.wix.reactnativenotifications.Defs.NOTIFICATION_RECEIVED_EVENT_NAME;

public class PushNotification implements IPushNotification {

    final protected Context mContext;
    final protected AppLifecycleFacade mAppLifecycleFacade;
    final protected AppLaunchHelper mAppLaunchHelper;
    final protected JsIOHelper mJsIOHelper;
    final protected PushNotificationProps mNotificationProps;
    final protected AppVisibilityListener mAppVisibilityListener = new AppVisibilityListener() {
        @Override
        public void onAppVisible() {
            mAppLifecycleFacade.removeVisibilityListener(this);
            dispatchImmediately();
        }

        @Override
        public void onAppNotVisible() {
        }
    };

    public static IPushNotification get(Context context, Bundle bundle) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof INotificationsApplication) {
            return ((INotificationsApplication) appContext).getPushNotification(context, bundle, AppLifecycleFacadeHolder.get(), new AppLaunchHelper());
        }
        return new PushNotification(context, bundle, AppLifecycleFacadeHolder.get(), new AppLaunchHelper(), new JsIOHelper());
    }

    protected PushNotification(Context context, Bundle bundle, AppLifecycleFacade appLifecycleFacade, AppLaunchHelper appLaunchHelper, JsIOHelper JsIOHelper) {
        mContext = context;
        mAppLifecycleFacade = appLifecycleFacade;
        mAppLaunchHelper = appLaunchHelper;
        mJsIOHelper = JsIOHelper;
        mNotificationProps = createProps(bundle);
    }

    @Override
    public void onReceived() throws InvalidNotificationException {
        this.onPostRequest(null);
    }

    @Override
    public void onOpened() {
        digestNotification();
        clearAllNotifications();
    }

    @Override
    public int onPostRequest(Integer notificationId) {
        final PendingIntent pendingIntent = getCTAPendingIntent();
        final ConvoyNotificationBuilder builder = new ConvoyNotificationBuilder(mContext, mNotificationProps, pendingIntent);
        if (!mAppLifecycleFacade.isAppVisible() && mNotificationProps.isVisible()) {
            final ConvoyNotificationBuilder.NotificationWithId notificationWithId = builder.buildNotificationWithId();
            notificationId = notificationWithId.id;
            postNotification(notificationId, notificationWithId.notification);
        } else if (!mNotificationProps.isVisible()) {
            notificationId = builder.buildNotificationId();
            cancelNotification(notificationId);
        }
        notifyReceivedToJS();
        return notificationId != null ? notificationId : 0;
    }

    @Override
    public PushNotificationProps asProps() {
        return mNotificationProps.copy();
    }

    protected void digestNotification() {
        if (!mAppLifecycleFacade.isReactInitialized()) {
            setAsInitialNotification();
            launchOrResumeApp();
            return;
        }

        final ReactContext reactContext = mAppLifecycleFacade.getRunningReactContext();
        if (reactContext.getCurrentActivity() == null) {
            setAsInitialNotification();
        }

        if (mAppLifecycleFacade.isAppVisible()) {
            dispatchImmediately();
        } else {
            dispatchUponVisibility();
        }
    }

    protected PushNotificationProps createProps(Bundle bundle) {
        return ConvoyNotificationBuilder.createFromBundle(bundle);
    }

    protected void setAsInitialNotification() {
        InitialNotificationHolder.getInstance().set(mNotificationProps);
    }

    protected void dispatchImmediately() {
        notifyOpenedToJS();
    }

    protected void dispatchUponVisibility() {
        mAppLifecycleFacade.addVisibilityListener(getIntermediateAppVisibilityListener());

        // Make the app visible so that we'll dispatch the notification opening when visibility changes to 'true' (see
        // above listener registration).
        launchOrResumeApp();
    }

    protected AppVisibilityListener getIntermediateAppVisibilityListener() {
        return mAppVisibilityListener;
    }

    protected PendingIntent getCTAPendingIntent() {
        final Intent cta = new Intent(mContext, ProxyService.class);
        return NotificationIntentAdapter.createPendingNotificationIntent(mContext, cta, mNotificationProps);
    }

    protected Notification.Builder getNotificationBuilder(PendingIntent intent) {

        String CHANNEL_ID = "channel_01";
        String CHANNEL_NAME = "Channel Name";

        final Notification.Builder notification = new Notification.Builder(mContext)
                .setContentTitle(mNotificationProps.getTitle())
                .setContentText(mNotificationProps.getBody())
                .setContentIntent(intent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);


             int resourceID = mContext.getResources().getIdentifier("notification_icon", "drawable", mContext.getPackageName());
                if (resourceID != 0) {
                    notification.setSmallIcon(resourceID);
                } else {
                    notification.setSmallIcon(mContext.getApplicationInfo().icon);
                }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notification.setChannelId(CHANNEL_ID);
        }

        return notification;
    }

    private int postNotification(int id, Notification notification) {
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
        return id;
    }

    private void cancelNotification(int id) {
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    protected void clearAllNotifications() {
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    protected int createNotificationId(Notification notification) {
        return (int) System.nanoTime();
    }

    private void notifyReceivedToJS() {
        Bundle bundle = mNotificationProps.asBundle();
        Bundle channelSettings = ConvoyNotificationBuilder.getNotificationChannelSettings(mContext, bundle);
        if (channelSettings != null) {
            bundle.putBundle("channelSettings", channelSettings);
        }
        mJsIOHelper.sendEventToJS(NOTIFICATION_RECEIVED_EVENT_NAME, bundle, mAppLifecycleFacade);
    }

    private void notifyOpenedToJS() {
        Bundle response = new Bundle();
        response.putBundle("notification", mNotificationProps.asBundle());

        mJsIOHelper.sendEventToJS(NOTIFICATION_OPENED_EVENT_NAME, response, mAppLifecycleFacade);
    }

    protected void launchOrResumeApp() {
        final Intent intent = mAppLaunchHelper.getLaunchIntent(mContext);
        mContext.startActivity(intent);
    }
}
