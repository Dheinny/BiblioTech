package com.fenix.bibliotech.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    //1. Handles Business Rules violations. (e.g.: ISBN duplicated)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, Locale locale) {
        return builtErrorResponseEntity(ex, HttpStatus.BAD_REQUEST, locale);
    }

    //2. Handles Resources not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                         Locale locale) {
        return builtErrorResponseEntity(ex, HttpStatus.NOT_FOUND, locale);
    }

    // 3. Handles Bean Validation errors to @Valid (required fields, etc)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    private ResponseEntity<ErrorResponse> builtErrorResponseEntity(BusinessException ex,
                                                                   HttpStatus httpStatus, Locale locale) {
        String errorMessage = messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale);

        ErrorResponse error = new ErrorResponse(
                httpStatus.value(),
                errorMessage,
                LocalDateTime.now()
        );

        return ResponseEntity.status(httpStatus).body(error);
    }
}

// Record for business error answer patterning
record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
