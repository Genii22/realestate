package com.realestate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

// Раздаём загруженные фото с диска по URL /uploads/**.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + root.toString() + "/")
                .setCachePeriod(3600);
    }
}
