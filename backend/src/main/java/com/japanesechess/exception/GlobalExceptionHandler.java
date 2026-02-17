package com.japanesechess.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(GameNotFoundException ex) {
        logger.warn("Game not found: {}", ex.getGameId());

        ErrorResponse error = new ErrorResponse(
            "GAME_NOT_FOUND",
            ex.getMessage(),
            Instant.now()
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }

    @ExceptionHandler(InvalidBoardStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBoardState(InvalidBoardStateException ex) {
        logger.error("Invalid board state", ex);

        ErrorResponse error = new ErrorResponse(
            "INVALID_BOARD_STATE",
            "Failed to reconstruct board state",
            Instant.now()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    @ExceptionHandler(ProjectionException.class)
    public ResponseEntity<ErrorResponse> handleProjectionError(ProjectionException ex) {
        logger.error(
            "Projection error - Game: {}, Event: {}",
            ex.getGameId(),
            ex.getEventType(),
            ex
        );

        ErrorResponse error = new ErrorResponse(
            "PROJECTION_ERROR",
            "Failed to process event",
            Instant.now()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");

        logger.warn("Validation error: {}", message);

        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            Instant.now()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            "ILLEGAL_ARGUMENT",
            ex.getMessage(),
            Instant.now()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        logger.error("Unexpected error", ex);

        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            Instant.now()
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
    ) {}
}
