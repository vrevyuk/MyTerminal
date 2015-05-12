package com.revyuk.myterminal;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.revyuk.myterminal.model.geocoding.Bounds;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private double[] lats, lngs;
    private LatLng myLatLng;
    String[] anchor_title, anchor_snippet;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        if(intent != null) {
            lats = intent.getDoubleArrayExtra("lats");
            lngs = intent.getDoubleArrayExtra("lngs");
            myLatLng = new LatLng(intent.getDoubleExtra("lat", 0),intent.getDoubleExtra("lng", 0));
            anchor_title = intent.getStringArrayExtra("title");
            anchor_snippet = intent.getStringArrayExtra("snippet");
        }
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_key));
        adView.setAdSize(AdSize.SMART_BANNER);
        ((LinearLayout)findViewById(R.id.ads_layout_on_map)).addView(adView);
        // .addTestDevice("B753262F3A437B6B499FE87973C01D79")
        adView.loadAd(new AdRequest.Builder().addTestDevice("B753262F3A437B6B499FE87973C01D79").build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                addMarkers();
            }
        });
        adView.resume();
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

    private void addMarkers() {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for(int i=0; i<lats.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(lats[i], lngs[i])).title(anchor_title[i]).snippet(anchor_snippet[i]).draggable(false));
            bounds = bounds.include(new LatLng(lats[i], lngs[i]));
            //Log.d("XXX", "Add "+lats[i]+", "+lngs[i]);
        }
        bounds = bounds.include(myLatLng);
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("My position").draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.moveCamera(cameraUpdate);
                mMap.setOnCameraChangeListener(null);
            }
        });
    }
}
