package com.skye.yunpicturebackend.manager.websocket;

import com.skye.yunpicturebackend.manager.auth.SpaceUserAuthManager;
import com.skye.yunpicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.skye.yunpicturebackend.model.entity.Picture;
import com.skye.yunpicturebackend.model.entity.Space;
import com.skye.yunpicturebackend.model.entity.User;
import com.skye.yunpicturebackend.model.enums.SpaceTypeEnum;
import com.skye.yunpicturebackend.service.PictureService;
import com.skye.yunpicturebackend.service.SpaceService;
import com.skye.yunpicturebackend.service.UserService;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket握手拦截器,建立连接前要先校验。用于验证登录用户是否有权限，并且记录到session中
 */

@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) throws Exception {
        //获取当前登录用户
        if (request instanceof ServletServerHttpRequest) {
            //平时开发经常拿到ServletRequest，所以这里先获取servletRequest
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            //获取请求参数
            String pictureId = servletRequest.getParameter("pictureId");
            if (pictureId == null) {
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            User loginUser = userService.getLoginUser(servletRequest);
            if (loginUser == null) {
                log.error("未登录，拒绝握手");
                return false;
            }
            //校验用户是否有当前图片的权限
            Picture picture = pictureService.getById(pictureId); //得到图片信息
            if (picture == null) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            //判断图片是否属于团队空间，如果是，则判断用户是否有权限
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (space == null) {
                    log.error("空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("非团队空间，拒绝握手");
                    return false;
                }
            }
            //如果是团队空间，则判断用户是否有权限
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("无权限，拒绝握手");
                return false;
            }
            //设置用户信息到websocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception e) {
    }
}
