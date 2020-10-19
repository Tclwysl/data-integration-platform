package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name="iot_elevator_identity")
public class ElevatorIdentity {
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

    @Column(name = "dist_status", nullable = false, columnDefinition = "int(32) COMMENT '识别结果状态值'")
    private Integer distStatus;

    @Column(name = "driverName", nullable = false, columnDefinition = "varchar(72) COMMENT '用户名-驾驶员姓名'")
    private String driverName;

    @Column(name = "userId", nullable = false, columnDefinition = "varchar(36) COMMENT '用户ID'")
    private String userId;

    @Column(name = "dist_num", nullable = false, columnDefinition = "int(32) COMMENT '识别分数'")
    private Integer distNum;

    @Column(name = "driverId", nullable = false, columnDefinition = "varchar(72) COMMENT '身份号码_驾驶员身份证号'")
    private String driverId;
}
