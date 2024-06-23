package com.ugr.microservices.dependencies.core.tddt4iots.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

public class Tddt4iotsUtil {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static <T> T fetchDataFromUrl(String urlString, Class<T> valueType) {
        T data = null;
        try {
            URL url = new URL(urlString);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                data = JSON_MAPPER.readValue(reader, valueType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // Método para peticiones GET que mapea la respuesta a un objeto de tipo T
    public static <T> T getRequest(String url, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return sendRequest(request, responseType);
    }

    // Método para peticiones POST que mapea la respuesta a un objeto de tipo T
    public static <T, R> T postRequest(String url, R requestBody, Class<T> responseType) throws IOException, InterruptedException {
        String json = JSON_MAPPER.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, responseType);
    }

    // Método para peticiones PUT que mapea la respuesta a un objeto de tipo T
    public static <T, R> T putRequest(String url, R requestBody, Class<T> responseType) throws IOException, InterruptedException {
        String json = JSON_MAPPER.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, responseType);
    }

    // Método para peticiones DELETE que mapea la respuesta a un objeto de tipo T
    public static <T> T deleteRequest(String url, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();
        return sendRequest(request, responseType);
    }

    // Método privado para enviar la solicitud y manejar la respuesta
    private static <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return JSON_MAPPER.readValue(response.body(), responseType);
        } else {
            throw new IOException("Error in request: " + response.statusCode() + " " + response.body());
        }
    }

    public static <T, R> List<T> postMultipleRequests(String url, Class<T> responseType, Class<R> requestBodyClass, String... names) {
        List<T> results = new ArrayList<>();
        for (String name : names) {
            try {
                Constructor<R> constructor = requestBodyClass.getConstructor(String.class);
                R requestBody = constructor.newInstance(name);
                T response = postRequest(url, requestBody, responseType);
                results.add(response);
            } catch (IOException | InterruptedException | ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
}
