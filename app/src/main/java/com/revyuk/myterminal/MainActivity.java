package com.revyuk.myterminal;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        LatLng myPos = getMyLocation();
        SearchFragment fragment;
        fragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", myPos.latitude);
        bundle.putDouble("lng", myPos.longitude);
        fragment.setArguments(bundle);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    public LatLng getMyLocation() {
        LocationManager locationManager;
        Boolean isGPSProvider=false, isNETWORKProvider=false;
        Location location;
        LatLng curr_pos = new LatLng(0,0);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager!=null) {
            isGPSProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNETWORKProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNETWORKProvider) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, new MyLocationListener());
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null) {
                    curr_pos = new LatLng(location.getLatitude(), location.getLongitude());
                }
            } else if(isGPSProvider) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, new MyLocationListener());
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location!=null) {
                    curr_pos = new LatLng(location.getLatitude(), location.getLongitude());
                }
            } else {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        return curr_pos;
    }

    class MyLocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
