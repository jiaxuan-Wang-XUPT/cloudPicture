package com.skye.yunpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片 id  根据图片id就能获取图片url地址，就能执行以图找图的api
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
