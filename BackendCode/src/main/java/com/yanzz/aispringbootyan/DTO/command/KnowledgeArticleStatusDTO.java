package com.yanzz.aispringbootyan.DTO.command;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 接收前端请求：修改文章发布状态
@Data
public class KnowledgeArticleStatusDTO {

    // 状态 0:草稿 1:已发布 2:已下线
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 2, message = "状态值不合法")
    private Integer status;
}
