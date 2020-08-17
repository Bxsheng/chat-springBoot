package cn.kdream.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author 笨小昇
 */
@SpringBootApplication
// 扫描 所有需要的包 包括一些用到的工具包 所在路径
@ComponentScan(basePackages = {"cn.kdream","org.n3r"})
 // 扫描mybatis mapper的路径
@MapperScan(basePackages="cn.kdream.mapper")
public class ChatApplication {

    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

}
