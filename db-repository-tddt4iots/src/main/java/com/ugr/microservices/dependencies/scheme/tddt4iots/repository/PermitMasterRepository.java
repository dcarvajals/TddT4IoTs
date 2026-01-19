package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.PermitMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermitMasterRepository extends JpaRepository<PermitMaster, Long> {
}