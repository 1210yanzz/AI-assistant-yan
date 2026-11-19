package com.yanzz.aispringbootyan.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 接收前端请求：AI 流式对话的入参对象，用户发起 AI 咨询时提交，后端开启 SSE 流式回复
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法
@Data
public class ConsultationStreamDTO {
    // 会话ID，必填，前端生成或后端创建后回传
    // @NotBlank 是 JSR-303 参数校验注解，不能为 null、空串或纯空格，message 是校验失败提示
    @NotBlank(message = "sessionId不能为空")
    private String sessionId;

    // 用户发的消息内容，必填
    // @NotBlank 校验非空白
    // @Size 校验最大长度 2000 字符，max 参数指定上限
    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息长度不能超过2000个字符")
    private String userMessage;
}