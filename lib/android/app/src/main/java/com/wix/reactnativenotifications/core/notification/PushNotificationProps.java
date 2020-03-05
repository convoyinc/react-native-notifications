package com.wix.reactnativenotifications.core.notification;

import android.os.Bundle;

public class PushNotificationProps {

    protected Bundle mBundle;

    public PushNotificationProps(Bundle bundle) {
        mBundle = bundle;
    }

    public String getTitle() {
        return mBundle.getString("title");
    }

    public String getBody() {
        String body = mBundle.getString("body");
        if (body == null) {
            body = mBundle.getString("alert");
        }
        return body;
    }

    public Bundle asBundle() {
        return (Bundle) mBundle.clone();
    }

    public boolean isFirebaseBackgroundPayload() {
        return mBundle.containsKey("google.message_id");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        for (String key : mBundle.keySet()) {
            sb.append(key).append("=").append(mBundle.get(key)).append(", ");
        }
        return sb.toString();
    }

    protected PushNotificationProps copy() {
        return new PushNotificationProps((Bundle) mBundle.clone());
    }

    public boolean isVisible() {
        String title = getTitle();
        String body = getBody();
        return (title != null && !title.isEmpty()) || (body != null && !body.isEmpty());
    }
}
