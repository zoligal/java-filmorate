package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Illegal argument error: {}", exception.getMessage());
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, exception.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException exception) {
        log.warn("User not found: {}", exception.getId());
        Map<String, Object> body = createErrorBody(
                HttpStatus.NOT_FOUND,
                String.format("User with id %d not found", exception.getId())
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FilmNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFilmNotFound(FilmNotFoundException exception) {
        log.warn("Film not found: {}", exception.getId());
        Map<String, Object> body = createErrorBody(
                HttpStatus.NOT_FOUND,
                String.format("Film with id %d not found", exception.getId())
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.warn("Validation errors in request: {}", exception.getBindingResult().getAllErrors());

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFriendshipNotFound(FriendshipNotFoundException exception) {
        log.warn("Friendship not found: {}", exception.getMessage());
        Map<String, Object> body = createErrorBody(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception exception) {
        log.error("Unexpected error", exception);
        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
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
