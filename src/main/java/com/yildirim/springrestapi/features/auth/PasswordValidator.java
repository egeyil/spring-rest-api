package com.yildirim.springrestapi.features.auth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private final AuthenticationService authService;

    @Override
    public boolean isValid(String pwd, ConstraintValidatorContext context) {
        return authService.isValidPassword(pwd);
    }
}
