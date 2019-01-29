package com.leyou.auth;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: JwtTest
 * @Author:   Administrator
 * @Date:     2019/1/8
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-26 15:43
 **/
public class JwtTest {

    private static final String pubKeyPath = "E:\\Git1\\rsa\\ras.pub";

    private static final String priKeyPath = "E:\\Git1\\rsa\\ras.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    //@Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU0Njk1MjUwNn0.CVeiH7_d0bf4bcMJwrHO1SAfatGr24ZJ_1L3FlqmaVxhm3q_Mz6-3TuQUxs7ui77YNhOY2jDWoiUK-duaZEVgo6l3PRRfKcWYr2vFr1hURUrkonffI4OR9Opu3xIGGcHzzuwIVT_Qpmzqo4chtNHtqjkkV-u784OdWy0F7jB2fA";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
