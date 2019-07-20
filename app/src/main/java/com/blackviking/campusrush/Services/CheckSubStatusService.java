package com.blackviking.campusrush.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.blackviking.campusrush.Common.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class CheckSubStatusService extends Service {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference subscriptionRef;
    private String subscriptionStatus, userId;

    public CheckSubStatusService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   FIREBASE   ---*/
        subscriptionRef = db.getReference("BusinessAccountSubscriptions");


        /*---   LOCAL   ---*/
        Paper.init(this);
        userId = Paper.book().read(Common.USER_ID);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startCheck();

        return START_STICKY;
    }

    private void startCheck() {

        if (Common.isConnectedToInternet(getApplicationContext())){

            /*---   SUBSCRIPTION LOT   ---*/
            subscriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(userId).exists()){

                        subscriptionStatus = dataSnapshot.child(userId).child("subscriptionStatus").getValue().toString();

                        if (subscriptionStatus.equalsIgnoreCase("Active")){

                            Intent intent = new Intent(getApplicationContext(), SubscriptionService.class);
                            startService(intent);

                            stopSelf();

                        } else {

                            stopSelf();

                        }

                    } else {

                        stopSelf();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {

            retryNetwork();

        }

    }

    private void retryNetwork() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startCheck();
            }
        }, 1800000);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
