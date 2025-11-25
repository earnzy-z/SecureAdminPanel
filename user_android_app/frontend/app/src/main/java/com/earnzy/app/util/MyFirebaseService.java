package com.earnzy.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseService";
    private static final String CHANNEL_ID = "default_channel";
    private static int notificationId = 0; // Increment for unique IDs

    @Override
    public void onCreate() {
        super.onCreate();
        // Create notification channel once during service initialization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Earnzy app notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // Save token to SharedPreferences
        getSharedPreferences("sp", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply();
        // Optionally, send token to server (e.g., via API call)
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received: " + remoteMessage.getData());

        String title = "New Notification";
        String message = "You have a message";
        String type = null;

        // Prioritize notification payload if present (used when app is in background)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        } else if (remoteMessage.getData().size() > 0) {
            // Fallback to data payload
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("message");
            type = remoteMessage.getData().get("type");
        }

        // Handle data payload for custom logic
        if (type != null) {
            switch (type) {
                case "SIGNUP_BONUS":
                    Log.d(TAG, "Signup bonus received: " + remoteMessage.getData().get("bonusAmount"));
                    // Optionally, broadcast to update UI
                    break;
                case "REFERRAL_SUCCESS":
                    Log.d(TAG, "Referral success: " + remoteMessage.getData().get("bonusAmount"));
                    // Optionally, broadcast to update UI
                    break;
            }
        }

        // Only show notification if title and message are valid
        if (title != null && message != null) {
            showNotification(title, message);
        } else {
            Log.e(TAG, "Invalid notification payload: title=" + title + ", message=" + message);
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your app's notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(notificationId++, builder.build());
        Log.d(TAG, "Notification shown: " + title);
    }

    private Bitmap getBitmapFromURL(String strURL) {
        if (strURL == null || strURL.isEmpty()) return null;
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            Log.d(TAG, "Successfully fetched notification image");
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch image: " + e.getMessage());
            return null;
        }
    }
}