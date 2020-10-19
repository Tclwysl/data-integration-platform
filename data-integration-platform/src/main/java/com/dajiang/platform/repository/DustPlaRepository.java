package com.dajiang.platform.repository;

import com.dajiang.platform.domain.DustPla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DustPlaRepository extends JpaRepository<DustPla, Integer>{
}
