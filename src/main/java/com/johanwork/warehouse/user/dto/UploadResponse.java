package com.johanwork.warehouse.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {

    private String url;
    private String path;
    private String filename;

}
