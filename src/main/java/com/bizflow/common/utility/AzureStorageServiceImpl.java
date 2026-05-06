package com.bizflow.common.utility;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "storage.type", havingValue = "azure")
public class AzureStorageServiceImpl implements FileStorageService {

    @Value("${AWS_CONNECTION_STRING:}")
    private String connectionString;

    @Value("${AWS_CONTAINER_NAME:}")
    private String containerName;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        // As requested: Using 'bf_' prefix and NO subfolders in Azure
        String fileName = "bf_" + folder + "_" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            BlobClient blobClient = getBlobClient(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                blobClient.upload(inputStream, file.getSize(), true);
            }

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType())
                    .setContentDisposition("inline");
            blobClient.setHttpHeaders(headers);

            BlobSasPermission permissions = new BlobSasPermission().setReadPermission(true);
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                    OffsetDateTime.now().plusYears(1), permissions);

            return blobClient.getBlobUrl() + "?" + blobClient.generateSas(sasValues);

        } catch (Exception e) {
            log.error("Failed to upload file to Azure: {}", fileName, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String fileUrlOrName) {
        try {
            BlobClient blobClient = getBlobClient(fileUrlOrName);

            if (!blobClient.exists()) {
                return null;
            }

            BlobSasPermission permissions = new BlobSasPermission().setReadPermission(true);
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                    OffsetDateTime.now().plusYears(1), permissions);

            return blobClient.getBlobUrl() + "?" + blobClient.generateSas(sasValues);

        } catch (Exception e) {
            log.error("Failed to fetch URL from Azure: {}", fileUrlOrName, e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            BlobClient blobClient = getBlobClient(fileUrl);

            if (!blobClient.exists()) {
                return false;
            }

            blobClient.delete();
            return true;

        } catch (Exception e) {
            log.error("Failed to delete file from Azure: {}", fileUrl, e);
            return false;
        }
    }

    private BlobClient getBlobClient(String fileUrlOrName) {
        String blobName = fileUrlOrName.contains("/")
                ? fileUrlOrName.substring(fileUrlOrName.lastIndexOf("/") + 1).split("\\?")[0] : fileUrlOrName;

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString)
                .buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName.trim());

        if (!containerClient.exists()) {
            containerClient.create();
        }

        return containerClient.getBlobClient(blobName);
    }
}
