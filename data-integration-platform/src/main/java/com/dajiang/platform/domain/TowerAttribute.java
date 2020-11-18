package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name="iot_tower_attribute")
public class TowerAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "factory_num", nullable = false, columnDefinition = "int(32) COMMENT '厂家编号'")
    private Integer factoryNum;

    @Column(name = "pro_version", nullable = false, columnDefinition = "int(32) COMMENT '协议版本'")
    private Integer proVersion;

    @Column(name = "device_serial", nullable = false, columnDefinition = "int(32) COMMENT '设备编号'")
    private Integer deviceSerial;

    @Column(name = "tower_number", nullable = false, columnDefinition = "int(32) COMMENT '塔吊编号'")
    private Integer towerNumber;

    @Column(name = "tower_curve", nullable = false, columnDefinition = "int(32) COMMENT '力矩曲线'")
    private Integer towerCurve;

    @Column(name = "tower_x", nullable = false, columnDefinition = "float(32,1) COMMENT '坐标X'")
    private Double towerX;

    @Column(name = "tower_y", nullable = false, columnDefinition = "float(32,1) COMMENT '坐标y'")
    private Double towerY;

    @Column(name = "tower_boomlen", nullable = false, columnDefinition = "float(32,1) COMMENT '起重臂长'")
    private Double towerBoomlen;

    @Column(name = "tower_balancelen", nullable = false, columnDefinition = "float(32,1) COMMENT '平衡臂长'")
    private Double towerBalancelen;

    @Column(name = "tower_caphei", nullable = false, columnDefinition = "float(32,1) COMMENT '塔帽高'")
    private Double towerCaphei;

    @Column(name = "tower_boomhei", nullable = false, columnDefinition = "float(32,1) COMMENT '起重臂高'")
    private Double towerBoomhei;

    @Column(name = "tower_rope", nullable = false, columnDefinition = "int(32) COMMENT '绳索倍率'")
    private Integer towerRope;

    @Column(name = "tower_heiad1", nullable = false, columnDefinition = "int(32) COMMENT '高度标定AD1'")
    private Integer towerHeiad1;

    @Column(name = "tower_heix1", nullable = false, columnDefinition = "float(32,1) COMMENT '高度标定X1'")
    private Double towerHeix1;

    @Column(name = "tower_heiad2", nullable = false, columnDefinition = "int(32) COMMENT '高度标定AD2'")
    private Integer towerHeiad2;

    @Column(name = "tower_heix2", nullable = false, columnDefinition = "float(32,1) COMMENT '高度标定X2'")
    private Double towerHeix2;

    @Column(name = "tower_rangead1", nullable = false, columnDefinition = "int(32) COMMENT '幅度标定ad1'")
    private Integer towerRangead1;

    @Column(name = "tower_rangex1", nullable = false, columnDefinition = "float(32,1) COMMENT '幅度标定x1'")
    private Double towerRangex1;

    @Column(name = "tower_rangead2", nullable = false, columnDefinition = "int(32) COMMENT '幅度标定ad2'")
    private Integer towerRangead2;

    @Column(name = "tower_rangex2", nullable = false, columnDefinition = "float(32,1) COMMENT '幅度标定x2'")
    private Double towerRangex2;

    @Column(name = "tower_turnad1", nullable = false, columnDefinition = "int(32) COMMENT '回转标定ad1'")
    private Integer towerTurnad1;

    @Column(name = "tower_turnx1", nullable = false, columnDefinition = "float(32,1) COMMENT '回转标定X1'")
    private Double towerTurnx1;

    @Column(name = "tower_turnad2", nullable = false, columnDefinition = "int(32) COMMENT '回转标定ad2'")
    private Integer towerTurnad2;

    @Column(name = "tower_turnx2", nullable = false, columnDefinition = "float(32,1) COMMENT '回转标定x2'")
    private Double towerTurnx2;

    @Column(name = "tower_weiad1", nullable = false, columnDefinition = "int(32) COMMENT '重量标定ad1'")
    private Integer towerWeid1;

    @Column(name = "tower_weix1", nullable = false, columnDefinition = "float(32,1) COMMENT '重量标定x1'")
    private Double towerWeix1;

    @Column(name = "tower_weid2", nullable = false, columnDefinition = "int(32) COMMENT '重量标定ad2'")
    private Integer towerWeid2;

    @Column(name = "tower_weix2", nullable = false, columnDefinition = "float(32,1) COMMENT '重量标定x2'")
    private Double towerWeix2;

    @Column(name = "wind_calibration", nullable = false, columnDefinition = "int(32) COMMENT '风速标定校准值'")
    private Integer windCalibration;

    @Column(name = "tilt_calibration", nullable = false, columnDefinition = "int(32) COMMENT '倾斜标定校准值'")
    private Integer tiltCalibration;

    @Column(name = "height_start", nullable = false, columnDefinition = "float(32,1) COMMENT '高度起点限位'")
    private Double heightStart;

    @Column(name = "height_end", nullable = false, columnDefinition = "float(32,1) COMMENT '高度终点限位'")
    private Double heightEnd;

    @Column(name = "range_start", nullable = false, columnDefinition = "float(32,1) COMMENT '幅度起点限位'")
    private Double rangeStart;

    @Column(name = "range_end", nullable = false, columnDefinition = "float(32,1) COMMENT '幅度起点限位'")
    private Double rangeEnd;

    @Column(name = "trun_lf", nullable = false, columnDefinition = "float(32,1) COMMENT '回转左限位'")
    private Double trunLf;

    @Column(name = "trun_ri", nullable = false, columnDefinition = "float(32,1) COMMENT '回转右限位'")
    private Double trunRi;

    @Column(name = "warning_hor", nullable = false, columnDefinition = "float(32,1) COMMENT '水平报警距离'")
    private Double warningHor;

    @Column(name = "warning_ver", nullable = false, columnDefinition = "float(32,1) COMMENT '垂直报警距离'")
    private Double warningVer;

    @Column(name = "warning_hei", nullable = false, columnDefinition = "float(32,1) COMMENT '重量报警百分比'")
    private Double warningHei;

    @Column(name = "warning_win", nullable = false, columnDefinition = "float(32,1) COMMENT '风速报警值'")
    private Double warningWin;

    @Column(name = "warning_til", nullable = false, columnDefinition = "float(32,1) COMMENT '倾斜报警值'")
    private Double warningTil;

    @Column(name = "earlywarning_hor", nullable = false, columnDefinition = "float(32,1) COMMENT '水平预警距离'")
    private Double earlywarningHor;

    @Column(name = "earlywarning_ver", nullable = false, columnDefinition = "float(32,1) COMMENT '垂直预警距离'")
    private Double earlywarningVer;

    @Column(name = "earlywarning_hei", nullable = false, columnDefinition = "float(32,1) COMMENT '重量预警百分比'")
    private Double earlywarningHer;

    @Column(name = "earlywarning_win", nullable = false, columnDefinition = "float(32,1) COMMENT '风速预警值'")
    private Double earlywarningWin;

    @Column(name = "earlywarning_til", nullable = false, columnDefinition = "float(32,1) COMMENT '倾斜预警值'")
    private Double earlywarningTil;

    @Column(name = "collision_judge", nullable = false, columnDefinition = "int(32) COMMENT '碰撞制动允许'")
    private Integer collisionJudge;

    @Column(name = "id_judge", nullable = false, columnDefinition = "int(32) COMMENT '身份认证是否启动'")
    private Integer idJudge;

    @Column(name = "gprs_judge", nullable = false, columnDefinition = "int(32) COMMENT 'GPRS锁车'")
    private Integer gprsJudge;
}
