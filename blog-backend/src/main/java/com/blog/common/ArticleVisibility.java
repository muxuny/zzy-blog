package com.blog.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ArticleVisibility {
    public static final String PUBLIC = "public";
    public static final String PRIVATE = "private";

    private static final Set<String> ALL = new HashSet<>(Arrays.asList(PUBLIC, PRIVATE));

    private ArticleVisibility() {
    }

    public static boolean isValid(String visibility) {
        return visibility != null && ALL.contains(visibility);
    }

    public static boolean isPublic(String visibility) {
        return visibility == null || PUBLIC.equals(visibility);
    }
}
