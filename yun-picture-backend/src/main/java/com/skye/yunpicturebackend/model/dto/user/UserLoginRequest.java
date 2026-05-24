package com.skye.yunpicturebackend.model.dto.user;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String userAccount;

    private String userPassword;
}
