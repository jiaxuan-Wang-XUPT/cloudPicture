package com.skye.yunpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skye.yunpicturebackend.exception.BusinessException;
import com.skye.yunpicturebackend.exception.ErrorCode;
import com.skye.yunpicturebackend.manager.auth.StpKit;
import com.skye.yunpicturebackend.model.dto.user.ChangePasswordRequest;
import com.skye.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.skye.yunpicturebackend.model.entity.User;
import com.skye.yunpicturebackend.model.enums.UserRoleEnum;
import com.skye.yunpicturebackend.model.vo.LoginUserVO;
import com.skye.yunpicturebackend.model.vo.UserVO;
import com.skye.yunpicturebackend.service.UserService;
import com.skye.yunpicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.skye.yunpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author skye
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-05-04 10:50:24
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

//用户注册
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 12) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度不对");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6 || userPassword.length() > 18) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码长度不对");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        /*
        这是 MyBatis-Plus 框架提供的 IService 接口中的默认方法。
        因为你的 UserServiceImpl 继承了 ServiceImpl<UserMapper, User>，
        所以你可以直接使用这个方法，而不需要自己写 SQL 语句
        */
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    //加密密码方法，
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "skye";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


//用户登录
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        //BeanUtil.copyProperties实现了将老对象user赋值非新对象loginUserVO
        //这样只能赋值新对象有的属性，没有的属性就不用赋值了
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

//获取当前登录用户
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    //
    // 获取查询条件（取字段）-》拼接成为mysql的查询
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest, Long id) {
        if (changePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        //根据id查询用户（获取用户信息）
        User user = this.getById(id);
        //校验旧密码是否正确(需要把旧密码按照之前加密的方法进行加密，否则明文和加密永远报错)
        String encryptOldPassword = getEncryptPassword(changePasswordRequest.getUserPassword());
        if (!encryptOldPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码输入错误");
        }
        //校验新旧密码不能一致
        if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新旧密码一致");
        }
        //校验新密码和确认密码一致
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码和确认密码不一致");
        }
        //获取新密码
        String newPassword = changePasswordRequest.getNewPassword();
        //新密码加密
        String encryptPassword = getEncryptPassword(newPassword);
        //构建更新对象，只更新密码字段，避免覆盖其他字段
        User updatePasswordUser = new User();
        updatePasswordUser.setId(id); // 手动把 ID 塞进对象里,告诉updateById去找哪个id用户
        updatePasswordUser.setUserPassword(encryptPassword);
        // 使用 MyBatis-Plus 的 updateById 更新
        boolean isSuccess = this.updateById(updatePasswordUser);
        if (!isSuccess) {
            throw new RuntimeException("密码更新失败");
        }

    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    //获取脱敏后的用户列表
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    //


}




