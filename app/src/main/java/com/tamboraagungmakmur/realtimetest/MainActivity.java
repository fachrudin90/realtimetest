package com.tamboraagungmakmur.realtimetest;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tamboraagungmakmur.realtimetest.Utils.AppConf;
import com.tamboraagungmakmur.realtimetest.Utils.FormatDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.txUsername)
    EditText txUsername;
    @Bind(R.id.btSave)
    Button btSave;
    @Bind(R.id.txMessage)
    TextView txMessage;
    @Bind(R.id.txTo)
    EditText txTo;
    @Bind(R.id.txText)
    EditText txText;
    @Bind(R.id.btSend)
    Button btSend;

    private String username;
    private boolean isTyping;
    private Handler handler;
    private int interval = 3000;
    private int socketInterval = 30000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        handler = new Handler();
        isTyping = false;

        if (!isMyServiceRunning(MessageService.class)) {
            startService(new Intent(MainActivity.this, MessageService.class));
        }

        btSave.setText("LOGIN (" + getSharedPreferences(AppConf.LASTPING_PREF, 0).getString(getString(R.string.ping), null) + ")");
        username = getSharedPreferences(AppConf.USERNAME_PREF, 0).getString("user", null);
        txUsername.setText(username);

        txText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isTyping && !txText.getText().toString().equals("")) {
                    isTyping = true;
                    Intent intent = new Intent(getString(R.string.sendmsg));
                    intent.putExtra(AppConf.USERNAME_PREF, txTo.getText().toString());
                    intent.putExtra(getString(R.string.message), getString(R.string.istyping));
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    handler.postDelayed(TypingStatus, interval);
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String user = bundle.getString(AppConf.USERNAME_PREF);
            txMessage.append(Html.fromHtml("<b><font color=\"#0a59d8\">" + user + " (" + FormatDate.currentTime() + ") : </font></b>" + bundle.getString(getString(R.string.message)) + "<br>"));
            txTo.setText(user);
        }


        CheckSocket();

    }

    @OnClick({R.id.btSave, R.id.btSend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btSave:
                SharedPreferences.Editor editor = MainActivity.this.getApplicationContext().getSharedPreferences(AppConf.USERNAME_PREF, 0).edit();
                editor.putString("user", txUsername.getText().toString());
                editor.commit();
                stopService(new Intent(MainActivity.this, MessageService.class));
                startService(new Intent(MainActivity.this, MessageService.class));

                break;
            case R.id.btSend:

                Intent intent = new Intent(getString(R.string.sendmsg));
                intent.putExtra(AppConf.USERNAME_PREF, txTo.getText().toString());
                intent.putExtra(getString(R.string.message), txText.getText().toString());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                txMessage.append(Html.fromHtml("<b><font color=\"#000000\">" + MainActivity.this.username + " (" + FormatDate.currentTime() + ") : </font></b>" + txText.getText().toString() + " <br>"));
                txText.setText("");

                break;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(AppConf.ISOPEN_PREF, 0).edit();
        editor.putInt("state", 1);
        editor.commit();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNewMessage);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNewMessage, new IntentFilter(getString(R.string.getmsg)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(AppConf.ISOPEN_PREF, 0).edit();
        editor.putInt("state", 0);
        editor.commit();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNewMessage);
    }

    private BroadcastReceiver onNewMessage = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String user = bundle.getString(AppConf.USERNAME_PREF);
                String msg = bundle.getString(getString(R.string.message));
                if (!msg.equals(getString(R.string.istyping))) {
                    txMessage.append(Html.fromHtml("<b><font color=\"#0a59d8\">" + user + " (" + FormatDate.currentTime() + ") : </font></b>" + msg + "<br>"));
                } else if (!isTyping) {
                    btSave.setText(user + " is typing...");
                    handler.postDelayed(TypingStatus, interval);
                }
            }
        }
    };

    private Runnable TypingStatus = new Runnable() {
        public void run() {
            String checkPing = getSharedPreferences(AppConf.LASTPING_PREF, 0).getString(getString(R.string.ping), null);
            isTyping = false;
            btSave.setText("LOGIN (" + checkPing + ")");
        }
    };


    private void CheckSocket() {
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

        if (difference > (socketInterval + (socketInterval / 4)) || difference < 0) {
            stopService(new Intent(MainActivity.this, MessageService.class));
            startService(new Intent(MainActivity.this, MessageService.class));
        }
    }


}
