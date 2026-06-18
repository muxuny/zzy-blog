package com.blog.dto;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleReviewRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectBlankReason() {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setReason(" ");

        Set<ConstraintViolation<ArticleReviewRequest>> violations = validator.validate(request);

        assertTrue(hasViolation(violations, NotBlank.class));
    }

    @Test
    void shouldRejectReasonLongerThan500Characters() {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setReason(repeat('a', 501));

        Set<ConstraintViolation<ArticleReviewRequest>> violations = validator.validate(request);

        assertTrue(hasViolation(violations, Size.class));
    }

    @Test
    void shouldAcceptReasonWith500Characters() {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setReason(repeat('a', 500));

        Set<ConstraintViolation<ArticleReviewRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    private boolean hasViolation(
            Set<ConstraintViolation<ArticleReviewRequest>> violations,
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
