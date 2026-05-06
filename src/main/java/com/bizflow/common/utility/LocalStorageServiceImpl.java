package com.bizflow.common.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements FileStorageService {

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            Path root = Paths.get(UPLOAD_DIR + folder);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(filename));

            return "/" + UPLOAD_DIR + folder + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store file locally", e);
            throw new RuntimeException("Failed to store file locally: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String fileNameOrUrl) {
        return fileNameOrUrl;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/"))
            return false;
        try {
            // Remove leading slash for local path
            String path = fileUrl.substring(1);
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            log.error("Failed to delete local file: {}", fileUrl, e);
            return false;
        }
    }
}
