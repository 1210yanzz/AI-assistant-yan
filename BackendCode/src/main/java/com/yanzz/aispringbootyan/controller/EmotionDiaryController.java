package com.yanzz.aispringbootyan.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.EmotionDiaryCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.EmotionDiaryVO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.service.EmotionDiaryService;
import com.yanzz.aispringbootyan.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    // 与 user.user_type 保持一致：2 为管理员
    private static final int ADMIN_USER_TYPE = 2;

    @Resource
    private EmotionDiaryService emotionDiaryService;

    // 新增情绪日记：同一用户同一天再次提交会覆盖原记录
    @PostMapping
    public Result<EmotionDiaryVO> saveDiary(@Valid @RequestBody EmotionDiaryCommandDTO emotionDiaryCommandDTO) {
        DecodedJWT decodedJWT = getCurrentJwt();
        return Result.ok(emotionDiaryService.saveDiary(getUserId(decodedJWT), emotionDiaryCommandDTO));
    }

    // 后台分页查询情绪日记
    @GetMapping("/admin/page")
    public Result<Page<EmotionDiaryVO>> pageDiaries(
            @RequestParam(value = "current", required = false) Long current,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "minMoodScore", required = false) Integer minMoodScore,
            @RequestParam(value = "maxMoodScore", required = false) Integer maxMoodScore) {
        checkAdmin(getCurrentJwt());

        long pageCurrent = current != null && current > 0 ? current : 1L;
        long pageSize = size != null && size > 0 && size <= 100 ? size : 8L;
        return Result.ok(emotionDiaryService.pageDiaries(userId, minMoodScore, maxMoodScore, pageCurrent, pageSize));
    }

    // 后台删除情绪日记
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id) {
        checkAdmin(getCurrentJwt());
        emotionDiaryService.deleteDiary(id);
        return Result.ok();
    }

    private DecodedJWT getCurrentJwt() {
        String token = JwtTokenUtil.getCurrentToken();
        return JwtTokenUtil.verifyToken(token);
    }

    private Long getUserId(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("userId").asLong();
    }

    private void checkAdmin(DecodedJWT decodedJWT) {
        Integer roleType = decodedJWT.getClaim("roleType").asInt();
        if (roleType == null || roleType != ADMIN_USER_TYPE) {
            throw new BusinessException(ResultCode.AUTHORIZED_ERROR.getCode(), "仅管理员可执行该操作");
        }
    }
}
