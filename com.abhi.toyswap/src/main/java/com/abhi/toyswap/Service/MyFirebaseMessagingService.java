package com.abhi.toyswap.Service;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.ChatActivity;
import com.abhi.toyswap.activity.DashboardActivity;
import com.abhi.toyswap.activity.LoginActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Abhi";
    public static String message="";
    public static final String BROADCAST_ACTION = "com.abhi.toyswap.activity.ChatActivity.refreshMessages";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());



        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        if( isForeground()){
            refreshUI(remoteMessage);
        }else{
            sendNotification(remoteMessage);

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    public void refreshUI(RemoteMessage messageBody){
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("notificationId", 0);
        intent.putExtra("IsFromNotification",true);
        intent.putExtra("ItemId",messageBody.getData().get("itemId"));
        intent.putExtra("ItemName",messageBody.getData().get("itemName"));

        intent.putExtra("ItemUserId",messageBody.getData().get("itemUserid"));

        intent.putExtra("SendersUserId",messageBody.getData().get("senderUserid"));
        intent.putExtra("SendersImageUrl",messageBody.getData().get("largeIcon"));
        intent.putExtra("ChatData",intent.getExtras());
        sendBroadcast(intent);
    }
    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(RemoteMessage messageBody) {

        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("notificationId", 0);
        intent.putExtra("IsFromNotification",true);
        intent.putExtra("ItemId",messageBody.getData().get("itemId"));
        intent.putExtra("ItemName",messageBody.getData().get("itemName"));
        Log.i("Abhi","USerID of owner="+messageBody.getData().get("itemUserid"));
        intent.putExtra("ItemUserId",messageBody.getData().get("itemUserid"));

        intent.putExtra("SendersUserId",messageBody.getData().get("senderUserid"));
        intent.putExtra("SendersImageUrl",messageBody.getData().get("largeIcon"));
        PendingIntent pendingIntent= PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.app_title);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setSound(defaultSoundUri);
        builder.setVibrate(new long[] { 1000, 1000});

        builder.setLargeIcon(getBitmapFromURL(messageBody.getData().get("largeIcon")));
        builder.setContentTitle(messageBody.getData().get("title"));
        if(message.isEmpty()){
            message=messageBody.getData().get("message")+"\n";

        }else{
            message=message+"\n"+messageBody.getData().get("message")+"\n";

        }
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setContentText(message);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        // builder.setStyle(new NotificationCompat.MessagingStyle(messageBody.getData().get("title")).setConversationTitle("Q&A Group")
        //        .addMessage(messageBody.getData().get("message"),0,messageBody.getData().get("title")));
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Notification notification=builder.build();
        // notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0 /* ID of notification */, builder.build());
    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
    public boolean isForeground() {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
            ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
            return componentInfo.getClassName().equals("com.abhi.toyswap.activity.ChatActivity");
        }catch (Exception e){
            return false;
        }
        //   Log.i("Abhi","Class name="+componentInfo.getClassName());
        //   Log.i("Abhi","PAckage Name="+componentInfo.getPackageName());
        // return componentInfo.getPackageName().equals(myPackage);
    }
}