package com.cueaudio.engine_consumer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cueaudio.engine.CUEEngine;

public class UltrasoundActivity extends AppCompatActivity {


    Button btnStartService,btnStopService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultrasound);

        btnStartService= btnStartService.findViewById(R.id.button);
        btnStopService= btnStopService.findViewById(R.id.button2);

        checkPermission();


        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                    Intent intent  = new Intent(UltrasoundActivity.this,TransmissionStarter.class);
                    intent.putExtra("delay",3000);
                    startForegroundService(intent);
                }else{

                    Intent intent  = new Intent(UltrasoundActivity.this,TransmissionStarter.class);
                    intent.putExtra("delay",3000);

                    startService(intent);
                }
            }
        });

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(UltrasoundActivity.this,TransmissionStarter.class);
                stopService(intent);
            }
        });

    }


    private static final int REQUEST_RECORD_AUDIO = 13;


    private void checkPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[] { android.Manifest.permission.RECORD_AUDIO },
                REQUEST_RECORD_AUDIO
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //check if permission was granted, and confirm that permission was mic access
        boolean permCondition = requestCode == REQUEST_RECORD_AUDIO &&
                grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
        // permission is not granted yet
        if (!permCondition) {
            checkPermission();
            return;
        }

    }
}
