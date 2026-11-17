package com.jaims.privacyneedle;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jaims.privacyneedle.ui.PostDetailsActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "privacy_needle_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        String imageUrl = remoteMessage.getData().get("image");
        String url = remoteMessage.getData().get("url");

        if (remoteMessage.getNotification() != null) {
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (message == null) message = remoteMessage.getNotification().getBody();
            if (imageUrl == null && remoteMessage.getNotification().getImageUrl() != null) {
                imageUrl = remoteMessage.getNotification().getImageUrl().toString();
            }
        }
        if (title == null || title.isEmpty()) title = "Privacy Needle";
        if (message == null || message.isEmpty()) message = "New privacy update available";

        sendNotification(title, message, imageUrl, url);
    }

    private void sendNotification(String title, String message, String imageUrl, String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Privacy Needle Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = (int) (System.currentTimeMillis() & 0x7fffffff);
        // A unique request code prevents older notifications from opening the latest article.
        Intent intent;
        if (url != null && (url.startsWith("https://privacyneedle.com/")
                || url.startsWith("https://www.privacyneedle.com/"))) {
            intent = new Intent(this, PostDetailsActivity.class);
            intent.putExtra("url", url);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.notifications_unread_24px)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Show useful text immediately; downloading a remote image must not block it.
        notificationManager.notify(notificationId, builder.build());

        if (imageUrl != null && imageUrl.startsWith("https://")) {
            final String summary = message;
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap,
                                                    Transition<? super Bitmap> transition) {
                            builder.setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(bitmap)
                                    .setSummaryText(summary));
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                                    || ContextCompat.checkSelfPermission(
                                    MyFirebaseMessagingService.this,
                                    Manifest.permission.POST_NOTIFICATIONS)
                                    == PackageManager.PERMISSION_GRANTED) {
                                notificationManager.notify(notificationId, builder.build());
                            }
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // The text notification has already been shown.
                        }
                    });
        }
    }
}
