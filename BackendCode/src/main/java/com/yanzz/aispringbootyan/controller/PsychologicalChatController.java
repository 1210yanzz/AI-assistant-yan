package com.yanzz.aispringbootyan.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.AiService.EmotionAnalysisService;
import com.yanzz.aispringbootyan.AiService.PsychologicalSupportService;
import com.yanzz.aispringbootyan.AiService.StructOutPut;
import com.yanzz.aispringbootyan.DTO.command.ConsultationSessionCreateDTO;
import com.yanzz.aispringbootyan.DTO.command.ConsultationStreamDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationMessageResponseDTO;
import com.yanzz.aispringbootyan.DTO.response.ConsultationSessionVO;
import com.yanzz.aispringbootyan.DTO.response.SessionEmotionVO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.entity.ConsultationSession;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.service.ConsultationMessageService;
import com.yanzz.aispringbootyan.service.ConsultationSessionService;
import com.yanzz.aispringbootyan.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {

    // 与 user.user_type 保持一致：2 为管理员
    private static final int ADMIN_USER_TYPE = 2;

    @Resource
    private PsychologicalSupportService psychologicalSupportService;

    @Resource
    private ConsultationSessionService consultationSessionService;

    @Resource
    private ConsultationMessageService consultationMessageService;

    @Resource
    private EmotionAnalysisService emotionAnalysisService;

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

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
        // 调用 AI 服务层进行流式对话，识别用户消息，返回增量文本
        return psychologicalSupportService
                .streamPsychologicalChat(userId, consultationStreamDTO.getSessionId(), consultationStreamDTO.getUserMessage())
                // 前端按 Result JSON 解析，每个增量包成 { code, message, data: { content } }
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", chunk))))
                        .build())
                .concatWithValues(ServerSentEvent.<String>builder().event("done").data("[DONE]").build())
                .onErrorResume(error -> Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(JSONUtil.toJsonStr(Result.error(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage(), error.getMessage())))
                        .build()));
    }

    // 分页查询会话：前台传 pageNum/pageSize，后台传 currentPage/size，这里都兼容
    @GetMapping("/sessions")
    public Result<Page<ConsultationSessionVO>> getSessionPage(
            @RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestParam(value = "currentPage", required = false) Long currentPage,
            @RequestParam(value = "size", required = false) Long size) {
        DecodedJWT decodedJWT = getCurrentJwt();
        long current = pageNum != null ? pageNum : (currentPage != null ? currentPage : 1L);
        long pageSizeValue = pageSize != null ? pageSize : (size != null ? size : 10L);
        // 防御非法分页参数
        current = Math.max(current, 1L);
        pageSizeValue = pageSizeValue < 1L || pageSizeValue > 100L ? 10L : pageSizeValue;
        return Result.ok(consultationSessionService.pageSessions(
                getUserId(decodedJWT), isAdmin(decodedJWT), current, pageSizeValue));
    }

    // 查询某个会话的全部消息
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponseDTO>> getSessionMessages(@PathVariable Long sessionId) {
        DecodedJWT decodedJWT = getCurrentJwt();
        // 先校验会话存在且当前用户有权限访问
        consultationSessionService.getOwnedSession(sessionId, getUserId(decodedJWT), isAdmin(decodedJWT));
        return Result.ok(consultationMessageService.listMessagesBySessionId(sessionId));
    }

    // 删除会话及其消息
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        DecodedJWT decodedJWT = getCurrentJwt();
        consultationSessionService.deleteSession(sessionId, getUserId(decodedJWT), isAdmin(decodedJWT));
        return Result.ok();
    }

    // 会话情绪分析：优先返回缓存，有新消息时调用 DeepSeek 重新分析
    @GetMapping("/session/{sessionId}/emotion")
    public Result<SessionEmotionVO> getSessionEmotion(@PathVariable String sessionId) {
        DecodedJWT decodedJWT = getCurrentJwt();
        Long sessionIdLong = parseSessionId(sessionId);
        ConsultationSession session = consultationSessionService.getOwnedSession(
                sessionIdLong, getUserId(decodedJWT), isAdmin(decodedJWT));
        return Result.ok(emotionAnalysisService.analyze(session));
    }

    // 前端传的是 session_123 格式，这里还原成数据库主键
    private Long parseSessionId(String sessionId) {
        if (sessionId == null) {
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "无效的会话ID");
        }
        String idPart = sessionId.startsWith("session_")
                ? sessionId.substring("session_".length())
                : sessionId;
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "无效的会话ID");
        }
    }

    // 获取当前请求携带的 JWT
    private DecodedJWT getCurrentJwt() {
        String token = JwtTokenUtil.getCurrentToken();
        return JwtTokenUtil.verifyToken(token);
    }

    private Long getUserId(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("userId").asLong();
    }

    // 登录时 roleType 写入的是 userType：1 普通用户，2 管理员
    private boolean isAdmin(DecodedJWT decodedJWT) {
        Integer roleType = decodedJWT.getClaim("roleType").asInt();
        return roleType != null && roleType == ADMIN_USER_TYPE;
    }
}
