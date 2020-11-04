package com.dajiang.platform.protocal;

import com.dajiang.platform.domain.*;

import com.dajiang.platform.repository.*;

import com.dajiang.platform.utils.DataTranslate;
import com.dajiang.platform.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Arc2D;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 塔机黑匣子协议解析
 */
@Slf4j
public class TowerCrane {

    // 根据接口文档编制如下协议字段
    private final String TOWERCRANE_HEAD = "A5AA";                      /* 头部 */
    private final String TOWERCRANE_TAIL = "CCC3C33C";                  /* 尾部*/
    private final Integer TOWERCRANE_DATA_LENGTH = 2;                   /* 长度字段字节数 */
    private final Integer TOWERCRANE_DATA_CHECKSUM = 2;                 /* 校验字段字节数 */
    private final String TOWERCRANE_COMMAND = "99";                     /* 自定义 */
    private final String TOWERCRANE_COMMAND_REGISTER = "00";            /* 设备注册信息上传 */
    private final String TOWERCRANE_COMMAND_REGISTER_DOWN = "01";       /* 后台注册信息下传 */
    private final String TOWERCRANE_COMMAND_ATTRIBUTE = "02";           /* 设备属性信息上传 */
    private final String TOWERCRANE_COMMAND_ANSWER_1 = "03";            /* 后台应答信息下传 */
    private final String TOWERCRANE_COMMAND_HEARTBEAT = "04";           /* 设备心跳包上传 */
    private final String TOWERCRANE_COMMAND_ANSWER_2 = "05";            /* 后台应答信息下传 */
    private final String TOWERCRANE_COMMAND_REAL_DATA = "06";           /* 设备实时数据上传 */
    private final String TOWERCRANE_COMMAND_ANSWER_3 = "07";            /* 后台应答信息下传 */
    private final String TOWERCRANE_COMMAND_LOOP = "08";                /* 设备工作循环数据上传 */
    private final String TOWERCRANE_COMMAND_ANSWER_4 = "09";            /* 后台应答信息下传 */

    /**
     * 判断接收到的数据是否是塔机数据
     * @param hexString
     * @return
     */
    public boolean isTowerCraneData(String hexString) {
        // 实际上是 "A5AA\\S+CCC3C33C\\S+$" 这样一个正则表达式
        String pattern = MessageFormat.format("{0}\\S+{2}\\S+$", TOWERCRANE_HEAD, TOWERCRANE_TAIL);
        return hexString.matches(pattern);
    }

    /**
     * 解析塔机数据. 只解析一条数据，结束位后面的字符串全部丢弃
     * @param hexString
     */
    public void parseTowerCraneData(String hexString){
        try{
            // 实际上是 "(A5AA\\S+CCC3C33C)(.*)" 这样一个正则表达式
            String pattern = MessageFormat.format("({0}\\S+{1})(.*)", TOWERCRANE_HEAD, TOWERCRANE_TAIL);

            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);

            // 创建 matcher 对象
            Matcher m = r.matcher(hexString);
            if (m.find()) {
                String towercraneData = m.group(1);
                log.info("收到塔机数据：" + towercraneData);

                // 去头去尾
                String data = towercraneData.substring(TOWERCRANE_HEAD.length());
                data = data.substring(0, data.length()-TOWERCRANE_TAIL.length());

                // 取出命令字段
                String command = data.substring(6, 8);
               // data = data.substring(TOWERCRANE_COMMAND.length());

                // 取出长度与校验和data.length()-TOWERCRANE_DATA_LENGTH
                String length = data.substring(0,2);
                String checksum = data.substring(data.length()-2);
                data = data.substring(2, 6) + data.substring(8, data.length()-TOWERCRANE_DATA_CHECKSUM);

                // 纯数据，也就是需要解析的数据
                log.info("pure data: " + data);

                if (!isDataValid(data, length, checksum)) {
                    return;
                }

                if (command.equals(TOWERCRANE_COMMAND_REGISTER)) {
                    parseRegisterData(data);
                } else if (command.equals(TOWERCRANE_COMMAND_REGISTER_DOWN)) {
                    parseRegisterDownData(data);
                } else if (command.equals(TOWERCRANE_COMMAND_ATTRIBUTE)) {
                    parseAttributeData(data);
                } else if (command.equals(TOWERCRANE_COMMAND_ANSWER_1)) {
                    parseAnswer1Data(data);
                } else if (command.equals(TOWERCRANE_COMMAND_HEARTBEAT)) {
                    parseHeartbeatData(data);
                } else if (command.equals(TOWERCRANE_COMMAND_ANSWER_2)) {
                    parseAnswer2Data(data);
                } else if (command.equals(TOWERCRANE_COMMAND_REAL_DATA)) {
                    parseRealData(data);
                } else if (command.equals(TOWERCRANE_COMMAND_ANSWER_3)) {
                    parseAnswer3Data(data);
                }else if (command.equals(TOWERCRANE_COMMAND_LOOP)) {
                    parseLoopData(data);
                }else if (command.equals(TOWERCRANE_COMMAND_ANSWER_4)) {
                    parseAnswer4Data(data);
                } else {
                    log.error("未识别的消息：" + hexString);
                }

            } else {
                log.error("塔机数据解析错误：" + hexString);
            }

        } catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 判断数据是否合规
     * @param data      纯数据
     * @param length    长度
     * @param checksum  校验和
     * @return
     */
    public boolean isDataValid(String data, String length, String checksum) {
        // 长度检验
        Integer iLength = Integer.valueOf(length, 16);
        if (iLength*2 != data.length() + TOWERCRANE_COMMAND.length() + TOWERCRANE_DATA_LENGTH + TOWERCRANE_DATA_CHECKSUM) {
            log.error("data length error.");
            return false;
        }

        // 校验和检验
        Integer iChecksum = Integer.valueOf(checksum, 16);
        byte[] dataBytes = DataTranslate.hexStringToBytes(data);
        Integer sum = 0;
        for (int i=0; i<dataBytes.length; i++) {
            sum += dataBytes[i];
        }

        if ((sum-iChecksum)%256 != 0) {
            log.error("data checksum error.");
            return false;
        }

        return true;
    }

    /**
     * 解析设备注册信息上传0x00
     * 测试数据A5AA0922220011223344EECCC3C33C
     * @param hexString
     */
    public void parseRegisterData(String hexString) {
        try {
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            //厂家编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes,0,1);
            Integer factoryNum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("factoryNum:" + factoryNum);
            //协议版本
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes,1,2);
            Integer proVersion = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("proVersion:" + proVersion);
            //设备编号
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes,2,6);
            Integer deviceSerial = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            log.info("deviceSerial:" + deviceSerial);

            TowerRegUp towerRegUp = new TowerRegUp();
            towerRegUp.setFactoryNum(factoryNum);
            towerRegUp.setProVersion(proVersion);
            towerRegUp.setDeviceSerial(deviceSerial);

            // 多线程中无法自动注入Repository，手动从容器中获取
            TowerRegUpRepository towerRegUpRepository = SpringUtil.getBean(TowerRegUpRepository.class);
            towerRegUpRepository.save(towerRegUp);


        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析后台注册信息下传0x01
     * 测试数据 A5AA1022220111223344200913010101224FCCC3C33C
     * @param hexString
     */
    public void parseRegisterDownData(String hexString) {
        try {
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            //厂家编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes,0,1);
            Integer factoryNum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("factoryNum:" + factoryNum);

            //协议版本
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes,1,2);
            Integer proVersion = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("proVersion:" + proVersion);

            //设备编号
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes,2,6);
            Integer deviceSerial = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            log.info("deviceSerial:" + deviceSerial);

            //时间
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 6, 7);//年
            Integer time_n1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            String time_n = time_n1.toString();
            if(time_n1<10)
            {
                time_n="0" + time_n;
            }
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 7, 8);//月
            Integer time_y1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            String time_y = time_y1.toString();
            if(time_y1<10)
            {
                time_y="0" + time_y;
            }
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 8, 9);//日
            Integer time_r1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            String time_r = time_r1.toString();
            if(time_r1<10)
            {
                time_r="0" + time_r;
            }
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 9, 10);//时
            Integer time_s1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            String time_s = time_s1.toString();
            if(time_s1<10)
            {
                time_s="0" + time_s;
            }
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 10, 11);//分
            Integer time_f1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            String time_f = time_f1.toString();
            if(time_f1<10)
            {
                time_f="0" + time_f;
            }
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 11, 12);//秒
            Integer time_m1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            String time_m = time_m1.toString();
            if(time_m1<10)
            {
                time_m="0" + time_m;
            }
            Timestamp ti_me=Timestamp.valueOf("20" + time_n + "-" + time_y + "-" + time_r + " " + time_s + ":" + time_f + ":" + time_m);
            log.info("ti_me: " + ti_me.toString());
            //上传周期
            byte[] copyBytes9 = Arrays.copyOfRange(dataBytes,12,13);
            Integer cycleUp = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes9),16);
            log.info("cycleUp:" + cycleUp);

            TowerRegDown towerRegDown = new TowerRegDown();
            towerRegDown.setFactoryNum(factoryNum);
            towerRegDown.setProVersion(proVersion);
            towerRegDown.setDeviceSerial(deviceSerial);
            towerRegDown.setTime(ti_me);
            towerRegDown.setCycleUp(cycleUp);

            // 多线程中无法自动注入Repository，手动从容器中获取
            TowerRegDownRepository towerRegDownRepository = SpringUtil.getBean(TowerRegDownRepository.class);
            towerRegDownRepository.save(towerRegDown);
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析设备属性信息上传0x02
     * 测试数据A5AA5F22220211223344010233334444555566667777888899111122223333444455556666777788889999111122223333444455556666777788889999111122223333444455556666777788889999111122223333444455556666777701020398CCC3C33C
     * @param hexString
     */
    public void parseAttributeData(String hexString) {
        try {
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            //厂家编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes,0,1);
            Integer factoryNum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("factoryNum:" + factoryNum);

            //协议版本
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes,1,2);
            Integer proVersion = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("proVersion:" + proVersion);

            //设备编号
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes,2,6);
            Integer deviceSerial = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            log.info("deviceSerial:" + deviceSerial);

            //塔吊编号
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes,6,7);
            Integer towerNumber = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            log.info("towerNumber:" + towerNumber);

            //力矩曲线
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes,7,8);
            Integer towerCurve = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            log.info("towerCurve:" + towerCurve);

            //坐标X
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 8, 10);
            Float towerX = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes5));
            log.info("towerX:" + towerX.toString());

            //坐标Y
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 10, 12);
            Float towerY = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes6));
            log.info("towerY:" + towerY.toString());

            //起重臂长
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 12, 14);
            Float towerBoomlen = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes7));
            log.info("towerBoomlen:" + towerBoomlen.toString());

            //平衡臂长
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 14, 16);
            Float towerBalancelen = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes8));
            log.info("towerBalancelen:" + towerBalancelen.toString());

            //塔帽高
            byte[] copyBytes9 = Arrays.copyOfRange(dataBytes, 16, 18);
            Float towerCaphei = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes9));
            log.info("towerCaphei:" + towerCaphei.toString());

            //起重臂高
            byte[] copyBytes10 = Arrays.copyOfRange(dataBytes, 18, 20);
            Float towerBoomhei = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes10));
            log.info("towerBoomhei:" + towerBoomhei.toString());

            //绳索倍率
            byte[] copyBytes11 = Arrays.copyOfRange(dataBytes,20,21);
            Integer towerRope = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes11),16);
            log.info("towerRope:" + towerRope);

            //高度标定
            byte[] copyBytes12 = Arrays.copyOfRange(dataBytes, 21, 23);
            Integer towerheiad1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes12),16);
            log.info("towerheiad1:" + towerheiad1.toString());

            //高度标定X1
            byte[] copyBytes13 = Arrays.copyOfRange(dataBytes, 23, 25);
            Float towerHeix1 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes13));
            log.info("towerHeix1:" + towerHeix1.toString());

            //高度标定AD2
            byte[] copyBytes14 = Arrays.copyOfRange(dataBytes, 25, 27);
            Integer towerHeiad2 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes14),16);
            log.info("towerHeiad2:" + towerHeiad2.toString());

            //高度标定 X2
            byte[] copyBytes15 = Arrays.copyOfRange(dataBytes, 27, 29);
            Float towerHeix2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes15));
            log.info("towerHeix2:" + towerHeix2.toString());

            //幅度标定AD1
            byte[] copyBytes16 = Arrays.copyOfRange(dataBytes, 29, 31);
            Integer towerRangead1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes16),16);
            log.info("towerRangead1:" + towerRangead1.toString());

            //幅度标定X1
            byte[] copyBytes17 = Arrays.copyOfRange(dataBytes, 31, 33);
            Float towerRangex1 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes17));
            log.info("towerRangex1:" + towerRangex1.toString());

            //幅度标定AD2
            byte[] copyBytes18 = Arrays.copyOfRange(dataBytes, 33, 35);
            Integer towerRangead2 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes18),16);
            log.info("towerRangead2:" + towerRangead2.toString());

            //幅度标定X2
            byte[] copyBytes19 = Arrays.copyOfRange(dataBytes, 35, 37);
            Float towerRangex2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes19));
            log.info("towerRangex2:" + towerRangex2.toString());

            //回转标定AD1
            byte[] copyBytes20 = Arrays.copyOfRange(dataBytes, 37, 39);
            Integer towerTurnad1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes20),16);
            log.info("towerTurnad1:" + towerTurnad1.toString());

            //回转标定X1
            byte[] copyBytes21 = Arrays.copyOfRange(dataBytes, 39, 41);
            Float towerTurnx1 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes21));
            log.info("towerRangex2:" + towerTurnx1.toString());

            //回转标定AD2
            byte[] copyBytes22 = Arrays.copyOfRange(dataBytes, 41, 43);
            Integer towerTurnad2 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes22),16);
            log.info("towerTurnad2:" + towerTurnad2.toString());

            //回转标定X2
            byte[] copyBytes23 = Arrays.copyOfRange(dataBytes, 43, 45);
            Float towerTurnx2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes23));
            log.info("towerTurnx2:" + towerTurnx2.toString());

            //重量标定AD1  2byte    towerWeid1
            byte[] copyBytes24 = Arrays.copyOfRange(dataBytes, 45, 47);
            Integer towerWeid1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes24),16);
            log.info("towerWeid1:" + towerWeid1.toString());

            //重量标定X1
            byte[] copyBytes25 = Arrays.copyOfRange(dataBytes, 47, 49);
            Float towerWeix1 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes25));
            log.info("towerWeix1:" + towerWeix1.toString());

            //重量标定AD2
            byte[] copyBytes26 = Arrays.copyOfRange(dataBytes, 49, 51);
            Integer towerWeid2 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes26),16);
            log.info("towerWeid2:" + towerWeid2.toString());

            //重量标定X2
            byte[] copyBytes27 = Arrays.copyOfRange(dataBytes, 51, 53);
            Float towerWeix2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes27));
            log.info("towerWeix2:" + towerWeix2.toString());

            //风速标定校准值
            byte[] copyBytes28 = Arrays.copyOfRange(dataBytes, 53, 55);
            Integer windCalibration = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes28),16);
            log.info("windCalibration:" + windCalibration.toString());

            //倾斜标定校准值
            byte[] copyBytes29 = Arrays.copyOfRange(dataBytes, 55, 57);
            Integer tiltCalibration = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes29),16);
            log.info("tiltCalibration:" + tiltCalibration.toString());

            //高度起点限位
            byte[] copyBytes30 = Arrays.copyOfRange(dataBytes, 57, 59);
            Float heightStart = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes30));
            log.info("heightStart:" + heightStart.toString());

            //高度终点限位
            byte[] copyBytes31 = Arrays.copyOfRange(dataBytes, 59, 61);
            Float heightEnd = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes31));
            log.info("heightEnd:" + heightEnd.toString());

            //幅度起点限位
            byte[] copyBytes32 = Arrays.copyOfRange(dataBytes, 61, 63);
            Float rangeStart = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes32));
            log.info("rangeStart:" + rangeStart.toString());

            //幅度终点限位
            byte[] copyBytes33 = Arrays.copyOfRange(dataBytes, 63, 65);
            Float rangeEnd = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes33));
            log.info("rangeEnd:" + rangeEnd.toString());

            //回转左限位
            byte[] copyBytes34 = Arrays.copyOfRange(dataBytes, 65, 67);
            Float trunLf = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes34));
            log.info("trunLf:" + trunLf.toString());

            //回转右限位
            byte[] copyBytes35 = Arrays.copyOfRange(dataBytes, 67, 69);
            Float trunRi = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes35));
            log.info("trunRi:" + trunRi.toString());

            //水平报警距离
            byte[] copyBytes36 = Arrays.copyOfRange(dataBytes, 69, 71);
            Float warningHor = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes36));
            log.info("warningHor:" + warningHor.toString());

            //垂直报警距离
            byte[] copyBytes37 = Arrays.copyOfRange(dataBytes, 71, 73);
            Float warningVer = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes37));
            log.info("warningVer:" + warningVer.toString());

            //重量报警百分比
            byte[] copyBytes38 = Arrays.copyOfRange(dataBytes, 73, 75);
            Float warningHei = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes38));
            log.info("warningHei:" + warningHei.toString());

            //风速报警值
            byte[] copyBytes39 = Arrays.copyOfRange(dataBytes, 75, 77);
            Float warningWin = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes39));
            log.info("warningWin:" + warningWin.toString());

            //倾斜报警值
            byte[] copyBytes40 = Arrays.copyOfRange(dataBytes, 77, 79);
            Float warningTil = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes40));
            log.info("warningTil:" + warningTil.toString());

            //水平预警距离
            byte[] copyBytes41 = Arrays.copyOfRange(dataBytes, 79, 81);
            Float earlywarningHor = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes41));
            log.info("earlywarningHor:" + earlywarningHor.toString());

            //垂直预警距离
            byte[] copyBytes42 = Arrays.copyOfRange(dataBytes, 81, 83);
            Float earlywarningVer = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes42));
            log.info("earlywarningVer:" + earlywarningVer.toString());

            //重量预警百分比
            byte[] copyBytes43 = Arrays.copyOfRange(dataBytes, 83, 85);
            Float earlywarningHer = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes43));
            log.info("earlywarningHer:" + earlywarningHer.toString());

            //风速预警值
            byte[] copyBytes44 = Arrays.copyOfRange(dataBytes, 85, 87);
            Float earlywarningWin = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes44));
            log.info("earlywarningWin:" + earlywarningWin.toString());

            //倾斜预警值
            byte[] copyBytes45 = Arrays.copyOfRange(dataBytes, 87, 89);
            Float earlywarningTil = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes45));
            log.info("earlywarningTil:" + earlywarningTil.toString());

            //碰撞制动允许
            byte[] copyBytes46 = Arrays.copyOfRange(dataBytes, 89, 90);
            Integer collisionJudge = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes46),16);
            log.info("collisionJudge:" + collisionJudge.toString());

            //身份认证是否启动
            byte[] copyBytes47 = Arrays.copyOfRange(dataBytes, 90, 91);
            Integer idJudge = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes47),16);
            log.info("idJudge:" + idJudge.toString());

            //GPRS锁车
            byte[] copyBytes48 = Arrays.copyOfRange(dataBytes, 91, 92);
            Integer gprsJudge = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes48),16);
            log.info("gprsJudge:" + gprsJudge.toString());

            TowerAttribute towerAttribute = new TowerAttribute();
            towerAttribute.setFactoryNum(factoryNum);
            towerAttribute.setProVersion(proVersion);
            towerAttribute.setDeviceSerial(deviceSerial);
            towerAttribute.setTowerNumber(towerNumber);
            towerAttribute.setTowerCurve(towerCurve);
            towerAttribute.setTowerX(towerX);
            towerAttribute.setTowerY(towerY);
            towerAttribute.setTowerBoomlen(towerBoomlen);
            towerAttribute.setTowerBalancelen(towerBalancelen);
            towerAttribute.setTowerCaphei(towerCaphei);
            towerAttribute.setTowerBoomhei(towerBoomhei);
            towerAttribute.setTowerRope(towerRope);
            towerAttribute.setTowerHeiad1(towerheiad1);
            towerAttribute.setTowerHeix1(towerHeix1);
            towerAttribute.setTowerHeiad2(towerHeiad2);
            towerAttribute.setTowerHeix2(towerHeix2);
            towerAttribute.setTowerRangead1(towerRangead1);
            towerAttribute.setTowerRangex1(towerRangex1);
            towerAttribute.setTowerRangead2(towerRangead2);
            towerAttribute.setTowerRangex2(towerRangex2);
            towerAttribute.setTowerTurnad1(towerTurnad1);
            towerAttribute.setTowerTurnx1(towerTurnx1);
            towerAttribute.setTowerTurnad2(towerTurnad2);
            towerAttribute.setTowerTurnx2(towerTurnx2);
            towerAttribute.setTowerWeid1(towerWeid1);
            towerAttribute.setTowerWeix1(towerWeix1);
            towerAttribute.setTowerWeid2(towerWeid2);
            towerAttribute.setTowerWeix2(towerWeix2);
            towerAttribute.setWindCalibration(windCalibration);
            towerAttribute.setTiltCalibration(tiltCalibration);
            towerAttribute.setHeightStart(heightStart);
            towerAttribute.setHeightEnd(heightEnd);
            towerAttribute.setRangeStart(rangeStart);
            towerAttribute.setRangeEnd(rangeEnd);
            towerAttribute.setTrunLf(trunLf);
            towerAttribute.setTrunRi(trunRi);
            towerAttribute.setWarningHor(warningHor);
            towerAttribute.setWarningVer(warningVer);
            towerAttribute.setWarningHei(warningHei);
            towerAttribute.setWarningWin(warningWin);
            towerAttribute.setWarningTil(warningTil);
            towerAttribute.setEarlywarningHor(earlywarningHor);
            towerAttribute.setEarlywarningVer(earlywarningVer);
            towerAttribute.setEarlywarningHer(earlywarningHer);
            towerAttribute.setEarlywarningWin(earlywarningWin);
            towerAttribute.setEarlywarningTil(earlywarningTil);
            towerAttribute.setCollisionJudge(collisionJudge);
            towerAttribute.setIdJudge(idJudge);
            towerAttribute.setGprsJudge(gprsJudge);

            // 多线程中无法自动注入Repository，手动从容器中获取
            TowerAttributeRepository towerAttributeRepository = SpringUtil.getBean(TowerAttributeRepository.class);
            towerAttributeRepository.save(towerAttribute);

        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析后台应答信息下传1
     * 未定义
     * @param hexString
     */
    public void parseAnswer1Data(String hexString) {
        try {
            log.info("未解析！");
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析设备心跳包上传
     * 未定义
     * @param hexString
     */
    public void parseHeartbeatData(String hexString) {
        try {
            log.info("未解析！");
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析后台应答信息下传2
     * 未定义
     * @param hexString
     */
    public void parseAnswer2Data(String hexString) {
        try {
            log.info("未解析！");
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析设备实时数据上传0x06
     * 测试数据:A5AA2B22220611223344200913052013111122223333444455556677778888991122334455667788991101228ECCC3C33C
     * @param hexString
     */
    public void parseRealData(String hexString) {
        try {
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            //厂家编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes,0,1);
            Integer factoryNum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("factoryNum:" + factoryNum);

            //协议版本
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes,1,2);
            Integer proVersion = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("proVersion:" + proVersion);

            //设备编号
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes,2,6);
            Integer deviceSerial = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            log.info("deviceSerial:" + deviceSerial);

            //时间
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 6, 7);//年
            Integer time_n1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            String time_n = time_n1.toString();
            if(time_n1<10)
            {
                time_n="0" + time_n;
            }
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 7, 8);//月
            Integer time_y1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            String time_y = time_y1.toString();
            if(time_y1<10)
            {
                time_y="0" + time_y;
            }
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 8, 9);//日
            Integer time_r1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            String time_r = time_r1.toString();
            if(time_r1<10)
            {
                time_r="0" + time_r;
            }
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 9, 10);//时
            Integer time_s1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            String time_s = time_s1.toString();
            if(time_s1<10)
            {
                time_s="0" + time_s;
            }
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 10, 11);//分
            Integer time_f1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            String time_f = time_f1.toString();
            if(time_f1<10)
            {
                time_f="0" + time_f;
            }
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 11, 12);//秒
            Integer time_m1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            String time_m = time_m1.toString();
            if(time_m1<10)
            {
                time_m="0" + time_m;
            }
            Timestamp ti_me=Timestamp.valueOf("20" + time_n + "-" + time_y + "-" + time_r + " " + time_s + ":" + time_f + ":" + time_m);
            log.info("ti_me: " + ti_me.toString());

            //高度
            byte[] copyBytes9 = Arrays.copyOfRange(dataBytes,12,14);
            Float Height = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes9));
            log.info("Height:" + Height);

            //幅度
            byte[] copyBytes10 = Arrays.copyOfRange(dataBytes,14,16);
            Float Range = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes10));
            log.info("Range:" + Range);

            //回转
            byte[] copyBytes11 = Arrays.copyOfRange(dataBytes,16,18);
            Float Turn = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes11));
            log.info("Turn:" + Turn);

            //载重
            byte[] copyBytes12 = Arrays.copyOfRange(dataBytes,18,20);
            Float Load = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes12));
            log.info("Load:" + Load);

            //当前允许载重
            byte[] copyBytes13 = Arrays.copyOfRange(dataBytes,20,22);
            Float loadJudge = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes13));
            log.info("loadJudge：" + loadJudge);

            //载重百分比
            byte[] copyBytes14 = Arrays.copyOfRange(dataBytes,22,23);
            Float loadPer = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes14));
            log.info("loadPer：" + loadPer);

            //风速
            byte[] copyBytes15 = Arrays.copyOfRange(dataBytes,23,25);
            Float Wind = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes15));
            log.info("Wind：" + Wind);

            //倾斜
            byte[] copyBytes16 = Arrays.copyOfRange(dataBytes,25,27);
            Float Tilt = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes16));
            log.info("Tilt：" + Tilt);

            //高度限位值
            Integer heightSpa16 = (dataBytes[27] & 0x0F);
            Integer heightSpa = Integer.parseInt(heightSpa16.toString(),16);
            log.info("heightSpa：" + heightSpa);

            //幅度回转限位
            //远近
            Integer  rangeSpayj16 = (dataBytes[28] & 0x0F);
            Integer rangeSpayj = Integer.parseInt(rangeSpayj16.toString(),16);
            //左右
            Integer rangeSpazy16 = (dataBytes[28] & 0xF0);
            Integer rangeSpazy = Integer.parseInt(rangeSpazy16.toString(),16);
            log.info(" rangeSpayj:" +  rangeSpayj + " ; rangeSpazy：" + rangeSpazy);

            //载重限制值
            byte[] copyBytes18 = Arrays.copyOfRange(dataBytes,29,30);
            Integer loadSpa = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes18),16);
            log.info("loadSpa:" + loadSpa);

            //倾斜风速限制值
            //风速
            Integer tiltwindSpawind16 = (dataBytes[30] & 0x0F);
            Integer tiltwindSpawind = Integer.parseInt(tiltwindSpawind16.toString(),16);
            //倾斜
            Integer tiltwindSpatilt16 = (dataBytes[30] & 0xF0);
            Integer tiltwindSpatilt = Integer.parseInt(tiltwindSpatilt16.toString(),16);
            log.info("tiltwindSpawind:" + tiltwindSpawind + " ; tiltwindSpatilt:" + tiltwindSpatilt);

            //干涉上下限位
            //上
            Integer interferenceSxs16 = (dataBytes[31] & 0x0F);
            Integer interferenceSxs = Integer.parseInt(interferenceSxs16.toString(),16);
            //下
            Integer interferenceSxx16 = (dataBytes[31] & 0xF0);
            Integer interferenceSxx = Integer.parseInt(interferenceSxx16.toString(),16);
            log.info("interferenceSxs:" + interferenceSxs + " ; interferenceSxx:" + interferenceSxx);

            //干涉前后限位
            //前
            Integer interferenceQhq16 = (dataBytes[32] & 0x0F);
            Integer interferenceQhq = Integer.parseInt(interferenceQhq16.toString(),16);
            //后
            Integer interferenceQhh16 = (dataBytes[32] & 0xF0);
            Integer interferenceQhh = Integer.parseInt(interferenceQhh16.toString(),16);
            log.info("interferenceQhq:" + interferenceQhq + " ; interferenceQhh:" + interferenceQhh);

            //干涉左右限位
            //左
            Integer interferenceZyz16 = (dataBytes[33] & 0x0F);
            Integer interferenceZyz = Integer.parseInt(interferenceZyz16.toString(),16);
            //右
            Integer interferenceZyy16 = (dataBytes[33] & 0xF0);
            Integer interferenceZyy = Integer.parseInt(interferenceZyy16.toString(),16);
            log.info("interferenceZyz:" + interferenceZyz + " ; interferenceZyy:" + interferenceZyy);

            //碰撞上下限位
            //上
            Integer collisionSxs16 = (dataBytes[34] & 0x0F);
            Integer collisionSxs = Integer.parseInt(collisionSxs16.toString(),16);
            //下
            Integer collisionSxx16  = (dataBytes[34] & 0xF0);
            Integer collisionSxx = Integer.parseInt(collisionSxx16.toString(),16);
            log.info("collisionSxs:" + collisionSxs + " ; collisionSxx :" + collisionSxx );

            //碰撞前后限位
            //前
            Integer collisionQhq16 = (dataBytes[35] & 0x0F);
            Integer collisionQhq = Integer.parseInt(collisionQhq16.toString(),16);
            //后
            Integer collisionQhh16 = (dataBytes[35] & 0xF0);
            Integer collisionQhh = Integer.parseInt(collisionQhh16.toString(),16);
            log.info("collisionQhq:" + collisionQhq + " ; collisionQhh:" + collisionQhh);

            //碰撞左右限位
            //左
            Integer collisionZyz16 = (dataBytes[36] & 0x0F);
            Integer collisionZyz = Integer.parseInt(collisionZyz16.toString(),16);
            //右
            Integer collisionZyy16 = (dataBytes[36] & 0xF0);
            Integer collisionZyy = Integer.parseInt(collisionZyy16.toString(),16);
            log.info("collisionZyz:" + collisionZyz + " ; collisionZyy:" + collisionZyy);

            //继电器状态
            //上行
            Integer relayState016 = (dataBytes[37] & 0x01);
            Integer relayState0 = Integer.parseInt(relayState016.toString(),16);
            //下行
            Integer relayState116 = (dataBytes[37] & 0x02);
            Integer relayState1 = Integer.parseInt(relayState116.toString(),16);
            //前行
            Integer relayState216 = (dataBytes[37] & 0x04);
            Integer relayState2 = Integer.parseInt(relayState216.toString(),16);
            //后行
            Integer relayState316 = (dataBytes[37] & 0x08);
            Integer relayState3 = Integer.parseInt(relayState316.toString(),16);
            //左行
            Integer relayState416 = (dataBytes[37] & 0x10);
            Integer relayState4 = Integer.parseInt(relayState416.toString(),16);
            //右行
            Integer relayState516 = (dataBytes[37] & 0x20);
            Integer relayState5 = Integer.parseInt(relayState516.toString(),16);
            log.info("relayState0:" + relayState0 + " ; relayState1:" + relayState1 + " ; relayState2:" + relayState2);
            log.info("relayState3:" + relayState3 + " ; relayState4:" + relayState4 + " ; relayState5:" + relayState5);

            //工作状态
            byte[] copyBytes19 = Arrays.copyOfRange(dataBytes,38,39);
            Integer workState = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes19),16);
            log.info("workState:" + workState);

            //传感器连接状态
            //高度
            Integer sensorState016 = (dataBytes[39] & 0x01);
            Integer sensorState0 = Integer.parseInt(sensorState016.toString(),16);
            //幅度
            Integer sensorState116 = (dataBytes[39] & 0x02);
            Integer sensorState1 = Integer.parseInt(sensorState116.toString(),16);
            //回转
            Integer sensorState216 = (dataBytes[39] & 0x04);
            Integer sensorState2 = Integer.parseInt(sensorState216.toString(),16);
            //重量
            Integer sensorState316 = (dataBytes[39] & 0x08);
            Integer sensorState3 = Integer.parseInt(sensorState316.toString(),16);
            //风速
            Integer sensorState416 = (dataBytes[39] & 0x10);
            Integer sensorState4 = Integer.parseInt(sensorState416.toString(),16);
            //倾斜
            Integer sensorState516 = (dataBytes[39] & 0x20);
            Integer sensorState5 = Integer.parseInt(sensorState516.toString(),16);
            log.info("sensorState0:" + sensorState0 + " ; sensorState1:" + sensorState1 + "; sensorState2:" + sensorState2);
            log.info("sensorState3:" + sensorState3 + " ; sensorState4:" + sensorState4 + "; sensorState5:" + sensorState5);

            TowerReal towerReal = new TowerReal();
            towerReal.setFactoryNum(factoryNum);
            towerReal.setProVersion(proVersion);
            towerReal.setDeviceSerial(deviceSerial);
            towerReal.setTime(ti_me);
            towerReal.setHeight(Height);
            towerReal.setRange(Range);
            towerReal.setTurn(Turn);
            towerReal.setLoad(Load);
            towerReal.setLoadJudge(loadJudge);
            towerReal.setLoadPer(loadPer);
            towerReal.setWind(Wind);
            towerReal.setTilt(Tilt);
            towerReal.setHeightSpa(heightSpa);
            towerReal.setRangeSpayj(rangeSpayj);
            towerReal.setRangeSpazy(rangeSpazy);
            towerReal.setLoadSpa(loadSpa);
            towerReal.setTiltwindSpawind(tiltwindSpawind);
            towerReal.setTiltwindSpatilt(tiltwindSpatilt);
            towerReal.setInterferenceQhq(interferenceQhq);
            towerReal.setInterferenceQhh(interferenceQhh);
            towerReal.setInterferenceZyz(interferenceZyz);
            towerReal.setInterferenceZyy(interferenceZyy);
            towerReal.setInterferenceSxs(interferenceSxs);
            towerReal.setInterferenceSxx(interferenceSxx);
            towerReal.setCollisionSxs(collisionSxs);
            towerReal.setCollisionSxx(collisionSxx);
            towerReal.setCollisionQhq(collisionQhq);
            towerReal.setCollisionQhh(collisionQhh);
            towerReal.setCollisionZyz(collisionZyz);
            towerReal.setCollisionZyy(collisionZyy);
            towerReal.setRelayState0(relayState0);
            towerReal.setRelayState1(relayState1);
            towerReal.setRelayState2(relayState2);
            towerReal.setRelayState3(relayState3);
            towerReal.setRelayState4(relayState4);
            towerReal.setRelayState5(relayState5);
            towerReal.setWorkState(workState);
            towerReal.setSensorState0(sensorState0);
            towerReal.setSensorState1(sensorState1);
            towerReal.setSensorState2(sensorState2);
            towerReal.setSensorState3(sensorState3);
            towerReal.setSensorState4(sensorState4);
            towerReal.setSensorState5(sensorState5);

            // 多线程中无法自动注入Repository，手动从容器中获取
            TowerRealRepository towerRealRepository = SpringUtil.getBean(TowerRealRepository.class);
            towerRealRepository.save(towerReal);

        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析后台应答信息下传3
     * 未定义
     * @param hexString
     */
    public void parseAnswer3Data(String hexString) {
        try {
            log.info("未解析！");
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析工作循环数据上传0x08
     * 测试数据:A5AA2B22220811223344180706050201180706050201222233334444180706050201222233334444111133012FCCC3C33C
     * @param hexString
     */
    public void parseLoopData(String hexString) {
        try {
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            //厂家编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes,0,1);
            Integer factoryNum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("factoryNum:" + factoryNum);

            //协议版本
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes,1,2);
            Integer proVersion = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("proVersion:" + proVersion);

            //设备编号
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes,2,6);
            Integer deviceSerial = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            log.info("deviceSerial:" + deviceSerial);

            //上传时间
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 6, 7);//年
            Integer time_n1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            String time_n = time_n1.toString();
            if(time_n1<10)
            {
                time_n="0" + time_n;
            }
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 7, 8);//月
            Integer time_y1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            String time_y = time_y1.toString();
            if(time_y1<10)
            {
                time_y="0" + time_y;
            }
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 8, 9);//日
            Integer time_r1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            String time_r = time_r1.toString();
            if(time_r1<10)
            {
                time_r="0" + time_r;
            }
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 9, 10);//时
            Integer time_s1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            String time_s = time_s1.toString();
            if(time_s1<10)
            {
                time_s="0" + time_s;
            }
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 10, 11);//分
            Integer time_f1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            String time_f = time_f1.toString();
            if(time_f1<10)
            {
                time_f="0" + time_f;
            }
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 11, 12);//秒
            Integer time_m1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            String time_m = time_m1.toString();
            if(time_m1<10)
            {
                time_m="0" + time_m;
            }
            Timestamp upload_time=Timestamp.valueOf("20" + time_n + "-" + time_y + "-" + time_r + " " + time_s + ":" + time_f + ":" + time_m);
            log.info("upload_time: " + upload_time.toString());
            //起吊时间
            byte[] copyBytes9 = Arrays.copyOfRange(dataBytes, 12, 13);//年
            Integer time_n1n1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes9),16);
            String time_n1n = time_n1n1.toString();
            if(time_n1n1<10)
            {
                time_n1n="0" + time_n1n;
            }
            byte[] copyBytes10 = Arrays.copyOfRange(dataBytes, 13, 14);//月
            Integer time_y1y1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes10),16);
            String time_y1y = time_y1y1.toString();
            if(time_y1y1<10)
            {
                time_y1y="0" + time_y1y;
            }
            byte[] copyBytes11 = Arrays.copyOfRange(dataBytes, 14, 15);//日
            Integer time_r1r1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes11),16);
            String time_r1r = time_r1r1.toString();
            if(time_r1r1<10)
            {
                time_r1r="0" + time_r1r;
            }
            byte[] copyBytes12 = Arrays.copyOfRange(dataBytes, 15, 16);//时
            Integer time_s1s1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes12),16);
            String time_s1s = time_s1s1.toString();
            if(time_s1s1<10)
            {
                time_s1s="0" + time_s1s;
            }
            byte[] copyBytes13 = Arrays.copyOfRange(dataBytes, 16, 17);//分
            Integer time_f1f1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes13),16);
            String time_f1f = time_f1f1.toString();
            if(time_f1f1<10)
            {
                time_f1f="0" + time_f1f;
            }
            byte[] copyBytes14 = Arrays.copyOfRange(dataBytes, 17, 18);//秒
            Integer time_m1m1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes14),16);
            String time_m1m = time_m1m1.toString();
            if(time_m1m1<10)
            {
                time_m1m="0" + time_m1m;
            }
            Timestamp liftingTime=Timestamp.valueOf("20" + time_n1n + "-" + time_y1y + "-" + time_r1r + " " + time_s1s + ":" + time_f1f + ":" + time_m1m);
            log.info("liftingTime: " + liftingTime.toString());

            //起吊点高度
            byte[] copyBytes15 = Arrays.copyOfRange(dataBytes,18,20);
            Float liftingHei = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes15));
            log.info("liftingHei:" + liftingHei);

            //起吊点幅度
            byte[] copyBytes16 = Arrays.copyOfRange(dataBytes,20,22);
            Float liftingRange = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes16));
            log.info("liftingRange:" + liftingRange);

            //起吊点回转
            byte[] copyBytes17 = Arrays.copyOfRange(dataBytes,22,24);
            Float liftingTurn = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes17));
            log.info("liftingTurn:" + liftingTurn);

            //起吊时间2
            byte[] copyBytes18 = Arrays.copyOfRange(dataBytes, 24, 25);//年
            Integer time_n2n1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes18),16);
            String time_n2n = time_n2n1.toString();
            if(time_n2n1<10)
            {
                time_n2n="0" + time_n2n;
            }
            byte[] copyBytes19 = Arrays.copyOfRange(dataBytes, 25, 26);//月
            Integer time_y2y1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes19),16);
            String time_y2y = time_y2y1.toString();
            if(time_y2y1<10)
            {
                time_y2y="0" + time_y2y;
            }
            byte[] copyBytes20 = Arrays.copyOfRange(dataBytes, 26, 27);//日
            Integer time_r2r1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes20),16);
            String time_r2r = time_r2r1.toString();
            if(time_r2r1<10)
            {
                time_r2r="0" + time_r2r;
            }
            byte[] copyBytes21 = Arrays.copyOfRange(dataBytes, 27, 28);//时
            Integer time_s2s1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes21),16);
            String time_s2s = time_s2s1.toString();
            if(time_s2s1<10)
            {
                time_s2s="0" + time_s2s;
            }
            byte[] copyBytes22 = Arrays.copyOfRange(dataBytes, 28, 29);//分
            Integer time_f2f1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes22),16);
            String time_f2f = time_f2f1.toString();
            if(time_f2f1<10)
            {
                time_f2f="0" + time_f2f;
            }
            byte[] copyBytes23 = Arrays.copyOfRange(dataBytes, 29, 30);//秒
            Integer time_m2m1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes23),16);
            String time_m2m = time_m2m1.toString();
            if(time_m2m1<10)
            {
                time_m2m="0" + time_m2m;
            }
            Timestamp liftingTime2=Timestamp.valueOf("20" + time_n2n + "-" + time_y2y + "-" + time_r2r + " " + time_s2s + ":" + time_f2f + ":" + time_m2m);
            log.info("liftingTime2: " + liftingTime2.toString());

            //起吊点高度2
            byte[] copyBytes24 = Arrays.copyOfRange(dataBytes,30,32);
            Float liftingHei2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes24));
            log.info("liftingHei2:" + liftingHei2);

            //起吊点幅度2
            byte[] copyBytes25 = Arrays.copyOfRange(dataBytes,32,34);
            Float liftingRange2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes25));
            log.info("liftingRange2:" + liftingRange2);

            //起吊点回转 2
            byte[] copyBytes26 = Arrays.copyOfRange(dataBytes,34,36);
            Float liftingTurn2 = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes26));
            log.info("liftingTurn2:" + liftingTurn2);

            //最大吊重
            byte[] copyBytes27 = Arrays.copyOfRange(dataBytes,36,38);
            Float liftingWei = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes27));
            log.info("liftingWei:" + liftingWei);

            //最大负荷
            byte[] copyBytes28 = Arrays.copyOfRange(dataBytes,38,39);
            Float liftingLoad = Float.parseFloat(DataTranslate.bytesToHexString(copyBytes28));
            log.info("liftingLoad:" + liftingLoad);

            //是否违章
            byte[] copyBytes29 = Arrays.copyOfRange(dataBytes,39,40);
            Integer violationReg = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes29),16);
            log.info("violationReg:" + violationReg);

            TowerLoop towerLoop = new TowerLoop();
            towerLoop.setFactoryNum(factoryNum);
            towerLoop.setProVersion(proVersion);
            towerLoop.setDeviceSerial(deviceSerial);
            towerLoop.setUploadTime(upload_time);
            towerLoop.setLiftingTime(liftingTime);
            towerLoop.setLiftingHei(liftingHei);
            towerLoop.setLiftingRange(liftingRange);
            towerLoop.setLiftingTurn(liftingTurn);
            towerLoop.setLiftingTime2(liftingTime2);
            towerLoop.setLiftingHei2(liftingHei2);
            towerLoop.setLiftingRange2(liftingRange2);
            towerLoop.setLiftingTurn2(liftingTurn2);
            towerLoop.setLiftingWei(liftingWei);
            towerLoop.setLiftingLoad(liftingLoad);
            towerLoop.setViolationReg(violationReg);

            // 多线程中无法自动注入Repository，手动从容器中获取
            TowerLoopRepository towerLoopRepository = SpringUtil.getBean(TowerLoopRepository.class);
            towerLoopRepository.save(towerLoop);
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析后台应答信息下传
     * 未定义
     * @param hexString
     */
    public void parseAnswer4Data(String hexString) {
        try {
            log.info("未解析！");
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }


}
