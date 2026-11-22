package com.yanzz.aispringbootyan.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.yanzz.aispringbootyan.DTO.response.FileUploadVO;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.service.FileStorageService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    // 与前端校验保持一致：最大 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    // 只允许常见图片格式
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${oss.endpoint:oss-cn-beijing.aliyuncs.com}")
    private String endpoint;

    @Value("${oss.bucket:java-yzz-web}")
    private String bucket;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    // 懒加载：没有配置密钥时应用也能正常启动，只有真正上传时才报错
    private volatile OSSClient ossClient;

    @Override
    public FileUploadVO upload(MultipartFile file, String businessType, String businessId, String businessField) {
        validateFile(file);

        String originalName = StrUtil.blankToDefault(file.getOriginalFilename(), "unknown");
        String extension = getExtension(originalName);
        String objectKey = "article/cover/" + UUID.randomUUID() + "." + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (StrUtil.isNotBlank(file.getContentType())) {
            metadata.setContentType(file.getContentType());
        }
        metadata.setContentDisposition("inline");

        try (InputStream inputStream = file.getInputStream()) {
            getClient().putObject(bucket, objectKey, inputStream, metadata);
        } catch (IOException | OSSException | ClientException e) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED.getCode(), "文件上传失败：" + e.getMessage());
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setOriginalName(originalName);
        vo.setFilePath("/" + objectKey);
        vo.setFileSize(file.getSize());
        vo.setFileType(extension);
        vo.setBusinessType(businessType);
        vo.setBusinessId(businessId);
        vo.setBusinessField(businessField);
        return vo;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_CONTENT_INVALID.getCode(), "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED.getCode(), "文件大小不能超过5MB");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED.getCode(), "只支持 jpg、jpeg、png、gif、webp 图片");
        }
    }

    private String getExtension(String fileName) {
        if (StrUtil.isBlank(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private OSSClient getClient() {
        if (StrUtil.isBlank(accessKeyId) || StrUtil.isBlank(accessKeySecret)) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED.getCode(),
                    "OSS 未配置，请设置环境变量 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET");
        }
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
                }
            }
        }
        return ossClient;
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
