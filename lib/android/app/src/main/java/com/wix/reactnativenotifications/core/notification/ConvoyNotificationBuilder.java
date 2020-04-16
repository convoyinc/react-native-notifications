package com.wix.reactnativenotifications.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class ConvoyNotificationBuilder {

    static class NotificationWithId {
        int id;
        Notification notification;

        NotificationWithId(int id, Notification notification) {
            this.id = id;
            this.notification = notification;
        }
    }

    final private Context mContext;
    final protected PushNotificationProps mNotificationProps;
    final private Bundle mBundle;
    final private PendingIntent mIntent;

    public ConvoyNotificationBuilder(Context context, PushNotificationProps notificationProps, PendingIntent intent) {
        mContext = context;
        mNotificationProps = notificationProps;
        mBundle = notificationProps.asBundle();
        mIntent = intent;
    }

    public NotificationWithId buildNotification() {
        Resources res = mContext.getResources();
        String packageName = mContext.getPackageName();


        int smallIconResId;
        int largeIconResId;

        String smallIcon = mBundle.getString("smallIcon");
        String largeIcon = mBundle.getString("largeIcon");

        if (smallIcon != null) {
            smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
        } else {
            smallIconResId = res.getIdentifier("ic_stat_name", "mipmap", packageName);
        }

        if (smallIconResId == 0) {
            smallIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);

            if (smallIconResId == 0) {
                smallIconResId = android.R.drawable.ic_dialog_info;
            }
        }

        if (largeIcon != null) {
            largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
        } else {
            largeIconResId = res.getIdentifier("ic_launcher", "mipmap", packageName);
        }

        Bitmap largeIconBitmap = BitmapFactory.decodeResource(res, largeIconResId);

        String title = mNotificationProps.getTitle();
        if (title == null) {
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
        }

        Bundle channelBundle = getChannel(mBundle);
        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(mContext);
        } else {
            createNotificationChannel(); // Must happen before notifying system of notification.
            notificationBuilder = new Notification.Builder(mContext, getChannelId(channelBundle));
        }

        notificationBuilder.setContentTitle(title)
                .setContentText(mNotificationProps.getBody())
                .setStyle(new Notification.BigTextStyle().bigText(mNotificationProps.getBody()))
                .setPriority(getPriority(channelBundle))
                .setContentIntent(mIntent)
                .setVibrate(getVibrationPattern(channelBundle))
                .setSmallIcon(smallIconResId)
                .setShowWhen(true)
                .setAutoCancel(true);

        int badge = getBadge();
        if (badge >= 0) {
            notificationBuilder.setNumber(badge);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(Color.parseColor("#f65335"));
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            notificationBuilder.setGroup(getChannelId(channelBundle));
            Bundle actions = getActions();
            if (actions != null) {
                String defaultAction = actions.getString("default");
                if (defaultAction != null) {
                    notificationBuilder.addAction(buildNotificationAction(defaultAction, mIntent));
                }
                String additionalAction = actions.getString("additional");
                if (additionalAction != null) {
                    notificationBuilder.addAction(buildNotificationAction(additionalAction, mIntent));
                }
            }
        }

        if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            notificationBuilder.setLargeIcon(largeIconBitmap);
        }

        return new NotificationWithId(buildNotificationId(), notificationBuilder.build());
    }

    private int buildNotificationId() {
        Bundle collapse = mBundle.containsKey("collapse") ? mBundle.getBundle("collapse") : null;
        String pushType = mBundle.containsKey("pushType") ? mBundle.getString("pushType") : "";
        String pushId = null;
        if (collapse != null) {
            String collapseType = collapse.getString("cs");
            switch (collapseType) {
                case "type":
                    pushId = pushType;
                    break;
                case "id":
                    pushId = collapse.getString("id");
                    break;
                default:
                    break;
            }
        }
        if (pushId == null) {
            pushId = pushType + ':' + System.currentTimeMillis();
        }
        return pushId.hashCode();
    }

    private Notification.Action buildNotificationAction(String label, PendingIntent intent) {
        return new Notification.Action.Builder(
                android.R.drawable.ic_menu_call,
                label,
                intent
        ).build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle channelBundle = getChannel(mBundle);
        NotificationChannel channel = new NotificationChannel(
                getChannelId(channelBundle),
                getChannelName(channelBundle),
                getChannelImportance(channelBundle)
        );

        channel.setDescription(getChannelDescription(channelBundle));
        channel.enableLights(getChannelLights(channelBundle));
        channel.enableVibration(getChannelVibration(channelBundle));
        channel.setSound(getChannelSound(mContext, channelBundle), new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build());

        channel.setShowBadge(getChannelBadge(channelBundle));

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after, only resets upon re-install.
        notificationManager.createNotificationChannel(channel);
    }

    private int getBadge() {
        if (mBundle.containsKey("badge")) {
            return mBundle.getInt("badge");
        }
        return 1;
    }

    private Bundle getActions() {
        if (mBundle.containsKey("actions")) {
            return mBundle.getBundle("actions");
        }
        return null;
    }

    private static Bundle getChannel(Bundle bundle) {
        if (bundle.containsKey("channel")) {
            return bundle.getBundle("channel");
        }
        return null;
    }

    private static String getChannelId(Bundle channel) {
        String channelId = null;
        if (channel != null) {
            try {
                channelId = channel.getString("id");
            } catch (Exception ignored){}
        }
        return "convoy-notifications-channel-id-" + (channelId == null ? "default": channelId);
    }

    private static String getChannelDescription(Bundle channel) {
        String channelDescription = null;
        try {
            channelDescription = channel.getString("description");
        } catch (Exception ignored){}
        return channelDescription == null ? "Convoy Notifications" : channelDescription;
    }

    private static String getChannelName(Bundle channel) {
        String channelName = null;
        try {
            channelName = channel.getString("name");
        } catch (Exception ignored){}
        return channelName == null ? "Notifications" : channelName;
    }

    private static Boolean getChannelVibration(Bundle channel) {
        Boolean channelVibration = null;
        try {
            channelVibration = channel.getBoolean("vibration");
        } catch (Exception ignored){}
        return channelVibration == null ? true : channelVibration;
    }

    private static Boolean getChannelBadge(Bundle channel) {
        Boolean channelBadge = null;
        try {
            channelBadge = channel.getBoolean("badge");
        } catch (Exception ignored){}
        return channelBadge == null ? true : channelBadge;
    }

    private static int getPriority(Bundle channel) {
        int priority = Notification.PRIORITY_MAX;
        String bundleImportance = null;
        try {
            bundleImportance = channel.getString("importance");
        } catch(Exception ignored) {}

        if (bundleImportance != null) {
            switch (bundleImportance) {
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
                case "default":
                default:
                    priority = Notification.PRIORITY_DEFAULT;
                    break;
            }
        }

        return priority;
    }

    private static int getChannelImportance(Bundle channel) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
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


    private static Uri getChannelSound(Context context, Bundle channel) {
        Resources res = context.getResources();
        String packageName = context.getPackageName();
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

    private static Boolean getChannelLights(Bundle channel) {
        Boolean channelLights = null;
        try {
            channelLights = channel.getBoolean("lights");
        } catch (Exception ignored){}
        return channelLights == null ? true : channelLights;
    }

    private static long[] getVibrationPattern(Bundle channel) {
        Boolean shouldVibrate = getChannelVibration(channel);
        if (shouldVibrate) {
            return new long[] { 1000, 1000, 1000 };
        }

        return null;
    }

    public static Bundle getNotificationChannelSettings(Context context, Bundle bundle) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }

        Bundle channelBundle = getChannel(bundle);
        if (channelBundle == null) {
            return null;
        }

        String channelId = getChannelId(channelBundle);
        Bundle channelSettings = new Bundle();
        channelSettings.putString("channelId", channelId);
        channelSettings.putInt("assignedImportance", getChannelImportance(channelBundle));

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        if (channel != null) {
            channelSettings.putInt("actualImportance", channel.getImportance());
        }
        return channelSettings;
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
