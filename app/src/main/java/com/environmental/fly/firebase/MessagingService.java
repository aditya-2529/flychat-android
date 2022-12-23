package com.environmental.fly.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.environmental.fly.R;
import com.environmental.fly.activities.ChatActivity;
import com.environmental.fly.models.User;
import com.environmental.fly.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String token){
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name = message.getData().get( Constants.KEY_NAME);
        user.token = message.getData().get(Constants.KEY_FLY_TOKEN) ;
        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent( this, ChatActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity( this,  0, intent,  0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(  this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(user.name);
        builder.setContentText(message.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText (
                message.getData().get(Constants . KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence cN = "ChatMessage";
            String cD = "This notification channel is used for chat message notifications";
            int important = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, cN, important);
            channel.setDescription(cD);
            NotificationManager nM = getSystemService(NotificationManager.class);
            nM.createNotificationChannel(channel);
        }

        NotificationManagerCompat nmc = NotificationManagerCompat.from(this);
        nmc.notify(notificationId, builder.build());
    }
}
