package com.revyuk.myterminal.model;

import java.util.List;

/**
 * Created by Andriy on 4/9/15.
 */
public class PredictionsResponse {
    private String status;
    private List<Prediction> predictions;

    public PredictionsResponse() {

    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }
}
