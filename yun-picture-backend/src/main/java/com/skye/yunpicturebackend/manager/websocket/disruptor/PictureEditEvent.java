package com.skye.yunpicturebackend.manager.websocket.disruptor;

import com.skye.yunpicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.skye.yunpicturebackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 把图片处理（也就是handleTextMessage 中switch（）的各种事件）当作一个事件，等会发送给队列
 */


@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;
    
    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}
