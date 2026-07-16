package com.blog.dto;

import com.blog.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章详情页上一篇/下一篇导航数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleNeighbors {
    private Article previous;

    private Article next;
}
