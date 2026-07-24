package com.johanwork.warehouse.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final int MAX_LENGTH = 1000;

    @Around("execution(* com.johanwork.warehouse..service..*(..)) || " +
            "execution(* com.johanwork.warehouse..controller..*(..))" +
            "execution(* com.johanwork.warehouse..repository..*(..))")
    public Object logAndMeasure(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime    = System.currentTimeMillis();
        log.info("ℹ️ {} | args: {}", methodName, sanitizeArgs(joinPoint.getArgs()));
        try {
            Object result   = joinPoint.proceed();
            long perform    = System.currentTimeMillis() - startTime;
            log.info("ℹ️ {} | {}ms | result: {}", methodName, perform, truncate(result));
            return result;
        } catch (Exception ex) {
            long perform = System.currentTimeMillis() - startTime;
            log.error("⚠️ {} | {}ms | error: {} - {}",
                    methodName, perform,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private Object[] sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) return new Object[]{};
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String str = arg.toString().toLowerCase();
                    if (str.contains("password") || str.contains("token") ||
                            str.contains("secret")   || str.contains("key")) {
                        return "***REDACTED***";
                    }
                    return truncate(arg);
                })
                .toArray();
    }

    private String truncate(Object obj) {
        if (obj == null) return "null";
        String str = obj.toString();
        return str.length() > MAX_LENGTH
                ? str.substring(0, MAX_LENGTH) + "..."
                : str;
    }

}
