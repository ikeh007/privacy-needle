package com.jaims.privacyneedle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jaims.privacyneedle.ui.PostDetailsActivity;

import android.graphics.drawable.Drawable;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "privacy_needle_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        String imageUrl = remoteMessage.getData().get("image"); // <-- no fallback here
        String url = remoteMessage.getData().get("url");
        String postId = remoteMessage.getData().get("post_id");

        // Fallbacks only for title and message
        if (title == null) title = "PrivacyNeedle";
        if (message == null) message = "New privacy update available";

        sendNotification(title, message, imageUrl, url, postId);
    }

    private void sendNotification(String title, String message, String imageUrl, String url, String postId) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "PrivacyNeedle Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("post_id", postId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        int notificationId = (int) System.currentTimeMillis();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.pneedle_bg)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Only load image if URL is provided
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            builder.setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(resource)
                                    .bigLargeIcon((Icon) null)
                                    .setSummaryText(message));
                            notificationManager.notify(notificationId, builder.build());
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) { }

                        @Override
                        public void onLoadFailed(Drawable errorDrawable) {
                            // Show notification without image
                            notificationManager.notify(notificationId, builder.build());
                        }
                    });
        } else {
            // No image, just show text notification
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
