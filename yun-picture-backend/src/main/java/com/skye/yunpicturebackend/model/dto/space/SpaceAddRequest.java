package com.skye.yunpicturebackend.model.dto.space;

import lombok.Data;

@Data
public class SpaceAddRequest {

    private String spaceName;

    /**
     * 空间级别： 0-普通 1-专业 2-旗舰
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;

}
