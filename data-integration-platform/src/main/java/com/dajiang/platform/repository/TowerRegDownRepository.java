package com.dajiang.platform.repository;

import com.dajiang.platform.domain.TowerRegDown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TowerRegDownRepository extends JpaRepository<TowerRegDown, Integer>{
}
