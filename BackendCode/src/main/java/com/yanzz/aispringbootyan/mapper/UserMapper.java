package com.yanzz.aispringbootyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanzz.aispringbootyan.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
