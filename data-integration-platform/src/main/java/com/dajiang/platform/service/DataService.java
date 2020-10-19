package com.dajiang.platform.service;

import com.dajiang.platform.config.SocketProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DataService implements CommandLineRunner {

    @Autowired
    private SocketProperties properties;

    @Override
    public void run(String... args) throws Exception {
        ServerSocket server = null;
        Socket socket = null;
        server = new ServerSocket(properties.getPort());
        log.info("设备服务器已经开启, 监听端口:" + properties.getPort());
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                properties.getPoolCore(),
                properties.getPoolMax(),
                properties.getPoolKeep(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(properties.getPoolQueueInit()),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        while (true) {
            socket = server.accept();
            pool.execute(new DataParseThread(socket, properties.getTimeout()));
        }
    }
}
