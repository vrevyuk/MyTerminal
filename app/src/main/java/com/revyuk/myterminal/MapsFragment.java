package com.revyuk.myterminal;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
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
import com.revyuk.myterminal.model.Prediction;
import com.revyuk.myterminal.model.PredictionsResponse;
import com.revyuk.myterminal.model.geocoding.GeocodingResponse;
import com.revyuk.myterminal.model.geocoding.GeocodingResult;

public class MapsFragment extends Fragment implements GoogleMap.OnMapClickListener, GoogleApiHelper.GoogleApiHelperCallback, AdapterView.OnItemClickListener {
    private LatLng myPosition;
    private GoogleMap map;
    private Marker marker;
    GoogleApiHelper apiHelper;

    private AutocompleteAdapter adapter;
    private AutoCompleteTextView text_location;
    //private ListView list_location;

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
        new AlertDialog.Builder(getActivity()).setTitle(" ").setMessage(getActivity().getString(R.string.map_goal)).setPositiveButton("Ok", null).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapFragment mapFragment = MapFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(new MapReady());
        text_location = (AutoCompleteTextView) view.findViewById(R.id.text_location);
        text_location.addTextChangedListener(new TextWatcher());
        text_location.setOnItemClickListener(this);
        //list_location = (ListView) view.findViewById(R.id.list_location);
        //list_location.setOnItemClickListener(this);
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
                    //Log.d("XXX", predictionsResponse.getStatus()+ " > prediction: " + predictionsResponse.getPredictions().size());
                    if(predictionsResponse.getStatus().equalsIgnoreCase("OK")) {
                        adapter = new AutocompleteAdapter(getActivity(), R.layout.autocomplete_item, predictionsResponse.getPredictions());
                        text_location.setAdapter(adapter);
                    }
                    break;
                case GoogleApiHelper.WHO_DETAIL_PLACE:
                    //Log.d("XXX", response);
                    GeocodingResponse geoResponse = gson.fromJson(response, GeocodingResponse.class);
                    if(geoResponse.getStatus().equalsIgnoreCase("OK") && geoResponse.getResult() != null) {
                        adapter.clear(); adapter.notifyDataSetChanged();
                        GeocodingResult geo = geoResponse.getResult();
                        LatLng pos = new LatLng(geo.geometry.location.lat, geo.geometry.location.lng);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pos, 15);
                        map.moveCamera(cameraUpdate);
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(text_location.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    break;
            }
        } else {
            Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        //Log.d("XXX", adapter.getItem(position).toString());
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Prediction place = adapter.getItem(position);
                apiHelper.detailPlace(place.getPlaceId());
                text_location.setText(place.getDescription());
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
            map.clear();
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
