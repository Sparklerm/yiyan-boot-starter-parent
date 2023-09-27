package com.yiyan.boot.oss.minio.autoconfigure;

import com.yiyan.boot.oss.minio.autoconfigure.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio自动配置类
 *
 * @author Sparkler
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "oss.minio.enable", havingValue = "true")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    @Bean
    public MinioClient getMinioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint(), minioProperties.getPort(), minioProperties.getSecure())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}