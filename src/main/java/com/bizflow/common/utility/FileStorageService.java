package com.bizflow.common.utility;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folder);

    String getFileUrl(String fileNameOrUrl);

    boolean deleteFile(String fileUrl);

    String uploadBase64(String base64Data, String folder, String filename);
}
