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

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private GoogleApiHelper apiHelper;
    private double[] lats, lngs;
    private LatLng myLatLng;
    String[] anchor_title, anchor_snippet, ids;
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
            ids = intent.getStringArrayExtra("id");
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
                apiHelper = GoogleApiHelper.newInstance(new ApiHelperCallback());
                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                mMap.setOnInfoWindowClickListener(new InfoWindowClickListener());
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
            mMap.addMarker(new MarkerOptions().position(new LatLng(lats[i], lngs[i])).title(anchor_title[i]).snippet(ids[i]).draggable(false));
            bounds = bounds.include(new LatLng(lats[i], lngs[i]));
            //Log.d("XXX", "Add "+lats[i]+", "+lngs[i]);
        }
        bounds = bounds.include(myLatLng);
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("My position").snippet("0").draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
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
            Log.d("XXX", response);
            Gson gson = new Gson();
            TerminalsServerResponse serverResponse = gson.fromJson(response, TerminalsServerResponse.class);
            if(serverResponse.isSuccess()) {
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
            title.setText(marker.getTitle());
            return view;
        }
    }

    private class InfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            String id = marker.getSnippet();
            View feedbackView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.feedback_dialog_message, null, false);
            final EditText feedbackMessage = (EditText) feedbackView.findViewById(R.id.feedback_message);
            feedbackMessage.setTag(id);
            new AlertDialog.Builder(MapsActivity.this).setTitle("Feedback")
                    .setView(feedbackView)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            apiHelper.sendFeedback(GoogleApiHelper.SEND_FEEDBACK, (String)feedbackMessage.getTag(), feedbackMessage.getText().toString());
                            Log.d("XXX", "feedback id: "+feedbackMessage.getTag()+" message: "+feedbackMessage.getText().toString());
                        }
                    }).show();
        }
    }
}
