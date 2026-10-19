package ru.study.shop.service;

/** Объект не найден -> HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
