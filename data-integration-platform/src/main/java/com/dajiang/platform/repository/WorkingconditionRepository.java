package com.dajiang.platform.repository;

import com.dajiang.platform.domain.Workingcondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkingconditionRepository extends JpaRepository<Workingcondition,Integer>{

}