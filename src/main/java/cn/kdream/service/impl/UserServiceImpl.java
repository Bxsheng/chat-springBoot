package cn.kdream.service.impl;

import cn.kdream.enums.MsgActionEnum;
import cn.kdream.enums.MsgSignFlagEnum;
import cn.kdream.enums.SerchFriendsStatusEnum;
import cn.kdream.mapper.*;
import cn.kdream.netty.UserChannelRel;
import cn.kdream.netty.pojo.ChatMsg;
import cn.kdream.netty.pojo.DataContent;
import cn.kdream.pojo.FriendsRequest;
import cn.kdream.pojo.MyFriends;
import cn.kdream.pojo.Users;
import cn.kdream.pojo.vo.FriendsRequestVo;
import cn.kdream.pojo.vo.FriendsVo;
import cn.kdream.service.UserService;
import cn.kdream.utils.JsonUtils;
import cn.kdream.utils.MinioUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-02
 * JDK 1.8
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private MinioUtils minioUtils;

    @Override
    public boolean queryUserNameIsExist(String name) {
        Users user  = new Users();
        user.setUsername(name);
        Users result =  usersMapper.selectOne(user);
        return result !=null ? true:false;
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",pwd);
        Users result = usersMapper.selectOneByExample(example);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {
        //为每个用户生成唯一的二维码
        String qRcode=null;
        try {
            qRcode= minioUtils.uploadQRcode(user.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setQrcode(qRcode);
        user.setId(sid.nextShort());
        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        //根据用户Bean里面的主键修改有值的数据信息
       usersMapper.updateByPrimaryKeySelective(user);
       //获取用户的最新的数据信息
        return  this.queryUserById(user.getId());
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserById(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myId, String friendName) {
        // 1. 判断 用户传过来的friendName是否存在
        // 2. 判断 用户是否是否是自己
        // 3. 判断 是否已经是已经是自己的好友了
        //4.返回 被搜索用户的信息

        Users user = this.queryUserInfoByUserName(friendName);
        if (user == null){
            return SerchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        // 获取到需要添加的好友ID
        if (user.getId().equals(myId)){
            return SerchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        MyFriends myFriends = this.queryUserAndFriend(myId,user.getId());
        if (myFriends != null){
            return SerchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SerchFriendsStatusEnum.SUCCESS.status;
    }

    /**
     *  根据用户名查找信息
     * @param userName
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUserName(String userName) {
        Example example = new Example(Users.class);
        Example.Criteria ec = example.createCriteria();
        ec.andEqualTo("username",userName);
        return usersMapper.selectOneByExample(example);
    }

    /**
     * 添加好友
     * @param myId
     * @param friendName
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addFriend(String myId, String friendName) {
        Users friend = this.queryUserInfoByUserName(friendName);
        //判断好友请求中是否存在 如果不存在就添加存在跳过
        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",myId);
        criteria.andEqualTo("acceptUserId",friend.getId());
        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(example);
        //如果没有申请数据在申请表中添加数据
        if (friendsRequest == null){
            FriendsRequest friendsRe= new FriendsRequest();
            friendsRe.setId(sid.nextShort());
            friendsRe.setSendUserId(myId);
            friendsRe.setAcceptUserId(friend.getId());
            friendsRe.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRe);
        }
    }

    @Override
    public List<FriendsRequestVo> queryFriendsRequestList(String acceptUserId) {


        return  usersMapperCustom.queryFriendsRequestList(acceptUserId);
    }

    /**
     * 通过请求在用户表中保存信息
     * @param myId
     * @param friendId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveFriendReuest(String myId, String friendId) {
            //保存好友信息在我的好友列表中
            saveFriends(myId,friendId);
            //保存我的信息在用户列表里
            saveFriends(friendId,myId);
            //删除好友申请
            deleteFriendReuest(myId,friendId);


            //如果朋友id在线 那么发送信息让他更新
        Channel channel = UserChannelRel.get(friendId);
        if (channel != null){
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            channel.writeAndFlush(
                    new TextWebSocketFrame(
                            JsonUtils.objectToJson(dataContent)));
        }

    }

    /**
     * 保存信息在好友列表中
     * @param myId
     * @param friendId
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFriends(String myId, String friendId){
        MyFriends myFriends =new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyUserId(myId);
        myFriends.setMyFriendUserId(friendId);
        myFriendsMapper.insert(myFriends);
    }

    /**
     *
     * @param myId 就是接受信息的人
     * @param friendId 发送信息的用户
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendReuest(String myId, String friendId) {
            //FriendsRequest friendsRequest = new FriendsRequest();
            Example example = new Example(FriendsRequest.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("sendUserId",friendId);
            criteria.andEqualTo("acceptUserId",myId);
            friendsRequestMapper.deleteByExample(example);
    }

    /**
     * 获取我的好友列表
     * @param userId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendsVo> getFriendsList(String userId) {
        return usersMapperCustom.queryFriendsList(userId);
    }

    @Override
    public Integer friendReauestCount(String myId) {
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(myId);
        return friendsRequestMapper.selectCount(friendsRequest);
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        cn.kdream.pojo.ChatMsg chatMsg1 = new cn.kdream.pojo.ChatMsg();
        String msgId = sid.nextShort();
        chatMsg1.setId(msgId);
        chatMsg1.setAcceptUserId(chatMsg.getReceiverId());
        chatMsg1.setSendUserId(chatMsg.getSenderId());
        chatMsg1.setMsg(chatMsg.getMsg());
        chatMsg1.setSignFlag(MsgSignFlagEnum.unsign.type);
        chatMsg1.setCreateTime(new Date());
        chatMsgMapper.insert(chatMsg1);

        return msgId;




    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(List<String> msgIdlist) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdlist);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<cn.kdream.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {

        Example chatExample = new Example(cn.kdream.pojo.ChatMsg.class);
        Example.Criteria criteria = chatExample.createCriteria();
        criteria.andEqualTo("signFlag",0);
        criteria.andEqualTo("acceptUserId",acceptUserId);
        List<cn.kdream.pojo.ChatMsg> chatMsgList = chatMsgMapper.selectByExample(chatExample);
        return chatMsgList;
    }


    /**
     * 根据我的Id和添加的用户名在关联表中查找数据
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public MyFriends queryUserAndFriend(String myId, String friendId){
        Example example = new Example(MyFriends.class);
        Example.Criteria ec = example.createCriteria();
        ec.andEqualTo("myUserId",myId);
        ec.andEqualTo("myFriendUserId",friendId);
        return myFriendsMapper.selectOneByExample(example);
    }


}
