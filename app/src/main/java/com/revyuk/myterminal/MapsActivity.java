package com.revyuk.myterminal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.revyuk.myterminal.model.TerminalsServerResponse;
import com.revyuk.myterminal.model.geocoding.Bounds;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private GoogleApiHelper apiHelper;
    private double[] lats, lngs;
    private LatLng myLatLng;
    String[] anchor_title, anchor_snippet, ids, providers;
    AdView adView;
    Marker currentMarker;
    Map<String, String> idTerminalMarker = new HashMap<>();
    ImageButton feedbackBtn;
    String[] feedbackMessages = new String[] {"терминал отсутствует","не той терминальной сети","нет услуги в терминале","терминал не работает"," Спасибо, все ок"};

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
            ids = intent.getStringArrayExtra("id");
            providers = intent.getStringArrayExtra("provider");
        }

        feedbackBtn = (ImageButton) findViewById(R.id.feedbackButton);
        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentMarker == null) return;
                final String id = idTerminalMarker.get(currentMarker.getId());
                new AlertDialog.Builder(MapsActivity.this).setTitle(currentMarker.getTitle())
                        .setItems(feedbackMessages, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                apiHelper.sendFeedback(GoogleApiHelper.SEND_FEEDBACK, String.valueOf(id), feedbackMessages[which]);
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_key));
        adView.setAdSize(AdSize.SMART_BANNER);
        ((LinearLayout)findViewById(R.id.ads_layout_on_map)).addView(adView);
        // .addTestDevice("B753262F3A437B6B499FE87973C01D79")
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setTrafficEnabled(false);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                apiHelper = GoogleApiHelper.newInstance(new ApiHelperCallback());
                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        feedbackBtn.setVisibility(View.GONE);
                        currentMarker = null;
                    }
                });
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
            Marker marker;
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lats[i], lngs[i])).title(anchor_title[i]).snippet(providers[i]).draggable(false));
            idTerminalMarker.put(marker.getId(), ids[i]);
            bounds = bounds.include(new LatLng(lats[i], lngs[i]));
            //Log.d("XXX", "Add "+lats[i]+", "+lngs[i]);
        }
        bounds = bounds.include(myLatLng);
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("Вы").snippet("").draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), 100);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.moveCamera(cameraUpdate);
                mMap.setOnCameraChangeListener(null);
            }
        });
    }

    private class ApiHelperCallback implements GoogleApiHelper.GoogleApiHelperCallback {

        @Override
        public void onResult(boolean success, int who, String response) {
            Gson gson = new Gson();
            TerminalsServerResponse serverResponse = gson.fromJson(response, TerminalsServerResponse.class);
            if(serverResponse.isSuccess()) {
                feedbackBtn.setVisibility(View.GONE);
                currentMarker = null;
                new AlertDialog.Builder(MapsActivity.this).setTitle(" ")
                        .setMessage("Спасибо за комментарий.")
                        .setNeutralButton("Ok", null)
                        .show();
            } else {
                Toast.makeText(MapsActivity.this, "Ошибка сервера, попробуйте позже.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View view = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_infoview, null, false);
            TextView title = (TextView) view.findViewById(R.id.markerTitle);
            TextView snippet = (TextView) view.findViewById(R.id.markerSnippet);
            title.setText(marker.getTitle()==null?"":marker.getTitle());
            snippet.setText(marker.getSnippet()==null?"":marker.getSnippet());
            currentMarker = marker;
            feedbackBtn.setVisibility(View.VISIBLE);
            return view;
        }
    }
}
