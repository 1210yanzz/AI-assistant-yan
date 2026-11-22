package com.yanzz.aispringbootyan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 心理健康文章实体，对应 knowledge_article 表；主键是前端传入或后端生成的 UUID
@Data
@TableName("knowledge_article")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticle {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("category_id")
    private Long categoryId;

    private String title;

    private String summary;

    private String content;

    // 封面图在 OSS 中的相对路径，如 /article/cover/xxx.png
    @TableField("cover_image")
    private String coverImage;

    // 逗号分隔的标签
    private String tags;

    @TableField("author_id")
    private Long authorId;

    @TableField("read_count")
    private Integer readCount;

    // 状态 0:草稿 1:已发布 2:已下线
    private Integer status;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
