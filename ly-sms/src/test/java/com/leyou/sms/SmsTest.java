package com.leyou.sms;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: SmsTest
 * @Author:   Administrator
 * @Date:     2019/1/6
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testSend() throws InterruptedException {
        Map<String,Object> map = new HashMap<>();
        map.put("phone","18861852641");
        String[] p = {"4567","5"};
        map.put("params",p);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",map);

        Thread.sleep(10000);
    }
}
