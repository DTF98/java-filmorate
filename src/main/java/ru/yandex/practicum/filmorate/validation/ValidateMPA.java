package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ValidateMPAValidator.class})
public @interface ValidateMPA {
    String value() default "{value.negative}";
    String message() default "Некорректный id рейтинга";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}