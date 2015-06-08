package com.revyuk.myterminal;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.revyuk.myterminal.adapters.TerminalAdapter;
import com.revyuk.myterminal.model.ResultList;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class SearchFragment extends Fragment implements TerminalHttpHelper.HelperCallback, View.OnClickListener {
    SharedPreferences preferences;
    TerminalHttpHelper helper;
    private ProgressDialog pd;
    Spinner serviceSpinner, agentSpinner, radiusSpinner;
    TextView found_count, current_location_text;
    LatLng myLatLng;
    double[] lats, lngs;
    String[] anchor_title, anchor_snippet, ids, providers;
    TerminalAdapter adapter;
    String myAddress;

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getActivity().getSharedPreferences("_preferences", Context.MODE_PRIVATE);

        helper = new TerminalHttpHelper(this);
        Bundle bundle = getArguments();
        if(bundle != null) {
            myLatLng = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.setMessage("Загрузка ...");

        current_location_text = (TextView) view.findViewById(R.id.current_location_text);
        serviceSpinner = (Spinner) view.findViewById(R.id.service);
        agentSpinner = (Spinner) view.findViewById(R.id.agent);
        radiusSpinner = (Spinner) view.findViewById(R.id.radius);

        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_row, getResources().getStringArray(R.array.radius_array));
        radiusAdapter.setDropDownViewResource(R.layout.spinner_row);
        radiusSpinner.setAdapter(radiusAdapter);
        radiusSpinner.setSelection(preferences.getInt("radius", 0));
        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt("radius", position).apply();
                helper.setRadius(getResources().getStringArray(R.array.radius_array)[position]);
                helper.getServices();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        view.findViewById(R.id.as_list_btn).setOnClickListener(this);
        view.findViewById(R.id.show_on_map).setOnClickListener(this);
        view.findViewById(R.id.change_location).setOnClickListener(this);

        found_count = (TextView) view.findViewById(R.id.found_count);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        helper.getMyAddr(myLatLng);
    }

    @Override
    public void onPause() {
        if(pd != null) pd.dismiss();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_location:
                ((MainActivity)getActivity()).handler.sendEmptyMessage(MainActivity.HANDLE_MSG_TWO);
                break;
            case R.id.as_list_btn:
                if(adapter.getCount()==0) {
                    new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.not_found)).setNeutralButton("OK", null).show();
                    return;
                }
                if(adapter.getCount() > 0) new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.list_title)).setAdapter(adapter, new MyOnClickListener()).show();
                break;
            case R.id.show_on_map:
                if(adapter != null && adapter.getCount()==0) {
                    new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.not_found)).setNeutralButton("OK", null).show();
                    return;
                }
                if(lats.length > 0) {
                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    intent.putExtra("lats", lats);
                    intent.putExtra("lngs", lngs);
                    intent.putExtra("lat", myLatLng.latitude);
                    intent.putExtra("lng", myLatLng.longitude);
                    intent.putExtra("title", anchor_title);
                    intent.putExtra("snippet", anchor_snippet);
                    intent.putExtra("id", ids);
                    intent.putExtra("provider", providers);
                    startActivity(intent);
                    pd.show();
                }
                break;
        }
    }

    class MyOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            pd.show();
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            intent.putExtra("lats", new double[]{lats[which]});
            intent.putExtra("lngs", new double[]{lngs[which]});
            intent.putExtra("lat", myLatLng.latitude);
            intent.putExtra("lng", myLatLng.longitude);
            intent.putExtra("title", new String[]{anchor_title[which]});
            intent.putExtra("snippet", new String[]{anchor_snippet[which]});
            intent.putExtra("id", new String[]{ids[which]});
            intent.putExtra("provider", new String[]{providers[which]});
            startActivity(intent);
        }
    }

    @Override
    public void isError(int requestCode) {
        Toast.makeText(getActivity(), "Error in "+requestCode, Toast.LENGTH_SHORT).show();
        pd.dismiss();
    }

    @Override
    public void setServiceSpinner(String[] list) {
        final String[] final_list = list;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_row, list);
        adapter.setDropDownViewResource(R.layout.spinner_row);
        serviceSpinner.setAdapter(adapter);
        serviceSpinner.setSelection(preferences.getInt("service", 0));
        serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if(preferences.getInt("service", 0) != position) preferences.edit().remove("agent").apply();
                    preferences.edit().putInt("service", position).apply();
                    helper.getAgentsByService(final_list[position]);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void setAgentSpinner(String[] list) {
        final String[] final_list = list;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_row, list);
        adapter.setDropDownViewResource(R.layout.spinner_row);
        agentSpinner.setAdapter(adapter);
        if(list.length > 0) agentSpinner.setSelection(preferences.getInt("agent", 0));
        agentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    preferences.edit().putInt("agent", position).apply();
                    helper.getAvailableTerminals(final_list[position], myLatLng);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void setTerminalList(ResultList[] resultList) {
        adapter = new TerminalAdapter(getActivity(), R.layout.terminal_list_item, resultList);
        found_count.setText(String.valueOf(resultList.length));
        lats = new double[resultList.length];
        lngs = new double[resultList.length];
        anchor_title = new String[resultList.length];
        anchor_snippet = new String[resultList.length];
        ids = new String[resultList.length];
        providers = new String[resultList.length];
        for(int i=0; i<resultList.length; i++) {
            lats[i] = resultList[i].lat;
            lngs[i] = resultList[i].lng;
            anchor_title[i] = resultList[i].street + " " + resultList[i].build;
            anchor_snippet[i] = resultList[i].location;
            ids[i] = resultList[i].id;
            providers[i] = resultList[i].provider;
        }
        pd.dismiss();
    }

    @Override
    public void myAddrSpace(List<String> addr) {

    }

    @Override
    public void setAddress(String addr) {
        myAddress = addr;
        current_location_text.setText(myAddress);
    }
}
