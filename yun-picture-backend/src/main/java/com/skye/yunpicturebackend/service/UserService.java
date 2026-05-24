package com.skye.yunpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.skye.yunpicturebackend.model.dto.user.ChangePasswordRequest;
import com.skye.yunpicturebackend.model.dto.user.UserLoginRequest;
import com.skye.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.skye.yunpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.skye.yunpicturebackend.model.vo.LoginUserVO;
import com.skye.yunpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author skye
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-05-04 10:50:24
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息（删去敏感信息，比如密码）
     * LoginUserVO是要返回给前端的封装类，习惯放在model/vo下
     * HttpServletRequest request就是在session存储信息？
     * 为什么要用自定义的一个封装了，是因为我们不需要把所有信息都返回到前端
     * 只需要所以多补充一些校验条件，在用户登录成功后，将信息存在当前session中
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /*
    获得脱敏后的登录用户信息
    */
    LoginUserVO getLoginUserVO(User user);

    /*
    获得脱敏后的用户信息
    */
    UserVO getUserVO(User user);

    /*
    获得脱敏后的用户信息列表
    */
    List<UserVO> getUserVOList(List<User> userList);


    /*
    获取当前登录用户，在service之间传递，不是接口类，
    */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销(这里指的是退出登录，而不是账号注销)
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    //获取查询条件
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    //更改用户密码
    void changePassword(ChangePasswordRequest changePasswordRequest, Long id);

}
