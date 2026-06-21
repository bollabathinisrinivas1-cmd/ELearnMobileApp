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
import java.net.URLEncoder;
import java.util.Map;

/**
 * Static HTTP client for communicating with the eLibrary backend API.
 * Provides methods for unauthenticated POST requests and authenticated GET requests.
 */
public class ApiClient {
    private static final String BASE_URL = "http://44.202.210.90/api";

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
     * Performs an authenticated GET request with query string parameters that returns a JSON array.
     *
     * @param path   the API endpoint path (e.g., "/courses")
     * @param params a map of query parameter names to values (values will be URL-encoded)
     * @param token  the Bearer token for authorization
     * @return the parsed JSON array response
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static JSONArray getArrayWithParams(String path, Map<String, String> params, String token) throws ApiException {
        HttpURLConnection connection = null;
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + path);
            if (params != null && !params.isEmpty()) {
                urlBuilder.append("?");
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (!first) {
                        urlBuilder.append("&");
                    }
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    urlBuilder.append("=");
                    urlBuilder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    first = false;
                }
            }

            URL url = new URL(urlBuilder.toString());
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
     * Performs an authenticated POST request with a JSON body.
     *
     * @param path  the API endpoint path
     * @param body  the JSON request body
     * @param token the Bearer token for authorization
     * @return the parsed JSON response object
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static JSONObject postWithAuth(String path, JSONObject body, String token) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

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
     * Performs an authenticated PUT request with a JSON body.
     *
     * @param path  the API endpoint path
     * @param body  the JSON request body
     * @param token the Bearer token for authorization
     * @return the parsed JSON response object
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static JSONObject put(String path, JSONObject body, String token) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);

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
     * Performs an authenticated DELETE request.
     *
     * @param path  the API endpoint path (e.g., "/enrollments/{id}")
     * @param token the Bearer token for authorization
     * @throws ApiException if the server returns a non-2xx status code
     */
    public static void delete(String path, String token) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int statusCode = connection.getResponseCode();

            if (statusCode < 200 || statusCode >= 300) {
                String errorBody = readErrorStream(connection);
                throw new ApiException(statusCode, errorBody);
            }
        } catch (ApiException e) {
            throw e;
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
