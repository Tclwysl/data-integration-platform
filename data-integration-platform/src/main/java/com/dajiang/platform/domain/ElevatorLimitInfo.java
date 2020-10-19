package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_elevator_limitinfo")
public class ElevatorLimitInfo {
    @Id
    @Column(name = "sourceId")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "cage_num", nullable = false, columnDefinition = "int(32) COMMENT '吊笼编号'")
    private Integer cageNum;

    @Column(name = "cage_loc", nullable = false, columnDefinition = "int(32) COMMENT '左右吊笼'")
    private Integer cageLoc;

    @Column(name = "serialNo", nullable = false, columnDefinition = "int(32) COMMENT '设备物理编号'")
    private Integer serialNO;

    //新增数据采集时间
    @Column(name = "recordTime", nullable = false, columnDefinition = "timestamp COMMENT '数据采集时间'")
    private Timestamp recordTIME;

    @Column(name = "maxlifting_warning", nullable = false, columnDefinition = "int(32) COMMENT '最大起重预警'")
    private Integer maxliftingWarning;

    @Column(name = "maxlifting_ctp", nullable = false, columnDefinition = "int(32) COMMENT '最大起重量报警'")
    private Integer maxliftingCtp;

    @Column(name = "maxlifting_hei", nullable = false, columnDefinition = "int(32) COMMENT '最大起升高度'")
    private Integer maxliftingHei;

    @Column(name = "maxspeed_warning", nullable = false, columnDefinition = "int(32) COMMENT '最大速度预警'")
    private Integer maxspeedWarning;

    @Column(name = "maxspeed_ctp", nullable = false, columnDefinition = "int(32) COMMENT '最大速度报警'")
    private Integer maxspeedCtp;

    @Column(name = "maxload_peo", nullable = false, columnDefinition = "int(32) COMMENT '最大承载人数'")
    private Integer maxloadPeo;

    @Column(name = "tilt_warning", nullable = false, columnDefinition = "int(32) COMMENT '倾斜预警值'")
    private Integer tiltWarning;

    @Column(name = "tilt_ctp", nullable = false, columnDefinition = "int(32) COMMENT '倾斜报警值'")
    private Integer tiltCtp;
}
