package ru.study.shop.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.study.shop.service.BusinessRuleException;
import ru.study.shop.service.InvalidDataException;
import ru.study.shop.service.NotFoundException;

import java.util.Map;

/** Превращает исключения в HTTP-ответы вида {"error": "текст"}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> businessRule(BusinessRuleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<Map<String, String>> invalid(InvalidDataException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Неверные данные");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
