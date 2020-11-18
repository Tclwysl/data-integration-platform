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
     * 升降机协议解析
     */
@Slf4j
public class Elevator {

    // 根据接口文档编制如下协议字段
    private final String ELEVATOR_HEAD = "A55A";                      /* 头部 */
    private final String ELEVATOR_TAIL = "CC33C33C";                  /* 尾部*/
    private final Integer ELEVATOR_DATA_LENGTH = 2;                   /* 长度字段字节数 */
    private final Integer ELEVATOR_DATA_CHECKSUM = 2;                 /* 校验字段字节数 */
    private final String ELEVATOR_COMMAND = "00";                     /* 自定义 */
    private final String ELEVATOR_COMMAND_REGISTER = "01";            /* 设备上报注册信息 */
    private final String ELEVATOR_COMMAND_REGISTER_RESPONSE = "02";   /* 平台返回注册信息 */
    private final String ELEVATOR_COMMAND_UPLOAD = "03";              /* 设备上报升降机信息 */
    private final String ELEVATOR_COMMAND_CALIBRATE = "04";           /* 标定信息 */
    private final String ELEVATOR_COMMAND_SPACING = "05";             /* 限位信息 */
    private final String ELEVATOR_COMMAND_REALTIME = "10";            /* 实时工况数据 */
    private final String ELEVATOR_COMMAND_WARNING = "11";             /* 报警信息 */
    private final String ELEVATOR_COMMAND_AUTHENTICATE = "12";        /* 人员身份认证信息 */

    /**
     * 判断接收到的数据是否是升降机数据
     * @param hexString
     * @return
     */
    public boolean isElevatorData(String hexString) {
        // 实际上是 "A55A\\S+CC33C33C\\S+$" 这样一个正则表达式
        String pattern = MessageFormat.format("{0}\\S+{2}\\S+$", ELEVATOR_HEAD, ELEVATOR_TAIL);
        return hexString.matches(pattern);
    }

    /**
     * 解析升降机数据. 只解析一条数据，结束位后面的字符串全部丢弃
     * @param hexString
     */
    public void parseElevatorData(String hexString) {
        try {
            // 实际上是 "(A55A\\S+CC33C33C)(.*)" 这样一个正则表达式
            String pattern = MessageFormat.format("({0}\\S+{1})(.*)", ELEVATOR_HEAD, ELEVATOR_TAIL);

            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);

            // 创建 matcher 对象
            Matcher m = r.matcher(hexString);
            if (m.find()) {
                String elevatorData = m.group(1);
                log.info("收到升降机数据：" + elevatorData);

                // 去头去尾
                String data = elevatorData.substring(ELEVATOR_HEAD.length());
                data = data.substring(0, data.length()-ELEVATOR_TAIL.length());

                // 取出命令字段
                String command = data.substring(0, ELEVATOR_COMMAND.length());
                data = data.substring(ELEVATOR_COMMAND.length());

                // 取出长度与校验和
                String length = data.substring(data.length()-ELEVATOR_DATA_LENGTH);
                String checksum = data.substring(data.length()-ELEVATOR_DATA_LENGTH-ELEVATOR_DATA_CHECKSUM, data.length()-ELEVATOR_DATA_LENGTH);
                data = data.substring(0, data.length()-ELEVATOR_DATA_LENGTH-ELEVATOR_DATA_CHECKSUM);

                // 纯数据，也就是需要解析的数据
                log.info("pure data: " + data);

                if (!isDataValid(data, length, checksum)) {
                    return;
                }

                if (command.equals(ELEVATOR_COMMAND_REGISTER)) {
                    parseRegisterData(data);
                } else if (command.equals(ELEVATOR_COMMAND_REGISTER_RESPONSE)) {
                    parseRegisterResponseData(data);
                } else if (command.equals(ELEVATOR_COMMAND_UPLOAD)) {
                    parseUploadData(data);
                } else if (command.equals(ELEVATOR_COMMAND_CALIBRATE)) {
                    parseCalibrateData(data);
                } else if (command.equals(ELEVATOR_COMMAND_SPACING)) {
                    parseSpacingData(data);
                } else if (command.equals(ELEVATOR_COMMAND_REALTIME)) {
                    parseRealTimeData(data);
                } else if (command.equals(ELEVATOR_COMMAND_WARNING)) {
                    parseWarningData(data);
                } else if (command.equals(ELEVATOR_COMMAND_AUTHENTICATE)) {
                    parseAuthenticateData(data);
                } else {
                    log.error("未识别的消息：" + hexString);
                }

            } else {
                log.error("升降机数据解析错误：" + hexString);
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
        if (iLength*2 != data.length() + ELEVATOR_COMMAND.length() + ELEVATOR_DATA_LENGTH + ELEVATOR_DATA_CHECKSUM) {
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
     * 解析设备上报注册信息   0x01
     * 测试数据    A55A01110000011207CC33C33C
     * 0XA55A01110000011207CC33C33C
     * @param hexString
     */
    public void parseRegisterData(String hexString) {
        try {

            //收到的16进制转字节流
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);

            // 吊笼编号

            Integer cage_num16 = (dataBytes[0] >> 1); //高7bit
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);  //低1bit
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);


            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNo);

            //数据采集时间
            Timestamp recordTIME = new Timestamp(System.currentTimeMillis());
            log.info("recordTIME: " + recordTIME.toString());

            ElevatorInfo elevatorInfo = new ElevatorInfo();
            elevatorInfo.setCageNum(cage_num);
            elevatorInfo.setCageLoc(cage_loc);
            elevatorInfo.setSerialNO(serialNo);
            elevatorInfo.setRecordTIME(recordTIME);

            // 多线程中无法自动注入Repository，手动从容器中获取
            ElevatorInfoRepository elevatorInfoRepository = SpringUtil.getBean(ElevatorInfoRepository.class);
            elevatorInfoRepository.save(elevatorInfo);

        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析平台返回注册信息帧   0x02
     * 测试数据 ： A55A02110000010110010101010101290FCC33C33C
     * @param hexString
     */
    public void parseRegisterResponseData(String hexString) {
        try {

            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            // 吊笼编号
            Integer cage_num16 = (dataBytes[0] >> 1);
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);

            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNo.toString());

            //注册结果
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes, 4, 5);
            Integer staTus = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("staTus: " + staTus.toString());
            //上报数据间隔
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes, 5, 6);
            Integer timeInterval1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            Double timeInterval = timeInterval1 * 0.1;
            log.info("timeInterval: " + timeInterval.toString());
            //时间
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 6, 7);//年
            Integer time_nn = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            String time_n = time_nn.toString();
            if(time_nn<10)
            {
                time_n="0" + time_n;
            }
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 7, 8);//月
            Integer time_yy = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            String time_y = time_yy.toString();
            if(time_yy<10)
            {
                time_y="0" + time_yy;
            }
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 8, 9);//日
            Integer time_rr = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            String time_r = time_rr.toString();
            if(time_rr<10)
            {
                time_r="0" + time_r;
            }
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 9, 10);//时
            Integer time_ss = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            String time_s = time_ss.toString();
            if(time_ss<10)
            {
                time_s="0" + time_s;
            }
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 10, 11);//分
            Integer time_ff = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            String time_f = time_ff.toString();
            if(time_ff<10)
            {
                time_f="0" + time_f;
            }
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 11, 12);//秒
            Integer time_mm = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            String time_m = time_mm.toString();
            if(time_mm<10)
            {
                time_m="0" + time_m;
            }
            Timestamp recordTIME=Timestamp.valueOf("20" + time_n + "-" + time_y + "-" + time_r + " " + time_s + ":" + time_f + ":" + time_m);
            log.info("recordTIME: " + recordTIME.toString());



            ElevatorReturnreg elevatorReturnreg = new ElevatorReturnreg();
            elevatorReturnreg.setCageNum(cage_num);
            elevatorReturnreg.setCageLoc(cage_loc);
            elevatorReturnreg.setSerialNO(serialNo);
            elevatorReturnreg.setStaTus(staTus);
            elevatorReturnreg.setTimeInterval(timeInterval);
            elevatorReturnreg.setRecordTIME(recordTIME);

            // 多线程中无法自动注入Repository，手动从容器中获取
            ElevatorReturnregRepository elevatorReturnregRepository = SpringUtil.getBean(ElevatorReturnregRepository.class);
            elevatorReturnregRepository .save(elevatorReturnreg);
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析设备上报升降机基本信息 0x03
     * 暂留
     * @param hexString
     */
    public void parseUploadData(String hexString) {

    }

    /**
     * 解析标定信息帧  0x04
     * 测试数据：A55A041100000111112222333344441111222233334444BA17CC33C33C
     * @param hexString
     */
    public void parseCalibrateData(String hexString) {
        try {
            //收到的16进制转字节流
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);

            // 吊笼编号
            Integer cage_num16 = (dataBytes[0] >> 1); //高7bit
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);  //低1bit
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);
            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNo);

            //重量空载AD值
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes, 4, 6);
            Integer weiemptyAd = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("weiemptyAd: " + weiemptyAd);

            //重量空载实际值
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes, 6, 8);
            Integer weiemptyActual = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            log.info("weiemptyActual: " + weiemptyActual);

            //重量负载AD值
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 8, 10);
            Integer weiloadAd = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            log.info("weiloadAd: " + weiloadAd);

            //重量负载实际值
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 10, 12);
            Integer weiloadActual = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            log.info("weiloadActual: " + weiloadActual);

            //高度底端AD值
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 12, 14);
            Integer heibotAd = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            log.info("heibotAd: " + heibotAd);

            //高度低端实际值
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 14, 16);
            Integer heibotActual = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            log.info("heibotActual: " + heibotActual);

            //高度顶端AD值
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 16, 18);
            Integer heitopAd = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            log.info("heitopAd: " + heitopAd);

            //高度顶端实际值
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 18, 20);
            Integer heitopActual = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            log.info("heitopActual: " + heitopActual);

            //数据采集时间
            Timestamp recordTIME = new Timestamp(System.currentTimeMillis());
            log.info("recordTIME: " + recordTIME.toString());

            ElevatorCaliInfo elevatorCaliInfo = new ElevatorCaliInfo();
            elevatorCaliInfo.setCageNum(cage_num);
            elevatorCaliInfo.setCageLoc(cage_loc);
            elevatorCaliInfo.setSerialNO(serialNo);
            elevatorCaliInfo.setRecordTIME(recordTIME);
            elevatorCaliInfo.setWeiemptyAd(weiemptyAd);
            elevatorCaliInfo.setWeiemptyActual(weiemptyActual);
            elevatorCaliInfo.setWeiloadAd(weiloadAd);
            elevatorCaliInfo.setWeiloadActual(weiloadActual);
            elevatorCaliInfo.setHeibotAd(heibotAd);
            elevatorCaliInfo.setHeibotActual(heibotActual);
            elevatorCaliInfo.setHeitopAd(heitopAd);
            elevatorCaliInfo.setHeitopActual(heitopActual);

            // 多线程中无法自动注入Repository，手动从容器中获取
            ElevatorCaliInfoRepository elevatorCaliInfoRepository = SpringUtil.getBean(ElevatorCaliInfoRepository.class);
            elevatorCaliInfoRepository.save(elevatorCaliInfo);

        }catch (Exception exp) {
            log.error(exp.getMessage());
        }

    }

    /**
     * 解析限位信息帧 0x05
     * 测试数据：A55A051100000111112222333344445555666677778888DA17CC33C33C
     * @param hexString
     */
    public void parseSpacingData(String hexString) {
        try {
            //收到的16进制转字节流
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);

            // 吊笼编号

            Integer cage_num16 = (dataBytes[0] >> 1); //高7bit
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);  //低1bit
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);
            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNo);

            //最大起重预警
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes, 4, 6);
            Integer maxliftingWarning1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            Double maxliftingWarning = maxliftingWarning1 *0.1;
            log.info("maxliftingWarning: " + maxliftingWarning);

            //最大起重量报警
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes, 6, 8);
            Integer maxliftingCtp1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            Double maxliftingCtp = maxliftingCtp1 * 0.1;
            log.info("maxliftingCtp: " + maxliftingCtp);

            //最大起升高度
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 8, 10);
            Integer maxliftingHei1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            Double maxliftingHei = maxliftingHei1 * 0.1;
            log.info("maxliftingHei: " + maxliftingHei);

            //最大速度预警
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 10, 12);
            Integer maxspeedWarning1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            Double maxspeedWarning = maxliftingWarning1 * 0.1;
            log.info("maxspeedWarning: " + maxspeedWarning);

            //最大速度报警
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 12, 14);
            Integer maxspeedCtp1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            Double maxspeedCtp = maxspeedCtp1 * 0.1;
            log.info("maxspeedCtp: " + maxspeedCtp);

            //最大承载人数
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 14, 16);
            Integer maxloadPeo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            log.info("maxloadPeo: " + maxloadPeo);

            //倾斜预警值
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 16, 18);
            Integer tiltWarning1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            Double tiltWarning = tiltWarning1 * 0.01;
            log.info("tiltWarning: " + tiltWarning);

            //倾斜报警值
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 18, 20);
            Integer tiltCtp1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            Double tiltCtp = tiltCtp1 * 0.01;
            log.info("tiltCtp: " + tiltCtp);

            //数据采集时间
            Timestamp recordTIME = new Timestamp(System.currentTimeMillis());
            log.info("recordTIME: " + recordTIME.toString());

            ElevatorLimitInfo elevatorLimitInfo = new ElevatorLimitInfo();
            elevatorLimitInfo.setCageNum(cage_num);
            elevatorLimitInfo.setCageLoc(cage_loc);
            elevatorLimitInfo.setSerialNO(serialNo);
            elevatorLimitInfo.setRecordTIME(recordTIME);
            elevatorLimitInfo.setMaxliftingWarning(maxliftingWarning);
            elevatorLimitInfo.setMaxliftingCtp(maxliftingCtp);
            elevatorLimitInfo.setMaxliftingHei(maxliftingHei);
            elevatorLimitInfo.setMaxspeedWarning(maxspeedWarning);
            elevatorLimitInfo.setMaxspeedCtp(maxspeedCtp);
            elevatorLimitInfo.setMaxloadPeo(maxloadPeo);
            elevatorLimitInfo.setTiltWarning(tiltWarning);
            elevatorLimitInfo.setTiltCtp(tiltCtp);

            // 多线程中无法自动注入Repository，手动从容器中获取
            ElevatorLimitInfoRepository elevatorLimitInfoRepository = SpringUtil.getBean(ElevatorLimitInfoRepository.class);
            elevatorLimitInfoRepository.save(elevatorLimitInfo);

        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析实时工况数据   命令字0x10
     * 测试数据: A55A1011000001200910010101101020171819202122232411111111C41CCC33C33C
     * @param hexString
     */
    public void parseRealTimeData(String hexString) {
        try {
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            // 吊笼编号
            Integer cage_num16 = (dataBytes[0] >> 1); //高7bit
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);  //低1bit
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);
            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNO = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNO.toString());

            //时间
            byte[] copyBytes11 = Arrays.copyOfRange(dataBytes, 4, 5);//年
            Integer time_nn = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes11),16);
            String time_n = time_nn.toString();
            if(time_nn<10)
            {
                time_n="0" + time_n;
            }
            byte[] copyBytes12 = Arrays.copyOfRange(dataBytes, 5, 6);//月
            Integer time_yy = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes12),16);
            String time_y = time_yy.toString();
            if(time_yy<10)
            {
                time_y="0" + time_y;
            }
            byte[] copyBytes13 = Arrays.copyOfRange(dataBytes, 6, 7);//日
            Integer time_rr = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes13),16);
            String time_r = time_rr.toString();
            if(time_rr<10)
            {
                time_r="0" + time_r;
            }
            byte[] copyBytes14 = Arrays.copyOfRange(dataBytes, 7, 8);//时
            Integer time_ss = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes14),16);
            String time_s = time_ss.toString();
            if(time_ss<10)
            {
                time_s="0" + time_s;
            }
            byte[] copyBytes15 = Arrays.copyOfRange(dataBytes, 8, 9);//分
            Integer time_ff = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes15),16);
            String time_f = time_ff.toString();
            if(time_ff<10)
            {
                time_f="0" + time_f;
            }
            byte[] copyBytes16 = Arrays.copyOfRange(dataBytes, 9, 10);//秒
            Integer time_mm = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes16),16);
            String time_m = time_mm.toString();
            if(time_mm<10)
            {
                time_m="0" + time_m;
            }
            Timestamp recordTIME=Timestamp.valueOf("20" + time_n + "-" + time_y + "-" + time_r + " " + time_s + ":" + time_f + ":" + time_m);
            log.info("recordTIME: " + recordTIME.toString());

            //本次运行载重
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes, 10, 12);
            Integer loAdd1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes2),16);
            Double loAdd = loAdd1 * 0.1;
            log.info("loAdd:" + loAdd.toString());

            //本次运行最大载重百分比
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 12, 13);
            Integer loadPErcent1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            Double loadPErcent = loadPErcent1 * 0.1;
            log.info("loadPErcent:" + loadPErcent.toString());

            //实时人数
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 13, 14);
            Integer peoplenum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            log.info("peoplenum:" + peoplenum.toString());

            //实时高度
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 14, 16);
            Integer startHEight1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            Double startHEight = startHEight1 * 0.1;
            log.info("startHEight:" + startHEight.toString());

            //高度百分比
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 16, 17);
            Integer heightper1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            Double heightper = heightper1 * 0.1;
            log.info("heightper:" + heightper.toString());

            //实时速度
            //高6bit速度
            Integer speeD16 = (dataBytes[17] & 0x40);
            Integer speeDd = Integer.parseInt(speeD16.toString(),16);
            //低2bit方向
            Integer direcTionn16 = (dataBytes[17] & 0x04);
            Integer direcTionn = Integer.parseInt(direcTionn16.toString(),16);
            log.info("speeDd:" + speeDd + "; direcTion:" + direcTionn);

            //实时倾斜度
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 18, 20);
            Integer tilt1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            Double tilt = tilt1 * 0.01;
            log.info("tilt:" + tilt.toString());

            //倾斜百分比
            byte[] copyBytes9 = Arrays.copyOfRange(dataBytes, 20, 21);
            Integer tiltper1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes9),16);
            Double tiltper = tiltper1 * 0.01;
            log.info("tiltper:" + tiltper.toString());

            //驾驶员认证结果
            byte[] copyBytes10 = Arrays.copyOfRange(dataBytes, 21, 22);
            Integer deiver = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes10));
            log.info("deiver:" + deiver.toString());

            //门锁状态
            //门锁状态——前门
            Integer lock_qm16 = (dataBytes[22] & 0x01);
            Integer lock_qm = Integer.parseInt(lock_qm16.toString(),16);
            //门锁状态——后门
            Integer lock_hm16 = (dataBytes[22] & 0x02);
            Integer lock_hm = Integer.parseInt(lock_hm16.toString(),16);
            //门锁异常提示
            Integer lock_abn16 = (dataBytes[22] & 0x03);
            Integer lock_abn = Integer.parseInt(lock_abn16.toString(),16);
            log.info("lock_qm:" + lock_qm + " ; lock_hm:" + lock_hm + " ; lock_abn:" + lock_abn);

            //系统状态
            //系统状态——重量
            Integer status_zl16 = (dataBytes[23] & 0x03);
            Integer status_zl = Integer.parseInt(status_zl16.toString(),16);
            //系统状态——高度限位
            Integer status_gd16 = (dataBytes[23] & 0x0c);
            Integer status_gd = Integer.parseInt(status_gd16.toString(),16);
            //系统状态——超速
            Integer status_cs16 = (dataBytes[23] & 0x30);
            Integer status_cs = Integer.parseInt(status_cs16.toString(),16);
            //系统状态——人数
            Integer status_rs16 = (dataBytes[23] & 0xc0);
            Integer status_rs = Integer.parseInt(status_rs16.toString(),16);

            log.info("status_zl:" + status_zl + " ; status_gd:" + status_gd + " ; status_cs:" + status_cs + " ; status_rs:" + status_rs);
            //系统状态——倾斜
            Integer status_qx16 = (dataBytes[24] & 0x03);
            Integer status_qx = Integer.parseInt(status_qx16.toString(),16);
            //系统状态——前门锁
            Integer status_qm16 = (dataBytes[24] & 0x04);
            Integer status_qm = Integer.parseInt(status_qm16.toString(),16);
            //系统状态——后门所
            Integer status_hm16 = (dataBytes[24] & 0x08);
            Integer status_hm = Integer.parseInt(status_hm16.toString(),16);
            //系统状态——拆除
            Integer status_cc16 = (dataBytes[24] & 0x10);
            Integer status_cc = Integer.parseInt(status_cc16.toString(),16);

            log.info("status_qx:" + status_qx + " ; status_qm:" + status_qm + " ; status_hm:" + status_hm + " ; status_cc:" + status_cc);


            Workingcondition workingcondition = new Workingcondition();
            workingcondition.setCageNum(cage_num);
            workingcondition.setCageLoc(cage_loc);
            workingcondition.setSerialNO(serialNO);
            workingcondition.setRecordTIME(recordTIME);
            workingcondition.setLoAdd(loAdd);
            workingcondition.setLoadPErcent(loadPErcent);
            workingcondition.setPeoplenum(peoplenum);
            workingcondition.setStartHEight(startHEight);
            workingcondition.setHeightper(heightper);
            workingcondition.setSpeeDd(speeDd);
            workingcondition.setDirecTionn(direcTionn);
            workingcondition.setTilt(tilt);
            workingcondition.setTiltper(tiltper);
            workingcondition.setDeiver(deiver);
            workingcondition.setLockqm(lock_qm);
            workingcondition.setLockhm(lock_hm);
            workingcondition.setLockabn(lock_abn);
            workingcondition.setStatuszl(status_zl);
            workingcondition.setStatusgd(status_gd);
            workingcondition.setStatuscs(status_cs);
            workingcondition.setStatusrs(status_rs);
            workingcondition.setStatusqx(status_qx);
            workingcondition.setStatusqm(status_qm);
            workingcondition.setStatushm(status_hm);
            workingcondition.setStatuscc(status_cc);

            // 多线程中无法自动注入Repository，手动从容器中获取
            WorkingconditionRepository workingconditionRepository = SpringUtil.getBean(WorkingconditionRepository.class);
            workingconditionRepository.save(workingcondition);


        }catch (Exception exp) {
            log.error(exp.getMessage());
        }


    }

    /**
     * 解析报警信息0x11
     * 测试数据：A55A1111000001050201050201111122334444556677778899112233511CCC33C33C
     * @param hexString
     */
    public void parseWarningData(String hexString){
        try {
            String recordTIME1 = null;
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);
            // 吊笼编号
            Integer cage_num16 = (dataBytes[0] >> 1);
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);
            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNo.toString());

            //时间
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 4, 5);//年
            Integer time_n1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes3),16);
            String time_n = time_n1.toString();
            if(time_n1<10)
            {
                time_n="0" + time_n;
            }
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 5, 6);//月
            Integer time_y1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            String time_y = time_y1.toString();
            if(time_y1<10)
            {
                time_y="0" + time_y;
            }
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 6, 7);//日
            Integer time_r1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes5),16);
            String time_r = time_r1.toString();
            if(time_r1<10)
            {
                time_r="0" + time_r;
            }
            byte[] copyBytes6 = Arrays.copyOfRange(dataBytes, 7, 8);//时
            Integer time_s1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes6),16);
            String time_s = time_s1.toString();
            if(time_s1<10)
            {
                time_s="0" + time_s;
            }
            byte[] copyBytes7 = Arrays.copyOfRange(dataBytes, 8, 9);//分
            Integer time_f1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes7),16);
            String time_f = time_f1.toString();
            if(time_f1<10)
            {
                time_f="0" + time_f;
            }
            byte[] copyBytes8 = Arrays.copyOfRange(dataBytes, 9, 10);//秒
            Integer time_m1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes8),16);
            String time_m = time_m1.toString();
            if(time_m1<10)
            {
                time_m="0" + time_m;
            }
            recordTIME1 = ("20" + time_n + "-" + time_y + "-" + time_r + " " + time_s + ":" + time_f + ":" + time_m).trim();
            Timestamp recordTIME = Timestamp.valueOf(recordTIME1);
            log.info("recordTIME: " + recordTIME);

            //实时起重量
            byte[] copyBytes9 = Arrays.copyOfRange(dataBytes, 10, 12);
            Integer realLifting1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes9),16);
            Double realLifting = realLifting1 * 0.1;
            log.info("realLifting: " + realLifting.toString());

            //重量百分比
            byte[] copyBytes10 = Arrays.copyOfRange(dataBytes, 12, 13);
            Integer weightPer1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes10),16);
            Double weightPer = weightPer1 * 0.1;
            log.info("weightPer: " + weightPer.toString());

            //实时人数
            byte[] copyBytes11 = Arrays.copyOfRange(dataBytes, 13, 14);
            Integer realPeoplenum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes11),16);
            log.info("realPeoplenum: " + realPeoplenum.toString());

            //实时高度
            byte[] copyBytes12 = Arrays.copyOfRange(dataBytes, 14, 16);
            Integer realHeight1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes12),16);
            Double realHeight = realHeight1 * 0.1;
            log.info("realHeight: " + realHeight.toString());

            //高度百分比
            byte[] copyBytes13 = Arrays.copyOfRange(dataBytes, 16, 17);
            Integer realHeightper1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes13),16);
            Double realHeightper = realHeightper1 * 0.1;
            log.info("realHeightper: " + realHeightper.toString());

            //实时速度-速度
            Integer realSpeed16 = (dataBytes[17] & 0x40);
            Integer realSpeed = Integer.parseInt(realSpeed16.toString(),16);
            //实时速度-方向
            Integer tiltDirection16 = (dataBytes[17] & 0x04);
            Integer tiltDirection = Integer.parseInt(tiltDirection16.toString(),16);
            log.info("realSpeed: " + realSpeed.toString() + " ； tiltDirection: " + tiltDirection.toString());


            //实时倾斜度
            byte[] copyBytes16 = Arrays.copyOfRange(dataBytes, 17, 19);
            Integer realTilt1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes16),16);
            Double realTilt = realTilt1 * 0.01;
            log.info("realTilt: " + realTilt.toString());

            //倾斜百分比
            byte[] copyBytes17 = Arrays.copyOfRange(dataBytes, 19, 20);
            Integer tiltPer1 = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes17),16);
            Double tiltPer = tiltPer1 * 0.01;
            log.info("tiltPer: " + tiltPer.toString());

            //驾驶员身份认证结果
            byte[] copyBytes18 = Arrays.copyOfRange(dataBytes, 20, 21);
            Integer driverStatus = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes18),16);
            log.info("driverStatus: " + driverStatus.toString());

            //门锁状态-前门
            Integer lockstatusQm16 = (dataBytes[21] & 0x01);
            Integer lockstatusQm = Integer.parseInt(lockstatusQm16.toString(),16);
            //门锁状态-后门
            Integer lockstatusHm16 = (dataBytes[21] & 0x02);
            Integer lockstatusHm = Integer.parseInt(lockstatusHm16.toString(),16);
            //门锁异常提示
            Integer lockAbnpro16 = (dataBytes[21] & 0x04);
            Integer lockAbnpro = Integer.parseInt(lockAbnpro16.toString(),16);
            log.info("lockstatusQm: " + lockstatusQm.toString() + "; lockstatusHm: " + lockstatusHm.toString() + " ; lockAbnpro: " + lockAbnpro.toString());

            //报警原因
            byte[] copyBytes22 = Arrays.copyOfRange(dataBytes, 22, 23);
            Integer alarmCause = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes22),16);
            log.info("alarmCause: " + alarmCause.toString());
            //级别
            byte[] copyBytes23 = Arrays.copyOfRange(dataBytes, 23, 24);
            Integer alarmLevel = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes23),16);
            log.info("alarmLevel: " + alarmLevel.toString());

            ElevatorAlarmInfo elevatorAlarmInfo = new ElevatorAlarmInfo();
            elevatorAlarmInfo.setCageNum(cage_num);
            elevatorAlarmInfo.setCageLoc(cage_loc);
            elevatorAlarmInfo.setSerialNO(serialNo);
            elevatorAlarmInfo.setRecordTIME(recordTIME);
            elevatorAlarmInfo.setRealLifting(realLifting);
            elevatorAlarmInfo.setWeightPer(weightPer);
            elevatorAlarmInfo.setRealPeoplenum(realPeoplenum);
            elevatorAlarmInfo.setRealHeight(realHeight);
            elevatorAlarmInfo.setRealHeightper(realHeightper);
            elevatorAlarmInfo.setRealSpeed(realSpeed);
            elevatorAlarmInfo.setTiltDirection(tiltDirection);
            elevatorAlarmInfo.setRealTilt(realTilt);
            elevatorAlarmInfo.setTiltPer(tiltPer);
            elevatorAlarmInfo.setDriverStatus(driverStatus);
            elevatorAlarmInfo.setLockstatusQm(lockstatusQm);
            elevatorAlarmInfo.setLockstatusHm(lockstatusHm);
            elevatorAlarmInfo.setLockAbnpro(lockAbnpro);
            elevatorAlarmInfo.setAlarmCause(alarmCause);
            elevatorAlarmInfo.setAlarmLevel(alarmLevel);

            // 多线程中无法自动注入Repository，手动从容器中获取
            ElevatorAlarmInfoRepository elevatorAlarmInfoRepository = SpringUtil.getBean(ElevatorAlarmInfoRepository.class);
            elevatorAlarmInfoRepository .save(elevatorAlarmInfo);
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 解析人员身份认证信息0x12
     * 测试数据:A55A121100000101e4b880e4ba8ce4b889e59b9be4ba94e585ade4b883e585abe4b99de58d81616263646566676899333730383239313939393038333630323334B341CC33C33C
     * @param hexString
     */
    public void parseAuthenticateData(String hexString) {
        try {

            //收到的16进制转字节流
            byte[] dataBytes = DataTranslate.hexStringToBytes(hexString);

            // 吊笼编号
            Integer cage_num16 = (dataBytes[0] >> 1); //高7bit
            Integer cage_num = Integer.parseInt(cage_num16.toString(),16);
            Integer cage_loc16 = (dataBytes[0] & 0x01);  //低1bit
            Integer cage_loc = Integer.parseInt(cage_loc16.toString(),16);
            log.info("cage_num: " + cage_num + "; cage_loc: " + cage_loc);

            // 设备编号
            byte[] copyBytes = Arrays.copyOfRange(dataBytes, 1, 4);
            Integer serialNo = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes),16);
            log.info("serialNO: " + serialNo);

            //识别结果状态值
            byte[] copyBytes1 = Arrays.copyOfRange(dataBytes, 4, 5);
            Integer distStatus = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes1),16);
            log.info("distStatus: " + distStatus);

            //用户名
            byte[] copyBytes2 = Arrays.copyOfRange(dataBytes, 5, 35);
            String driverName = DataTranslate.getASCII(copyBytes2);
            log.info("driverName: " + driverName);

            //用户ID
            byte[] copyBytes3 = Arrays.copyOfRange(dataBytes, 35, 43);
            String userId = DataTranslate.getASCII(copyBytes3);
            log.info("userId: " + userId);

            //识别分数
            byte[] copyBytes4 = Arrays.copyOfRange(dataBytes, 43, 44);
            Integer distNum = Integer.parseInt(DataTranslate.bytesToHexString(copyBytes4),16);
            log.info("distNum: " + distNum);

            //身份证号码
            byte[] copyBytes5 = Arrays.copyOfRange(dataBytes, 44, 62);
            String driverId = DataTranslate.getASCII(copyBytes5);
            log.info("driverId: " + driverId);

            //数据采集时间
            Timestamp recordTIME = new Timestamp(System.currentTimeMillis());
            log.info("recordTIME: " + recordTIME.toString());

            ElevatorIdentity elevatorIdentity = new ElevatorIdentity();
            elevatorIdentity.setCageNum(cage_num);
            elevatorIdentity.setCageLoc(cage_loc);
            elevatorIdentity.setSerialNO(serialNo);
            elevatorIdentity.setRecordTIME(recordTIME);
            elevatorIdentity.setDistStatus(distStatus);
            elevatorIdentity.setDriverName(driverName);
            elevatorIdentity.setUserId(userId);
            elevatorIdentity.setDistNum(distNum);
            elevatorIdentity.setDriverId(driverId);

            // 多线程中无法自动注入Repository，手动从容器中获取
            ElevatorIdentityRepository elevatorIdentityRepository = SpringUtil.getBean(ElevatorIdentityRepository.class);
            elevatorIdentityRepository.save(elevatorIdentity);

        }catch (Exception exp){
            log.error(exp.getMessage());
        }
    }
}
