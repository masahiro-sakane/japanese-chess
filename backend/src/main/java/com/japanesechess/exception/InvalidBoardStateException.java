package com.japanesechess.exception;

public class InvalidBoardStateException extends RuntimeException {
    public InvalidBoardStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBoardStateException(String message) {
        super(message);
    }
}
