package com.yanzz.aispringbootyan.service;

import com.yanzz.aispringbootyan.DTO.command.UserLoginCommandDTO;
import com.yanzz.aispringbootyan.DTO.command.UserRegisterCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.UserLoginResponseDTO;
import jakarta.validation.Valid;

public interface UserService {

    UserLoginResponseDTO login(@Valid UserLoginCommandDTO userLoginCommandDTO);

    UserLoginResponseDTO.UserDetailResponseDTO register(@Valid UserRegisterCommandDTO userRegisterCommandDTO);

    UserLoginResponseDTO.UserDetailResponseDTO getUserDetailByUserId(Long userId);
}
