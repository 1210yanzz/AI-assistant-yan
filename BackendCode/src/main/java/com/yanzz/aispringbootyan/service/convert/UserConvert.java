package com.yanzz.aispringbootyan.service.convert;

import com.yanzz.aispringbootyan.DTO.command.UserLoginCommandDTO;
import com.yanzz.aispringbootyan.DTO.command.UserRegisterCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.UserLoginResponseDTO;
import com.yanzz.aispringbootyan.Enum.UserStatus;
import com.yanzz.aispringbootyan.entity.User;

import java.time.LocalDateTime;

public class UserConvert {
    // 构建响应DTO

    /**
     * User实体转换为详情响应DTO
     *
     * @param user User实体
     * @return 用户详情响应DTO
     */
    //  赋值顺序,由内到外。因此这里先给UserLoginResponseDTO中的UserDetailResponseDTO赋值
    public static UserLoginResponseDTO.UserDetailResponseDTO entityToDetailResponse(User user) {
        return UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender())) // 获取性别显示名称,这里需要转换
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 构建登录响应DTO
     *
     * @param token    JWT令牌
     * @param userInfo 用户信息
     * @return 登录响应DTO
     */
    //  再给整个UserLoginResponseDTO赋值
    public static UserLoginResponseDTO buildLoginResponse(String token, UserLoginResponseDTO.UserDetailResponseDTO userInfo) {
        return UserLoginResponseDTO.builder()
                .userInfo(userInfo)
                .token(token)
                .roleType(userInfo.getUserType().toString())
                .build();
    }

    /**
     * 构建用户注册响应DTO
     *
     * @param userRegisterCommandDTO 用户登录命令DTO
     * @param password            密码
      * @return 用户注册响应DTO
     */
    public static User registerToEntity(UserRegisterCommandDTO userRegisterCommandDTO, String password) {
        return User.builder()
                .username(userRegisterCommandDTO.getUsername())
                .email(userRegisterCommandDTO.getEmail())
                .password(password)
                .nickname(userRegisterCommandDTO.getNickname())
                .phone(userRegisterCommandDTO.getPhone())
                .gender(userRegisterCommandDTO.getGender())
                .birthday(userRegisterCommandDTO.getBirthday())
                .userType(userRegisterCommandDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 获取性别显示名称
     *
     * @param gender 性别代码
     * @return 性别显示名称
     */
    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }
}
