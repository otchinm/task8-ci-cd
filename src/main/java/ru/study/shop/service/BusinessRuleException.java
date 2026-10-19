package ru.study.shop.service;

/** Нарушено бизнес-правило (нет на складе, дубль названия) -> HTTP 409. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
