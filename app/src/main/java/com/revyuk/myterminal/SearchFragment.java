package com.revyuk.myterminal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.revyuk.myterminal.adapters.TerminalAdapter;
import com.revyuk.myterminal.model.ResultList;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class SearchFragment extends Fragment implements TerminalHttpHelper.HelperCallback {
    TerminalHttpHelper helper;
    Spinner serviceSpinner, agentSpinner;
    ListView terminals;
    LatLng myLatLng;
    List<String> myAddrSpace;

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new TerminalHttpHelper(this);
        Bundle bundle = getArguments();
        if(bundle != null) {
            myLatLng = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        serviceSpinner = (Spinner) view.findViewById(R.id.service);
        agentSpinner = (Spinner) view.findViewById(R.id.agent);
        terminals = (ListView) view.findViewById(R.id.terminals);
        terminals.setOnItemClickListener(new MyOnItemClickListener());
        helper.getServices();
        return view;
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TerminalAdapter adapter = (TerminalAdapter) parent.getAdapter();
            Intent intent = new Intent(parent.getContext(), MapsActivity.class);
            intent.putExtra("lat", adapter.getItem(position).lat);
            intent.putExtra("lng", adapter.getItem(position).lng);
            startActivity(intent);
        }
    }

    @Override
    public void isError(int requestCode) {
        Toast.makeText(getActivity(), "Error in "+requestCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setServiceSpinner(String[] list) {
        final String[] final_list = list;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);
        serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agentSpinner.setAdapter(adapter);
        agentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
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
        TerminalAdapter adapter = new TerminalAdapter(getActivity(), R.layout.terminal_list_item, resultList);
        terminals.setAdapter(adapter);
    }

    @Override
    public void myAddrSpace(List<String> addr) {

    }
}
