package com.skye.yunpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangePasswordRequest implements Serializable {

    private String userPassword;

    private String newPassword;

    private String checkPassword;

}

