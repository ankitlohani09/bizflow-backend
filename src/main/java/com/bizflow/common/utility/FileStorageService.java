package com.bizflow.common.utility;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folder);

    String getFileUrl(String fileNameOrUrl);

    boolean deleteFile(String fileUrl);
}
