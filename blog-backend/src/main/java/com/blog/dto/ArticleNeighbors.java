package com.blog.dto;

import com.blog.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleNeighbors {
    private Article previous;
    private Article next;
}
