package cn.kdream.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

@Component 
public class WSServer {
    private static WSServer single;
    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;
    private WSServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup,subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitializer());
    };
    public  void start(){
        this.future = server.bind(8089);
        System.err.println("netty websocket service 启动完成");
    }



   public static synchronized WSServer getInstace(){
        if (single == null){
            synchronized (WSServer.class){
                if(single == null){
                    single = new WSServer();
                }
            }
        }
        return  single;
    }


}
