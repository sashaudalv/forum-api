package main.models;

import java.util.Map;

/**
 * alex on 03.01.16.
 */
public class Status {

    private final int code = 0;
    private final Map response;

    public Status(Map response) {
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public Map getResponse() {
        return response;
    }
}
