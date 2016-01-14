package main.models;

/**
 * alex on 14.01.16.
 */
public class Response {
    public static enum Codes {
        OK,
        NOT_FOUND,
        INVALID_QUERY,
        INCORRECT_QUERY,
        UNKNOWN_ERROR,
        USER_ALREDY_EXIST
    }

    private final int code;
    private final Object response;

    public Response(Object response) {
        this(Codes.OK, response);
    }

    public Response(Codes errorCode) {
        this(errorCode, errorCode);
    }

    public Response(Codes code, Object response) {
        this.code = code.ordinal();
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public Object getResponse() {
        return response;
    }
}
