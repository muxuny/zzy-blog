package com.blog.service;

import com.blog.dto.ReadingOverview;

public interface ReadingSpaceService {

    ReadingOverview getOverview(String username);
}
