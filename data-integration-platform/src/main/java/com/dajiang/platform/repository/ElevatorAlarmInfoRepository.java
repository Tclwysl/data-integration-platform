package com.dajiang.platform.repository;

import com.dajiang.platform.domain.ElevatorAlarmInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorAlarmInfoRepository extends JpaRepository<ElevatorAlarmInfo, Integer> {
}
