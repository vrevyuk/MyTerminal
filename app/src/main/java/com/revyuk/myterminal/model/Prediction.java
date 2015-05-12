package com.revyuk.myterminal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Andriy on 4/9/15.
 */
public class Prediction {
    private String description;
    private String id;
    @SerializedName("place_id")
    private String placeId;
    private String reference;

    public Prediction() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return description;
    }
}
