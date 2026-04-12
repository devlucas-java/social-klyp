package com.github.devlucasjava.socialklyp.delivery.rest.advice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResponseErrorsDTO {

    private int code;
    private String message;
    private List<FieldErrorDTO> errors;
}
