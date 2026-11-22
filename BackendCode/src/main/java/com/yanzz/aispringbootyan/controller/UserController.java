package com.yanzz.aispringbootyan.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.yanzz.aispringbootyan.DTO.command.UserLoginCommandDTO;
import com.yanzz.aispringbootyan.DTO.command.UserRegisterCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.UserLoginResponseDTO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.service.UserService;
import com.yanzz.aispringbootyan.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     *
     * @param userLoginCommandDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO userLoginCommandDTO) {
        UserLoginResponseDTO result = userService.login(userLoginCommandDTO);
        return Result.ok(result);
    }

    /**
     * 用户注册
     *
     * @param userRegisterCommandDTO
     * @return
     */
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO userRegisterCommandDTO) {
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(userRegisterCommandDTO);
        return Result.ok(result);
    }

    /**
     * 退出登录
     * JWT 是无状态的，服务端不保存登录状态，前端收到成功后清除本地 token 即可
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        // 获取token
        String token = JwtTokenUtil.getCurrentToken();
        // 解析token
        DecodedJWT decodedJWT = JwtTokenUtil.verifyToken(token);
        // 获取用户Id
        Long userId = decodedJWT.getClaim("userId").asLong();
        // 调用服务层获取当前用户信息
        UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserDetailByUserId(userId);
        return Result.ok(user);
    }
}
