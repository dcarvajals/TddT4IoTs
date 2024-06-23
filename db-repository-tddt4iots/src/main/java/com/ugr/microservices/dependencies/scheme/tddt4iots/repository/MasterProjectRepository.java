package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.MasterProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MasterProjectRepository extends JpaRepository<MasterProject, Long> {

    @Query(value = "SELECT MP FROM MasterProject MP JOIN MP.permitMasters PM " +
            "WHERE MP.statusIot <> 'S' AND MP.statusUml <> 'S' AND PM.joinactivePm <> 'I' ORDER BY MP.creationdateMp DESC")
    List<MasterProject> getAllProjectsActive () throws GenericException;

    @Query(value = "SELECT MP FROM MasterProject MP JOIN MP.permitMasters PM " +
            "WHERE MP.statusIot <> 'S' AND MP.statusUml <> 'S' AND PM.joinactivePm <> 'I' " +
            " AND MP.creationdateMp BETWEEN :startDate AND :endDate " +
            " ORDER BY MP.creationdateMp DESC")
    List<MasterProject> getProjectFromToDate (
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    ) throws GenericException;

}