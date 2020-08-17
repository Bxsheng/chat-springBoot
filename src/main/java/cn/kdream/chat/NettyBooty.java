package cn.kdream.chat;

import cn.kdream.netty.WSServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author 笨小昇
 */
@Component
public class NettyBooty implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null){
            //启动netty
            try {
                WSServer.getInstace().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
