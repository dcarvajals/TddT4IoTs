package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tddt4iotsServerResDTO {
    private int status;
    private String information;
    private List<Object> data; // Assuming data is a list of objects. Adjust if needed.
}
