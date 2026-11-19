package com.yanzz.aispringbootyan.DTO.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// 返回给前端展示：知识文章详情/列表的返回对象，多带分类名、作者名、标签数组等展示友好字段
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class KnowledgeArticleVO {
    // 文章ID
    private String id;

    // 文章标题
    private String title;

    // 摘要
    private String summary;

    // 正文
    private String content;

    // 封面图片地址
    private String coverImage;

    // 所属分类ID
    // Long 是 long 的包装类型，适合外键 ID
    private Long categoryId;

    // 分类名称，关联查询得到，前端直接展示
    private String categoryName;

    // 标签原文，逗号分隔的字符串
    private String tags;

    // 标签拆分后的数组，前端遍历渲染标签更顺手
    // List<String> 是 Java 泛型集合，List 是列表接口，<> 里指定元素类型为 String（字符串）
    private List<String> tagArray;

    // 作者ID
    private Long authorId;

    // 作者名，关联查询得到
    private String authorName;

    // 阅读量
    // Integer 是 int 的包装类型
    private Integer readCount;

    // 状态 0:草稿 1:已发布 2:已下线
    private Integer status;

    // 发布时间
    // LocalDateTime 是 Java 8 日期时间类型，包含年月日时分秒
    private LocalDateTime publishedAt;

    // 创建时间
    // LocalDateTime 日期时间类型
    private LocalDateTime createdAt;

    // 更新时间
    // LocalDateTime 日期时间类型
    private LocalDateTime updatedAt;
}