package com.revyuk.myterminal;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.revyuk.myterminal.adapters.AutocompleteAdapter;
import com.revyuk.myterminal.model.PredictionsResponse;
import com.revyuk.myterminal.model.geocoding.GeocodingResponse;

public class MapsFragment extends Fragment implements GoogleMap.OnMapClickListener, GoogleApiHelper.GoogleApiHelperCallback, AdapterView.OnItemClickListener {
    private LatLng myPosition;
    private GoogleMap map;
    private Marker marker;
    GoogleApiHelper apiHelper;

    private AutocompleteAdapter adapter;
    private EditText text_location;
    private ListView list_location;

    public MapsFragment() {
    }

    public static MapsFragment getInstance(LatLng position) {
        MapsFragment fragment = new MapsFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", position.latitude);
        bundle.putDouble("lng", position.longitude);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            myPosition = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
        } else {
            myPosition = new LatLng(0,0);
        }
        apiHelper = GoogleApiHelper.newInstance(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapFragment mapFragment = MapFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(new MapReady());
        text_location = (EditText) view.findViewById(R.id.text_location);
        text_location.addTextChangedListener(new TextWatcher());
        list_location = (ListView) view.findViewById(R.id.list_location);
        list_location.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(marker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            marker = map.addMarker(markerOptions);
        }
        marker.setPosition(latLng);
        marker.setTitle("My new position");
        marker.showInfoWindow();

        Message msg = new Message();
        msg.what = MainActivity.HANDLE_MSG_THREE;
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", latLng.latitude);
        bundle.putDouble("lng", latLng.longitude);
        msg.setData(bundle);
        ((MainActivity)getActivity()).handler.sendMessage(msg);
    }

    @Override
    public void onResult(boolean success, int who, String response) {
        Gson gson = new Gson();
        if(success) {
            switch (who) {
                case GoogleApiHelper.WHO_AUTOTEXT:
                    PredictionsResponse predictionsResponse = gson.fromJson(response, PredictionsResponse.class);
                    if(predictionsResponse.getStatus().equalsIgnoreCase("OK")) {
                        if(adapter == null) {
                            adapter = new AutocompleteAdapter(getActivity(), R.layout.autocomplete_item, predictionsResponse.getPredictions());
                            list_location.setAdapter(adapter);
                        } else {
                            adapter.clear();
                            adapter.addAll(predictionsResponse.getPredictions());
                            adapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case GoogleApiHelper.WHO_DETAIL_PLACE:
                    Log.d("XXX", response);
                    GeocodingResponse geoResponse = gson.fromJson(response, GeocodingResponse.class);
                    if(geoResponse.getStatus().equalsIgnoreCase("OK")) {

                    }
                    break;
            }
        } else {
            Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Log.d("XXX", adapter.getItem(position).toString());
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                String placeId = adapter.getItem(position).getPlaceId();
                apiHelper.detailPlace(placeId);
            }
        });
    }

    private class MapReady implements OnMapReadyCallback {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.setOnMapClickListener(MapsFragment.this);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPosition, 16);
            map.moveCamera(cameraUpdate);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(myPosition);
            markerOptions.title("My current position");
            marker = map.addMarker(markerOptions);
            marker.showInfoWindow();
        }
    }

    private class TextWatcher implements android.text.TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String text = s.toString();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    apiHelper.autoText(text);
                }
            });
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
