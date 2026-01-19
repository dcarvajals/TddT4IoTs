package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelUseTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelUseToolRepository extends JpaRepository<ModelUseTool, Long> {

    @Query(value = "SELECT MUT FROM ModelUseTool MUT JOIN MUT.person JOIN MUT.model " +
            "WHERE MUT.person.id = :idPerson")
    List<ModelUseTool> getAllModelUseTool (
            @Param("idPerson") Long idPerson
    ) throws GenericException;

    @Query(value = "SELECT MUT FROM ModelUseTool MUT JOIN MUT.person JOIN MUT.model " +
            "WHERE MUT.person.id = :idPerson AND MUT.active = true")
    Optional<ModelUseTool> getModelUseToolByPerson (
            @Param("idPerson") Long idPerson
    ) throws GenericException;

}