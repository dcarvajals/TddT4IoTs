package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelPermissionRepository extends JpaRepository<ModelPermission, Long> {

    @Query(value = "SELECT MP FROM ModelPermission MP JOIN MP.person JOIN MP.model " +
            "WHERE MP.person.id = :idPerson ORDER BY MP.model.name")
    List<ModelPermission> getAllModelPermission (
            @Param("idPerson") Long idPerson
    ) throws GenericException;

    @Query(value = "SELECT MP FROM ModelPermission MP JOIN MP.model " +
            "WHERE MP.model.id = :idModel ORDER BY MP.id DESC")
    List<ModelPermission> getAllModelPermissionFromModel (
            @Param("idModel") Long idModel
    ) throws GenericException;

    @Query(value = "SELECT MP FROM ModelPermission MP JOIN MP.model JOIN MP.person " +
            "WHERE MP.model.id = :idModel")
    List<ModelPermission> getModelPermissionFromModel (
            @Param("idModel") Long idModel
    ) throws GenericException;

}