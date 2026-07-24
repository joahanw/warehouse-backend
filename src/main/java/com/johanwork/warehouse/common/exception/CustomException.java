package com.johanwork.warehouse.common.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomException extends RuntimeException{
    private HttpStatus status;
    private String title;
    private Map<String, String> violations;

    public CustomException(HttpStatus status, String title, String message) {
        super(message);
        this.status = status;
        this.title = title;
    }

    public CustomException(HttpStatus status, String title, String message, Map<String, String> violations) {
        super(message);
        this.status = status;
        this.title = title;
        this.violations = violations;
    }
}
