package com.example.myrajourney.data.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private ApiError error;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public ApiError getError() { return error; }

    public static class ApiError {
        // ✅ FIXED: Changed 'int' to 'String' to handle codes like "EMAIL_TAKEN"
        @SerializedName("code")
        private String code;

        @SerializedName("message")
        private String message;

        // ✅ ADDED: Missing getter method
        public String getCode() { return code; }

        public String getMessage() { return message; }
    }
}