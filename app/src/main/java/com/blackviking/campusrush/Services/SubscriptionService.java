package com.blackviking.campusrush.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.blackviking.campusrush.Common.Common;
import com.blackviking.campusrush.Messaging.Messaging;
import com.blackviking.campusrush.Notification.APIService;
import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;

public class SubscriptionService extends Service {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, subScriptionRef, notificationRef;
    private long usedDays;
    private String currentSubDate, currentDate, userId;
    private Date lastSubDate, todayDate;


    private long date, dayDiff;
    private SimpleDateFormat sdfToday, sdfSubDay;
    private APIService mService;

    public SubscriptionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   LOCAL   ---*/
        Paper.init(this);
        userId = Paper.book().read(Common.USER_ID);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        subScriptionRef = db.getReference("BusinessAccountSubscriptions");
        notificationRef = db.getReference("Notifications");
        

        /*---   TODAY DATE   ---*/
        final long date = System.currentTimeMillis();
        sdfToday = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = sdfToday.format(date);
        try {
            todayDate = sdfToday.parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startComputation();

        return START_STICKY;
    }

    private void startComputation() {

        if (Common.isConnectedToInternet(getApplicationContext())){

            Common.isSubServiceRunning = true;

            subScriptionRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    currentSubDate = dataSnapshot.child("transactionDate").getValue().toString();

                    sdfSubDay = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        lastSubDate = sdfSubDay.parse(currentSubDate);

                        /*---   CALCULATE   ---*/
                        dayDiff = todayDate.getTime() - lastSubDate.getTime();

                        usedDays = TimeUnit.DAYS.convert(dayDiff, TimeUnit.MILLISECONDS);

                        if (usedDays >= 30){

                            final Map<String, Object> expirationMap = new HashMap<>();
                            expirationMap.put("subscriptionStatus", "Inactive");
                            expirationMap.put("paymentStatus", "Unpaid");
                            expirationMap.put("transactionRef", "");
                            expirationMap.put("transactionDate", "");

                            subScriptionRef.child(userId)
                                    .updateChildren(expirationMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            updateNotification();
                                        }
                                    });


                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            repeatAction();

        } else {

            retryNetwork();

        }

    }

    private void retryNetwork() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startComputation();
            }
        }, 1800000);
    }

    private void updateNotification() {

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", "Account");
        notificationMap.put("details", "Hello User, \nYour Business Account subscription has expired. Please renew your subscription to continue enjoying unlimited access.");
        notificationMap.put("comment", "");
        notificationMap.put("type", "Business");
        notificationMap.put("status", "Unread");
        notificationMap.put("intentPrimaryKey", "");
        notificationMap.put("intentSecondaryKey", "");
        notificationMap.put("user", userId);
        notificationMap.put("timestamp", ServerValue.TIMESTAMP);

        notificationRef.child(userId)
                .push()
                .setValue(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendNotification();
            }
        });

    }

    private void sendNotification() {

        Map<String, String> dataSend = new HashMap<>();
        dataSend.put("title", "Account");
        dataSend.put("message", "Hello User, \nYour Business Account subscription has expired. Please renew your subscription to continue enjoying unlimited access.");
        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

        mService.sendNotification(dataMessage)
                .enqueue(new retrofit2.Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(SubscriptionService.this, "Error Sending Notification", Toast.LENGTH_SHORT).show();
                    }
                });
        stopSelf();

    }

    private void repeatAction() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startComputation();
            }
        }, 86400000);
    }

    //24 hours = 86400000
    //6 hours = 21600000
    //2 hours = 7200000
    //30 minutes = 1800000

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
