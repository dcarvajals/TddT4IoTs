package com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.TrainingHistoryDTO;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
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
public interface TrainingHistoryMapper {
    TrainingHistory trainingHistoryTo (TrainingHistoryDTO trainingHistoryDTO);

    List<TrainingHistoryDTO> trainingHistoryDTOListTo (List<TrainingHistory> trainingHistory);
}
