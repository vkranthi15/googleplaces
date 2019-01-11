package com.example.yasaswy.googleplaces;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yasaswy.googleplaces.utils.AppConstants;
import com.example.yasaswy.googleplaces.utils.SharedPref;

public class SplashActivity extends AppCompatActivity {
    Context mContext;
    GPSTracker gps;
    private String latString, longString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;

        SharedPref.init(getApplicationContext());

        splash();


    }


    private void loadGpsToPref() {
        gps = new GPSTracker(mContext);

        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            latString = String.valueOf(latitude);
            longString = String.valueOf(longitude);
            SharedPref.write(AppConstants.PREF_CURRENT_LAT, latString);
            SharedPref.write(AppConstants.PREF_CURRENT_LONG, longString);
            splash();


        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            //gps.showSettingsAlert();

            showSettingsAlert();
        }

    }


    private void splash() {

        final boolean _active = true;
        final int _splashTime = 3000;

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (_active && (waited < _splashTime)) {
                        sleep(100);
                        if (_active) {
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    Intent intent = new Intent(mContext, MapsActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        };
        splashTread.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            loadGpsToPref();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);

            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                showSettingsAlert();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

}
