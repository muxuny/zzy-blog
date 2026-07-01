package com.blog.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文章审核状态常量。
 */
public final class ArticleStatus {
    /** 作者草稿，未进入审核流程。 */
    public static final String DRAFT = "draft";

    /** 作者已提交，等待管理员审核。 */
    public static final String PENDING = "pending";

    /** 审核通过，允许公开阅读接口按可见性返回。 */
    public static final String PUBLISHED = "published";

    /** 审核驳回，作者可修改后重新提交。 */
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
