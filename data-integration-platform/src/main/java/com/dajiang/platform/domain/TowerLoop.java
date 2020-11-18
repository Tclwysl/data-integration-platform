package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_tower_loop")
public class TowerLoop {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "factory_num", nullable = false, columnDefinition = "int(32) COMMENT '厂家编号'")
    private Integer factoryNum;

    @Column(name = "pro_version", nullable = false, columnDefinition = "int(32) COMMENT '协议版本'")
    private Integer proVersion;

    @Column(name = "device_serial", nullable = false, columnDefinition = "int(32) COMMENT '设备编号'")
    private Integer deviceSerial;

    @Column(name = "upload_time", nullable = false, columnDefinition = "timestamp COMMENT '上传时间'")
    private Timestamp uploadTime;

    @Column(name = "lifting_time", nullable = false, columnDefinition = "timestamp COMMENT '起吊时间'")
    private Timestamp liftingTime;

    @Column(name = "lifting_hei", nullable = false, columnDefinition = "float(32,1) COMMENT '起吊点高度'")
    private Double liftingHei;

    @Column(name = "lifting_range", nullable = false, columnDefinition = "float(32,1) COMMENT '起吊点幅度'")
    private Double liftingRange;

    @Column(name = "lifting_turn", nullable = false, columnDefinition = "float(32,1) COMMENT '起吊点回转'")
    private Double liftingTurn;

    @Column(name = "lifting_time2", nullable = false, columnDefinition = "timestamp COMMENT '起吊时间2'")
    private Timestamp liftingTime2;

    @Column(name = "lifting_hei2", nullable = false, columnDefinition = "float(32,1) COMMENT '起吊点高度2'")
    private Double liftingHei2;

    @Column(name = "lifting_range2", nullable = false, columnDefinition = "float(32,1) COMMENT '起吊点幅度2'")
    private Double liftingRange2;

    @Column(name = "lifting_turn2", nullable = false, columnDefinition = "float(32,1) COMMENT '起吊点回转2'")
    private Double liftingTurn2;

    @Column(name = "lifting_wei", nullable = false, columnDefinition = "float(32,1) COMMENT '最大吊重'")
    private Double liftingWei;

    @Column(name = "lifting_load", nullable = false, columnDefinition = "float(32,1) COMMENT '最大负荷'")
    private Double liftingLoad;

    @Column(name = "violation_reg", nullable = false, columnDefinition = "int(32) COMMENT '是否违章'")
    private Integer violationReg;


}
