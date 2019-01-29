package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/** Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: AuthService
 * @Author:   shentiepeng
 * @Date:     2019/1/8
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties porp;

    public String login(String username,String password){
        try {
            User user = userClient.queryUserByUsernameAndPassword(username, password);
            if(user == null){
                throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
            }
            return JwtUtils.generateToken(new UserInfo(user.getId(), username), porp.getPrivateKey(), porp.getExpire());
        } catch (Exception e) {
            log.error("[授权中心]用户名或密码错误,用户名称{}",username,e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }
}
