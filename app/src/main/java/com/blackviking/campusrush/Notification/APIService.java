package com.blackviking.campusrush.Notification;

import com.blackviking.campusrush.Notification.DataMessage;
import com.blackviking.campusrush.Notification.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Scarecrow on 4/1/2018.
 */

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAw4tQYgE:APA91bFQnEMOYBqEO2K-nRRPrXW-jCw8VMyWNMxTWm_6R7jxqJAYZOao0zXAjltOOoJQ9kaDhe8cegMuS7jgk8uFsMio7qy46QkNUbMrEi8MVspMZZV4xlqSe51Ilq-trYLhP5brS7Nu"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);

}
