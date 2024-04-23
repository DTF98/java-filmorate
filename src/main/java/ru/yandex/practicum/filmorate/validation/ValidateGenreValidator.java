package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.model.Genre;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

public class ValidateGenreValidator implements ConstraintValidator<ValidateGenre, Set<Genre>> {
    @Override
    public boolean isValid(Set<Genre> value, ConstraintValidatorContext constraintValidatorContext) {
        if (value.isEmpty()) {
            return true;
        }
        for (Genre g : value) {
            if (g.getId() > 6 || g.getId() < 0) {
                return false;
            }
        }
        return true;
    }
}
