package ru.study.shop.service;

/** Неверные данные -> HTTP 400. */
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
