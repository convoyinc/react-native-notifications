package com.wix.reactnativenotifications.gcm;

import android.os.Bundle;
import android.util.Log;
import org.json.*;

import com.google.android.gms.gcm.GcmListenerService;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class GcmMessageHandlerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        Log.d(LOGTAG, "New message from GCM: " + bundle);
        String rawData = bundle.getString("data");
        // Hack by Convoy, all of our data is nested in "data" json. We need to bring it up a level.
        // we could change this in API but it's backwards incompatible with current app to do so.
        if (rawData.length() > 0) {
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
                try {
                    bundle.putString("group", data.getString("group"));
                } catch (JSONException ignored) {}
                try {
                    bundle.putString("sound", data.getString("sound"));
                } catch (JSONException ignored) {}
            } catch (JSONException ignored) {
                Log.d(LOGTAG, "Failed to parse raw data");
            }
        }

        try {
            final IPushNotification notification = PushNotification.get(getApplicationContext(), bundle);
            notification.onReceived();
        } catch (IPushNotification.InvalidNotificationException e) {
            // A GCM message, yes - but not the kind we know how to work with.
            Log.v(LOGTAG, "GCM message handling aborted", e);
        }
    }
}
