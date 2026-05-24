package com.skye.yunpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skye.yunpicturebackend.exception.BusinessException;
import com.skye.yunpicturebackend.exception.ErrorCode;
import com.skye.yunpicturebackend.exception.ThrowUtils;
import com.skye.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.skye.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.skye.yunpicturebackend.model.entity.Picture;
import com.skye.yunpicturebackend.model.entity.Space;
import com.skye.yunpicturebackend.model.entity.SpaceUser;
import com.skye.yunpicturebackend.model.entity.User;
import com.skye.yunpicturebackend.model.enums.SpaceLevelEnum;
import com.skye.yunpicturebackend.model.enums.SpaceRoleEnum;
import com.skye.yunpicturebackend.model.enums.SpaceTypeEnum;
import com.skye.yunpicturebackend.model.vo.PictureVO;
import com.skye.yunpicturebackend.model.vo.SpaceVO;
import com.skye.yunpicturebackend.model.vo.UserVO;
import com.skye.yunpicturebackend.service.SpaceService;
import com.skye.yunpicturebackend.mapper.SpaceMapper;
import com.skye.yunpicturebackend.service.SpaceUserService;
import com.skye.yunpicturebackend.service.UserService;
import com.skye.yunpicturebackend.manager.sharding.DynamicShardingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static net.sf.jsqlparser.util.validation.metadata.MetadataContext.exists;

/**
 * @author skye
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-05-14 11:12:05
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

//    如果试图将 SpaceService 注入到它自己内部，形成了“自我循环依赖
//    如果想用这个，可以直接用this代替spaceService，比如原本spaceService.fillSpaceBySpaceLevel替换为this.fillSpaceBySpaceLevel
//    @Resource
//    @Lazy
//    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    @Override
    public Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //1.校验参数默认值
        //转换实体类 和dto
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        //有了上面的转换，就可以填入默认值了
        if (spaceAddRequest.getSpaceName() == null) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(0);
        }
        if (spaceAddRequest.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        //填充参数
        this.fillSpaceBySpaceLevel(space);
        //2.校验参数
        this.validSpace(space, true);
        //3.校验权限，非管理员只能创建普通空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        //如果创建的等级不等于普通等级 或者 不是管理员
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() &&
                !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限创建指定级别空间");
        }
        //4.控制同一用户只能创建一个私有空间,以及一个团队空间
        //针对用户进行加锁（让不同用户能同时创建空间，所以加锁不能在方法上，而应该在用户上(userId)）
        //不加 intern()：每次都在现做新锁，哪怕内容一样，锁也不是同一把，导致锁不住。
        //加上 intern()：保证相同内容的字符串，拿到的是全局唯一的一把锁。相同用户抢同一把锁（串行排队），不同用户抢不同的锁（并行执行）
        //注意，上述代码中，我们使用本地 synchronized 锁对 userld 进行加锁，
        // 这样不同的用户可以拿到不同的锁，对性能的影响较低。
        // 在加锁的代码中，我们使用 Spring 的 编程式事务管理器 transactionTemplate 封装跟数据库有关的査询和插入操作，
        // 而不是使用 @Transactional 注解来控制事务，这样可以保证事务的提交在加锁的范围内。
        String lock = String.valueOf(userId).intern(); //intern() 的核心作用是“确保返回常量池中的同一个引用”
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                //判断是否已经存在空间
                //用mybatis中的lambdaQuery方法，查一下Space的getUserId是否等于userId
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, space.getSpaceType())
                        .exists();
                //如果有，不能创建
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类只有一个空间");
                //如果没有，创建
                // 写入数据库
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                // 如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                //创建分表
                // dynamicShardingManager.createSpacePictureTable(space);
                // 返回新写入的数据 id
                return space.getId();

            });
            //返回结果是包装类，可以做一些处理
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    ;

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space, Boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额.
        // (因为我们已经在Enum中设置好了等级对于多少容量和条数，所以我们这里就能通过传递过来的level获得到MaxSize MaxCount)
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {  //优先已管理员输入的为主，如果管理员没有输入，则按照默认标准来
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }


}




