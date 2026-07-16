package com.blog.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文章可见性常量。
 */
public final class ArticleVisibility {
    /** 全站公开可读。 */
    public static final String PUBLIC = "public";

    /** 仅作者本人和后台管理侧可见。 */
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
