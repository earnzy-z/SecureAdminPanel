package com.earnzy.app;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
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

public class FCMService extends FirebaseMessagingService {

    // This ID is used for both creating the channel and building the notification.
    private static final String CHANNEL_ID = "earnzy_notifications_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // This part was already correct. It correctly parses the incoming message.
        String title = "";
        String body = "";
        String imageUrl = "";

        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            imageUrl = remoteMessage.getData().get("image");
        }

        // Fallback to the notification payload if data payload is empty
        if (remoteMessage.getNotification() != null) {
            if (title == null || title.isEmpty()) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (body == null || body.isEmpty()) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        // A title and body are necessary to show a notification.
        if (title != null && !title.isEmpty() && body != null && !body.isEmpty()) {
            sendNotification(title, body, imageUrl);
        }
    }

    private void sendNotification(String title, String message, String imageUrl) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // --- THE PRIMARY FIX IS HERE ---
        // We now correctly use the CHANNEL_ID defined above for the builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // FIXED: This line is MANDATORY for all notifications.
                // It sets the small icon that appears in the status bar. Without it, the app will crash.
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // This part handles the big image and was already correct.
        if (imageUrl != null && imageUrl.startsWith("http")) {
            Bitmap bigImage = getBitmapFromURL(imageUrl);
            if (bigImage != null) {
                NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                        .bigPicture(bigImage)
                        .setSummaryText(message);
                builder.setStyle(style);
            }
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // BEST PRACTICE: This code creates the channel if it doesn't exist.
        // It's safe to call this every time because creating an existing channel does nothing.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "General Notifications", // This is the user-visible name in app settings
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications from Earnzy");
            manager.createNotificationChannel(channel);
        }

        // This will now work correctly because the small icon is set.
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private Bitmap getBitmapFromURL(String src) {
        // This utility function was already correct.
        try {
            URL url = new URL(src);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream input = conn.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e("BitmapError", "Failed to get bitmap from URL: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onNewToken(String token) {
        // This is important for server-side logic but not related to the crash.
        Log.d("FCM_TOKEN", "Refreshed token: " + token);
        // Here you would typically send the new token to your server.
    }
}