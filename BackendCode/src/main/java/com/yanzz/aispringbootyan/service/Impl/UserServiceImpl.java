package com.yanzz.aispringbootyan.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzz.aispringbootyan.DTO.command.UserLoginCommandDTO;
import com.yanzz.aispringbootyan.DTO.command.UserRegisterCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.UserLoginResponseDTO;
import com.yanzz.aispringbootyan.Enum.UserType;
import com.yanzz.aispringbootyan.entity.User;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.mapper.UserMapper;
import com.yanzz.aispringbootyan.service.UserService;
import com.yanzz.aispringbootyan.service.convert.UserConvert;
import com.yanzz.aispringbootyan.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    // 密码加密器
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserLoginResponseDTO login(UserLoginCommandDTO userLoginCommandDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userLoginCommandDTO.getUsername())
                .or()
                .eq(User::getEmail, userLoginCommandDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        // 判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 验证密码（将明文密码和加密后的密码进行匹配）
        if (!bCryptPasswordEncoder.matches(userLoginCommandDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 检查用户状态
        if (!user.isActive()) {
            throw new BusinessException("用户已禁用");
        }
        // 生成登录令牌
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        System.out.println("Generated Token: " + token);
        UserLoginResponseDTO.UserDetailResponseDTO userDetailResponseDTO = UserConvert.entityToDetailResponse(user);
        return UserConvert.buildLoginResponse(token, userDetailResponseDTO);
    }

    @Override
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO userRegisterCommandDTO) {
        // 校验密码
        if (!userRegisterCommandDTO.getPassword().equals(userRegisterCommandDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        // 校验用户名
        LambdaQueryWrapper<User> UserNameQueryWrapper = new LambdaQueryWrapper<>();
        UserNameQueryWrapper.eq(User::getUsername, userRegisterCommandDTO.getUsername());
        Long count = userMapper.selectCount(UserNameQueryWrapper);  //根据用户名查询用户数量
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 校验邮箱
        LambdaQueryWrapper<User> UserEmailQueryWrapper = new LambdaQueryWrapper<>();
        UserEmailQueryWrapper.eq(User::getEmail, userRegisterCommandDTO.getEmail());
        count = userMapper.selectCount(UserEmailQueryWrapper);  //根据邮箱查询用户数量
        if (count > 0) {
            throw new BusinessException("邮箱已存在");
        }
        // 校验用户类型
        if (!UserType.isValidCode(userRegisterCommandDTO.getUserType())) {
            throw new BusinessException("用户类型不合法");
        }
        // 创建用户
        // 获取要设置的原始密码
        String password = userRegisterCommandDTO.getPassword().trim();
        // 设置密码（使用BCryptPasswordEncoder进行加密）
        String encodePassword = bCryptPasswordEncoder.encode(password);
        User user = UserConvert.registerToEntity(userRegisterCommandDTO, encodePassword);
        // 保存用户到数据库
        userMapper.insert(user);
        return UserConvert.entityToDetailResponse(user);
    }

    @Override
    public UserLoginResponseDTO.UserDetailResponseDTO getUserDetailByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        return UserConvert.entityToDetailResponse(user);
    }
}
