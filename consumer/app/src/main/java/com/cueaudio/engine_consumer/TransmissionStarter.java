package com.cueaudio.engine_consumer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.cueaudio.engine.CUEEngine;
import com.cueaudio.engine.CUEReceiverCallbackInterface;
import com.cueaudio.engine.CUETrigger;
import java.util.Random;


public class TransmissionStarter extends Service {


    boolean isServiceStarted = false;
    int firstOffset=7,//identifying signals from only our app
            secondOffset,thirdOffset;
    String uniqueId;
    private static final String API_KEY = "EH0GHbslb0pNWAxPf57qA6n23w4Zgu5U";
    private static final int NOTIFICATION_ID = 1;
    private static final int DELAY = 3000;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    String ProfileFormat = "audible";


    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(2, createNotification());
    }

    public Notification createNotification() {

        Context mContext;
        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder;
        final String NOTIFICATION_CHANNEL_ID = "10001";
        /**Creates an explicit intent for an Activity in your app**/
        Intent resultIntent = new Intent(TransmissionStarter.this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(TransmissionStarter.this,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(TransmissionStarter.this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Check for distance")
                .setContentText("This is your favourite check for distance service running")
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        return mBuilder.build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            startUltrasoundService();
        } else {

        }
        return START_STICKY;
    }

    private Handler mHandler;

    private HandlerThread mHandlerThread;

    private void startUltrasoundService() {
        if (isServiceStarted) {
            return;
        } else {
            isServiceStarted = true;
            Random rand = new Random();

            secondOffset = rand.nextInt(100);
            thirdOffset = rand.nextInt(999);
            uniqueId =  firstOffset+"."+secondOffset+"."+thirdOffset;
            audioEngineSetup();
            mHandlerThread = new HandlerThread("HandlerThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Your task goes here

                    if (isServiceStarted) {

                        initiateTransmission();
                        mHandler.postDelayed(this, DELAY);

                    } else {

                    }

                }
            }, DELAY);
        }
    }


    private void audioEngineSetup(){

        CUEEngine.getInstance().setupWithAPIKey(this, API_KEY);
        CUEEngine.getInstance().setDefaultGeneration(2);

        CUEEngine.getInstance().setReceiverCallback(new OutputListener());
        enableListening(true);

        final String config = CUEEngine.getInstance().getConfig();
//        Log.v(TAG, config);

        CUEEngine.getInstance().setTransmittingEnabled(true);
    }

    private class OutputListener implements CUEReceiverCallbackInterface {
        @Override
        public void run(@NonNull String json) {
            final CUETrigger model = CUETrigger.parse(json);

            onTriggerHeard(model);

        }
    }

    private void onTriggerHeard(CUETrigger model) {


        if(uniqueId.charAt(0)=='7') {
            if (uniqueId.equals(model.getRawIndices())) {


            } else {
                //vibrateDevice();
                showNotification("Alert! Please maintain social distance" + model.getRawIndices());

            }
        }


    }

    private void vibrateDevice(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    private void showNotification(@NonNull String message) {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final String channelId = getString(R.string.notification_channel_id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final CharSequence name = getString(R.string.notification_channel_name);
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            //noinspection ConstantConditions
            notificationManager.createNotificationChannel(channel);
        }

        final Intent intent = new Intent(this, UltrasoundActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, 0
        );

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 100, 1000, 1000 ,1000})
                .setAutoCancel(true);
        //noinspection ConstantConditions
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void initiateTransmission() {

        triggerTransmission();

    }

    private void triggerTransmission(){

        boolean triggerAsNumber = false;
        int realMode= CUETrigger.MODE_TRIGGER;

        queueInput(uniqueId, realMode, triggerAsNumber);
    }

    private void queueInput(@NonNull String input, int mode, boolean triggerAsNumber) {
        int result;

        switch (mode) {
            case CUETrigger.MODE_TRIGGER:
                if(triggerAsNumber) {
                    long number = Long.parseLong(input);
                    result = CUEEngine.getInstance().queueTriggerAsNumber(number);
                    if( result == -110 ) {
//                        messageLayout.setError(
//                                "Triggers as number sending is unsupported for engine generation 1" );
                    } else if( result < 0 ) /* -120 */ {
//                        messageLayout.setError(
//                                "Triggers us number can not exceed 98611127" );
                    }
                } else {
                    CUEEngine.getInstance().queueTrigger(input);
                }
                break;

            case CUETrigger.MODE_LIVE:
                result = CUEEngine.getInstance().queueLive(input);
                if ( result == -10 ) {
//                    messageLayout.setError(
//                            "Live triggers sending is unsupported for engine generation 2");
                }
                break;

            case CUETrigger.MODE_ASCII:
                result = CUEEngine.getInstance().queueMessage(input);
                if ( result == -10 ) {
//                    messageLayout.setError(
//                            "Message sending is unsupported for engine generatin 2");
                } else if (result < 0) {
                    ///!!! should be fixed some how (but how?)
//                    messageLayout.setError("Ascii stream can't contain more then 10 symbols");
                }
                break;
        }
    }



    private void enableListening(boolean enable) {
        if (enable) {
            CUEEngine.getInstance().startListening();
        } else {
            CUEEngine.getInstance().stopListening();
        }
    }

    @Override
    public void onDestroy() {
        CUEEngine.getInstance().stopListening();
        CUEEngine.getInstance().setTransmittingEnabled(false);

        isServiceStarted= false;

    }
}




