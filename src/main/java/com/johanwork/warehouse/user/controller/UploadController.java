package com.johanwork.warehouse.user.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.user.dto.UploadResponse;
import com.johanwork.warehouse.user.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final IUploadService uploadService;

    @PostMapping(path = "/photo", version = "1.0",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse<UploadResponse>> uploadPhoto(@RequestPart(value = "photo") MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uploadService.uploadPhoto(photo,"user"));
    }

    @PostMapping(path = "/product-image", version = "1.0",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse<UploadResponse>> uploadProductImage(@RequestPart(value = "photo") MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uploadService.uploadPhoto(photo,"product"));
    }

    @PostMapping(path = "/category-image", version = "1.0",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse<UploadResponse>> uploadCategoryImage(@RequestPart(value = "photo") MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uploadService.uploadPhoto(photo,"category"));
    }

    @PostMapping(path = "/warehouse-image", version = "1.0",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse<UploadResponse>> uploadWarehouseImage(@RequestPart(value = "photo") MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uploadService.uploadPhoto(photo,"warehouse"));
    }

    @PostMapping(path = "/merchant-image", version = "1.0",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse<UploadResponse>> uploadMerchantImage(@RequestPart(value = "photo") MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(uploadService.uploadPhoto(photo,"merchant"));
    }

}
