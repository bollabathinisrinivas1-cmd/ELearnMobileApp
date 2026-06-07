package com.example.elearn.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Static HTTP client for communicating with the eLibrary backend API.
 * Provides methods for unauthenticated POST requests and authenticated GET requests.
 */
public class ApiClient {
    private static final String BASE_URL = "http://3.238.242.124/api";

    /**
     * Performs an unauthenticated POST request with a JSON body.
     *
     * @param path the API endpoint path (e.g., "/auth/token")
     * @param body the JSON request body
     * @return the parsed JSON response object
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static JSONObject post(String path, JSONObject body) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write request body
            byte[] bodyBytes = body.toString().getBytes("UTF-8");
            OutputStream os = connection.getOutputStream();
            os.write(bodyBytes);
            os.flush();
            os.close();

            int statusCode = connection.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = readStream(connection.getInputStream());
                return new JSONObject(responseBody);
            } else {
                String errorBody = readErrorStream(connection);
                throw new ApiException(statusCode, errorBody);
            }
        } catch (ApiException e) {
            throw e;
        } catch (JSONException e) {
            throw new ApiException(0, "Invalid JSON response: " + e.getMessage());
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Performs an authenticated GET request that returns a JSON array.
     *
     * @param path  the API endpoint path (e.g., "/courses")
     * @param token the Bearer token for authorization
     * @return the parsed JSON array response
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static JSONArray getArray(String path, String token) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int statusCode = connection.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = readStream(connection.getInputStream());
                return new JSONArray(responseBody);
            } else {
                String errorBody = readErrorStream(connection);
                throw new ApiException(statusCode, errorBody);
            }
        } catch (ApiException e) {
            throw e;
        } catch (JSONException e) {
            throw new ApiException(0, "Invalid JSON response: " + e.getMessage());
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Performs an authenticated GET request that returns a JSON object.
     *
     * @param path  the API endpoint path (e.g., "/users/1")
     * @param token the Bearer token for authorization
     * @return the parsed JSON object response
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static JSONObject getObject(String path, String token) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int statusCode = connection.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = readStream(connection.getInputStream());
                return new JSONObject(responseBody);
            } else {
                String errorBody = readErrorStream(connection);
                throw new ApiException(statusCode, errorBody);
            }
        } catch (ApiException e) {
            throw e;
        } catch (JSONException e) {
            throw new ApiException(0, "Invalid JSON response: " + e.getMessage());
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Reads all content from an InputStream into a String.
     */
    private static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Reads the error stream from a connection, falling back to an empty string
     * if the error stream is not available.
     */
    private static String readErrorStream(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                return readStream(errorStream);
            }
        } catch (IOException e) {
            // Fall through to return empty string
        }
        return "";
    }
}
