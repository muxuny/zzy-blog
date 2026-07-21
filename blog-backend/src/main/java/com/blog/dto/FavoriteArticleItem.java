package com.blog.dto;

import com.blog.entity.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class FavoriteArticleItem {
    private Long articleId;

    private String title;

    private String summary;

    private String coverImage;

    private String authorName;

    private List<Tag> tags = Collections.emptyList();

    private Integer viewCount;

    private LocalDateTime favoritedAt;

    private boolean available;

    private String unavailableMessage;
}
