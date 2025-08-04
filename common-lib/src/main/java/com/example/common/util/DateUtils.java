package com.example.common.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Common date utility functions.
 */
@Slf4j
public class DateUtils {
    
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public static String formatNow() {
        String formatted = LocalDateTime.now().format(ISO_FORMAT);
        log.debug("Formatted current time: {}", formatted);
        return formatted;
    }
    
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            log.warn("Attempted to format null LocalDateTime");
            return null;
        }
        String formatted = dateTime.format(ISO_FORMAT);
        log.debug("Formatted datetime: {} -> {}", dateTime, formatted);
        return formatted;
    }
    
    public static LocalDateTime parseIsoDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            log.warn("Attempted to parse null or empty datetime string");
            throw new IllegalArgumentException("DateTime string cannot be null or empty");
        }
        
        try {
            LocalDateTime parsed = LocalDateTime.parse(dateTimeString, ISO_FORMAT);
            log.debug("Parsed datetime: {} -> {}", dateTimeString, parsed);
            return parsed;
        } catch (DateTimeParseException e) {
            log.error("Failed to parse datetime string: {} - {}", dateTimeString, e.getMessage());
            throw e;
        }
    }
}