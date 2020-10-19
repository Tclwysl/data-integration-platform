package com.dajiang.platform.repository;

import com.dajiang.platform.domain.TowerLoop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TowerLoopRepository extends JpaRepository<TowerLoop, Integer>{
}
