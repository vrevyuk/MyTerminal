package com.revyuk.myterminal;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

/**
 * Created by Vitaly on 23.05.2015.
 */
public class GoogleApiHelper {
    private GoogleApiHelperCallback callback;
    private AsyncHttpClient httpClient;

    public static final int WHO_AUTOTEXT = 1;
    public static final int WHO_DETAIL_PLACE = 2;
    public static final int SEND_FEEDBACK = 3;

    public interface GoogleApiHelperCallback {
        void onResult(boolean success, int who, String response);
    }

    private GoogleApiHelper(GoogleApiHelperCallback callback) {
        this.callback = (GoogleApiHelperCallback) callback;
        httpClient = new AsyncHttpClient();
        httpClient.setConnectTimeout(10000);
        httpClient.setResponseTimeout(20000);
    }

    public static GoogleApiHelper newInstance(GoogleApiHelperCallback callback) {
        GoogleApiHelper helper = new GoogleApiHelper(callback);
        return helper;
    }

    private class HttpHandler extends AsyncHttpResponseHandler {
        private int who;

        public HttpHandler(int who) {
            this.who = who;
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            callback.onResult(true, who, new String(bytes));
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            callback.onResult(false, who, throwable.getMessage());
        }
    }

    public void autoText(String text) {
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
        RequestParams params = new RequestParams();
        params.add("input", text);
        params.add("types", "address");
        params.add("language", "ru");
        params.add("key", "AIzaSyDCE7xs40dLOf8pQpCRcZKOZuYAErdMz18");
        params.setContentEncoding("UTF-8");
        httpClient.get(url, params, new HttpHandler(WHO_AUTOTEXT));
    }


    public void detailPlace(String placeId) {
        String url = "https://maps.googleapis.com/maps/api/place/details/json";
        RequestParams params = new RequestParams();
        params.add("placeid", placeId);
        params.add("language", "ru");
        params.add("key", "AIzaSyDCE7xs40dLOf8pQpCRcZKOZuYAErdMz18");
        params.setContentEncoding("UTF-8");
        httpClient.get(url, params, new HttpHandler(WHO_DETAIL_PLACE));
    }

    public void sendFeedback(int mode, String id, String msg) {
        String url = "http://terminals.vivat-tv.com/terminal_feedback.php";
        RequestParams params = new RequestParams();
        params.add("mode", String.valueOf(mode));
        params.add("id", id);
        params.add("message", msg);
        params.setContentEncoding("UTF-8");
        httpClient.get(url, params, new HttpHandler(SEND_FEEDBACK));
    }
}
