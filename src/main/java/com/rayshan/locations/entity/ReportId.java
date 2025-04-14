package com.rayshan.locations.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportId implements Serializable {
    private String idShort;
    private Integer timestamp;
}
