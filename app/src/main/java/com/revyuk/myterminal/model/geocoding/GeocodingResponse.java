package com.revyuk.myterminal.model.geocoding;

/**
 * Created by Notebook on 07.04.2015.
 */
public class GeocodingResponse {
    public String status;
    public GeocodingResult[] results;
    private GeocodingResult result;

    public String getStatus() {
        return status;
    }

    public GeocodingResult[] getResults() {
        return results;
    }

    public GeocodingResult getResult() {
        return result;
    }
 }
