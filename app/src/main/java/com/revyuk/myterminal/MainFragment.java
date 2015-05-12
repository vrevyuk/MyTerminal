package com.revyuk.myterminal;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.zip.Inflater;

/**
 * Created by Notebook on 07.04.2015.
 */
public class MainFragment extends Fragment {
    SelectorFragment selector;

    public MainFragment() {
    }

    interface SelectorFragment {
        void searchFragment(LatLng curPosition);
    }

    public static MainFragment newInstance(Context context) {
        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();
        mainFragment.setArguments(bundle);
        return mainFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        selector = (SelectorFragment) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selector.searchFragment(null);
            }
        });
        return view;
    }
}
