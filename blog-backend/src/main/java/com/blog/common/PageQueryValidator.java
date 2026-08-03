package com.blog.common;

/**
 * Shared guard for paginated endpoints that accept caller supplied page size.
 */
public final class PageQueryValidator {
    public static final long MAX_PAGE_SIZE = 100;

    private PageQueryValidator() {
    }

    public static void validate(long page, long size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException("分页参数不合法");
        }
    }
}
