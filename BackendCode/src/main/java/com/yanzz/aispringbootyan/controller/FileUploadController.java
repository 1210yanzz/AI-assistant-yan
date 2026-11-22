package com.yanzz.aispringbootyan.controller;

import com.yanzz.aispringbootyan.DTO.response.FileUploadVO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.service.FileStorageService;
import com.yanzz.aispringbootyan.utils.CurrentUserUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Resource
    private FileStorageService fileStorageService;

    // 上传文件（当前用于后台文章封面，管理员可用）
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "businessType", required = false) String businessType,
                                       @RequestParam(value = "businessId", required = false) String businessId,
                                       @RequestParam(value = "businessField", required = false) String businessField) {
        CurrentUserUtil.requireAdmin();
        return Result.ok(fileStorageService.upload(file, businessType, businessId, businessField));
    }
}
