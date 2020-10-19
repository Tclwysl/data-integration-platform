package com.dajiang.platform.repository;

import com.dajiang.platform.domain.ElevatorReturnreg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorReturnregRepository extends JpaRepository<ElevatorReturnreg, Integer>{
}
