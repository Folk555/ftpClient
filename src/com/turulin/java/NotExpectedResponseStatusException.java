package com.turulin.java;

public class NotExpectedResponseStatusException extends Exception {
    public NotExpectedResponseStatusException (String message) {
        super(message);
    }
}
