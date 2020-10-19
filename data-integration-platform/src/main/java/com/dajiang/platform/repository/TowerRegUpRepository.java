package com.dajiang.platform.repository;

import com.dajiang.platform.domain.TowerRegUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TowerRegUpRepository extends JpaRepository<TowerRegUp, Integer>{

}
