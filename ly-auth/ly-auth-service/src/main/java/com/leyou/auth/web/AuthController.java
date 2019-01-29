package com.leyou.auth.web;
/**
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: AuthController
 * @Author:   Administrator
 * @Date:     2019/1/8
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties porp;

    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password")String password,
                                      HttpServletResponse response, HttpServletRequest request){
        //denglu
        String token = authService.login(username,password);
        //写入cookie
        CookieUtils.setCookie(request,response,"LY_TOKEN",token,1800,null,true);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletResponse response, HttpServletRequest request){
        if (StringUtils.isBlank(token)){
            throw new LyException(ExceptionEnum.UN_AUTHORIZED);
        }
        try {
            UserInfo info = JwtUtils.getInfoFromToken(token,porp.getPublicKey());

            String newToken = JwtUtils.generateToken(info,porp.getPrivateKey(),porp.getExpire());
            //写入cookie
            CookieUtils.setCookie(request,response,"LY_TOKEN",newToken,1800,null,true);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UN_AUTHORIZED);
        }
    }
}
