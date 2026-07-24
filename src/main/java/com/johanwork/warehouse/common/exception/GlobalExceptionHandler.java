package com.johanwork.warehouse.common.exception;

import com.johanwork.warehouse.common.constant.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        Map<String, String> violations = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                violations.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(createProblemDetail(HttpStatus.BAD_REQUEST,
                        AppConstant.Error.TITLE_BAD_REQUEST,
                        AppConstant.Error.MESSAGE_BAD_REQUEST,
                        request.getDescription(false),
                        violations
                ));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {

        Map<String, String> violations = new HashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();

            String messages = result.getResolvableErrors()
                    .stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            violations.put(paramName, messages);
        });
        return ResponseEntity.badRequest()
                .body(createProblemDetail(
                        HttpStatus.BAD_REQUEST,
                        AppConstant.Error.TITLE_BAD_REQUEST,
                        AppConstant.Error.MESSAGE_BAD_REQUEST,
                        request.getDescription(false),
                        violations
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleExceptions(Exception ex, WebRequest request){
        log.error(ex.getMessage());
        return ResponseEntity.internalServerError()
                .body(createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        AppConstant.Error.TITLE_INTERNAL_SERVER_ERROR,
                        AppConstant.Error.MESSAGE_INTERNAL_SERVER_ERROR,
                        request.getDescription(false),
                        null
                ));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException ex, WebRequest request){
        return ResponseEntity.status(ex.getStatus())
                .body(createProblemDetail(ex.getStatus(),
                       ex.getTitle(), ex.getMessage(),
                        request.getDescription(false),
                        ex.getViolations()
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(BadCredentialsException ex, WebRequest request){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createProblemDetail(HttpStatus.UNAUTHORIZED,
                        AppConstant.Error.TITLE_BAD_CREDENTIALS,
                        AppConstant.Error.MESSAGE_BAD_CREDENTIALS,
                        request.getDescription(false),
                        null
                ));
    }

    private ProblemDetail createProblemDetail(HttpStatus status,
                                              String title, String detail,
                                              String instance,
                                              Map<String, String> violations) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        if (null != violations) problemDetail.setProperty("violations", violations);
        return problemDetail;
    }
}
