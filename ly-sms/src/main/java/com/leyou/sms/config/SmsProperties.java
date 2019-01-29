package com.leyou.sms.config;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: SmsProperties
 * @Author:   Administrator
 * @Date:     2019/1/6
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {

    Integer appid;
    String appkey;
    Integer templateId;
    String smsSign;
}
