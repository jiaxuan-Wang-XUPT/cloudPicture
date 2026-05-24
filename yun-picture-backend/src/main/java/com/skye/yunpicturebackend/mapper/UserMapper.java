package com.skye.yunpicturebackend.mapper;

import com.skye.yunpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author skye
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2026-05-04 10:50:24
* @Entity com.yupi.yupicturebackend.model.entity.User
*/

//官方提供的extends BaseMapper 里面包含很多增删改查功能，方便书写
public interface UserMapper extends BaseMapper<User> {

}




