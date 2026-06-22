package com.blog.dto;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleGroupRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectBlankName() {
        ArticleGroupRequest request = request(" ");

        Set<ConstraintViolation<ArticleGroupRequest>> violations = validator.validate(request);

        assertTrue(hasViolation(violations, NotBlank.class));
    }

    @Test
    void shouldRejectNameLongerThan50Characters() {
        ArticleGroupRequest request = request(repeat('a', 51));

        Set<ConstraintViolation<ArticleGroupRequest>> violations = validator.validate(request);

        assertTrue(hasViolation(violations, Size.class));
    }

    @Test
    void shouldAcceptNameWith50Characters() {
        ArticleGroupRequest request = request(repeat('a', 50));

        Set<ConstraintViolation<ArticleGroupRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    private static ArticleGroupRequest request(String name) {
        ArticleGroupRequest request = new ArticleGroupRequest();
        request.setName(name);
        return request;
    }

    private boolean hasViolation(
            Set<ConstraintViolation<ArticleGroupRequest>> violations,
            Class<?> annotationType) {
        return violations.stream()
                .anyMatch(violation -> annotationType.equals(
                        violation.getConstraintDescriptor().getAnnotation().annotationType()));
    }

    private String repeat(char value, int count) {
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, value);
        return new String(chars);
    }
}
