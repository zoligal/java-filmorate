package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Illegal argument: {}", exception.getMessage());
        return new ResponseEntity<>(createErrorBody(HttpStatus.BAD_REQUEST, exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCount(InvalidCountException exception) {
        log.warn("Invalid count: {}", exception.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("count", exception.getMessage());
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.BAD_REQUEST, "Некорректный параметр count", details),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        log.warn("Validation errors: {}", exception.getBindingResult().getAllErrors());
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(e ->
                errors.put(e.getField(), e.getDefaultMessage())
        );
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.BAD_REQUEST, "Validation failed", errors),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException exception) {
        log.warn("Bad request: {}", exception.getMessage());
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.BAD_REQUEST, exception.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException exception) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", exception.getMessage());
        body.put("timestamp", Instant.now());
        return ResponseEntity.badRequest().body(body);
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("404 Not Found (пользователь): {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFriendshipNotFound(FriendshipNotFoundException ex) {
        log.warn("404 Not Found (дружба): {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LikeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLikeNotFound(LikeNotFoundException ex) {
        log.warn("404 Not Found (лайк): {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FilmNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFilmNotFound(FilmNotFoundException ex) {
        log.warn("500 Internal Server Error: {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*@ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAnyException(Exception exception) {
        log.error("ПОЛНЫЙ СТЕК ОШИБКИ", exception);

        var body = Map.of(
                "error", "Internal Server Error",
                "message", "Произошла непредвиденная ошибка"
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        log.warn("User already exists / friendship already exists: {}", exception.getMessage());
        return new ResponseEntity<>(createErrorBody(HttpStatus.CONFLICT, exception.getMessage()), HttpStatus.CONFLICT);
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String message, Map<String, String> details) {
        Map<String, Object> body = createErrorBody(status, message);
        body.put("details", details);
        return body;
    }
}