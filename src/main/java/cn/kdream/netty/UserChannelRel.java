package cn.kdream.netty;

import io.netty.channel.Channel;

import java.util.HashMap;

/**
 * @Description
 * @Author Bxsheng
 * @BoolAddress kdream.cn
 * @Date 2020-08-13
 */
public class UserChannelRel {
    private static HashMap<String, Channel> manager = new HashMap<>();

    public static void put(String sendId,Channel channel){
        manager.put(sendId,channel);
    }
    public static Channel get(String sendId){
        return manager.get(sendId);
    }
    public static void output(){
        for (HashMap.Entry<String, Channel> entry : manager.entrySet()) {
            System.out.println("UserId：" + entry.getKey() + ", ChannelId：" + entry.getValue().id().asLongText());
        }
    }
}
