package com.blog.config;

import com.blog.entity.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeLongIdsAsStringsToAvoidFrontendPrecisionLoss() throws Exception {
        Tag tag = new Tag();
        tag.setId(758902345678901301L);
        tag.setName("前端");

        String json = objectMapper.writeValueAsString(tag);

        assertTrue(json.contains("\"id\":\"758902345678901301\""));
    }
}
