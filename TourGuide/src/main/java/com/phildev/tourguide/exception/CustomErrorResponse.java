package com.phildev.tourguide.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomErrorResponse {

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class, RuntimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> errors(HttpServletRequest request, Exception exception){
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", new Date());
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("method", request.getMethod());
        errorBody.put("params",  request.getParameterMap());
        errorBody.put("path", request.getRequestURL());
        errorBody.put("message", exception.getMessage());
        return errorBody;
    }
}
