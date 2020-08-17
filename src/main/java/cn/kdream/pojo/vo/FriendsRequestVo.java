package cn.kdream.pojo.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

/**
 * 请求信息封装类
 */
@Getter
@Setter
public class FriendsRequestVo {
    /**
     *     发送用户id
     */
    private String sendUserId;
    /**
     *     发送用户名称
     */
    private String sendUserNick;
    /**
     *     发送用户闲鱼号
     */
    private String sendUserName;

    /**
     *     发送用户的头像
     */
    private String sendUserFaceImage;


}
