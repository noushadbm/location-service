package com.rayshan.locations.model;

import lombok.Data;

@Data
public class LocationData {
    private String dateTime;
    private double latitude;
    private double longitude;
    private int confidence;
    private int status;
    private String url;
}
