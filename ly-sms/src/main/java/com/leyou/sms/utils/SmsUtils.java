package com.leyou.sms.utils;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: SmsUtils
 * @Author:   Administrator
 * @Date:     2019/1/6
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {

    private static final String KEY_PREFIX = "sms:phone:";

    @Autowired
    private SmsProperties properties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public synchronized SmsSingleSenderResult sendSms(String phoneNumber,String[] params){
        SmsSingleSenderResult result = null;
        String key = KEY_PREFIX + phoneNumber;

        String lastTime = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(lastTime)){
            Long last = Long.valueOf(lastTime);
            //判断一分钟前有没有发送过短信
            if(System.currentTimeMillis() - last < 60*1000){
                return null;
            }
        }
        try {
            //数组具体的元素个数和模板中变量个数必须一致，例如事例中templateId:5678对应一个变量，参数数组中元素个数也必须是一个
            SmsSingleSender ssender = new SmsSingleSender(properties.getAppid(),properties.getAppkey() );
            // 签名参数未提供或者为空时，会使用默认签名发送短信
            result = ssender.sendWithParam("86", phoneNumber,
                        properties.getTemplateId(), params, properties.getSmsSign(), "", "");
            System.out.println(result);

            if(result.result != 0){
                log.info("[短信服务] 发送短信失败, phoneNumber:{}, 原因:{}",phoneNumber, result.errMsg);
            }

        } catch (HTTPException e) {
            // HTTP响应码错误
            e.printStackTrace();
        } catch (JSONException e) {
            // json解析错误
            e.printStackTrace();
        } catch (IOException e) {
            // 网络IO错误
            e.printStackTrace();
        }
        redisTemplate.opsForValue().set(key,String.valueOf(System.currentTimeMillis()),1,TimeUnit.MINUTES);
        return result;
    }

}
