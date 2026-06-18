package com.blog.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ArticleStatus {
    public static final String DRAFT = "draft";
    public static final String PENDING = "pending";
    public static final String PUBLISHED = "published";
    public static final String REJECTED = "rejected";

    private static final Set<String> ALL = new HashSet<>(
            Arrays.asList(DRAFT, PENDING, PUBLISHED, REJECTED));

    private ArticleStatus() {
    }

    public static boolean isValid(String status) {
        return status != null && ALL.contains(status);
    }

    public static boolean isPublic(String status) {
        return PUBLISHED.equals(status);
    }
}
