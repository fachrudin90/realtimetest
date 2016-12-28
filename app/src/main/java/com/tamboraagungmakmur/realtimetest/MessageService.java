package com.tamboraagungmakmur.realtimetest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tamboraagungmakmur.realtimetest.Utils.AppConf;
import com.tamboraagungmakmur.realtimetest.Utils.NotificationContent;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Tambora on 30/11/2016.
 */
public class MessageService extends Service {

    private Handler handler;
    private Handler handlerMsg;
    private Socket mSocket;
    private String username;
    private String usermsg, msg;
    private int interval = 30000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("MEvent", "onStartCommand ");

        handler = new Handler();
        handlerMsg = new Handler();

        try {
            mSocket = IO.socket(AppConf.URL_SERVER);
        } catch (URISyntaxException e) {
        }

        mSocket.connect();
        username = getApplicationContext().getSharedPreferences(AppConf.USERNAME_PREF, 0).getString("user", null);
        mSocket.emit("adduser", username);
        mSocket.on("updatechat", onNewMessage);
        handler.postDelayed(runnable, interval);
        LocalBroadcastManager.getInstance(this).registerReceiver(onSendMessage, new IntentFilter(getString(R.string.sendmsg)));

        return START_STICKY;
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            if (args.length > 0) {
                MessageService.this.usermsg = args[0].toString();
                MessageService.this.msg = args[1].toString();
                String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
                SharedPreferences.Editor editor = getSharedPreferences(AppConf.LASTPING_PREF, 0).edit();
                editor.putString(getString(R.string.ping), currentTime);
                editor.commit();
                handlerMsg.post(runnableMsg);
            }
        }
    };

    private Runnable runnableMsg = new Runnable() {
        @Override
        public void run() {

            if (msg != null && usermsg != null) {
                Log.i(MessageService.class.getSimpleName(), msg);
                if (!usermsg.toLowerCase().equals("server") && !msg.equals(getString(R.string.ping))) {
                    if (getSharedPreferences(AppConf.ISOPEN_PREF, 0).getInt("state", 0) == 0 && !msg.equals(getString(R.string.istyping))) {
                        NotificationContent.push(MessageService.this.getApplicationContext(), new Random().nextInt(900) + 100, usermsg, msg, MainActivity.class);
                    }
                    Intent intent = new Intent(MessageService.this.getString(R.string.getmsg));
                    intent.putExtra(AppConf.USERNAME_PREF, usermsg);
                    intent.putExtra(getString(R.string.message), msg);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }


        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getDefault());
            String currentDateandTime = sdf.format(new Date());

            File file;
            FileOutputStream outputStream;
            try {
                file = new File(Environment.getExternalStorageDirectory(), "Listener.log");
                outputStream = new FileOutputStream(file);
                outputStream.write(currentDateandTime.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */


            String checkPing = getSharedPreferences(AppConf.LASTPING_PREF, 0).getString(getString(R.string.ping), null);
            String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            long difference = 0;
            if (checkPing != null) {
                try {
                    difference = format.parse(currentTime).getTime() - format.parse(checkPing).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Log.i("MEvent", String.valueOf(difference));

            if (difference > (interval + (interval / 4)) || difference < 0) {
                Restarting();
            }

            if (mSocket != null) {
                mSocket.emit("pm", username, getString(R.string.ping));

            }

            if (handler != null) {
                handler.postDelayed(runnable, interval);
            }

        }
    };

    private BroadcastReceiver onSendMessage = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String user = bundle.getString(AppConf.USERNAME_PREF);
                String msg = bundle.getString(getString(R.string.message));
                MessageService.this.mSocket.emit("pm", user, msg);
            }
        }
    };


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        Restarting();

    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("MEvent", "onTaskRemoved ");
        // TODO Auto-generated method stub

        Restarting();


    }

    public void Restarting() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSendMessage);
        mSocket.disconnect();
        mSocket.off("updatechat", onNewMessage);
        handler.removeCallbacks(runnable);
        handlerMsg.removeCallbacks(runnableMsg);
        handler = null;
        handlerMsg = null;

        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
    }
}
