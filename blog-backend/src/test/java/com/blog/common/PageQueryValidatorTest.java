package com.blog.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryValidatorTest {

    @Test
    void validateAllowsNormalPageRequests() {
        PageQueryValidator.validate(1, 10);
        PageQueryValidator.validate(2, 100);
    }

    @Test
    void validateRejectsInvalidPageRequests() {
        assertInvalid(0, 10);
        assertInvalid(1, 0);
        assertInvalid(1, 101);
    }

    private static void assertInvalid(long page, long size) {
        assertThatThrownBy(() -> PageQueryValidator.validate(page, size))
                .isInstanceOf(BusinessException.class)
                .hasMessage("分页参数不合法");
    }
}
