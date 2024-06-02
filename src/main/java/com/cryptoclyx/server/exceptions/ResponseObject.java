package com.cryptoclyx.server.exceptions;

import lombok.Data;

@Data
public class ResponseObject {

    private int httpStatus;
    private String message;
    private Object details;

    public ResponseObject() {
    }

    public ResponseObject(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public ResponseObject(int httpStatus, String message, Object details) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.details = details;
    }
}
