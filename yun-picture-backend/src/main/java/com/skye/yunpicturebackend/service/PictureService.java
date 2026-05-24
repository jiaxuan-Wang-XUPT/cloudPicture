package com.skye.yunpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skye.yunpicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.skye.yunpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.skye.yunpicturebackend.model.dto.picture.*;
import com.skye.yunpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.skye.yunpicturebackend.model.entity.User;
import com.skye.yunpicturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author skye
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-05-07 15:22:03
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    // 获取查询对象   获取查询条件（取字段）-》拼接成为mysql的查询
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    //获取图片包装类（单条）
    // 实际的业务含义是：把数据库查出来的原始 Picture 实体，转换成专门用于前端展示的 PictureVO 对象。
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    //获取图片包装类（分页）
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    //数据校验
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 图片审核
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * 清理图片文件 （区别于删除）
     *
     * @param oldPicture
     */

    void clearPictureFile(Picture oldPicture);

    /**
     * 校验空间图片的权限     公共编辑/删除（编辑和删除的权限一致）
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);


    void deletePicture(long pictureId, User loginUser);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

}



