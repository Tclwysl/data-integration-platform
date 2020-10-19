package com.dajiang.platform.repository;

import com.dajiang.platform.domain.ElevatorIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElevatorIdentityRepository extends JpaRepository<ElevatorIdentity, Integer> {
}
