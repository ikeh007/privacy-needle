package com.jaims.privacyneedle.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.jaims.privacyneedle.QuizActivity;
import com.jaims.privacyneedle.R;
import com.jaims.privacyneedle.models.QuizQuestion;

public class QuizReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        Intent quizIntent = new Intent(context, QuizActivity.class);
        quizIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                quizIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_quiz_channel")
                .setSmallIcon(R.drawable.clarify_24px) // your notification icon
                .setContentTitle("Daily Quiz Reminder")
                .setContentText("Test your Data Privacy knowledge today!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW) // silent
                .setSilent(true); // ensures no sound or vibration

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "daily_quiz_channel",
                    "Daily Quiz Reminder",
                    NotificationManager.IMPORTANCE_LOW // silent
            );
            channel.setDescription("Channel for daily quiz notifications");
            channel.setSound(null, null); // no sound
            channel.enableVibration(false); // no vibration

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
