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

public class ConvoyNotificationBuilder {

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

    public Notification buildNotification() {
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

        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(mContext);
        } else {
            createNotificationChannel(); // Must happen before notifying system of notification.
            notificationBuilder = new Notification.Builder(mContext, getChannelId());
        }

        notificationBuilder.setContentTitle(title)
                .setContentText(mNotificationProps.getBody())
                .setStyle(new Notification.BigTextStyle().bigText(mNotificationProps.getBody()))
                .setPriority(getPriority())
                .setContentIntent(mIntent)
                .setVibrate(getVibrationPattern())
                .setSmallIcon(smallIconResId)
                .setAutoCancel(true);

        int badge = getBadge();
        if (badge >= 0) {
            notificationBuilder.setNumber(badge);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(Color.parseColor("#f65335"));
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            notificationBuilder.setGroup(getChannelId());
        }

        if (largeIconResId != 0 && (largeIcon != null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            notificationBuilder.setLargeIcon(largeIconBitmap);
        }

        return notificationBuilder.build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                getChannelId(),
                getChannelName(),
                getChannelImportance()
        );

        channel.setDescription(getChannelDescription());
        channel.enableLights(getChannelLights());
        channel.enableVibration(getChannelVibration());
        channel.setSound(getChannelSound(), new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build());

        channel.setShowBadge(getChannelBadge());

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

    private Bundle getChannel() {
        if (mBundle.containsKey("channel")) {
            return mBundle.getBundle("channel");
        }
        return null;
    }

    private String getChannelId() {
        Bundle channel = getChannel();
        String channelId = null;
        if(channel != null) {
            try {
                channelId = channel.getString("id");
            } catch (Exception ignored){}
        }
        return "convoy-notifications-channel-id-" + (channelId == null ? "default": channelId);
    }

    private String getChannelDescription() {
        Bundle channel = getChannel();
        String channelDescription = null;
        try {
            channelDescription = channel.getString("description");
        } catch (Exception ignored){}
        return channelDescription == null ? "Convoy Notifications" : channelDescription;
    }

    private String getChannelName() {
        Bundle channel = getChannel();
        String channelName = null;
        try {
            channelName = channel.getString("name");
        } catch (Exception ignored){}
        return channelName == null ? "Notifications" : channelName;
    }

    private Boolean getChannelVibration() {
        Bundle channel = getChannel();
        Boolean channelVibration = null;
        try {
            channelVibration = channel.getBoolean("vibration");
        } catch (Exception ignored){}
        return channelVibration == null ? true : channelVibration;
    }

    private Boolean getChannelBadge() {
        Bundle channel = getChannel();
        Boolean channelBadge = null;
        try {
            channelBadge = channel.getBoolean("badge");
        } catch (Exception ignored){}
        return channelBadge == null ? true : channelBadge;
    }

    private int getPriority() {
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

    private int getChannelImportance() {
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


    private Uri getChannelSound() {
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

    private Boolean getChannelLights() {
        Bundle channel = getChannel();
        Boolean channelLights = null;
        try {
            channelLights = channel.getBoolean("lights");
        } catch (Exception ignored){}
        return channelLights == null ? true : channelLights;
    }

    private long[] getVibrationPattern() {
        Boolean shouldVibrate = getChannelVibration();
        if (shouldVibrate) {
            return new long[] { 1000, 1000, 1000 };
        }

        return null;
    }
}
