package com.japanesechess.query;

/**
 * Daily game count DTO for time-series chart
 *
 * @param date  Date string in yyyy-MM-dd format
 * @param count Number of games created on that date
 */
public record DailyGameCountDto(
        String date,
        long count
) {}
