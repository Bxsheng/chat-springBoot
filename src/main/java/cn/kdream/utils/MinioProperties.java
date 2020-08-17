package cn.kdream.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Author Bxsheng
 * @BlogAddress www.kdream.cn
 * @CreateTime 2020-08-03
 * JDK 1.8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String url;

    private Integer port;

    private String accessKey;

    private String secretKey;

    private Boolean secure;

    private String bucketName;

    private Integer fileSize;

    private String PrefixName;


}
