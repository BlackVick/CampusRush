package com.blackviking.campusrush.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.blackviking.campusrush.AdminManagement;
import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Home;
import com.blackviking.campusrush.Messaging.Messaging;
import com.blackviking.campusrush.Plugins.GamersHub.GameFeedDetail;
import com.blackviking.campusrush.Plugins.SkitCenter.SkitDetails;
import com.blackviking.campusrush.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.paperdb.Paper;

import static com.blackviking.campusrush.Common.PersistenceClass.CHANNEL_1_ID;
import static com.blackviking.campusrush.Common.PersistenceClass.CHANNEL_2_ID;
import static com.blackviking.campusrush.Common.PersistenceClass.CHANNEL_3_ID;
import static com.blackviking.campusrush.Common.PersistenceClass.CHANNEL_4_ID;
import static com.blackviking.campusrush.Common.PersistenceClass.CHANNEL_5_ID;

/**
 * Created by Scarecrow on 2/21/2019.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Paper.init(this);
        String appState = Paper.book().read(Common.APP_STATE);

        if (remoteMessage.getData() != null && appState.equalsIgnoreCase("Background")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                sendNotificationAPI26(remoteMessage);

            } else {

                sendNotification(remoteMessage);

            }

        } else if (remoteMessage.getData() != null && appState.equalsIgnoreCase("Foreground")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                sendNotificationAPI26Internally(remoteMessage);

            } else {

                sendNotificationInternally(remoteMessage);

            }

        }
    }

    private void sendNotificationInternally(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String skitId = data.get("skit_id");
        String skitOwner = data.get("skit_owner");
        String feedId = data.get("feed_id");
        String gameFeedId = data.get("game_feed_id");
        String userId = data.get("user_id");



        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Campus Feed")) {

            Intent feedBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            feedBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(feedBroadcastIntent);

        } else if (title.equalsIgnoreCase("Gamers Hub")) {

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Messaging")) {

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Admin")) {

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Skit Center")) {

            Intent accountBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            accountBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(accountBroadcastIntent);

        } else if (title.equalsIgnoreCase("Skit Approved")) {

            Intent accountBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            accountBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(accountBroadcastIntent);

        }

    }

    private void sendNotificationAPI26Internally(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String skitId = data.get("skit_id");
        String skitOwner = data.get("skit_owner");
        String feedId = data.get("feed_id");
        String gameFeedId = data.get("game_feed_id");
        String userId = data.get("user_id");


        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Campus Feed")) {

            Intent feedBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            feedBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(feedBroadcastIntent);

        } else if (title.equalsIgnoreCase("Gamers Hub")) {

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Messaging")) {

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Admin")) {

            Intent messageBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            messageBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageBroadcastIntent);

        } else if (title.equalsIgnoreCase("Skit Center")) {

            Intent accountBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            accountBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(accountBroadcastIntent);

        } else if (title.equalsIgnoreCase("Skit Approved")) {

            Intent accountBroadcastIntent = new Intent("NOTIFICATION_BROADCAST");
            accountBroadcastIntent.putExtra("Message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(accountBroadcastIntent);

        }

    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String skitId = data.get("skit_id");
        String skitOwner = data.get("skit_owner");
        String feedId = data.get("feed_id");
        String gameFeedId = data.get("game_feed_id");
        String userId = data.get("user_id");


        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Admin")) {

            Intent notificationsIntent = new Intent(this, AdminManagement.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.campus_rush_red_logo, "Manage", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(7, notification);

        } else if (title.equalsIgnoreCase("Campus Feed")) {

            Intent notificationsIntent = new Intent(this, Home.class);
            notificationsIntent.putExtra("IntentInstruction", "Notification");
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.campus_rush_red_logo, "Open Feed", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);

        } else if (title.equalsIgnoreCase("Account")) {

            Intent notificationsIntent = new Intent(this, Home.class);
            notificationsIntent.putExtra("IntentInstruction", "Notification");
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_3_ID)
                    .setSmallIcon(R.drawable.ic_campus_business)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(10, notification);

        } else if (title.equalsIgnoreCase("Messaging")) {

            Intent notificationsIntent = new Intent(this, Messaging.class);
            notificationsIntent.putExtra("UserId", userId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_message_user, "Open Message", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9, notification);

        } else if (title.equalsIgnoreCase("Skit Approval")) {

            Intent intent = new Intent(this, SkitDetails.class);
            intent.putExtra("SkitId", skitId);
            intent.putExtra("SkitOwner", skitOwner);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, intent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_5_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_skit_center, "View Your Skit", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(3, notification);

        } else if (title.equalsIgnoreCase("Skit Center")) {

            Intent intent = new Intent(this, SkitDetails.class);
            intent.putExtra("SkitId", skitId);
            intent.putExtra("SkitOwner", skitOwner);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, intent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_5_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_skit_center, "View Skit", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(4, notification);

        } else if (title.equalsIgnoreCase("Gamers Hub")) {

            Intent intent = new Intent(this, GameFeedDetail.class);
            intent.putExtra("GameFeedId", gameFeedId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, intent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_4_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.red_gamers_hub, "Open Game News", contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(5, notification);

        }

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");
        String skitId = data.get("skit_id");
        String skitOwner = data.get("skit_owner");
        String feedId = data.get("feed_id");
        String gameFeedId = data.get("game_feed_id");
        String userId = data.get("user_id");


        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Admin")) {

            Intent notificationsIntent = new Intent(this, AdminManagement.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.campus_rush_red_logo, "Manage", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(7, notification);

        } else if (title.equalsIgnoreCase("Campus Feed")) {

            Intent notificationsIntent = new Intent(this, Home.class);
            notificationsIntent.putExtra("IntentInstruction", "Notification");
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.campus_rush_red_logo, "Check Feed", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);

        } else if (title.equalsIgnoreCase("Account")) {

            Intent notificationsIntent = new Intent(this, Home.class);
            notificationsIntent.putExtra("IntentInstruction", "Notification");
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_3_ID)
                    .setSmallIcon(R.drawable.ic_campus_business)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(10, notification);

        } else if (title.equalsIgnoreCase("Messaging")) {

            Intent notificationsIntent = new Intent(this, Messaging.class);
            notificationsIntent.putExtra("UserId", userId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_message_user, "Open Message", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(9, notification);

        } else if (title.equalsIgnoreCase("Skit Approval")) {

            Intent intent = new Intent(this, SkitDetails.class);
            intent.putExtra("SkitId", skitId);
            intent.putExtra("SkitOwner", skitOwner);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, intent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_5_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_skit_center, "View Your Skit", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(3, notification);

        } else if (title.equalsIgnoreCase("Skit Center")) {

            Intent intent = new Intent(this, SkitDetails.class);
            intent.putExtra("SkitId", skitId);
            intent.putExtra("SkitOwner", skitOwner);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, intent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_5_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_skit_center, "View Skit", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(4, notification);

        } else if (title.equalsIgnoreCase("Gamers Hub")) {

            Intent intent = new Intent(this, GameFeedDetail.class);
            intent.putExtra("GameFeedId", gameFeedId);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, intent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_5_ID)
                    .setSmallIcon(R.drawable.ic_stat_campus_rush_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .addAction(R.drawable.red_gamers_hub, "Open Game News", contentIntent)
                    .setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound))
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(5, notification);

        }

    }
}
