package com.leyou.gateway.conf;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: JwtProperties
 * @Author:   Administrator
 * @Date:     2019/1/8
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        //生成公钥和是要
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
