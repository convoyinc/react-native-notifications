package com.wix.reactnativenotifications.gcm;

import android.os.Bundle;
import android.util.Log;
import org.json.*;

import com.facebook.common.logging.FLog;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;

import java.util.Map;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

/**
 * Instance-ID + token refreshing handling service. Contacts the GCM to fetch the updated token.
 *
 * @author amitd
 */
public class FcmInstanceIdListenerService extends FirebaseMessagingService {

    private final static String LOG_TAG = "GcmMessageHandlerService";

    @Override
    public void onMessageReceived(RemoteMessage message){
        Map messageData = message.getData();
        Bundle bundle = convertMapToBundle(messageData);
        Log.d(LOGTAG, "New message from GCM: " + bundle);
        // Hack by Convoy, all of our data is nested in "data" json. We need to bring it up a level.
        // we could change this in API but it's backwards incompatible with current app to do so.
        String rawData = bundle.getString("data");
        if (rawData != null && rawData.length() > 0) {
            try {
                JSONObject data = new JSONObject(rawData);
                try {
                    bundle.putString("title", data.getString("title"));
                } catch (JSONException ignored) {}
                try {
                    bundle.putString("body", data.getString("alert"));
                } catch (JSONException ignored) {}
                try {
                    bundle.putString("badge", data.getString("badge"));
                } catch (JSONException ignored) {}

                JSONObject channel = null;
                Bundle channelBundle = new Bundle();

                try {
                    channel = data.getJSONObject("channel");
                } catch (JSONException ignored) {}

                if (channel != null) {
                    try {
                        channelBundle.putString("id", channel.getString("id"));
                    } catch (JSONException ignored) {}
                    try {
                    channelBundle.putString("name", channel.getString("name"));
                    } catch (JSONException ignored) {}
                    try {
                        channelBundle.putString("description", channel.getString("description"));
                    } catch (JSONException ignored) {}
                    try {
                        channelBundle.putString("sound", channel.getString("sound"));
                    } catch (JSONException ignored) {}
                    try {
                        channelBundle.putString("importance", channel.getString("importance"));
                    } catch (JSONException ignored) {}
                    try {
                        channelBundle.putBoolean("lights", channel.getBoolean("lights"));
                    } catch (JSONException ignored) {}
                    try {
                        channelBundle.putBoolean("vibration", channel.getBoolean("vibration"));
                    } catch (JSONException ignored) {}
                    try {
                        channelBundle.putBoolean("badge", channel.getBoolean("badge"));
                    } catch (JSONException ignored) {}

                    bundle.putBundle("channel", channelBundle);
                }
                try {
                    bundle.putString("sound", data.getString("sound"));
                } catch (JSONException ignored) {}
            } catch (JSONException ignored) {
                Log.d(LOGTAG, "Failed to parse raw data");
            }
        } else {
            FLog.i(LOG_TAG, "rawData doesn't contain data key or data is empty: " + bundle);
        }

        try {
            final IPushNotification notification = PushNotification.get(getApplicationContext(), bundle);
            notification.onReceived();
        } catch (IPushNotification.InvalidNotificationException e) {
            // A GCM message, yes - but not the kind we know how to work with.
            Log.v(LOGTAG, "GCM message handling aborted", e);
            FLog.i(LOG_TAG, "GCM message handling aborted: " + bundle);
        }
    }

    private Bundle convertMapToBundle(Map<String, String> map) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        return bundle;
    }

}
