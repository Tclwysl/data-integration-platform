package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_dust_pla")
public class DustPla {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "time", nullable = false, columnDefinition = "timestamp COMMENT '获取时间'")
    private Timestamp tiMe;

    @Column(name = "temperature", nullable = false, columnDefinition = "float(32) COMMENT '温度'")
    private Float temperaTure;

    @Column(name = "humidity", nullable = false, columnDefinition = "float(32) COMMENT '湿度'")
    private Float humiDity;

    @Column(name = "wind_speed", nullable = false, columnDefinition = "float(32) COMMENT '风速'")
    private Float windSpeed;

    @Column(name = "wind_direction", nullable = false, columnDefinition = "float(32) COMMENT '风向'")
    private Float windDirection;

    @Column(name = "atmosphere", nullable = false, columnDefinition = "float(32) COMMENT '大气压力'")
    private Float atmoSphere;

    @Column(name = "pm2_5", nullable = false, columnDefinition = "float(32) COMMENT 'PM2.5'")
    private Float pM25;

    @Column(name = "pm_10", nullable = false, columnDefinition = "float(32) COMMENT 'PM10'")
    private Float pM10;

    @Column(name = "noise", nullable = false, columnDefinition = "float(32) COMMENT '噪声'")
    private Float noIse;



}
