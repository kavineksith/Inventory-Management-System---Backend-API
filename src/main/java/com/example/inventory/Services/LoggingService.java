package com.example.inventory.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logError(String message, Exception e) {
        logger.error(message, e);
    }

    public void logDebug(String message) {
        logger.debug(message);
    }

    public void logWarn(String message) {
        logger.warn(message);
    }

    // Method-specific logging
    public void logServiceStart(String serviceName) {
        logger.info("Starting service: {}", serviceName);
    }

    public void logServiceEnd(String serviceName, long executionTime) {
        logger.info("Completed service: {} in {}ms", serviceName, executionTime);
    }

    public void logDatabaseOperation(String operation, String table, Object params) {
        logger.info("Database operation: {} on table: {} with params: {}", operation, table, params);
    }

    public void logBusinessLogic(String operation, Object data) {
        logger.info("Business logic: {} with data: {}", operation, data);
    }
}

