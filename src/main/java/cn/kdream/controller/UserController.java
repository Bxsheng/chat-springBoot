package cn.kdream.controller;

import cn.kdream.enums.OperTypeEnum;
import cn.kdream.enums.SerchFriendsStatusEnum;
import cn.kdream.pojo.ChatMsg;
import cn.kdream.pojo.Users;
import cn.kdream.pojo.bo.UsersBo;
import cn.kdream.pojo.vo.FriendsRequestVo;
import cn.kdream.pojo.vo.FriendsVo;
import cn.kdream.pojo.vo.UsersVo;
import cn.kdream.service.UserService;
import cn.kdream.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-02
 * JDK 1.8
 */
@RestController
@RequestMapping("user")
public class UserController {


    @Autowired
    private UserService userService;

    @Autowired
    private  MinioUtils minioUtils;

    @Autowired
    private MinioProperties minioProperties;



    @PostMapping("/registOrLogin")
    public UnifyJsonResult registOrLogin(@RequestBody Users user) throws Exception {
        UnifyJsonResult unifyJsonResultV = this.VerificationUser(user);
        if (unifyJsonResultV!=null){
            return  unifyJsonResultV;
        }
        // 判断用户是否存在 不存在注册存在登录
        boolean userNameIsExist = userService.queryUserNameIsExist(user.getUsername());
        Users userResult = null;
        if (userNameIsExist){
            //登录操作
            userResult=  userService.queryUserForLogin(user.getUsername(),MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null){
                return UnifyJsonResult.errorMsg("用户名或密码不正确");
            }
        }else {
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult  = userService.saveUser(user);
        }
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(userResult,usersVo);
        return UnifyJsonResult.success(usersVo);
    }

    /**
     * 验证用户信息
     * @param user
     * @return
     */
    public  UnifyJsonResult VerificationUser(Users user){
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return UnifyJsonResult.errorMsg("用户名或密码不能为空");
        }
        if (VerificationUtils.isContainChinese(user.getUsername())){
            return  UnifyJsonResult.errorMsg("用户名不允许为中文");
        }
        return  null;
    }


    /**
     * 修改用户头像信息
     * @param id 用户id
     * @param file 文件信息
     * @return
     * @throws Exception
     */
    @PostMapping("/upload/{id}")
    public UnifyJsonResult uploadFace(@PathVariable String id,@RequestParam("file") MultipartFile file ) throws Exception {

        System.out.println("请求数");
        UsersBo usersBo = new UsersBo(id,file);
        Users users = new Users();
        //获取更新前的用户信息
        List<String> userImageOld = this.getUpdateOldInfo(usersBo);
        //文件上传到minio
        Map<String,String> imageMap = minioUtils.uploadImageThumb(usersBo.getFile());
        //更新用户头像信息
        users.setId(usersBo.getUserId());
        users.setFaceImage(imageMap.get("url"));
        users.setFaceImageBig(imageMap.get("urlThumb"));
        Users usersUpdate =  userService.updateUserInfo(users);
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(usersUpdate,usersVo);
        //删除旧文件信息
        if (!userImageOld.isEmpty()){
            minioUtils.removeImages(userImageOld);
        }
        return UnifyJsonResult.success(usersVo);

    }

    /**
     * 修改用户昵称
     * @param usersBo
     * @return
     */
    @PostMapping("/modifyUserName")
    public UnifyJsonResult updateUserName(@RequestBody UsersBo usersBo ){
        Users user = new Users();
        user.setId(usersBo.getUserId());
        user.setNickname(usersBo.getNickname());
        Users userInfo = userService.updateUserInfo(user);
        UsersVo usersVo =new UsersVo();
        BeanUtils.copyProperties(userInfo,usersVo);
        return UnifyJsonResult.success(usersVo);
    }


    /**
     * 搜索用户名字
     * @param myId
     * @param friendName
     * @return
     */
    @PostMapping("/search")
    public UnifyJsonResult searchFriend(@RequestParam("myId") String myId,@RequestParam("friendName") String friendName){

        // 1. 判断 用户传过来的friendName是否存在
        // 2. 判断 用户是否是否是自己
        // 3. 判断 是否已经是已经是自己的好友了
        //4.返回 被搜索用户的信息
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(friendName)){
            return UnifyJsonResult.errorMsg("被搜索用户非法");
        }
        Users user = userService.queryUserById(myId);
        if (user ==null){
            return UnifyJsonResult.errorMsg("myId非法");
        }
        Integer resultInt = userService.preconditionSearchFriends(myId, friendName);
        if (resultInt == SerchFriendsStatusEnum.SUCCESS.status){
            Users users = userService.queryUserInfoByUserName(friendName);
            UsersVo usersVo =new UsersVo();
            BeanUtils.copyProperties(users,usersVo);
            //返回成功信息
            return UnifyJsonResult.success(usersVo);

        }
        //返回具体错误提示
        return UnifyJsonResult.errorMsg(SerchFriendsStatusEnum.getMsgByKey(resultInt));
    }

    /**
     * 发送好友请求信息
     * @param myId
     * @param friendName
     * @return
     */
    @PostMapping("/addFriend")
    public UnifyJsonResult addFriend(@RequestParam("myId") String myId,@RequestParam("friendName") String friendName) {
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(friendName)){
            return UnifyJsonResult.errorMsg("请求非法");
        }
        Users user = userService.queryUserById(myId);
        if (user ==null){
            return UnifyJsonResult.errorMsg("myId非法");
        }
        //再次判断添加的好友 是否存在 或者已经是好友
        Integer resultInt = userService.preconditionSearchFriends(myId, friendName);
        //查找失败 返回失败信息
        if (resultInt != SerchFriendsStatusEnum.SUCCESS.status){
            return UnifyJsonResult.errorMsg(SerchFriendsStatusEnum.getMsgByKey(resultInt));
        }
        //处理添加还有信息
        userService.addFriend(myId,friendName);
        return UnifyJsonResult.success("好友请求已发送");

    }

    /**
     * 获取好友请求数
     * @param myId
     * @return
     */
    @PostMapping("/friendReauestCount")
    public UnifyJsonResult friendReauestCount(String myId){
        Integer count =userService.friendReauestCount(myId);
        return UnifyJsonResult.success(count);
    }


    /**
     * 获取好友列表
     * @param myId
     * @return
     */
    @PostMapping("/listFriendRe")
    public UnifyJsonResult addFriend(String myId){
        List<FriendsRequestVo> friendsRequestVoList = userService.queryFriendsRequestList(myId);
        if (friendsRequestVoList.size() <=0){
            return UnifyJsonResult.errorMsg("好友列表为空");
        }
        return UnifyJsonResult.success(friendsRequestVoList);
    }


    /**
     * 忽略或者接受好友请求
     * @param myId
     * @param friendId
     * @param operType
     * @return
     */
    @PostMapping("/lAndAFriendReq")
    public UnifyJsonResult lAndAFriendReq(String myId,String friendId,Integer operType){
        //判断是否为空数据
        if(StringUtils.isBlank(myId) || StringUtils.isBlank(friendId) ||
                operType ==null){
            return UnifyJsonResult.errorMsg("请求非法");
        }
        if (StringUtils.isBlank(OperTypeEnum.getMsgByType(operType))  ){
            return UnifyJsonResult.errorMsg("请求非法");
        }
        //忽略
        if(OperTypeEnum.IGNORE.type.equals(operType)){
            userService.deleteFriendReuest(myId,friendId);
            return UnifyJsonResult.success("忽略好友申请成功");
        }else{
            //接受
            userService.saveFriendReuest(myId,friendId);
            return UnifyJsonResult.success("接受好友申请成功");
        }
    }

    /**
     * 获取我的好友列表
     * @param myId
     * @return
     */
    @PostMapping("/getFriendsList")
    public UnifyJsonResult getFriendsList(String myId){
        if (StringUtils.isBlank(myId)){
            return UnifyJsonResult.errorMsg("请求非法");
        }
        Users user = userService.queryUserById(myId);
        if (user ==null){
            return UnifyJsonResult.errorMsg("myId非法");
        }
        List<FriendsVo> friendsVoList = userService.getFriendsList(myId);
        if (friendsVoList.size() <=0){
            return UnifyJsonResult.errorMsg("没有好友请求信息");
        }
        return UnifyJsonResult.success(friendsVoList);
    }

    @PostMapping("/getUnReadMsgList")
    public UnifyJsonResult getUnReadMsgList(String myId){
        if (StringUtils.isBlank(myId)){
            return UnifyJsonResult.errorMsg("请求非法");
        }
        Users user = userService.queryUserById(myId);
        if (user ==null){
            return UnifyJsonResult.errorMsg("myId非法");
        }
        List<ChatMsg> chatMsgs = userService.getUnReadMsgList(myId);
        return UnifyJsonResult.success(chatMsgs);
    }

    private List<String> getUpdateOldInfo(UsersBo usersBo) {
        Users userOldInfo =userService.queryUserById(usersBo.getUserId());
        List<String> images = new LinkedList<>();
        if (userOldInfo!=null){
            if (userOldInfo.getFaceImage()!=null){
                String url = minioProperties.getUrl()+":"+minioProperties.getPort()+"/"+minioProperties.getBucketName()
                        +"/";
                images.add(userOldInfo.getFaceImage().replace(url,""));
                images.add(userOldInfo.getFaceImageBig().replace(url,""));
                return images;
            }
        }
        return null;
    }





}
