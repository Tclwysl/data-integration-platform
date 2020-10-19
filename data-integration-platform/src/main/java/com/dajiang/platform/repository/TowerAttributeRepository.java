package com.dajiang.platform.repository;

import com.dajiang.platform.domain.TowerAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TowerAttributeRepository extends JpaRepository<TowerAttribute, Integer>{
}
