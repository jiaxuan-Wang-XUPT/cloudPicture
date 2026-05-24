package com.skye.yunpicturebackend.model.dto.space;

//更新请求，给管理员使用，修改空间级别和限额

import lombok.Data;

@Data
public class SpaceUpdateRequest {

    private Long id;

    private String spaceName;

    private Integer spaceLevel;

    private Long maxSize;

    private Long maxCount;


}
