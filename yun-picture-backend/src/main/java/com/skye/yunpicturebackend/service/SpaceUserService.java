package com.skye.yunpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.skye.yunpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.skye.yunpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.skye.yunpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.skye.yunpicturebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author skye
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-05-21 10:43:50
 */
public interface SpaceUserService extends IService<SpaceUser> {
    //创建空间成员
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    //数据校验
    void validSpaceUser(SpaceUser spaceUser, boolean add);


    //获取空间成员包装类（单条）
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    //获取空间成员包装类（分页）
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    // 获取查询对象   获取查询条件（取字段）-》拼接成为mysql的查询
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
