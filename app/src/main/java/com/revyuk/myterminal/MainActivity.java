package com.revyuk.myterminal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity  implements MainFragment.SelectorFragment {
    public static final int HANDLE_MSG_ONE = 1;
    public static final int HANDLE_MSG_TWO = 2;
    public static final int HANDLE_MSG_THREE = 3;

    public AdView adView;
    public GoogleApiClient googleApiClient;
    private LatLng myPos;

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle;
            switch (msg.what) {
                case HANDLE_MSG_THREE:
                    myPos = new LatLng(msg.getData().getDouble("lat"), msg.getData().getDouble("lng"));
                case HANDLE_MSG_ONE:
                    //if(msg.getData() != null) Log.d("XXX", "LAT: "+msg.getData().getDouble("lat")+", LNG: "+msg.getData().getDouble("lng"));
                    SearchFragment fragment = new SearchFragment();
                    bundle = new Bundle();
                    bundle.putDouble("lat", myPos.latitude);
                    bundle.putDouble("lng", myPos.longitude);
                    fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction()
                            .add(R.id.container, fragment)
                            .commit();
                    return true;
                case  HANDLE_MSG_TWO:
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, MapsFragment.getInstance(myPos))
                            .commit();
                    return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 69);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    MainActivity.this.finish();
                }
            });
            dialog.show();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_key));
        adView.setAdSize(AdSize.SMART_BANNER);
        ((LinearLayout)findViewById(R.id.ads_layout)).addView(adView);
        // .addTestDevice("B753262F3A437B6B499FE87973C01D79")
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onPause() {
        adView.pause();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
    }

    public LatLng getMyLocation() {
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        googleApiClient.connect();

        if (googleApiClient.isConnected()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);

            return new LatLng(location.getLatitude(), location.getLongitude());
        }

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

    @Override
    public void searchFragment(LatLng curPosition) {
        myPos = curPosition!=null?curPosition:getMyLocation();
        Message msg = new Message();
        msg.what = HANDLE_MSG_ONE;
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", myPos.latitude);
        bundle.putDouble("lng", myPos.longitude);
        msg.setData(bundle);
        handler.sendMessage(msg);
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

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        if(fragment instanceof MapsFragment) {
            handler.sendEmptyMessage(HANDLE_MSG_ONE);
            return;
        }
        super.onBackPressed();
    }
}
