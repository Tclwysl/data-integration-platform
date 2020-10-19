package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_tower_real")
public class TowerReal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "factory_num", nullable = false, columnDefinition = "int(32) COMMENT '厂家编号'")
    private Integer factoryNum;

    @Column(name = "pro_version", nullable = false, columnDefinition = "int(32) COMMENT '协议版本'")
    private Integer proVersion;

    @Column(name = "device_serial", nullable = false, columnDefinition = "int(32) COMMENT '设备编号'")
    private Integer deviceSerial;

    @Column(name = "ti_me", nullable = false, columnDefinition = "timestamp COMMENT '上传时间'")
    private Timestamp time;

    @Column(name = "hei_ght", nullable = false, columnDefinition = "float(32) COMMENT '高度'")
    private Float Height;

    @Column(name = "ran_ge", nullable = false, columnDefinition = "float(32) COMMENT '幅度'")
    private Float Range;

    @Column(name = "tu_rn", nullable = false, columnDefinition = "float(32) COMMENT '回转'")
    private Float Turn;

    @Column(name = "lo_ad", nullable = false, columnDefinition = "float(32) COMMENT '载重'")
    private Float Load;

    @Column(name = "load_judge", nullable = false, columnDefinition = "float(32) COMMENT '当前允许载重'")
    private Float loadJudge;

    @Column(name = "load_per", nullable = false, columnDefinition = "float(32) COMMENT '载重百分比'")
    private Float loadPer;

    @Column(name = "wi_nd", nullable = false, columnDefinition = "float(32) COMMENT '风速'")
    private Float Wind;

    @Column(name = "ti_lt", nullable = false, columnDefinition = "float(32) COMMENT '倾斜'")
    private Float Tilt;

    @Column(name = "height_spa", nullable = false, columnDefinition = "int(32) COMMENT '高度限位值'")
    private Integer heightSpa;

    @Column(name = "range_spa_yj", nullable = false, columnDefinition = "int(32) COMMENT '幅度回转限位-远近端'")
    private Integer rangeSpayj;

    @Column(name = "range_spa_zy", nullable = false, columnDefinition = "int(32) COMMENT '幅度回转限位-左右'")
    private Integer rangeSpazy;

    @Column(name = "load_spa", nullable = false, columnDefinition = "int(32) COMMENT '载重限制值'")
    private Integer loadSpa;

    @Column(name = "tiltwind_spa_wind", nullable = false, columnDefinition = "int(32) COMMENT '倾斜风速限制-风速'")
    private Integer tiltwindSpawind;

    @Column(name = "tiltwin_spa_tilt", nullable = false, columnDefinition = "int(32) COMMENT '倾斜风速限制-倾斜'")
    private Integer tiltwindSpatilt;

    @Column(name = "interference_qh_q", nullable = false, columnDefinition = "int(32) COMMENT '干涉前后限位-前'")
    private Integer interferenceQhq;

    @Column(name = "interference_qh_h", nullable = false, columnDefinition = "int(32) COMMENT '干涉前后限位-后'")
    private Integer interferenceQhh;

    @Column(name = "interference_zy_z", nullable = false, columnDefinition = "int(32) COMMENT '干涉左右限位-左'")
    private Integer interferenceZyz;

    @Column(name = "interference_zy_y", nullable = false, columnDefinition = "int(32) COMMENT '干涉左右限位-右'")
    private Integer interferenceZyy;

    @Column(name = "interference_sx_s", nullable = false, columnDefinition = "int(32) COMMENT '干涉上下限位-上'")
    private Integer interferenceSxs;

    @Column(name = "interference_sx_x", nullable = false, columnDefinition = "int(32) COMMENT '干涉上下限位-下'")
    private Integer interferenceSxx;

    @Column(name = "collision_sx_s", nullable = false, columnDefinition = "int(32) COMMENT '碰撞上下限位-上'")
    private Integer collisionSxs;

    @Column(name = "collision_sx_x", nullable = false, columnDefinition = "int(32) COMMENT '碰撞上下限位-下'")
    private Integer collisionSxx;

    @Column(name = "collision_qh_q", nullable = false, columnDefinition = "int(32) COMMENT '碰撞前后限位-前'")
    private Integer collisionQhq;

    @Column(name = "collision_qh_h", nullable = false, columnDefinition = "int(32) COMMENT '碰撞前后限位-后'")
    private Integer collisionQhh;

    @Column(name = "collision_zy_z", nullable = false, columnDefinition = "int(32) COMMENT '碰撞左右限位-左'")
    private Integer collisionZyz;

    @Column(name = "collision_zy_y", nullable = false, columnDefinition = "int(32) COMMENT '碰撞左右限位-右'")
    private Integer collisionZyy;

    @Column(name = "relay_state_0", nullable = false, columnDefinition = "int(32) COMMENT '上行制动状态'")
    private Integer relayState0;

    @Column(name = "relay_state_1", nullable = false, columnDefinition = "int(32) COMMENT '下行制动状态'")
    private Integer relayState1;

    @Column(name = "relay_state_2", nullable = false, columnDefinition = "int(32) COMMENT '前行制动状态'")
    private Integer relayState2;

    @Column(name = "relay_state_3", nullable = false, columnDefinition = "int(32) COMMENT '后行制动状态'")
    private Integer relayState3;

    @Column(name = "relay_state_4", nullable = false, columnDefinition = "int(32) COMMENT '左行制动状态'")
    private Integer relayState4;

    @Column(name = "relay_state_5", nullable = false, columnDefinition = "int(32) COMMENT '右行制动状态'")
    private Integer relayState5;

    @Column(name = "work_state", nullable = false, columnDefinition = "int(32) COMMENT '工作状态'")
    private Integer workState;

    @Column(name = "sensor_state_0", nullable = false, columnDefinition = "int(32) COMMENT '高度传感器连接状态'")
    private Integer sensorState0;

    @Column(name = "sensor_state_1", nullable = false, columnDefinition = "int(32) COMMENT '幅度传感器连接状态'")
    private Integer sensorState1;

    @Column(name = "sensor_state_2", nullable = false, columnDefinition = "int(32) COMMENT '回转传感器连接状态'")
    private Integer sensorState2;

    @Column(name = "sensor_state_3", nullable = false, columnDefinition = "int(32) COMMENT '重量传感器连接状态'")
    private Integer sensorState3;

    @Column(name = "sensor_state_4", nullable = false, columnDefinition = "int(32) COMMENT '风速传感器连接状态'")
    private Integer sensorState4;

    @Column(name = "sensor_state_5", nullable = false, columnDefinition = "int(32) COMMENT '倾斜传感器连接状态'")
    private Integer sensorState5;



}
