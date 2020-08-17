package cn.kdream.mapper;

import cn.kdream.pojo.Users;
import cn.kdream.pojo.vo.FriendsRequestVo;
import cn.kdream.pojo.vo.FriendsVo;
import cn.kdream.utils.MyMapper;

import java.util.List;

/**
 * @author Bxsheng
 */
public interface UsersMapperCustom<batchUpdateMsgSigned> extends MyMapper<Users> {

     /**
      * 获取申请好友列表
      * @param acceptUserId
      * @return
      */
     List<FriendsRequestVo> queryFriendsRequestList(String acceptUserId);

     /**
      * 获取我的好友列表
      * @param userId
      * @return
      */
     List<FriendsVo> queryFriendsList(String userId);

     /**
      * 批量修改消息状态
      * @param msgIdList
      */
     void batchUpdateMsgSigned(List<String> msgIdList);
}