package com.blackviking.campusrush.Common;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import com.crashlytics.android.Crashlytics;

import co.paystack.android.PaystackSdk;
import io.fabric.sdk.android.Fabric;
import io.paperdb.Paper;

import com.blackviking.campusrush.R;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Scarecrow on 6/18/2018.
 */

public class PersistenceClass extends Application {

    public static final String CHANNEL_1_ID = "FeedChannel";
    public static final String CHANNEL_2_ID = "AdminChannel";
    public static final String CHANNEL_3_ID = "AccountChannel";
    public static final String CHANNEL_4_ID = "GamersChannel";
    public static final String CHANNEL_5_ID = "SkitChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   PAPER   ---*/
        Paper.init(getApplicationContext());

        /*---   PAYSTACK   ---*/
        PaystackSdk.initialize(getApplicationContext());

        /*---   DATE CALC   ---*/
        JodaTimeAndroid.init(this);

        /*---   CRASHLYTICS   ---*/
        Fabric.with(this, new Crashlytics());

        /*---   FIREBASE OFFLINE   ---*/
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        /*-------PICASSO--------*/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        createNotificationChannels();

    }

    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            /*---   OREO CUSTOM SOUND   ---*/
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri sound = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification_sound);



            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "FeedChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel1.setDescription("Feed Channel");
            channel1.setSound(sound, attributes);



            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "AdminChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel2.setDescription("Admin Channel");
            channel2.setSound(sound, attributes);



            NotificationChannel channel3 = new NotificationChannel(
                    CHANNEL_3_ID,
                    "AccountChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel3.setDescription("Account Channel");
            channel3.setSound(sound, attributes);



            NotificationChannel channel4 = new NotificationChannel(
                    CHANNEL_4_ID,
                    "GamersChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel4.setDescription("Gamers Channel");
            channel4.setSound(sound, attributes);



            NotificationChannel channel5 = new NotificationChannel(
                    CHANNEL_5_ID,
                    "SkitChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel1.enableVibration(true);
            channel1.enableLights(true);
            channel1.setShowBadge(true);
            channel5.setDescription("Skit Channel");
            channel5.setSound(sound, attributes);


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel3);
            manager.createNotificationChannel(channel4);
            manager.createNotificationChannel(channel5);
        }

    }
}
