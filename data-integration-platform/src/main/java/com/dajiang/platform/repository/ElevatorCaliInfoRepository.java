package com.dajiang.platform.repository;

import com.dajiang.platform.domain.ElevatorCaliInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorCaliInfoRepository extends JpaRepository<ElevatorCaliInfo, Integer>{
}
