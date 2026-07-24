package com.johanwork.warehouse.common.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
public class GenericResponse <T>{
    private T data;
    private String message;
    private Instant timestamp = Instant.now();

    public GenericResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

}
