package main.models;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * alex on 03.01.16.
 */
public class SimpleStringResponse {
    private final int code = 0;
    private final String response;

    public SimpleStringResponse(String response) {
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    @JsonRawValue
    public String getResponse() {
        return response;
    }
}
