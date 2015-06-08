package com.revyuk.myterminal.model;

public class ResultList {
    public String id;
    public String provider;

    public String service;
    public String agent;

    public String area;
    public String city;
    public String street;
    public String build;
    public String location;
    public String status;

    public double lat;
    public double lng;
    public double distance;

    @Override
    public String toString() {
        return "ResultList{" +
                "id='" + id + '\'' +
                ", provider='" + provider + '\'' +
                ", service='" + service + '\'' +
                ", agent='" + agent + '\'' +
                ", area='" + area + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", build='" + build + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", distance=" + distance +
                '}';
    }
}
