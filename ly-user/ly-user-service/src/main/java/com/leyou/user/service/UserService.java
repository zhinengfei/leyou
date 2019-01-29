package com.leyou.user.service;/*
 * Copyright (C), 2016-2018, 南京园立方信息科技有限公司
 * @ClassName: UserService
 * @Author:   Administrator
 * @Date:     2018/12/15 21:06
 * @Description: TODO
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    private final String KEY_PREFIX = "user:verify:phone:";

    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user) == 0;
    }

    public void sendCode(String phone) {
        //生成key
        String key = KEY_PREFIX + phone;
        //生成验证码
        String code = NumberUtils.generateCode(6);

        Map<String,Object> map = new HashMap<>();
        map.put("phone",phone);
        String[] p = {code,"5"};
        map.put("params",p);

        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",map);
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        //获取验证码
        String catchCode = (String) redisTemplate.opsForValue().get(KEY_PREFIX+user.getPhone());
        //效验
        if(!StringUtils.equals(code,catchCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        user.setSalt(salt);
        //添加
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    public User  queryUserByUsernameAndPassword(String username,String password){
        User user = new  User();
        user.setUsername(username);
        //效验
        User u = userMapper.selectOne(user);
        if(u == null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        if(!StringUtils.equals(u.getPassword(),CodecUtils.md5Hex(password,u.getSalt()))){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //验证成功
        return u;
    }
}
