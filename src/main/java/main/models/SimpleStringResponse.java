package main.models;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * alex on 03.01.16.
 */
public class SimpleStringResponse {
    private final int code;
    private final String response;

    public SimpleStringResponse(String response) {
        this(0, response);
    }

    public SimpleStringResponse(int errorCode) {
        this(errorCode, "\"Error errorCode " + errorCode + '\"');
    }

    public SimpleStringResponse(int code, String response) {
        this.code = code;
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
