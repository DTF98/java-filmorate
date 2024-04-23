package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.MPA;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidateMPAValidator implements ConstraintValidator<ValidateMPA, MPA> {

    @Override
    public boolean isValid(MPA value, ConstraintValidatorContext constraintValidatorContext) {
        return !(value.getId() > 5 || value.getId() < 1);
    }
}
