package com.example.demo.util;

import org.springframework.stereotype.Component;

@Component
public class ImageUtil {
    private static final String BASE_URL = "http://localhost:8080/images/";
    
    public String getFullImageUrl(String image) {
        if (image == null || image.isEmpty()) {
            return "";
        }
        // If already full URL, return as is
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        // Otherwise, add base URL
        return BASE_URL + image;
    }
}