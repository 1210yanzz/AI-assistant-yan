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

// 知识分类实体，对应 knowledge_category 表
@Data
@TableName("knowledge_category")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 父分类ID，0 表示顶级
    @TableField("parent_id")
    private Long parentId;

    @TableField("category_name")
    private String categoryName;

    @TableField("category_code")
    private String categoryCode;

    private String description;

    @TableField("sort_order")
    private Integer sortOrder;

    // 状态 0:禁用 1:启用
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
