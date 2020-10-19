package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_elevator_info")
public class ElevatorInfo {
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
}
