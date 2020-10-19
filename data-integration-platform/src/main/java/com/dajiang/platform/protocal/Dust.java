package com.dajiang.platform.protocal;

import com.dajiang.platform.domain.DustPla;//扬尘平台
import com.dajiang.platform.repository.DustPlaRepository;
import com.dajiang.platform.utils.DataTranslate;
import com.dajiang.platform.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Arc2D;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 扬尘协议解析
 */
@Slf4j
public class Dust {

    // 根据接口文档编制如下协议字段
    private final String ELEVATOR_HEAD = "#,";                      /* 头部 */
    private final String ELEVATOR_TAIL = "#";                  /* 尾部*/

    /**
     * 判断接收到的数据是否是扬尘数据
     * @param dustdata
     * @return
     */
    public boolean isDustData(String dustdata) {
        // 实际上是 "#\\S+#\\S+$" 这样一个正则表达式
        String pattern = MessageFormat.format("{0}\\S+{2}\\S+$", ELEVATOR_HEAD, ELEVATOR_TAIL);
        return dustdata.matches(pattern);
    }

    /**
     * 解析扬尘平台数据. 只解析一条数据，结束位后面的字符串全部丢弃
     * @param dustdata
     */
    public void parseDustData(String dustdata) {
        try {
            // 实际上是 "#\\S+#\\S+$" 这样一个正则表达式
            String pattern = MessageFormat.format("({0}\\S+{1})(.*)", ELEVATOR_HEAD, ELEVATOR_TAIL);

            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);

            // 创建 matcher 对象
            Matcher m = r.matcher(dustdata);
            if (m.find()) {
                String elevatorData = m.group(1);
                log.info("收到扬尘数据：" + elevatorData);

                // 去头去尾
                String data = elevatorData.substring(ELEVATOR_HEAD.length());
                data = data.substring(0, data.length()-ELEVATOR_TAIL.length());

                // 纯数据，也就是需要解析的数据
                log.info("pure data: " + data);

                if (!isDataValid(data)) {
                    return;
                } else if (isDataValid(data)) {
                    parseDustPlatformData(data);
                } else {
                    log.error("未识别的消息：" + dustdata);
                }

            } else {
                log.error("扬尘平台数据解析错误：" + dustdata);
            }

        } catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }

    /**
     * 判断数据是否合规
     * @param data      纯数据
     * @return
     */
    public boolean isDataValid(String data) {
        // 长度检验
        Integer iLength = 8;
        if (iLength != data.split(",").length) {
            log.error("data length error.");
            return false;
        }
        return true;
    }


    /**
     * 解析扬尘平台数据
     * 测试数据:  （文本）        #,11.1,22.2,33.3,44.4,55.5,66.6,77.7,88.8#
     * 测试数据：（Hex）
     * @param data
     */
    public void parseDustPlatformData(String data) {
        data =data.trim();
        String dataTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
        Timestamp time = Timestamp.valueOf(dataTime);	//时间
        log.info("time:" + time);
        String temp = null;
        String humi = null;
        String windsp = null;
        String windd = null;
        String atmo = null;
        String pm2_5 = null;
        String pm10 = null;
        String nois = null;
        try{
            for (int i = 0; i < data.split(",").length; i++) {
                switch (i) {
                    case 0:
                        temp = data.split(",")[i];
                        break;
                    case 1:
                        humi= data.split(",")[i];
                        break;
                    case 2:
                        windsp = data.split(",")[i];
                        break;
                    case 3:
                        windd = data.split(",")[i];

                        break;
                    case 4:
                        atmo = data.split(",")[i];

                        break;
                    case 5:
                        pm2_5 = data.split(",")[i];

                        break;
                    case 6:
                        pm10 = data.split(",")[i];

                        break;
                    case 7:
                        nois = data.split(",")[i];

                        break;
                }
            }
            Float temperaTure = Float.parseFloat(temp);
            log.info("temperaTure:" + temperaTure);

            Float humiDity = Float.parseFloat(humi);
            log.info("humiDity:" + humiDity);

            Float windSpeed = Float.parseFloat(windsp);
            log.info("windSpeed:" + windSpeed);

            Float windDirection = Float.parseFloat(windd);
            log.info("windDirection:" + windDirection);

            Float atmoSphere = Float.parseFloat(atmo);
            log.info("atmoSphere:" + atmoSphere);

            Float pM25 = Float.parseFloat(pm2_5);
            log.info("pM25:" + pM25);

            Float pM10 = Float.parseFloat(pm10);
            log.info("pM10:" + pM10);

            Float noIse = Float.parseFloat(nois);
            log.info("noIse:" + noIse);

            DustPla dustPla = new DustPla();
            dustPla.setTiMe(time);
            dustPla.setTemperaTure(temperaTure);
            dustPla.setHumiDity(humiDity);
            dustPla.setWindSpeed(windSpeed);
            dustPla.setWindDirection(windDirection);
            dustPla.setAtmoSphere(atmoSphere);
            dustPla.setPM25(pM25);
            dustPla.setPM10(pM10);
            dustPla.setNoIse(noIse);

            // 多线程中无法自动注入Repository，手动从容器中获取
            DustPlaRepository dustPlaRepository = SpringUtil.getBean(DustPlaRepository.class);
            dustPlaRepository.save(dustPla);
        }catch (Exception exp) {
            log.error(exp.getMessage());
        }
    }
}
