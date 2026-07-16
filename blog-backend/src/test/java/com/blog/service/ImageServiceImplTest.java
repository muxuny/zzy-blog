package com.blog.service;

import com.blog.entity.Image;
import com.blog.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ImageServiceImplTest {

    @Test
    void uploadImage_shouldRecordCurrentUploader() throws Exception {
        TestImageService imageService = new TestImageService();
        Path uploadDir = Files.createTempDirectory("blog-image-upload-test");
        ReflectionTestUtils.setField(imageService, "uploadDir", uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                "image-content".getBytes());

        Image uploaded = imageService.uploadImage(file, "alice");

        assertThat(uploaded.getCreatedBy()).isEqualTo("alice");
        assertThat(imageService.savedImage.getCreatedBy()).isEqualTo("alice");
    }

    private static class TestImageService extends ImageServiceImpl {
        private Image savedImage;

        @Override
        public boolean save(Image image) {
            savedImage = image;
            if (image.getId() == null) {
                image.setId(1L);
            }
            return true;
        }

        @Override
        public Image getById(Serializable id) {
            return savedImage;
        }
    }
}
