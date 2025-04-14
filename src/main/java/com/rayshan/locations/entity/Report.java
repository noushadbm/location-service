package com.rayshan.locations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReportId.class)
public class Report {

    @Id
    @Column(name = "id_short")
    private String idShort;

    @Id
    @Column(name = "timestamp")
    private Integer timestamp;

    @Column(name = "datePublished", columnDefinition = "INTEGER")
    private Integer datePublished;

    @Column(name = "payload")
    private String payload;

    @Column(name = "id")
    private String id;

    @Column(name = "statusCode")
    private Integer statusCode;
}
