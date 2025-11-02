
// src/main/java/se/ifmo/route_information_system/web/RestExceptionHandler.java
package se.ifmo.route_information_system.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@ControllerAdvice
@ResponseBody
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(Map.of(
                    "field", fe.getField(),
                    "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")));
        }
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Validation failed",
                "errors", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "path", v.getPropertyPath().toString(),
                        "message", v.getMessage()))
                .toList();
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Constraint violation",
                "errors", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadJson(HttpMessageNotReadableException ex) {
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Malformed JSON payload");
    }
}
