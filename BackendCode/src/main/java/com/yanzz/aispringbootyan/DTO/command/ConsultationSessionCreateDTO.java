package com.yanzz.aispringbootyan.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 接收前端请求：创建咨询会话的入参对象，用户点开咨询页时提交
// @Data 是 Lombok 注解，编译期自动生成 getter/setter/toString/equals/hashCode 等方法，无需手写
@Data
public class ConsultationSessionCreateDTO {
    // 会话标题，选填，不填后端会用首条消息或默认值生成
    // @Size 是 JSR-303 参数校验注解，max=200 表示最大长度 200 字符，message 是校验失败时的提示信息
    @Size(max = 200, message = "会话标题最多200个字符")
    private String sessionTitle;

    // 初始消息，必填，作为会话的第一条内容
    // @NotBlank 是 JSR-303 参数校验注解，校验字符串不能为 null、空串或纯空格
    // @Size 校验最大长度 2000 字符
    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息最多2000个字符")
    private String initialMessage;
}