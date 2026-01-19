package com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelPermissionDTO;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelPermission;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ModelPermissionMapper {
    ModelPermission modelPermissionTo (ModelPermissionDTO modelPermissionDTO);

    ModelPermissionDTO modelPermissionDTOTo (ModelPermission modelPermission);

    List<ModelPermissionDTO> modelPermissionDTOListTo (List<ModelPermission> modelPermissionList);
}
