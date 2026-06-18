package com.blog.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleStatusTest {

    @Test
    void shouldRecognizePublishedAsPublicOnlyStatus() {
        assertTrue(ArticleStatus.isPublic("published"));
        assertFalse(ArticleStatus.isPublic("draft"));
        assertFalse(ArticleStatus.isPublic("pending"));
        assertFalse(ArticleStatus.isPublic("rejected"));
    }

    @Test
    void shouldRecognizeValidStatuses() {
        assertTrue(ArticleStatus.isValid("draft"));
        assertTrue(ArticleStatus.isValid("pending"));
        assertTrue(ArticleStatus.isValid("published"));
        assertTrue(ArticleStatus.isValid("rejected"));
        assertFalse(ArticleStatus.isValid("archived"));
        assertFalse(ArticleStatus.isValid(null));
    }
}
