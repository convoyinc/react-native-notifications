package com.wix.reactnativenotifications.core.notification;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class PushNotificationProps {

    protected Bundle mBundle;

    private PushNotificationProps(Bundle bundle) {
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

    public static PushNotificationProps createFromBundle(Bundle bundle) {
        String dataString = bundle.getString("data");
        if (dataString != null && !dataString.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(dataString);
                addJsonObjectToBundle(jsonObject, bundle);
            } catch (JSONException e) {
                Log.e(LOGTAG, "Error parsing data bundle", e);
            }
        }
        return new PushNotificationProps(bundle);
    }

    private static void addJsonObjectToBundle(JSONObject jsonObject, Bundle bundle) throws JSONException {
        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof String) {
                bundle.putString(key, (String)value);
            } else if (value instanceof Boolean) {
                bundle.putBoolean(key, (Boolean)value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer)value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double)value);
            } else if (value instanceof Float) {
                bundle.putFloat(key, (Float)value);
            } else if (value instanceof JSONObject) {
                Bundle subBundle = new Bundle();
                bundle.putBundle(key, subBundle);
                addJsonObjectToBundle((JSONObject)value, subBundle);
            }
        }
    }
}
