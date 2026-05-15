package com.flowboard.flowboard_api_gateway.aop;

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
    @Around("execution(* com.flowboard.flowboard_api_gateway.filter.*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String method =
                joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();

        try {

            log.info("API Called: {}", method);
            log.info("Request Data: {}",
                    Arrays.toString(joinPoint.getArgs()));

            Object response = joinPoint.proceed();

            log.info("Response: {}", response);

            return response;

        }
        catch (Exception ex) {

            log.error("Exception in API: {}", method);
            log.error("Error Message: {}", ex.getMessage());

            throw ex;
        }
        finally {

            long end = System.currentTimeMillis();

            log.info("API Completed: {}", method);
            log.info("Execution Time: {} ms",
                    (end - start));
        }
    }
}