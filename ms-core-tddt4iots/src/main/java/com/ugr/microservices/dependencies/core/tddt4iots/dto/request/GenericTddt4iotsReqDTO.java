package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class GenericTddt4iotsReqDTO<T> {
    private T classDTO;
    private String userToken;
}
