package com.dajiang.platform.repository;

import com.dajiang.platform.domain.ElevatorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorInfoRepository extends JpaRepository<ElevatorInfo, Integer> {

}

