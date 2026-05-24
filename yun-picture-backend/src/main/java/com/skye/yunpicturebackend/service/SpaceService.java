package com.skye.yunpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skye.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.skye.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.skye.yunpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.skye.yunpicturebackend.model.entity.User;
import com.skye.yunpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author skye
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-05-14 11:12:05
 */
public interface SpaceService extends IService<Space> {

    //创建私人空间
    Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    // 获取查询对象   获取查询条件（取字段）-》拼接成为mysql的查询
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    //获取空间包装类（单条）
    // 实际的业务含义是：把数据库查出来的原始 Space 实体，转换成专门用于前端展示的 SpaceVO 对象。
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    //获取空间包装类（分页）
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    //数据校验
    void validSpace(Space space, Boolean add);

    //根据空间级别，给空间对象填入最大容量和条数
    void fillSpaceBySpaceLevel(Space space);


}
