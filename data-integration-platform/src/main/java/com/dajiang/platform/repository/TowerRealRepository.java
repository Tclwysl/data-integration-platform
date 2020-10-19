package com.dajiang.platform.repository;

import com.dajiang.platform.domain.TowerReal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TowerRealRepository extends JpaRepository<TowerReal, Integer>{
}
