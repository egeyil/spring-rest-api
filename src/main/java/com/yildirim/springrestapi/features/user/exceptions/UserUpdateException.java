package com.yildirim.springrestapi.features.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserUpdateException extends RuntimeException {
    public UserUpdateException(String message) {
        super(message);
    }
}
