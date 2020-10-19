package com.dajiang.platform.utils;

import org.springframework.stereotype.Component;

/**
 * 返回给客户端的状态码
 */
@Component
public class Response {
    // 接收正常、解析正常
    public static final String OK = "OK";
    // 接收正常、解析异常
    public static final String UNRECOGNIZED = "UNRECOGNIZED";
    // 接收异常
    public static final String ERROR = "ERROR";
}
