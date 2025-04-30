package com.example.beblog.common;

import org.springframework.http.HttpStatus;

public class ResponseEntityWrapper<T> {
    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> success(T data) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.success(data), HttpStatus.OK);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.success(data, message), HttpStatus.OK);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> created(T data) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.success(data), HttpStatus.CREATED);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> error(String message) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.error(message), HttpStatus.BAD_REQUEST);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> error(String message, String errorCode) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.error(message, errorCode),
                HttpStatus.BAD_REQUEST);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> notFound(String message) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.error(message), HttpStatus.NOT_FOUND);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.error(message), HttpStatus.UNAUTHORIZED);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.error(message), HttpStatus.FORBIDDEN);
    }

    public static <T> org.springframework.http.ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return new org.springframework.http.ResponseEntity<>(ApiResponse.error(message),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}