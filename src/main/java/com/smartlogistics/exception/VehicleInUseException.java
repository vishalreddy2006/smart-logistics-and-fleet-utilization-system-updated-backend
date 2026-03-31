package com.smartlogistics.exception;

public class VehicleInUseException extends RuntimeException {

    public VehicleInUseException(String message) {
        super(message);
    }
}
