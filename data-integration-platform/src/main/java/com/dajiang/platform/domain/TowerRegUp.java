package com.dajiang.platform.domain;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name="iot_tower_reg")
public class TowerRegUp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "factory_num", nullable = false, columnDefinition = "int(32) COMMENT '厂家编号'")
    private Integer factoryNum;

    @Column(name = "pro_version", nullable = false, columnDefinition = "int(32) COMMENT '协议版本'")
    private Integer proVersion;

    @Column(name = "device_serial", nullable = false, columnDefinition = "int(32) COMMENT '设备编号'")
    private Integer deviceSerial;
}
