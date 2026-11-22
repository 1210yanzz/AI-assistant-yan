package com.yanzz.aispringbootyan.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 接收前端请求：新增/编辑知识文章的入参对象，前端提交文章表单时使用
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class KnowledgeArticleCommandDTO {
    // 文章ID；新增时为 null，编辑时前端带上，用来定位要修改的文章
    private String id;

    // 文章标题，必填
    // @NotBlank 是 JSR-303 参数校验注解，不能为 null、空串或纯空格，message 是校验失败提示
    // @Size 校验最大长度 200 字符，max 参数指定上限
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题最多200个字符")
    private String title;

    // 文章正文，必填
    // @NotBlank 校验非空白
    @NotBlank(message = "文章内容不能为空")
    private String content;

    // 封面图片地址（OSS 相对路径），选填
    @Size(max = 500, message = "封面图片地址最多500个字符")
    private String coverImage;

    // 所属分类ID，必填（数据库该列非空）
    // Long 是 long 的包装类型，可以为 null，适合数据库主键 ID
    @NotNull(message = "文章分类不能为空")
    private Long categoryId;

    // 摘要，选填
    private String summary;

    // 标签，多个标签用逗号分隔，选填
    @Size(max = 500, message = "标签最多500个字符")
    private String tags;
}