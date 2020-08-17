package cn.kdream.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Description 心跳检查
 * @Author Bxsheng
 * @BoolAddress kdream.cn
 * @Date 2020-08-17
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        //判断evt 是否是 IdleStateEvent (用于触发用户事件，包括读空闲/写空闲/读写空闲)
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;

            if (event.state() == IdleState.READER_IDLE){
                System.out.println("进入读空闲");
            }else if(event.state() == IdleState.WRITER_IDLE){
                System.out.println("进入写空闲");
            }else if(event.state() == IdleState.ALL_IDLE){
                System.out.println("channel关闭前，users的数量为：" + ChatHandler.CLIENTS.size());
                System.out.println("进入读写空闲");
                Channel channel = ctx.channel();
                channel.close();
                System.out.println("channel关闭前，users的数量为：" + ChatHandler.CLIENTS.size());

            }
        }

    }
}
