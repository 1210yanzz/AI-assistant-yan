package com.yanzz.aispringbootyan.service;

import com.yanzz.aispringbootyan.DTO.response.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // 上传文件到 OSS，返回相对访问路径
    FileUploadVO upload(MultipartFile file, String businessType, String businessId, String businessField);
}
