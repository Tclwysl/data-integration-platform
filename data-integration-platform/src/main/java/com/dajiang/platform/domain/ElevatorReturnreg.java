package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_elevator_returnreg")
public class ElevatorReturnreg {
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

    @Column(name = "recordTime", nullable = false, columnDefinition = "timestamp COMMENT '解析的时间  ，recordTime一般为数据采集时间'")
    private Timestamp recordTIME;

    @Column(name = "status", nullable = false, columnDefinition = "int(32) COMMENT '注册结果 非零即为许可'")
    private Integer staTus;

    @Column(name = "timeinterval", nullable = false, columnDefinition = "float(32) COMMENT '上传时间间隔'")
    private Float timeInterval;
}
