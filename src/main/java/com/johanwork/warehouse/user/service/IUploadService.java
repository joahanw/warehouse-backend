package com.johanwork.warehouse.user.service;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.user.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IUploadService {
    GenericResponse<UploadResponse> uploadPhoto(MultipartFile photo, String data);
}
