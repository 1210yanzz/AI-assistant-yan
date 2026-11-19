package com.yanzz.aispringbootyan.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yanzz.aispringbootyan.AiService.PsychologicalSupportService;
import com.yanzz.aispringbootyan.AiService.StructOutPut;
import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.command.ConsultationStreamDTO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.swing.*;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {

    @Resource
    private PsychologicalSupportService psychologicalSupportService;

    @PostMapping("/session/start")
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO consultationSessionCreateDTO) {
        // 获取用户Id
        // 获取token
        String token = JwtTokenUtil.getCurrentToken();
        // 解析token
        DecodedJWT decodedJWT = JwtTokenUtil.verifyToken(token);
        // 获取用户Id
        Long userId = decodedJWT.getClaim("userId").asLong();
        // 调用AI服务层方法开始心理咨询会话
        StructOutPut.StreamChatSession streamChatSession = psychologicalSupportService.startStreamChatSession(userId, consultationSessionCreateDTO);
        return Result.ok(streamChatSession);
    }

    @PostMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sessionChat(@Valid @RequestBody ConsultationStreamDTO consultationStreamDTO) {
        // 获取token
        String token = JwtTokenUtil.getCurrentToken();
        // 解析token
        DecodedJWT decodedJWT = JwtTokenUtil.verifyToken(token);
        // 获取用户Id
        Long userId = decodedJWT.getClaim("userId").asLong();
        if(userId == null) {
            return Flux.just(ServerSentEvent
                    .<String>builder()
                    .event("error")
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), "用户未登录")))
                    .build()
            );
        }
    }
}
