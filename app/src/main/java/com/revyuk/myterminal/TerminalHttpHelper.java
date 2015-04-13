package com.revyuk.myterminal;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.revyuk.myterminal.model.ResponseModel;
import com.revyuk.myterminal.model.Result;
import com.revyuk.myterminal.model.ResultList;
import com.revyuk.myterminal.model.geocoding.*;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Notebook on 09.04.2015.
 */

public class TerminalHttpHelper {
    public final String API_URL = "http://terminals.vivat-tv.com/data_term/";

    HelperCallback helperCallback;
    AsyncHttpClient client = new AsyncHttpClient();
    Gson gson;
    ResponseModel responseModel;
    RequestParams params;

    interface HelperCallback {
        void isError(int requestCode);
        void setServiceSpinner(String[] list);
        void setAgentSpinner(String[] list);
        void setTerminalList(ResultList[] resultList);
        void myAddrSpace(List<String> addr);
    }

    public TerminalHttpHelper (SearchFragment fragment) {
        helperCallback = fragment;
        gson = new Gson();
        client.setConnectTimeout(5);
    }

    public void getMyAddr(LatLng pos) {
        String url = "http://maps.googleapis.com/maps/api/geocode/json";
        params = new RequestParams();
        params.add("latlng", pos.latitude+","+pos.longitude);
        params.add("sensor", "true");
        params.add("region", "ua");
        params.add("language", "ru");
        client.get(url, params, new HttpResponseHandler(3));
    }

    public void getServices() {
        String url = API_URL+"getServices.php";
        client.get(url, new HttpResponseHandler(1));
    }

    public void getAgentsByService(String service) throws UnsupportedEncodingException {
        String url = API_URL+"getAgentByService.php";
        params = new RequestParams();
        params.add("service", service);
        client.get(url, params, new HttpResponseHandler(2));
    }

    public void getAvailableTerminals(String agent, LatLng myPosition) throws UnsupportedEncodingException {
        String url = API_URL+"getTerminals.php";
        params = new RequestParams();
        params.add("agent", agent);
        params.add("lat", String.valueOf(myPosition.latitude));
        params.add("lng", String.valueOf(myPosition.longitude));
        client.get(url, params, new HttpResponseHandler(4));
    }

    class HttpResponseHandler extends AsyncHttpResponseHandler {
        int requestCode;

        public HttpResponseHandler(int i) {
            requestCode = i;
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            switch (requestCode) {
                case 1:
                    responseModel = gson.fromJson(new String(bytes), ResponseModel.class);
                    String[] services = new String[responseModel.result.list.length];
                    for(int x=0; x<responseModel.result.list.length; x++) {
                        services[x] = responseModel.result.list[x].service;
                    }
                    helperCallback.setServiceSpinner(services);
                    break;
                case 2:
                    responseModel = gson.fromJson(new String(bytes), ResponseModel.class);
                    String[] agents = new String[responseModel.result.list.length];
                    for(int x=0; x<responseModel.result.list.length; x++) {
                        agents[x] = responseModel.result.list[x].agent;
                    }
                    helperCallback.setAgentSpinner(agents);
                    break;
                case 3:
                    String str = new String(bytes);
                    Log.d("XXX", str);
                    GeocodingResponse geocodingResponse = gson.fromJson(str, GeocodingResponse.class);
                    for(GeocodingResult result:geocodingResponse.results) {
                        Log.d("XXX", ">"+result.formatted_address);
                        for(AddressComponent component: result.address_components) {
                            Log.d("XXX", "---------------->"+component.types[0]+", "+component.long_name);
                        }
                    }
                    break;
                case 4:
                    String str2 = new String(bytes);
                    Log.d("XXX", str2);
                    responseModel = gson.fromJson(str2, ResponseModel.class);
                    helperCallback.setTerminalList(responseModel.result.list);
                    break;
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            if(helperCallback != null) helperCallback.isError(requestCode);
        }
    }

}
