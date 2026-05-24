package com.skye.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/*
用户角色枚举:
对于用户角色这样值的数量有限的、可枚举的字段，
最好定义一个枚举类，便于在项目中获取值、减少枚举值输入错误的情况。
*/
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
//        如果枚举值特别多，可以 Map 缓存所有枚举值来加速查找，而不是遍历列表。
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}

