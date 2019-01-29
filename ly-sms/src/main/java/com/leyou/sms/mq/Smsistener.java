package com.leyou.sms.mq;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: ItemListener
 * @Author:   Administrator
 * @Date:     2018/12/14 21:01
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Smsistener {

    @Autowired
    private SmsUtils smsUtils;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange",type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"
    ))
    public void listenerUpdateOrInsert(Map<String,Object> msg){
        String phoneNumber = msg.get("phone").toString();
        String[] params = (String[])msg.get("params");
        if(StringUtils.isBlank(phoneNumber) || params.length == 0){
            return;
        }
        smsUtils.sendSms(phoneNumber,params);
    }

}
