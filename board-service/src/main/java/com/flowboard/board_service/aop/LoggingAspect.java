package com.flowboard.board_service.aop;

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

    @Around("execution(* com.flowboard.board_service.service.*.*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String methodName =
                joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();

        try {

            log.info("Method Started: {}", methodName);
            log.info("Arguments: {}",
                    Arrays.toString(joinPoint.getArgs()));

            Object result = joinPoint.proceed();

            log.info("Method Ended Successfully: {}",
                    methodName);

            log.info("Return Value: {}", result);

            return result;

        }
        catch (Exception ex) {

            log.error("Exception in Method: {}",
                    methodName);

            log.error("Exception Message: {}",
                    ex.getMessage());

            throw ex;
        }
        finally {

            long end = System.currentTimeMillis();

            log.info("Execution Time of {} : {} ms",
                    methodName,
                    (end - start));
        }
    }

    @Around("execution(* com.flowboard.board_service.controller.*.*(..))")
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