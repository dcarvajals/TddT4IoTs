package com.ugr.microservices.dependencies.core.tddt4iots.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class GenericBasicResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code = 0;
    private String status = "OK";
    private String message = "Transaction successfully completed";
    private T data;
}
