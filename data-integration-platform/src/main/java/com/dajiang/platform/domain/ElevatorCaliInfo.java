package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_elevator_caliinfo")
public class ElevatorCaliInfo {
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

    @Column(name = "weiempty_ad", nullable = false, columnDefinition = "int(32) COMMENT '重量空载AD值'")
    private Integer weiemptyAd;

    @Column(name = "weiempty_actual", nullable = false, columnDefinition = "int(32) COMMENT '重量空载实际值'")
    private Integer weiemptyActual;

    @Column(name = "weiload_ad", nullable = false, columnDefinition = "int(32) COMMENT '重量负载AD值'")
    private Integer weiloadAd;

    @Column(name = "weiload_actual", nullable = false, columnDefinition = "int(32) COMMENT '重量负载实际值'")
    private Integer weiloadActual;

    @Column(name = "heibot_ad", nullable = false, columnDefinition = "int(32) COMMENT '高度底端AD值'")
    private Integer heibotAd;

    @Column(name = "heibot_actual", nullable = false, columnDefinition = "int(32) COMMENT '高度底端实际值'")
    private Integer heibotActual;

    @Column(name = "heitop_ad", nullable = false, columnDefinition = "int(32) COMMENT '高度顶端AD值'")
    private Integer heitopAd;

    @Column(name = "heitop_actual", nullable = false, columnDefinition = "int(32) COMMENT '高度顶端实际值'")
    private Integer heitopActual;
    /*@Column(name = "calibration_value", nullable = false, columnDefinition = "int(32) COMMENT '标定值'")
    private Integer calibrationValue;*/
}
