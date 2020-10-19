package com.dajiang.platform.service;

import com.dajiang.platform.protocal.Dust;
import com.dajiang.platform.protocal.Elevator;
import com.dajiang.platform.protocal.TowerCrane;
import com.dajiang.platform.utils.DataTranslate;
import com.dajiang.platform.utils.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

@Slf4j
public class DataParseThread extends Thread {
    static int eSSSJ=0;
    static int tSSSJ=0;
    private Socket socket;
    private Integer timeout;

    public DataParseThread(Socket socket, Integer timeout) {
        this.socket = socket;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        BufferedWriter writer = null;
            try {
                log.info("客户 - " + socket.getRemoteSocketAddress() + " -> 机连接成功");

                // 设置超时，影响读取客户端数据的阻塞时间长度
                socket.setSoTimeout(this.timeout);
                InputStream inputStream = socket.getInputStream();

                // 处理数据
                String result = parseData(inputStream);

                // 向客户端返回消息
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(result);
                writer.newLine();
                writer.flush();
            } catch (SocketException socketException) {
                socketException.printStackTrace();
                try {
                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(eSSSJ==16)
                    {
                        eSSSJ=0;
                        //log.info("实时数据继续接收！");
                        run();
                    } else if(tSSSJ==6)
                    {
                        tSSSJ=0;
                        //log.info("实时数据继续接收！");
                        run();
                    }
                    //log.info("非实时数据，主动关闭Socket！");
                    writer.close(); // 主动关闭Socket
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    /**
     * 解析数据
     * @param inputStream
     * @return
     * @throws IOException
     */
    private String parseData(InputStream inputStream) throws IOException/*, DataFormException */{
        // 据说客户端用C/C++写，必须使用下述方法读取数据
        byte[] bytes = new byte[512];
        String hexString;//new
        int len = inputStream.read(bytes);
        String dustdata = new String(bytes);
        if (len != -1) {
            //String hexString = DataTranslate.bytesToHexString(bytes);//old
            String hexString1 = DataTranslate.bytesToHexString(bytes);//new

            byte[] pdsj = DataTranslate.hexStringToBytes(hexString1);
            if(pdsj[2]==18 || pdsj[2]==1 || pdsj[2]==2 || pdsj[2]==4 || pdsj[2]==5 || pdsj[2]==16 || pdsj[2]==17 || pdsj[5]==0 || pdsj[5]==1 || pdsj[5]==2 || pdsj[5]==6 || pdsj[5]==8)
            {
                hexString = DataTranslate.bytesToHexString(bytes);
                log.info("收到的数据: " + hexString);
            }
            else{
                hexString = DataTranslate.getASCII(bytes);//new
                log.info("收到的数据: " + hexString);
            }


            Elevator elevator = new Elevator();
            TowerCrane towerCrane = new TowerCrane();
            Dust dust = new Dust();

            if (elevator.isElevatorData(hexString)) {
                log.debug("升降机数据");
                elevator.parseElevatorData(hexString);
                byte[] elevatorsssj = DataTranslate.hexStringToBytes(hexString);
                if(elevatorsssj[2] == 16)
                {
                    eSSSJ=16;
                }
                return Response.OK;
            } else if (towerCrane.isTowerCraneData(hexString)) {
                log.debug("塔机数据");
                towerCrane.parseTowerCraneData(hexString);
                byte[] towersssj = DataTranslate.hexStringToBytes(hexString);
                if(towersssj[5] == 6)
                {
                    tSSSJ=6;
                }
                return Response.OK;
            } else if (dust.isDustData(dustdata)) {
                log.debug("扬尘数据");
                dust.parseDustData(dustdata);
                return Response.OK;
            } else {
                log.info("未识别的数据");
                return Response.UNRECOGNIZED;
            }
        }
        return Response.ERROR;
    }
}
