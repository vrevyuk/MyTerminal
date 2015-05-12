package com.revyuk.myterminal;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class MapsFragment extends Fragment {
    private LatLng myPosition;
    private GoogleMap map;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapFragment mapFragment = MapFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(new MapReady());
        return view;
    }

    public class MapReady implements OnMapReadyCallback {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.d("XXX", "map load");
            map = googleMap;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPosition, 14);
            map.moveCamera(cameraUpdate);
        }
    }
}
