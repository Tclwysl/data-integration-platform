package com.dajiang.platform.repository;

import com.dajiang.platform.domain.ElevatorLimitInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorLimitInfoRepository extends JpaRepository<ElevatorLimitInfo,Integer>{
}
