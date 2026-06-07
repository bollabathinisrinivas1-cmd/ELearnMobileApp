package com.example.elearn.network;

/**
 * Exception thrown when an API request returns a non-2xx HTTP status code.
 * Captures the HTTP status code and response body for error handling.
 */
public class ApiException extends Exception {
    private final int statusCode;
    private final String responseBody;

    public ApiException(int statusCode, String responseBody) {
        super("HTTP " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
