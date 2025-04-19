package com.rayshan.locations.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LocationResponse {
    private List<LocationData> locations;
    private Map<String, Object> metadata;
}
