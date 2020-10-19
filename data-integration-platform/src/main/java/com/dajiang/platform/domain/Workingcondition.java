package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_working_condition")
public class Workingcondition {
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

    @Column(name = "recordTime", nullable = false, columnDefinition = "timestamp COMMENT '解析的时间'")
    private Timestamp recordTIME;

    @Column(name = "loadd", nullable = false, columnDefinition = "float(32) COMMENT '本次运行载重'")
    private Float loAdd;

    @Column(name = "loadPercent", nullable = false, columnDefinition = "float(32) COMMENT '本次运行最大载重百分比'")
    private Float loadPErcent;

    @Column(name = "people_num", nullable = false, columnDefinition = "int(32) COMMENT '实时人数'")
    private Integer peoplenum;

    @Column(name = "startHeight", nullable = false, columnDefinition = "float(32) COMMENT '实时高度'")
    private Float startHEight;

    @Column(name = "height_per", nullable = false, columnDefinition = "float(32) COMMENT '高度百分比'")
    private Float heightper;

    @Column(name = "speedd",nullable = false,columnDefinition = "int(32) COMMENT '运行速度'")
    private Integer speeDd;

    @Column(name = "directionn",nullable = false,columnDefinition = "int(32) COMMENT '运行方向'")
    private Integer direcTionn;

    @Column(name = "ti_lt", nullable = false, columnDefinition = "float(32) COMMENT '实时倾斜度'")
    private Float tilt;

    @Column(name = "tilt_per", nullable = false, columnDefinition = "float(32) COMMENT '倾斜百分比'")
    private Float tiltper;

    @Column(name = "de_iver", nullable = false, columnDefinition = "int(32) COMMENT '驾驶员身份认证结果'")
    private Integer deiver;

    @Column(name = "lock_qm", nullable = false, columnDefinition = "int(32) COMMENT '门锁状态——前门'")
    private Integer lockqm;
    @Column(name = "lock_hm", nullable = false, columnDefinition = "int(32) COMMENT '门锁状态——后门'")
    private Integer lockhm;
    @Column(name = "lock_abn", nullable = false, columnDefinition = "int(32) COMMENT '门锁状态——门锁异常提示'")
    private Integer lockabn;

    @Column(name = "status_zl", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——重量'")
    private Integer statuszl;
    @Column(name = "status_gd", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——高度限位'")
    private Integer statusgd;
    @Column(name = "status_cs", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——超速'")
    private Integer statuscs;
    @Column(name = "status_rs", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——人数'")
    private Integer statusrs;
    @Column(name = "status_qx", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——倾斜'")
    private Integer statusqx;
    @Column(name = "status_qm", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——前门锁'")
    private Integer statusqm;
    @Column(name = "status_hm", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——后门锁'")
    private Integer statushm;
    @Column(name = "status_cc", nullable = false, columnDefinition = "int(32) COMMENT '系统状态——拆除'")
    private Integer statuscc;
}
