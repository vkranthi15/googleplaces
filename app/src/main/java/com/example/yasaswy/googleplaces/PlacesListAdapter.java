package com.example.yasaswy.googleplaces;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yasaswy.googleplaces.utils.AppConstants;
import com.example.yasaswy.googleplaces.utils.SharedPref;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * Created by yasaswy on 24-04-2017.
 */

public class PlacesListAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<String> placesList;
    Integer[] placesImages;
    LayoutInflater inflater;
    ArrayList<String> placesTypes;

    public PlacesListAdapter(Context mContext, ArrayList<String> placesList, Integer[] placesImages, ArrayList<String> placesTypes) {
        this.mContext = mContext;
        this.placesImages = placesImages;
        this.placesList = placesList;
        this.placesTypes = placesTypes;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return placesList.size();
    }

    @Override
    public Object getItem(int position) {
        return placesImages[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_place_list, null);

        TextView tvCatName = (TextView) convertView.findViewById(R.id.tvCatName);
        ImageView imgCat = (ImageView) convertView.findViewById(R.id.imgCat);
        tvCatName.setText(placesList.get(position));
        Picasso.with(mContext)
                .load(placesImages[position])
                .into(imgCat);


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable(mContext)) {
                    loadMarkers(placesList.get(position), placesTypes.get(position));
                }else{
                    Toast.makeText(mContext, "No Internet Connected", Toast.LENGTH_LONG).show();
                }
            }
        });
        return convertView;
    }

    private void loadMarkers(final String placeName, String placeType) {

        String lat = SharedPref.read(AppConstants.PREF_CURRENT_LAT, "");
        String lng = SharedPref.read(AppConstants.PREF_CURRENT_LONG, "");
        String latLongString = lat + "," + lng;
        WebServices ws = new WebServices(mContext);
        RequestParams params = new RequestParams();
        params.put("location", latLongString);
        params.put("type", placeType);
        params.put("radius", "5000");
        params.put("key", "AIzaSyAGn_q3RsRSfSxfjkfsWO7v4diZy_gq4sI");

        ws.invokeWebService(params, "", new WSResponnse() {
            @Override
            public void onResponse(boolean success, String response) {
                if (success) {

                    Intent intent = new Intent(mContext, MapsActivity.class);
                    intent.putExtra("Response", response);
                    intent.putExtra("Name", placeName);
                    mContext.startActivity(intent);
                    Log.i("response", ">>>>" + response);

                }else{
                    Toast.makeText(mContext, "Your Internet is too slow Please Try Again After Some Time", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || mConnectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else
            return false;
    }

}
