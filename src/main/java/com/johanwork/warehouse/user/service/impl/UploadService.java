package com.johanwork.warehouse.user.service.impl;

import com.johanwork.warehouse.common.config.configProps.OciStorageProperties;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.user.dto.UploadResponse;
import com.johanwork.warehouse.user.service.IUploadService;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService implements IUploadService {

    private final OciStorageProperties props;
    private final ObjectStorageClient client;

    @Override
    public GenericResponse<UploadResponse> uploadPhoto(MultipartFile photo, String data) {
        validateFile(photo);
        String objectName = buildObjectName(photo.getOriginalFilename(), data);

        try (InputStream in = photo.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(props.namespace())
                    .bucketName(props.bucket())
                    .objectName(objectName)
                    .contentLength(photo.getSize())
                    .contentType(photo.getContentType())
                    .putObjectBody(in)
                    .build();

            client.putObject(request);
            log.info("Successfully to upload: {} ({} bytes)", objectName, photo.getSize());
            return new GenericResponse<>(new UploadResponse(
                    resolveUrl(objectName),
                    objectName,
                    photo.getOriginalFilename()
            ),AppConstant.Success.UPLOAD);


        } catch (IOException e) {
           throw new CustomException(HttpStatus.BAD_REQUEST,
                   AppConstant.Error.TITLE_BAD_REQUEST,
                   AppConstant.Error.TITLE_BAD_REQUEST);
        } catch (BmcException e) {
            log.info("This error: {}", e.getMessage());
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                    AppConstant.Error.TITLE_FAILED_UPLOAD,
                    AppConstant.Error.MESSAGE_FAILED_UPLOAD);
        }

    }

    private String buildObjectName(String originalFilename, String folder) {
        String name = UUID.randomUUID() + extensionOf(originalFilename);
        if (folder != null && !folder.isBlank()) {
            return folder.replaceAll("^/+|/+$", "") + "/" + name;
        }
        return name;
    }

    private String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    public String resolveUrl(String objectName) {
        return props.bucketPublic()
                ? buildPublicUrl(objectName)
                : createPreAuthenticatedUrl(objectName, 1600);
    }

    public String buildPublicUrl(String objectName) {
        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                props.region(), props.namespace(), props.bucket(), objectName);
    }

    public String createPreAuthenticatedUrl(String objectName, long expiryMinutes) {
        try {
            Date expiry = Date.from(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES));

            CreatePreauthenticatedRequestDetails details =
                    CreatePreauthenticatedRequestDetails.builder()
                            .name("par-" + objectName + "-" + System.currentTimeMillis())
                            .objectName(objectName)
                            .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                            .timeExpires(expiry)
                            .build();

            CreatePreauthenticatedRequestResponse response = client.createPreauthenticatedRequest(
                    CreatePreauthenticatedRequestRequest.builder()
                            .namespaceName(props.namespace())
                            .bucketName(props.bucket())
                            .createPreauthenticatedRequestDetails(details)
                            .build());

            return "https://objectstorage." + props.region() + ".oraclecloud.com"
                    + response.getPreauthenticatedRequest().getAccessUri();

        } catch (BmcException e) {
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                    AppConstant.Error.TITLE_FAILED_UPLOAD,
                    AppConstant.Error.MESSAGE_FAILED_UPLOAD);
        }
    }

    private void validateFile(MultipartFile file) {
        long maxSize = 2 * 1024 * 1024;
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/") || file.getSize() > maxSize) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    AppConstant.Error.TITLE_BAD_REQUEST,
                    AppConstant.Error.MESSAGE_BAD_REQUEST
            );
        }
    }

}
