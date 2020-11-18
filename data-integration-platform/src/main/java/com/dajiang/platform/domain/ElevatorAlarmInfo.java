package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_elevator_alarminfo")
public class ElevatorAlarmInfo {
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

    @Column(name = "recordTime", nullable = false, columnDefinition = "timestamp COMMENT '时间(此为信息体中时间，信息体中包含时间则不记录采集时间)'")
    private Timestamp recordTIME;

    @Column(name = "real_lifting", nullable = false, columnDefinition = "float(32,1) COMMENT '实时起重量'")
    private Double realLifting;

    @Column(name = "weight_per", nullable = false, columnDefinition = "float(32,1) COMMENT '重量百分比'")
    private Double weightPer;

    @Column(name = "real_peoplenum", nullable = false, columnDefinition = "int(32) COMMENT '实时人数'")
    private Integer realPeoplenum;

    @Column(name = "real_height", nullable = false, columnDefinition = "float(32,1) COMMENT '实时高度'")
    private Double realHeight;

    @Column(name = "real_height_per", nullable = false, columnDefinition = "float(32,1) COMMENT '高度百分比'")
    private Double realHeightper;

    @Column(name = "real_speed", nullable = false, columnDefinition = "int(32) COMMENT '实时速度—速度'")
    private Integer realSpeed;

    @Column(name = "tilt_direction", nullable = false, columnDefinition = "int(32) COMMENT '实时速度—方向'")
    private Integer tiltDirection;

    @Column(name = "real_tilt", nullable = false, columnDefinition = "float(32,1) COMMENT '实时倾斜度'")
    private Double realTilt;

    @Column(name = "tilt_per", nullable = false, columnDefinition = "float(32,1) COMMENT '倾斜百分比'")
    private Double tiltPer;

    @Column(name = "driverStatus", nullable = false, columnDefinition = "int(32) COMMENT '驾驶员身份认证结果'")
    private Integer driverStatus;

    @Column(name = "lockstatus_qm", nullable = false, columnDefinition = "int(32) COMMENT '门锁状态-前门'")
    private Integer lockstatusQm;

    @Column(name = "lockstatus_hm", nullable = false, columnDefinition = "int(32) COMMENT '门锁状态-后门'")
    private Integer lockstatusHm;

    @Column(name = "lock_abnpro", nullable = false, columnDefinition = "int(32) COMMENT '门锁异常提示'")
    private Integer lockAbnpro;

    @Column(name = "alarm_cause", nullable = false, columnDefinition = "int(32) COMMENT '报警原因'")
    private Integer alarmCause;

    @Column(name = "alarm_level", nullable = false, columnDefinition = "int(32) COMMENT '级别'")
    private Integer alarmLevel;

}
