package com.example.yasaswy.googleplaces;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yasaswy.googleplaces.utils.AppConstants;
import com.example.yasaswy.googleplaces.utils.SharedPref;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String response;
    Context mContext;
    SearchView search;
    private GPSTracker gps;
    private AlertDialog alertDialog;
    private Marker marker;
    HashMap<String, Marker> hashMapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext = this;
        hashMapMarker = new HashMap<>();
        loadGpsToPref();
        search = (SearchView) findViewById(R.id.search);
        search.onActionViewExpanded();
        search.setQueryHint("Search cities eg: New York");
        search.setIconified(false);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // loadMarkers(null, query);
                setMap(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            }
        });


    }

    private void setMap(String query) {


        List<Address> addressList = null;

        if (query != null || !query.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(query, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(0)));
                Double latDouble = address.getLatitude();
                Double longDouble = address.getLongitude();

                String addressString = address.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();


                String fullAdrress = addressString + "," + city + "," + state + "," + country + "," + postalCode + ".";


                marker.setTag(fullAdrress);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latDouble, longDouble), 12);
                mMap.moveCamera(cameraUpdate);
                hashMapMarker.put(fullAdrress, marker);

            }else Toast.makeText(mContext, "Location not found", Toast.LENGTH_SHORT).show();
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                    String fullAddres = marker.getTag().toString();
                    showAlertDialog(fullAddres);


                }
            });


        }
    }

    private void showAlertDialog(final String fullAddres) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_show, null);
        builder.setView(view);


        TextView tvEmail = (TextView) view.findViewById(R.id.tvEmail);

        TextView tvDelete = (TextView) view.findViewById(R.id.tvDelete);
        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Marker marker = hashMapMarker.get(fullAddres);
                marker.remove();
                hashMapMarker.remove(fullAddres);

            }
        });


        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareViaEmail(fullAddres);
                alertDialog.dismiss();

            }
        });


        alertDialog = builder.create();

        // show it
        alertDialog.show();


    }

    private void shareViaEmail(String fullAddres) {


        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(fullAddres));
        startActivity(Intent.createChooser(sharingIntent, "Share using"));

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);



/*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


    }

    private void loaMarkersForResponse(GoogleMap mMap) {

        ArrayList<String> latArray = new ArrayList<>();
        ArrayList<String> longArray = new ArrayList<>();
        ArrayList<String> nameArray = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(response);

            JSONArray jArray = jObj.getJSONArray("results");
            if (jArray != null && jArray.length() > 0) {

                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject jInnerObj = jArray.getJSONObject(i);
                    JSONObject jGeometry = jInnerObj.getJSONObject("geometry");
                    JSONObject jLocation = jGeometry.getJSONObject("location");

                    String lat = jLocation.getString("lat");
                    String lng = jLocation.getString("lng");
                    String name = jInnerObj.getString("name");
                    latArray.add(lat);
                    longArray.add(lng);
                    nameArray.add(name);

                }


                for (int j = 0; j < latArray.size(); j++) {

                    String latString = latArray.get(j);
                    String longString = longArray.get(j);
                    String name = nameArray.get(j);

                    Double latDouble = Double.valueOf(latString);
                    Double longDouble = Double.valueOf(longString);

                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latDouble, longDouble)).title(name));


                    if (j == latArray.size() - 1) {

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latDouble, longDouble), 12);
                        mMap.moveCamera(cameraUpdate);

                    }
                }


            } else {
                Toast.makeText(mContext, "No Results Found", Toast.LENGTH_LONG).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void loadGpsToPref() {
        gps = new GPSTracker(mContext);

        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String latString = String.valueOf(latitude);
            String longString = String.valueOf(longitude);
            SharedPref.write(AppConstants.PREF_CURRENT_LAT, latString);
            SharedPref.write(AppConstants.PREF_CURRENT_LONG, longString);


        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            //gps.showSettingsAlert();
        }

    }


    private void loadMarkers(final String placeName, String placeType) {

        String lat = SharedPref.read(AppConstants.PREF_CURRENT_LAT, "");
        String lng = SharedPref.read(AppConstants.PREF_CURRENT_LONG, "");
        String latLongString = "17.23" + "," + "78.27";
        WebServices ws = new WebServices(mContext);
        RequestParams params = new RequestParams();
        params.put("location", latLongString);
        params.put("keyword", placeType);
        params.put("radius", "5000");
        params.put("key", "AIzaSyAGn_q3RsRSfSxfjkfsWO7v4diZy_gq4sI");

        ws.invokeWebService(params, "", new WSResponnse() {
            @Override
            public void onResponse(boolean success, String response) {
                if (success) {

                  /*  Intent intent = new Intent(mContext, MapsActivity.class);
                    intent.putExtra("Response", response);
                    intent.putExtra("Name", placeName);
                    mContext.startActivity(intent);
                    Log.i("response", ">>>>" + response);*/

                } else {
                    Toast.makeText(mContext, "Your Internet is too slow Please Try Again After Some Time", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


}
