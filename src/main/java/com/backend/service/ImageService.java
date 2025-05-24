package com.backend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class ImageService {
    @Value("${firebase.storage.bucket}")
    private String bucketName;

    private final Bucket storageBucket;

    @Autowired
    public ImageService(StorageClient storageClient) {
        this.storageBucket = storageClient.bucket();
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        Blob blob = storageBucket.create(fileName, file.getBytes(), file.getContentType());

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                bucketName, encodedFileName, blob.getGeneratedId());
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String imagePath = extractImagePathFromUrl(imageUrl);
            if (imagePath != null) {
                Blob blob = storageBucket.get(imagePath);
                if (blob != null) {
                    blob.delete();
                }
            }
        }
    }

    public static String extractImagePathFromUrl(String url) {
        try {
            String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);
            int startIndex = decodedUrl.indexOf("/o/") + 3;
            int endIndex = decodedUrl.indexOf("?alt=media");
            if (startIndex > 0 && endIndex > startIndex) {
                return decodedUrl.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}