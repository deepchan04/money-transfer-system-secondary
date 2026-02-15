package com.training.mts.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(LoggingAspect.class);

    // Intercept all service layer methods
    @Pointcut("execution(* com.training.mts.service..*(..))")
    public void serviceLayer() {}

    // Log BEFORE method execution
    @Before("serviceLayer()")
    public void logMethodCall(JoinPoint joinPoint) {

        logger.info("Entering method: {} with arguments: {}",
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logMethodSuccess(JoinPoint joinPoint, Object result) {

        logger.info("Method executed successfully: {} | Returned: {}",
                joinPoint.getSignature().toShortString(),
                result);
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Exception ex) {

        logger.error("Exception in method: {} | Message: {} | Arguments: {}",
                joinPoint.getSignature().toShortString(),
                ex.getMessage(),
                Arrays.toString(joinPoint.getArgs()),
                ex);
    }
}
