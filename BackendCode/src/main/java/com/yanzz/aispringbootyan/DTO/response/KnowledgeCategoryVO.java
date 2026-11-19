package com.yanzz.aispringbootyan.DTO.response;

import lombok.Data;

// 返回给前端展示：知识分类的返回对象，字段基本和实体一一对应，主要用于分类树/下拉框展示
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class KnowledgeCategoryVO {
    // 分类ID
    // Long 是 long 的包装类型，适合数据库主键
    private Long id;

    // 父分类ID，前端靠它拼出分类树
    private Long parentId;

    // 分类名称
    private String categoryName;

    // 分类编码
    private String categoryCode;

    // 分类描述
    private String description;

    // 排序号
    // Integer 是 int 的包装类型
    private Integer sortOrder;

    // 状态 0:禁用 1:启用
    private Integer status;
}