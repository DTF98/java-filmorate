package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ValidateDateValidator implements ConstraintValidator<ValidateDate, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(ValidateDate constraintAnnotation) {
        minDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && minDate != null && !value.isBefore(minDate);
    }
}
