package cn.kdream.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author 笨小昇
 */
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //websocket 基于http协议，所以要有http编解码器
        pipeline.addLast(new HttpServerCodec());

        //对大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());

        //对httpMessage进行聚合，聚合成fullHttpRequest 或 fullHttpResponse
        // 机会所有netty中的编程，都会用到此hanler
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        //==============以上是用于支持http协议=================//


        //==============心跳绑定=================//
        pipeline.addLast(new IdleStateHandler(
                57,58,60));
        pipeline.addLast(new HeartBeatHandler());


        //==============心跳绑定=================//






        //webscoket 服务器处理的协议，用于指定给客户端链接访问的路由：/ws
        //本 handler会帮你处理一些繁重的复杂额的事
        //会帮你处理握手动作 ： handshaking （close，ping，pong） ping+ pong =心跳
        // 对于websocket来讲，都死以frames进行传输的，不同的数据类型对应不同的frames也不同；
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义的handler
        pipeline.addLast(new ChatHandler());
    }
}
