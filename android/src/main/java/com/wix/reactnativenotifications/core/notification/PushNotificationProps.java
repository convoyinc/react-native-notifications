package com.wix.reactnativenotifications.core.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import org.json.JSONException;

public class PushNotificationProps {

    final protected Bundle mBundle;
    final protected Context mContext;

    public PushNotificationProps(Bundle bundle, Context context) {
        mBundle = bundle;
        mContext = context;
    }

    public int getBadge() {
        if (mBundle.containsKey("badge")) {
            return mBundle.getInt("badge");
        }
        return -1;
    }

    public String getTitle() { return mBundle.getString("title"); }

    public String getBody() { return mBundle.getString("body"); }

    public Bundle getChannel() {  return mBundle.getBundle("channel"); }

    public String getChannelId() {
        Bundle channel = getChannel();
        String channelId = null;
        if(channel != null) {
            try {
                channelId = channel.getString("id");
            } catch (Exception ignored){}
        }
        return "convoy-notifications-channel-id-" + (channelId == null ? "default": channelId);
    }

    public long[] getVibrationPattern() {
      Boolean shouldVibrate = getChannelVibration();
      if (shouldVibrate) {
        return new long[] { 1000, 1000, 1000 };
      }

      return null;
    }

    public int getPriority() {
        int priority = Notification.PRIORITY_MAX;
        Bundle channel = getChannel();
        String bundleImportance = null;
        try {
            bundleImportance = channel.getString("importance");
        } catch(Exception ignored) {}

        if (bundleImportance != null) {
            switch (bundleImportance) {
                case "default":
                    priority = Notification.PRIORITY_DEFAULT;
                    break;
                case "high":
                    priority = Notification.PRIORITY_HIGH;
                    break;
                case "low":
                    priority = Notification.PRIORITY_LOW;
                    break;
                case "max":
                    priority = Notification.PRIORITY_MAX;
                    break;
                case "min":
                    priority = Notification.PRIORITY_MIN;
                    break;
                case "none":
                    priority = Notification.PRIORITY_DEFAULT;
                    break;
            }
        }

        return priority;
    }

    public int getChannelImportance() {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        Bundle channel = getChannel();
        String bundleImportance = null;
        try {
            bundleImportance = channel.getString("importance");
        } catch(Exception ignored) {}

        if (bundleImportance != null) {
            switch (bundleImportance) {
                case "default":
                    importance = NotificationManager.IMPORTANCE_DEFAULT;
                    break;
                case "high":
                    importance = NotificationManager.IMPORTANCE_HIGH;
                    break;
                case "low":
                    importance = NotificationManager.IMPORTANCE_LOW;
                    break;
                case "max":
                    importance = NotificationManager.IMPORTANCE_MAX;
                    break;
                case "min":
                    importance = NotificationManager.IMPORTANCE_MIN;
                    break;
                case "none":
                    importance = NotificationManager.IMPORTANCE_NONE;
                    break;
            }
        }

        return importance;
    }

    public Uri getChannelSound() {
        Resources res = mContext.getResources();
        String packageName = mContext.getPackageName();
        Bundle channel = getChannel();
        String soundName = null;
        try {
            soundName = channel.getString("sound");
        } catch(Exception ignored) {}

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (soundName != null) {
            if (!"default".equalsIgnoreCase(soundName)) {
                // sound name can be full filename, or just the resource name.
                // So the strings 'my_sound.mp3' AND 'my_sound' are accepted
                // The reason is to make the iOS and android javascript interfaces compatible
                int resId;
                if (res.getIdentifier(soundName, "raw", packageName) != 0) {
                    resId = res.getIdentifier(soundName, "raw", packageName);
                } else {
                    resId = res.getIdentifier(soundName.substring(0, soundName.lastIndexOf('.')), "raw", packageName);
                }

                soundUri = Uri.parse("android.resource://" + packageName + "/" + resId);
            }
        }

        return soundUri;
    }

    public  String getChannelDescription() {
        Bundle channel = getChannel();
        String channelDescription = null;
        try {
            channelDescription = channel.getString("description");
        } catch (Exception ignored){}
        return channelDescription == null ? "Convoy Notifications" : channelDescription;
    }

    public  String getChannelName() {
        Bundle channel = getChannel();
        String channelName = null;
        try {
            channelName = channel.getString("name");
        } catch (Exception ignored){}
        return channelName == null ? "Notifications" : channelName;
    }

    public Boolean getChannelVibration() {
        Bundle channel = getChannel();
        Boolean channelVibration = null;
        try {
            channelVibration = channel.getBoolean("vibration");
        } catch (Exception ignored){}
        return channelVibration == null ? true : channelVibration;
    }

    public Boolean getChannelBadge() {
        Bundle channel = getChannel();
        Boolean channelBadge = null;
        try {
            channelBadge = channel.getBoolean("badge");
        } catch (Exception ignored){}
        return channelBadge == null ? true : channelBadge;
    }

    public Boolean getChannelLights() {
        Bundle channel = getChannel();
        Boolean channelLights = null;
        try {
            channelLights = channel.getBoolean("lights");
        } catch (Exception ignored){}
        return channelLights == null ? true : channelLights;
    }


    public boolean isVisible() {
        String title = getTitle();
        String sound = null;
        Bundle channel = getChannel();
        if (channel != null) {
            try {
                sound = channel.getString("sound");
            } catch(Exception ignored) {}
        }
        String body = getBody();
        return  (title != null && !title.isEmpty()) ||
                (sound != null && !sound.isEmpty()) ||
                (body != null && !body.isEmpty());
    }

    public Bundle asBundle() {
        return (Bundle) mBundle.clone();
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
        return new PushNotificationProps((Bundle) mBundle.clone(), mContext);
    }
}
