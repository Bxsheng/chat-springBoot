package cn.kdream.service;

import cn.kdream.netty.pojo.ChatMsg;
import cn.kdream.pojo.Users;
import cn.kdream.pojo.vo.FriendsRequestVo;
import cn.kdream.pojo.vo.FriendsVo;

import java.util.List;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-02
 * JDK 1.8
 */
public interface UserService {
    /**
     * 判断用户名是否存在
     * @param name
     * @return
     */
    boolean queryUserNameIsExist(String name);

    /**
     * 查询用户是否存在
     * @param username
     * @param pwd
     * @return
     */
    Users queryUserForLogin(String username,String pwd);

    /**
     * 注册用户信息
     * @param user
     * @return
     */
    Users saveUser(Users user);


    /**
     * 根据用户信息修改数据
     * @param user
     * @return
     */
    Users updateUserInfo(Users user);

    /**
     * 根据用户id获取信息
     * @param userId
     * @return
     */
    Users queryUserById(String userId);


    /**
     *  添加好友
     * @param myId 自己的ID
     * @param friendName 被搜索用户的用户名
     * @return
     */
    Integer  preconditionSearchFriends(String myId,String friendName);

    /**
     * 根据用户名获取信息
     * @param userName
     * @return
     */
    Users queryUserInfoByUserName(String userName);

    /**
     * 添加好友
     * @param myId
     * @param friendName
     */
    void addFriend(String myId, String friendName);


    /**
     * 查询关于我的所有请求添加好友信息
     * @param acceptUserId
     * @return
     */
    List<FriendsRequestVo> queryFriendsRequestList(String acceptUserId);

    /**
     * 保存好友申请信息
     * @param myId
     * @param friendId
     */
    void saveFriendReuest(String myId, String friendId);

    /**
     * 删除保存好友申请信息
     * @param myId
     * @param friendId
     */
    void deleteFriendReuest(String myId, String friendId);

    /**
     * 获取我的好友列表
     * @param userId
     * @return
     */
    List<FriendsVo> getFriendsList(String userId);

    /**
     * 获取好友请求数
     * @param myId
     * @return
     */
    Integer friendReauestCount(String myId);

    /**
     * 保存聊天信息
     * @param chatMsg
     */
    String saveMsg(ChatMsg chatMsg);

    /**
     * 批量签收数据
     * @param msgIdlist
     */
    void updateMsgSigned(List<String> msgIdlist);

    /**
     * 获取未签收的所有消息
     * @param acceptUserId
     * @return
     */
    List<cn.kdream.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
