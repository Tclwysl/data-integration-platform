package com.dajiang.platform.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ToString
@Component
@Configuration
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix = "socket")
public class SocketProperties {
    private Integer port;
    private Integer timeout;
    private Integer poolKeep;
    private Integer poolCore;
    private Integer poolMax;
    private Integer poolQueueInit;
}

