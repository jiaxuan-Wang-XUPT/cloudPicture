package com.skye.yunpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/*
dto表示封装对象，一般用于接收前端的值，或者是接收service之间的传递值
个人理解：原本我们讲这些参数直接写在controller中的形参列表中，
但是上述方法会比较麻烦不好管理，而我们将其抽离写在一个额外的dto中，
虽然多写一个类，但是后期更好维护和检查，接口更安全。

在 Java 接口开发中，为每个接口定义一个专门的类来接收请求参数，
可以提高代码的可读性和维护性，便于对参数进行统一验证和扩展，
同时减少接口方法参数过多导致的复杂性，有助于在复杂场景下更清晰地管理和传递数据。
*/

/*
用户注册请求
*/

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}

