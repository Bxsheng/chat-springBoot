package cn.kdream.netty;

import cn.kdream.chat.SpringUtil;
import cn.kdream.enums.MsgActionEnum;
import cn.kdream.netty.pojo.ChatMsg;
import cn.kdream.netty.pojo.DataContent;
import cn.kdream.service.UserService;
import cn.kdream.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class ChatHandler  extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static final ChannelGroup CLIENTS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取客服端传过来的消息
        String content = msg.text();
        System.out.println("接收过来的数据："+content);
        Channel channel = ctx.channel();

        System.out.println("数据查看");
        for (Channel client : CLIENTS) {
            System.out.println(client.id().asLongText());
        }

        UserChannelRel.output();
        System.out.println("数据查看");
//        1.获取客户端传过来的消息
//            1.1 把页面传过来的数据转换为对象
        DataContent dataContent = JsonUtils.jsonToPojo(content,DataContent.class);
        Integer action = dataContent.getAction();
//        2. 根据不同的消息类型对应不同的业务
//            2.1 第一次连接的时候 初始化channel ，把channel和userid 关联起来
            if (MsgActionEnum.CONNECT.type.equals(action)){
                String senderId = dataContent.getChatMsg().getSenderId();
                //保存到hashmap中
                UserChannelRel.put(senderId,channel);

                for (Channel client : CLIENTS) {
                    System.out.println(client.id().asLongText());
                }

                UserChannelRel.output();
            }

//            2.2 聊天类型信息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]
            else if(MsgActionEnum.CHAT.type.equals(action)){
//                2.3.1 保存信息到数据库并且标记为未签收
                ChatMsg chatMsg = dataContent.getChatMsg();
                String msgText = chatMsg.getMsg();
                String receiverId = chatMsg.getReceiverId();
                String senderId = chatMsg.getSenderId();

                //保存信息到数据库 使用工具类获取容器中的bean
                UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
                String saveMsgId = userService.saveMsg(chatMsg);
                chatMsg.setMsgId(saveMsgId);
                //封装需要发送的数据信息
                DataContent dataContentMsg = new DataContent();
                dataContentMsg.setChatMsg(chatMsg);
//                 2.3.2 发送消息到接收方
                //获取接收方的channel
                Channel receiverChannel = UserChannelRel.get(receiverId);
                if (receiverChannel ==null){
                    // 用户离线 ，可以使用推送消息

                }else{
                    //当receiverChannel不为空的时候去 CLIENTS 查询channel 是否存在
                    Channel findChannel = CLIENTS.find(receiverChannel.id());
                    if (findChannel !=null){
                        System.out.println("发送数据：");
                        System.out.println(JsonUtils.objectToJson(dataContentMsg));
                        //用户在线
                        receiverChannel.writeAndFlush(new TextWebSocketFrame(
                                JsonUtils.objectToJson(dataContentMsg)
                        ));
                    }else{
                        // 用户离线 ，可以使用推送消息
                    }
                }
            }
//            2.3 签收消息类型 ，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]
            else if(MsgActionEnum.SIGNED.type.equals(action)){
                UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
                String msgIdsStr = dataContent.getExtand();
                String msgIds[] = msgIdsStr.split(",");

                List<String> msgIdlist = new ArrayList<>();
                for (String mid : msgIds) {
                    if (StringUtils.isNoneBlank(mid)){
                        msgIdlist.add(mid);
                    }
                }
                //输出信息查看
                System.out.println(msgIdlist.toString());
                if (msgIdlist.size()>0 && !msgIdlist.isEmpty() && msgIdlist!=null){
                    //批量签收
                    userService.updateMsgSigned(msgIdlist);
                }else{
                    //单条数据签收
                    msgIdlist.add(msgIdsStr);
                    userService.updateMsgSigned(msgIdlist);
                }

            }
//            2.4 心跳类型消息
            else if(MsgActionEnum.KEEPALIVE.type.equals(action)){
                System.out.println("收到来自channel为[" + channel + "]的心跳包...");
            }


    }

    /**
     * 当客户端连接服务端之后（打开连接）
     * 获取客户端的channle，并且放到ChannelGroup中去进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // super.handlerAdded(ctx);
        CLIENTS.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当触发handlerRemoved ，ChannelGroup会自动移除客户端的channel
        //  CLIENTS.remove(ctx.channel());
        System.out.println("客户端断开，channle对应的长id为"+ctx.channel().id().asLongText());
        System.out.println("客户端断开，channle对应的短id为"+ctx.channel().id().asShortText());
        //移除对象的channel
        CLIENTS.remove(ctx.channel());
        for (Channel client : CLIENTS) {
            System.out.println(client.id().asShortText());
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
        //发生异常移除
        CLIENTS.remove(ctx.channel());
      //  super.exceptionCaught(ctx, cause);
    }
}
