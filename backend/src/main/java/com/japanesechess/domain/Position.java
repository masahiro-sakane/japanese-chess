package com.japanesechess.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position {
    int row;
    int column;

    @JsonCreator
    public Position(
        @JsonProperty("row") int row,
        @JsonProperty("column") int column
    ) {
        if (row < 0 || row >= 9 || column < 0 || column >= 9) {
            throw new IllegalArgumentException(
                String.format("Invalid position: row=%d, column=%d. Must be 0-8", row, column)
            );
        }
        this.row = row;
        this.column = column;
    }

    public boolean isValid() {
        return row >= 0 && row < 9 && column >= 0 && column < 9;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", row, column);
    }
}
