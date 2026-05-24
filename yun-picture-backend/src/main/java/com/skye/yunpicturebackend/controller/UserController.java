package com.skye.yunpicturebackend.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skye.yunpicturebackend.annotation.AuthCheck;
import com.skye.yunpicturebackend.common.BaseResponse;
import com.skye.yunpicturebackend.common.DeleteRequest;
import com.skye.yunpicturebackend.common.ResultUtils;
import com.skye.yunpicturebackend.constant.UserConstant;
import com.skye.yunpicturebackend.exception.BusinessException;
import com.skye.yunpicturebackend.exception.ErrorCode;
import com.skye.yunpicturebackend.exception.ThrowUtils;
import com.skye.yunpicturebackend.model.dto.user.*;
import com.skye.yunpicturebackend.model.entity.User;
import com.skye.yunpicturebackend.model.vo.LoginUserVO;
import com.skye.yunpicturebackend.model.vo.UserVO;
import com.skye.yunpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController  //输出为json
@RequestMapping("/user")
public class UserController {

    @Resource  //等价于Autowried但是，A通过类型匹配，R通过名称匹配
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")  //用户注册是一个动作，不是查询，所以用post
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户注销（退出登录）
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 用户管理（增删改查）(针对管理员使用的)
     * 由于这部分比较简单，所以业务逻辑直接写在controller，没有抽一层service
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) //自定义的注释，要求必须有某角色admin/user
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtils.throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        User user = new User();
        //前端接收封装userAddRequest赋值给user（也就是前端穿什么，user就接收什么）
        //本质就是将UserAddRequest转换为user对象插入到数据库中
        BeanUtil.copyProperties(userAddRequest,user);
        //默认密码
        final String DEFAULT_PASSWORD = "12345678";
        //加密密码
        String encryptPassword =userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId()); //<Long>
    };

    /**
     * 根据id获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id){
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        // default T getById(Serializable id) {
        //        return (T)this.getBaseMapper().selectById(id);
        //    }  这是mybatis-plus的getById方法
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    };

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     *
     */
    @PostMapping("/update/password")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updatePassword(@RequestBody ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(changePasswordRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        //获取当前登录用户id
        User loginUser = userService.getLoginUser(request);
        Long currentUserId = loginUser.getId();
        userService.changePassword(changePasswordRequest, currentUserId);
        return ResultUtils.success(true);
    }


    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }


}

