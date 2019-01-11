package com.example.yasaswy.googleplaces;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yasaswy.googleplaces.utils.AppConstants;
import com.example.yasaswy.googleplaces.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    ListView placesListView;
    ArrayList<String> placesList;
    ArrayList<String> placesTypes;
    Integer[] placesImages = {
            R.drawable.hospital,
            R.drawable.airport,
            R.drawable.bank,
            R.drawable.busnew,
            R.drawable.nightnew,
            R.drawable.salon,
            R.drawable.police,
            R.drawable.doctor,
            R.drawable.schools,
            R.drawable.bars
    };
    Context mContext;
    GPSTracker gps;
    private String latString, longString;
    SharedPreferences mPref;
    SharedPreferences.Editor mPrefEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        placesListView = (ListView) findViewById(R.id.placesListView);
        placesList = new ArrayList<>();
        placesTypes = new ArrayList<>();
        mContext = this;



        locationAccessTask();

        loadplacesListAndImages();

        PlacesListAdapter adapter = new PlacesListAdapter(mContext, placesList, placesImages, placesTypes);
        placesListView.setAdapter(adapter);


    }

    private void loadGpsToPref() {
        gps = new GPSTracker(mContext);

        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            latString = String.valueOf(latitude);
            longString = String.valueOf(longitude);


        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        SharedPref.write(AppConstants.PREF_CURRENT_LAT, latString);
        SharedPref.write(AppConstants.PREF_CURRENT_LONG, longString);

    }

    private static final int ACCESS_LOCATION = 126;

    @AfterPermissionGranted(ACCESS_LOCATION)

    //Manifest.permission.ACCESS_FINE_LOCATION,2
    public void locationAccessTask() {
        if (EasyPermissions.hasPermissions(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            loadGpsToPref();
           // Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
//            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } else
            EasyPermissions.requestPermissions(this, "This app needs location permission", ACCESS_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        loadGpsToPref();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        locationAccessTask();
    }

    private void loadplacesListAndImages() {

        placesList.add("Hospital");
        placesList.add("Airport");
        placesList.add("Banks");
        placesList.add("Bus Station");
        placesList.add("Night Club");
        placesList.add("Beauty Salon");
        placesList.add("police Station");
        placesList.add("Doctor");
        placesList.add("Schools");
        placesList.add("Bars");

        placesTypes.add("hospital");
        placesTypes.add("airport");
        placesTypes.add("bank");
        placesTypes.add("bus_station");
        placesTypes.add("night_club");
        placesTypes.add("beauty_salon");
        placesTypes.add("police");
        placesTypes.add("doctor");
        placesTypes.add("school");
        placesTypes.add("bar");


    }
}
