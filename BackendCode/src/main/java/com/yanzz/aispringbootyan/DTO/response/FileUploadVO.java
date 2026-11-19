package com.yanzz.aispringbootyan.DTO.response;

import lombok.Data;

// 返回给前端展示：文件上传接口的返回对象（VO = View Object 视图对象），只暴露前端关心的文件信息
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class FileUploadVO {
    // 文件记录ID
    // Long 是 long 的包装类型，适合数据库主键
    private Long id;

    // 原始文件名（带扩展名），前端展示给用户看
    private String originalName;

    // 文件存储路径，前端预览或下载时使用
    private String filePath;

    // 文件大小（字节），前端展示成"xx KB/MB"
    // Long 类型适合存大文件大小，避免 int 溢出
    private Long fileSize;

    // 文件类型/扩展名
    private String fileType;

    // 业务类型，说明文件用于哪个场景
    private String businessType;

    // 关联的业务ID
    private String businessId;

    // 关联的业务字段
    private String businessField;
}